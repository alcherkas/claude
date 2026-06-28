---
title: 'Infisical Agent Vault: Credential proxy for AI agents'
url: https://github.com/Infisical/agent-vault
canonical_url: https://github.com/Infisical/agent-vault
org: Other
theme: security-governance
subtopic: tools
source_type: repo
tags:
- Other
- security-governance
- tools
fetch_status: ok
http_status: 200
fetched_at: '2026-06-28'
also_in: null
---
# Infisical Agent Vault: Credential proxy for AI agents

> **Excerpt from the original:** A HTTP credential proxy and vault for AI agents like Claude Code. Agent Vault intercepts every request and attaches credentials onto it before forwarding the request to the target outbound API. Agents use dummy placeholders (e.g. __anthropic_api_key__) that are substituted with real secrets at the proxy; the agent sees the response, never the credential. MIT-licensed except the ee directory.

**License:** MIT (open-core; enterprise 'ee' dir excepted)

**Relevance to securing & governing AI coding agents:** HTTP credential proxy that intercepts an agent's outbound requests and injects real secrets before forwarding, so the agent uses placeholder values and never holds or sees the actual credentials.

[Read the original →](https://github.com/Infisical/agent-vault)

*Source: Other · hand-catalogued 2026-06-28 (not auto-fetched). Verify version/date at time of use.*
