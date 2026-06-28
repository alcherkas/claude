---
title: 'Dapr Agents: Durable agentic workflows'
url: https://docs.dapr.io/developing-ai/dapr-agents/dapr-agents-core-concepts/
canonical_url: https://docs.dapr.io/developing-ai/dapr-agents/dapr-agents-core-concepts
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

# Dapr Agents: Durable agentic workflows

> **Excerpt from the original:** Workflows enable the implementation of various agentic patterns through structured orchestration — Prompt Chaining, Routing, Parallelization, Orchestrator-Workers, Evaluator-Optimizer, Human-in-the-loop, and others. The DurableAgent class is a workflow-based agent that extends the standard Agent with Dapr Workflows for long-running, fault-tolerant, and durable execution.

**Relevance to agentic orchestration & workflows:** A CNCF project (Apache-2.0) that layers a `DurableAgent` plus the full agentic-pattern set over Dapr Workflows, with checkpointing and automatic retry/recovery. State/context is passed via persistent workflow state plus three pluggable conversation-memory backends — in-memory, vector store, and a Dapr state store (backed by many state-store providers). *Maturity: primary docs + repo.*

[Read the original →](https://docs.dapr.io/developing-ai/dapr-agents/dapr-agents-core-concepts/)

*Source: Other · Dapr Docs · archived 2026-06-28.*
