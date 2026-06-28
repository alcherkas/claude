---
title: 'Restate: Durable execution for agentic loops'
url: https://docs.restate.dev/concepts/durable_execution
canonical_url: https://docs.restate.dev/concepts/durable_execution
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

# Restate: Durable execution for agentic loops

> **Excerpt from the original:** Restate records the results of intermediate steps in the journal and uses it to recover the execution to the point where it failed. On retry, it replays the journal, skipping completed steps and resuming from exactly where it left off — so failures result in retries that bring the agent back to the last completed step before the crash.

**Relevance to agentic orchestration & workflows:** A durable-execution runtime that is explicitly **framework-agnostic** — the journal-and-replay mechanism is orthogonal to any agent SDK, with runnable examples across the OpenAI Agents SDK, Vercel AI SDK, Google ADK, Pydantic AI, and LangChain. Useful as a reliability layer *under* an application framework rather than as a competitor to it. *Maturity: primary docs; framework-agnostic claim corroborated by the vendor's runnable example repo.*

[Read the original →](https://docs.restate.dev/concepts/durable_execution)

*Source: Other · Restate · archived 2026-06-28.*
