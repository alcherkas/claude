---
title: 'Azure AI Foundry Agent Service: agents as versioned runtime assets'
url: https://learn.microsoft.com/en-us/azure/foundry/agents/concepts/runtime-components
canonical_url: https://learn.microsoft.com/en-us/azure/foundry/agents/concepts/runtime-components
org: Microsoft/GitHub
theme: orchestration
subtopic: tools
source_type: html
tags:
- Microsoft/GitHub
- orchestration
- tools
fetch_status: ok
http_status: 200
fetched_at: '2026-06-28'
also_in: null
---

# Azure AI Foundry Agent Service: agents as versioned runtime assets

> **Excerpt from the original:** Microsoft Foundry Agent Service (formerly Azure AI Foundry Agent Service; GA at Build 2025) structures its runtime around **three core components — agents, conversations, and responses** — to power stateful, multi-turn interactions. An **agent** is a *persisted orchestration definition* combining model, instructions, code, tools, parameters, and optional governance controls, **stored as a named, versioned asset** (now identified by name+version, no longer a GUID `AgentID`). A **conversation** persists history across turns (in Azure Cosmos DB); a **response** is the output. State is persisted **server-side by default** (reference `previous_response_id` for multi-turn context), or set **`store=false`** for zero-data-retention scenarios where the client carries context forward. **Pricing:** no extra charge for running Foundry-native agents — you pay for model tokens plus Tools/IQ connections; **hosted agents** (external frameworks — Agent Framework, LangGraph, Semantic Kernel, custom — packaged as containers on the managed runtime) bill by **container compute per hour** (~$0.0994/vCPU-hour, ~$0.0118/GiB-hour, scale-to-zero per session). Deployable end-to-end via the **Azure Developer CLI** (`azd up` provisions resources via Bicep, uploads the agent definition, registers the endpoint).

**Relevance to agentic orchestration & workflows:** The "where does this run" layer for the Microsoft/Azure ecosystem, and the most **IaC-native** of the three managed runtimes (agents as versioned assets + `azd`/Bicep deployment). Its distinguishing trait is treating an **agent as a first-class, server-side, versioned orchestration asset** with explicit conversation/response state management — closest to a "stateful agent application server." Pairs with the Microsoft Agent Framework (the SDK) catalogued under [Application frameworks](../index.md#application-frameworks). *Evidence: high-confidence for architecture/pricing — verified unanimous (3-0) against primary Microsoft Learn + Azure pricing pages (June 2026). **Caveat:** the long-term **Memory feature was still Public Preview** as of Q1 2026 (not GA), and the "no extra charge" wording understates Standard/BYO setups that bill customer Cosmos DB / Storage / AI Search at normal Azure rates.*

[Read the original →](https://learn.microsoft.com/en-us/azure/foundry/agents/concepts/runtime-components)

*Source: Microsoft/GitHub · Microsoft Learn — Foundry Agent Service runtime components · GA 2025 (Memory in preview) · archived 2026-06-28.*
