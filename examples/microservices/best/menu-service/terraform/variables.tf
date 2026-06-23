variable "env" {
  description = "Deployment environment (used in state keys, names and tags)."
  type        = string
  default     = "dev"
}

variable "image_tag" {
  description = "Container image tag to deploy (e.g. a git SHA or semver)."
  type        = string
  default     = "latest"
}

variable "image_registry" {
  description = "Container registry / repository prefix for the menu-service image."
  type        = string
  default     = "ghcr.io/quickbite"
}

variable "tfstate_bucket" {
  description = "S3 bucket holding remote state (used to read platform-infra outputs)."
  type        = string
  default     = "quickbite-tfstate-dev"
}

variable "desired_count" {
  description = "Baseline number of running ECS tasks."
  type        = number
  default     = 2
}

variable "kafka_bootstrap_servers" {
  description = "Kafka bootstrap servers reachable from the ECS tasks."
  type        = string
  default     = "kafka.quickbite.internal:9092"
}
