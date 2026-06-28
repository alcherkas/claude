---
title: 'Permit.io MCP Gateway: per-tool authorization for coding agents'
url: https://docs.permit.io/permit-mcp-gateway/guide/
canonical_url: https://docs.permit.io/permit-mcp-gateway/guide/
org: Other
theme: security-governance
subtopic: tools
source_type: html
tags:
- Other
- security-governance
- tools
fetch_status: ok
http_status: 200
fetched_at: '2026-06-28'
also_in: null
---
# Permit.io MCP Gateway: per-tool authorization for coding agents

> **Excerpt from the original:** A managed proxy between MCP clients (Claude Code, Cursor, Claude Desktop, VS Code) and upstream MCP servers that authenticates the user and authorizes every tool call at runtime — checking whether a given agent may call a given tool on a given MCP server, then proxying the call (allow) or returning a permission-denied error (deny). Tools are classified by trust level (Low/Medium/High = read/write/destructive) and every decision is audit-logged.

**License:** Commercial / managed (built on a Permit PDP backed by OPA or AWS Cedar)

**Relevance to securing & governing AI coding agents:** The most direct policy-as-code enforcement point that names coding agents explicitly — a Policy Decision Point in front of each MCP tool invocation, enforced out-of-model. Vendor docs describe capability/architecture, not independently-verified production efficacy.

[Read the original →](https://docs.permit.io/permit-mcp-gateway/guide/)

*Source: Other · hand-catalogued 2026-06-28 (not auto-fetched). Verify version/date at time of use.*
