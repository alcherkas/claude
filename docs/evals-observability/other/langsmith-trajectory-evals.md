---
title: 'LangSmith: trajectory evaluations'
url: https://docs.langchain.com/langsmith/trajectory-evals
canonical_url: https://docs.langchain.com/langsmith/trajectory-evals
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
# LangSmith: trajectory evaluations

> **Excerpt from the original:** Trajectory evaluations score the full sequence of steps an agent takes — its tool calls and intermediate reasoning — rather than only the final answer, letting you check whether the agent followed an acceptable path and used the right tools.

**Type:** Vendor docs (LangChain / LangSmith).

**Pattern / technique:** A concrete tooling instance of **trajectory vs. final-answer evaluation**. Complements the outcome-over-path consensus: grade the end-state for pass/fail, but use trajectory evals to *diagnose* why a run failed. See also Google [Vertex agent evaluation](../google/introducing-agent-evaluation-in-vertex-ai-gen-ai-evaluation-service.md) for the metric taxonomy.

[Read the original →](https://docs.langchain.com/langsmith/trajectory-evals)

*Source: Other · hand-catalogued 2026-06-28 from the Patterns research pass (3-vote verified). Verify version at time of use.*
