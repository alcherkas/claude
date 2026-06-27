---
title: 'code-review-graph: Multi-repo code knowledge graph over MCP'
url: https://github.com/tirth8205/code-review-graph
canonical_url: https://github.com/tirth8205/code-review-graph
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

# code-review-graph: Multi-repo code knowledge graph over MCP

> **Excerpt from the original:** A codebase is parsed into an AST with Tree-sitter, stored as a graph of nodes (functions, classes, imports) and edges (calls, inheritance, test coverage) in a local SQLite database, then queried at review time to compute the minimal set of files your AI assistant needs to read. Exposes ~30 MCP tools (e.g. `get_impact_radius_tool` for blast radius, `get_review_context_tool`, `detect_changes_tool`, `cross_repo_search_tool`) for Codex, Claude Code, Cursor, Windsurf, Zed, and Continue. MIT, local-first.

**Relevance to multi-repo context engineering:** One of the strongest open-source multi-repo options — a multi-repo registry plus a background daemon (`crg-daemon`) watches several repos at once with health checks, and `cross_repo_search_tool` queries across them, all served as a local code knowledge graph over MCP.

[Read the original →](https://github.com/tirth8205/code-review-graph)

*Source: Other · GitHub (tirth8205/code-review-graph) · archived 2026-06-27.*
