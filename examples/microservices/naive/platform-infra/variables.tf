variable "env" {
  description = "Deployment environment (dev, prod, ...). Drives naming and state key."
  type        = string
  default     = "dev"
}

variable "region" {
  description = "AWS region for all platform resources."
  type        = string
  default     = "us-east-1"
}

variable "vpc_cidr" {
  description = "CIDR block for the platform VPC."
  type        = string
  default     = "10.20.0.0/16"
}

variable "az_count" {
  description = "Number of Availability Zones to spread subnets across."
  type        = number
  default     = 3
}

variable "single_nat_gateway" {
  description = "Use a single shared NAT gateway (cheaper) instead of one per AZ."
  type        = bool
  default     = true
}

variable "internal_dns_namespace" {
  description = "Cloud Map private DNS namespace for service-to-service discovery."
  type        = string
  default     = "quickbite.internal"
}

variable "cluster_name" {
  description = "ECS cluster name shared by all QuickBite services."
  type        = string
  default     = "quickbite"
}

variable "create_shared_ecr" {
  description = "Whether to create a shared ECR repository (services normally create their own)."
  type        = bool
  default     = false
}
