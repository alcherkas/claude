provider "aws" {
  region = "us-east-1"

  default_tags {
    tags = {
      Project = "quickbite"
      Service = "notification-service"
      Env     = var.env
    }
  }
}
