---
title: Reference architecture — safe autonomy for AI coding agents
---

# Reference architecture — safe autonomy for AI coding agents

!!! note "What this page is"
    A **synthesis**, not a single external source. No vendor has published a complete end-to-end reference architecture for securing and governing autonomous AI coding agents, so this page stitches one together from the primary sources catalogued in this wiki — the [Tools](index.md#tools) parts list, the [Patterns / Techniques](index.md#patterns-techniques) catalog, and the governance frameworks. The design principle throughout is **defense-in-depth that assumes the model can be fooled**: no single layer is trusted, and the backbone is *out-of-model* enforcement (rules the LLM cannot widen via prompt or `CLAUDE.md`). Layers that remain immature are marked **⚠ seam**. Assembled June 2026.

## The pipeline

A safe-autonomy system is an assembly of seven runtime layers, wrapped by a governance/standards layer and (for regulated environments) a compliance overlay. Most layers are well-covered by primary sources; three are genuine seams you must engineer around.

```
  GOVERN  standards wrapper — OWASP Agentic / NIST AI RMF / SAIF 2.0 / MS maturity
  ┌──────────────────────────────────────────────────────────────────┐
  │  agent action request                                             │
  │     │                                                             │
  │     ▼                                                             │
  │  1 IDENTITY      scoped agent identity + short-lived credentials  │
  │  2 AUTHORIZE     out-of-model PDP — policy-as-code (OPA/Cedar)  ⚠  │
  │  3 PERMISSION    harness allow · ask · deny + modes (the gate)    │
  │  4 ISOLATE       sandbox: filesystem / network confinement        │
  │  5 INJECT-DEF    prompt-injection / MCP guards                 ⚠  │
  │  6 MONITOR       runtime interception + audit trail               │
  │  7 RESPOND       kill switch · credential revoke · rollback    ⚠  │
  └──────────────────────────────────────────────────────────────────┘
            ▲                                              │
            └──────────── SDLC harness feedback ───────────┘
  COMPLY  compliance overlay — PCI DSS Reqs 3 / 6 / 7 / 10 for the CDE
```

## Layer by layer

| # | Layer | What to use | Maturity |
|---|---|---|---|
| 0 | **Govern (wrapper)** | [OWASP Agentic Threats T1–T15](other/owasp-agentic-ai-threats-and-mitigations.md) + [Top 10 for Agentic Apps](other/owasp-top-10-for-agentic-applications-2026.md); [NIST AI RMF](other/nist-ai-risk-management-framework.md); [Google SAIF 2.0](google/saif-focus-on-agents.md); [MS agentic maturity model](microsoft-github/agentic-ai-maturity-model-ai-governance-and-security.md). | **Strong** — standards-body primary |
| 1 | **Identity & credentials** | [Entra Agent ID](microsoft-github/entra-agent-id-governance-and-kill-switch.md); [Auth0 Token Vault](other/auth0-for-ai-agents-token-vault-and-approvals.md); [Arcade](other/arcade-dev-oauth-tool-authorization-for-agents.md); [Infisical Agent Vault](other/infisical-agent-vault-credential-proxy-for-agents.md); [HashiCorp Vault](other/hashicorp-vault-agentic-runtime-security.md). | Strong |
| 2 | **Authorize (out-of-model PDP)** | [Permit.io MCP Gateway](other/permit-io-mcp-gateway-agent-authorization.md); [MS Agent Governance Toolkit](microsoft-github/agent-governance-toolkit-opa-cedar-policy-backends.md); [OPA/Rego + OPAL](other/open-policy-agent-opa-rego-and-opal.md); [AWS Cedar / AVP](other/aws-cedar-amazon-verified-permissions.md). | **⚠ seam** — engines production-grade; *agent wiring* early & vendor-asserted |
| 3 | **Harness permission model** | [Claude Code permissions & modes](anthropic/claude-code-permissions-and-permission-modes.md); [Copilot allowlist](microsoft-github/copilot-allowlist-reference.md) + [firewall](microsoft-github/customizing-or-disabling-the-firewall-for-copilot-coding-agent.md); [Codex approvals](other/openai-codex-agent-approvals-and-sandboxing.md). | **Strongest layer** — best-documented, enforceable today |
| 4 | **Sandboxed execution** | [E2B](other/e2b-secure-cloud-sandboxes-for-ai-generated-code.md), [Firecracker](other/firecracker-microvm-monitor-for-isolated-execution.md), [gVisor](other/gvisor-application-kernel-container-sandbox.md), [Anthropic Sandbox Runtime](anthropic/sandbox-runtime.md) + the [Claude Code sandboxing blog](anthropic/making-claude-code-more-secure-and-autonomous-with-sandboxing.md). | Strong |
| 5 | **Prompt-injection defense** | [LlamaFirewall](other/llamafirewall-open-source-guardrail-framework.md); [NeMo Guardrails](other/nvidia-nemo-guardrails.md); [Guardrails AI](other/guardrails-ai-validators-and-guards.md); MCP scanners ([MCP-Scan](other/mcp-scan-snyk-agent-scan-mcp-security-scanner.md), [Cisco](other/cisco-ai-defense-mcp-scanner.md)); [CaMeL](other/camel-defeating-prompt-injections-by-design.md) / [dual-LLM design patterns](other/design-patterns-for-securing-llm-agents-against-prompt-injection.md). | **⚠ seam** — *contained, not prevented*; architect for blast radius |
| 6 | **Monitor (interception + audit)** | [MS Defender runtime protection](microsoft-github/defender-ai-agent-runtime-protection.md) (pre-tool-call block); audit/tracing lives in the [evals-observability theme](../evals-observability/index.md). | Interception **Preview**; audit **cross-theme** |
| 7 | **Respond (kill / revoke / roll back)** | [Claude Code /rewind](anthropic/claude-code-checkpointing-and-rewind.md); [Entra disable](microsoft-github/entra-agent-id-governance-and-kill-switch.md); [Google SCC revoke](google/scc-agent-credential-detection-and-revocation.md); [denial-threshold auto-halt](anthropic/claude-code-auto-mode-model-graded-approvals.md). | **⚠ seam** — no agent-native rollback of bash/merged changes |

## How shipping agents map onto it

Each production coding agent implements a *subset* of this pipeline — which is exactly why no single one is a complete blueprint:

- **Claude Code** — strongest on 3/4/7: [allow·ask·deny + permission modes](anthropic/claude-code-permissions-and-permission-modes.md) with *out-of-model* enforcement, [OS-level sandbox](anthropic/making-claude-code-more-secure-and-autonomous-with-sandboxing.md) (−84% prompts internally), [/rewind](anthropic/claude-code-checkpointing-and-rewind.md) + [denial-threshold halt and model-graded approvals](anthropic/claude-code-auto-mode-model-graded-approvals.md). Identity/authz come from [enterprise SSO/RBAC](anthropic/claude-code-for-enterprise.md).
- **GitHub Copilot coding agent** — strongest on 4/6: [ephemeral firewalled environment](microsoft-github/responsible-use-of-github-copilot-cloud-agent.md) + [allowlist](microsoft-github/copilot-allowlist-reference.md), CodeQL/secret/dependency scanning, signed commits + session logs ([application card](microsoft-github/application-card-github-copilot-agents.md)). Thin on the in-loop permission gate (layer 3).
- **OpenAI Codex** — strongest on 3/4: [approvals + sandboxing](other/openai-codex-agent-approvals-and-sandboxing.md) tiers.
- **Microsoft control plane** (Defender + Entra + Purview) — strongest on 1/2/6/7: [agent identity kill switch](microsoft-github/entra-agent-id-governance-and-kill-switch.md), [runtime interception + blast-radius graphs](microsoft-github/defender-ai-agent-runtime-protection.md), [org-wide governance](microsoft-github/governance-and-security-for-ai-agents-across-the-organization.md). Mostly **Preview** (1 Jul 2026 licensing gate for cloud-agent blocking).

## The seams

1. **No published end-to-end blueprint.** This page is the closest assembly; treat it as a starting skeleton, not a proven design.
2. **Prompt injection is contained, not prevented** (layer 5). Every layer below it exists *because* the model can be fooled. Buy/build blast-radius reduction (sandbox + out-of-model authz), not "no injection." Do not overstate any single guard's indirect-injection coverage.
3. **No agent-native rollback of destructive bash (`rm`/`mv`) or already-merged changes** (layer 7). [/rewind](anthropic/claude-code-checkpointing-and-rewind.md) covers only model-driven edits. Compensate with ephemeral throwaway workspaces, default-deny egress, and branch protection. It is also unclear whether *halt* (kill switch) and *revoke* (credential rotation) compose automatically or leave an exploitable latency gap.
4. **Policy-as-code agent wiring is vendor-asserted, not benchmarked** (layer 2). [OPA](other/open-policy-agent-opa-rego-and-opal.md)/[Cedar](other/aws-cedar-amazon-verified-permissions.md) engines are production-grade; reaching them into the *coding-agent* path ([Permit.io](other/permit-io-mcp-gateway-agent-authorization.md), [MS toolkit](microsoft-github/agent-governance-toolkit-opa-cedar-policy-backends.md)) is early and uses hand-written policy. NL→Rego ([Prose2Policy](other/guardrails-as-code-nl-to-rego-research.md), [AAGATE](other/aagate-nist-rmf-aligned-agentic-governance-platform.md)) is still research-only.
5. **Audit (layer 6) and SAST/SCA for AI-generated code lean outside this theme.** Tracing/observability is covered in the [evals-observability theme](../evals-observability/index.md); SAST/SCA tooling (Semgrep, Socket, CodeQL, OSV-Scanner, Snyk) surfaced only as unverified leads — close with a follow-up pass before relying on them.

## A pragmatic starting path

1. **Permissions + sandbox first** (layers 3, 4) — highest leverage and *available today* in Claude Code / Copilot / Codex. Enforce allow·ask·deny out-of-model and run inside an OS sandbox before anything else.
2. **Add identity + authz** (layers 1, 2) — scoped, short-lived per-agent credentials plus an MCP gateway acting as a Policy Decision Point. Expect to hand-write the policy.
3. **Layer injection defense + audit** (layers 5, 6) — guardrail/MCP scanners in the loop and full tool-call tracing ([evals-observability](../evals-observability/index.md)); treat injection defense as depth, not a perimeter.
4. **Wire incident response** (layer 7) — kill switch + checkpoint rollback, *plus* the compensating controls in seam 3 (ephemeral workspaces, branch protection, default-deny egress) for the changes /rewind can't undo.
5. **Wrap in a governance framework** (layer 0) — map controls onto [OWASP Agentic](other/owasp-agentic-ai-threats-and-mitigations.md)/[NIST AI RMF](other/nist-ai-risk-management-framework.md)/[SAIF](google/saif-focus-on-agents.md), and apply the [PCI DSS overlay](index.md#pci-dss-considerations) (Reqs 3/6/7/10) if the agent can reach a cardholder data environment.

## Governing principle: assume the model can be fooled

The strongest cross-cutting finding across every primary source: **prompting the model to be safe is not a control.** Safety comes from enforcement the LLM cannot influence — out-of-model permission gates, OS sandboxing, an external PDP, and runtime interception — composed so that any single fooled layer is caught by the next. Optimize for *minimizing blast radius and time-to-contain*, not for trusting the agent. See [Patterns / Techniques](index.md#patterns-techniques) for each pattern and the failure mode it mitigates.
