---
name: product-lookup-lean
description: >-
  Look up a product's price and stock by id or name. Returns just the matched
  product. Use for any single-product price/stock question.
---

# Product lookup (lean — the right way)

The script and data live at the **repo root** (`scripts/`, `data/`) — not in this
skill folder. Run everything from the repo root.

1. Run `python3 scripts/lookup.py "<id-or-name>"` from the repo root. It searches
   `data/catalog.json`, applies the product's pricing rule, and prints **only** the
   matched product as JSON (`id`, `name`, `price`, `stock`, `applied_rule`).
2. Report that result.

Only if the user asks *why* a price was adjusted: `grep -n "<rule-name>"
data/pricing-rules.md` and read just those lines — don't open the whole file.

Never read `data/catalog.json` directly; the script returns the one row you need.
