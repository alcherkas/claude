---
title: 'Portkey: managed LLM gateway with semantic caching'
url: https://github.com/portkey-ai/gateway
canonical_url: https://github.com/portkey-ai/gateway
org: Other
theme: orchestration
subtopic: tools
source_type: html
tags:
- Other
- orchestration
- tools
fetch_status: ok
http_status: 200
fetched_at: '2026-06-28'
also_in: null
---

# Portkey: managed LLM gateway with semantic caching

> **Excerpt from the original:** Portkey is a managed AI gateway advertising the **broadest reach** (1,600+ LLMs; older docs say 250+). Pricing: a **free "Developer" tier** (~10K logs/month), a **$49/month "Production" tier** (100K recorded logs/month, 30-day retention, $9 per additional 100K requests), and custom **Enterprise** — with **no per-token markup**. It adds enterprise features including **semantic caching** — returning cached responses for *semantically similar* prompts (embedding cosine similarity against a configurable threshold), not only exact text matches. The Portkey **gateway was open-sourced** (MIT, ~March 2026), enabling a $0 self-hosted path alongside the hosted tiers; Portkey was **acquired by Palo Alto Networks**.

**Relevance to agentic orchestration & workflows:** The managed-with-enterprise-features option for the **model-gateway layer** — its differentiator is **semantic caching** (a direct latency/cost lever for repetitive agent prompts) plus governance features, billed by log volume rather than a percentage of spend. *Evidence: high-confidence for tiers, semantic caching, and open-sourcing — verified unanimous 3-0 against Portkey's own pricing/docs (June 2026). Caveats: the Palo Alto acquisition may change pricing/feature gating; the "1,600+ LLMs" count is a vendor lower bound in non-comparable units. A claim that Portkey is the **only** gateway with semantic caching was **refuted** (LiteLLM also offers it).*

[Read the original →](https://github.com/portkey-ai/gateway)

*Source: Other · github.com/portkey-ai/gateway (MIT) + portkey.ai/pricing · archived 2026-06-28.*
