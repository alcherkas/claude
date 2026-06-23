# CLAUDE.md — `account-mfe`

> Project-level agent context for **account-mfe**. Inherited **on top of** the workspace spine (`../CLAUDE.md`) and the canonical contract (`../PLATFORM_SPEC.md`). If anything here disagrees with the spec, **the spec wins**.

## What this repo is
Profile, wallet, past orders, reviews.

- **Dev port:** `3004`  ·  **Module-Federation name:** `account`
- **Stack:** React 18.3 / TypeScript 5.5 / Vite 5.4 / `@originjs/vite-plugin-federation` · TanStack Query + Axios
- **API base URL:** `import.meta.env.VITE_GATEWAY_URL` (defaults to `http://localhost:8080`). **All calls go through the gateway** — never call a service port directly.

## Cross-repo coupling
- **Talks to (via gateway):** `identity-service`, `wallet-service`, `review-service`, `order-service`
- **Exact request/response shapes:** the relevant service's `../<service>/api/openapi.yaml` + `../PLATFORM_SPEC.md §3`.

## Write-scope rules (spine convention)
- You are scoped to **this repo**. Make code edits **here only** — never reach into a sibling repo's files to "fix" a caller.
- A change to `api/openapi.yaml`, a `/internal/**` endpoint, or an event schema is a **cross-repo change**: update `../PLATFORM_SPEC.md`, every caller / consumer listed above, then run `../tools/check-contracts.sh` and follow `../CONTRIBUTING-cross-repo.md` (change-set ID, coordinated PRs).
- Keep ports, base-package names, topic names and gateway routes **exactly** as the spec defines them.
