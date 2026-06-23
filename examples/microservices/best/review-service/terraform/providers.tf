provider "aws" {
  region = "us-east-1"

  default_tags {
    tags = {
      Project = "quickbite"
      Service = "review-service"
      Env     = var.env
    }
  }
}
