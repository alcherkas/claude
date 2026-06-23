variable "env" {
  description = "Deployment environment (dev, staging, prod)."
  type        = string
  default     = "dev"
}

variable "domain_name" {
  description = "Optional custom domain for the order-tracking CloudFront distribution."
  type        = string
  default     = null
}

variable "price_class" {
  description = "CloudFront price class for the static distribution."
  type        = string
  default     = "PriceClass_100"
}
