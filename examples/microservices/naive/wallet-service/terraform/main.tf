data "aws_caller_identity" "current" {}

data "aws_region" "current" {}

locals {
  # The java-service module creates the ECR repo named after the service; this is the URI the
  # pushed image lands at (<account>.dkr.ecr.<region>.amazonaws.com/<name>).
  image_uri = "${data.aws_caller_identity.current.account_id}.dkr.ecr.${data.aws_region.current.name}.amazonaws.com/wallet-service:${var.image_tag}"
}

# Read shared platform foundation (VPC, subnets, ECS cluster, ALB) from platform-infra.
data "terraform_remote_state" "platform" {
  backend = "s3"
  config = {
    bucket = "quickbite-tfstate-${var.env}"
    key    = "platform-infra/${var.env}/terraform.tfstate"
    region = "us-east-1"
  }
}

# Postgres database for wallet-service (wallet_db).
# In a real polyrepo this source is pinned:
#   git::https://github.com/quickbite/terraform-modules.git//modules/postgres-db?ref=v1.0.0
module "db" {
  source = "../../terraform-modules/modules/postgres-db"

  name                       = "wallet"
  engine_version             = "16.3"
  instance_class             = "db.t4g.micro"
  allocated_storage          = 20
  vpc_id                     = data.terraform_remote_state.platform.outputs.vpc_id
  subnet_ids                 = data.terraform_remote_state.platform.outputs.private_subnet_ids
  allowed_security_group_ids = [module.service.security_group_id]
}

# ECS Fargate service behind the shared ALB.
# In a real polyrepo this source is pinned:
#   git::https://github.com/quickbite/terraform-modules.git//modules/java-service?ref=v1.0.0
module "service" {
  source = "../../terraform-modules/modules/java-service"

  name               = "wallet-service"
  image              = local.image_uri
  container_port     = var.container_port
  cpu                = var.cpu
  memory             = var.memory
  desired_count      = var.desired_count
  vpc_id             = data.terraform_remote_state.platform.outputs.vpc_id
  private_subnet_ids = data.terraform_remote_state.platform.outputs.private_subnet_ids
  ecs_cluster_arn    = data.terraform_remote_state.platform.outputs.ecs_cluster_arn
  alb_listener_arn   = data.terraform_remote_state.platform.outputs.alb_listener_arn
  path_prefix        = "/api/wallets"
  health_path        = "/actuator/health"
  db_secret_arn      = module.db.secret_arn

  # Mirrors application.yml — values overridable per environment.
  # DB_URL/DB_USERNAME/DB_PASSWORD are injected as container secrets from db_secret_arn.
  environment = {
    SERVER_PORT  = tostring(var.container_port)
    IDENTITY_URL = var.identity_url
  }
}
