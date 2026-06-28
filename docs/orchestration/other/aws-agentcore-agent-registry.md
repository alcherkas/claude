---
title: 'AWS AgentCore Agent Registry: governed catalog for agent discovery'
url: https://aws.amazon.com/about-aws/whats-new/2026/04/aws-agent-registry-in-agentcore-preview/
canonical_url: https://aws.amazon.com/about-aws/whats-new/2026/04/aws-agent-registry-in-agentcore-preview/
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

# AWS AgentCore Agent Registry: governed catalog for agent discovery

> **Excerpt from the original:** As part of the AWS Bedrock AgentCore fleet stack, the **Agent Registry** (Preview, April 2026) is a **centralized, governed catalog** for **discovering and managing** agents, MCP servers, tools, and skills via a **publish / review / approve** workflow. It complements AgentCore's session-isolated serverless [Runtime](aws-bedrock-agentcore-managed-agent-runtime.md) and its cross-agent **Memory** (shared stores with long-term persistence), so a fleet has a single place to register what exists and control what gets used.

**Relevance to agentic orchestration & workflows:** The **registry / discovery** layer for operating a fleet — the catalog-and-governance answer to "what agents/tools exist, who approved them, and how does an orchestrator find one to delegate to." It is the *platform* form of discovery, sitting alongside the *protocol* form (A2A [Agent Cards](../google/agent2agent-protocol-specification.md)) and the *infrastructure* form ([Kubernetes Agent Sandbox](kubernetes-agent-sandbox-fleet-lifecycle.md) stable identity). *Evidence: high-confidence for the registry's existence and publish/review/approve model — verified unanimous 3-0 against the AWS what's-new + AgentCore docs (June 2026). Caveat: **Preview, not GA** (April 2026) — capabilities and availability will change.*

[Read the original →](https://aws.amazon.com/about-aws/whats-new/2026/04/aws-agent-registry-in-agentcore-preview/)

*Source: Other · AWS what's-new (AgentCore Agent Registry, Preview) · April 2026 · archived 2026-06-28.*
