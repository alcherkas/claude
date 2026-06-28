---
title: 'Snyk: CI/CD security scanning (deps, containers, IaC)'
url: https://github.com/snyk/actions
canonical_url: https://github.com/snyk/actions
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
# Snyk: CI/CD security scanning (deps, containers, IaC)

> **Excerpt from the original:** Snyk integrates into GitHub Actions CI/CD via per-stack actions (e.g. `snyk/actions/node@master`) added as a workflow step and authenticated with a `SNYK_TOKEN` secret. The actions cover open-source dependencies, Docker containers, and Infrastructure as Code, with results exported to GitHub Code Scanning / the Security tab via SARIF.

**License:** Commercial (free tier available; the `snyk/actions` wrappers are open source but the platform/engine is proprietary).

**Relevance to securing & governing AI coding agents:** A multi-product SCA/container/IaC gate for AI-generated code that drops into CI as discrete per-stack steps. Snyk also publishes practitioner guidance on [package hallucinations / "slopsquatting"](https://snyk.io/articles/package-hallucinations/) — the AI-specific risk that an agent imports a plausibly-named but malicious/nonexistent package — which is precisely why SCA matters on the agent path.

**Scans:** open-source dependencies, Docker containers, Infrastructure as Code (per-stack actions: node, python, golang, maven, gradle, ruby, php, docker, iac). *(3-0 verified, June 2026)*

**CI integration:** GitHub Actions (`uses: snyk/actions/<stack>@master`, `SNYK_TOKEN`); SARIF → GitHub Code Scanning (SARIF upload requires the `--sarif-file-output` flag, and GHAS for private repos). *(3-0 verified)*

[Read the original →](https://github.com/snyk/actions)

*Source: Other · hand-catalogued 2026-06-28 (deep-research verified, not auto-fetched). Pricing/plan terms are time-sensitive — verify at time of use.*
