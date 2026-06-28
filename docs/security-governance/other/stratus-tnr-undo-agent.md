---
title: 'STRATUS & Transactional-No-Regression: undo + pre-execution rejection of destructive actions'
url: https://research.ibm.com/blog/undo-agent-for-cloud
canonical_url: https://research.ibm.com/blog/undo-agent-for-cloud
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
# STRATUS & Transactional-No-Regression (TNR)

> **Excerpt from the original:** STRATUS (IBM Research + UIUC, NeurIPS 2025) reverts the system to the last checkpoint after an unsuccessful remediation move, and **rejects destructive, non-recoverable actions before execution** ("deleting a database… will be rejected before it can even run"). Its Transactional-No-Regression (TNR) safety property ensures mitigation actions are always undoable and undoes any action that worsens system health — enforced via write locks, command simulation, undo operators, and transaction limits.

**License / type:** Peer-reviewed research (NeurIPS 2025, arXiv:2506.02009).

**Relevance to securing & governing AI coding agents:** The most direct research answer to **seam 3** ("no agent-native rollback of destructive bash / already-merged changes"). Where checkpointing only undoes model file edits, TNR adds two things the production tools lack: **pre-execution rejection** of non-recoverable actions (a `DROP DATABASE` is screened *before* it runs) and a formal undoability guarantee via command simulation + undo operators. Still research-stage and aimed at cloud-ops remediation rather than coding agents — describes design and a formal property, not production coding-agent deployment.

*(3-0 verified, June 2026.)*

[Read the original →](https://research.ibm.com/blog/undo-agent-for-cloud)

*Source: IBM Research · hand-catalogued 2026-06-28 (deep-research verified, not auto-fetched). Research-stage — verify applicability before relying on it.*
