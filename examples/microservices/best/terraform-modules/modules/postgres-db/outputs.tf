output "endpoint" {
  description = "RDS connection endpoint (host:port)."
  value       = aws_db_instance.this.endpoint
}

output "secret_arn" {
  description = "Secrets Manager secret ARN holding the JDBC url/username/password (pass to java-service.db_secret_arn)."
  value       = aws_secretsmanager_secret.this.arn
}

output "security_group_id" {
  description = "ID of the database security group."
  value       = aws_security_group.this.id
}
