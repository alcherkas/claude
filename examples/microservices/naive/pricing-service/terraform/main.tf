data "aws_caller_identity" "current" {}

data "aws_region" "current" {}

locals {
  service_name = "pricing-service"

  # The java-service module creates the ECR repo named after the service; this is
  # the URI the pushed image lands at (<account>.dkr.ecr.<region>.amazonaws.com/<name>).
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

# pricing-service is stateless: no postgres-db module.
# In a real polyrepo this source is pinned, e.g.:
#   git::https://github.com/quickbite/terraform-modules.git//modules/java-service?ref=v1.0.0
module "service" {
  source = "../../terraform-modules/modules/java-service"

  name           = local.service_name
  image          = local.image_uri
  container_port = var.container_port
  desired_count  = var.desired_count

  vpc_id             = data.terraform_remote_state.platform.outputs.vpc_id
  private_subnet_ids = data.terraform_remote_state.platform.outputs.private_subnet_ids
  ecs_cluster_arn    = data.terraform_remote_state.platform.outputs.ecs_cluster_arn
  alb_listener_arn   = data.terraform_remote_state.platform.outputs.alb_listener_arn

  # Gateway/ALB route for this service (see PLATFORM_SPEC §4).
  path_prefix = "/api/pricing"
  health_path = "/actuator/health"

  # Mirrors application.yml — overridable env vars consumed by the container.
  environment = {
    SERVER_PORT           = tostring(var.container_port)
    CLIENTS_MENU_URL      = var.menu_url
    CLIENTS_PROMOTION_URL = var.promotion_url
  }
}
