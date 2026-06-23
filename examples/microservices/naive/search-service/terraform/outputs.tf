output "service_name" {
  description = "ECS service name."
  value       = module.service.service_name
}

output "ecr_repository_url" {
  description = "ECR repository to push the search-service image to."
  value       = module.service.ecr_repository_url
}

output "target_group_arn" {
  description = "ALB target group fronting the service."
  value       = module.service.target_group_arn
}

output "security_group_id" {
  description = "Service security group id."
  value       = module.service.security_group_id
}

output "db_endpoint" {
  description = "search_db RDS endpoint."
  value       = module.db.endpoint
}

output "db_secret_arn" {
  description = "Secrets Manager ARN holding the search_db credentials."
  value       = module.db.secret_arn
}
