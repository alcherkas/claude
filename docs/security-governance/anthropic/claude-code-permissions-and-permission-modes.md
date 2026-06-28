---
title: 'Claude Code: Permissions & permission modes'
url: https://code.claude.com/docs/en/permissions
canonical_url: https://code.claude.com/docs/en/permissions
org: Anthropic
theme: security-governance
subtopic: patterns
source_type: html
tags:
- Anthropic
- security-governance
- patterns
fetch_status: ok
http_status: 200
fetched_at: '2026-06-28'
also_in: null
---
# Claude Code: Permissions & permission modes

> **Excerpt from the original:** Claude Code permission rules are evaluated allow/ask/deny with deny taking precedence, and permission modes (plan, default, acceptEdits) gate how much runs without prompting. Rules are enforced by the harness, not the model — so instructions in a prompt or CLAUDE.md cannot change what is allowed.

**License:** Vendor docs (Anthropic)

**Relevance to securing & governing AI coding agents:** Primary source for several safe-autonomy patterns: discrete permission modes (plan → acceptEdits → bypassPermissions), a three-tier allow/ask/deny rule system (deny → ask → allow, first match wins), and harness-enforced (out-of-model) permission checks that prompt-injected instructions cannot override.

[Read the original →](https://code.claude.com/docs/en/permissions)

*Source: Anthropic · hand-catalogued 2026-06-28 (not auto-fetched). Verify version/date at time of use.*
