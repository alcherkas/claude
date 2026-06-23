variable "env" {
  description = "Deployment environment (dev, staging, prod)."
  type        = string
  default     = "dev"
}

variable "image_tag" {
  description = "Container image tag to deploy (usually the git SHA)."
  type        = string
  default     = "latest"
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
  description = "Desired number of running tasks."
  type        = number
  default     = 2
}

variable "jwt_secret" {
  description = "Shared HS256 JWT signing secret (also consumed by the gateway and downstream services)."
  type        = string
  sensitive   = true
}
