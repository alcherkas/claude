---
title: 'Justice or Prejudice? Quantifying Biases in LLM-as-a-Judge (CALM)'
url: https://arxiv.org/abs/2410.02736
canonical_url: https://arxiv.org/abs/2410.02736
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
# Justice or Prejudice? Quantifying Biases in LLM-as-a-Judge (CALM)

> **Excerpt from the original:** "We identify twelve distinct types of biases" in LLM judges — position, verbosity, compassion-fade, bandwagon, distraction, fallacy-oversight, authority, sentiment, diversity, chain-of-thought, self-enhancement, and refinement-aware. "All judge models are significantly impacted by position bias." Self-enhancement is real but model-dependent (e.g., ChatGPT 8.91% vs. GPT-4-Turbo 1.16% error, persisting even when answer sources are anonymized).

**Type:** Peer-reviewed academic (arXiv 2410.02736; ICLR 2025).

**Pattern / technique — LLM-as-a-judge limits:** The authoritative quantification of judge bias. Implies standard mitigations: calibrate judges against human graders, **swap order and average** in pairwise comparisons, use isolated per-dimension rubrics, and treat extraordinary judge verdicts skeptically. Pairs with [ThoughtWorks' "LLM as a judge"](../thoughtworks/llm-as-a-judge.md) (moved Trial→Assess) and [A Survey on LLM-as-a-Judge](a-survey-on-llm-as-a-judge.md).

[Read the original →](https://arxiv.org/abs/2410.02736)

*Source: Other · hand-catalogued 2026-06-28 from the Patterns research pass (3-vote verified). Verify version at time of use.*
