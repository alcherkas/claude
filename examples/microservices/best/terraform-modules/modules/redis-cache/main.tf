locals {
  identifier = "quickbite-${var.name}"

  tags = merge(var.tags, {
    "quickbite:cache"   = var.name
    "quickbite:managed" = "terraform"
  })
}

resource "aws_elasticache_subnet_group" "this" {
  name       = "${local.identifier}-subnets"
  subnet_ids = var.subnet_ids
  tags       = local.tags
}

resource "aws_security_group" "this" {
  name        = "${local.identifier}-redis"
  description = "Redis SG for ${var.name}"
  vpc_id      = var.vpc_id

  egress {
    description = "All outbound"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = local.tags
}

resource "aws_security_group_rule" "ingress" {
  for_each = toset(var.allowed_security_group_ids)

  type                     = "ingress"
  description              = "Redis from allowed service SG"
  from_port                = 6379
  to_port                  = 6379
  protocol                 = "tcp"
  security_group_id        = aws_security_group.this.id
  source_security_group_id = each.value
}

resource "aws_elasticache_replication_group" "this" {
  replication_group_id = local.identifier
  description          = "QuickBite ${var.name} Redis cache"

  engine               = "redis"
  engine_version        = var.engine_version
  node_type             = var.node_type
  num_cache_clusters    = var.num_cache_clusters
  port                  = 6379
  parameter_group_name  = "default.redis7"

  subnet_group_name  = aws_elasticache_subnet_group.this.name
  security_group_ids = [aws_security_group.this.id]

  automatic_failover_enabled = var.num_cache_clusters > 1
  multi_az_enabled           = var.num_cache_clusters > 1
  at_rest_encryption_enabled = true
  transit_encryption_enabled = true

  tags = local.tags
}
