---
title: OpenAI Agents SDK - Agent orchestration
url: https://openai.github.io/openai-agents-python/multi_agent/
canonical_url: https://openai.github.io/openai-agents-python/multi_agent/
org: Other
theme: orchestration
subtopic: tools
source_type: html
tags:
- Other
- orchestration
- tools
- OpenAI
fetch_status: ok
http_status: 200
fetched_at: '2026-06-21'
also_in: null
---

# OpenAI Agents SDK - Agent orchestration

> **Excerpt from the original:** There are two main ways to orchestrate agents: 1. Allowing the LLM to make decisions: this uses the intelligence of an LLM to plan, reason, and decide on what steps to take. 2. Orchestrating via code: determining the flow of agents via your code. Orchestrating via code makes tasks more deterministic and predictable, in terms of speed, cost and performance.

**Relevance to agentic orchestration & workflows:** OpenAI's official agent framework (MIT-licensed; the production successor to the experimental Swarm). The code-driven path covers sequential chaining, parallel fan-out (`asyncio.gather`), deterministic routing via structured outputs, and evaluator feedback loops; for multi-agent control it offers two named patterns — **handoffs** (delegated ownership) and **agents-as-tools** (a manager keeps control via `Agent.as_tool()`). *Maturity: primary vendor documentation.*

[Read the original →](https://openai.github.io/openai-agents-python/multi_agent/)

*Source: Other · OpenAI Agents SDK · accessed 2026-06-21.*
