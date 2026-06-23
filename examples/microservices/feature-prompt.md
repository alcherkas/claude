# Feature prompt — the demo task

Paste the **exact same prompt** into a Claude Code session rooted in `naive/` and another
rooted in `best/`. Do **not** add hints about which services to change — discovering that
scope is the whole point of the experiment. Reset with `git restore .` between runs.

> ⚠️ The prompt is deliberately **outcome-focused** (what the customer should get), not a
> task list. It never names `pricing` / `order` / `payment` / `checkout` or the contract.
> The `best/` agent figures the scope out from the spine; the `naive/` agent has to grep.

---

## Prompt (copy verbatim)

```
Add support for customer tips to QuickBite.

At checkout, a customer should be able to add an optional tip for their courier —
either a preset (e.g. 10% / 15% / 20%) or a custom amount. The tip must be:

  - shown to the customer as part of the order total before they pay,
  - included in the amount they are actually charged,
  - stored with the order so it appears on the receipt and in order history.

Implement it consistently across the whole platform — the data model, the APIs, the
price/total calculation, and the checkout UI — so the tip flows correctly from the
customer entering it, through payment, to the recorded order. Keep everything
internally consistent: contracts, field names, and types should line up across
services.

Don't run any builds (no mvn / npm install / terraform) — just make the code changes.
```

---

## Acceptance criteria (behavioural — for grading, don't paste)

A correct implementation, in either folder, should:

1. Let the customer choose a preset **or** custom tip at checkout.
2. Add the tip into the **total** shown before payment.
3. Charge the customer the total **including** the tip.
4. **Persist** the tip on the order (receipt + history).
5. Keep the cross-service contract consistent — the same tip/total fields and types
   everywhere they appear.

## Facilitator notes

- **Expected scope:** `pricing-service` (tip in the quote total) → `order-service` (tip in
  the immutable pricing snapshot) → `payment-service` (capture total incl. tip) →
  `checkout-mfe` (tip input). In `best/`, also `PLATFORM_SPEC.md §3`. That's 4 repos
  (+ the spec); the common miss is `payment-service`.
- **What to observe / measure:** see [`differences.md`](./differences.md) → "What to watch
  for" and "What to measure".
- Optionally re-run with **GitHub Copilot** (it reads `best/.github/copilot-instructions.md`).
