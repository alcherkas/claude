locals {
  name = "restaurant-admin-mfe"
}

# Shared platform foundation (Route53 zone, ACM, logging, etc.).
data "terraform_remote_state" "platform" {
  backend = "s3"
  config = {
    bucket = "quickbite-tfstate-${var.env}"
    key    = "platform-infra/${var.env}/terraform.tfstate"
    region = "us-east-1"
  }
}

# Static site (S3 private bucket + CloudFront + OAC) for the remote MFE bundle.
# In a real polyrepo this source is a pinned git ref:
#   git::https://github.com/quickbite/terraform-modules.git//modules/react-mfe?ref=v1.0.0
module "site" {
  source = "../../terraform-modules/modules/react-mfe"

  name        = local.name
  environment = var.env
  domain_name = var.domain_name
  price_class = var.price_class

  tags = {
    Project = "quickbite"
    MFE     = local.name
  }
}
