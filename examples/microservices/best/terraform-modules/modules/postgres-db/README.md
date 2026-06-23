# postgres-db module

Provisions an RDS **PostgreSQL** instance for a stateful QuickBite service, plus its
DB subnet group, a security group, generated credentials, and a Secrets Manager
secret holding the connection details.

## Behaviour

- The database created is `<name>_db` (e.g. `name = "order"` → `order_db`), matching
  the DB names in PLATFORM_SPEC §1.1.
- A `random_password` is generated and stored — together with the JDBC `url`,
  `host`, `port`, `dbname` and `username` — as JSON in Secrets Manager. The `url`,
  `username` and `password` keys line up with the `java-service` module's
  `db_secret_arn` wiring.
- The security group opens `5432` only to the security groups in
  `allowed_security_group_ids` (typically the consuming service's SG).

## Inputs

| Name | Type | Default | Description |
|------|------|---------|-------------|
| `name` | string | — | Logical name; DB becomes `<name>_db`. |
| `engine_version` | string | `16.3` | Postgres version. |
| `instance_class` | string | `db.t4g.micro` | RDS instance class. |
| `allocated_storage` | number | `20` | Storage (GiB). |
| `vpc_id` | string | — | VPC id. |
| `subnet_ids` | list(string) | — | Private subnets. |
| `allowed_security_group_ids` | list(string) | `[]` | SGs allowed on 5432. |
| `username` | string | `quickbite` | Master username. |
| `tags` | map(string) | `{}` | Extra tags. |

## Outputs

| Name | Description |
|------|-------------|
| `endpoint` | RDS endpoint (host:port). |
| `secret_arn` | Secrets Manager secret ARN. |
| `security_group_id` | DB security group id. |
