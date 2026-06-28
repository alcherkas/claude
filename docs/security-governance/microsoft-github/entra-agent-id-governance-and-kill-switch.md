---
title: 'Microsoft Entra Agent ID: identity kill switch, auto-expiry & least privilege'
url: https://learn.microsoft.com/en-us/entra/id-governance/agent-id-governance-overview
canonical_url: https://learn.microsoft.com/en-us/entra/id-governance/agent-id-governance-overview
org: Microsoft/GitHub
theme: security-governance
subtopic: tools
source_type: html
tags:
- Microsoft/GitHub
- security-governance
- tools
fetch_status: ok
http_status: 200
fetched_at: '2026-06-28'
also_in: null
---
# Microsoft Entra Agent ID: identity kill switch, auto-expiry & least privilege

> **Excerpt from the original:** Identity-lifecycle controls for agents. Sponsors/owners can disable an agent identity from the My Account portal (a nondestructive kill switch); access granted via access packages automatically expires on its end date if the sponsor takes no action, causing the agent to lose resource access. New agent identities receive limited inherited OAuth scopes; resource access is explicitly assigned via scoped access packages, and Conditional Access evaluates agent context and risk (including Entra ID Protection agent identity risk) before granting access — inheritable from the agent identity blueprint to block high-risk agents.

**License:** Microsoft product (Agent 365 / Entra Suite licensing; parts in Preview)

**Relevance to securing & governing AI coding agents:** Credential revocation and least-privilege blast-radius containment via agent identity — the "revoke" axis of incident response, complementing Defender's runtime "halt/contain."

[Read the original →](https://learn.microsoft.com/en-us/entra/id-governance/agent-id-governance-overview)

*Source: Microsoft/GitHub · hand-catalogued 2026-06-28 (not auto-fetched). Verify version/date at time of use.*
