output "service_name" {
  description = "ECS service name."
  value       = module.service.service_name
}

output "target_group_arn" {
  description = "ALB target group ARN for identity-service."
  value       = module.service.target_group_arn
}

output "ecr_repository_url" {
  description = "ECR repository URL for the service image."
  value       = module.service.ecr_repository_url
}

output "db_endpoint" {
  description = "Postgres endpoint backing identity_db."
  value       = module.db.endpoint
}

output "db_secret_arn" {
  description = "Secrets Manager ARN holding identity_db credentials."
  value       = module.db.secret_arn
}
