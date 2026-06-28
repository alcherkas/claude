---
title: 'Inside the LLM Call: GenAI Observability with OpenTelemetry'
url: https://opentelemetry.io/blog/2026/genai-observability/
canonical_url: https://opentelemetry.io/blog/2026/genai-observability
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
# Inside the LLM Call: GenAI Observability with OpenTelemetry

> **Excerpt from the original:** OpenTelemetry models an agent run as "a top-level `invoke_agent` span with child `chat` spans for each LLM call and `execute_tool` spans for each tool invocation," so trajectory inspection falls out of standard span hierarchies. The GenAI semantic conventions standardize token/context attributes (`gen_ai.usage.input_tokens`/`output_tokens`, `gen_ai.request.model`/`max_tokens`) and latency/token metric histograms.

**License:** Open-source (OpenTelemetry / CNCF, Apache-2.0).

**Integration / coding-agent relevance:** This is the vendor-neutral backbone the other observability tools map onto. Notably, three named **coding agents already emit OTel GenAI telemetry** — VS Code Copilot (traces, metrics, events per interaction), OpenAI Codex (log events + metrics for API requests, tool calls, sessions), and Claude Code (metrics and log events; trace support in beta). **Status caveat:** the GenAI/agent semconv pages remain "Development/experimental" and the conventions have relocated to a dedicated `semantic-conventions-genai` repo, with token attribute renames in flight.

[Read the original →](https://opentelemetry.io/blog/2026/genai-observability/)

*Source: Other · hand-catalogued 2026-06-28 from the Tools research pass (3-vote verified). Verify spec status at time of use.*
