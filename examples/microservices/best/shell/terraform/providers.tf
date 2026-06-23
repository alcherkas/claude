provider "aws" {
  region = "us-east-1"

  default_tags {
    tags = {
      Project = "quickbite"
      MFE     = "shell"
      Env     = var.env
    }
  }
}
