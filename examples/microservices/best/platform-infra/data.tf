data "aws_availability_zones" "available" {
  state = "available"

  filter {
    name   = "opt-in-status"
    values = ["opt-in-not-required"]
  }
}

locals {
  name = "quickbite-${var.env}"

  azs = slice(data.aws_availability_zones.available.names, 0, var.az_count)

  # /16 split into /20 subnets: public + private per AZ.
  public_subnet_cidrs  = [for i in range(var.az_count) : cidrsubnet(var.vpc_cidr, 4, i)]
  private_subnet_cidrs = [for i in range(var.az_count) : cidrsubnet(var.vpc_cidr, 4, i + 8)]

  tags = {
    Project     = "QuickBite"
    Environment = var.env
  }
}
