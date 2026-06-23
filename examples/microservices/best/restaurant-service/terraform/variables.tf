variable "env" {
  description = "Deployment environment (e.g. dev, staging, prod)."
  type        = string
  default     = "dev"
}

variable "image_tag" {
  description = "Container image tag to deploy (typically the git SHA)."
  type        = string
  default     = "latest"
}

variable "container_port" {
  description = "Port the restaurant-service listens on (matches server.port)."
  type        = number
  default     = 8082
}

variable "desired_count" {
  description = "Baseline number of running tasks."
  type        = number
  default     = 2
}

variable "identity_url" {
  description = "Internal base URL of identity-service for service-to-service calls."
  type        = string
  default     = "http://identity-service.quickbite.internal:8081"
}

variable "kafka_bootstrap_servers" {
  description = "Kafka bootstrap servers for the restaurant.events producer."
  type        = string
  default     = "kafka.quickbite.internal:9092"
}
