---
title: 'OpenLLMetry (Traceloop): OTel-based LLM instrumentation'
url: https://www.traceloop.com/docs/openllmetry/
canonical_url: https://www.traceloop.com/docs/openllmetry
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
# OpenLLMetry (Traceloop): OTel-based LLM instrumentation

> **Excerpt from the original:** OpenLLMetry, by Traceloop, captures LLM/GenAI span attributes — prompts, completions, and token usage — using OpenTelemetry GenAI semantic-convention attribute names such as `gen_ai.system`, `gen_ai.request.model`, `gen_ai.prompt`, `gen_ai.completion`, and `gen_ai.usage.prompt_tokens`/`completion_tokens`/`total_tokens`, framed as an extension of the standard OTel GenAI conventions.

**License:** Apache-2.0 (open-source).

**Integration / coding-agent relevance:** A set of OpenTelemetry instrumentations (SDKs) that emit `gen_ai.*` spans to any OTel-compatible backend; vendor-neutral by construction. General LLM tooling. *Note: a claim that Traceloop "leads the OTel LLM semantic-convention working group" was refuted in verification and is not asserted here; its documented attribute names can also lag upstream renames.*

[Read the original →](https://www.traceloop.com/docs/openllmetry/)

*Source: Other · hand-catalogued 2026-06-28 from the Tools research pass (3-vote verified). Verify version/license at time of use.*
