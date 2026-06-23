data "aws_caller_identity" "current" {}

data "aws_region" "current" {}

locals {
  port        = 8094
  path_prefix = "/api/reviews"

  # The java-service module creates the ECR repo named after the service; this is the URI the
  # pushed image lands at (<account>.dkr.ecr.<region>.amazonaws.com/<name>).
  image_uri = "${data.aws_caller_identity.current.account_id}.dkr.ecr.${data.aws_region.current.name}.amazonaws.com/review-service:${var.image_tag}"
}

# Shared platform foundation (VPC, ECS cluster, ALB, ECR, RDS host, etc.)
data "terraform_remote_state" "platform" {
  backend = "s3"
  config = {
    bucket = "quickbite-tfstate-${var.env}"
    key    = "platform-infra/${var.env}/terraform.tfstate"
    region = "us-east-1"
  }
}

# Dedicated Postgres database (review_db) for this stateful service.
# In a real polyrepo the source is a pinned git ref:
#   git::https://github.com/quickbite/terraform-modules.git//modules/postgres-db?ref=v1.0.0
module "db" {
  source = "../../terraform-modules/modules/postgres-db"

  name                       = "review"
  engine_version             = "16.3"
  instance_class             = "db.t4g.micro"
  allocated_storage          = 20
  vpc_id                     = data.terraform_remote_state.platform.outputs.vpc_id
  subnet_ids                 = data.terraform_remote_state.platform.outputs.private_subnet_ids
  allowed_security_group_ids = [module.service.security_group_id]
}

# ECS Fargate service behind the shared ALB.
#   git::https://github.com/quickbite/terraform-modules.git//modules/java-service?ref=v1.0.0
module "service" {
  source = "../../terraform-modules/modules/java-service"

  name               = "review-service"
  image              = local.image_uri
  container_port     = local.port
  cpu                = var.cpu
  memory             = var.memory
  desired_count      = var.desired_count
  vpc_id             = data.terraform_remote_state.platform.outputs.vpc_id
  private_subnet_ids = data.terraform_remote_state.platform.outputs.private_subnet_ids
  ecs_cluster_arn    = data.terraform_remote_state.platform.outputs.ecs_cluster_arn
  alb_listener_arn   = data.terraform_remote_state.platform.outputs.alb_listener_arn
  path_prefix        = local.path_prefix
  health_path        = "/actuator/health"
  db_secret_arn      = module.db.secret_arn

  # Mirrors application.yml; service-discovery URLs use internal Cloud Map DNS.
  # DB_URL/DB_USERNAME/DB_PASSWORD are injected as container secrets from db_secret_arn.
  environment = {
    SERVER_PORT = tostring(local.port)

    CLIENTS_ORDER_URL      = "http://order-service.quickbite.internal:8088"
    CLIENTS_RESTAURANT_URL = "http://restaurant-service.quickbite.internal:8082"
    CLIENTS_IDENTITY_URL   = "http://identity-service.quickbite.internal:8081"
  }
}
