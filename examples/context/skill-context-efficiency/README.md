# Efficient vs. inefficient Skills — a context-cost A/B

Two Agent Skills that do the **exact same job** — answer *"what's the price and stock
of `<product>`?"* over the same 2,000-product catalog — written two ways. They return
the **same answer**. The only thing that differs is **how many tokens land in the
context window when the skill fires**.

One pulls the **haystack**. The other pulls the **needle**.

| | `product-lookup-bloated` | `product-lookup-lean` |
|---|---|---|
| `SKILL.md` body | ~760 tok (full rule table inlined) | ~180 tok (15 lines + pointers) |
| Runtime load | whole `catalog.json` + whole `pricing-rules.md` | one matched row from a script |
| **Total context when applied** | **~84,400 tok** | **~220 tok** |
| Answer | identical | identical |

**≈ 380× more context for the same result.** *(byte/4 estimates — the same heuristic
the [context-overflow guard](../context-overflow-guard/README.md) uses; confirm the
exact split with `/context`, see "Measure it" below.)*

---

## The two axes of a Skill's context cost

A Skill can waste context in **two** places, and this demo shows both:

1. **The `SKILL.md` body itself** — injected into context the moment the skill triggers.
   The bloated skill inlines the entire pricing reference (rationale, history, edge
   cases) so you pay for it on *every* call, used or not. The lean skill is 15 lines.
2. **What the skill tells Claude to load at runtime.** The bloated skill says *"read the
   entire catalog"*; the lean skill runs `scripts/lookup.py`, which returns only the one
   matched row.

The lesson in one line: **a Skill should locate and load the needle, not dump the haystack.**

This is [progressive disclosure](../../docs/context-engineering/thoughtworks/progressive-context-disclosure-technology-radar.md)
at the skill level, and the same "selective reading — search over read" and "subagent
offloading" principles the [context-overflow guard](../context-overflow-guard/README.md)
enforces.

---

## The two skills, side by side

**❌ `product-lookup-bloated`** ([SKILL.md](.claude/skills/product-lookup-bloated/SKILL.md))
> Read the entire `data/catalog.json` (all ~2,000 products) … read the entire
> `data/pricing-rules.md` … *(+ the full rule table reproduced inline in the body)*

**✅ `product-lookup-lean`** ([SKILL.md](.claude/skills/product-lookup-lean/SKILL.md))
> Run `python3 scripts/lookup.py "<id-or-name>"`. It returns **only** the matched
> product as JSON. Report that. Need a rule's rationale? `grep` just that rule.

Same prompt, same correct answer — only the context bill differs.

---

## Run it

```bash
cd examples/skill-context-efficiency
python3 scripts/gen_catalog.py        # writes data/catalog.json (deterministic)
python3 scripts/lookup.py SKU-1042    # the lean path: one row of JSON
```

Then open Claude Code **in this folder** and ask the same question twice:

```
/product-lookup-bloated   →  "What's the price and stock of Cobalt Sprocket 1042?"
/clear
/product-lookup-lean      →  "What's the price and stock of Cobalt Sprocket 1042?"
```

## Measure it

After each run, type `/context` and read off the token usage. You'll see the bloated
run balloon (it tries to drag the whole catalog in — large enough that it even bumps
the default `Read` line cap), while the lean run barely moves the needle. Both print
`SKU-1042 · member-price · $13.17 · stock 365`.

Drop your two `/context` numbers in here to make the delta yours, not just ours:

| Run | `/context` tokens |
|---|---|
| `product-lookup-bloated` | _your number_ (est. ~84k) |
| `product-lookup-lean`    | _your number_ (est. ~220) |

---

## Files

```
.claude/skills/product-lookup-bloated/SKILL.md   # inefficient: pulls the haystack
.claude/skills/product-lookup-lean/SKILL.md      # efficient: pulls the needle
scripts/gen_catalog.py                           # deterministic catalog generator
scripts/lookup.py                                # id-or-name -> one product, stdlib only
data/catalog.json                                # 2,000 products (generated, committed)
data/pricing-rules.md                            # the rule table the lean skill greps on demand
```

Everything is stdlib-only and dependency-free. Re-running `gen_catalog.py` leaves
`git diff` clean.
