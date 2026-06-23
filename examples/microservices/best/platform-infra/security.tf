# Public-facing ALB security group: open :80 to the world, egress anywhere.
resource "aws_security_group" "alb" {
  name        = "${local.name}-alb-sg"
  description = "Public ALB ingress for QuickBite"
  vpc_id      = module.vpc.vpc_id

  ingress {
    description = "HTTP from anywhere"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    description = "All egress"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(local.tags, { Name = "${local.name}-alb-sg" })
}

# Shared SG that service task SGs can reference for intra-cluster traffic.
# Service repos create their own task SGs (java-service module) but allow
# ingress from this SG so anything inside the cluster mesh can reach them.
resource "aws_security_group" "services_internal" {
  name        = "${local.name}-services-internal-sg"
  description = "Intra-cluster traffic between QuickBite services"
  vpc_id      = module.vpc.vpc_id

  ingress {
    description = "All traffic from within the platform VPC"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = [var.vpc_cidr]
  }

  egress {
    description = "All egress"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(local.tags, { Name = "${local.name}-services-internal-sg" })
}
