---
title: 'Loop engineering: writing loops, not prompts'
url: https://addyosmani.com/blog/loop-engineering/
canonical_url: https://addyosmani.com/blog/loop-engineering/
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

# Loop engineering: writing loops, not prompts

> **Excerpt from the original:** "Loop engineering" reframes coding-agent work from **writing prompts to building loops** that hold context externally, decide what to prompt, dispatch the agent, and check completion. Anthropic's Boris Cherny (creator of Claude Code) said in June 2026: *"I don't prompt Claude anymore… My job is to write loops."* Addy Osmani's essay names **six building blocks** — Automations, Worktrees, Skills, Connectors, Sub-agents, and Memory — that map onto Claude Code primitives (`/loop`, `--worktree`, `SKILL.md`, MCP connectors, `.claude/agents`, markdown state files). A related refinement is the **goal-conditioned maker-checker loop**: Claude Code's `/goal` uses a separate, faster model (default Haiku) to verify the completion condition after each turn — distinct from the model that wrote the code, because a model grading its own work over-reports success.

**Relevance to agentic orchestration & workflows:** The generalization of the [Ralph Loop](ralph-loop-fresh-context-agentic-coding.md) into a paradigm — the orchestration unit of work becomes *the loop* (external context + dispatch + completion-check), not the prompt. The maker-checker `/goal` pattern is a concrete reliability lever (separate-verifier-model) that connects to the evaluator-optimizer pattern and the topic's broader "grade the outcome, not the path" theme. *Evidence: **medium** — Osmani's essay and the Claude Code `/goal` docs are primary, but the "loop engineering" framing and the Boris Cherny quote are very recent (June 2026) and partly anchored on secondary news; treat the paradigm as emerging. The official `/loop` plugin's single-context behavior (vs. the fresh-context bash loop) is the subject of an open community dispute.*

[Read the original →](https://addyosmani.com/blog/loop-engineering/)

*Source: Other · addyosmani.com/blog/loop-engineering (Addy Osmani) · Jun 2026 · archived 2026-06-29.*
