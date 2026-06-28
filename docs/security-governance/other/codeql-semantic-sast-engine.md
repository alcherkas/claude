---
title: 'CodeQL: GitHub semantic SAST engine'
url: https://github.com/github/codeql-cli-binaries
canonical_url: https://github.com/github/codeql-cli-binaries
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
# CodeQL: GitHub semantic SAST engine

> **Excerpt from the original:** CodeQL is GitHub's semantic static-analysis engine: it builds a relational database of a codebase and runs QL queries against it to find vulnerability patterns. It powers GitHub code scanning. The CLI is **licensed, not sold** — free only for **open-source** code; using it on non-open-source code (e.g. a private repo) or in automated analysis/CI/CD on such code is prohibited under the standard license unless you hold a paid GitHub Advanced Security license.

**License:** Proprietary CLI (free for open-source codebases only); the query libraries (`github/codeql`) are open source. Packaged commercially as **GitHub Code Security** (a GitHub Advanced Security SKU), licensed **per unique active committer** (a committer who pushed within the last 90 days).

**Relevance to securing & governing AI coding agents:** The deepest semantic SAST layer for AI-generated code on GitHub — data-flow / taint analysis that pattern-based scanners miss — and the default scanner behind GitHub Copilot coding-agent PRs (see [Responsible use of the Copilot cloud agent](../microsoft-github/responsible-use-of-github-copilot-cloud-agent.md)). The license restriction is load-bearing: outside paid GHAS, CI/CD use is limited to open-source repos.

**Scans:** semantic SAST via QL queries over a code database (data-flow/taint), ~12 languages; no IaC/Terraform. *(3-0 verified, June 2026)*

**CI integration:** GitHub code scanning (`github/codeql-action`), SARIF to the Security tab; bundled in GitHub Code Security / GHAS. *(3-0 verified)*

[Read the original →](https://github.com/github/codeql-cli-binaries)

*Source: Other · hand-catalogued 2026-06-28 (deep-research verified, not auto-fetched). License terms carry exemptions (paid GHAS bypasses the open-source-only restriction) and are time-sensitive — verify at time of use.*
