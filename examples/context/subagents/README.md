# Subagent context overflow — what hooks see

Demonstrates, in one run:

1. **A subagent overflows its context** — a Haiku `overflower` subagent reads ~40
   large filler files (far past Haiku's window).
2. **Mid-task detail is dropped in the summary** — a `CANARY=` token planted in the
   *first* file is asked for at the *end*. After compaction it comes back garbled or
   as `CANARY LOST`.
3. **`PreCompact` / `PostCompact` never fire for the subagent** — those hooks are
   main-session only, so the subagent's own compaction is invisible to them.
4. **`SubagentStop` is the only thing that fires** — and the run prints the *exact*
   JSON payload it received.

## Files

| File | Role |
|------|------|
| `.claude/agents/overflower.md` | The subagent — pinned to `model: haiku`, `tools: Read` |
| `.claude/settings.json` | Hooks for `PreCompact`, `PostCompact`, `SubagentStop` |
| `hooks/log.sh` | Logs each firing + dumps the raw stdin payload to `logs/last-<hook>.json` |
| `prompt.md` | What the main agent is told (spawn the subagent, print its answer) |
| `run.sh` | Generates filler, runs the demo, prints the evidence |

## Run

```bash
bash run.sh           # tune size with: N=60 LINES=2000 bash run.sh
```

Requires the `claude` CLI and `jq`. Reads ~400k tokens on Haiku — cheap, not free.

## What to look at afterward

- `logs/fired.log` — should list **SubagentStop** but **not** PreCompact/PostCompact.
- `logs/last-SubagentStop.json` — the exact payload (note it carries a
  `transcript_path`, not the subagent's output text).
- `logs/run.out` — the subagent's final line vs. the real canary printed below it.

If nothing compacted (canary came back intact), the filler wasn't big enough —
bump `N`. The hook evidence (3 + 4) holds regardless.
