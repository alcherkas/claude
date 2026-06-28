---
title: Managed Agents
url: https://www.anthropic.com/engineering/managed-agents
canonical_url: https://anthropic.com/engineering/managed-agents
org: Anthropic
theme: orchestration
subtopic: patterns
source_type: html
tags:
- Anthropic
- orchestration
- patterns
fetch_status: ok
http_status: 200
fetched_at: '2026-06-28'
also_in: null
---

# Managed Agents

> **Excerpt from the original:** The solution we arrived at was to decouple what we thought of as the "brain" (Claude and its harness) from both the "hands" (sandboxes and tools that perform actions) and the "session" (the log of session events). Each became an interface that made few assumptions about the others, and each could fail or be replaced independently.

**Relevance to agentic orchestration & workflows:** An infrastructure-level orchestration pattern for long-running agents: separating the **brain** (model + harness), the **hands** (sandboxes/tools), and the **session** (event log) lets each layer scale, fail, and recover independently — e.g. the harness can be rebooted (`wake(sessionId)`) without state loss because the session log persists separately, and sandboxes are treated as replaceable "cattle." *Maturity: primary (Anthropic Engineering); vendor-reported latency figures aside, the decoupling architecture stands on its own.*

[Read the original →](https://www.anthropic.com/engineering/managed-agents)

*Source: Anthropic · Anthropic Engineering · archived 2026-06-28.*
