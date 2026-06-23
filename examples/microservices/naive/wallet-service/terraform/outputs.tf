output "service_name" {
  description = "ECS service name for wallet-service."
  value       = module.service.service_name
}

output "target_group_arn" {
  description = "ALB target group ARN for wallet-service."
  value       = module.service.target_group_arn
}

output "security_group_id" {
  description = "Security group id of the wallet-service tasks."
  value       = module.service.security_group_id
}

output "db_endpoint" {
  description = "Endpoint of the wallet_db Postgres instance."
  value       = module.db.endpoint
}
