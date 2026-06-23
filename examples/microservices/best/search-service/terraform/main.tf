###############################################################################
# search-service infrastructure
#
# Reads shared foundation outputs from platform-infra, provisions a dedicated
# Postgres database (search_db), and deploys the service onto the shared ECS/ALB.
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
  image = "${var.image_registry}/search-service:${var.image_tag}"

  # Mirrors src/main/resources/application.yml. Cross-service calls target the dependency's
  # /internal/** endpoints over internal service-discovery DNS (PLATFORM_SPEC §2.5).
  environment = {
    SERVER_PORT             = tostring(var.container_port)
    SPRING_PROFILES_ACTIVE  = var.env
    KAFKA_BOOTSTRAP_SERVERS = var.kafka_bootstrap_servers
    KAFKA_GROUP_ID          = "search-service"
    RESTAURANT_URL          = var.restaurant_url
    MENU_URL                = var.menu_url
  }
}

# In a real polyrepo this source is pinned:
#   git::https://github.com/quickbite/terraform-modules.git//modules/postgres-db?ref=v1.0.0
module "db" {
  source = "../../terraform-modules/modules/postgres-db"

  name                       = "search"
  vpc_id                     = data.terraform_remote_state.platform.outputs.vpc_id
  subnet_ids                 = data.terraform_remote_state.platform.outputs.private_subnet_ids
  allowed_security_group_ids = [module.service.security_group_id]

  tags = {
    Service = "search-service"
    Env     = var.env
  }
}

# In a real polyrepo this source is pinned:
#   git::https://github.com/quickbite/terraform-modules.git//modules/java-service?ref=v1.0.0
module "service" {
  source = "../../terraform-modules/modules/java-service"

  name               = "search-service"
  image              = local.image
  container_port     = var.container_port
  desired_count      = var.desired_count
  vpc_id             = data.terraform_remote_state.platform.outputs.vpc_id
  private_subnet_ids = data.terraform_remote_state.platform.outputs.private_subnet_ids
  ecs_cluster_arn    = data.terraform_remote_state.platform.outputs.ecs_cluster_arn
  alb_listener_arn   = data.terraform_remote_state.platform.outputs.alb_listener_arn

  # Matches the gateway route table (PLATFORM_SPEC §4): /api/search/**
  path_prefix = "/api/search"
  health_path = "/actuator/health"

  # DB_URL / DB_USERNAME / DB_PASSWORD injected as container secrets from this ARN.
  db_secret_arn = module.db.secret_arn

  environment = local.environment

  tags = {
    Service = "search-service"
    Env     = var.env
  }
}
