output "service_name" {
  description = "Logical service name."
  value       = local.service_name
}

output "service_url_path" {
  description = "Gateway path prefix routed to this service."
  value       = "/api/promotions"
}

output "ecs_service_name" {
  description = "Name of the ECS service created by the java-service module."
  value       = module.service.service_name
}

output "ecr_repository_url" {
  description = "ECR repository URL created by the java-service module."
  value       = module.service.ecr_repository_url
}

output "target_group_arn" {
  description = "ALB target group ARN for this service."
  value       = module.service.target_group_arn
}

output "db_endpoint" {
  description = "Postgres endpoint for promotion_db."
  value       = module.db.endpoint
}

output "db_secret_arn" {
  description = "Secrets Manager ARN holding promotion_db credentials."
  value       = module.db.secret_arn
}
