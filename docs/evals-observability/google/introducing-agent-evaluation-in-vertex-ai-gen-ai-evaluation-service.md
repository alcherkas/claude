---
title: Introducing Agent Evaluation in Vertex AI Gen AI evaluation service
url: https://cloud.google.com/blog/products/ai-machine-learning/introducing-agent-evaluation-in-vertex-ai-gen-ai-evaluation-service
canonical_url: https://cloud.google.com/blog/products/ai-machine-learning/introducing-agent-evaluation-in-vertex-ai-gen-ai-evaluation-service
org: Google
theme: evals-observability
subtopic: patterns
source_type: html
tags:
- Google
- evals-observability
- patterns
fetch_status: ok
http_status: 200
fetched_at: '2026-06-28'
also_in: null
---
# Introducing Agent Evaluation in Vertex AI Gen AI evaluation service

> **Excerpt from the original:** Agent evaluation "metrics can be grouped in two categories: final response and trajectory evaluation." Trajectory evaluation is "crucial for understanding your agent's reasoning, identifying potential errors or inefficiencies," and ships six named metrics: `trajectory_exact_match`, `_in_order_match`, `_any_order_match`, `_precision`, `_recall`, and `_single_tool_use`.

**Type:** Vendor-primary (Google Cloud blog / Vertex AI docs).

**Pattern / technique:** Formalizes the **final-response vs. trajectory** evaluation split. The cross-vendor consensus (Anthropic, Google, Microsoft) is to grade the agent's outcome/end-state rather than rigidly step-matching the path, while still using trajectory metrics for *diagnosis*. Note: Vertex's trajectory precision/recall are **tool-trajectory** metrics — distinct from RAG retrieval precision/recall.

[Read the original →](https://cloud.google.com/blog/products/ai-machine-learning/introducing-agent-evaluation-in-vertex-ai-gen-ai-evaluation-service)

*Source: Google · hand-catalogued 2026-06-28 from the Patterns research pass (3-vote verified). Verify product status at time of use.*
