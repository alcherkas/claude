---
title: Examples
---

# Examples

Hands-on, runnable recipes for building with Claude Code — distinct from the curated source themes elsewhere in this wiki. These are things you can drop into a session and run or adapt, with the reasoning behind each design choice explained.

## Available examples

- [Workflow: plan → approve → implement](plan-approve-implement-workflow.md) — a multi-agent Claude Code `Workflow` that **plans** a change (Opus · max effort), **pauses for human approval**, then breaks the plan into a **dependency graph** and **implements + verifies** it (Sonnet · xhigh). Demonstrates per-agent **model** and **effort** selection plus a **blocking/parallel dependency scheduler** in plain JS.
