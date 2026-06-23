# CLAUDE.md — `payment-service`

> Project-level agent context for **payment-service**. Inherited **on top of** the workspace spine (`../CLAUDE.md`) and the canonical contract (`../PLATFORM_SPEC.md`). If anything here disagrees with the spec, **the spec wins**.

## What this repo is
Validates order amount, optionally debits wallet, captures via mock PSP, emits PaymentCaptured.

- **Port:** `8089`  ·  **Database:** `payment_db`  ·  **Gateway route:** `/api/payments`
- **Stack:** Java 21 / Spring Boot 3.3.2 · base package `com.quickbite.payment`
- **Exact contract:** `./api/openapi.yaml` + `../PLATFORM_SPEC.md` (§1.1 deps, §3 domain, §4 routes, §5 events). **Do not guess endpoint shapes — read them.**

## Cross-repo coupling — read before touching the contract
- **Calls (sync, Feign `/internal/**`):** `order-service`, `wallet-service`, `identity-service`
- **Called by (sync):** `notification-service`
- **Events produced:** `payments.events`
- **Events consumed:** —

## ⚠️ This repo sits on the "money path"
A checkout **tip / gratuity** crosses four repos in this order:
`pricing-service` (add a tip line to the quote total) → `order-service` (store it in the immutable pricing snapshot) → `payment-service` (capture the total *including* tip) → `checkout-mfe` (collect the tip input).
If you add or change a tip/total field here, mirror it across **all four** repos and in `../PLATFORM_SPEC.md §3` (pricing quote response) before you consider the task done. Read `../PLATFORM_SPEC.md §6` for the end-to-end flow.

## Write-scope rules (spine convention)
- You are scoped to **this repo**. Make code edits **here only** — never reach into a sibling repo's files to "fix" a caller.
- A change to `api/openapi.yaml`, a `/internal/**` endpoint, or an event schema is a **cross-repo change**: update `../PLATFORM_SPEC.md`, every caller / consumer listed above, then run `../tools/check-contracts.sh` and follow `../CONTRIBUTING-cross-repo.md` (change-set ID, coordinated PRs).
- Keep ports, base-package names, topic names and gateway routes **exactly** as the spec defines them.
