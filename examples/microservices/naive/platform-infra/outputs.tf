# These outputs form the contract read by every service/MFE repo via
# data "terraform_remote_state" "platform". Names MUST NOT change.

output "vpc_id" {
  description = "ID of the shared platform VPC."
  value       = module.vpc.vpc_id
}

output "public_subnet_ids" {
  description = "Public subnet IDs (ALB lives here)."
  value       = module.vpc.public_subnets
}

output "private_subnet_ids" {
  description = "Private subnet IDs (ECS tasks + RDS live here)."
  value       = module.vpc.private_subnets
}

output "ecs_cluster_arn" {
  description = "ARN of the shared ECS cluster."
  value       = aws_ecs_cluster.quickbite.arn
}

output "ecs_cluster_name" {
  description = "Name of the shared ECS cluster."
  value       = aws_ecs_cluster.quickbite.name
}

output "alb_arn" {
  description = "ARN of the public Application Load Balancer."
  value       = aws_lb.public.arn
}

output "alb_dns_name" {
  description = "Public DNS name of the ALB."
  value       = aws_lb.public.dns_name
}

output "alb_listener_arn" {
  description = "ARN of the public :80 listener; services attach listener rules here."
  value       = aws_lb_listener.http.arn
}

output "service_discovery_namespace_id" {
  description = "Cloud Map private DNS namespace ID for service discovery."
  value       = aws_service_discovery_private_dns_namespace.quickbite.id
}
