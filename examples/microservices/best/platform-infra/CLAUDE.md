# CLAUDE.md — `platform-infra`

> Project-level agent context for **platform-infra**. Inherited **on top of** the workspace spine (`../CLAUDE.md`) and the canonical contract (`../PLATFORM_SPEC.md §2.5`). If anything here disagrees with the spec, **the spec wins**.

## What this repo is
Shared AWS foundation: VPC, subnets, ECS cluster, ALB, Route53, ECR, shared RDS, MSK/Redis. **Exposes the remote-state outputs every service/MFE repo reads** via `data "terraform_remote_state" "platform"`. Apply order: 2nd (after terraform-modules).

- **Stack:** Terraform >= 1.6 · AWS provider ~> 5.40 · region `us-east-1` · default env `dev`.
- **Conventions:** `../PLATFORM_SPEC.md §2.5` (remote state, module inputs, apply order).

## Write-scope rules (spine convention)
- Edit **this repo only**. Changing a shared module input or a remote-state output is a **cross-repo change** — update `../PLATFORM_SPEC.md §2.5` and every consuming repo, and follow `../CONTRIBUTING-cross-repo.md`.
- Respect the apply order: `terraform-modules` → `platform-infra` → services → MFEs.
