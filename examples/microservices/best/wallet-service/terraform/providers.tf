provider "aws" {
  region = "us-east-1"

  default_tags {
    tags = {
      Project = "quickbite"
      Service = "wallet-service"
      Env     = var.env
    }
  }
}
