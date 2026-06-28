---
title: 'Braintrust eval-action: evals in CI with PR-comment regressions'
url: https://github.com/braintrustdata/eval-action
canonical_url: https://github.com/braintrustdata/eval-action
org: Other
theme: evals-observability
subtopic: tools
source_type: html
tags:
- Other
- evals-observability
- tools
fetch_status: ok
http_status: 200
fetched_at: '2026-06-28'
also_in: null
---
# Braintrust eval-action: evals in CI with PR-comment regressions

> **Excerpt from the original:** An official GitHub Action that runs Braintrust evals in CI/CD: it executes `braintrust eval`, collects experiment results, and posts them as a PR comment (a Score / Average / Improvements / Regressions table) linking to the Braintrust dashboard — surfacing regressions for review on every pull request.

**License:** Open-source GitHub Action; the Braintrust eval platform itself is commercial (free tier available).

**Integration / coding-agent relevance:** Drop-in GitHub Actions step; the PR-comment regression/improvement surface is the quality-gate pattern (block or flag on eval regression). The eval-action itself is general LLM/agent tooling, but Braintrust *also* ships a hosted **MCP server** (US `https://api.braintrust.dev/mcp` / EU `https://api-eu.braintrust.dev/mcp`) with documented per-client config for ~10 coding agents — Claude Code, Cursor, VS Code, Codex, Gemini CLI, Windsurf, OpenCode — exposing tools (`sql_query`, `search_docs`, `list_recent_objects`) to query experiments and production logs from inside the agent. That MCP path is a **verified coding-agent-native integration** (3-vote, 2026-06-28). See also [opentelemetry-hooks](opentelemetry-hooks.md) for a vendor-neutral OTel bridge.

[Read the original →](https://github.com/braintrustdata/eval-action)

*Source: Other · hand-catalogued 2026-06-28 from the Tools research pass (3-vote verified). Verify version/license at time of use.*
