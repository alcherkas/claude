# Structured return — fan out so nothing overflows

The fix paired with [`../overflow`](../overflow). There, one Haiku subagent tries to
hold every file's `MARK` code and **loses the early ones** when its context compacts.

Here we don't make one agent hold them all. The parent **fans out one bounded
`reader` subagent per file**; each reads only its own file (~40k tokens — nowhere near
the window) and returns a structured `{"NNN":"code"}`. The parent merges them, so
**every code survives, early ones included**.

Two ideas working together:
- **Scope so nothing overflows** — each fact lives in its own fresh context.
- **Structured return** — each reader returns a validated object, not prose, so the
  parent can merge results mechanically instead of scraping paragraphs.

## Files

| File | Role |
|------|------|
| `.claude/agents/reader.md` | The per-file subagent — `model: haiku`, `tools: Read` |
| `run.sh` | Generates the same filler as `../overflow`, fans out, verifies every code |

## Run

```bash
bash run.sh
```

The per-file table should be **all `kept`** (contrast `../overflow`, where the early
files come back `LOST`).

## In real pipelines

The enforced version is the Workflow tool's schema option —
`agent(prompt, { schema })` forces each subagent to call a `StructuredOutput` tool and
returns the *validated object*, so a malformed result is rejected and retried instead
of silently merged.
