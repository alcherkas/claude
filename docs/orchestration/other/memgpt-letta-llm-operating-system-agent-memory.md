---
title: 'MemGPT / Letta: LLMs as Operating Systems (tiered agent memory)'
url: https://arxiv.org/abs/2310.08560
canonical_url: https://arxiv.org/abs/2310.08560
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

# MemGPT / Letta: LLMs as Operating Systems (tiered agent memory)

> **Excerpt from the original:** MemGPT (Packer et al., 2023) proposes treating the LLM context window like an operating system's main memory, with the model paging information in and out of a larger external store. Memory is split into **three tiers** — **core memory** (always in-context, the agent's "RAM"), **recall memory** (searchable conversation history), and **archival memory** (an external vector store, the agent's "disk"). The agent **self-manages** these tiers through explicit tool/function calls (e.g., editing core memory, searching archival memory), so it can decide what to remember, retrieve, and evict rather than relying on a fixed context window. The project is now developed as the open-source **Letta** framework/runtime.

**Relevance to agentic orchestration & workflows:** This is the canonical *agent-managed* long-term-memory architecture — the "where does persistent state live across context windows" layer that an end-to-end orchestration stack needs. Letta sits one layer beneath the orchestration frameworks (it is a stateful agent server you call), and is the contrast case to the *automatic-extraction* model (Mem0) and the *temporal-knowledge-graph* model (Zep/Graphiti). The OS/RAM-vs-disk framing also connects directly to the context-engineering topic's compaction and note-taking levers. *Evidence: architecture description is high-confidence (primary paper + Letta docs, unanimous across the June 2026 research pass); **no independent accuracy benchmark for Letta surfaced** — treat any head-to-head accuracy ranking against Mem0/Zep as unestablished.*

[Read the original →](https://arxiv.org/abs/2310.08560)

*Source: Other · arXiv:2310.08560 (MemGPT) · 2023, developed as Letta · archived 2026-06-28.*
