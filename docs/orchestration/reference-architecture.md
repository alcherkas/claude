---
title: Reference architecture — an end-to-end agentic orchestration system
---

# Reference architecture — an end-to-end agentic orchestration system

!!! note "What this page is"
    A **synthesis**, not a single external source. No vendor has published an end-to-end reference architecture for building a production multi-agent orchestration system, so this page stitches one together from the primary sources catalogued in this topic — the [Tools](index.md#tools) parts list, the [Patterns & Techniques](index.md#patterns-techniques) catalog, and the cross-linked [context-engineering](../context-engineering/index.md), [evals-observability](../evals-observability/index.md), and [security-governance](../security-governance/index.md) topics. The design principle throughout is **add a layer only when forced to**: every step from a single agent to a full orchestration multiplies cost and failure surface, so each layer must earn its place. Layers that remain immature are marked **⚠ seam**. Assembled June 2026.

## The pipeline

An agentic orchestration system is an assembly of six build layers, gated at the top by a **decision** ("should you orchestrate at all?"), wrapped by a **governance** layer, and closed by an **evaluate + observe** feedback loop. Most layers are well-covered by primary sources; three are genuine seams you must engineer around.

```
  GOVERN   safe-autonomy wrapper  →  see security-governance reference architecture
  ┌──────────────────────────────────────────────────────────────────┐
  │  DECIDE  single agent? deterministic workflow? multi-agent?       │
  │     │     Anthropic's bar — go multi-agent ONLY if work exceeds   │
  │     ▼     one context window, parallelizes, or spans many tools   │
  │           (else don't pay the ~15× token cost)                    │
  │  1 PATTERN     compose: chaining·routing·parallel·orchestrator-   │
  │                workers·evaluator-optimizer → named topology       │
  │                (sequential/concurrent/group-chat/handoff/magentic)│
  │  2 FRAMEWORK   OpenAI Agents SDK · MS Agent Framework · Google    │
  │                ADK · LangGraph-supervisor                         │
  │  3 INTEROP     MCP (agent→tool)   +   A2A (agent→agent)        ⚠  │
  │  4 CONTEXT     per-subagent isolation · compaction · note-taking  │
  │  5 MEMORY      full-context first → Letta / Zep / Mem0         ⚠  │
  │  6 DURABLE     journal-and-replay — Temporal/Restate/Dapr/Inngest │
  │  7 DEPLOY      managed runtime (AgentCore/Agent Engine/Foundry)   │
  │                or self-host the substrate above                   │
  └──────────────────────────────────────────────────────────────────┘
            ▲                                                      │
            └──── EVALUATE + OBSERVE (eval-driven · traces · OTel) ⚠┘
```

## Layer by layer

| # | Layer | What to use | Maturity |
|---|---|---|---|
| 0 | **Decide** | [Building Effective Agents](anthropic/building-effective-agents.md) (workflow-vs-agent distinction + the "don't orchestrate unless…" bar); [Don't Build Multi-Agents](other/don-t-build-multi-agents.md); [Why Multi-Agent Systems Fail](other/why-do-multi-agent-llm-systems-fail.md) (MAST). | Strong — the most-cited framing in the field |
| 1 | **Pattern / topology** | Five composable patterns from [Building Effective Agents](anthropic/building-effective-agents.md); named topologies from the [Azure Architecture Center](microsoft-github/ai-agent-orchestration-patterns-azure-architecture-center-the-canonical-named.md) + [Semantic Kernel](microsoft-github/semantic-kernel-agent-orchestration.md); the [orchestrator-worker case study](anthropic/how-we-built-our-multi-agent-research-system.md) and [Magentic-One](microsoft-github/magentic-one-a-generalist-multi-agent-system.md) planner/executor. | Strong |
| 2 | **Framework** | [OpenAI Agents SDK](other/openai-agents-sdk-agent-orchestration.md); [Microsoft Agent Framework](microsoft-github/microsoft-agent-framework-overview.md); [Google ADK primitives](google/adk-custom-agents-orchestration-primitives.md); [langgraph-supervisor](other/langgraph-supervisor-hierarchical-multi-agent-systems-on-langgraph.md). | Strong — but verify MS Agent Framework GA status |
| 3 | **Interop / protocol** | [MCP](../context-engineering/other/model-context-protocol-specification-2025-06-18.md) for agent→tool; [A2A](google/agent2agent-protocol-specification.md) for agent→agent. | MCP strong; **⚠ A2A adoption contested**, enterprise-oriented, still maturing |
| 4 | **Context** | [per-subagent context isolation · compaction · note-taking](../context-engineering/anthropic/effective-context-engineering-for-ai-agents.md); [durable/resumable harnesses](../context-engineering/anthropic/effective-harnesses-for-long-running-agents.md). | Strong — deeper in the [context-engineering topic](../context-engineering/index.md) |
| 5 | **Memory / state** | full-context first ([ConvoMem](other/convomem-first-150-conversations-dont-need-rag.md)), then [Letta](other/memgpt-letta-llm-operating-system-agent-memory.md) (OS-tiered) / [Zep](other/zep-graphiti-temporal-knowledge-graph-agent-memory.md) (temporal KG) / [Mem0](other/mem0-scalable-long-term-memory-for-ai-agents.md) (vector-first). | **⚠ seam** — architectures clear, but benchmarks vendor-disputed and the layer is *scale-triggered*, not default |
| 6 | **Durable execution** | [Temporal](other/temporal-durable-execution-for-ai-agents.md), [Restate](other/restate-durable-execution-for-agentic-loops.md) (framework-agnostic), [Dapr Agents](other/dapr-agents-durable-agentic-workflows.md), [Inngest](other/inngest-durable-execution-for-ai-agents.md). | Strong substrate; agent-specific patterns still emerging |
| 7 | **Deploy / runtime** | self-host the substrate above, **or** a managed runtime that bundles durability/scaling/identity/observability: [AWS Bedrock AgentCore](other/aws-bedrock-agentcore-managed-agent-runtime.md) (microVM-per-session), [Google Vertex AI Agent Engine](google/build-and-manage-multi-system-agents-with-vertex-ai.md) (free tier), [Azure AI Foundry Agent Service](microsoft-github/azure-foundry-agent-service-managed-runtime.md) (`azd`/Bicep, agents-as-assets). | Strong — all three GA & framework/model-agnostic; pricing & preview status move fast |
| ↺ | **Evaluate + observe** | eval-driven development, trajectory evals, LLM-as-judge, CI gates, OTel GenAI traces — the whole [evals-observability topic](../evals-observability/index.md). | **⚠ seam** for *multi-agent* eval — MAST shows failures are coordination-driven; no standard for grading an orchestration vs. a single agent |
| ▣ | **Govern** | sandboxing, out-of-model policy-as-code, permission modes, kill switches — the [security-governance reference architecture](../security-governance/reference-architecture.md). | Covered there; assume the model can be fooled |

## How the canonical implementations map on

Each reference system in this topic implements a *subset* of the pipeline — which is exactly why no single one is a complete blueprint:

- **[Anthropic multi-agent research system](anthropic/how-we-built-our-multi-agent-research-system.md)** — strongest on 1/4/5: a lead agent plans and stores its plan in **memory**, spawns parallel subagents each with an **isolated context window**, and a CitationAgent attributes. Light on layer 6 (durable execution).
- **[Magentic-One](microsoft-github/magentic-one-a-generalist-multi-agent-system.md)** — strongest on 1/2: the reference **planner/executor** (Orchestrator directs WebSurfer/FileSurfer/Coder/ComputerTerminal), with re-planning on error, built on AutoGen. The cleanest concrete topology implementation.
- **Google ADK + A2A + Agent Engine** — strongest on 2/3: deterministic [workflow agents](google/adk-custom-agents-orchestration-primitives.md) (`SequentialAgent`/`ParallelAgent`/`LoopAgent`) alongside LLM-driven routing, plus [A2A](google/agent2agent-protocol-specification.md) interop and a managed runtime.

## The three seams

1. **No published end-to-end blueprint.** This page is the closest assembly; treat it as a starting skeleton, not a proven design. The pieces are individually well-documented but the wiring is left to you.
2. **Agent memory (layer 5) is scale-triggered and benchmark-contested.** The three architectures are clear, but [ConvoMem](other/convomem-first-150-conversations-dont-need-rag.md) (vendor-independent) shows full context beats RAG-style memory under ~150 interactions, and the cross-vendor accuracy claims are an open dispute. Default to full context; add a memory system only when context volume forces it.
3. **No standard for multi-agent evaluation/observability.** [MAST](other/why-do-multi-agent-llm-systems-fail.md) finds failures are driven as much by coordination as by model capability, but there is no settled way to *evaluate an orchestration* (as opposed to a single agent). Instrument with OTel GenAI traces from day one and read the transcripts.

## Governing principle: add a layer only when forced to

The strongest cross-cutting finding is Anthropic's own cost guidance: **agents use ~4× the tokens of a chat, and multi-agent systems ~15×.** Token usage alone explains ~80% of the performance variance — multi-agent wins largely because parallel subagents with separate context windows can *spend more tokens* against the context limit. So every layer is a cost you justify, not a default you adopt: stay single-agent, then workflow, then orchestrator-worker, escalating only when the work clears the bar (exceeds one context window, benefits from parallelism, or spans many complex tools).

## A pragmatic starting path

1. **Start with a single agent or a deterministic [workflow](anthropic/building-effective-agents.md)** (layers 0–1) — cheapest, highest leverage; most tasks never need more.
2. **Add a framework + MCP tools** (layers 2–3) when you need real orchestration; pick one framework family early (MS Agent Framework *or* Google ADK) based on cloud/language.
3. **Go orchestrator-worker** (layer 1, multi-agent) only when work exceeds one context window or parallelizes — and budget for the ~15× token cost.
4. **Add durable execution** (layer 6) once runs are long enough to crash mid-flight; **add a memory system** (layer 5) only at [ConvoMem's](other/convomem-first-150-conversations-dont-need-rag.md) scale threshold.
5. **Wire evaluation + OTel observability from the start** (the ↺ feedback loop) and **govern** via the [security-governance reference architecture](../security-governance/reference-architecture.md) — these are not afterthoughts; they are how you keep the orchestration honest.
