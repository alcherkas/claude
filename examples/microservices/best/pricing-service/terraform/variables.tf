variable "env" {
  description = "Deployment environment (matches the tfstate key segment)."
  type        = string
  default     = "dev"
}

variable "image_tag" {
  description = "Container image tag to deploy (e.g. a git SHA or semver)."
  type        = string
  default     = "latest"
}

variable "desired_count" {
  description = "Baseline number of running tasks."
  type        = number
  default     = 2
}

variable "container_port" {
  description = "Port the Spring Boot app listens on (matches server.port)."
  type        = number
  default     = 8086
}

variable "menu_url" {
  description = "Internal URL of menu-service for the clients.menu.url env var."
  type        = string
  default     = "http://menu-service.quickbite.internal:8083"
}

variable "promotion_url" {
  description = "Internal URL of promotion-service for the clients.promotion.url env var."
  type        = string
  default     = "http://promotion-service.quickbite.internal:8087"
}
