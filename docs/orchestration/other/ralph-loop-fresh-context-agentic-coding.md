---
title: 'The Ralph Loop: fresh-context agentic coding loop'
url: https://ghuntley.com/ralph/
canonical_url: https://ghuntley.com/ralph/
org: Other
theme: orchestration
subtopic: patterns
source_type: html
tags:
- Other
- orchestration
- patterns
fetch_status: ok
http_status: 200
fetched_at: '2026-06-29'
also_in: null
---

# The Ralph Loop: fresh-context agentic coding loop

> **Excerpt from the original:** The **Ralph Loop** (Geoffrey Huntley, **July 2025**; named after The Simpsons' Ralph Wiggum as a metaphor for *"persistent, unsophisticated effectiveness"*) is an agentic coding-loop pattern in which a **bash loop repeatedly restarts a fresh AI coding agent** — each iteration getting a clean, identically-allocated context window — to grind through a task list. In its purest form it is a one-liner: `while :; do cat PROMPT.md | claude -p ; done`. It is **monolithic** (single process, single repo, **one task per loop**) and **agent-agnostic** (Claude, Codex, Gemini, local Ollama/Qwen). The key mechanism: persistence between iterations is carried **only through external artifacts — git history, a `progress.txt` learnings file, and a `prd.json` story-status file — not in-context state**, deliberately discarding conversation history to avoid context-window degradation. Structured implementations (e.g. `snarktank/ralph`) drive one user story per iteration: select highest-priority incomplete story → implement → run quality checks → commit → mark done → append learnings → repeat until complete or a `MAX_ITERATIONS` cap. As a demo, Ralph ran ~3 months continuously to build **CURSED**, an esoteric language absent from the LLM's training data.

**Relevance to agentic orchestration & workflows:** The canonical *named* version of the long-running single-agent harness pattern — and the externally-originated mirror of Anthropic's [Effective harnesses for long-running agents](../../context-engineering/anthropic/effective-harnesses-for-long-running-agents.md) (which independently converged on the same design: initializer + coding agent, one feature per iteration, durable `claude-progress.txt` + git). It operationalizes "filesystem-and-git as memory" and "fresh context beats compaction" for unattended work. *Evidence: high-confidence — origin, canonical form, fresh-context mechanism, and PRD-driven sequence verified unanimous 3-0 across the creator's blog + multiple GitHub repos (June 2026). **Two misconceptions refuted (don't repeat):** Ralph is **not** an error-feedback loop that feeds its own output back to converge (it discards context each iteration), and **not** a fixed five-phase plan/implement/test/verify/PR pipeline (that's one tutorial's framing). Limits: struggles with tightly-coupled multi-file changes and deep codebases.*

[Read the original →](https://ghuntley.com/ralph/)

*Source: Other · ghuntley.com/ralph (Geoffrey Huntley) · Jul 2025 · archived 2026-06-29.*
