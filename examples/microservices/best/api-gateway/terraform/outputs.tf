output "service_name" {
  description = "ECS service name."
  value       = module.service.service_name
}

output "target_group_arn" {
  description = "ALB target group ARN fronting the public /api edge."
  value       = module.service.target_group_arn
}

output "ecr_repository_url" {
  description = "ECR repository URL to push the gateway image to."
  value       = module.service.ecr_repository_url
}

output "security_group_id" {
  description = "Gateway service security group ID."
  value       = module.service.security_group_id
}
