output "primary_endpoint" {
  description = "Primary endpoint address of the Redis replication group."
  value       = aws_elasticache_replication_group.this.primary_endpoint_address
}

output "security_group_id" {
  description = "ID of the Redis security group."
  value       = aws_security_group.this.id
}
