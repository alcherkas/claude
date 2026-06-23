data "aws_caller_identity" "current" {}

data "aws_region" "current" {}

locals {
  service_name = "restaurant-service"

  # The java-service module creates the ECR repo named after the service; this is the URI the
  # pushed image lands at (<account>.dkr.ecr.<region>.amazonaws.com/<name>).
  image_uri = "${data.aws_caller_identity.current.account_id}.dkr.ecr.${data.aws_region.current.name}.amazonaws.com/${local.service_name}:${var.image_tag}"
}

# Shared platform foundation (VPC, ECS cluster, ALB listener, subnets) exposed
# by platform-infra via remote-state outputs.
data "terraform_remote_state" "platform" {
  backend = "s3"
  config = {
    bucket = "quickbite-tfstate-${var.env}"
    key    = "platform-infra/${var.env}/terraform.tfstate"
    region = "us-east-1"
  }
}

# Dedicated Postgres instance for restaurant_db.
# NOTE: in a real polyrepo the source is a pinned git ref, e.g.
#   git::https://github.com/quickbite/terraform-modules.git//modules/postgres-db?ref=v1.0.0
module "db" {
  source = "../../terraform-modules/modules/postgres-db"

  name           = "restaurant"
  engine_version = "16.3"
  instance_class = "db.t4g.micro"
  vpc_id         = data.terraform_remote_state.platform.outputs.vpc_id
  subnet_ids     = data.terraform_remote_state.platform.outputs.private_subnet_ids

  allowed_security_group_ids = [module.service.security_group_id]
}

# ECS Fargate service behind the shared ALB.
# NOTE: in a real polyrepo the source is a pinned git ref, e.g.
#   git::https://github.com/quickbite/terraform-modules.git//modules/java-service?ref=v1.0.0
module "service" {
  source = "../../terraform-modules/modules/java-service"

  name           = local.service_name
  image          = local.image_uri
  container_port = var.container_port
  path_prefix    = "/api/restaurants"
  health_path    = "/actuator/health"
  desired_count  = var.desired_count

  vpc_id             = data.terraform_remote_state.platform.outputs.vpc_id
  private_subnet_ids = data.terraform_remote_state.platform.outputs.private_subnet_ids
  ecs_cluster_arn    = data.terraform_remote_state.platform.outputs.ecs_cluster_arn
  alb_listener_arn   = data.terraform_remote_state.platform.outputs.alb_listener_arn

  # Wired from the postgres-db secret (DB_URL/DB_USERNAME/DB_PASSWORD).
  db_secret_arn = module.db.secret_arn

  # Mirrors application.yml. DB_* are injected as secrets via db_secret_arn.
  environment = {
    SERVER_PORT             = tostring(var.container_port)
    CLIENTS_IDENTITY_URL    = var.identity_url
    KAFKA_BOOTSTRAP_SERVERS = var.kafka_bootstrap_servers
  }
}
