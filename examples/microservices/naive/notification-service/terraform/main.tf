###############################################################################
# notification-service infrastructure
#
# Reads shared foundation outputs from platform-infra, provisions a dedicated
# Postgres database (notification_db), and deploys the service onto the shared
# ECS/ALB. Pure event consumer (orders/payments/deliveries) — no synchronous
# (Feign) dependencies, so only Kafka + DB wiring is injected.
###############################################################################

data "terraform_remote_state" "platform" {
  backend = "s3"
  config = {
    bucket = "quickbite-tfstate-${var.env}"
    key    = "platform-infra/${var.env}/terraform.tfstate"
    region = "us-east-1"
  }
}

locals {
  image = "${var.image_registry}/notification-service:${var.image_tag}"

  # Mirrors src/main/resources/application.yml: Kafka wiring only (no clients.*.url — this
  # service has no synchronous dependencies). DB_* are injected as secrets from the db module below.
  environment = {
    SERVER_PORT             = tostring(var.container_port)
    SPRING_PROFILES_ACTIVE  = var.env
    KAFKA_BOOTSTRAP_SERVERS = var.kafka_bootstrap_servers
    KAFKA_GROUP_ID          = "notification-service"
  }
}

# In a real polyrepo this source is pinned:
#   git::https://github.com/quickbite/terraform-modules.git//modules/postgres-db?ref=v1.0.0
module "db" {
  source = "../../terraform-modules/modules/postgres-db"

  name                       = "notification"
  vpc_id                     = data.terraform_remote_state.platform.outputs.vpc_id
  subnet_ids                 = data.terraform_remote_state.platform.outputs.private_subnet_ids
  allowed_security_group_ids = [module.service.security_group_id]

  tags = {
    Service = "notification-service"
    Env     = var.env
  }
}

# In a real polyrepo this source is pinned:
#   git::https://github.com/quickbite/terraform-modules.git//modules/java-service?ref=v1.0.0
module "service" {
  source = "../../terraform-modules/modules/java-service"

  name               = "notification-service"
  image              = local.image
  container_port     = var.container_port
  desired_count      = var.desired_count
  vpc_id             = data.terraform_remote_state.platform.outputs.vpc_id
  private_subnet_ids = data.terraform_remote_state.platform.outputs.private_subnet_ids
  ecs_cluster_arn    = data.terraform_remote_state.platform.outputs.ecs_cluster_arn
  alb_listener_arn   = data.terraform_remote_state.platform.outputs.alb_listener_arn

  # Matches the gateway route table (PLATFORM_SPEC §4): /api/notifications/**
  path_prefix = "/api/notifications"
  health_path = "/actuator/health"

  # DB_URL / DB_USERNAME / DB_PASSWORD injected as container secrets from this ARN.
  db_secret_arn = module.db.secret_arn

  environment = local.environment

  tags = {
    Service = "notification-service"
    Env     = var.env
  }
}
