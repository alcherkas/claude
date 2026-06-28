---
title: 'CodeRAG-Bench: can retrieval augment code generation?'
url: https://arxiv.org/abs/2406.14497
canonical_url: https://arxiv.org/abs/2406.14497
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

# CodeRAG-Bench: can retrieval augment code generation?

> **Excerpt from the original:** CodeRAG-Bench (arXiv:2406.14497) evaluates retrieval-augmented code generation over a **multi-source ~25M-document retrieval corpus** (library docs, StackOverflow, GitHub files, tutorials, competition solutions), with completion targets drawn from existing single-repo benchmarks (RepoBench, CrossCodeEval, and others).

**Relevance to evaluating multi-repo context:** One of the **closest approximations to cross-*source* retrieval** evaluation — a system *could* retrieve from heterogeneous sources, so it exercises retrieval quality more honestly than completion-only benchmarks. But it is only **partial**: the ground-truth answers remain within-repo completions, so it does not require true simultaneous cross-repo retrieval. Useful mainly for its retrieval-quality methodology (retrieve-then-generate, retrieval scored separately from generation).

[Read the original →](https://arxiv.org/abs/2406.14497)

*Source: Other · arXiv:2406.14497 · archived 2026-06-28.*
