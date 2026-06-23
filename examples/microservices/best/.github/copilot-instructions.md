# GitHub Copilot — QuickBite workspace instructions

> ⚠️ Copilot does **not** reliably auto-discover and merge every repo's instructions
> across an open multi-root workspace. This file is the **single routing table** so both
> Copilot and developers know where conventions live. Keep it in the shared spine and
> update it in the **same PR** that adds a new repo.

## Where the rules live

| You need… | Read… |
|-----------|-------|
| The canonical contract (ports, routes, Feign URLs, topics, TF inputs) | `PLATFORM_SPEC.md` (**authoritative — wins over code**) |
| How the platform fits together | `CLAUDE.md` (workspace spine) · `ARCHITECTURE.md` · `docs/dependency-graph.md` |
| Rules for a specific repo | `<repo>/CLAUDE.md` (port, deps, callers, events, write-scope) |
| How to make a change spanning repos | `CONTRIBUTING-cross-repo.md` |

## Guardrails

- **`PLATFORM_SPEC.md` is the source of truth.** Use its exact ports, base packages
  (`com.quickbite.<svc>`), gateway routes (`/api/**`), Feign targets, and Kafka topics.
- **Edit one repo at a time.** Read across repos freely; write only to the repo in scope.
- **Cross-repo contract change** (OpenAPI / `/internal/**` / event schema): update the
  spec, the producer, and **every consumer** (listed in each repo's `CLAUDE.md`), then run
  `tools/check-contracts.sh`.
- MFEs call services **only through the gateway** (`VITE_GATEWAY_URL`); `/internal/**` is
  never public.
- Generate files only — no `mvn` / `npm install` / `terraform` runs.
