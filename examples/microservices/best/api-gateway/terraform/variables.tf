variable "env" {
  description = "Deployment environment (matches the tfstate key segment)."
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
  description = "Desired number of running tasks (the edge usually runs >1)."
  type        = number
  default     = 2
}

variable "container_port" {
  description = "Port the gateway listens on (matches server.port)."
  type        = number
  default     = 8080
}

variable "jwt_secret" {
  description = "Shared HS256 JWT signing secret (must match identity-service and downstream services)."
  type        = string
  sensitive   = true
}

# Internal URLs of each backend service the gateway routes to. In a real env these are internal
# Cloud Map / ALB DNS names; the defaults below use the convention from PLATFORM_SPEC §2.5.
variable "identity_url" {
  type    = string
  default = "http://identity-service.quickbite.internal:8081"
}

variable "restaurant_url" {
  type    = string
  default = "http://restaurant-service.quickbite.internal:8082"
}

variable "menu_url" {
  type    = string
  default = "http://menu-service.quickbite.internal:8083"
}

variable "search_url" {
  type    = string
  default = "http://search-service.quickbite.internal:8084"
}

variable "cart_url" {
  type    = string
  default = "http://cart-service.quickbite.internal:8085"
}

variable "pricing_url" {
  type    = string
  default = "http://pricing-service.quickbite.internal:8086"
}

variable "promotion_url" {
  type    = string
  default = "http://promotion-service.quickbite.internal:8087"
}

variable "order_url" {
  type    = string
  default = "http://order-service.quickbite.internal:8088"
}

variable "payment_url" {
  type    = string
  default = "http://payment-service.quickbite.internal:8089"
}

variable "wallet_url" {
  type    = string
  default = "http://wallet-service.quickbite.internal:8090"
}

variable "driver_url" {
  type    = string
  default = "http://driver-service.quickbite.internal:8091"
}

variable "delivery_url" {
  type    = string
  default = "http://delivery-service.quickbite.internal:8092"
}

variable "notification_url" {
  type    = string
  default = "http://notification-service.quickbite.internal:8093"
}

variable "review_url" {
  type    = string
  default = "http://review-service.quickbite.internal:8094"
}
