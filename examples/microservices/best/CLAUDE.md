# CLAUDE.md — QuickBite workspace spine

> **You are at the workspace root of a 24-repo polyrepo** (15 Java/Spring Boot services + 7 React MFEs + `platform-infra` + `terraform-modules`). In production each folder is its own Git repository; here they are checked out side-by-side so an agent can see the whole platform at once.
>
> This file is the **spine**: a context-orchestration layer that sits *above* the code repos. The repos stay independent and unaware of it. Commits still go to each repo's own origin/CI/branch protection.

## 🔑 The one rule

**`PLATFORM_SPEC.md` in this folder is the single source of truth.** Every port, database, package name, gateway route, Feign URL, Kafka topic and Terraform input is defined there. **If a repo's code disagrees with the spec, the spec wins** — and that disagreement is a bug to fix, not a fact to copy. Read it before any cross-service change.

Companion docs: `ARCHITECTURE.md` (narrative), `docs/dependency-graph.md` (Mermaid graphs).

## The platform in one screen

| Tier | Repos | Coupling |
|------|-------|----------|
| Edge | `api-gateway` (8080) | routes `/api/**`, validates JWT, forwards `X-User-Id/Role` |
| Services | 14 Spring Boot services (8081–8094) | **sync** via Feign `/internal/**`; **async** via Kafka |
| MFEs | `shell` (3000) + 6 remotes (3001–3006) | call services **only through the gateway** |
| Infra | `platform-infra`, `terraform-modules` | remote-state outputs + reusable modules |

- **Sync graph** (who calls whom) and **event topics** (`orders.events`, `payments.events`, `deliveries.events`, `restaurant.events`, `menu.events`) → `PLATFORM_SPEC.md §1.1` and `§5`.
- **Conventions** (Java/React/Terraform stack, naming, ports, `/internal` rule) → `PLATFORM_SPEC.md §2`. Do not deviate.

## How to work here — two roles

1. **Explorer (read-only):** to understand or trace a flow, read across *all* repos (Read/Grep/Glob). Start from `PLATFORM_SPEC.md`, then the relevant repo's `CLAUDE.md`, then its `api/openapi.yaml`.
2. **Worker (write-scoped):** when implementing, work in **one repo at a time** and obey **that repo's own `CLAUDE.md`**. Every repo has one. Do **not** edit a sibling repo to "fix" a caller as a side effect.

## Cross-repo changes (the thing polyrepos get wrong)

A change to an `api/openapi.yaml`, a `/internal/**` endpoint, or a Kafka event schema is **never local**. Before you stop:
1. Update `PLATFORM_SPEC.md` first (the contract leads the code).
2. Update the producer **and every consumer** — find them in the target repo's `CLAUDE.md` ("Called by" / "Events consumed").
3. Run `./tools/check-contracts.sh` to catch breaking OpenAPI diffs.
4. Follow `CONTRIBUTING-cross-repo.md` (change-set ID, coordinated PRs, merge model).

> Worked example — **adding a checkout tip** touches exactly four repos on the "money path": `pricing-service` → `order-service` → `payment-service` → `checkout-mfe`, plus `PLATFORM_SPEC.md §3`. Each of those repos' `CLAUDE.md` flags this. See `CONTRIBUTING-cross-repo.md` for the full walkthrough.

## Spine files in this folder

- `PLATFORM_SPEC.md` — canonical contract (authoritative).
- `<repo>/CLAUDE.md` — per-repo project context (port, deps, callers, events, write-scope).
- `AGENTS.md`, `.github/copilot-instructions.md` — same context for non-Claude agents.
- `.meta` — repo manifest (`meta git clone`/`meta exec` bootstrap & fan-out).
- `quickbite.code-workspace` — VS Code multi-root workspace (open all 24 at once).
- `components.lock.json` — pinned working versions of every repo (integration baseline).
- `tools/check-contracts.sh` + `.github/workflows/contract-check.yml` — contract-drift gate.
- `CONTRIBUTING-cross-repo.md` — how to make a change that spans repos.

## Do not

- Do **not** run builds (`mvn`, `npm install`, `terraform init`) — create/edit files only.
- Do **not** invent ports, paths, package names, or event shapes — they are all in the spec.
- Do **not** expose `/internal/**` through the gateway.
