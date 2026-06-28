---
title: 'Evaluating AI Agents: a practical guide with Microsoft Foundry'
url: https://techcommunity.microsoft.com/blog/azure-ai-foundry-blog/evaluating-ai-agents-a-practical-guide-with-microsoft-foundry/4500224
canonical_url: https://techcommunity.microsoft.com/blog/azure-ai-foundry-blog/evaluating-ai-agents-a-practical-guide-with-microsoft-foundry/4500224
org: Microsoft/GitHub
theme: evals-observability
subtopic: patterns
source_type: html
tags:
- Microsoft/GitHub
- evals-observability
- patterns
fetch_status: ok
http_status: 200
fetched_at: '2026-06-28'
also_in: null
---
# Evaluating AI Agents: a practical guide with Microsoft Foundry

> **Excerpt from the original:** Evaluation is framed as a two-loop model: an **inner loop (pre-ship)** run on demand during development and "As CI/CD gates: triggered on every merge to block regressions," and an **outer loop (post-ship)** "Continuously in production: scoring live traffic and scheduled runs" against golden datasets. On grading: "Prioritize outcomes over paths… Rigid step-checking creates false failures… verify end state." On gating: "A failing eval should block the merge," with built-in statistical analysis "to help distinguish real regressions from run-to-run noise."

**Type:** Vendor-primary (Azure AI Foundry blog).

**Pattern / technique:** Consolidates several well-evidenced patterns — eval-driven development, **outcome-over-trajectory** grading, the **offline/online two-loop**, and **CI merge-gates** with significance testing and thresholds that "start low… then tighten." Vendor guidance about Microsoft's own product, but the patterns are broadly applicable.

[Read the original →](https://techcommunity.microsoft.com/blog/azure-ai-foundry-blog/evaluating-ai-agents-a-practical-guide-with-microsoft-foundry/4500224)

*Source: Microsoft/GitHub · hand-catalogued 2026-06-28 from the Patterns research pass (3-vote verified). Verify product status at time of use.*
