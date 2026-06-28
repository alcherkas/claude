---
title: 'Claude Code auto mode: model-graded action approvals'
url: https://www.anthropic.com/engineering/claude-code-auto-mode
canonical_url: https://www.anthropic.com/engineering/claude-code-auto-mode
org: Anthropic
theme: security-governance
subtopic: patterns
source_type: html
tags:
- Anthropic
- security-governance
- patterns
fetch_status: ok
http_status: 200
fetched_at: '2026-06-28'
also_in: null
---
# Claude Code auto mode: model-graded action approvals

> **Excerpt from the original:** Auto mode evaluates each action before execution with a model-based classifier (safe-tool allowlist; in-project edits without a classifier call; classifier-gated shell, web fetches, external tools, subagent spawns, out-of-project filesystem ops). Tool outputs are scanned for hijack attempts before entering context, and blanket shell / wildcarded interpreter rules are dropped on entering auto mode.

**License:** Engineering blog (Anthropic)

**Relevance to securing & governing AI coding agents:** Delegates per-action approvals to a model-based transcript classifier — a middle ground between manual review and --dangerously-skip-permissions — plus tool-output sanitization and dropping arbitrary-code-execution rules on entering auto mode. Disclosed research preview (~17% of overeager actions slip through).

[Read the original →](https://www.anthropic.com/engineering/claude-code-auto-mode)

*Source: Anthropic · hand-catalogued 2026-06-28 (not auto-fetched). Verify version/date at time of use.*
