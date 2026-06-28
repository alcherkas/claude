---
title: "Kythe: Google's cross-repo code graph (VName 5-tuple)"
url: https://kythe.io/docs/kythe-overview.html
canonical_url: https://kythe.io/docs/kythe-overview.html
org: Other
theme: context-engineering
subtopic: tools
source_type: html
tags:
- Other
- context-engineering
- tools
fetch_status: ok
http_status: 200
fetched_at: '2026-06-28'
also_in: null
---

# Kythe: Google's cross-repo code graph (VName 5-tuple)

> **Excerpt from the original:** Kythe is a pluggable, language-agnostic ecosystem for building a graph of source code. Every node gets a **VName** 5-tuple — `(corpus, root, path, language, signature)` — and multiple repositories can be assigned **distinct corpus names** and indexed into one graph store, which is what makes cross-repo cross-references possible in principle. Apache-2.0.

**Relevance to multi-repo context engineering:** A genuine cross-repo code-graph model (the lineage behind Google's internal Code Search). Two hard caveats: (a) cross-repo is **practically tied to a unified build** (compiler-extractor instrumentation, typically Bazel/monorepo) — a high infra burden; and (b) **maintenance risk** — Google's broad 2024 "Core" layoffs are well-sourced, and while a Kythe-team-specific impact is not firmly documented, treat the project as a soft hedge rather than a safe production bet.

[Read the original →](https://kythe.io/docs/kythe-overview.html)

*Source: Other · kythe.io · archived 2026-06-28.*
