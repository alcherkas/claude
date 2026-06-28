---
title: 'Semgrep: SAST engine + supply-chain scanning'
url: https://github.com/semgrep/semgrep
canonical_url: https://github.com/semgrep/semgrep
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
# Semgrep: SAST engine + supply-chain scanning

> **Excerpt from the original:** Semgrep is a fast, open-source static-analysis (SAST) engine supporting 30+ languages, with rules written in a syntax that mirrors the code being scanned. Semgrep Supply Chain adds SCA with reachability analysis across 12 languages / 15 package managers.

**License:** Open-core — the Semgrep CE engine / OSS CLI is **LGPL-2.1**; the commercial products (AppSec Platform, Code, Secrets, Supply Chain) are proprietary. A free tier covers SAST (Code) and SCA (Supply Chain) for up to **10 contributors and 10 repos**.

**Relevance to securing & governing AI coding agents:** The most-deployable open SAST layer for catching vulnerable patterns in AI-generated code, with custom YAML rules you can author as never-allow gates (pairs with the Thoughtworks ["VibeSec" never-allow-rule pattern](../thoughtworks/the-vibesec-reckoning-why-prompting-your-ai-to-be-secure-isn-t-enough.md)). Diff-aware: when configured to scan pull requests, it reports **only** issues newly introduced by that PR — ideal for gating agent-authored diffs without drowning in pre-existing findings.

**Scans:** SAST across 30+ languages (Python, JS/TS, Java, Go, Rust, C++, …); SCA via Semgrep Supply Chain (12 langs / 15 package managers, reachability). *(3-0 verified, June 2026)*

**CI integration:** GitHub Actions + other CI; diff-aware PR scanning. *(3-0 verified)*

[Read the original →](https://github.com/semgrep/semgrep)

*Source: Other · hand-catalogued 2026-06-28 (deep-research verified, not auto-fetched). License terms are time-sensitive — verify the open-core split and free-tier limits at time of use.*
