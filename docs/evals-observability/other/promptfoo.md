---
title: 'Promptfoo: CI/CD eval runner & quality gate'
url: https://www.promptfoo.dev/docs/integrations/ci-cd/
canonical_url: https://www.promptfoo.dev/docs/integrations/ci-cd
org: Other
theme: evals-observability
subtopic: tools
source_type: html
tags:
- Other
- evals-observability
- tools
fetch_status: ok
http_status: 200
fetched_at: '2026-06-28'
also_in: null
---
# Promptfoo: CI/CD eval runner & quality gate

> **Excerpt from the original:** Promptfoo runs prompt, agent, and RAG evaluations as a CI/CD quality gate — via the `npx promptfoo@latest eval` CLI and an official GitHub Actions marketplace action (`promptfoo/promptfoo-action`) that runs when monitored files change on `pull_request`/`push`/`workflow_dispatch`.

**License:** MIT (open-source).

**Integration / coding-agent relevance:** Documented support for GitHub Actions, GitLab CI, Jenkins, Azure Pipelines, CircleCI, Bitbucket, and Travis CI. Builds can be failed/blocked via `--fail-on-error`, custom pass-rate thresholds computed from result stats, and a `fail-on-threshold` input (0–100%), with `repeat`/`min-pass` to tolerate flaky LLM-grader variance. General-purpose LLM tooling adaptable to coding-agent pipelines.

[Read the original →](https://www.promptfoo.dev/docs/integrations/ci-cd/)

*Source: Other · hand-catalogued 2026-06-28 from the Tools research pass (3-vote verified). Verify version/license at time of use.*
