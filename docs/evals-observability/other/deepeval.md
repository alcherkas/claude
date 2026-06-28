---
title: 'DeepEval: Pytest-style LLM eval framework'
url: https://github.com/confident-ai/deepeval
canonical_url: https://github.com/confident-ai/deepeval
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
# DeepEval: Pytest-style LLM eval framework

> **Excerpt from the original:** DeepEval is an open-source LLM evaluation framework "similar to Pytest but specialized for unit testing LLM apps." It ships 50+ metrics — LLM-as-judge (G-Eval, DAG), RAG (Answer Relevancy, Faithfulness, Contextual Recall/Precision/Relevancy), and agentic/trajectory metrics (Task Completion, Tool Correctness, Plan Adherence, Step Efficiency).

**License:** Apache-2.0 (open-source). The hosted Confident AI platform is commercial.

**Integration / coding-agent relevance:** CI/CD via a pytest-style CLI (`deepeval test run`) with `assert_test()` and threshold-based pass/fail gates that can block deployment. A **Confident AI MCP server** lets coding agents (Claude Code, Cursor, Windsurf) "run evals, pull datasets, and inspect traces" directly — one of the few genuinely coding-agent-targeted eval integrations found in this research pass; otherwise it is general LLM/agent tooling.

[Read the original →](https://github.com/confident-ai/deepeval)

*Source: Other · hand-catalogued 2026-06-28 from the Tools research pass (3-vote verified). Verify version/license at time of use.*
