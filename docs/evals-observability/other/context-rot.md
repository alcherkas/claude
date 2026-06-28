---
title: 'Context Rot: how increasing input tokens impacts LLM performance'
url: https://www.trychroma.com/research/context-rot
canonical_url: https://www.trychroma.com/research/context-rot
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
# Context Rot: how increasing input tokens impacts LLM performance

> **Excerpt from the original:** "Models do not use their context uniformly; instead, their performance grows increasingly unreliable as input length grows" — often degrading well before the nominal window limit. "As needle-question similarity decreases, model performance degrades more significantly with increasing input length." On LongMemEval, "across all models, we see significantly higher performance on focused prompts compared to full prompts" (focused ~300 tokens vs. full ~113k).

**Type:** Vendor-primary research report (Chroma, 2025); controlled study across 18 frontier models.

**Pattern / technique — evaluating context quality itself:** Direct evidence that **curating/trimming context measurably improves accuracy** — the empirical backbone of context engineering. **Caveats:** Chroma sells a retrieval product (vendor incentive), and the "focused" condition is an oracle (assumes perfect relevance knowledge); but the core finding is corroborated by peer-reviewed [lost-in-the-middle](../../context-engineering/other/lost-in-the-middle-how-language-models-use-long-contexts.md) work.

[Read the original →](https://www.trychroma.com/research/context-rot)

*Source: Other · hand-catalogued 2026-06-28 from the Patterns research pass (3-vote verified). Model-dependent; re-check on current models.*
