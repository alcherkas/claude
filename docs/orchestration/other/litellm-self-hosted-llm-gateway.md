---
title: 'LiteLLM: self-hosted open-source LLM gateway (100+ providers)'
url: https://github.com/BerriAI/litellm
canonical_url: https://github.com/BerriAI/litellm
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

# LiteLLM: self-hosted open-source LLM gateway (100+ providers)

> **Excerpt from the original:** LiteLLM (BerriAI) is a **free, self-hosted, MIT-licensed** AI gateway you deploy yourself as either a **Python library/SDK** or a **standalone proxy server**, calling **100+ LLM providers in OpenAI (or native) format**. It has **no usage fees or token markup** — cost is only your own infrastructure (~$20–50/mo small-scale, ~$1,000–2,500/mo in production). The proxy provides **built-in load balancing** across multiple deployments of the same model (simple-shuffle, least-busy, usage-based, latency-based; Redis-backed rpm/tpm tracking) and **automatic failover/fallbacks** — if a call fails after `num_retries`, the request routes to a different model group. A paid **Enterprise tier** (custom pricing) adds SSO/RBAC/audit/SLA. *(The small `enterprise/` subdirectory is separately licensed.)*

**Relevance to agentic orchestration & workflows:** The self-hosted option for the **model-gateway / cost-routing layer** — the control plane an orchestration calls *through* to reach providers, giving one OpenAI-compatible interface plus retries, fallbacks, and load balancing without per-token markup. The direct lever on the topic's central cost concern (agents/multi-agent burn 4–15× the tokens of a chat). Pairs with [Helicone](../../evals-observability/other/helicone.md) (gateway + observability) catalogued under evals. *Evidence: high-confidence — all components (MIT, Python, library-or-proxy, 100+ providers, zero markup, load-balancing, fallbacks) verified unanimous 3-0 against primary repo + docs (June 2026).*

[Read the original →](https://github.com/BerriAI/litellm)

*Source: Other · github.com/BerriAI/litellm (MIT) · docs.litellm.ai · archived 2026-06-28.*
