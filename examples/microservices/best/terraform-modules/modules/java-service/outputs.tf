output "service_name" {
  description = "Name of the ECS service."
  value       = aws_ecs_service.this.name
}

output "target_group_arn" {
  description = "ARN of the ALB target group fronting the service."
  value       = aws_lb_target_group.this.arn
}

output "ecr_repository_url" {
  description = "URL of the ECR repository to push images to."
  value       = aws_ecr_repository.this.repository_url
}

output "security_group_id" {
  description = "ID of the service security group (pass to postgres-db/redis-cache allow lists)."
  value       = aws_security_group.this.id
}
