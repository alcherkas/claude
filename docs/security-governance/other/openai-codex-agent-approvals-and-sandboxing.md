---
title: 'OpenAI Codex: Agent approvals & sandboxing'
url: https://developers.openai.com/codex/agent-approvals-security
canonical_url: https://developers.openai.com/codex/agent-approvals-security
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
# OpenAI Codex: Agent approvals & sandboxing

> **Excerpt from the original:** Codex gates risky actions behind approval modes and runs commands in an OS-level sandbox that restricts filesystem and network access, so a prompt injection that bypasses the model's decision-making still cannot reach out-of-boundary resources. Approvals and sandboxing are presented as complementary layers.

**License:** Vendor docs (OpenAI)

**Relevance to securing & governing AI coding agents:** Codex's approval modes and OS-level sandboxing: read-only / auto / full-access tiers gate state-changing actions, while sandboxing restricts filesystem and network reach as a complementary defense layer.

[Read the original →](https://developers.openai.com/codex/agent-approvals-security)

*Source: Other · hand-catalogued 2026-06-28 (not auto-fetched). Verify version/date at time of use.*
