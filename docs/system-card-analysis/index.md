---
title: "System Card Analysis — Practitioner Takeaways from Frontier-Model System Cards"
---

# System Card Analysis — Practitioner Takeaways from Frontier-Model System Cards

## TL;DR

- A **system card** is a vendor's pre-deployment safety and capability report for a frontier model — documenting evaluations, alignment findings, known failure modes, and the mitigations the release ships with.
- This theme **distills the actionable signal** from those cards: deployment guidance, page-cited best practices, and the regressions and quirks you have to engineer around — so you don't have to read 200+ pages to know what changes for your stack.
- These pages are **hand-authored analysis**, not source mirrors or excerpts — opinionated, cross-referenced, and organized by what a practitioner does with the information.
- The theme is **actively growing**; expect new pages as additional frontier-model cards land.

## Key Findings (how to use this theme)

- **Best practices grouped by audience** — build, app, ops, eval, and safety — so you can jump to the recommendations that match your role.
- **Every claim is page-cited** back to the source card, so you can verify and quote the original without re-deriving it.
- **Deployment-first framing** — what to enable, what to re-test on migration, and what to never trust — including a dedicated **Claude Code playbook** mapping card findings to concrete harness mechanisms (permission modes, hooks, sandboxing, egress allowlists).
- **Capability numbers are decomposed, not headlined** — safeguards-off vs safeguards-on, threat-relevant subsets vs aggregates, and effort/budget sensitivity — so benchmark figures don't mislead your rollout.

## Analyses

### Anthropic

- [Claude Opus 4.8](opus-4-8.md) — 28 May 2026, 246 pp.; the safeguards-are-load-bearing through-line, two agentic/cyber regressions, and ~45 page-cited best practices incl. a Claude Code playbook.

*More analyses are coming as new frontier-model system cards are published.*