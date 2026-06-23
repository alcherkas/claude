# terraform-modules

Reusable Terraform modules consumed by every QuickBite repository. Each service /
MFE / infra repo references these modules; in this example layout the source is a
**relative path** (`../../terraform-modules/modules/<name>`), while in a real polyrepo
it would be a **pinned git source**:

```hcl
module "service" {
  source = "git::https://github.com/quickbite/terraform-modules.git//modules/java-service?ref=v1.0.0"
  # ...
}
```

> Pinning to a tag (`?ref=v1.0.0`) is mandatory in real usage so that a module
> change never silently re-plans every consumer. The relative source used in this
> repo is purely for the self-contained example.

## Conventions (per PLATFORM_SPEC §2.5)

- Terraform `>= 1.6`, AWS provider `~> 5.40`.
- Region `us-east-1`, default environment `dev`.
- Modules are pure building blocks: they declare **no** `provider` or `backend`
  blocks (those belong to the consuming root module) — only `required_providers`
  in `versions.tf`.

## Modules

| Module | Purpose |
|--------|---------|
| [`modules/java-service`](modules/java-service) | ECR + ECS Fargate service behind the shared ALB, with autoscaling, logs, IAM and an ALB listener rule on a `path_prefix`. |
| [`modules/react-mfe`](modules/react-mfe)       | Private S3 bucket + CloudFront (OAC) static site for a React micro-frontend, with SPA fallback routing. |
| [`modules/postgres-db`](modules/postgres-db)   | RDS Postgres instance, subnet group, security group and a Secrets Manager secret holding the JDBC URL / user / password. |
| [`modules/redis-cache`](modules/redis-cache)   | ElastiCache Redis replication group with subnet group and security group. |

## Usage example

```hcl
# In a service repo (e.g. order-service/terraform/main.tf)

data "terraform_remote_state" "platform" {
  backend = "s3"
  config = {
    bucket = "quickbite-tfstate-${var.environment}"
    key    = "platform-infra/${var.environment}/terraform.tfstate"
    region = "us-east-1"
  }
}

module "db" {
  source                     = "../../terraform-modules/modules/postgres-db"
  name                       = "order"
  vpc_id                     = data.terraform_remote_state.platform.outputs.vpc_id
  subnet_ids                 = data.terraform_remote_state.platform.outputs.private_subnet_ids
  allowed_security_group_ids = [module.service.security_group_id]
}

module "service" {
  source              = "../../terraform-modules/modules/java-service"
  name                = "order-service"
  image               = "${data.terraform_remote_state.platform.outputs.ecr_registry}/order-service:latest"
  container_port      = 8088
  vpc_id              = data.terraform_remote_state.platform.outputs.vpc_id
  private_subnet_ids  = data.terraform_remote_state.platform.outputs.private_subnet_ids
  ecs_cluster_arn     = data.terraform_remote_state.platform.outputs.ecs_cluster_arn
  alb_listener_arn    = data.terraform_remote_state.platform.outputs.alb_listener_arn
  path_prefix         = "/api/orders"
  db_secret_arn       = module.db.secret_arn
  environment = {
    DB_URL              = "jdbc:postgresql://${module.db.endpoint}/order_db"
    KAFKA_BOOTSTRAP     = "kafka:9092"
    CLIENTS_PRICING_URL = "http://pricing-service.quickbite.internal:8086"
  }
}
```

See each module's own README for the full input/output contract.
