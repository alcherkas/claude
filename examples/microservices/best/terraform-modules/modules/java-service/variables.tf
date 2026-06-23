variable "name" {
  description = "Service name (also the ECR repo, log group and ECS service name), e.g. order-service."
  type        = string
}

variable "image" {
  description = "Fully-qualified container image URI to run (e.g. <registry>/order-service:latest)."
  type        = string
}

variable "container_port" {
  description = "Port the Spring Boot app listens on inside the container (matches server.port)."
  type        = number
}

variable "cpu" {
  description = "Fargate task CPU units."
  type        = number
  default     = 256
}

variable "memory" {
  description = "Fargate task memory (MiB)."
  type        = number
  default     = 512
}

variable "desired_count" {
  description = "Baseline number of running tasks."
  type        = number
  default     = 2
}

variable "vpc_id" {
  description = "VPC the service and its security group live in."
  type        = string
}

variable "private_subnet_ids" {
  description = "Private subnets the Fargate tasks run in."
  type        = list(string)
}

variable "ecs_cluster_arn" {
  description = "ARN of the shared ECS cluster (from platform-infra)."
  type        = string
}

variable "alb_listener_arn" {
  description = "ARN of the shared ALB HTTPS/HTTP listener to attach the listener rule to."
  type        = string
}

variable "path_prefix" {
  description = "Gateway/ALB path prefix this service answers on, e.g. /api/orders. The listener rule matches <path_prefix>/*."
  type        = string
}

variable "environment" {
  description = "Plain (non-secret) environment variables injected into the container."
  type        = map(string)
  default     = {}
}

variable "health_path" {
  description = "Container health check / target group health check path."
  type        = string
  default     = "/actuator/health"
}

variable "db_secret_arn" {
  description = "Optional Secrets Manager secret ARN (JSON with url/user/pass). When set, DB_URL/DB_USERNAME/DB_PASSWORD are wired in as container secrets."
  type        = string
  default     = null
}

variable "listener_rule_priority" {
  description = "Priority of the ALB listener rule. Must be unique across services sharing the listener."
  type        = number
  default     = 100
}

variable "log_retention_days" {
  description = "CloudWatch log retention in days."
  type        = number
  default     = 14
}

variable "min_capacity" {
  description = "Autoscaling minimum task count."
  type        = number
  default     = 2
}

variable "max_capacity" {
  description = "Autoscaling maximum task count."
  type        = number
  default     = 6
}

variable "cpu_target_percent" {
  description = "Target average CPU utilization for the autoscaling policy."
  type        = number
  default     = 60
}

variable "tags" {
  description = "Tags applied to all resources."
  type        = map(string)
  default     = {}
}
