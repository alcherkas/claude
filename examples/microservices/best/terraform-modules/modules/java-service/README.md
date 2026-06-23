# java-service module

Deploys a QuickBite Java/Spring Boot service as an **ECS Fargate** service behind
the shared ALB. It provisions the full per-service footprint: ECR repository,
CloudWatch log group, task-execution IAM role, security group, ALB target group +
listener rule, the Fargate task definition/service, and CPU target-tracking
autoscaling.

## Behaviour

- The container is wired with `environment` (plain vars) and, when `db_secret_arn`
  is set, with `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` pulled as **secrets** from
  the Secrets Manager JSON (`url` / `username` / `password` keys produced by the
  `postgres-db` module). The execution role is granted `GetSecretValue` on it.
- The ALB listener rule matches `${path_prefix}/*` (e.g. `/api/orders/*`), which is
  exactly the gateway route from PLATFORM_SPEC §4.
- `desired_count` is the baseline; autoscaling owns it after the first apply
  (`ignore_changes`) and scales between `min_capacity` and `max_capacity` on CPU.

## Inputs

| Name | Type | Default | Description |
|------|------|---------|-------------|
| `name` | string | — | Service name; also ECR repo / log group / ECS service name. |
| `image` | string | — | Container image URI. |
| `container_port` | number | — | App listen port (matches `server.port`). |
| `cpu` | number | `256` | Fargate CPU units. |
| `memory` | number | `512` | Fargate memory (MiB). |
| `desired_count` | number | `2` | Baseline task count. |
| `vpc_id` | string | — | VPC id. |
| `private_subnet_ids` | list(string) | — | Private subnets for tasks. |
| `ecs_cluster_arn` | string | — | Shared ECS cluster ARN. |
| `alb_listener_arn` | string | — | Shared ALB listener ARN. |
| `path_prefix` | string | — | ALB/gateway path prefix, e.g. `/api/orders`. |
| `environment` | map(string) | `{}` | Plain env vars. |
| `health_path` | string | `/actuator/health` | Health check path. |
| `db_secret_arn` | string | `null` | Optional DB secret ARN. |
| `listener_rule_priority` | number | `100` | Unique listener-rule priority. |
| `log_retention_days` | number | `14` | Log retention. |
| `min_capacity` | number | `2` | Autoscaling min. |
| `max_capacity` | number | `6` | Autoscaling max. |
| `cpu_target_percent` | number | `60` | CPU target for scaling. |
| `tags` | map(string) | `{}` | Extra tags. |

## Outputs

| Name | Description |
|------|-------------|
| `service_name` | ECS service name. |
| `target_group_arn` | ALB target group ARN. |
| `ecr_repository_url` | ECR repository URL. |
| `security_group_id` | Service security group id. |
