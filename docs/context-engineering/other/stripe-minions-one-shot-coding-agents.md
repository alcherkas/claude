---
title: 'Stripe "Minions": one-shot end-to-end coding agents at scale'
url: https://stripe.dev/blog/minions-stripes-one-shot-end-to-end-coding-agents
canonical_url: https://stripe.dev/blog/minions-stripes-one-shot-end-to-end-coding-agents
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

# Stripe "Minions": one-shot end-to-end coding agents at scale

> **Excerpt from the original:** Stripe's "Minions" are autonomous coding agents built on a fork of Block's open-source **Goose** agent, operating over a few very large repos (100M+ LOC, primarily Ruby with Sorbet typing). A central internal MCP server, **"Toolshed,"** exposes **400+ MCP tools** (each agent gets a curated subset, not the full catalog); context is gathered from docs, tickets, build status, and code intelligence via **Sourcegraph search through MCP**; orchestration runs through **"blueprints"** that wire deterministic nodes (git/lint/test) together with agentic nodes, with selective CI across 3M+ tests to cap cost. As of March 2026 the system ships **1,300+ merged PRs/week**, every PR human-reviewed.

**Relevance to multi-repo context engineering:** The strongest *public* large-estate case study, and the closest thing to a real assembled pipeline: ingest (fresh devbox checkout) → retrieval (Sourcegraph via MCP) → orchestration (blueprints) → eval/verification (capped CI loop). Note what it is *not*: a persistent cross-repo embedding index and agent memory are not the centerpiece. First-hand failure modes: custom code patterns absent from training data, context-window limits on complex changes, and diminishing returns from unbounded CI iterations. *(Independently covered by InfoQ and ByteByteGo.)*

[Read the original →](https://stripe.dev/blog/minions-stripes-one-shot-end-to-end-coding-agents)

*Source: Other · Stripe Engineering blog · archived 2026-06-28.*
