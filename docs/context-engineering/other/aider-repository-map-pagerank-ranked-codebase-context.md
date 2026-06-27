---
title: "Aider's repository map: PageRank-ranked codebase context"
url: https://aider.chat/docs/repomap.html
canonical_url: https://aider.chat/docs/repomap.html
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

# Aider's repository map: PageRank-ranked codebase context

> **Excerpt from the original:** Aider provides the LLM a "repo map" — a list of files plus the key symbols defined in each, including the critical lines of code for each definition. It builds this with Tree-sitter symbol extraction and a graph-ranking (PageRank-style) algorithm over a graph where each source file is a node and edges connect files with dependencies, personalized to the current chat context and held within a token budget — showing the most important symbols rather than full file contents.

**Relevance to multi-repo context engineering:** An influential in-tool (not MCP) pre-indexed context mechanism — a reference design for token-budgeted, dependency-ranked context selection that surfaces just the relevant symbols instead of dumping whole files. Scope is per-repo.

[Read the original →](https://aider.chat/docs/repomap.html)

*Source: Other · Aider docs · archived 2026-06-27.*
