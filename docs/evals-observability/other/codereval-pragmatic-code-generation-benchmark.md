---
title: 'CoderEval: pragmatic code generation with dependency levels'
url: https://arxiv.org/abs/2302.00288
canonical_url: https://arxiv.org/abs/2302.00288
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

# CoderEval: pragmatic code generation with dependency levels

> **Excerpt from the original:** CoderEval (arXiv:2302.00288) evaluates code generation on functions stratified by **six dependency levels**, including a `plib_runnable` level whose functions depend on **third-party (external) packages** — the only category where the required context literally lives in a distinct external codebase rather than the task's own repo.

**Relevance to evaluating multi-repo context:** The closest a mainstream benchmark gets to **cross-repo dependency** — its `plib_runnable` level is, structurally, "context lives in another repo." The important limitation: the eval does **not** require *retrieving* source from those external repos; it assumes the model already learned the third-party API from training. So it bounds the gap rather than filling it: it shows the dependency-on-external-code dimension exists, but no public benchmark yet scores an agent on retrieving that external context on demand.

[Read the original →](https://arxiv.org/abs/2302.00288)

*Source: Other · arXiv:2302.00288 · archived 2026-06-28.*
