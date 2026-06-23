output "service_name" {
  description = "ECS service name."
  value       = module.service.service_name
}

output "target_group_arn" {
  description = "ALB target group ARN for delivery-service."
  value       = module.service.target_group_arn
}

output "ecr_repository_url" {
  description = "ECR repository URL for the delivery-service image."
  value       = module.service.ecr_repository_url
}

output "db_endpoint" {
  description = "Postgres endpoint for delivery_db."
  value       = module.db.endpoint
}

output "db_secret_arn" {
  description = "Secrets Manager ARN holding delivery_db credentials."
  value       = module.db.secret_arn
}
