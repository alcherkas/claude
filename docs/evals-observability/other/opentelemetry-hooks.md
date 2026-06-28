---
title: 'opentelemetry-hooks: one OTel layer across all coding agents'
url: https://github.com/o11y-dev/opentelemetry-hooks
canonical_url: https://github.com/o11y-dev/opentelemetry-hooks
org: Other
theme: evals-observability
subtopic: tools
source_type: repo
tags:
- Other
- evals-observability
- tools
fetch_status: ok
http_status: 200
fetched_at: '2026-06-28'
also_in: null
---
# opentelemetry-hooks: one OTel layer across all coding agents

> **Excerpt from the original:** "Works with any AI coding agent — today: Antigravity, Claude Code, Codex, Cursor IDE / Cursor CLI, Gemini CLI, GitHub Copilot, and OpenCode." A single hook-based integration that emits OpenTelemetry (GenAI-semconv) telemetry for each agent.

**License:** Open-source (verify the current license on the repo).

**Integration / coding-agent relevance:** The only verified option that uniformly spans **GitHub Copilot and Cursor**, which lack first-party OTel/MCP eval-observability integrations of their own. It is **hook-based** — each agent pipes JSON event payloads via stdin/stdout on every event (no daemon or sidecar) — so it complements, rather than competes with, the agents that emit OTel natively ([Claude Code](../../security-governance/anthropic/making-claude-code-more-secure-and-autonomous-with-sandboxing.md) and Gemini CLI) and the vendor MCP servers ([Braintrust](braintrust-eval-action.md), [LangSmith](langsmith-observability.md)).

[Read the original →](https://github.com/o11y-dev/opentelemetry-hooks)

*Source: Other · hand-catalogued 2026-06-28 from a coding-agent-integration research pass (3-vote verified). Verify license/version at time of use.*
