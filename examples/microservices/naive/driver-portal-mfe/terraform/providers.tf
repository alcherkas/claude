provider "aws" {
  region = "us-east-1"

  default_tags {
    tags = {
      Project = "quickbite"
      MFE     = "driver-portal-mfe"
      Env     = var.env
    }
  }
}
