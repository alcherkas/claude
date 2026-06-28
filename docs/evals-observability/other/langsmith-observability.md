---
title: 'LangSmith: framework-agnostic agent tracing & observability'
url: https://www.langchain.com/langsmith/observability
canonical_url: https://www.langchain.com/langsmith/observability
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
# LangSmith: framework-agnostic agent tracing & observability

> **Excerpt from the original:** LangSmith provides framework-agnostic agent tracing — step-by-step trace/span capture and tool/agent trajectory monitoring — and tracks token usage, latency percentiles (P50/P99), error rates, cost breakdowns, and feedback scores.

**License:** Commercial (LangChain; free developer tier).

**Integration / coding-agent relevance:** SDKs (Python, TypeScript, Go, Java) plus OpenTelemetry; works with the OpenAI SDK, Anthropic SDK, Vercel AI SDK, LlamaIndex, or custom implementations — **not just LangChain**. Capability claims rest on vendor self-description. LangSmith *also* ships an official **MCP server** (`langchain-ai/langsmith-mcp-server`, MIT) exposing eval/observability tools — `fetch_runs` (FQL filtering), `list_experiments` (latency/cost/feedback metrics), `get_billing_usage` — to coding agents such as Cursor and Claude Desktop; a **verified coding-agent-native integration** (3-vote, 2026-06-28), with v0.15+ users pointed to its "LangSmith Remote MCP" successor. (See also the topic's [LangSmith evaluation concepts](langsmith-evaluation-concepts.md) for the offline/online eval side.)

[Read the original →](https://www.langchain.com/langsmith/observability)

*Source: Other · hand-catalogued 2026-06-28 from the Tools research pass (3-vote verified). Verify version/status at time of use.*
