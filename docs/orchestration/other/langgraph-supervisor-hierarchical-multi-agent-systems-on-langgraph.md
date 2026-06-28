---
title: 'langgraph-supervisor: Hierarchical multi-agent systems on LangGraph'
url: https://github.com/langchain-ai/langgraph-supervisor-py
canonical_url: https://github.com/langchain-ai/langgraph-supervisor-py
org: Other
theme: orchestration
subtopic: tools
source_type: repo
tags:
- Other
- orchestration
- tools
fetch_status: ok
http_status: 200
fetched_at: '2026-06-28'
also_in: null
---

# langgraph-supervisor: Hierarchical multi-agent systems on LangGraph

> **Excerpt from the original:** A Python library for creating hierarchical multi-agent systems using LangGraph. Hierarchical systems are a type of multi-agent architecture where specialized agents are coordinated by a central supervisor agent. The supervisor controls all communication flow and task delegation, making decisions about which agent to invoke based on the current context and task requirements.

**Relevance to agentic orchestration & workflows:** A LangChain-maintained (langchain-ai, MIT) implementation of the **supervisor/hierarchical** topology on top of LangGraph. Delegation is done with tool-based handoffs (`create_handoff_tool`); how much worker context flows back to the supervisor is governed by an `output_mode` parameter (`full_history` vs `last_message`, default `last_message`), and `create_forward_message_tool` forwards a worker's message verbatim to avoid information loss. *Maturity: primary (repo README); LangChain now also recommends building the supervisor pattern directly via tools, and the library tracks a `langgraph>=1.0` dependency.*

[Read the original →](https://github.com/langchain-ai/langgraph-supervisor-py)

*Source: Other · GitHub (langchain-ai/langgraph-supervisor-py) · archived 2026-06-28.*
