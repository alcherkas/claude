resource "aws_ecs_cluster" "quickbite" {
  name = var.cluster_name

  setting {
    name  = "containerInsights"
    value = "enabled"
  }

  tags = merge(local.tags, { Name = var.cluster_name })
}

resource "aws_ecs_cluster_capacity_providers" "quickbite" {
  cluster_name = aws_ecs_cluster.quickbite.name

  capacity_providers = ["FARGATE", "FARGATE_SPOT"]

  default_capacity_provider_strategy {
    capacity_provider = "FARGATE"
    base              = 1
    weight            = 1
  }
}

# Cloud Map private DNS namespace so services resolve each other at
# <service>.quickbite.internal:<port> (matches clients.*.url convention).
resource "aws_service_discovery_private_dns_namespace" "quickbite" {
  name        = var.internal_dns_namespace
  description = "Private service discovery namespace for QuickBite services"
  vpc         = module.vpc.vpc_id

  tags = local.tags
}
