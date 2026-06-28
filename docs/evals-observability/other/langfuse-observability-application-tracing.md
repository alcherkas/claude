---
title: Langfuse Observability and Application Tracing
url: https://langfuse.com/docs/observability/overview
canonical_url: https://langfuse.com/docs/observability/overview
org: Other
theme: evals-observability
subtopic: Adjacent primary sources
source_type: html
tags:
- Other
- evals-observability
- Langfuse
fetch_status: ok
http_status: 200
fetched_at: '2026-06-21'
also_in: null
---

# Langfuse Observability and Application Tracing

Official Langfuse docs for tracing LLM application prompts, responses, tool calls, costs, latency, and production behavior.

> **Excerpt from the original:** Langfuse observability captures traces across LLM applications so teams can inspect inputs, outputs, metadata, timing, cost, and nested execution steps.

**Coding-agent integration (verified 2026-06-28):** Langfuse has a native **Claude Code** integration shipped as a Marketplace **plugin built on Claude Code's hooks** (Stop + SessionEnd) that traces each session — turns, generations, tool calls, token usage — with zero code changes. Note the mechanism: it is **hooks, not MCP or OTel** (Langfuse's separate Claude *Agent SDK* integration *does* use OTel). Install via `claude plugin marketplace add langfuse/Claude-Observability-Plugin`. For agents without a first-party Langfuse plugin, see [opentelemetry-hooks](opentelemetry-hooks.md).

[Read the original ->](https://langfuse.com/docs/observability/overview)

*Source: Other · Langfuse Docs · accessed 2026-06-21.*
