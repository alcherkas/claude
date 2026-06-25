# Structured return — surviving subagent context overflow

The fix paired with [`../overflow`](../overflow). Same Haiku subagent, same ~40
overflowing filler files — but here the canary comes back **intact**.

## The idea

Instead of "read everything, then recall," the parent hands the subagent an
**output contract**:

```json
{"canary": "<the CANARY= line from filler/000.txt>", "scanned": <int>}
```

Because the subagent must *return that shape*, it fills each field from its source
(re-reading `filler/000.txt`) instead of trusting a transcript that compaction has
already thinned. The answer lives in the **return value**, not stranded mid-context.

## Files

| File | Role |
|------|------|
| `.claude/agents/extractor.md` | Haiku subagent, `tools: Read`, must emit the JSON contract |
| `prompt.md` | Tells the main agent to spawn it and print its answer |
| `run.sh` | Generates filler, runs it, then verifies `.canary` == the planted value |

## Run

```bash
bash run.sh        # prints RESULT: PASS when the fact survives
```

Contrast: `../overflow` returns a garbled canary / `CANARY LOST`; this one PASSes.

## In real pipelines

The enforced version of this is the Workflow tool's schema option —
`agent(prompt, { schema })` forces the subagent to call a `StructuredOutput` tool
and returns the *validated object*, so a malformed or hallucinated result is
rejected and retried instead of silently passed up.
