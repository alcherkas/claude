---
title: 'Datadog LLM Observability: native OpenTelemetry GenAI support'
url: https://www.datadoghq.com/blog/llm-otel-semantic-convention/
canonical_url: https://www.datadoghq.com/blog/llm-otel-semantic-convention
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
# Datadog LLM Observability: native OpenTelemetry GenAI support

> **Excerpt from the original:** "Datadog now natively supports OpenTelemetry GenAI Semantic Conventions (v1.37+)," eliminating parallel instrumentation paths — auto-mapping attributes such as `gen_ai.request.model`, `gen_ai.usage.input_tokens`, `gen_ai.provider.name`, and `gen_ai.operation.name` to Datadog's schema for latency, token, cost, model, and finish-reason views.

**License:** Commercial (SaaS).

**Integration / coding-agent relevance:** OTLP-based — instrument once with OpenTelemetry and export via a direct OTLP exporter to Datadog intake, the Datadog Agent with OTLP ingest enabled, or the OpenTelemetry Collector. Capability claims rest on vendor self-description. General LLM/agent observability; adaptable to coding-agent telemetry that follows the OTel GenAI conventions.

[Read the original →](https://www.datadoghq.com/blog/llm-otel-semantic-convention/)

*Source: Other · hand-catalogued 2026-06-28 from the Tools research pass (3-vote verified). Verify version/status at time of use.*
