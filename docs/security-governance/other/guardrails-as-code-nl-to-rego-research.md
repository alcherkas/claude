---
title: 'Guardrails-as-code: natural-language to Rego/policy (research)'
url: https://machinelearning.apple.com/research/prose2policy
canonical_url: https://machinelearning.apple.com/research/prose2policy
org: Other
theme: security-governance
subtopic: patterns
source_type: html
tags:
- Other
- security-governance
- patterns
fetch_status: ok
http_status: 200
fetched_at: '2026-06-28'
also_in: null
---
# Guardrails-as-code: natural-language to Rego/policy (research)

> **Excerpt from the original:** Research systems translating natural-language policy into executable enforcement: ARPaCCino (agentic-RAG NL->Rego loop with `opa check` syntax validation and a semantic Rule Checker; domain is IaC/Terraform compliance); Apple's Prose2Policy (LLM pipeline NL->executable Rego — 95.3% compile rate, 82.2% positive-test, 98.9% negative-test on the ACRE access-control dataset); and Policy-as-Prompt (converts PRDs/TDDs/code into a source-linked policy tree compiled to prompt-based classifiers).

**License:** Research preprints (arXiv / Apple ML Research)

**Relevance to securing & governing AI coding agents:** Demonstrates that guardrails-as-code (NL->policy) is feasible and quantified, but remains research-stage and aimed at IaC/access-control — no production NL-to-Rego coding-agent governance product was confirmed. Answers the standing open question: NL->Rego is still research-only; the engines it targets (OPA/Cedar) are production-ready.

[Read the original →](https://machinelearning.apple.com/research/prose2policy)

*Source: Other · hand-catalogued 2026-06-28 (not auto-fetched). Verify version/date at time of use.*
