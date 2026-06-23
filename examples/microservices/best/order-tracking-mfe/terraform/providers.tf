provider "aws" {
  region = "us-east-1"

  default_tags {
    tags = {
      Project = "quickbite"
      MFE     = "order-tracking-mfe"
      Env     = var.env
    }
  }
}
