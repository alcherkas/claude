---
title: 'OpenRouter: hosted LLM marketplace gateway (one API, 300+ models)'
url: https://openrouter.ai/docs/faq
canonical_url: https://openrouter.ai/docs/faq
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

# OpenRouter: hosted LLM marketplace gateway (one API, 300+ models)

> **Excerpt from the original:** OpenRouter is a **hosted-only** proxy/marketplace (no self-host / on-prem / open-source path): the client calls OpenRouter with a single **OpenAI-compatible API key** and OpenRouter routes each request to the underlying provider, exposing **300+ (now 400+) models** across 60–70+ providers. It applies **no per-token markup** (provider pricing passes through at cost) and monetizes via a **5.5% platform fee on non-crypto credit purchases** ($0.80 minimum per transaction; **5.0% flat for crypto**, no minimum). **Bring-Your-Own-Key (BYOK)** usage is charged 5% of the equivalent OpenRouter cost, with the **first 1,000,000 BYOK requests/month free**. Its fee scales linearly with spend (~$1K/mo ≈ $1,055; ~$50K/mo ≈ $52,750).

**Relevance to agentic orchestration & workflows:** The zero-ops, hosted option for the **model-gateway layer** — fastest way to give an orchestration access to many models behind one key, at the cost of a percentage fee that scales with spend (the opposite tradeoff to self-hosting LiteLLM). A self-hosted LiteLLM at ~$200/mo infra breaks even versus OpenRouter once model spend exceeds **~$3,600/mo**. *Evidence: high-confidence — hosted-only model, OpenAI-compatibility, and the full fee structure verified unanimous 3-0 against OpenRouter's own FAQ + fee announcement (June 2026). Note: the 5.5% is technically a credit-purchase/payment fee, not a per-token markup, though the net effect on a credit-funded user is ~5.5%. An outdated "5% markup" framing was **refuted** in verification.*

[Read the original →](https://openrouter.ai/docs/faq)

*Source: Other · openrouter.ai/docs/faq + fee announcement · pricing current 2026-06 (changes often) · archived 2026-06-28.*
