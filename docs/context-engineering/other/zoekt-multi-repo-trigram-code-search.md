---
title: 'Zoekt: trigram-based multi-repo code search index'
url: https://github.com/sourcegraph/zoekt
canonical_url: https://github.com/sourcegraph/zoekt
org: Other
theme: context-engineering
subtopic: tools
source_type: repo
tags:
- Other
- context-engineering
- tools
fetch_status: ok
http_status: 200
fetched_at: '2026-06-28'
also_in: null
---

# Zoekt: trigram-based multi-repo code search index

> **Excerpt from the original:** Zoekt builds a **trigram-based shared index across whole GitHub orgs** (or arbitrary repo sets); an index server periodically pulls from code hosts, and a web/gRPC API serves queries. Apache-2.0, self-hostable (`ghcr.io/sourcegraph/zoekt`), production-proven, and Sourcegraph-maintained.

**Relevance to multi-repo context engineering:** The most mature **open-source** persistent multi-repo *index* — the retrieval substrate under Sourcegraph and Sourcebot. Important limitation: it is **lexical/regex, not semantic** (excellent for exact symbol names, error strings, and regex; no embedding layer), and it needs an **MCP wrapper** to feed an agent directly (see Sourcebot for a packaged option).

[Read the original →](https://github.com/sourcegraph/zoekt)

*Source: Other · GitHub (sourcegraph/zoekt) · archived 2026-06-28.*
