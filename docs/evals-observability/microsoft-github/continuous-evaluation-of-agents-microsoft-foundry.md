---
title: Continuous evaluation of agents (Microsoft Foundry)
url: https://learn.microsoft.com/en-us/azure/ai-foundry/how-to/continuous-evaluation-agents
canonical_url: https://learn.microsoft.com/en-us/azure/ai-foundry/how-to/continuous-evaluation-agents
org: Microsoft/GitHub
theme: evals-observability
subtopic: patterns
source_type: html
tags:
- Microsoft/GitHub
- evals-observability
- patterns
fetch_status: ok
http_status: 200
fetched_at: '2026-06-28'
also_in: null
---
# Continuous evaluation of agents (Microsoft Foundry)

> **Excerpt from the original:** Continuous evaluation "continuously evaluates agent interactions at a set sampling rate to provide insights into quality, safety, and performance," with metrics surfaced on the Foundry Observability dashboard. Sampling is configured via `AgentEvaluationSamplingConfiguration` (`samplingPercent`, `maxRequestRate`). Crucially, "Evaluations are also connected to traces to enable detailed debugging and root cause analysis."

**Type:** Vendor-primary (Microsoft Learn how-to; some capabilities in public preview).

**Pattern / technique:** Operationalizes the **online/production evaluation** half of the two-loop model and the **eval-linked-to-traces** observability pattern (a low Task-Adherence score links straight to the trace showing where the agent went wrong). The eval-linked-to-traces pattern is cross-vendor (also Langfuse, Arize, LangSmith).

[Read the original →](https://learn.microsoft.com/en-us/azure/ai-foundry/how-to/continuous-evaluation-agents)

*Source: Microsoft/GitHub · hand-catalogued 2026-06-28 from the Patterns research pass (3-vote verified). Verify preview/GA status at time of use.*
