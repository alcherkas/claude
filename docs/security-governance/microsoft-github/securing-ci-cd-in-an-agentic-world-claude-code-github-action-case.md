---
title: 'Securing CI/CD in an agentic world: Claude Code Github action case | Microsoft
  Security Blog'
url: https://www.microsoft.com/en-us/security/blog/2026/06/05/securing-ci-cd-in-agentic-world-claude-code-github-action-case/
canonical_url: https://microsoft.com/en-us/security/blog/2026/06/05/securing-ci-cd-in-agentic-world-claude-code-github-action-case
org: Microsoft/GitHub
theme: security-governance
subtopic: null
source_type: html
tags:
- Microsoft/GitHub
- security-governance
fetch_status: ok
http_status: 200
fetched_at: '2026-06-20'
also_in: null
---

# Securing CI/CD in an agentic world: Claude Code Github action case | Microsoft Security Blog

prompt-injection to workflow secrets; "Agents Rule of Two"

> **Excerpt from the original:** Microsoft Threat Intelligence discovered that Anthropic’s Claude Code GitHub Action could expose CI/CD workflow secrets when AI agents process untrusted GitHub content, including issue bodies, pull request descriptions, and comments. We found that while Claude Code Action supported environment scrubbing for subprocess execution paths such as Bash, the Read tool was not subject to the same sandboxing model. It was eventually authorized to access /proc/self/environ, reading the workflow’s ANTHROPIC_API_KEY and potentially other credentials available to …

[Read the original →](https://www.microsoft.com/en-us/security/blog/2026/06/05/securing-ci-cd-in-agentic-world-claude-code-github-action-case/)

*Source: Microsoft/GitHub · Microsoft Security Blog · 2026-06-05 · archived 2026-06-20.*
