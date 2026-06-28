---
title: 'OWASP AI Agent Security Cheat Sheet'
url: https://cheatsheetseries.owasp.org/cheatsheets/AI_Agent_Security_Cheat_Sheet.html
canonical_url: https://cheatsheetseries.owasp.org/cheatsheets/AI_Agent_Security_Cheat_Sheet.html
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
# OWASP AI Agent Security Cheat Sheet

> **Excerpt from the original:** Concrete human-in-the-loop containment guidance for AI agents. States verbatim "Allow users to interrupt and rollback agent operations" and "Require explicit approval for high-impact or irreversible actions," alongside action previews, risk-based autonomy boundaries, binding approval to the exact action being approved, and fail-closed policies.

**License / type:** OWASP community standard (CC).

**Relevance to securing & governing AI coding agents:** The **primary-source incident-response / containment guidance** that was previously missing from this theme — a standards-body checklist for interrupt, rollback, approval-before-irreversible-action, and fail-closed defaults. Pairs with the [OWASP Top 10 for Agentic Applications](owasp-top-10-for-agentic-applications-2026.md) (whose **ASI10 "Rogue Agents"** names the failure mode) and the [OWASP Agentic Threats T1–T15](owasp-agentic-ai-threats-and-mitigations.md) taxonomy.

*Key containment controls:* interrupt + rollback of agent operations; explicit approval for high-impact/irreversible actions; action previews; autonomy boundaries scaled by risk level; approval bound to the exact action; fail-closed policy. *(3-0 verified, June 2026)*

[Read the original →](https://cheatsheetseries.owasp.org/cheatsheets/AI_Agent_Security_Cheat_Sheet.html)

*Source: OWASP · hand-catalogued 2026-06-28 (deep-research verified, not auto-fetched). Living document — verify at time of use.*
