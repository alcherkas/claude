---
title: 'Kubernetes Agent Sandbox: fleet-lifecycle primitives for agents'
url: https://kubernetes.io/blog/2026/03/20/running-agents-on-kubernetes-with-agent-sandbox/
canonical_url: https://kubernetes.io/blog/2026/03/20/running-agents-on-kubernetes-with-agent-sandbox/
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

# Kubernetes Agent Sandbox: fleet-lifecycle primitives for agents

> **Excerpt from the original:** Kubernetes Agent Sandbox ([kubernetes-sigs/agent-sandbox](https://github.com/kubernetes-sigs/agent-sandbox); kubernetes.io blog, Mar 20 2026) provides production **fleet-lifecycle primitives** via four CRDs. A **`SandboxWarmPool`** maintains pre-provisioned pods to **eliminate cold starts** (claimed via a `SandboxClaim` against a `SandboxTemplate`); the **`Sandbox`** resource **scales idle environments to zero** to save resources while **resuming exactly where they left off**; and each Sandbox gets a **stable hostname / network identity** so agents in a coordinated multi-agent system can **discover and communicate with each other**.

**Relevance to agentic orchestration & workflows:** The open, self-hostable answer to the "how do you *operate* a fleet of agents over time" layer — the lifecycle primitives (warm pools, scale-to-zero-with-resume, stable identity) that a swarm needs but that the application frameworks and durable substrates don't provide. The **stable network identity** is also a concrete *agent-discovery* mechanism, complementing A2A's protocol-level [Agent Cards](../google/agent2agent-protocol-specification.md) and the platform [AgentCore Registry](aws-agentcore-agent-registry.md). *Evidence: high-confidence — the four-CRD model and each primitive verified unanimous 3-0 against the official kubernetes.io blog + the kubernetes-sigs repo (June 2026). Caveat: very recent (Mar 2026); CRDs/APIs are fast-moving.*

[Read the original →](https://kubernetes.io/blog/2026/03/20/running-agents-on-kubernetes-with-agent-sandbox/)

*Source: Other · kubernetes.io blog + github.com/kubernetes-sigs/agent-sandbox · Mar 2026 · archived 2026-06-28.*
