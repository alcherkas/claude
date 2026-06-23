# NOTE: each service repo creates its own ECR repository via the
# terraform-modules/modules/java-service module. This shared repository is
# OPTIONAL (off by default) and exists only for platform-level images such as
# a bastion or a migration runner.
resource "aws_ecr_repository" "shared" {
  count = var.create_shared_ecr ? 1 : 0

  name                 = "quickbite/shared"
  image_tag_mutability = "MUTABLE"
  force_delete         = true

  image_scanning_configuration {
    scan_on_push = true
  }

  encryption_configuration {
    encryption_type = "AES256"
  }

  tags = local.tags
}

resource "aws_ecr_lifecycle_policy" "shared" {
  count = var.create_shared_ecr ? 1 : 0

  repository = aws_ecr_repository.shared[0].name

  policy = jsonencode({
    rules = [
      {
        rulePriority = 1
        description  = "Keep last 10 images"
        selection = {
          tagStatus   = "any"
          countType   = "imageCountMoreThan"
          countNumber = 10
        }
        action = { type = "expire" }
      }
    ]
  })
}
