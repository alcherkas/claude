output "service_name" {
  description = "ECS service name."
  value       = module.service.service_name
}

output "ecr_repository_url" {
  description = "ECR repository to push restaurant-service images to."
  value       = module.service.ecr_repository_url
}

output "target_group_arn" {
  description = "ALB target group fronting the service."
  value       = module.service.target_group_arn
}

output "service_security_group_id" {
  description = "Security group ID of the ECS service."
  value       = module.service.security_group_id
}

output "db_endpoint" {
  description = "Endpoint of the restaurant_db Postgres instance."
  value       = module.db.endpoint
}

output "db_secret_arn" {
  description = "Secrets Manager ARN holding the restaurant_db credentials."
  value       = module.db.secret_arn
}
