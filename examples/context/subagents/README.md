# Subagents & context overflow

Two paired, runnable demos using a Haiku subagent:

- [`overflow/`](overflow) — the problem: the subagent overflows its context, drops a
  mid-task canary, and `PreCompact`/`PostCompact` never fire for it — only
  `SubagentStop` does (the run dumps its exact payload).
- [`structured-return/`](structured-return) — the fix: an output contract makes the
  same subagent return the fact as a validated structure, so it survives the overflow.

Each is self-contained: `cd` into it and `bash run.sh`.
