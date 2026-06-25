# Subagent context overflow — what hooks see

Demonstrates, in one run:

1. **A subagent overflows its context** — a Haiku `overflower` subagent reads large
   filler files until it blows past Haiku's window (each file is ~45k tokens, so a
   handful of reads is enough).
2. **Mid-task detail can be dropped in the summary** — a `CANARY=` token planted in
   the *first* file is asked for at the *end*. Once its context compacts, the answer
   may come back garbled or as `CANARY LOST` (compaction is lossy, not guaranteed to
   drop any specific line — re-run if it survives).
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
bash run.sh           # tune size with: LINES=3000 bash run.sh
```

Requires the `claude` CLI and `jq`. Reads a few hundred k tokens on Haiku — cheap,
not free.

## What to look at afterward

- `logs/fired.log` — should list **SubagentStop** but **not** PreCompact/PostCompact.
- `logs/last-SubagentStop.json` — the exact payload. It carries `agent_id`,
  `agent_type`, an `agent_transcript_path`, **and `last_assistant_message`** (the
  subagent's actual final text — so the hook sees the output, not just a pointer).
- `logs/run.out` — the subagent's final line vs. the real canary printed below it.

A subagent reads only ~10 files before stopping, so overflow has to happen *within*
those reads — that's why each file is large. If the canary survives, the subagent
didn't read enough to overflow: raise `LINES` (file size), not `N`. The hook
evidence (3 + 4) holds regardless.
