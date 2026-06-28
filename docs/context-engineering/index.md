---
title: Solution-Level Context Engineering Across Multiple Repositories
---

# Solution-Level Context Engineering Across Multiple Repositories


**TL;DR**

- The strongest authoritative material comes from **Anthropic** (the foundational "Effective context engineering for AI agents" guide, which defines context engineering as "the set of strategies for curating and maintaining the optimal set of tokens during LLM inference"), **ThoughtWorks** (Technology Radar blips + Birgitta Böckeler's "Exploring Gen AI" series), and **Martin Fowler's** site; **Google** (ADK context stack) and **Microsoft/GitHub** (AGENTS.md, custom instructions, MCP) cover the agent-context tooling layer.
- True multi-repo / "solution-level" context is still an emerging, lightly-documented area: per GitHub Docs, "By default, Copilot can only access context in the repository specified when you start a task… You can, however, configure broader access through repository MCP settings." There is no native cross-repo context feature, so the dominant patterns (repo-of-repos, meta/spine repos, shared instruction repos, widened MCP token scope) come mostly from practitioner write-ups.
- Links are grouped below by the five prioritized sources, with a final section of high-value multi-repo practitioner references.

## Anthropic
- [Effective context engineering for AI agents](anthropic/effective-context-engineering-for-ai-agents.md)
- [Effective harnesses for long-running agents](anthropic/effective-harnesses-for-long-running-agents.md)
- [Writing effective tools for AI agents—using AI agents](anthropic/writing-effective-tools-for-ai-agents-using-ai-agents.md)
- [Equipping agents for the real world with Agent Skills](anthropic/equipping-agents-for-the-real-world-with-agent-skills.md)
- [Anthropic Engineering](anthropic/anthropic-engineering.md) — blog index
- [Claude Code overview / docs](anthropic/claude-code-overview-docs.md) — CLAUDE.md, memory, multi-dir support
- [Memory tool — Claude API Docs](anthropic/memory-tool-claude-api-docs.md)
- [How Anthropic teams use Claude Code](anthropic/how-anthropic-teams-use-claude-code.md) — PDF

## Google
- [Architecting efficient context-aware multi-agent framework for production](google/architecting-efficient-context-aware-multi-agent-framework-for-production.md) — ADK context stack
- [Conductor: Introducing context-driven development for Gemini CLI](google/conductor-introducing-context-driven-development-for-gemini-cli.md)
- [Building AI Agents with Google Gemini 3 and Open Source Frameworks](google/building-ai-agents-with-google-gemini-3-and-open-source-frameworks.md)
- [Developer's guide to multi-agent patterns in ADK](google/developer-s-guide-to-multi-agent-patterns-in-adk.md)
- [Set up your coding assistant with Gemini MCP and Skills](google/set-up-your-coding-assistant-with-gemini-mcp-and-skills.md)
- [Google announces Gemini CLI: your open-source AI agent](google/google-announces-gemini-cli-your-open-source-ai-agent.md)
- [Agent Development Kit (ADK) docs](google/agent-development-kit-adk-docs.md)
- [Building Collaborative AI: A Developer's Guide to Multi-Agent Systems with ADK](google/building-collaborative-ai-a-developer-s-guide-to-multi-agent-systems-with-adk.md)
- [What's New in the Agentic Data Cloud](google/what-s-new-in-the-agentic-data-cloud.md) — universal context engine / Knowledge Catalog
- [Lessons from 2025 on agents and trust](google/lessons-from-2025-on-agents-and-trust.md) — Office of the CTO
- [Gemini Code Assist for teams and businesses](google/gemini-code-assist-for-teams-and-businesses.md) — Enterprise Context

## Microsoft / GitHub
- [Want better AI outputs? Try context engineering.](microsoft-github/want-better-ai-outputs-try-context-engineering.md) — GitHub Blog
- [How to build reliable AI workflows with agentic primitives and context engineering](microsoft-github/how-to-build-reliable-ai-workflows-with-agentic-primitives-and-context.md) — GitHub Blog
- [How to write a great agents.md: Lessons from over 2,500 repositories](microsoft-github/how-to-write-a-great-agents-md-lessons-from-over-2500-repositories.md) — GitHub Blog
- [How Squad runs coordinated AI agents inside your repository](microsoft-github/how-squad-runs-coordinated-ai-agents-inside-your-repository.md) — GitHub Blog
- [About GitHub Copilot cloud agent](microsoft-github/about-github-copilot-cloud-agent.md) — single-repo default; broader access via MCP settings
- [Connect agents to external tools](microsoft-github/connect-agents-to-external-tools.md) — widen PAT scope for cross-repo access
- [Model Context Protocol (MCP) and GitHub Copilot cloud agent](microsoft-github/model-context-protocol-mcp-and-github-copilot-cloud-agent.md)
- [Adding repository custom instructions for GitHub Copilot](microsoft-github/adding-repository-custom-instructions-for-github-copilot.md)
- [Managing agent sessions](microsoft-github/managing-agent-sessions.md) — passing context between sessions
- [GitHub Copilot CLI](microsoft-github/github-copilot-cli.md) — subagents, /fleet, AGENTS.md across sessions
- [Context Engineering for Reliable AI Agents: Lessons from Building Azure SRE Agent](microsoft-github/context-engineering-for-reliable-ai-agents-lessons-from-building-azure-sre-agent.md)
- [Never Explain Context Twice: Introducing Azure SRE Agent memory](microsoft-github/never-explain-context-twice-introducing-azure-sre-agent-memory.md)
- [Efficient AI applications: context engineering and agents](microsoft-github/efficient-ai-applications-context-engineering-and-agents.md) — Microsoft Research
- [Context Engineering for AI Agents](microsoft-github/context-engineering-for-ai-agents.md) — ai-agents-for-beginners course
- [Introducing Microsoft Agent Framework](microsoft-github/introducing-microsoft-agent-framework.md)
- [Search Less, Build More: Inner Sourcing with GitHub Copilot and ADO MCP Server](microsoft-github/search-less-build-more-inner-sourcing-with-github-copilot-and-ado-mcp-server.md) — cross-repo code search
- [Set up the remote Azure DevOps MCP Server](microsoft-github/set-up-the-remote-azure-devops-mcp-server.md) — cross-repo context
- [Connect GitHub Copilot coding agent to the Azure MCP Server](microsoft-github/connect-github-copilot-coding-agent-to-the-azure-mcp-server.md)
- [AGENTS.md open format](microsoft-github/agents-md-open-format.md) — used by Jules/Gemini, Codex, Cursor; nested per-package files

## ThoughtWorks
- [Context engineering | Technology Radar](thoughtworks/context-engineering-technology-radar.md)
- [Progressive context disclosure | Technology Radar](thoughtworks/progressive-context-disclosure-technology-radar.md)
- [Curated shared instructions for software teams | Technology Radar](thoughtworks/curated-shared-instructions-for-software-teams-technology-radar.md)
- [Agent Skills | Technology Radar](thoughtworks/agent-skills-technology-radar.md)
- [Techniques | Technology Radar](thoughtworks/techniques-technology-radar.md) — vol 34 — skills as executable onboarding; anchoring to reference app
- [Context engineering: How to give AI exactly what it needs](thoughtworks/context-engineering-how-to-give-ai-exactly-what-it-needs.md)
- [From vibe coding to context engineering: 2025 in software development](thoughtworks/from-vibe-coding-to-context-engineering-2025-in-software-development.md)
- [Beyond vibe coding: The five building blocks of AI-native engineering](thoughtworks/beyond-vibe-coding-the-five-building-blocks-of-ai-native-engineering.md)
- [What we're talking about when we talk about context engineering](thoughtworks/what-we-re-talking-about-when-we-talk-about-context-engineering.md) — podcast
- [Context engineering: Tackling legacy systems with generative AI](thoughtworks/context-engineering-tackling-legacy-systems-with-generative-ai.md) — podcast
- [Technology Radar Vol 33](thoughtworks/technology-radar-vol-33.md) — PDF
- [Technology Radar Vol 34](thoughtworks/technology-radar-vol-34.md) — PDF

## Martin Fowler (martinfowler.com)
- [Context Engineering for Coding Agents](martin-fowler/context-engineering-for-coding-agents.md) — Birgitta Böckeler
- [Harness engineering for coding agent users](martin-fowler/harness-engineering-for-coding-agent-users.md)
- [Harness Engineering - first thoughts](martin-fowler/harness-engineering-first-thoughts.md)
- [Anchoring AI to a reference application](martin-fowler/anchoring-ai-to-a-reference-application.md)
- [How far can we push AI autonomy in code generation?](martin-fowler/how-far-can-we-push-ai-autonomy-in-code-generation.md)
- [Maintainability sensors for coding agents](martin-fowler/maintainability-sensors-for-coding-agents.md)
- [Assessing internal quality while coding with an agent](martin-fowler/assessing-internal-quality-while-coding-with-an-agent.md)
- [Humans and Agents in Software Engineering Loops](martin-fowler/humans-and-agents-in-software-engineering-loops.md)
- [Building your own CLI Coding Agent with Pydantic-AI](martin-fowler/building-your-own-cli-coding-agent-with-pydantic-ai.md)
- [Exploring Generative AI](martin-fowler/exploring-generative-ai.md) — series index
- [Fragments: April 29](martin-fowler/fragments-april-29.md) — notes on harness / AGENTS.md

## Adjacent standards and research
- [Model Context Protocol Specification 2025-06-18](other/model-context-protocol-specification-2025-06-18.md) — authoritative MCP spec for context/tools/resources/prompts
- [Lost in the Middle: How Language Models Use Long Contexts](other/lost-in-the-middle-how-language-models-use-long-contexts.md) — peer-reviewed long-context caveat

## High-value multi-repo / solution-level practitioner references (non-priority sources)
- [Repo-of-Repos: Tony's Multi-Repo Workspace for AI Coding Agents](other/repo-of-repos-tony-s-multi-repo-workspace-for-ai-coding-agents.md)
- [The Spine Pattern: Multi-Repo Context for AI-Assisted Development](other/the-spine-pattern-multi-repo-context-for-ai-assisted-development.md)
- [Setting Up AI Coding Assistants for Large Multi-Repo Solutions](other/setting-up-ai-coding-assistants-for-large-multi-repo-solutions.md) — root repository pattern
- [GitHub Copilot Multi-Repo Instructions](other/github-copilot-multi-repo-instructions.md) — shared Copilot repo + multi-root workspace, Arinco
- [Patterns for providing an AI agent context from other internal repos](other/patterns-for-providing-an-ai-agent-context-from-other-internal-repos.md) — submodules, distillation
- [Agentic Tooling Across Multiple Repositories](other/agentic-tooling-across-multiple-repositories.md)
- [RepoSwarm: Give AI Agents Context Across All Your Repos](other/reposwarm-give-ai-agents-context-across-all-your-repos.md)
- [Introducing Context Repositories: Git-based Memory for Coding Agents](other/introducing-context-repositories-git-based-memory-for-coding-agents.md) — Letta
- [How Copilot understands your workspace](other/how-copilot-understands-your-workspace.md) — VS Code multi-root
- [Optimizing GitHub Copilot for Multi-Repository Teams in VS Code](other/optimizing-github-copilot-for-multi-repository-teams-in-vs-code.md) — community discussion
- [Monorepo vs Multi-Repo AI: Architecture-based AI Tool Selection](other/monorepo-vs-multi-repo-ai-architecture-based-ai-tool-selection.md) — Augment Code

## Tools
Three approaches to feeding agents codebase context — **packing** (simple, local, zero-infra, dumps the repo), **indexing/retrieval** (token-efficient, queryable, sometimes needs a vector DB or LSP/SCIP infra), and **cross-repo** platforms.

### Packing — repo → single file
- [Repomix: Pack your codebase into AI-friendly formats](other/repomix-pack-your-codebase-into-ai-friendly-formats.md) — packs one or more repos into a single token-optimized file; MCP server, Tree-sitter compression (MIT)
- [code2prompt: Convert your codebase into a single LLM prompt](other/code2prompt-convert-your-codebase-into-a-single-llm-prompt.md) — Rust CLI with templating + token counting; also ships an MCP server and Python SDK for RAG (MIT)

### Indexing / semantic retrieval
- [CodeGraph: Pre-indexed knowledge graph for AI code context](other/codegraph-pre-indexed-knowledge-graph-for-ai-code-context.md) — local SQLite index of symbols/dependencies/call paths; surgical context in one tool call; per-project multi-repo via `projectPath` (MIT)
- [Serena: Semantic MCP toolkit for coding agents](other/serena-semantic-mcp-toolkit-for-coding-agents.md) — symbol-level retrieval/editing over LSP backends (40+ languages); no extra infra (MIT)
- [Aider's repository map: PageRank-ranked codebase context](other/aider-repository-map-pagerank-ranked-codebase-context.md) — in-tool, token-budgeted, dependency-ranked symbol selection (reference design)
- [Claude Context: Semantic code-search MCP for Claude Code](other/claude-context-semantic-code-search-mcp-for-claude-code.md) — hybrid BM25 + dense-vector search; requires a vector DB + embedding provider

### Multi-repo / cross-repo
- [code-review-graph: Multi-repo code knowledge graph over MCP](other/code-review-graph-multi-repo-code-knowledge-graph-over-mcp.md) — multi-repo registry + background daemon watching several repos; ~30 MCP tools incl. cross-repo search (MIT)
- [Sourcegraph / Cody: Cross-repository context via SCIP code graph](other/sourcegraph-cody-cross-repository-context-via-scip-code-graph.md) — true cross-repo navigation via SCIP, scales to 300k+ repos; full multi-repo largely Enterprise-tier

## Recommendations
- **Start here for the core concept:** Anthropic's "Effective context engineering for AI agents" and ThoughtWorks' Technology Radar "Context engineering" + "Progressive context disclosure" blips. These define the discipline and its key techniques (write/select/compress/isolate, just-in-time retrieval, compaction).
- **For the multi-repo / solution-level angle specifically:** read the practitioner cluster (Repo-of-Repos, Spine Pattern, root-repository, Arinco multi-repo instructions) alongside GitHub's "Connect agents to external tools" docs and Microsoft's "Search Less, Build More" ADO MCP post — together these cover the realistic toolkit (shared instruction repos, multi-root workspaces, widened MCP/PAT scope, cross-repo code search via MCP).
- **For shared-team/org distribution of context:** ThoughtWorks "Curated shared instructions for software teams" + "Agent Skills" and the AGENTS.md standard are the canonical references for propagating context across many repos.

## Caveats
- "Solution-level context across multiple repositories" is not yet a named, vendor-blessed discipline; the priority sources mostly address context engineering and single-repo agent context, while explicit multi-repo patterns come largely from practitioner blogs (listed separately and clearly marked as non-priority sources).
- GitHub Copilot's cloud agent is documented as scoped to a single repository by default (broader access requires giving it a personal access token with wider scope via repository MCP settings); there is no native cross-repo "whole-solution" context feature as of June 2026.
- There is no GitHub Blog page literally titled "Get started with context engineering in GitHub Copilot" — that phrase appears only as an in-article call-to-action; the intended practical guide is the "How to build reliable AI workflows with agentic primitives and context engineering" post, with "Want better AI outputs? Try context engineering." as the overview.
- Several Microsoft and Google links are product-announcement or marketing pages (e.g., Agentic Data Cloud, Microsoft Agent Framework, Gemini Code Assist business) and should be read as such rather than neutral technical guides.
