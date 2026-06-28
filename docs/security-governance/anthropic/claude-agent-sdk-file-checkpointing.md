---
title: 'Claude Agent SDK file checkpointing (rewindFiles)'
url: https://platform.claude.com/docs/en/agent-sdk/file-checkpointing
canonical_url: https://platform.claude.com/docs/en/agent-sdk/file-checkpointing
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
# Claude Agent SDK file checkpointing (rewindFiles)

> **Excerpt from the original:** The Claude Agent SDK backs up files before Write/Edit/NotebookEdit modifications and exposes `rewindFiles()` / `rewind_files()` to restore any checkpoint. It explicitly does **not** track Bash-driven changes (e.g. `echo > file.txt`, `sed -i`).

**License / type:** Anthropic developer SDK (docs).

**Relevance to securing & governing AI coding agents:** The programmatic, SDK-level form of rollback for custom agents — the same model-driven-edit scope (and the same documented limitation) as [Claude Code /rewind](claude-code-checkpointing-and-rewind.md). Naming the Bash gap in first-party docs is what makes it citable evidence for **seam 3** of the [reference architecture](../reference-architecture.md): file-checkpointing tools across the ecosystem ([Roo Code](https://docs.roocode.com/features/checkpoints/) shadow-git, [Hermes](https://hermes-agent.nousresearch.com/docs/user-guide/checkpoints-and-rollback) auto-snapshots) recover model edits but not arbitrary Bash side effects — compensate with ephemeral workspaces + version control + branch protection.

*(3-0 verified, June 2026.)*

[Read the original →](https://platform.claude.com/docs/en/agent-sdk/file-checkpointing)

*Source: Anthropic · hand-catalogued 2026-06-28 (deep-research verified, not auto-fetched). Doc URLs may redirect to new hosts — verify at time of use.*
