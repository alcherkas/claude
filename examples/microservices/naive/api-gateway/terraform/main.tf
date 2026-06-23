data "aws_caller_identity" "current" {}

data "aws_region" "current" {}

locals {
  service_name = "api-gateway"

  # The java-service module creates the ECR repo named after the service; this is the URI the
  # pushed image lands at (<account>.dkr.ecr.<region>.amazonaws.com/<name>).
  image_uri = "${data.aws_caller_identity.current.account_id}.dkr.ecr.${data.aws_region.current.name}.amazonaws.com/${local.service_name}:${var.image_tag}"
}

# Read the shared platform foundation (VPC, ECS cluster, ALB listener, subnets).
data "terraform_remote_state" "platform" {
  backend = "s3"
  config = {
    bucket = "quickbite-tfstate-${var.env}"
    key    = "platform-infra/${var.env}/terraform.tfstate"
    region = "us-east-1"
  }
}

# api-gateway is the public edge: stateless (no postgres-db module), routed on /api.
# In a real polyrepo this source is pinned, e.g.:
#   git::https://github.com/quickbite/terraform-modules.git//modules/java-service?ref=v1.0.0
module "service" {
  source = "../../terraform-modules/modules/java-service"

  name           = local.service_name
  image          = local.image_uri
  container_port = var.container_port
  cpu            = var.cpu
  memory         = var.memory
  desired_count  = var.desired_count

  vpc_id             = data.terraform_remote_state.platform.outputs.vpc_id
  private_subnet_ids = data.terraform_remote_state.platform.outputs.private_subnet_ids
  ecs_cluster_arn    = data.terraform_remote_state.platform.outputs.ecs_cluster_arn
  alb_listener_arn   = data.terraform_remote_state.platform.outputs.alb_listener_arn

  # The gateway owns the public /api prefix on the shared ALB (PLATFORM_SPEC §4).
  path_prefix = "/api"
  health_path = "/actuator/health"

  # Mirrors application.yml — every backend URL plus the shared JWT secret, all overridable via env.
  environment = {
    SERVER_PORT               = tostring(var.container_port)
    JWT_SECRET                = var.jwt_secret
    JWT_ISSUER                = "quickbite-identity"
    CLIENTS_IDENTITY_URL      = var.identity_url
    CLIENTS_RESTAURANT_URL    = var.restaurant_url
    CLIENTS_MENU_URL          = var.menu_url
    CLIENTS_SEARCH_URL        = var.search_url
    CLIENTS_CART_URL          = var.cart_url
    CLIENTS_PRICING_URL       = var.pricing_url
    CLIENTS_PROMOTION_URL     = var.promotion_url
    CLIENTS_ORDER_URL         = var.order_url
    CLIENTS_PAYMENT_URL       = var.payment_url
    CLIENTS_WALLET_URL        = var.wallet_url
    CLIENTS_DRIVER_URL        = var.driver_url
    CLIENTS_DELIVERY_URL      = var.delivery_url
    CLIENTS_NOTIFICATION_URL  = var.notification_url
    CLIENTS_REVIEW_URL        = var.review_url
  }
}
