---
title: Agentic Orchestration & Workflows
---

# Agentic Orchestration & Workflows


!!! tip "Start here"
    📐 **[Reference architecture — an end-to-end agentic orchestration system](reference-architecture.md)** — the assembled six-layer build pipeline (pattern → framework → interop → context → memory → durable execution), gated by a decision step and closed by an evaluate+observe loop, how the canonical implementations (Anthropic multi-agent research, Magentic-One, Google ADK) map onto it, and the three immature seams (no published blueprint; scale-triggered memory; no multi-agent eval standard).

**TL;DR**
- The single most authoritative starting point is Anthropic's "Building Effective Agents," which establishes the field's foundational vocabulary (workflows vs. agents; the orchestrator-worker pattern), complemented by their "How we built our multi-agent research system" for a production planner/sub-agent case study.
- Across all five prioritized sources, the dominant documented patterns converge: planner/executor (plan-then-execute), orchestrator-worker (lead agent + sub-agents), and a small set of named orchestration topologies (sequential, concurrent, group chat, handoff, magentic/supervisor).
- Microsoft (Agent Framework, Azure Architecture Center, Magentic-One), Google (ADK, A2A, Vertex/Gemini Enterprise), ThoughtWorks (Technology Radar Vol. 33/34, Perspectives) and Martin Fowler (Exploring Gen AI series) provide the deepest current engineering/architecture guidance.

---

## Key Findings — Best Links by Organization

### 1. Anthropic
- [Building Effective Agents](../security-governance/anthropic/building-effective-ai-agents.md) — workflows vs agents; orchestrator-workers; prompt chaining; routing; parallelization; evaluator-optimizer
- [Building Effective Agents](anthropic/building-effective-agents.md) — engineering-blog mirror
- [How we built our multi-agent research system](anthropic/how-we-built-our-multi-agent-research-system.md) — orchestrator-worker; Lead Researcher + parallel subagents; CitationAgent
- [Effective context engineering for AI agents](../context-engineering/anthropic/effective-context-engineering-for-ai-agents.md) — Sep 29, 2025
- [Writing effective tools for AI agents](../context-engineering/anthropic/writing-effective-tools-for-ai-agents-using-ai-agents.md) — Sep 11, 2025
- [Building agents with the Claude Agent SDK](anthropic/building-agents-with-the-claude-agent-sdk.md)
- [Code execution with MCP: building more efficient AI agents](anthropic/code-execution-with-mcp-building-more-efficient-ai-agents.md)
- [Introducing advanced tool use on the Claude Developer Platform](anthropic/introducing-advanced-tool-use-on-the-claude-developer-platform.md) — Tool Search, Programmatic Tool Calling, Tool Use Examples
- [Equipping agents for the real world with Agent Skills](../context-engineering/anthropic/equipping-agents-for-the-real-world-with-agent-skills.md)
- [Effective harnesses for long-running agents](../context-engineering/anthropic/effective-harnesses-for-long-running-agents.md) — Nov 26, 2025; initializer + coding agent across context windows
- [How we contain Claude across products](../security-governance/anthropic/how-we-contain-claude-across-products.md) — agent sandboxing/isolation, MCP security
- [Building Effective AI Agents — Architecture Patterns & Implementation Frameworks](anthropic/building-effective-ai-agents-architecture-patterns-implementation-frameworks.md) — resource hub / PDF
- [Reference implementation](anthropic/reference-implementation.md) — code, anthropic-cookbook patterns/agents

