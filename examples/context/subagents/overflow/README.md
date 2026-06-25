# Subagent context overflow — what hooks see

A Haiku `overflower` subagent reads a stack of large files in order — each carrying a
unique `MARK-### = code` on its first line — then is asked to recall *every* code from
memory. One run shows:

1. **The subagent overflows and silently compacts** — it reads far more than its
   window holds (each file ~40k tokens; ~9 reads ≈ 360k vs Haiku's ~200k), so its
   context is summarized mid-task.
2. **Early details fall out** — codes from the first files come back `LOST` while the
   most recent ones survive. Compaction is lossy and recency-biased. (A *single* value
   the agent is explicitly told to guard usually survives — it keeps restating it —
   which is why one lone "canary" can't demonstrate loss. You need many incidental
   facts it can't all cling to.)
3. **`PreCompact` / `PostCompact` never fire** — those hooks are main-session only, so
   the subagent's own compaction is invisible to them.
4. **`SubagentStop` is the only hook that fires** — the run prints its exact payload,
   which includes `last_assistant_message` (the subagent's final text), `agent_id`,
   `agent_type`, and `agent_transcript_path`.

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
bash run.sh           # push harder with: N=16 LINES=3000 bash run.sh
```

Requires the `claude` CLI and `jq`. Reads a few hundred k tokens on Haiku.

## What to look at afterward

- The **kept / LOST / WRONG** table `run.sh` prints — early files `LOST`, later kept.
- `logs/fired.log` — **SubagentStop** only; no PreCompact/PostCompact.
- `logs/last-SubagentStop.json` — the exact payload the hook received.

If everything is `kept`, the subagent didn't read enough to overflow — raise `LINES`.
The hook evidence (3 + 4) holds regardless.
