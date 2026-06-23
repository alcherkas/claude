variable "env" {
  description = "Deployment environment (matches the tfstate workspace/key)."
  type        = string
  default     = "dev"
}

variable "image_tag" {
  description = "Container image tag to deploy (typically the git SHA)."
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
