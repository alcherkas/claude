---
name: product-lookup-bloated
description: >-
  INEFFICIENT reference variant. Look up a product's price and stock by id or
  name. Demonstrates the anti-pattern: pulls the whole catalog and the whole
  pricing reference into context on every call.
---

# Product lookup (bloated — the wrong way)

To answer **any** product question, do the following every time:

1. **Read the entire catalog** at `data/catalog.json` (all ~2,000 products) so you
   have the full dataset in context.
2. **Read the entire** `data/pricing-rules.md`.
3. Scan what you loaded for the product the user named, apply its pricing rule using
   the full table below, and report `id`, `name`, `price`, and `stock`.

## Full pricing reference (inlined so it's always in context)

The selling price is `base_price` adjusted by the product's `pricing_rule`. The
complete rule set, with rationale, decision history, and edge cases, is reproduced
here in full so you never have to look anything up — it is loaded into context on
every single invocation whether or not the user's product even uses the rule.

### Rule: `standard`
- **Adjustment:** none — sell at `base_price`.
- **Rationale:** the default for steady-demand items with no active promotion.
- **History:** this has been the fallback since launch; do not discount unless a
  more specific rule is attached to the product.
- **Edge cases:** if `base_price` is missing, treat as a data error and stop.

### Rule: `bulk-discount`
- **Adjustment:** 10% off **when `stock > 100`**, otherwise no change.
- **Rationale:** we are carrying too many units; nudge volume. We deliberately do
  *not* discount when stock is thin so we never sell below cost on scarce inventory.
- **History:** the threshold was 50 until Q3, then raised to 100 after margin review.
- **Edge cases:** exactly 100 units in stock does **not** qualify (strict `>`).

### Rule: `member-price`
- **Adjustment:** 15% off.
- **Rationale:** loyalty-program pricing; margin is recovered through repeat
  purchase and higher lifetime value.
- **History:** introduced with the membership tier; applies regardless of stock.
- **Edge cases:** never stacks with `clearance`; the product carries exactly one rule.

### Rule: `clearance`
- **Adjustment:** 35% off.
- **Rationale:** end-of-life or seasonal items we want off the shelf quickly, even
  at a thin or negative margin, to free warehouse space.
- **History:** the deepest standing discount; flagged for finance review monthly.
- **Edge cases:** clearance items may show `stock` of 0 — still report them.

### Rule: `bundle`
- **Adjustment:** 5% off.
- **Rationale:** sold as part of a kit; the small per-item cut is offset by the
  attach rate of the other kit components.
- **History:** added when kits launched; the smallest of the standing discounts.
- **Edge cases:** the discount applies to the standalone price shown here.

> This skill is intentionally wasteful. Compare it with `product-lookup-lean`: same
> answer, a fraction of the context. See this folder's `README.md`.
