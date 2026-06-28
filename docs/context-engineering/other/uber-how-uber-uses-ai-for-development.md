---
title: 'How Uber uses AI for development (Minion, Shepherd, MCP Gateway)'
url: https://newsletter.pragmaticengineer.com/p/how-uber-uses-ai-for-development
canonical_url: https://newsletter.pragmaticengineer.com/p/how-uber-uses-ai-for-development
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

# How Uber uses AI for development (Minion, Shepherd, MCP Gateway)

> **Excerpt from the original:** Uber runs AI coding agents over a **monorepo**, with a tool suite that includes **Minion** (a background agent with monorepo access), **Shepherd** (large-scale migrations), **Code Inbox** (PR routing for reviewer overload), **uReview** (code review), and **Autocover** (5,000+ unit tests/month) — all fronted by an **MCP Gateway** that centralizes authorization, telemetry, and logging for every agent tool call. Company-reported adoption: 92% of devs use agents monthly, 65–72% of IDE code is AI-generated, and 11% of all PRs are opened by agents.

**Relevance to multi-repo context engineering:** A monorepo case (so cross-repo *indexing* is moot), but valuable for one piece of connective tissue most case studies omit: the **MCP Gateway** as a single governance/wiring layer (authz + telemetry + logging) in front of all agent tools — directly relevant to the access-control layer of a solution-level system. Caveat: these are **self-reported** productivity figures with no independent audit; the primary source (Pragmatic Engineer) is paywalled, with metrics corroborated by secondary coverage (ShiftMag).

[Read the original →](https://newsletter.pragmaticengineer.com/p/how-uber-uses-ai-for-development)

*Source: Other · The Pragmatic Engineer · archived 2026-06-28.*
