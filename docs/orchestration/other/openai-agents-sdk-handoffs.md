---
title: 'OpenAI Agents SDK: Handoffs'
url: https://openai.github.io/openai-agents-python/handoffs/
canonical_url: https://openai.github.io/openai-agents-python/handoffs
org: Other
theme: orchestration
subtopic: patterns
source_type: html
tags:
- Other
- orchestration
- patterns
fetch_status: ok
http_status: 200
fetched_at: '2026-06-28'
also_in: null
---

# OpenAI Agents SDK: Handoffs

> **Excerpt from the original:** Handoffs allow an agent to delegate tasks to another agent... Handoffs are represented as tools to the LLM. So if there's a handoff to an agent named Refund Agent, the tool would be called `transfer_to_refund_agent`. When a handoff occurs, it's as though the new agent takes over the conversation, and gets to see the entire previous conversation history.

**Relevance to agentic orchestration & workflows:** The canonical formalization of **agent handoffs** as a first-class, *model-directed* routing mechanism: a triage agent transfers control to a specialist, which becomes the active agent for the turn. Crucially, by default the handoff transfers the **entire** prior conversation history, a concrete context-pollution risk — controllable via `input_filter` / `handoff_filters` / the `nest_handoff_history` beta. Contrast with "agents-as-tools," where a manager retains control. *Maturity: primary vendor docs.*

[Read the original →](https://openai.github.io/openai-agents-python/handoffs/)

*Source: Other · OpenAI (openai.github.io) · archived 2026-06-28.*
