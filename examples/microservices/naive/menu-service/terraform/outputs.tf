output "service_name" {
  description = "Name of the ECS service."
  value       = module.service.service_name
}

output "ecr_repository_url" {
  description = "ECR repository to push the menu-service image to."
  value       = module.service.ecr_repository_url
}

output "target_group_arn" {
  description = "ALB target group ARN fronting menu-service."
  value       = module.service.target_group_arn
}

output "security_group_id" {
  description = "Security group ID of the menu-service tasks."
  value       = module.service.security_group_id
}

output "db_endpoint" {
  description = "Endpoint of the menu_db RDS instance."
  value       = module.db.endpoint
}

output "db_secret_arn" {
  description = "Secrets Manager ARN holding menu_db credentials."
  value       = module.db.secret_arn
}
