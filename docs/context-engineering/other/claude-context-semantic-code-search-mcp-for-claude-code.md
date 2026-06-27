---
title: 'Claude Context: Semantic code-search MCP for Claude Code'
url: https://github.com/zilliztech/claude-context
canonical_url: https://github.com/zilliztech/claude-context
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
fetched_at: '2026-06-27'
also_in: null
---

# Claude Context: Semantic code-search MCP for Claude Code

> **Excerpt from the original:** A code-search MCP for Claude Code using advanced hybrid search (BM25 + dense vector) over AST-chunked code stored in a vector database. Exposes MCP tools `index_codebase`, `search_code`, `clear_index`, and `get_indexing_status`, retrieving relevant snippets on demand rather than packing the repo (~40% token reduction vs. monolithic packing). Requires external infrastructure: a vector DB (Zilliz Cloud or self-hosted Milvus) plus an embedding provider (OpenAI/VoyageAI/Ollama/Gemini).

**Relevance to multi-repo context engineering:** A retrieval/indexing approach that serves just-in-time semantic context to agents over MCP — token-efficient and queryable, at the cost of standing up a vector DB and embedding API. Indexing is per-codebase (multi-repo is not a highlighted feature).

[Read the original →](https://github.com/zilliztech/claude-context)

*Source: Other · GitHub (zilliztech/claude-context) · archived 2026-06-27.*
