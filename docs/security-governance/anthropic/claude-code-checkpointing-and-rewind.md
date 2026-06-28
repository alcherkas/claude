---
title: 'Claude Code checkpointing & /rewind: session-level rollback'
url: https://code.claude.com/docs/en/checkpointing
canonical_url: https://code.claude.com/docs/en/checkpointing
org: Anthropic
theme: security-governance
subtopic: tools
source_type: html
tags:
- Anthropic
- security-governance
- tools
fetch_status: ok
http_status: 200
fetched_at: '2026-06-28'
also_in: null
---
# Claude Code checkpointing & /rewind: session-level rollback

> **Excerpt from the original:** Automatic checkpointing captures the state of your code before each edit; every user prompt creates a new checkpoint, and checkpoints persist across sessions. An operator rolls back mid-session via /rewind (or pressing Esc twice on an empty prompt), choosing "Restore code" (revert file changes, keep the conversation) or "Restore code and conversation." Limitation: checkpointing tracks only Claude's own file-editing tools — it cannot undo bash-command changes (rm/mv/cp), external edits, or concurrent-session edits, and is explicitly "not a replacement for version control."

**License:** Product documentation (Anthropic); referenced at Claude Code v2.1.191

**Relevance to securing & governing AI coding agents:** Agent-native, session-level undo for blast-radius recovery — but bounded to model-driven edits, so it is not a substitute for version control or filesystem-level rollback of destructive bash operations.

[Read the original →](https://code.claude.com/docs/en/checkpointing)

*Source: Anthropic · hand-catalogued 2026-06-28 (not auto-fetched). Verify version/date at time of use.*
