# CLAUDE.md — `wallet-service`

> Project-level agent context for **wallet-service**. Inherited **on top of** the workspace spine (`../CLAUDE.md`) and the canonical contract (`../PLATFORM_SPEC.md`). If anything here disagrees with the spec, **the spec wins**.

## What this repo is
Customer wallet balance + transactions; internal debit/credit.

- **Port:** `8090`  ·  **Database:** `wallet_db`  ·  **Gateway route:** `/api/wallets`
- **Stack:** Java 21 / Spring Boot 3.3.2 · base package `com.quickbite.wallet`
- **Exact contract:** `./api/openapi.yaml` + `../PLATFORM_SPEC.md` (§1.1 deps, §3 domain, §4 routes, §5 events). **Do not guess endpoint shapes — read them.**

## Cross-repo coupling — read before touching the contract
- **Calls (sync, Feign `/internal/**`):** `identity-service`
- **Called by (sync):** `payment-service`
- **Events produced:** —
- **Events consumed:** —

## Write-scope rules (spine convention)
- You are scoped to **this repo**. Make code edits **here only** — never reach into a sibling repo's files to "fix" a caller.
- A change to `api/openapi.yaml`, a `/internal/**` endpoint, or an event schema is a **cross-repo change**: update `../PLATFORM_SPEC.md`, every caller / consumer listed above, then run `../tools/check-contracts.sh` and follow `../CONTRIBUTING-cross-repo.md` (change-set ID, coordinated PRs).
- Keep ports, base-package names, topic names and gateway routes **exactly** as the spec defines them.
