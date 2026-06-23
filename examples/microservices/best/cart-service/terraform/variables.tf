variable "env" {
  description = "Deployment environment (dev, staging, prod)."
  type        = string
  default     = "dev"
}

variable "image_tag" {
  description = "Container image tag to deploy (e.g. git SHA)."
  type        = string
  default     = "latest"
}

variable "container_port" {
  description = "Port the cart-service container listens on."
  type        = number
  default     = 8085
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

variable "menu_url" {
  description = "Base URL of menu-service for /internal calls."
  type        = string
  default     = "http://menu-service.quickbite.internal:8083"
}

variable "identity_url" {
  description = "Base URL of identity-service for /internal calls."
  type        = string
  default     = "http://identity-service.quickbite.internal:8081"
}
