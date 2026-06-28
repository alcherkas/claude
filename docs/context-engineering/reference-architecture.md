---
title: Reference architecture — solution-level context across repositories
---

# Reference architecture — solution-level context across repositories

!!! note "What this page is"
    A **synthesis**, not a single external source. No vendor has published an end-to-end reference architecture for giving AI coding agents context across multiple repositories, so this page stitches one together from the primary sources catalogued in this wiki — the production [case studies](index.md#production-case-studies) (Stripe, Spotify, Uber), the [cross-repo tooling](index.md#multi-repo-cross-repo), and the [evaluation](../evals-observability/index.md#cross-repo-retrieval-quality-evaluation-emerging) findings. Layers that remain immature are marked **⚠ seam**. Assembled June 2026.

## The pipeline

A solution-level context system is an assembly of eight layers. Most are well-covered; two are genuine seams you must build yourself.

```
            ┌─────────────────────────────────────────────────────────┐
 repos ──►  │ 1 TOPOLOGY      meta-repo / multi-root workspace          │
            │ 2 INGEST        checkout / packing (Repomix, code2prompt) │
            │ 3 RETRIEVE      cross-repo index  ◄── the hard core       │
            │ 4 INSTRUCTIONS  AGENTS.md / shared-instruction repos      │
            │ 5 MEMORY        git-based / per-repo distillation         │
            │ 6 ORCHESTRATE   harness, sub-agents, blueprints           │
            │ 7 GOVERN        widened PAT/MCP scope, MCP gateway        │
            │ 8 EVALUATE      cross-repo retrieval quality   ⚠ seam     │
            └─────────────────────────────────────────────────────────┘
                         ▲                              │
                         └────────── feedback ──────────┘
```

## Layer by layer

| # | Layer | What to use | Maturity |
|---|---|---|---|
| 1 | **Topology** | [meta-repo / virtual monorepo](other/meta-repo-virtual-monorepo-mateodelnorte.md) umbrella; [multi-root workspaces](other/how-copilot-understands-your-workspace.md) (vendor-primary). | Adequate — converged practice, no vendor-blessed "pattern" |
| 2 | **Ingest / packing** | [Repomix](other/repomix-pack-your-codebase-into-ai-friendly-formats.md), [code2prompt](other/code2prompt-convert-your-codebase-into-a-single-llm-prompt.md). | Strong |
| 3 | **Cross-repo retrieval** | [Augment Context Engine MCP](other/augment-context-engine-mcp-cross-repo-context.md) (agent-facing, gated); [Sourcegraph/SCIP](other/sourcegraph-cody-cross-repository-context-via-scip-code-graph.md) + [SCIP](other/scip-source-code-intelligence-protocol.md); [Zoekt](other/zoekt-multi-repo-trigram-code-search.md)/[Sourcebot](other/sourcebot-multi-repo-code-search-mcp.md) (OSS-ish); [code-review-graph](other/code-review-graph-multi-repo-code-knowledge-graph-over-mcp.md). | **The hard core** — workable, but no mature *open + semantic* option |
| 4 | **Shared instructions** | [AGENTS.md](microsoft-github/agents-md-open-format.md) (Linux Foundation); [curated shared instructions](thoughtworks/curated-shared-instructions-for-software-teams-technology-radar.md). | Strong |
| 5 | **Agent memory** | [Context Repositories](other/introducing-context-repositories-git-based-memory-for-coding-agents.md), [memory tool](anthropic/memory-tool-claude-api-docs.md), [RepoSwarm distillation](other/reposwarm-give-ai-agents-context-across-all-your-repos.md). | Adequate |
| 6 | **Orchestration** | [harness engineering](martin-fowler/harness-engineering-for-coding-agent-users.md); Stripe "blueprints", [Copilot CLI /fleet](microsoft-github/github-copilot-cli.md). Deeper in the [orchestration topic](../orchestration/index.md). | Adequate |
| 7 | **Governance / access** | [widened MCP/PAT scope](microsoft-github/connect-agents-to-external-tools.md); Uber's **MCP Gateway** (authz + telemetry). | Adequate |
| 8 | **Evaluation** | [ContextBench methodology](../evals-observability/other/contextbench-gold-context-retrieval-benchmark.md); [dependency-closure recall](../evals-observability/other/hydra-dependency-aware-retriever.md). | **⚠ seam** — no benchmark tests true cross-repo retrieval |

## How the production case studies map on

The three public [case studies](index.md#production-case-studies) each implement a *subset* of this pipeline — which is exactly why no single one is a complete blueprint:

- **[Stripe "Minions"](other/stripe-minions-one-shot-coding-agents.md)** — strongest on 2/3/6/8: fresh-checkout ingest, **Sourcegraph retrieval via MCP**, "blueprint" orchestration wiring deterministic + agentic nodes, capped-CI verification. Persistent embedding index and memory are not central.
- **[Spotify "Honk"](other/spotify-honk-background-coding-agents.md)** — strongest on 1/4/6/8: poly-repo fleet, *static curated instructions over dynamic retrieval*, deterministic verifiers + LLM-as-judge. Deliberately **minimal on layer 3**.
- **[Uber](other/uber-how-uber-uses-ai-for-development.md)** — strongest on 6/7: monorepo (layer 3 moot), with an **MCP Gateway** as the governance/wiring layer most others omit.

## The two seams

1. **No published end-to-end blueprint.** This page is the closest assembly; treat it as a starting skeleton, not a proven design.
2. **No cross-repo retrieval evaluation** (layer 8). Every public benchmark is single-repo; to validate, build gold-context ground truth yourself (static dependency closure / PR history / CODEOWNERS) and score **Context Recall/Precision/F1** and **dependency-closure recall**. See the [cross-repo evaluation section](../evals-observability/index.md#cross-repo-retrieval-quality-evaluation-emerging).

## Governing principle: more context ≠ better

The strongest cross-cutting finding (production *and* benchmark): **flooding the agent with repos degrades accuracy.** Optimize for *selecting the right cross-repo slice* — dependency closure, just-in-time retrieval — not for packing everything. Cross-repo retrieval is inherently high-recall / low-precision, so managing precision is the real work. See [Anti-pattern: more context ≠ better](index.md#anti-pattern-more-context--better).

## A pragmatic starting path

1. **Topology + instructions first** (layers 1, 4) — cheapest, highest leverage: a meta-repo/workspace plus committed `AGENTS.md`/`CLAUDE.md`.
2. **Add retrieval** (layer 3) — start with [Sourcebot](other/sourcebot-multi-repo-code-search-mcp.md)/[Zoekt](other/zoekt-multi-repo-trigram-code-search.md) (self-host) or [Augment](other/augment-context-engine-mcp-cross-repo-context.md) (managed) over an MCP server.
3. **Wire a harness + governance** (layers 6, 7) — deterministic gates in the loop, an MCP gateway for authz/telemetry.
4. **Stand up evaluation early** (layer 8) — even a small hand-annotated gold-context set beats flying blind; it is the only way to tune layer 3 against over-retrieval.
