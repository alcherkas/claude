---
title: Spec-Driven, Constraints-Driven & Test-Driven Development
---

# Spec-Driven, Constraints-Driven & Test-Driven Development


## TL;DR
- The richest, most credible recent material clusters around **spec-driven development (SDD)** — GitHub Spec Kit, Amazon Kiro, Google Conductor/Antigravity — and **TDD adapted for agents**, with Anthropic, GitHub/Microsoft, Google, ThoughtWorks, and Martin Fowler all publishing primary-source guidance in 2025–2026.
- **"Constraints-driven development"** is the thinnest category: it is not a term any of the five priority sources uses as a formal methodology; the closest credible material is third-party (GrayBeam's "Constraint-Driven Development") plus academic arXiv work, while the priority sources address constraints under "spec-anchored," "steering files," "guardrails," and "harness engineering."
- Below is an organized, link-first deliverable grouped by source, then by topic, with titles, URLs, and publication dates where available.

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
- [Beyond Vibe Coding: Amazon Introduces Kiro, the Spec-Driven Agentic AI IDE](other/beyond-vibe-coding-amazon-introduces-kiro-the-spec-driven-agentic-ai-ide.md) — InfoQ, Aug 2025
- [Kiro Documentation](other/kiro-documentation.md) — AWS

## Recommendations
- **Start with the primary-source canon.** For SDD: GitHub's Spec Kit announcement + repo, Fowler/Böckeler's three-levels taxonomy, and the ThoughtWorks Radar blip. For TDD-with-agents: Anthropic's best-practices post plus ThoughtWorks's "The hidden pearls of TDD" and the Copilot/TDD article.
- **Treat "constraints-driven development" as an emerging, mostly third-party label.** If the user needs methodology from the five priority sources, point them to "spec-anchored" (Fowler/Böckeler), "steering files"/"constitution" (Kiro/Spec Kit), and "harness engineering"/"feedback sensors for coding agents" (ThoughtWorks Radar Vol. 34) as the credible equivalents.
- **Track fast-moving tooling.** Spec Kit, Kiro, Conductor, and Antigravity are all evolving monthly (e.g., Gemini CLI is being folded into Antigravity CLI; Spec Kit functions are migrating into native VS Code commands); verify version-specific claims at the source before relying on them.

## Caveats
- Several high-value items are authored by ThoughtWorks staff but hosted on Fowler's site (the "Exploring Gen AI" and "Reduce Friction" series); they are credited to guest authors (Böckeler, Morris, Gadkari, etc.), not Fowler himself.
- Some Google and Anthropic SDD/TDD write-ups are on Medium (Google Cloud Community) or claude.com rather than the core corporate blog; they are official-adjacent but author-attributed.
- Anthropic's best-practices engineering URL still resolves but now serves revised content canonicalized to code.claude.com; the explicit four-step TDD recipe widely quoted online traces to the earlier (April 2025) version of the post/docs.
- Dates are included where reliably verified; living documentation pages (Claude Code docs, VS Code docs) carry no fixed publication date.
- The two arXiv papers (2602.00180, 2605.06445) carry forward-dated identifiers consistent with 2026 submissions; treat them as recent preprints, not peer-reviewed publications.
