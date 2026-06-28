---
title: 'Zep / Graphiti: A Temporal Knowledge Graph Architecture for Agent Memory'
url: https://arxiv.org/abs/2501.13956
canonical_url: https://arxiv.org/abs/2501.13956
org: Other
theme: orchestration
subtopic: tools
source_type: arxiv
tags:
- Other
- orchestration
- tools
fetch_status: arxiv
http_status: 200
fetched_at: '2026-06-28'
also_in: null
---

# Zep / Graphiti: A Temporal Knowledge Graph Architecture for Agent Memory

> **Excerpt from the original:** Zep (arXiv:2501.13956) is a memory layer built on **Graphiti**, an open-source **temporally-aware knowledge-graph engine** ([github.com/getzep/graphiti](https://github.com/getzep/graphiti)). Ingested **Episodes** are decomposed into entities and edges; every fact carries a **bi-temporal validity window** — when it became true (`valid_at`) and when it was superseded (`invalid_at`). When information changes, old facts are **invalidated, not deleted**, which enables automatic fact invalidation and point-in-time ("what was true then vs. now") queries — the explicit differentiator from static-document RAG. Graphiti is the open-source engine; Zep is the managed platform built on top of it.

**Relevance to agentic orchestration & workflows:** The knowledge-graph branch of the agent-memory design space, and the strongest fit when an orchestration needs **temporal reasoning** and **provenance** (facts that change over time, audited histories) rather than flat fact recall. Contrast with Letta (agent-managed OS tiers) and Mem0 (vector-first automatic extraction). *Evidence: the Graphiti architecture and bi-temporal model are high-confidence (primary paper + repo, unanimous in the June 2026 research pass). Zep's headline **LongMemEval** result (up to ~18.5% relative accuracy gain and ~90% latency reduction vs. a full-context baseline) is **vendor-self-reported against a self-selected baseline** — directional, not independently verified. Cross-vendor LoCoMo numbers are **actively disputed** (see the Mem0↔Zep dispute in the index caveat).*

[Read the original →](https://arxiv.org/abs/2501.13956)

*Source: Other · arXiv:2501.13956 (Zep) · Graphiti repo: github.com/getzep/graphiti · archived 2026-06-28.*
