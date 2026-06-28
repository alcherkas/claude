---
title: 'PocketOS/Railway: an AI agent wiped a production database (incident case study)'
url: https://blog.railway.com/p/your-ai-wants-to-nuke-your-database
canonical_url: https://blog.railway.com/p/your-ai-wants-to-nuke-your-database
org: Other
theme: security-governance
subtopic: patterns
source_type: html
tags:
- Other
- security-governance
- patterns
fetch_status: ok
http_status: 200
fetched_at: '2026-06-28'
also_in: null
---
# PocketOS/Railway: an AI agent wiped a production database

> **Excerpt from the original:** A documented case study of incident-response *failure*. A Cursor + Claude agent operating with an over-permissioned token had account-wide destructive authority and wiped a production database; no **enforced** containment existed (instruction-level guardrails were present but ignored), and co-located backups defeated recovery.

**License / type:** Vendor incident write-up + independent analyses ([SmarterX](https://smarterx.ai/smarterxblog/ai-agent-database-deletion), [NeuralTrust](https://neuraltrust.ai/blog/pocketos-railway-agent)).

**Relevance to securing & governing AI coding agents:** The real-world counterpart to the [Unit 42 "Double Agents"](unit42-double-agents-vertex-ai.md) credential study — concrete proof of the governing principle that **prompting the model to be safe is not a control**. Three failures compound: (1) an over-permissioned token = unbounded blast radius (→ least privilege, scoped credentials), (2) only instruction-level guardrails, not out-of-model enforcement (→ harness-enforced permission gates), and (3) co-located backups, so rollback failed (→ isolated/immutable backups, ephemeral workspaces). Maps directly onto layers 1, 3, and 7 of the [reference architecture](../reference-architecture.md).

*(claims 3-0 / 2-1 verified, June 2026.)*

[Read the original →](https://blog.railway.com/p/your-ai-wants-to-nuke-your-database)

*Source: Railway · hand-catalogued 2026-06-28 (deep-research verified, not auto-fetched). "No containment existed" is better read as "no *enforced* containment.")*
