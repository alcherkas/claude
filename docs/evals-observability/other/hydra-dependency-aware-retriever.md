---
title: 'Hydra: a dependency-aware retriever for repo-level code'
url: https://arxiv.org/abs/2602.11671
canonical_url: https://arxiv.org/abs/2602.11671
org: Other
theme: evals-observability
subtopic: patterns
source_type: html
tags:
- Other
- evals-observability
- patterns
fetch_status: ok
http_status: 200
fetched_at: '2026-06-28'
also_in: null
---

# Hydra: a dependency-aware retriever for repo-level code

> **Excerpt from the original:** Hydra's **Dependency-Aware Retriever** (arXiv:2602.11671, FSE 2026), evaluated on RepoExec, reports **Recall 0.9223 / Precision 0.1086**, versus BM25 (0.5378 / 0.0809) and dense embeddings (0.5483 / 0.0824) — a graph-aware retriever roughly *doubles* dependency recall over lexical/dense baselines, at a precision cost.

**Relevance to evaluating multi-repo context:** Establishes the structural shape of the cross-repo-relevant metric — **dependency-closure recall** (did you pull *all* needed symbols/imports/call-sites?) — and shows it is inherently **high-recall, low-precision**. The lesson for a multi-repo system: a graph/dependency-aware retriever is the way to get the needed cross-boundary context, but you must then actively *manage precision* (the retrieved set will contain a lot of noise) because over-retrieval degrades the agent.

[Read the original →](https://arxiv.org/abs/2602.11671)

*Source: Other · arXiv:2602.11671 · archived 2026-06-28.*