### 2. Google (Cloud, DeepMind, Developers)
- [Developer's guide to multi-agent patterns in ADK](../context-engineering/google/developer-s-guide-to-multi-agent-patterns-in-adk.md) — sequential, parallel, coordinator/router, generator-critic loop
- [Agent Development Kit announcement](google/agent-development-kit-announcement.md)
- [ADK docs home](../context-engineering/google/agent-development-kit-adk-docs.md) — SequentialAgent / ParallelAgent / LoopAgent; graph workflows in ADK 2.0
- [ADK custom agents / orchestration primitives](google/adk-custom-agents-orchestration-primitives.md)
- [ADK technical overview](google/adk-technical-overview.md)
- [Build multi-agentic systems using Google ADK](google/build-multi-agentic-systems-using-google-adk.md) — Cloud blog
- [Announcing the Agent2Agent (A2A) Protocol](google/announcing-the-agent2agent-a2a-protocol.md) — Apr 9, 2025
- [Agent2Agent Protocol Specification](google/agent2agent-protocol-specification.md) — official A2A interoperability spec
- [A2A protocol upgrade](google/a2a-protocol-upgrade.md) — v0.3; now Linux Foundation–governed
- [What's new with Agents: ADK, Agent Engine, and A2A enhancements](google/what-s-new-with-agents-adk-agent-engine-and-a2a-enhancements.md) — v0.2, Python SDK
- [Build and manage multi-system agents with Vertex AI](google/build-and-manage-multi-system-agents-with-vertex-ai.md) — ADK, A2A, Agent Garden, Agent Engine
- [Introducing Gemini Enterprise Agent Platform](google/introducing-gemini-enterprise-agent-platform.md) — formerly Vertex AI
- [Gemini Enterprise Agent Platform / Agent Builder](google/gemini-enterprise-agent-platform-agent-builder.md) — product page
- [DeepMind AI Co-Scientist](google/deepmind-ai-co-scientist.md) — supervisor "freeform planner" + tournament-of-ideas multi-agent
- [Build Multi-Agent Systems with ADK](google/build-multi-agent-systems-with-adk.md) — hands-on codelab; "Plan and Execute"/"Draft and Revise"
- [Getting started with A2A](google/getting-started-with-a2a.md) — purchasing concierge codelab

### 3. Microsoft (Research, Azure, AutoGen, Semantic Kernel)
- [AI Agent Orchestration Patterns (Azure Architecture Center) — the canonical named-pattern reference: sequential, concurrent, group chat, handoff, magentic](microsoft-github/ai-agent-orchestration-patterns-azure-architecture-center-the-canonical-named.md)
- [Semantic Kernel Agent Orchestration](microsoft-github/semantic-kernel-agent-orchestration.md) — SequentialOrchestration / Concurrent / GroupChat / Handoff / Magentic
- [Introducing Microsoft Agent Framework](microsoft-github/introducing-microsoft-agent-framework-fc4621.md) — unifies AutoGen + Semantic Kernel; agent vs workflow orchestration
- [Microsoft Agent Framework overview](microsoft-github/microsoft-agent-framework-overview.md) — docs
- [Workflow orchestrations in Agent Framework](microsoft-github/workflow-orchestrations-in-agent-framework.md) — graph-based; checkpointing; human-in-the-loop
- [Microsoft Agent Framework](microsoft-github/microsoft-agent-framework.md) — GitHub
- [Multi-agent orchestration patterns and best practices (Copilot Studio) — inline/child agents, connected agents, parent-orchestrator prompting guidance](microsoft-github/multi-agent-orchestration-patterns-and-best-practices-copilot-studio-inline.md)
- [Orchestrate agent behavior with generative AI](microsoft-github/orchestrate-agent-behavior-with-generative-ai.md) — Copilot Studio planner
- [Magentic-One: A Generalist Multi-Agent System](microsoft-github/magentic-one-a-generalist-multi-agent-system.md) — Microsoft Research
- [Magentic-One paper](microsoft-github/magentic-one-paper.md) — arXiv:2411.04468
- [Magentic-One in AutoGen](microsoft-github/magentic-one-in-autogen.md) — docs; MagenticOneGroupChat
- [Introduction to AI Agent Orchestration Patterns](microsoft-github/introduction-to-ai-agent-orchestration-patterns.md) — training
- [Implement Advanced Multi-Agent Orchestration Patterns in Foundry](microsoft-github/implement-advanced-multi-agent-orchestration-patterns-in-foundry.md) — hub-and-spoke, supervisor/coordinator, parallel spawning
- [Process to build agents](../security-governance/microsoft-github/process-to-build-agents-across-your-organization.md) — Cloud Adoption Framework; orchestration governance

### 4. ThoughtWorks
- [Technology Radar](../security-governance/thoughtworks/technology-radar.md) — landing / latest volume
- [Technology Radar — Techniques](thoughtworks/technology-radar-techniques.md) — coding agent swarms vs agent teams; orchestrators/supervisors/ephemeral workers; Ralph Loop; sandboxed execution
- [Technology Radar — Platforms](thoughtworks/technology-radar-platforms.md) — Bedrock AgentCore, Vertex Agent Builder, Azure AI Foundry, Dialogflow CX
- [Technology Radar — Tools](thoughtworks/technology-radar-tools.md) — Google Antigravity multi-agent orchestration; Warp Oz
- [Technology Radar Vol. 33 news](thoughtworks/technology-radar-vol-33-news.md) — the rise of agents elevated by MCP
- [Themes from Technology Radar Vol. 33](thoughtworks/themes-from-technology-radar-vol-33.md) — podcast
- [Agentic AI: The business realities](thoughtworks/agentic-ai-the-business-realities.md) — Perspectives, edition 35
- [The agentic journey from idea to product](thoughtworks/the-agentic-journey-from-idea-to-product.md) — Agentic AI series, part 4
- [How agentic AI will change customer experience design](thoughtworks/how-agentic-ai-will-change-customer-experience-design.md) — series, part 2
- [Thoughtworks launches Agent/works — governed agent runtime/control plane](../security-governance/thoughtworks/thoughtworks-launches-agent-works-to-govern-enterprise-ai-agents.md) — Jun 16, 2026

### 5. Martin Fowler (martinfowler.com)
- [Exploring Generative AI](../context-engineering/martin-fowler/exploring-generative-ai.md) — living index of all memos; curated by Birgitta Böckeler
- [Emerging Patterns in Building GenAI Products](martin-fowler/emerging-patterns-in-building-genai-products.md) — Bharani Subramaniam & Martin Fowler; RAG, evals, guardrails, query rewriting, reranking patterns
- [Humans and Agents in Software Engineering Loops](../context-engineering/martin-fowler/humans-and-agents-in-software-engineering-loops.md) — Kief Morris, Mar 4, 2026; "why loop"/"how loop", on-the-loop
- [Harness Engineering — full article](../context-engineering/martin-fowler/harness-engineering-for-coding-agent-users.md) — Birgitta Böckeler, Apr 2, 2026
- [Harness Engineering — first thoughts](../context-engineering/martin-fowler/harness-engineering-first-thoughts.md) — memo, Feb 17, 2026
- [Context Engineering for Coding Agents](../context-engineering/martin-fowler/context-engineering-for-coding-agents.md) — Birgitta Böckeler, Feb 5, 2026
- [The role of developer skills in agentic coding](martin-fowler/the-role-of-developer-skills-in-agentic-coding.md) — Birgitta Böckeler, Mar 25, 2025
- [How far can we push AI autonomy in code generation?](../context-engineering/martin-fowler/how-far-can-we-push-ai-autonomy-in-code-generation.md) — Spring Boot multi-agent experiment, Aug 5, 2025
- [VibeCoding vs Agentic Programming](martin-fowler/vibecoding-vs-agentic-programming.md) — bliki; "semantic diffusion" of the term

### 6. Adjacent primary sources
- [Model Context Protocol Specification 2025-06-18](../context-engineering/other/model-context-protocol-specification-2025-06-18.md) — official agent-to-tool/context protocol spec; complements A2A's agent-to-agent layer
- [OpenAI Agents SDK - Agent orchestration](other/openai-agents-sdk-agent-orchestration.md) — LLM-driven vs. code-driven orchestration, agents-as-tools, and handoffs

---

## Details — What the Sources Actually Say

**The core distinction (Anthropic).** The most-cited framing in the entire literature, verbatim from "Building Effective Agents" (Dec 2024): *"Workflows are systems where LLMs and tools are orchestrated through predefined code paths. Agents, on the other hand, are systems where LLMs dynamically direct their own processes and tool usage, maintaining control over how they accomplish tasks."* That same article names the reusable building blocks the rest of the industry has adopted: prompt chaining, routing, parallelization, **orchestrator-workers**, and evaluator-optimizer.

**Orchestrator-worker / planner + sub-agents (Anthropic).** The multi-agent research system is the canonical production case study: a Lead Researcher agent plans, stores its plan in memory, and spawns parallel subagents, with a final CitationAgent for attribution. Anthropic reports that *"a multi-agent system with Claude Opus 4 as the lead agent and Claude Sonnet 4 subagents outperformed single-agent Claude Opus 4 by 90.2% on our internal research eval."* The trade-off is cost: Anthropic states verbatim that *"agents typically use about 4× more tokens than chat interactions, and multi-agent systems use about 15× more tokens than chats."* The mechanism: *"token usage by itself explains 80% of the variance, with the number of tool calls and the model choice as the two other explanatory factors,"* with all three together explaining 95% of performance variance on Anthropic's BrowseComp evaluation — i.e., multi-agent wins largely because parallel subagents with separate context windows can spend more tokens against the 200K context limit.

**Named orchestration topologies (Microsoft).** Microsoft has done the most to standardize pattern names. The Azure Architecture Center and Semantic Kernel both enumerate five: **sequential, concurrent, group chat, handoff, and magentic**. The "magentic" pattern is the explicit planner/executor archetype — a planner agent builds and documents a plan of approach while worker agents use tools to act in external systems, suitable for open-ended problems with no predetermined path.

**Magentic-One (Microsoft Research) is the reference planner/executor artifact.** Released November 2024 (arXiv:2411.04468), it *"employs a multi-agent architecture where a lead agent, the Orchestrator, directs four other agents"* — WebSurfer, FileSurfer, Coder, and ComputerTerminal — with the Orchestrator planning, tracking progress, and re-planning to recover from errors. It *"achieves statistically competitive performance to the state-of-the-art"* on the GAIA, AssistantBench, and WebArena benchmarks, and is implemented on AutoGen.

**Workflow agents vs. dynamic routing (Google).** Google ADK operationalizes the workflow-vs-agent split as code primitives: `SequentialAgent`, `ParallelAgent`, and `LoopAgent` for deterministic pipelines, versus `LlmAgent`-driven transfer for adaptive routing. The A2A protocol (announced Apr 9, 2025; donated to the Linux Foundation June 2025) is the horizontal agent-to-agent interoperability layer that complements Anthropic's MCP (the vertical agent-to-tool layer). DeepMind's Co-Scientist is Google's flagship research-grade multi-agent system, using a supervisor "freeform planner" plus a tournament-of-ideas ranking mechanism.

**Emerging 2025–2026 patterns (ThoughtWorks + Fowler).** ThoughtWorks Technology Radar now distinguishes deliberate **agent teams** (small, hand-composed) from **coding agent swarms** (dozens-to-hundreds of dynamically sized agents, e.g., Gas Town, Ruflo/Claude Flow), with emerging swarm sub-patterns: hierarchical role separation (orchestrators/supervisors/ephemeral workers), a durable work ledger, and a merge/conflict mechanism. Martin Fowler's Exploring Gen AI series has shifted from "context engineering" toward "**harness engineering**" (everything in the agent minus the model) and the "why loop / how loop / on-the-loop" model of human-agent collaboration.

---

## Tools

Frameworks and runtimes for orchestrating multi-agent and multi-step LLM workflows, split by layer: **application frameworks** (which orchestration patterns are first-class, and how they pass state/context between agents and steps) and the **durable-execution substrate** (the reliability layer beneath agent loops — journal-and-replay so a crash resumes instead of restarting). Two focused research passes (June 2026) anchored each entry to primary/official docs where possible; vendor-blog-only entries are marked.

### Application frameworks
- [OpenAI Agents SDK — Agent orchestration](other/openai-agents-sdk-agent-orchestration.md) — MIT, official; successor to Swarm. Splits **LLM-driven vs code-driven** orchestration; two multi-agent patterns — **handoffs** (delegated ownership) and **agents-as-tools** (a manager keeps control via `Agent.as_tool()`).
- [Microsoft Agent Framework — overview](microsoft-github/microsoft-agent-framework-overview.md) · [workflow orchestrations](microsoft-github/workflow-orchestrations-in-agent-framework.md) · [GitHub](microsoft-github/microsoft-agent-framework.md) — open-source (MIT), the unification of **Semantic Kernel + AutoGen**; five named orchestrations: sequential, concurrent, group chat, handoff, magentic.
- [langgraph-supervisor](other/langgraph-supervisor-hierarchical-multi-agent-systems-on-langgraph.md) — LangChain (MIT); the **supervisor/hierarchical** topology over LangGraph; an `output_mode` parameter (`full_history` vs `last_message`) controls how much worker context flows back.
- [Google ADK — multi-agent patterns](../context-engineering/google/developer-s-guide-to-multi-agent-patterns-in-adk.md) · [orchestration primitives](google/adk-custom-agents-orchestration-primitives.md) — `SequentialAgent` / `ParallelAgent` / `LoopAgent` deterministic templates plus `LlmAgent`-driven routing.
- [OpenAI Swarm](other/openai-swarm-lightweight-multi-agent-orchestration-educational.md) — the experimental, *educational* predecessor to the Agents SDK; included for lineage. *(Not for production.)*

### Durable-execution substrate
- [Temporal](other/temporal-durable-execution-for-ai-agents.md) — records an Event History and replays it to resume after a crash; holds workflow state for years without hand-built state machines; configurable retries.
- [Restate](other/restate-durable-execution-for-agentic-loops.md) — explicitly **framework-agnostic** journal-and-replay; runnable examples across the OpenAI Agents SDK, Vercel AI SDK, Google ADK, Pydantic AI, and LangChain.
- [Dapr Agents](other/dapr-agents-durable-agentic-workflows.md) — CNCF, Apache-2.0; a `DurableAgent` over Dapr Workflows supporting the full pattern set, with three pluggable memory backends.
- [Inngest](other/inngest-durable-execution-for-ai-agents.md) — step-level checkpointing + automatic retries as the substrate for reliable agent loops. *(Vendor blog — backfill, not separately verified.)*

### Agent memory & long-term state

The persistence layer beneath the frameworks — where agent state and facts live *across* context windows (distinct from in-context working memory, which the [context-engineering topic](../context-engineering/index.md) covers via compaction and note-taking). Three architecturally distinct systems dominate; choose by **how memory is managed**, not by benchmark scores (see caveat). A focused research pass (June 2026) anchored each to its primary paper/repo.

- [MemGPT / Letta](other/memgpt-letta-llm-operating-system-agent-memory.md) — **OS-inspired, agent-managed** tiers: core (in-context "RAM"), recall (searchable history), archival (external vector store, "disk"), self-managed via tool calls. The canonical stateful-agent runtime.
- [Zep / Graphiti](other/zep-graphiti-temporal-knowledge-graph-agent-memory.md) — **temporal knowledge graph** (open-source Graphiti engine): facts carry bi-temporal validity windows and are *invalidated, not deleted*, enabling point-in-time queries. Best fit when memory needs temporal reasoning + provenance.
- [Mem0](other/mem0-scalable-long-term-memory-for-ai-agents.md) — **vector-first / hybrid** (vector + graph + key-value): an LLM extracts atomic facts on write; multi-signal retrieval (semantic + BM25 + entity linking). The lowest-friction drop-in layer; tradeoff is an LLM call per write.
- [ConvoMem — "your first 150 conversations don't need RAG"](other/convomem-first-150-conversations-dont-need-rag.md) — the **vendor-independent** counterweight (Salesforce): under ~150 interactions, full context beats all RAG-style memory; adopt a memory system only at scales where full context is infeasible.

> **Evidence caveat (read before choosing).** The *architecture* descriptions above are high-confidence (primary papers + repos, unanimous in verification). The *benchmark* numbers are not: each vendor publishes self-favorable results on self-selected baselines, and the cross-vendor LoCoMo comparison is an open dispute — Zep's original ~84% was alleged by Mem0 to be inflated to a corrected 58.44%, and Zep rebuts with ~75% citing a misconfigured integration ([getzep/zep-papers#5](https://github.com/getzep/zep-papers/issues/5)). Ten head-to-head superiority claims were *refuted* during 3-vote verification. Treat all single-vendor accuracy/latency/token figures as directional marketing data, and default to ConvoMem's framing: agent memory is a **scale-triggered** layer, not a default.

### Managed agent runtimes

The deployment target — *where the orchestration actually runs in production* — distinct from the self-hosted [durable-execution substrate](#durable-execution-substrate) above (a managed runtime may bundle its own durability, scaling, identity, and observability). All three hyperscaler runtimes are now **framework-agnostic and model-agnostic** (bring LangGraph / CrewAI / ADK / OpenAI Agents SDK + any LLM); they differ on isolation, state model, pricing, and IaC. From a June 2026 research pass (24 claims verified unanimous 3-0 against primary vendor docs; benchmark-free, so high-confidence).

- [AWS Bedrock AgentCore](other/aws-bedrock-agentcore-managed-agent-runtime.md) — serverless; **dedicated Firecracker microVM per session** (the strongest isolation story), long-running up to 8h, **modular composable services** (Runtime / Memory / Gateway / Identity / Observability, used à la carte). Per-second consumption pricing ($0.0895/vCPU-hr). GA Oct 2025.
- [Google Vertex AI Agent Engine](google/build-and-manage-multi-system-agents-with-vertex-ai.md) — fully managed runtime (infra / scaling / security / eval / monitoring); ADK-native; the only one of the three with a **free tier** (50 vCPU-hrs + 100 GB-hrs/mo; idle agents not billed). Now part of the Gemini Enterprise Agent Platform.
- [Azure AI Foundry Agent Service](microsoft-github/azure-foundry-agent-service-managed-runtime.md) — **agents as named, versioned, server-side orchestration assets** (agents / conversations / responses); the most **IaC-native** (`azd` / Bicep deploy); hosted-agent containers billed by compute/hour. GA 2025 (Memory still Preview as of Q1 2026).

> **Pick by:** isolation model (AWS microVM-per-session) · agent-as-versioned-asset + IaC (Azure `azd`/Bicep) · cost floor + ADK fit (Google free tier). All three avoid framework/model lock-in. *Caveat: pricing and preview-vs-GA status move fast — verify at adoption.*

### Model gateway & cost routing

The control plane an orchestration calls *through* to reach model providers — one OpenAI-compatible interface across many models, plus retries, fallbacks, load balancing, and caching. The direct lever on the topic's central cost concern (agents burn ~4× and multi-agent ~15× the tokens of a chat). The choice is **self-host vs. hosted-percentage vs. managed-by-logs**. From a June 2026 research pass (8 findings verified unanimous 3-0 against primary vendor docs).

- [LiteLLM](other/litellm-self-hosted-llm-gateway.md) — **self-hosted, MIT**, Python SDK or proxy; 100+ providers, **zero token markup**, built-in load balancing + automatic fallbacks; paid Enterprise tier for SSO/RBAC. You pay only infra.
- [OpenRouter](other/openrouter-hosted-llm-marketplace-gateway.md) — **hosted-only marketplace**; one key to 300–400+ models, no per-token markup but a **5.5% credit-purchase fee** (5% crypto/BYOK; first 1M BYOK req/mo free). Zero ops, fee scales linearly with spend.
- [Portkey](other/portkey-managed-llm-gateway-semantic-cache.md) — **managed** (free Developer + $49/mo Production), 1,600+ LLMs; differentiator is **semantic caching**. Gateway now open-sourced (MIT); acquired by Palo Alto Networks.

> **Cost crossover:** OpenRouter's 5.5% scales linearly with spend; a self-hosted LiteLLM at ~$200/mo infra **breaks even around ~$3,600/mo of model spend** and is cheaper above that. *Refuted in verification (don't repeat): per-request latency benchmarks, semantic-caching exclusivity, and an outdated "5% markup" framing for OpenRouter.*

### Fleet lifecycle, swarms & registries

Operating *many* agents over time (provisioning, scaling, discovery, health) — distinct from building one orchestration. From a June 2026 research pass (24 claims verified; architecture/lifecycle findings high-confidence, swarm-resilience medium).

- **Topology & coordination (the academic taxonomy under "swarm"):** [Beyond Self-Talk survey](other/beyond-self-talk-multi-agent-communication-survey.md) — five architectures (flat / hierarchical / team / society / hybrid), three turn-taking strategies, and **static-vs-dynamic membership** (runtime activate/deactivate of agents).
- **Scaling & resilience:** [SWARM+](other/swarm-plus-hierarchical-agent-fleet-scaling.md) — flat all-to-all communication is **O(n²)** and doesn't scale; the answer is a configurable-depth **hierarchical tree** with a shared workload pool, plus health-check/heartbeat failure detection, adaptive quorum, and coordinator failover. *(Single preprint; resilience split-voted.)*
- **Production lifecycle primitives:** [Kubernetes Agent Sandbox](other/kubernetes-agent-sandbox-fleet-lifecycle.md) — warm pools (no cold start), **scale-to-zero-with-resume**, stable network identity (self-hostable, four CRDs).
- **Registry & discovery:** [AWS AgentCore Agent Registry](other/aws-agentcore-agent-registry.md) (governed publish/review/approve catalog, Preview) — the *platform* form; complemented by the *protocol* form (A2A [Agent Cards](google/agent2agent-protocol-specification.md)) and the *infrastructure* form (Agent Sandbox stable identity).

> **The binding constraint is coordination, not agent count.** Rigorous evaluation ([MAST / Why Multi-Agent Systems Fail](other/why-do-multi-agent-llm-systems-fail.md)) finds SOTA multi-agent systems fail **41–86.7%** of the time with often-minimal gains over a single agent, and **~79% of failures are themselves orchestration/coordination defects** (step repetition, missed termination, reasoning–action mismatch). Scaling to a swarm multiplies this — invest in orchestration mechanism design (and the reliability levers above) before agent count. *(Refuted: that a serverless runtime "fully removes infrastructure management" for the fleet.)*

## Patterns & Techniques

Named orchestration patterns, graded by how well they are evidenced. The first three groups rest on **authoritative / vendor-primary** sources; the final group is the contrarian / failure-mode literature that says when *not* to orchestrate. (Anthropic's "Building Effective Agents," listed under Anthropic above, is the canonical taxonomy underpinning this whole section.)

### Composition patterns & the core distinction (authoritative)
- [Building Effective Agents](anthropic/building-effective-agents.md) — the foundational **workflows vs agents** distinction and the five composable patterns: prompt chaining, routing, parallelization (sectioning/voting), orchestrator-workers, evaluator-optimizer.
- [AI Agent Orchestration Patterns (Azure Architecture Center)](microsoft-github/ai-agent-orchestration-patterns-azure-architecture-center-the-canonical-named.md) · [Semantic Kernel orchestration](microsoft-github/semantic-kernel-agent-orchestration.md) — the cleanest vendor-neutral naming: sequential, concurrent, group chat, handoff, magentic.

### Multi-agent topologies (authoritative)
- [How we built our multi-agent research system](anthropic/how-we-built-our-multi-agent-research-system.md) — the canonical **orchestrator-worker** production case study (lead agent + parallel subagents, each with its own context window).
- [Magentic-One](microsoft-github/magentic-one-a-generalist-multi-agent-system.md) — a reference orchestrator/planner directing specialized worker agents.
- [OpenAI Agents SDK — Handoffs](other/openai-agents-sdk-handoffs.md) — **agent handoffs** as a first-class, model-directed routing tool (`transfer_to_<agent>`); by default transfers the entire prior history — a named context-pollution lever.
- [Managed Agents](anthropic/managed-agents.md) — an infrastructure-level pattern that decouples the agent's **brain / hands / session** so each layer can fail and recover independently.

### Context engineering within orchestration (authoritative)
- [Effective context engineering for AI agents](../context-engineering/anthropic/effective-context-engineering-for-ai-agents.md) — per-subagent **context isolation** (subagents return ~1–2K-token summaries), **compaction**, and **structured note-taking** as the three long-horizon levers.
- [Effective harnesses for long-running agents](../context-engineering/anthropic/effective-harnesses-for-long-running-agents.md) — **durable/resumable execution** via git commits + a progress file to survive fresh context windows; compaction alone is shown to be insufficient.

### Agentic loops (fresh-context / the Ralph Loop)

The single-agent **harness loop** — restart a fresh agent each iteration and carry state in the filesystem/git rather than the conversation — as the unattended-work counterpart to multi-agent orchestration. From a June 2026 research pass (core mechanism high-confidence; the 2026 "loop engineering" framing is newer/medium).

- [The Ralph Loop](other/ralph-loop-fresh-context-agentic-coding.md) — Geoffrey Huntley's named pattern (Jul 2025): a bash loop restarts a fresh agent each iteration (clean context window), one task per loop, **git + `progress.txt` + `prd.json` as memory**, agent-agnostic. The externally-originated mirror of Anthropic's [long-running-agent harness](../context-engineering/anthropic/effective-harnesses-for-long-running-agents.md) — both choose *fresh context over compaction* for long-horizon work.
- [Loop engineering](other/loop-engineering-paradigm.md) — the generalization ("write loops, not prompts"): the unit of work becomes the loop (external context + dispatch + completion-check), plus the **maker-checker `/goal`** pattern (a separate, faster model verifies completion, since a model grading its own work over-reports success).

> **Two misconceptions to avoid (refuted in verification):** Ralph is **not** an error-feedback loop that feeds its own output back to converge — it *discards* context each iteration; and it is **not** a fixed five-phase plan/implement/test/verify/PR pipeline (that's one tutorial's framing). Known limit: it struggles with tightly-coupled multi-file changes and deep codebases.

### When NOT to use / failure modes
- [Don't Build Multi-Agents](other/don-t-build-multi-agents.md) — Cognition's (Devin) case for single-threaded, continuous-context agents over multi-agent topologies for coding work. *(Vendor blog / opinion — backfill.)*
- [Why Do Multi-Agent LLM Systems Fail?](other/why-do-multi-agent-llm-systems-fail.md) — the empirical **MAST** failure taxonomy (14 failure modes in 3 categories). *(arXiv preprint — backfill; early research.)*

---

## Recommendations
1. **Read first for vocabulary (1 hour):** Anthropic "Building Effective Agents" → "How we built our multi-agent research system." These give you the workflow/agent distinction and the orchestrator-worker + planner/sub-agent patterns that every other source builds on.
2. **For pattern naming and architecture decisions:** Microsoft's Azure Architecture Center "AI Agent Orchestration Patterns" is the cleanest vendor-neutral taxonomy (sequential/concurrent/group-chat/handoff/magentic); pair it with the Magentic-One paper for a concrete planner/executor implementation.
3. **For implementation, pick one framework family early:** Microsoft Agent Framework (the AutoGen + Semantic Kernel successor; graph workflows + the five orchestration modes) **or** Google ADK (workflow agents + A2A + Agent Engine runtime). Both now expose deterministic "workflow" control alongside LLM-driven orchestration — choose based on your cloud and language (.NET/Python vs. Python/Java).
4. **For ongoing trend tracking:** Subscribe to ThoughtWorks Technology Radar (Techniques quadrant) and Martin Fowler's Exploring Gen AI index — these are the two best signals for newly named patterns (swarms, harness engineering, the Ralph Loop) before they hit vendor docs.
5. **Benchmarks that should change your approach:** If a task isn't high-value enough to justify ~15× chat token cost, Anthropic's own guidance says don't go multi-agent — use a single agent or a deterministic workflow. Move from a single agent to orchestrator-worker only when work exceeds one context window, benefits from parallelism, or spans many complex tools.

---

## Caveats
- **URL verification:** Several Martin Fowler paths (harness-engineering.html, the harness-engineering-memo.html, pushing-ai-autonomy.html, context-engineering-coding-agents.html) were confirmed via a dedicated research subagent. The suggested path `exploring-gen-ai/harness-engineering.html` does *not* exist — use the two correct URLs listed above. If any path shifts, navigate from the Exploring Gen AI index.
- **Marketing vs. engineering:** Vendor blog posts blend promotion with substance. The most technically reliable links are the `*.anthropic.com/engineering/*`, `learn.microsoft.com`, `google.github.io/adk-docs`, and arXiv URLs; Vertex/Gemini and some Cloud-blog posts lean promotional.
- **A2A adoption is contested:** Multiple independent commentators note MCP gained far more grassroots developer traction than Google's A2A, despite 150+ enterprise A2A supporters; treat A2A as enterprise-oriented and still maturing rather than a settled standard.
- **Recency:** Microsoft Agent Framework was in public preview as of late 2025 with a stated Q1 2026 GA target; AutoGen is in maintenance mode and Semantic Kernel/AutoGen are being consolidated into Agent Framework — verify current GA status before committing.
- **Third-party mirrors excluded:** This list prioritizes primary sources from the five requested organizations; secondary explainers (Medium, ByteByteGo, Simon Willison's analysis of the Anthropic post at simonwillison.net/2025/Jun/14/multi-agent-research-system/) are useful summaries but are not authoritative primary documents.
