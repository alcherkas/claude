# Pricing rules

Each product in `catalog.json` carries a `pricing_rule`. The selling price is the
`base_price` adjusted by that rule. `scripts/lookup.py` implements these exactly.

| Rule            | Adjustment                                  | Rationale |
|-----------------|---------------------------------------------|-----------|
| `standard`      | none — sell at `base_price`                 | The default for steady-demand items with no promotion. |
| `bulk-discount` | **10% off** when `stock > 100`, else none   | We carry too many units; nudge volume without going below cost on thin stock. |
| `member-price`  | **15% off**                                 | Loyalty-program pricing; margin is recovered through repeat purchase. |
| `clearance`     | **35% off**                                 | End-of-life or seasonal items we want off the shelf quickly. |
| `bundle`        | **5% off**                                  | Sold as part of a kit; the small cut is offset by attach rate. |

> Teaching note: this file is small on purpose. The point of the demo is not the
> rules themselves — it's that the **bloated** skill drags this whole table (and the
> entire catalog) into context on every call, while the **lean** skill only `grep`s
> the one rule a user actually asks about.
