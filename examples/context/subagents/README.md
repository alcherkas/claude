# Subagents & context overflow

Two paired, runnable demos using a Haiku subagent. Both plant a unique `MARK` code in
each of N large files; the contrast is whether those codes survive.

- [`overflow/`](overflow) — the problem: one subagent reads them all, overflows, and
  silently compacts — the **early codes come back `LOST`**. Also shows that
  `PreCompact`/`PostCompact` never fire for a subagent; only `SubagentStop` does (the
  run dumps its exact payload).
- [`structured-return/`](structured-return) — the fix: fan out one bounded subagent per
  file, each returning a structured `{"NNN":"code"}`; the parent merges them, so
  **every code is kept**.

Each is self-contained: `cd` into it and `bash run.sh`.
