---
title: Agentic Orchestration & Workflows
---

# Agentic Orchestration & Workflows


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
