# Making a change that spans repositories

GitHub has **no native multi-repo change object** — issues, PRs and workflows are
repo-scoped. So a change that crosses repos (a contract, a `/internal/**` endpoint, a
Kafka event, a shared Terraform module) must be modeled **explicitly**. This is the
discipline the spine exists to enforce.

## 1. Open a change set

1. Pick a change-set ID: `CHG-<n>` (e.g. `CHG-1042`).
2. Create a **parent tracking issue** in the spine with: scope, impacted repos,
   acceptance criteria, and the chosen merge model (below).
3. Open a **child issue in each impacted repo**, linked to the parent (GitHub cross-repo
   sub-issues work well here).
4. Use the ID everywhere: branch `changeset/CHG-1042/add-tip`, PR titles
   `[CHG-1042] …`, and a `CHG-1042` label. That string is how humans and CI correlate
   the otherwise-independent PRs.

## 2. Drive the change contract-first

1. **Edit `PLATFORM_SPEC.md` first.** The contract leads the code.
2. Change the **producer** repo (the one that owns the endpoint / event).
3. Change **every consumer** — find them in the producer repo's `CLAUDE.md`
   ("Called by" for sync, "Events consumed" for async).
4. Run `./tools/check-contracts.sh` to diff each `api/openapi.yaml` and flag breaking
   changes before review.

## 3. Choose a merge model (by risk)

| Model | Use when | How |
|-------|----------|-----|
| **Integration branch** | Breaking contract/event spanning many repos | All PRs target a shared integration branch; validate together; fast-forward to mains |
| **Meta-repo manifest** | Coordinated release of several repos | Bump `components.lock.json`; the spine's CI replays integration + contract tests against the pinned set |
| **Versioned artifacts** | Non-breaking; consumers can adopt later | Producer releases a new version; each consumer bumps on its own cadence |
| **Linked PRs + merge gating** | Small, 2–3 repos | Cross-link PRs; a required check blocks merge until siblings are green |

Breaking **Feign** or **Kafka** changes → *integration branch* or *meta-repo manifest*.
A routine shared-client bump → *versioned artifacts*.

## 4. Worked example — add an optional checkout **tip**

A tip crosses the **money path**: `pricing-service → order-service → payment-service →
checkout-mfe`, plus the contract.

| Step | Repo | Change | Merge model |
|------|------|--------|-------------|
| 0 | *spine* | `PLATFORM_SPEC.md §3`: add `tipCents` to the pricing quote response and `§6` note; open `CHG-1042` parent issue | — |
| 1 | `pricing-service` | add `tipCents` to the quote DTO + include it in `totalCents`; update `api/openapi.yaml` | integration branch |
| 2 | `order-service` | persist `tipCents` in the immutable pricing snapshot (it calls pricing) | integration branch |
| 3 | `payment-service` | capture the total **including** tip (it validates the order amount) | integration branch |
| 4 | `checkout-mfe` | add the tip input; send it in the quote/checkout call (via gateway) | integration branch |
| 5 | *spine* | `./tools/check-contracts.sh` green; bump `components.lock.json`; merge the set | — |

**Why the spine makes this fast:** the agent reads `PLATFORM_SPEC.md §3/§6` and each
repo's `CLAUDE.md`, so it knows *up front* these four repos (and only these) are
involved and what each must change. Without the spine it must grep all 24 repos to
discover the chain — and will likely miss `payment-service` (the coupling is "validate
order amount", not the word "tip").
