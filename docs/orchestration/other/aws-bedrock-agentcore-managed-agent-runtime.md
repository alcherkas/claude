---
title: 'AWS Bedrock AgentCore: serverless managed runtime for AI agents'
url: https://docs.aws.amazon.com/bedrock-agentcore/latest/devguide/agents-tools-runtime.html
canonical_url: https://docs.aws.amazon.com/bedrock-agentcore/latest/devguide/agents-tools-runtime.html
org: Other
theme: orchestration
subtopic: tools
source_type: html
tags:
- Other
- orchestration
- tools
fetch_status: ok
http_status: 200
fetched_at: '2026-06-28'
also_in: null
---

# AWS Bedrock AgentCore: serverless managed runtime for AI agents

> **Excerpt from the original:** Amazon Bedrock AgentCore is a **fully managed agentic platform** to "build, deploy, and operate highly capable agents securely at scale using any framework, model, or protocol… all without any infrastructure management" (GA **Oct 13, 2025**; preview Jul 2025). It is **framework-, model-, and protocol-agnostic** (LangGraph, CrewAI, LlamaIndex, Strands, Google ADK, OpenAI Agents SDK; any LLM in or outside Bedrock; MCP and A2A). It is composed of **modular services usable together or independently** — Runtime, Memory, Gateway, Identity, built-in tools, Observability. The **Runtime** is a serverless host that gives **each user session its own dedicated Firecracker microVM** (isolated CPU/memory/filesystem), terminated and memory-sanitized after the session, and supports **long-running workloads up to 8 hours**. Pricing is **consumption-based with no commitments**, billed per second (1-second minimum): **$0.0895/vCPU-hour** and **$0.00945/GB-hour**, charging only actual CPU consumed (free during I/O wait), with Memory/Gateway/Identity/Web Search billed per request.

**Relevance to agentic orchestration & workflows:** The "where does this run in production" layer for the AWS ecosystem — the managed deployment target that sits *above* a self-hosted durable substrate (Temporal/Restate/etc.). Its distinguishing traits in the three-way comparison are **microVM-per-session isolation** (the strongest isolation story of the three) and **modular composable services** (you can adopt just the Runtime, or add Memory/Gateway/Identity à la carte). *Evidence: high-confidence — nearly all claims verified unanimous (3-0) against primary AWS docs (devguide + prescriptive-guidance + pricing pages) in the June 2026 research pass. Pricing/limits are documented service specs, not marketing. The "any framework/model/protocol" absolutes are vendor self-description but architecturally plausible (the runtime hosts your code, which makes its own model calls).*

[Read the original →](https://docs.aws.amazon.com/bedrock-agentcore/latest/devguide/agents-tools-runtime.html)

*Source: Other · AWS Bedrock AgentCore devguide · GA 2025-10-13 · archived 2026-06-28.*
