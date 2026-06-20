---
title: Home
hide:
  - navigation
---

# AI-Agent Engineering Knowledge Wiki

A curated, searchable knowledge base of the strongest **authoritative sources**
on how to build, secure, orchestrate, develop with, and evaluate **AI coding
agents** across the software development lifecycle.

Sources are filtered for primary, recent (2024–2026) material from five priority
organizations — **Anthropic, Google, Microsoft/GitHub, ThoughtWorks, and Martin
Fowler** — plus high-value practitioner and academic references.

## Themes

<div class="grid cards" markdown>

-   :material-layers-triple: **[Context Engineering](context-engineering/index.md)**

    Curating the optimal set of tokens during inference, and emerging patterns
    for context across multiple repositories (repo-of-repos, spine, shared
    instructions, MCP scope).

-   :material-shield-lock: **[Security & Governance](security-governance/index.md)**

    Authorization and guardrails, governance frameworks (SAIF, maturity models),
    safe-autonomy engineering, and PCI DSS considerations for agents touching
    cardholder data.

-   :material-sitemap: **[Orchestration & Workflows](orchestration/index.md)**

    Workflows vs. agents, orchestrator-worker and planner/executor patterns, and
    named topologies (sequential, concurrent, group chat, handoff, magentic).

-   :material-file-document-edit: **[Spec / Test / Constraints-Driven Dev](spec-driven/index.md)**

    Spec-driven development (Spec Kit, Kiro, Conductor), test-driven development
    as the strongest pattern, and constraints/steering approaches.

-   :material-chart-line: **[Evals & Observability](evals-observability/index.md)**

    Evals-as-regression-sentinels, observability pipelines (OpenTelemetry GenAI),
    deterministic quality gates, and benchmarks (SWE-bench Verified).

</div>

## How to use this wiki

- **Browse** by theme using the tabs above; each theme opens with a curated
  overview that links to every source, grouped by organization.
- **Search** (top of the page) covers every source's title, annotation, and
  excerpt — try `orchestrator-worker`, `SAIF`, or `spec-driven`.
- **Filter by [tag](tags.md)** to see all sources from one organization or theme.

Each source page carries a short **annotation** (why it matters), a brief
**excerpt** from the original, and a link to **read the original**. See
[About](about.md) for how the wiki is built and why it shows excerpts rather
than full text.
