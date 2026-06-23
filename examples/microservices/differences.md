# `naive/` vs `best/` — what differs and why it matters

Two copies of the **same 24-repo QuickBite polyrepo**, used to demo how much an AI coding
agent's efficiency depends on the **context/"spine" layer** around the repos — not the
code itself.

> **Controlled experiment:** the application code in the 24 repos is **byte-for-byte
> identical** between `naive/` and `best/`. No drift, no broken code, no sabotage. The
> **only** variable is the spine — the canonical contract, the layered instruction files,
> the repo manifest, and the contract tooling that sit *above* the repos. So any
> difference you see in an agent's speed or correctness is attributable to context alone.

The patterns applied in `best/` come from the deep-research report and this project's
curated references under `docs/context-engineering/` (the **spine / repo-of-repos /
bootstrap-repo** pattern, **layered `CLAUDE.md`**, the **`meta` manifest** + **multi-root
workspace**, **contract-first + oasdiff**, and **change-set** orchestration).

---

## Folder inventory — the delta

| Artifact | `naive/` | `best/` | Why it matters to an agent |
|----------|:--------:|:-------:|----------------------------|
| 24 service/MFE repos (full code) | ✅ | ✅ | Identical — the constant in the experiment |
| `README.md`, `ARCHITECTURE.md`, `docs/` | ✅ | ✅ | Both have narrative docs (prose, not authoritative) |
| **`PLATFORM_SPEC.md`** (canonical contract) | ❌ | ✅ | The single source of truth for ports, packages, routes, Feign URLs, Kafka topics, TF inputs. Without it the agent must reconstruct the contract by grepping 24 repos and may guess wrong |
| **Root `CLAUDE.md`** | ⚠️ thin (≈18 lines) | ✅ rich spine | naive's is a generic "it's microservices, run `make up`" stub with **no** topology, coupling, or write-scope rules. best's is a true workspace spine |
| **`AGENTS.md`** / **`.github/copilot-instructions.md`** | ❌ | ✅ | Same context for non-Claude agents; centralized so Copilot (which does *not* reliably auto-merge per-repo instructions) has one routing table |
| **Per-repo `<repo>/CLAUDE.md`** (×24) | ❌ (0) | ✅ (24) | Every repo an agent opens states its port, sync deps, **who calls it**, events produced/consumed, and the "edit this repo only" rule |
| **`.meta`** (repo manifest) | ❌ | ✅ | `meta git clone` bootstraps all 24 in one command; `meta exec` fans a command across them |
| **`quickbite.code-workspace`** (multi-root) | ❌ | ✅ | Opens all 24 as one VS Code workspace so indexing/grep spans the platform |
| **`components.lock.json`** | ❌ | ✅ | Integration baseline pinning every repo to a working version ("do these versions work together?") |
| **`tools/check-contracts.sh`** + **`.github/workflows/contract-check.yml`** | ❌ | ✅ | oasdiff gate: a breaking `api/openapi.yaml` change fails CI instead of breaking at runtime |
| **`CONTRIBUTING-cross-repo.md`** | ❌ | ✅ | Change-set IDs + four merge models + the tip change worked end-to-end |

**Net:** `best/` adds 1 canonical spec + ~10 spine artifacts + 24 per-repo context files
(36 files) and upgrades the root `CLAUDE.md`. Nothing else changes.

---

## The two root `CLAUDE.md` files

- **`naive/CLAUDE.md`** — what a team usually writes: a short blurb, the `make` commands,
  "services talk over HTTP and there's some Kafka." It is *present* (so this isn't a
  "has a CLAUDE.md vs doesn't" strawman) but it carries **no cross-repo knowledge**.
- **`best/CLAUDE.md`** — a spine: the canonical-spec rule, the tier map, the
  explorer/worker operating model, and the cross-repo change protocol — with pointers
  into `PLATFORM_SPEC.md` and each repo's own `CLAUDE.md`.

## Side effect of removing the spec (left intentionally)

`naive/README.md` still ends with "Further reading → `PLATFORM_SPEC.md`", but the file is
gone — a **dangling pointer**. This is realistic drift: docs reference an authority that
no longer exists, so an agent following the breadcrumb hits a dead end and falls back to
guessing. (We did **not** edit naive's code to create this; it falls out of removing the
spec.)

---

## The demo: run the same task in both folders

**Task:** *"Add an optional **tip / gratuity** to checkout."*

It is a true cross-repo change on the **money path**:
`pricing-service` (tip in the quote total) → `order-service` (tip in the immutable
pricing snapshot) → `payment-service` (capture total **incl. tip**) → `checkout-mfe`
(tip input) → and the contract in `PLATFORM_SPEC.md §3`.

### How to run

1. Open a Claude Code session **rooted in `best/`**, give it the task verbatim.
2. Open a separate session **rooted in `naive/`**, give it the *same* task.
3. Compare. (Optionally repeat with GitHub Copilot using `.github/copilot-instructions.md`.)

### What to watch for (the efficiency gap)

| | `naive/` (no spine) | `best/` (spine) |
|---|---|---|
| **Orientation** | Greps across 24 repos to find what "tip/total/price" touches | Reads `PLATFORM_SPEC.md §3/§6` + the money-path note in 4 repos' `CLAUDE.md` → knows the scope immediately |
| **Completeness** | Likely edits `pricing` + `checkout`, **misses `payment-service`** (its coupling is "validate order amount", never the word "tip") | The four repos are named up front; nothing is missed |
| **Contract correctness** | Guesses the quote response shape (no authoritative spec) | Uses the exact `{subtotalCents…totalCents}` contract |
| **Discipline** | May edit several repos in one tangle; no record of the cross-repo change | change-set ID, contract leads code, `check-contracts.sh` gate, coordinated PRs |
| **Verification** | None — breakage surfaces at runtime | `tools/check-contracts.sh` catches a breaking OpenAPI diff before merge |

### What to measure

- **Tool calls / turns / wall-clock** to a correct change.
- **Repos correctly touched** (target: pricing, order, payment, checkout — 4/4).
- **Contract accuracy** (did it match the spec's field names / cents convention?).
- **Misses & hallucinations** (skipped consumer, invented endpoint/port).

---

## Reset between runs

The demo edits are committed as a baseline first (see below), so after each run:

```bash
git restore .            # discard the agent's working-tree edits
# or, if the agent committed: git reset --hard <baseline-commit>
```

Keep the folders' code identical between runs so each agent starts from the same state.
