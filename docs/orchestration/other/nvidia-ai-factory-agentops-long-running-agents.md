---
title: 'NVIDIA Enterprise AI Factory: AgentOps & long-running-agent primitives'
url: https://docs.nvidia.com/ai-enterprise/planning-resource/ai-factory-white-paper/latest/agentic-ai-in-the-factory.html
canonical_url: https://docs.nvidia.com/ai-enterprise/planning-resource/ai-factory-white-paper/latest/agentic-ai-in-the-factory.html
org: Other
theme: orchestration
subtopic: patterns
source_type: html
tags:
- Other
- orchestration
- patterns
fetch_status: ok
http_status: 200
fetched_at: '2026-06-29'
also_in: null
---

# NVIDIA Enterprise AI Factory: AgentOps & long-running-agent primitives

> **Excerpt from the original:** NVIDIA's Enterprise AI Factory Design Guide white paper defines **AgentOps** — *"a new operational discipline for managing agentic AI systems at scale"* — built on a **three-pronged monitoring strategy**: workflow traces, logging of user activity and model responses, and outcome performance metrics. It provisions each **long-running agent** with three concrete primitives: a **mountable workspace** filesystem (e.g. `/workspace/` backed by S3, SQLite, or local storage), versioned modular **Skills** (directories with an interface spec `SKILL.md`, executable code, and dependencies), and **isolated sandboxes** (restricted network, CPU/memory limits, time-bounded sessions).

**Relevance to agentic orchestration & workflows:** The piece that **bridges the two senses of "AI factory"** — NVIDIA's term originally means the data-center-as-AI-plant, but this chapter defines the *agentic* operating discipline (AgentOps) and the concrete per-agent runtime primitives (workspace, Skills, sandbox) a factory uses to build and run agents at scale. The Skill format (`SKILL.md` + code + deps) is the same open Agent Skills standard catalogued in [context-engineering](../../context-engineering/index.md); the workspace+sandbox primitives parallel [Kubernetes Agent Sandbox](kubernetes-agent-sandbox-fleet-lifecycle.md). *Evidence: high-confidence — AgentOps definition, three-pronged monitoring, and all three per-agent primitives verified unanimous 3-0 against the primary NVIDIA white paper (June 2026).*

[Read the original →](https://docs.nvidia.com/ai-enterprise/planning-resource/ai-factory-white-paper/latest/agentic-ai-in-the-factory.html)

*Source: Other · NVIDIA Enterprise AI Factory Design Guide (agentic) · updated May 2026 · archived 2026-06-29.*
