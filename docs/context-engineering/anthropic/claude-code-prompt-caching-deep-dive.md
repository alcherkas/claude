---
title: 'Claude Code prompt caching: what invalidates the cache mid-session'
url: https://code.claude.com/docs/en/prompt-caching
canonical_url: https://code.claude.com/docs/en/prompt-caching
org: Anthropic
theme: context-engineering
subtopic: null
source_type: html
tags:
- Anthropic
- context-engineering
- prompt-caching
fetch_status: ok
http_status: 200
fetched_at: '2026-06-28'
also_in: null
---

# Claude Code prompt caching: what invalidates the cache mid-session

> **Why this page:** Claude Code reuses a prompt cache across turns so it doesn't re-read your whole conversation every time. A handful of mid-session actions silently bust that cache, forcing a full uncached re-read at write-rate pricing. This page distills the documented invalidation rules and cost mechanics so you can time expensive operations at natural breaks.

## The cache key

The prompt cache is keyed by **`(model, effort level, fast-mode header, exact prefix hash)`**, scoped to one **machine + working directory**. Change any element of the key and that cache entry is gone.

The cached prefix is layered, top to bottom:

1. **System prompt + tool definitions** (core instructions, output style, available tools)
2. **Project context** (CLAUDE.md, auto-memory, unscoped permission rules)
3. **Conversation history** (all prior user messages, assistant turns, tool results)

Your current message is always the uncached tail. Invalidating a higher layer cascades to everything below it.

## What busts the cache — and how badly

### Full re-read of the entire history (most expensive)
- **`/effort` change** — effort level is part of the cache key, so a new level is a different cache entry. Claude Code shows a confirmation dialog first (unless it resolves to the level already in effect).
- **`/model` switch** — each model has its own cache store. Includes `opusplan` toggling Opus ↔ Sonnet (plan → execute → plan = two re-reads) and Fable 5's safety re-routing.
- **`/fast` toggle** — adds a request header that is part of the cache key. Enable it at session start. (Claude Code v2.1.86+ keeps the cache on subsequent toggles; older versions re-bust on every toggle.)
- **Denying a bare tool** (e.g. deny `Bash`) — removes it from the system-prompt layer.
- **Claude Code upgrade** — a new version changes the system prompt/tools, so the first request after upgrade re-reads everything. Resuming a *long* session after an upgrade is the worst case.
- **Idle past the TTL** — the cache silently expires; the first turn back is a full uncached re-read.

### Partial invalidation (cheaper — message layer only)
- **Extended thinking on/off, or changing `thinking.budget_tokens`** — invalidates the message layer; the system prompt stays cached. (On Fable 5, thinking is adaptive per effort and cannot be turned off; `MAX_THINKING_TOKENS=0` has no effect.)
- **Screenshot add/remove** — images live in the messages array, so toggling one changes the message prefix.
- **`/compact`** — replaces history with a summary; system prompt and project context stay cached, only the (now much shorter) conversation rebuilds. The summarization request itself reads the full prefix from cache. Run `/compact` at task boundaries so *you* control when the rebuild happens, rather than auto-compaction firing mid-task.

### Cache-safe — the surprising ones
- **Editing CLAUDE.md mid-session** — double gotcha: it doesn't bust the cache *and the edit has no effect* until `/clear`, `/compact`, or restart (CLAUDE.md is read once at start).
- **Output-style change mid-session** — same: no effect and no bust until restart.
- **Scoped permission rules** like `Bash(rm *)` — checked at call time, not embedded in the prompt.
- **`/rewind`** — the remaining history matches an earlier, still-valid cache entry.
- **Invoking skills/commands** — appended as a user message after the cached prefix.
- **File edits** — append as system reminders; the file body is only in history once read.
- **Connecting/disconnecting an MCP server *when tool deferral is on*** (default on Opus 4.8+, Sonnet 4.6+, Fable 5) — tools load on demand as appended `tool_reference` blocks rather than in the system prompt. ⚠️ With *loaded* (non-deferred) tools, or on Haiku / Vertex / custom `ANTHROPIC_BASE_URL` gateways where deferral is unavailable, MCP changes **do** bust the system-prompt layer.

## TTL and pricing

- **TTL:** A Claude subscription auto-uses a **1-hour** cache (included). API key / Bedrock / Vertex default to **5 minutes**; opt into 1 hour with `ENABLE_PROMPT_CACHING_1H=1`, or force short with `FORCE_PROMPT_CACHING_5M=1`. Over plan limits → drops to 5 minutes. Each cache-hitting request resets the idle timer.
- **Pricing multipliers (× base input):** 5-minute cache write = **1.25×**; 1-hour cache write = **2.0×**; cache read = **0.10×**. A single write pays for itself after ~2–3 hits on a large prefix.
- **Minimum cacheable prefix:** Fable 5 = 512 tokens; Opus 4.8 / Sonnet 4.6 / Haiku 4.5 = 1,024; older Opus higher. Verify with `cache_creation_input_tokens` / `cache_read_input_tokens` in usage — both `0` means nothing was cached.

## Other behaviors worth knowing

- **Subagents** start with no parent cache (their own system prompt + tools) and use a 5-minute TTL even on a subscription — though *spawning* one is cache-safe for the parent.
- **Git worktrees** get separate caches: the system prompt embeds the working directory and git status (branch + commits), so each worktree is a distinct prefix. Parallel sessions in the *same* directory with matching git status share a cache.
- **Auto-compaction** drops older tool outputs first, then summarizes the conversation — early in-conversation instructions can be lost, which is why durable rules belong in CLAUDE.md.
- **Server tool results** (web search/fetch, code execution) get an automatic 5-minute cache breakpoint mid-agentic-loop when the request already carries a `cache_control` marker.

## Invalidation cheat-sheet

| Action | System layer | Message layer | Full re-read |
|---|---|---|---|
| `/model` switch | ✕ | ✕ | **yes** |
| `/effort` change | ✕ | ✕ | **yes** |
| `/fast` toggle | ✕ | ✕ | **yes** |
| Deny a bare tool | ✕ | ✕ | **yes** |
| Claude Code upgrade | ✕ | ✕ | **yes** |
| Idle > TTL | (expired) | (expired) | **yes** |
| Thinking param change | ✓ kept | ✕ | partial |
| Screenshot add/remove | ✓ kept | ✕ | partial |
| `/compact` | ✓ kept | ✕ (new short history) | no |
| Edit CLAUDE.md mid-session | ✓ kept | ✓ kept | no (and no effect) |
| Output-style change | ✓ kept | ✓ kept | no (and no effect) |
| Scoped permission rule | ✓ kept | ✓ kept | no |
| `/rewind` | ✓ kept | ✓ kept | no |
| Invoke skill/command | ✓ kept | ✓ kept | no |
| MCP connect/disconnect (deferred tools) | ✓ kept | ✓ kept | no |

## Practical takeaway

Batch your expensive mid-session changes — model, effort, fast mode — at natural breaks rather than mid-task, and prefer `/compact` at task boundaries over letting auto-compaction fire. Stepping away longer than the TTL costs a full re-read on return, so the subscription's 1-hour cache is meaningfully cheaper for stop-start work.

[Read the original →](https://code.claude.com/docs/en/prompt-caching)

*Source: Anthropic · Claude Code docs · synthesized 2026-06-28. Distilled from the Claude Code prompt-caching, model-config, and extended-thinking / tool-use-with-prompt-caching documentation.*
