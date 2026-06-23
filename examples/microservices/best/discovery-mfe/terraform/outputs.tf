output "bucket_name" {
  description = "S3 origin bucket to sync the built discovery bundle into."
  value       = module.site.bucket_name
}

output "distribution_id" {
  description = "CloudFront distribution id (use for cache invalidation on deploy)."
  value       = module.site.distribution_id
}

output "domain" {
  description = "Public domain serving the discovery remote (and its remoteEntry.js)."
  value       = module.site.domain
}
