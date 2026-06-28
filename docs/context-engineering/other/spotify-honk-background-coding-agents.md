---
title: 'Spotify "Honk": background coding agents across thousands of repos'
url: https://engineering.atspotify.com/2025/11/spotifys-background-coding-agent-part-1
canonical_url: https://engineering.atspotify.com/2025/11/spotifys-background-coding-agent-part-1
org: Other
theme: context-engineering
subtopic: ''
source_type: html
tags:
- Other
- context-engineering
fetch_status: ok
http_status: 200
fetched_at: '2026-06-28'
also_in: null
---

# Spotify "Honk": background coding agents across thousands of repos

> **Excerpt from the original:** "Honk" is Spotify's background coding agent running over an explicitly **poly-repo** estate ("thousands of repos"), built atop **Fleet Management** (their framework since 2022 for changes across hundreds-to-thousands of repos) with **Backstage** as catalog. It uses **Claude Code / the Claude Agent SDK** as its top-performing agent across ~50 migrations, with a custom verification layer (deterministic verifiers — Maven/formatters/linters/tests — plus an **LLM-as-judge**). Agent tool access is deliberately restricted to a "verify" tool, a git allowlist, and a bash allowlist, with code-search/doc tools *excluded*; Slack is the human-agent interface. 1,500+ merged AI PRs (late 2025), 650+/month.

**Relevance to multi-repo context engineering:** The clearest **poly-repo fleet** case study — and the most candid on failure modes. Two findings matter for context engineering specifically: Spotify favored **large static prompts over dynamic MCP retrieval** ("predictability over flexibility"), and reported that **"dumping entire repositories into the context window reduced accuracy"** (more context ≠ better context). Other first-hand failures: context-window exhaustion ("forgetting the original task"), scope creep, and functional incorrectness (PRs that pass CI but break production) — rated their most serious error. *(See also the Anthropic Spotify customer story.)*

[Read the original →](https://engineering.atspotify.com/2025/11/spotifys-background-coding-agent-part-1)

*Source: Other · Spotify Engineering blog · archived 2026-06-28.*
