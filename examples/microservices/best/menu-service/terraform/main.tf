locals {
  name           = "menu-service"
  container_port = 8083
  path_prefix    = "/api/menu"
  image          = "${var.image_registry}/menu-service:${var.image_tag}"

  # Mirrors application.yml. Service-discovery URLs use internal Cloud Map DNS.
  environment = {
    SERVER_PORT             = tostring(local.container_port)
    SPRING_APPLICATION_NAME = local.name
    CLIENTS_RESTAURANT_URL  = "http://restaurant-service.quickbite.internal:8082"
    KAFKA_BOOTSTRAP_SERVERS = var.kafka_bootstrap_servers
    KAFKA_TOPIC_MENU_EVENTS = "menu.events"
  }
}

# Shared platform foundation (VPC, ECS cluster, ALB listener, subnets).
data "terraform_remote_state" "platform" {
  backend = "s3"
  config = {
    bucket = var.tfstate_bucket
    key    = "platform-infra/${var.env}/terraform.tfstate"
    region = "us-east-1"
  }
}

# Postgres (menu_db) for this stateful service.
# In a real polyrepo this source is pinned:
#   git::https://github.com/quickbite/terraform-modules.git//modules/postgres-db?ref=v1.0.0
module "db" {
  source = "../../terraform-modules/modules/postgres-db"

  name                       = "menu"
  vpc_id                     = data.terraform_remote_state.platform.outputs.vpc_id
  subnet_ids                 = data.terraform_remote_state.platform.outputs.private_subnet_ids
  allowed_security_group_ids = [module.service.security_group_id]

  tags = {
    Service = local.name
    Env     = var.env
  }
}

# ECS Fargate service behind the shared ALB.
# Pinned source in a real polyrepo:
#   git::https://github.com/quickbite/terraform-modules.git//modules/java-service?ref=v1.0.0
module "service" {
  source = "../../terraform-modules/modules/java-service"

  name               = local.name
  image              = local.image
  container_port     = local.container_port
  path_prefix        = local.path_prefix
  health_path        = "/actuator/health"
  desired_count      = var.desired_count

  vpc_id             = data.terraform_remote_state.platform.outputs.vpc_id
  private_subnet_ids = data.terraform_remote_state.platform.outputs.private_subnet_ids
  ecs_cluster_arn    = data.terraform_remote_state.platform.outputs.ecs_cluster_arn
  alb_listener_arn   = data.terraform_remote_state.platform.outputs.alb_listener_arn

  # DB connection (DB_URL/DB_USERNAME/DB_PASSWORD) injected as container secrets.
  db_secret_arn = module.db.secret_arn

  environment            = local.environment
  listener_rule_priority = 30

  tags = {
    Service = local.name
    Env     = var.env
  }
}
