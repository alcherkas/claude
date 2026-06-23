provider "aws" {
  region = "us-east-1"

  default_tags {
    tags = {
      Project = "quickbite"
      MFE     = "checkout-mfe"
      Env     = var.env
    }
  }
}
