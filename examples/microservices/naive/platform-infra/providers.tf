provider "aws" {
  region = var.region

  default_tags {
    tags = {
      Project     = "QuickBite"
      Environment = var.env
      ManagedBy   = "Terraform"
      Repo        = "platform-infra"
    }
  }
}
