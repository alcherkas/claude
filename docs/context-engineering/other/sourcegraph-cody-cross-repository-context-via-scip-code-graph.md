---
title: 'Sourcegraph / Cody: Cross-repository context via SCIP code graph'
url: https://sourcegraph.com/docs/cody/core-concepts/context
canonical_url: https://sourcegraph.com/docs/cody/core-concepts/context
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
fetched_at: '2026-06-27'
also_in: null
---

# Sourcegraph / Cody: Cross-repository context via SCIP code graph

> **Excerpt from the original:** Cody retrieves codebase context via three mechanisms — keyword search, the Sourcegraph Search API, and a Code Graph that examines how components are interconnected and used — and supports linking single or multiple repositories. Cross-repository navigation (jump from an imported symbol to its definition in another repo, tracking relationships across repo boundaries) is powered by SCIP (Source Code Intelligence Protocol), Sourcegraph's language-agnostic pre-indexing format that replaced LSIF and scales to 300,000+ repositories.

**Relevance to multi-repo context engineering:** The strongest cross-repo / solution-level context platform — true SCIP-indexed navigation across repository boundaries. Caveats: cross-repo navigation requires SCIP indexes on both repos at matching commits, and full remote multi-repo access is largely Enterprise-tier / RBAC-gated.

[Read the original →](https://sourcegraph.com/docs/cody/core-concepts/context)

*Source: Other · Sourcegraph docs · archived 2026-06-27.*
