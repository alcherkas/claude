provider "aws" {
  region = "us-east-1"

  default_tags {
    tags = {
      Project = "quickbite"
      Service = "menu-service"
      Env     = var.env
    }
  }
}
