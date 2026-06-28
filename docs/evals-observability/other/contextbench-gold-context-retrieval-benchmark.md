---
title: 'ContextBench: gold-context retrieval benchmark for code agents'
url: https://arxiv.org/abs/2602.05892
canonical_url: https://arxiv.org/abs/2602.05892
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

# ContextBench: gold-context retrieval benchmark for code agents

> **Excerpt from the original:** ContextBench (arXiv:2602.05892, Feb 2026) is 1,136 issue-resolution tasks from 66 repositories across 8 languages, each augmented with **human-annotated gold contexts** at file / AST-block / line granularity (plus a 500-task "Lite" subset). Retrieval is scored with **Context Recall, Context Precision, and Context F1** against a minimal-sufficient gold set. Headline findings: agent scaffolding yields only marginal retrieval gains; LLMs structurally **over-retrieve (recall ≫ precision)**; and there is a measurable gap between context agents *explore* and context they actually *utilize*.

**Relevance to evaluating multi-repo context:** The closest thing to a purpose-built **context-retrieval** benchmark. It is still single-repo-per-task, so it does not test true cross-repo retrieval — but its **methodology is directly reusable** for multi-repo work: build a human-annotated minimal-sufficient gold context per task, then score Context Recall/Precision/F1. The over-retrieval finding is the benchmark-side echo of Spotify's "dumping entire repos reduced accuracy": precision is not free, because models degrade with irrelevant context.

[Read the original →](https://arxiv.org/abs/2602.05892)

*Source: Other · arXiv:2602.05892 · archived 2026-06-28.*
