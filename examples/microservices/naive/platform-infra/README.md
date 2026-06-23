# platform-infra

Shared AWS foundation for the **QuickBite** food-delivery platform. Terraform only.

This repo provisions the networking and compute plane that **every** QuickBite
service and micro-frontend repo depends on, and it exposes a stable set of
remote-state outputs those repos read via `data "terraform_remote_state"`.

It is governed by [`../PLATFORM_SPEC.md`](../PLATFORM_SPEC.md) — that document is
the single source of truth for ports, names, and outputs.

## What it creates

| File          | Resources                                                                 |
|---------------|---------------------------------------------------------------------------|
| `vpc.tf`      | VPC (`10.20.0.0/16` dev) via `terraform-aws-modules/vpc/aws ~> 5.0`: 3 AZs, public + private subnets, single NAT gateway. |
| `ecs.tf`      | `aws_ecs_cluster.quickbite` (+ FARGATE / FARGATE_SPOT capacity providers) and the `quickbite.internal` Cloud Map private DNS namespace. |
| `alb.tf`      | Public `aws_lb` + `:80` `aws_lb_listener` with a default fixed-response 404. Service repos attach their own listener rules per `path_prefix`. |
| `ecr.tf`      | Optional shared ECR repo (off by default — services create their own via the `java-service` module). |
| `security.tf` | ALB security group and a shared intra-cluster security group.             |
| `data.tf`     | AZ lookup, CIDR math, common locals/tags.                                 |

## Outputs (the contract)

Every service/MFE repo reads these from `platform-infra/<env>/terraform.tfstate`:

| Output                           | Meaning                                              |
|----------------------------------|------------------------------------------------------|
| `vpc_id`                         | Shared VPC id.                                        |
| `public_subnet_ids`              | Public subnets (ALB).                                 |
| `private_subnet_ids`             | Private subnets (ECS tasks + RDS).                    |
| `ecs_cluster_arn`                | ECS cluster ARN.                                      |
| `ecs_cluster_name`               | ECS cluster name (`quickbite`).                       |
| `alb_arn`                        | Public ALB ARN.                                       |
| `alb_dns_name`                   | Public ALB DNS name.                                  |
| `alb_listener_arn`               | `:80` listener — services attach rules here.          |
| `service_discovery_namespace_id` | Cloud Map namespace id (`quickbite.internal`).        |

Consuming repos reference them like:

```hcl
data "terraform_remote_state" "platform" {
  backend = "s3"
  config = {
    bucket = "quickbite-tfstate-${var.env}"
    key    = "platform-infra/${var.env}/terraform.tfstate"
    region = "us-east-1"
  }
}

# e.g. data.terraform_remote_state.platform.outputs.alb_listener_arn
```

## State backend

S3 partial backend (real values via `-backend-config`):

- Bucket: `quickbite-tfstate-<env>`
- Key:    `platform-infra/<env>/terraform.tfstate`
- Lock:   DynamoDB table `quickbite-tflocks`
- Region: `us-east-1`

## Apply order

**This repo must be applied first.** It is the root of the dependency graph;
all service and MFE repos read its outputs. The reusable `terraform-modules`
repo has no deployable resources of its own (modules only), so the order is:

1. `platform-infra`  ← **here, first**
2. service repos (`identity-service`, `restaurant-service`, … in dependency
   order from `PLATFORM_SPEC.md` §1.1)
3. MFE repos (`shell`, feature MFEs)

## Usage

```bash
# dev
terraform init \
  -backend-config="bucket=quickbite-tfstate-dev" \
  -backend-config="key=platform-infra/dev/terraform.tfstate" \
  -backend-config="region=us-east-1" \
  -backend-config="dynamodb_table=quickbite-tflocks" \
  -backend-config="encrypt=true"

terraform plan  -var-file=environments/dev.tfvars
terraform apply -var-file=environments/dev.tfvars

# prod — same, swapping dev → prod in the bucket/key and var-file
```

> Do not run any build/apply as part of code generation; the commands above are
> for real operators.
