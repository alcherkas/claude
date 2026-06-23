# redis-cache module

Provisions an ElastiCache **Redis replication group** with its subnet group and a
security group. Used by services that need a shared cache (e.g. session / hot data).

## Behaviour

- Creates a multi-node replication group by default (`num_cache_clusters = 2`),
  enabling automatic failover and multi-AZ when more than one node is present.
- At-rest and in-transit encryption are enabled.
- The security group opens `6379` only to the SGs in `allowed_security_group_ids`.

## Inputs

| Name | Type | Default | Description |
|------|------|---------|-------------|
| `name` | string | — | Cache name. |
| `node_type` | string | `cache.t4g.micro` | ElastiCache node type. |
| `engine_version` | string | `7.1` | Redis version. |
| `vpc_id` | string | — | VPC id. |
| `subnet_ids` | list(string) | — | Private subnets. |
| `allowed_security_group_ids` | list(string) | `[]` | SGs allowed on 6379. |
| `num_cache_clusters` | number | `2` | Nodes (primary + replicas). |
| `tags` | map(string) | `{}` | Extra tags. |

## Outputs

| Name | Description |
|------|-------------|
| `primary_endpoint` | Primary endpoint address. |
| `security_group_id` | Redis security group id. |
