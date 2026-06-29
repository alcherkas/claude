---
title: 'Dynamic AI Agents at Scale: the agent factory pattern'
url: https://learn.microsoft.com/en-us/azure/architecture/solution-ideas/articles/ai-agents-at-scale
canonical_url: https://learn.microsoft.com/en-us/azure/architecture/solution-ideas/articles/ai-agents-at-scale
org: Microsoft/GitHub
theme: orchestration
subtopic: patterns
source_type: html
tags:
- Microsoft/GitHub
- orchestration
- patterns
fetch_status: ok
http_status: 200
fetched_at: '2026-06-29'
also_in: null
---

# Dynamic AI Agents at Scale: the agent factory pattern

> **Excerpt from the original:** Microsoft's "Dynamic AI Agents at Scale" pattern (Azure Architecture Center, May 2026) is the canonical **agent factory**: *"Given an agent name, the factory returns a ready-to-use agent instance regardless of its implementation, such as code or a YAML template. Use this approach to add new agent types without changing orchestration logic."* It is built to **scale to hundreds of agents** with **dynamic agent selection** (no predefined workflows). The cost mechanism is a **semantic cache** — Azure AI Search does vector-similarity matching against stored sample utterances and returns only a *shortlist* of candidate agents, so the LLM receives definitions for a small subset rather than the full catalog, **keeping per-request token consumption flat regardless of total agent count** (vs. sending all definitions, which grows tokens linearly with agent count).

**Relevance to agentic orchestration & workflows:** The primary-source definition of the **agent factory** as a classic *creational* design pattern (name → instance, add types without touching orchestration), plus the one genuinely novel scaling idea in the space: a **semantic-cache shortlist that decouples per-request token cost from fleet size**. This is the architecture backbone for "operating hundreds of agents," and it composes with the [fleet lifecycle & registry](../index.md#fleet-lifecycle-swarms-registries) layer and the [model gateway](../index.md#model-gateway-cost-routing) cost lever. *Evidence: high-confidence — the factory definition, hundreds-of-agents scaling, and semantic-cache token mechanism all verified unanimous 3-0 against the primary Microsoft Learn page (June 2026). Caveat: a "solution idea" / design-pattern article (documented design intent, not a measured benchmark).*

[Read the original →](https://learn.microsoft.com/en-us/azure/architecture/solution-ideas/articles/ai-agents-at-scale)

*Source: Microsoft/GitHub · Azure Architecture Center — Dynamic AI Agents at Scale · May 2026 · archived 2026-06-29.*
