output "bucket_name" {
  description = "Name of the private S3 origin bucket to sync the built MFE into."
  value       = aws_s3_bucket.this.bucket
}

output "distribution_id" {
  description = "CloudFront distribution id (use for cache invalidation on deploy)."
  value       = aws_cloudfront_distribution.this.id
}

output "domain" {
  description = "Public domain of the MFE (custom domain when set, else the CloudFront domain)."
  value       = var.domain_name != null ? var.domain_name : aws_cloudfront_distribution.this.domain_name
}
