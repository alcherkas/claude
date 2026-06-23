variable "env" {
  description = "Deployment environment (matches the tfstate workspace/key segment)."
  type        = string
  default     = "dev"
}

variable "image_tag" {
  description = "Container image tag to deploy (e.g. a git SHA or semantic version)."
  type        = string
  default     = "latest"
}

variable "image_registry" {
  description = "Container registry/namespace hosting the notification-service image."
  type        = string
  default     = "quickbite"
}

variable "container_port" {
  description = "Port the Spring Boot app listens on (must match server.port)."
  type        = number
  default     = 8093
}

variable "desired_count" {
  description = "Baseline number of running tasks."
  type        = number
  default     = 2
}

variable "kafka_bootstrap_servers" {
  description = "Kafka bootstrap servers the consumer connects to (MSK in real envs)."
  type        = string
  default     = "kafka.quickbite.internal:9092"
}
