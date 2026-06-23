variable "name" {
  description = "MFE name, used for the S3 bucket and CloudFront naming (e.g. checkout-mfe)."
  type        = string
}

variable "environment" {
  description = "Deployment environment (e.g. dev)."
  type        = string
}

variable "domain_name" {
  description = "Optional custom domain (CNAME) for the CloudFront distribution. When unset, the *.cloudfront.net domain is used."
  type        = string
  default     = null
}

variable "acm_certificate_arn" {
  description = "Optional ACM certificate ARN (must be in us-east-1) to back domain_name. Required if domain_name is set with TLS."
  type        = string
  default     = null
}

variable "price_class" {
  description = "CloudFront price class."
  type        = string
  default     = "PriceClass_100"
}

variable "tags" {
  description = "Tags applied to all resources."
  type        = map(string)
  default     = {}
}
