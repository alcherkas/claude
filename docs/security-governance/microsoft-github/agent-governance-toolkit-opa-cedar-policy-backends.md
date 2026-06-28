---
title: 'Microsoft Agent Governance Toolkit: pluggable OPA/Rego & Cedar policy backends'
url: https://microsoft.github.io/agent-governance-toolkit/tutorials/08-opa-rego-cedar-policies/
canonical_url: https://microsoft.github.io/agent-governance-toolkit/tutorials/08-opa-rego-cedar-policies/
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
# Microsoft Agent Governance Toolkit: pluggable OPA/Rego & Cedar policy backends

> **Excerpt from the original:** Runtime policy enforcement for AI agents via pluggable OPA/Rego and AWS Cedar backends behind a common ExternalPolicyBackend protocol returning a normalized BackendDecision. The OPA backend offers three evaluation modes (remote OPA server REST API, local `opa eval` CLI, built-in fallback parser) for zero-mandatory-dependency evaluation; the Cedar backend uses permit/forbid and auto-converts snake_case tool names to PascalCase actions (file_read -> Action::"FileRead").

**License:** MIT (announced opensource.microsoft.com, April 2026)

**Relevance to securing & governing AI coding agents:** An open-source (MIT) way to wire mature policy engines (OPA + Cedar) into the agent action path behind one interface — the clearest first-party bridge between governance frameworks and runtime gates.

[Read the original →](https://microsoft.github.io/agent-governance-toolkit/tutorials/08-opa-rego-cedar-policies/)

*Source: Microsoft/GitHub · hand-catalogued 2026-06-28 (not auto-fetched). Verify version/date at time of use.*
