---
title: 'ConvoMem: Why Your First 150 Conversations Don''t Need RAG'
url: https://arxiv.org/abs/2511.10523
canonical_url: https://arxiv.org/abs/2511.10523
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

# ConvoMem: Why Your First 150 Conversations Don't Need RAG

> **Excerpt from the original:** ConvoMem (Salesforce AI Research, arXiv:2511.10523, Nov 2025) is a **vendor-independent** conversational-memory benchmark. Its headline finding: for **short histories (under ~150 interactions)**, a **simple full-context** approach beats RAG-style memory systems — full-context reaches **70–82%** on the hardest multi-message evidence cases, while a sophisticated RAG-based memory layer (Mem0) reaches only **30–45%** at that scale. On straightforward user-facts recall, Mem0 starts at **77.5%** vs **94.7%** for long context — a stable **15–25-point gap** across scales. Implication: dedicated memory frameworks pay off **mainly at scales where full context is infeasible**, not on small histories.

**Relevance to agentic orchestration & workflows:** The crucial **"do you even need a memory system yet"** counterweight to the vendor benchmark wars — and the single most decision-useful source for an end-to-end build, because it comes from a party not selling a memory product. It reframes agent memory as a **scale-triggered** layer: default to full context, and adopt Mem0/Zep/Letta only when conversation/state volume exceeds what the context window can hold. *Evidence: vendor-independent (Salesforce, not a memory vendor) and unanimous 3–0 in verification, but a **single non-peer-reviewed preprint introducing its own benchmark** (not yet independently replicated) and somewhat advocacy-toned; Zep and Letta were not evaluated in this particular result. Treat the crossover threshold as directional.*

[Read the original →](https://arxiv.org/abs/2511.10523)

*Source: Other · arXiv:2511.10523 (ConvoMem, Salesforce AI Research) · Nov 2025 · archived 2026-06-28.*
