locals {
  db_name     = "${var.name}_db"
  identifier  = "quickbite-${var.name}"
  secret_name = "quickbite/${var.name}/db"

  tags = merge(var.tags, {
    "quickbite:db"      = local.db_name
    "quickbite:managed" = "terraform"
  })
}

# --- Networking -------------------------------------------------------------

resource "aws_db_subnet_group" "this" {
  name       = "${local.identifier}-subnets"
  subnet_ids = var.subnet_ids
  tags       = local.tags
}

resource "aws_security_group" "this" {
  name        = "${local.identifier}-db"
  description = "Postgres SG for ${local.db_name}"
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
  description              = "Postgres from allowed service SG"
  from_port                = 5432
  to_port                  = 5432
  protocol                 = "tcp"
  security_group_id        = aws_security_group.this.id
  source_security_group_id = each.value
}

# --- Credentials ------------------------------------------------------------

resource "random_password" "this" {
  length  = 24
  special = false
}

resource "aws_db_instance" "this" {
  identifier     = local.identifier
  engine         = "postgres"
  engine_version = var.engine_version
  instance_class = var.instance_class

  db_name  = local.db_name
  username = var.username
  password = random_password.this.result

  allocated_storage     = var.allocated_storage
  storage_type          = "gp3"
  storage_encrypted     = true
  max_allocated_storage = var.allocated_storage * 2

  db_subnet_group_name   = aws_db_subnet_group.this.name
  vpc_security_group_ids = [aws_security_group.this.id]
  publicly_accessible    = false
  multi_az               = false

  backup_retention_period = 7
  skip_final_snapshot     = true
  deletion_protection     = false
  apply_immediately       = true

  tags = local.tags
}

# --- Secret -----------------------------------------------------------------

resource "aws_secretsmanager_secret" "this" {
  name        = local.secret_name
  description = "Connection details for ${local.db_name}"
  tags        = local.tags
}

resource "aws_secretsmanager_secret_version" "this" {
  secret_id = aws_secretsmanager_secret.this.id

  secret_string = jsonencode({
    url      = "jdbc:postgresql://${aws_db_instance.this.endpoint}/${local.db_name}"
    host     = aws_db_instance.this.address
    port     = aws_db_instance.this.port
    dbname   = local.db_name
    username = var.username
    password = random_password.this.result
  })
}
