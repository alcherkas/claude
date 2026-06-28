---
title: Spec-Driven, Constraints-Driven & Test-Driven Development
---

# Spec-Driven, Constraints-Driven & Test-Driven Development


!!! tip "Start here"
    📐 **[Reference architecture — an end-to-end spec-driven & test-driven system](reference-architecture.md)** — the assembled spec→code pipeline (specify → constrain → plan → implement → verify), gated by a rigor decision and closed by review + spec-adherence evaluation, how the canonical tools (Spec Kit, Kiro, Antigravity, Claude Code) each implement a subset, and the two seams (the unnamed constraint layer; spec-as-source rarely realized).


## TL;DR
- The richest, most credible recent material clusters around **spec-driven development (SDD)** — GitHub Spec Kit, Amazon Kiro, Google Conductor/Antigravity — and **TDD adapted for agents**, with Anthropic, GitHub/Microsoft, Google, ThoughtWorks, and Martin Fowler all publishing primary-source guidance in 2025–2026.
- **"Constraints-driven development"** is the thinnest category: it is not a term any of the five priority sources uses as a formal methodology; the closest credible material is third-party (GrayBeam's "Constraint-Driven Development") plus academic arXiv work, while the priority sources address constraints under "spec-anchored," "steering files," "guardrails," and "harness engineering."
- Below is an organized, link-first deliverable grouped by source, then by topic, with titles, URLs, and publication dates where available.

!!! tip "In-depth report"
    [**OpenSpec with Claude Code: Best Practices & Efficiency Tips**](openspec-best-practices-claude-code.md) — a deep-research deliverable on the OpenSpec spec-driven workflow (`/opsx:*` commands, delta specs, the `proposal → specs → design → tasks → archive` cycle) and how to run it efficiently inside Claude Code.

## Key Findings
- **Anthropic** frames TDD as "the single strongest pattern for working with agentic coding tools" (verbatim from Boris Cherny's "Claude Code: Best practices for agentic coding," orig. April 2025), and publishes best-practices and harness-engineering guidance on its engineering blog and Claude Code docs. Note: the four-step TDD recipe widely quoted online (write tests → confirm they fail → commit → implement until green) traces to that earlier version; the live page now serves revised content.
- **GitHub/Microsoft** own the flagship SDD tool (Spec Kit) with a Constitution → Specify → Plan → Tasks → Implement workflow, plus GitHub Blog TDD-with-Copilot tutorials and VS Code custom-agent/chat-mode docs. The launch post was authored by Den Delimarsky (GitHub) and published September 2, 2025; the Spec Kit "constitution" defines nine articles (e.g., Article I requires every feature to begin as a standalone, reusable library).
- **Google** has shipped multiple spec/context-driven artifacts: Gemini CLI custom commands, the Conductor extension ("context-driven development"), and the Antigravity agentic platform.
- **ThoughtWorks** Technology Radar Vol. 34 (April 2026) places spec-driven development in the **"Assess"** ring (defined as "an emerging approach to AI-assisted coding workflows… that begin with a structured functional specification, then proceed through multiple steps to break it down"), names the blip "feedback sensors for coding agents" (deterministic quality gates — compilers, linters, type checkers and test suites integrated into agent workflows), and warns explicitly of "semantic diffusion" around terms like spec-driven development and harness engineering. Its Insights blog has deep TDD/agent material (Casper, review gates).
- **Martin Fowler's** site hosts the canonical "Exploring Gen AI" series, including Birgitta Böckeler's (Distinguished Engineer, ThoughtWorks) spec-first → spec-anchored → spec-as-source taxonomy (Oct 15, 2025) — now the most-cited working vocabulary in the field — and the "Reducing Friction in AI-Assisted Development" pattern series.

## Details

### 1. ANTHROPIC

**Test-driven development & agentic coding best practices**
- [Best practices for Claude Code](anthropic/best-practices-for-claude-code.md) — engineering blog; orig. "Claude Code: Best practices for agentic coding," Boris Cherny, April 18, 2025; page since revised
- [Best practices](anthropic/best-practices.md) — current canonical Claude Code docs
- [Common workflows](anthropic/common-workflows.md) — Claude Code docs; includes working-with-tests guidance
- [Claude Code overview](../context-engineering/anthropic/claude-code-overview-docs.md) — docs
- [Effective harnesses for long-running agents](../context-engineering/anthropic/effective-harnesses-for-long-running-agents.md) — Justin Young, Nov 26, 2025
- [Harness design for long-running application development](anthropic/harness-design-for-long-running-application-development.md) — engineering, ~2026
- [Building agents with the Claude Agent SDK](../orchestration/anthropic/building-agents-with-the-claude-agent-sdk.md) — Thariq Shihipar, Sep 29, 2025
- [Engineering blog index](../context-engineering/anthropic/anthropic-engineering.md)
- [How Anthropic teams use Claude Code](anthropic/how-anthropic-teams-use-claude-code-1276b6.md) — blog
- [How Anthropic teams use Claude Code](../context-engineering/anthropic/how-anthropic-teams-use-claude-code.md) — PDF
- [Scaling Agentic Coding Across Your Organization](anthropic/scaling-agentic-coding-across-your-organization.md) — PDF, Aug 2025
- [Claude Code product page](anthropic/claude-code-product-page.md)
- [Claude Agent SDK overview](anthropic/claude-agent-sdk-overview.md) — docs

### 2. GITHUB / MICROSOFT

**Spec-driven development (Spec Kit)**
- [Spec-driven development with AI: get started with a new open source toolkit](microsoft-github/spec-driven-development-with-ai-get-started-with-a-new-open-source-toolkit.md) — GitHub Blog; Den Delimarsky, Sep 2, 2025
- [GitHub Spec Kit repository](microsoft-github/github-spec-kit-repository.md)
- [Spec Kit documentation site](microsoft-github/spec-kit-documentation-site.md)
- [GitHub Spec Kit - spec-driven.md](microsoft-github/spec-kit-spec-driven-md.md) — primary repository document for SDD philosophy and workflow
- [GitHub Spec Kit - spec-template.md](microsoft-github/spec-kit-spec-template.md) — feature-spec template for scenarios, tests, requirements, and success criteria
- [Spec Kit AGENTS.md](microsoft-github/spec-kit-agents-md.md) — workflow conventions
- [Diving Into Spec-Driven Development With GitHub Spec Kit](microsoft-github/diving-into-spec-driven-development-with-github-spec-kit.md) — Microsoft for Developers blog
- [VS Code Live: Where does spec-kit fit now?](microsoft-github/vs-code-live-where-does-spec-kit-fit-now.md) — Spec Kit discussion #1175

**TDD with GitHub Copilot**
- [GitHub for Beginners: Test-driven development (TDD) with GitHub Copilot](microsoft-github/github-for-beginners-test-driven-development-tdd-with-github-copilot.md) — GitHub Blog
- [Context windows, Plan agent, and TDD: building a countdown app with GitHub Copilot](microsoft-github/context-windows-plan-agent-and-tdd-building-a-countdown-app-with-github-copilot.md) — GitHub Blog
- [Agent-driven development in Copilot Applied Science](microsoft-github/agent-driven-development-in-copilot-applied-science.md) — GitHub Blog
- [Accelerate test-driven development with AI](microsoft-github/accelerate-test-driven-development-with-ai.md) — GitHub Readme Guides

**Copilot agent mode, coding agent & customization**
- [Agent mode 101: All about GitHub Copilot's powerful mode](microsoft-github/agent-mode-101-all-about-github-copilot-s-powerful-mode.md) — GitHub Blog
- [GitHub Copilot: Meet the new coding agent](microsoft-github/github-copilot-meet-the-new-coding-agent.md) — GitHub Blog
- [Vibe coding with GitHub Copilot: Agent mode and MCP support](microsoft-github/vibe-coding-with-github-copilot-agent-mode-and-mcp-support.md) — GitHub Blog
- [GitHub Copilot app: the agent-native desktop experience](microsoft-github/github-copilot-app-the-agent-native-desktop-experience.md) — GitHub Blog, Build 2026
- [Custom agents in VS Code](microsoft-github/custom-agents-in-vs-code.md) — VS Code docs
- [Custom chat modes](microsoft-github/custom-chat-modes.md) — vscode-docs source

### 3. GOOGLE

**Spec-driven / context-driven development**
- [Conductor: Introducing context-driven development for Gemini CLI](../context-engineering/google/conductor-introducing-context-driven-development-for-gemini-cli.md) — Google Developers Blog
- [Spec-Driven Development with Gemini CLI](google/spec-driven-development-with-gemini-cli.md) — Giovanni Galloro, Google Cloud Community
- [How Google Antigravity is changing spec-driven development](google/how-google-antigravity-is-changing-spec-driven-development.md) — Giovanni Galloro, Google Cloud Community
- [Using Spec Kit with Gemini CLI](google/using-spec-kit-with-gemini-cli.md) — gemini-cli discussion #7905

**Gemini CLI / Antigravity platform**
- [Google announces Gemini CLI: your open-source AI agent](../context-engineering/google/google-announces-gemini-cli-your-open-source-ai-agent.md) — blog.google
- [Build with Google Antigravity, our new agentic development platform](google/build-with-google-antigravity-our-new-agentic-development-platform.md) — Google Developers Blog, Nov 2025
- [I/O 2026 developer highlights: Antigravity, Gemini API, AI Studio](google/i-o-2026-developer-highlights-antigravity-gemini-api-ai-studio.md) — blog.google
- [An important update: Transitioning Gemini CLI to Antigravity CLI](google/an-important-update-transitioning-gemini-cli-to-antigravity-cli.md) — Google Developers Blog
- [Browse Gemini CLI Extensions](google/browse-gemini-cli-extensions.md) — incl. Conductor, spec-driven skills

### 4. THOUGHTWORKS

**Technology Radar (Vol. 34, April 2026)**
- [Spec-driven development](thoughtworks/spec-driven-development.md) — Radar technique blip — "Assess" ring
- [Technology Radar landing page](../security-governance/thoughtworks/technology-radar.md)
- [Tools quadrant](thoughtworks/tools-quadrant.md) — OpenSpec, Spec-Kit, feedback sensors
- [Technology Radar Vol. 34 PDF](../context-engineering/thoughtworks/technology-radar-vol-34.md)

**Insights blog — TDD & agentic workflows**
- [The hidden pearls of TDD](thoughtworks/the-hidden-pearls-of-tdd.md)
- [Why TDD and pair programming are perfect companions for GitHub Copilot](thoughtworks/why-tdd-and-pair-programming-are-perfect-companions-for-github-copilot.md)
- [Casper: Helping developers work effectively with coding assistants](thoughtworks/casper-helping-developers-work-effectively-with-coding-assistants.md)
- [How to implement effective review gates for AI-assisted development](thoughtworks/how-to-implement-effective-review-gates-for-ai-assisted-development.md)
- [How much faster can coding assistants really make software delivery?](thoughtworks/how-much-faster-can-coding-assistants-really-make-software-delivery.md)
- [How to cultivate trust with AI coding assistants](thoughtworks/how-to-cultivate-trust-with-ai-coding-assistants.md)
- [AI-assisted coding: Experiences and perspectives](thoughtworks/ai-assisted-coding-experiences-and-perspectives.md) — podcast

### 5. MARTIN FOWLER (martinfowler.com)

**Exploring Gen AI series**
- [Exploring Generative AI](../context-engineering/martin-fowler/exploring-generative-ai.md) — series index
- [Understanding Spec-Driven-Development: Kiro, spec-kit, and Tessl](martin-fowler/understanding-spec-driven-development-kiro-spec-kit-and-tessl.md) — Birgitta Böckeler, Oct 15, 2025 — canonical spec-first/spec-anchored/spec-as-source taxonomy
- [Humans and Agents in Software Engineering Loops](../context-engineering/martin-fowler/humans-and-agents-in-software-engineering-loops.md) — Kief Morris, Mar 4, 2026

**Reducing Friction in AI-Assisted Development (Rahul Gadkari series)**
- [Patterns for Reducing Friction in AI-Assisted Development](martin-fowler/patterns-for-reducing-friction-in-ai-assisted-development.md) — overview
- [Design-First Collaboration](martin-fowler/design-first-collaboration.md) — "whiteboard before keyboard"
- [Knowledge Priming](martin-fowler/knowledge-priming.md)
- [Encoding Team Standards](martin-fowler/encoding-team-standards.md)

**Other relevant Fowler articles & fragments**
- [Structured-Prompt-Driven Development](martin-fowler/structured-prompt-driven-development.md) — SPDD
- [Fragments: January 8 (2026) — on TDD with Claude Code & spec critiques](martin-fowler/fragments-january-8-2026-on-tdd-with-claude-code-spec-critiques.md)
- [Fragments: April 29 (2026) — verification-centric AI coding](../context-engineering/martin-fowler/fragments-april-29.md)
- [Generative AI tag index](martin-fowler/generative-ai-tag-index.md)

### 6. CONSTRAINTS-DRIVEN DEVELOPMENT & SUPPORTING ACADEMIC / THIRD-PARTY MATERIAL
- [Constraint-Driven Development: A New Paradigm for AI-Assisted Software Engineering](other/constraint-driven-development-a-new-paradigm-for-ai-assisted-software.md) — GrayBeam Engineering / Claude
- [Spec-Driven Development: From Code to Contract in the Age of AI Coding Assistants](other/spec-driven-development-from-code-to-contract-in-the-age-of-ai-coding-assistants.md) — arXiv
- [Constraint decay: The Fragility of LLM Agents in Backend Code Generation](other/constraint-decay-the-fragility-of-llm-agents-in-backend-code-generation.md) — arXiv
- [Constitutional Spec-Driven Development: Enforcing Security by Construction in AI-Assisted Code Generation](https://arxiv.org/abs/2602.02584) — arXiv; embeds non-negotiable security principles into the specification layer before code generation.
- [Spec Kit Agents: Context-Grounded Agentic Workflows](https://arxiv.org/abs/2604.05278) — arXiv; connects Spec Kit-style phases with repository grounding and validation hooks.
- [From Prompt to Process: a Process Taxonomy and Comparative Assessment of Frameworks Supporting AI Software Development Agents](https://arxiv.org/abs/2606.04967) — arXiv; compares Spec Kit, OpenSpec, BMAD, GSD, Spec Kitty, and Reversa as process frameworks for AI development agents.
- [BaxBench: Can LLMs Generate Correct and Secure Backends?](https://arxiv.org/abs/2502.11844) — arXiv; closest backend-generation benchmark to "Constraint Decay," focused on correct and secure backend services.
- [NL2Repo-Bench: Towards Long-Horizon Repository Generation Evaluation of Coding Agents](https://arxiv.org/abs/2512.12730) — arXiv; evaluates repo generation from natural-language requirements, relevant to spec-to-code coherence.
- [FeatBench: Towards More Realistic Evaluation of Feature-level Code Generation](https://arxiv.org/abs/2509.22237) — arXiv; studies feature implementation from natural-language requirements, including scope creep and regressions.
- [SWE-Bench Pro: Can AI Agents Solve Long-Horizon Software Engineering Tasks?](https://arxiv.org/abs/2509.16941) — arXiv; long-horizon SWE-agent benchmark with enterprise-style task complexity and failure analysis.
- [Instruction-Following Evaluation for Large Language Models](https://arxiv.org/abs/2311.07911) — arXiv; introduces verifiable instruction constraints, useful as a model for checking spec adherence.
- [FollowBench: A Multi-level Fine-grained Constraints Following Benchmark for Large Language Models](https://arxiv.org/abs/2310.20410) — arXiv; incrementally adds constraints, a direct analogue for measuring constraint accumulation.
- [InFoBench: Evaluating Instruction Following Ability in Large Language Models](https://arxiv.org/abs/2401.03601) — arXiv; decomposes instructions into requirements and measures per-requirement following.
- [ComplexBench: Benchmarking Complex Instruction-Following with Multiple Constraints Composition](https://arxiv.org/abs/2407.03978) — arXiv; evaluates composed constraints, relevant to specs that combine API contracts, architecture, data, security, and test gates.
- [Beyond Vibe Coding: Amazon Introduces Kiro, the Spec-Driven Agentic AI IDE](other/beyond-vibe-coding-amazon-introduces-kiro-the-spec-driven-agentic-ai-ide.md) — InfoQ, Aug 2025
- [Kiro Documentation](other/kiro-documentation.md) — AWS
- [Kiro Docs - Specs](other/kiro-specs.md) — requirements/design/tasks workflow for spec-driven agentic development
- [Kiro Docs - Steering](other/kiro-steering.md) — persistent product, tech-stack, and structure context for agents

## Tools
Three groups of tooling for spec-driven, test-driven, and constraints-driven workflows — **spec-driven toolkits** (turn a written spec into staged, reviewable implementation), **TDD / feedback-sensor harnesses** (wire deterministic gates — test runners, coverage, compilers — into the agent loop), and **constraint-enforcement tooling** (constitution/steering files plus runtime guardrail/policy engines). A focused research pass (June 2026; 24 sources → 24 verified claims, 1 refuted) grades each entry; weakly-evidenced or uncovered tools are collected under *Gaps* at the end.

### Spec-driven toolkits
- [GitHub Spec Kit](microsoft-github/github-spec-kit-repository.md) — **vendor-primary** (GitHub; MIT): Specify → Plan → Tasks → Implement, "specifications become executable," 29+ agent integrations. *(high, 3-0)*
- [OpenSpec](other/openspec-spec-driven-development-for-ai-coding-assistants.md) — emerging, org-published (Fission-AI; MIT): lightweight SDD via `/opsx:*` commands. *(high, 3-0)*
- [Amazon Kiro](other/kiro-documentation.md) — agentic SDD IDE; Requirements → Design → Tasks ([Specs](other/kiro-specs.md), [Steering](other/kiro-steering.md)). *(steering verified high, 3-0; rated spec-first only)*
- [Google Conductor](../context-engineering/google/conductor-introducing-context-driven-development-for-gemini-cli.md) — Gemini CLI extension (Apache-2.0) for "context-driven development"; persistent `plan.md` specs. *(high, 3-0)*
- [Google Antigravity](google/build-with-google-antigravity-our-new-agentic-development-platform.md) — agent-first IDE/platform on Gemini 3; verification-centric "Artifacts" rather than a native spec/TDD format. *(high, 3-0)*

### TDD & feedback-sensor harnesses
- [Qodo-Cover](other/qodo-cover-ai-test-generation-and-coverage-agent.md) — test-runner + coverage-parser gates in a generate → run → validate loop (AGPL-3.0; formerly CodiumAI Cover-Agent). **Deprecated 2025-06-15.** *(high, 3-0)*
- [Large Language Models for Unit Test Generation](other/large-language-models-for-unit-test-generation-survey.md) — survey of the **generate-validate-repair** paradigm (execution feedback → self-correction). *(medium, 2-1 — single survey)*
- **TestForge** ([arXiv 2503.14713](https://arxiv.org/abs/2503.14713)) & **SocraTest** ([arXiv 2306.05152](https://arxiv.org/abs/2306.05152)) — research autonomous testing agents; TestForge wires file-edit / run-tests / read-coverage tools (84.3% pass@1, self-reported). *(high, 3-0; academic)*
- [Anthropic best practices](anthropic/best-practices.md) — the **self-closing verification loop**: "give the agent a check it can run" (test/build/lint/diff) so the agent verifies its own work. *(high, primary)*
- [Feedback sensors for coding agents](../evals-observability/thoughtworks/feedback-sensors-for-coding-agents.md) — ThoughtWorks Radar Vol. 34, **Trial** ring — the practitioner-org name for the same loop. *(high, 3-0)*

### Constraint-enforcement tooling
- [Spec Kit constitution](microsoft-github/spec-kit-spec-driven-md.md) — `/speckit.constitution` writes non-negotiable principles at `.specify/memory/constitution.md` and propagates them across templates. *(high, 3-0)*
- [Kiro steering files](other/kiro-steering.md) — persistent `product.md` / `tech.md` / `structure.md`, auto-included once created. *(high, 3-0)*
- [NVIDIA NeMo Guardrails](other/nvidia-nemo-guardrails-programmable-guardrails-for-llm-systems.md) — guardrail/policy engine; five rail categories incl. execution rails over tool calls (Apache-2.0). *(high, 3-0; general-purpose)*
- [Guardrails AI](other/guardrails-ai-validation-framework-for-llm-applications.md) — Input/Output Guards + Pydantic/RAIL schema validation (Apache-2.0). *(high, 3-0; general-purpose)*

### Gaps identified during research — Tools
Less-evidenced or uncovered; read as gaps, not recommendations.

- **Tessl** and **BMAD-METHOD** — named as spec-driven tools but produced **no verified primary-source claim** in this pass, so not cataloged above (absence = no verified evidence here, not evidence of non-existence).
- **Refuted (0-3):** the *conflated* claim "Antigravity launched 2025-11-20 + cross-platform + free for individuals" — each component is individually verified, but the bundled statement was killed.
- **Deprecations / renames to watch:** Qodo-Cover unmaintained; [Gemini CLI → Antigravity CLI](google/an-important-update-transitioning-gemini-cli-to-antigravity-cli.md) (individual Pro/Ultra/free tiers ended 2026-06-18); Spec Kit `/constitution` → `/speckit.constitution`.
- **Category caveat:** NeMo Guardrails and Guardrails AI are general LLM-guardrail engines, not coding-agent-specific — included as the closest verified constraint-enforcement tooling.

## Patterns / Techniques
Named patterns and techniques for spec-driven, test-driven, and constraints-driven development with coding agents, grouped by how well-evidenced they are. A focused research pass (June 2026; 22 sources → 25 verified claims, 0 refuted) backs the first two groups with originator or vendor-primary sources; the **Gaps** group collects terms the topic is named for that produced no verifiable primary-source support.

### Well-evidenced (authoritative / vendor-primary)
- [Spec-first / spec-anchored / spec-as-source](martin-fowler/understanding-spec-driven-development-kiro-spec-kit-and-tessl.md) — Birgitta Böckeler's three-tier taxonomy of SDD rigor (Fowler site, 2025-10-15). *Failure mode: most tools only reach spec-first; spec-as-source rarely realized.*
- [SDD "power inversion"](other/spec-driven-development-from-code-to-contract-in-the-age-of-ai-coding-assistants.md) — specs are the source of truth, code is a generated/verified artifact ("code serves specifications"). *Caveat: "executable specs" is aspirational framing; critics call it "reinvented waterfall."*
- [The "constitution" pattern](microsoft-github/spec-kit-spec-driven-md.md) — Spec Kit governance file of non-negotiable principles (nine articles) referenced across specify/plan/implement. *Caveat: "immutable" is scare-quoted — constitutions are versioned/amendable.*
- [The "steering files" pattern](other/kiro-steering.md) — Kiro's persistent project-knowledge markdown (`product.md`/`tech.md`/`structure.md`). *Caveat: not auto-created — "auto-included once created"; context-bloat risk.*
- [Spec Kit slash-command workflow](microsoft-github/spec-kit-agents-md.md) — `/specify → /plan → /tasks → implement`, preceded by `/constitution` (multi-step refinement, not one-shot). *Caveat: surface evolves fast (`/speckit.*`; added `/clarify`, `/analyze`, `/checklist`).*
- [Explore → Plan → Implement → Commit](anthropic/best-practices.md) — Anthropic's recommended Claude Code workflow separating research/planning from execution.
- [Self-closing verification loop](anthropic/best-practices.md) — "give the agent a check it can run"; the conceptual cousin of feedback sensors and the most-emphasized practice in Anthropic's docs.
- [Feedback sensors for coding agents](../evals-observability/thoughtworks/feedback-sensors-for-coding-agents.md) — ThoughtWorks Radar Vol. 34, **Trial** ring. *Caveat: Trial, not yet Adopt; "review gates" not confirmed as a separate blip.*
- [Structured-Prompt-Driven Development (SPDD)](martin-fowler/structured-prompt-driven-development.md) — Zhang & Xia (Thoughtworks) / Fowler, 2026-04-28: prompts as version-controlled artifacts + the seven-part **REASONS Canvas** (Norms ≈ steering; Safeguards ≈ constraints). *Caveat: single (originator) source.*
- **Semantic diffusion** (meta) — [Böckeler](martin-fowler/understanding-spec-driven-development-kiro-spec-kit-and-tessl.md) notes "spec-driven development… is already semantically diffused… 'spec' as a synonym for detailed prompt." Attribute every SDD entry to a specific flavor (Kiro vs Spec Kit vs Tessl).

### Emerging / academic (arXiv, single-vendor self-report)
- [SDD: From Code to Contract](other/spec-driven-development-from-code-to-contract-in-the-age-of-ai-coding-assistants.md) — single-author arXiv preprint (Piskala, 2026-01-30); academic SDD definition; **reuses Böckeler's taxonomy without attribution** (a semantic-diffusion signal). *(medium)*
- [Agentic TDD loop in practice at Anthropic](anthropic/how-anthropic-teams-use-claude-code-1276b6.md) — Security Eng guided TDD + autonomous test-iterate loops (self-reported). *Caveat: promotional; red-green-refactor implied, not verbatim-confirmed.* (See also Anthropic's "TDD is the single strongest pattern for agentic coding" framing in [best practices](anthropic/best-practices.md).)
- [Review gates for AI-assisted development](thoughtworks/how-to-implement-effective-review-gates-for-ai-assisted-development.md) — ThoughtWorks Insights; staged human/automated review checkpoints. *(not confirmed as a distinct Radar blip)*

### Gaps identified during research — Patterns
The Constraints-Driven cluster the topic is named for is the weakest: **no CDD claim survived verification.** Treat the items below as lightly-documented coinages, not established practice; the closest *verified* analogues are SPDD's **Safeguards** and Spec Kit's **constitution**.

- [Constraint-Driven Development (CDD)](other/constraint-driven-development-a-new-paradigm-for-ai-assisted-software.md) — GrayBeam/Claude coinage; **no authoritative primary source** beyond the originating post.
- [Constraint decay](other/constraint-decay-the-fragility-of-llm-agents-in-backend-code-generation.md) — arXiv framing of constraints degrading over long backend generations. *(single preprint)*
- **Constitutional / security-by-construction SDD** — [arXiv 2602.02584](https://arxiv.org/abs/2602.02584); embeds non-negotiable security principles in the spec layer before code generation. *(emerging; not independently verified)*
- **"Reducing Friction" coinages** — [Design-First Collaboration](martin-fowler/design-first-collaboration.md), [Knowledge Priming](martin-fowler/knowledge-priming.md), [Encoding Team Standards](martin-fowler/encoding-team-standards.md) ([series overview](martin-fowler/patterns-for-reducing-friction-in-ai-assisted-development.md)) — Rahul Gadkari; did not survive verification as named patterns; map loosely onto steering/constitution/SPDD-Norms.
- **"Harness engineering"** — used loosely across sources; not separately confirmed here (a semantic-diffusion flag).

## Recommendations
- **Start with the primary-source canon.** For SDD: GitHub's Spec Kit announcement + repo, Fowler/Böckeler's three-levels taxonomy, and the ThoughtWorks Radar blip. For TDD-with-agents: Anthropic's best-practices post plus ThoughtWorks's "The hidden pearls of TDD" and the Copilot/TDD article.
- **Treat "constraints-driven development" as an emerging, mostly third-party label.** If the user needs methodology from the five priority sources, point them to "spec-anchored" (Fowler/Böckeler), "steering files"/"constitution" (Kiro/Spec Kit), and "harness engineering"/"feedback sensors for coding agents" (ThoughtWorks Radar Vol. 34) as the credible equivalents.
- **Track fast-moving tooling.** Spec Kit, Kiro, Conductor, and Antigravity are all evolving monthly (e.g., Gemini CLI is being folded into Antigravity CLI; Spec Kit functions are migrating into native VS Code commands); verify version-specific claims at the source before relying on them.

## Caveats
- Several high-value items are authored by ThoughtWorks staff but hosted on Fowler's site (the "Exploring Gen AI" and "Reduce Friction" series); they are credited to guest authors (Böckeler, Morris, Gadkari, etc.), not Fowler himself.
- Some Google and Anthropic SDD/TDD write-ups are on Medium (Google Cloud Community) or claude.com rather than the core corporate blog; they are official-adjacent but author-attributed.
- Anthropic's best-practices engineering URL still resolves but now serves revised content canonicalized to code.claude.com; the explicit four-step TDD recipe widely quoted online traces to the earlier (April 2025) version of the post/docs.
- Dates are included where reliably verified; living documentation pages (Claude Code docs, VS Code docs) carry no fixed publication date.
- The arXiv papers carry recent or forward-dated identifiers consistent with fast-moving 2025–2026 submissions; treat them as recent preprints, not peer-reviewed publications unless the linked source states otherwise.
