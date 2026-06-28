---
title: 'SCIP: Source Code Intelligence Protocol (cross-repo symbol IDs)'
url: https://scip-code.org/
canonical_url: https://scip-code.org/
org: Other
theme: context-engineering
subtopic: tools
source_type: html
tags:
- Other
- context-engineering
- tools
fetch_status: ok
http_status: 200
fetched_at: '2026-06-28'
also_in: null
---

# SCIP: Source Code Intelligence Protocol (cross-repo symbol IDs)

> **Excerpt from the original:** SCIP is a language-agnostic, Protobuf-based code-indexing format (the LSIF successor) whose **symbol IDs encode package name + version**, so an imported symbol in one repository resolves to the exact file and line of its definition in the *dependency* repository. The format is **open (Apache-2.0)** with independent governance, and is ingested by tools beyond Sourcegraph — including Mozilla's Searchfox and Meta's Glean.

**Relevance to multi-repo context engineering:** This is the underlying *mechanism* for **true cross-repo symbol navigation** — the thing that distinguishes a real cross-repo system from multi-root file packing. Broad language indexers exist (scip-typescript, scip-java, scip-python, scip-clang, scip-go, rust-analyzer, …). Key caveat: **SCIP files alone are inert** — cross-repo resolution requires a coordination/broker layer to stitch indexes together, and in practice that broker is usually Sourcegraph.

[Read the original →](https://scip-code.org/)

*Source: Other · scip-code.org · archived 2026-06-28.*
