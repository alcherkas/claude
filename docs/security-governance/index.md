---
title: Security, Governance & Safe Autonomy for AI Coding Agents
---

# Security, Governance & Safe Autonomy for AI Coding Agents


## TL;DR
- This is a link-first reference delivering the most authoritative and recent materials from **Anthropic, Google, Microsoft/GitHub, Thoughtworks, and Martin Fowler** on securing and governing AI coding agents across the SDLC, plus dedicated **PCI DSS** sources.
- The strongest primary sources are vendor docs and engineering blogs (Anthropic Claude Code security, sandboxing & managed settings; GitHub coding-agent firewall docs; Microsoft Learn agent governance; Google **SAIF 2.0**, announced October 2025 with an agent risk map, alongside CodeMender and the new AI Vulnerability Reward Program) plus Thoughtworks/Martin Fowler "harness engineering."
- For PCI DSS, the authoritative primary sources are the **PCI SSC's 2025 AI principles** and **AI-in-assessments guidance**; AI tools touching cardholder data are in-scope under **PCI DSS v4.0.1** (published 11 June 2024 as a limited revision with no added/deleted requirements; v4.0 retired 31 December 2024; future-dated v4.0 requirements became mandatory 31 March 2025).
- New **[Tools](#tools)** and **[Patterns / Techniques](#patterns-techniques)** catalogs (added June 2026 from adversarially-verified research passes) cover the concrete safe-autonomy toolkit — **sandboxed execution** (E2B, Firecracker, gVisor), **agent authorization & least-privilege scoping** (Auth0, Arcade, Infisical, Vault), **prompt-injection/MCP scanning** (MCP-Scan, Cisco MCP Scanner, LlamaFirewall), **policy-as-code & guardrail engines** (OPA/Cedar, Permit.io MCP Gateway, Microsoft Agent Governance Toolkit, NeMo Guardrails), and **incident response, kill switches & rollback** (Claude Code /rewind, Microsoft Defender agent runtime protection + Entra Agent ID, Google SCC) — plus named patterns grouped by evidence quality and a **[Gaps identified during research](#gaps-identified-during-research)** section.

---

## Key Findings (how to use this list)
- **Permission/guardrail models** are best documented by Anthropic (permissions allow/ask/deny, sandboxing, managed settings) and GitHub (coding-agent firewall + allowlist).
- **Governance frameworks** are best documented by Google (SAIF 2.0 + Agent Risk Map), Microsoft (agentic AI maturity model, Cloud Adoption Framework), and Thoughtworks (Agent/works, Technology Radar).
- **Safe-autonomy engineering practice** is best articulated by Martin Fowler / Thoughtworks "harness engineering" (guides + sensors, feedforward + feedback).
- **PCI DSS** specifics live with the PCI SSC primary sources; practitioner pieces map AI use to Requirements 3, 6, 7, and 10.

---

## Anthropic

### Claude Code security, sandboxing & permissions
- [Security – Claude Code Docs](anthropic/security-claude-code-docs.md) — read-only default, permission model, MCP controls
- [Claude Code settings](anthropic/claude-code-settings.md) — managed settings, permissions, MDM/registry policy, precedence
- [Making Claude Code more secure and autonomous with sandboxing](anthropic/making-claude-code-more-secure-and-autonomous-with-sandboxing.md) — engineering blog; OS-level bubblewrap/Seatbelt; reduced permission prompts by 84% in internal use
- [Sandbox Runtime](anthropic/sandbox-runtime.md) — Anthropic Experimental repository for filesystem/network-isolated agent workloads
- [How we contain Claude across products](anthropic/how-we-contain-claude-across-products.md) — engineering; environment/runtime containment
- [Claude Code for Enterprise](anthropic/claude-code-for-enterprise.md) — SSO, SCIM, RBAC, custom retention, IP allowlisting, Bedrock/Vertex/Foundry

### Agent security frameworks, governance & safeguards
- [Trustworthy agents in practice](anthropic/trustworthy-agents-in-practice.md) — shared-responsibility model: Model / Harness / Tools / Environment
- [Building Effective AI Agents](anthropic/building-effective-ai-agents.md) — research; guardrails, sandboxed testing patterns
- [Building safeguards for Claude](anthropic/building-safeguards-for-claude.md) — classifiers, usage-policy enforcement
- [Mitigating the risk of prompt injections in browser use](anthropic/mitigating-the-risk-of-prompt-injections-in-browser-use.md) — research; RL-based injection robustness
- [Mitigate jailbreaks and prompt injections](anthropic/mitigate-jailbreaks-and-prompt-injections.md) — Claude API docs; red-teaming guidance
- [Anthropic Transparency Hub](anthropic/anthropic-transparency-hub.md) — prompt-injection / agentic-coding eval results by model

### AI for code security (defensive)
- [Making frontier cybersecurity capabilities available to defenders — Claude Code Security](anthropic/making-frontier-cybersecurity-capabilities-available-to-defenders-claude-code.md) — vulnerability scanning + human-approved patches

---

## Google (Google Cloud, Google Research, DeepMind, Safety)

### Secure AI Framework (SAIF) and agent security
- [SAIF home](google/saif-home.md)
- [SAIF "Focus on Agents"](google/saif-focus-on-agents.md) — Agent Risk Map; orchestration, memory, tools, RAG, system instructions
- [Google's Secure AI Framework](google/google-s-secure-ai-framework.md) — Safety Center
- [Secure AI Framework (SAIF) — Google Cloud use case](google/secure-ai-framework-saif-google-cloud-use-case.md) — Model Armor, Security Command Center
- [Implementing SAIF Controls in Google Cloud](google/implementing-saif-controls-in-google-cloud.md) — technical paper, PDF; six domains incl. Governance/Assurance
- [Cloud CISO Perspectives: Practical guidance on building with SAIF](google/cloud-ciso-perspectives-practical-guidance-on-building-with-saif.md) — "prompts as code," identity propagation for agents
- [Google's AI security strategy](google/google-s-ai-security-strategy.md) — SAIF 2.0 + Agent Risk Map, CodeMender, AI Vulnerability Reward Program; announced October 2025
- [10 Tips for Governing AI Agents](google/10-tips-for-governing-ai-agents.md) — Google Cloud Office of the CISO blog; sandboxes, kill switches, auditability
- [Secure AI Innovation Without Interruption](google/secure-ai-innovation-without-interruption.md) — Model Armor, least-privilege agent IAM

### DeepMind / Google Research — AI for code security
- [Introducing CodeMender: an AI agent for code security](google/introducing-codemender-an-ai-agent-for-code-security.md) — DeepMind; per the post, "we have already upstreamed 72 security fixes to open source projects, including some as large as 4.5 million lines of code"; uses Gemini Deep Think; e.g., -fbounds-safety annotations would have rendered the libwebp CVE-2023-4863 zero-click exploit unexploitable
- [Securing the AI Software Supply Chain](google/securing-the-ai-software-supply-chain.md) — Google Research paper; provenance, SLSA for AI
- [Same same but also different: Google guidance on AI supply chain security](google/same-same-but-also-different-google-guidance-on-ai-supply-chain-security.md)

### Software supply chain / SDLC security (applicable to AI-generated code)
- [Software supply chain security](google/software-supply-chain-security.md) — Google Cloud docs; Binary Authorization, provenance, SBOM
- [Google introduces SLSA framework](google/google-introduces-slsa-framework.md)

---

## Microsoft & GitHub

### GitHub Copilot coding agent — security docs
- [Responsible use of GitHub Copilot cloud agent](microsoft-github/responsible-use-of-github-copilot-cloud-agent.md) — ephemeral firewalled env, CodeQL/secret/dependency scanning, write-access gating
- [Application card: GitHub Copilot Agents](microsoft-github/application-card-github-copilot-agents.md) — cloud agent, CLI, SDK; signed commits, session logs
- [Customizing or disabling the firewall for Copilot coding agent](microsoft-github/customizing-or-disabling-the-firewall-for-copilot-coding-agent.md) — allowlist, data-exfiltration controls, documented limitations
- [Copilot allowlist reference](microsoft-github/copilot-allowlist-reference.md) — default-allowed package registries, CAs, container registries
- [Organization firewall settings for Copilot cloud agent](microsoft-github/organization-firewall-settings-for-copilot-cloud-agent.md) — GitHub Changelog, 3 Apr 2026; org-wide enforcement

### Microsoft Learn — AI agent governance & secure SDLC
- [Agentic AI maturity model — AI governance and security](microsoft-github/agentic-ai-maturity-model-ai-governance-and-security.md) — CMM-based, five levels
- [Governance and security for AI agents across the organization](microsoft-github/governance-and-security-for-ai-agents-across-the-organization.md) — Cloud Adoption Framework; four-layer model, Purview, Defender for Cloud, RBAC
- [Process to build agents across your organization](microsoft-github/process-to-build-agents-across-your-organization.md) — secure build process; AI gateway, memory governance, guardrails, CI/CD evals
- [Security and governance — Microsoft Copilot Studio](microsoft-github/security-and-governance-microsoft-copilot-studio.md) — Security Development Lifecycle, DLP, Customer Lockbox
- [Copilot Control System Security and Governance](microsoft-github/copilot-control-system-security-and-governance.md) — Microsoft 365; Purview audit, DSPM for AI, sensitivity labels

### Microsoft Security Blog — agentic AI threats & defenses
- [Secure agentic AI end-to-end](microsoft-github/secure-agentic-ai-end-to-end.md) — RSAC 2026; Zero Trust for agents, Entra prompt-injection protection
- [When prompts become shells: RCE vulnerabilities in AI agent frameworks](microsoft-github/when-prompts-become-shells-rce-vulnerabilities-in-ai-agent-frameworks.md) — Semantic Kernel CVE-2026-25592, CVE-2026-26030
- [Securing CI/CD in an agentic world: Claude Code GitHub Action case](microsoft-github/securing-ci-cd-in-an-agentic-world-claude-code-github-action-case.md) — prompt-injection to workflow secrets; "Agents Rule of Two"
- [Threat modeling AI applications](microsoft-github/threat-modeling-ai-applications.md)
- [Detecting and analyzing prompt abuse in AI tools](microsoft-github/detecting-and-analyzing-prompt-abuse-in-ai-tools.md) — OWASP LLM, indirect injection playbook

---

## Thoughtworks

- [Technology Radar](thoughtworks/technology-radar.md) — latest volume — "cognitive debt," permission-hungry agents, harnesses, zero trust
- [Technology Radar — Techniques](../context-engineering/thoughtworks/techniques-technology-radar.md) — CLAUDE.md/AGENTS.md service templates, feedback sensors for coding agents
- [The VibeSec reckoning: Why prompting your AI to be secure isn't enough](thoughtworks/the-vibesec-reckoning-why-prompting-your-ai-to-be-secure-isn-t-enough.md) — secure-by-default harness, never-allow rules as gates
- [What is harness engineering?](thoughtworks/what-is-harness-engineering.md) — Technology Podcast with Birgitta Böckeler
- [Thoughtworks launches Agent/works to govern enterprise AI agents](thoughtworks/thoughtworks-launches-agent-works-to-govern-enterprise-ai-agents.md) — governed runtime, single control plane

---

## Martin Fowler (martinfowler.com)

- [Harness engineering for coding agent users](../context-engineering/martin-fowler/harness-engineering-for-coding-agent-users.md) — Böckeler; guides + sensors, feedforward + feedback, Agent = Model + Harness
- [Maintainability sensors for coding agents](../context-engineering/martin-fowler/maintainability-sensors-for-coding-agents.md) — Böckeler; linting/rules/self-correction in the agent loop
- [The VibeSec Reckoning](martin-fowler/the-vibesec-reckoning.md) — security guardrails and never-allow rules as mandatory pre-commit hooks
- [Exploring Generative AI](../context-engineering/martin-fowler/exploring-generative-ai.md) — memo series index — Context Engineering, Spec-Driven Development, reference apps
- [To vibe or not to vibe](martin-fowler/to-vibe-or-not-to-vibe.md) — risk-based review: probability × impact × detectability
- [I still care about the code](martin-fowler/i-still-care-about-the-code.md) — why review/oversight remains essential
- [Some thoughts on LLMs and Software Development](martin-fowler/some-thoughts-on-llms-and-software-development.md) — Fowler, 28 Aug 2025

---

## PCI DSS Considerations

### PCI SSC primary sources
- [AI Principles: Securing the Use of AI in Payment Environments](pci-dss/ai-principles-securing-the-use-of-ai-in-payment-environments.md) — Andrew Jamieson, 11 Sept 2025; "must/should not/should/may" principles, least privilege, Requirement 6/7 references
- [New Guidance: Integrating Artificial Intelligence into PCI Assessments](pci-dss/new-guidance-integrating-artificial-intelligence-into-pci-assessments.md) — Alicia Malone, 17 Mar 2025; "AI is a tool, not an assessor"
- [PCI Perspectives — Artificial Intelligence (AI) topic / "The AI Exchange: Innovators in Payment Security" series landing](pci-dss/pci-perspectives-artificial-intelligence-ai-topic-the-ai-exchange-innovators-in.md)
- ["Integrating Artificial Intelligence in PCI Assessments – Guidelines, Version 1.0"](pci-dss/integrating-artificial-intelligence-in-pci-assessments-guidelines-version-1-0.md) — 12-page official doc; access via the PCI SSC Document Library — search the title
- [Just Published: PCI DSS v4.0.1](pci-dss/just-published-pci-dss-v4-0-1.md) — limited revision; no added/deleted requirements

### Analysis & practitioner guidance on AI + PCI DSS
- [AI PCI Compliance: A Complete Guide for Businesses](pci-dss/ai-pci-compliance-a-complete-guide-for-businesses.md) — Fieldguide; maps AI risks to Requirements 3, 6, 7, 10
- [AI and PCI Compliance: What Every Company Needs to Know in 2026](pci-dss/ai-and-pci-compliance-what-every-company-needs-to-know-in-2026.md) — Very Good Security
- [PCI Compliance in AI-driven Payment Systems](pci-dss/pci-compliance-in-ai-driven-payment-systems.md) — Walturn; scoping AI into the CDE
- [AI in Payment Environments](pci-dss/ai-in-payment-environments.md) — former-QSA analysis; shadow AI, AI inventory in the CDE, AUP gaps

---

## Cross-cutting risk & research
- [OWASP Top 10 for LLM Applications](other/owasp-top-10-for-llm-applications.md) — community vulnerability taxonomy for LLM application risks
- [OWASP GenAI Security Project - LLM Top 10](other/owasp-genai-security-project-llm-top-10.md) — OWASP GenAI Security Project landing page and guidance
- [NIST AI Risk Management Framework](other/nist-ai-risk-management-framework.md) — standards-body framework for AI risk management and trustworthiness
- [Model Context Protocol Specification 2025-06-18](../context-engineering/other/model-context-protocol-specification-2025-06-18.md) — MCP security principles for user consent, data privacy, and tool safety
- [The 2025 AI Agent Index](other/the-2025-ai-agent-index.md) — arXiv; documents safety/guardrail disclosures across deployed agents incl. Claude Code, Copilot, Gemini
- [SCGAgent: Recreating the Benefits of Reasoning Models for Secure Code Generation with Agentic Workflows](other/scgagent-recreating-the-benefits-of-reasoning-models-for-secure-code-generation.md) — arXiv
- [Are AI-assisted Development Tools Immune to Prompt Injection?](other/are-ai-assisted-development-tools-immune-to-prompt-injection.md) — arXiv survey of agentic coding editor injection attacks
- [Unit 42 "Double Agents": exploiting Vertex AI agent credentials](other/unit42-double-agents-vertex-ai.md) — Palo Alto Networks; independent demonstration that an overprivileged agent service account is a real, revocable attack surface (extracted P4SA creds → all-bucket read); Google remediated via BYOSA

---

## Tools

Tools, products, and open-source frameworks for securing and governing autonomous coding agents, grouped by function — each with what it does and its license. Compiled from a focused research pass (June 2026); every entry below is backed by a primary source (official repo, vendor docs, or spec) and survived 3-vote adversarial verification. Thinly-covered categories are listed under [Gaps identified during research](#gaps-identified-during-research).

### Sandboxed / isolated execution of agent-run code

- [E2B: Secure cloud sandboxes for AI-generated code](other/e2b-secure-cloud-sandboxes-for-ai-generated-code.md) — Apache-2.0 (hosted cloud proprietary); Firecracker-microVM sandboxes for running untrusted agent-produced code.
- [Firecracker: Lightweight microVM monitor](other/firecracker-microvm-monitor-for-isolated-execution.md) — Apache-2.0; KVM microVMs combining hardware isolation with container speed (the primitive under E2B, AWS Lambda, Fargate).
- [gVisor: Application-kernel container sandbox (runsc)](other/gvisor-application-kernel-container-sandbox.md) — Apache-2.0; userspace kernel hardening containers against single-vulnerability escapes.
- [Anthropic Sandbox Runtime](anthropic/sandbox-runtime.md) — experimental filesystem/network-isolated agent workloads (see also the [Claude Code sandboxing blog](anthropic/making-claude-code-more-secure-and-autonomous-with-sandboxing.md)).

### Agent authorization & least-privilege credential scoping

- [Auth0 for AI Agents: Token Vault & human approval](other/auth0-for-ai-agents-token-vault-and-approvals.md) — proprietary; scoped per-user OAuth tokens plus CIBA human-in-the-loop approval.
- [Arcade.dev: OAuth tool authorization for agents](other/arcade-dev-oauth-tool-authorization-for-agents.md) — commercial; per-tool OAuth scopes, user-delegated least privilege.
- [Infisical Agent Vault: credential proxy](other/infisical-agent-vault-credential-proxy-for-agents.md) — MIT (open-core); injects real secrets at a proxy so the agent never sees them.
- [HashiCorp Vault: agentic runtime security](other/hashicorp-vault-agentic-runtime-security.md) — commercial; verifiable per-agent identity plus dynamic secrets.

### Prompt-injection / MCP-security scanning & guardrails

- [MCP-Scan / Snyk Agent Scan](other/mcp-scan-snyk-agent-scan-mcp-security-scanner.md) — Apache-2.0; scans MCP servers/agents/skills for injection, tool poisoning, shadowing, toxic flows.
- [Cisco AI Defense MCP Scanner](other/cisco-ai-defense-mcp-scanner.md) — Apache-2.0; multi-engine (inspect API + YARA + LLM-judge) MCP threat scanner.
- [LlamaFirewall: open-source guardrail framework](other/llamafirewall-open-source-guardrail-framework.md) — MIT; layered guardrails (PromptGuard 2, Agent Alignment Checks, CodeShield).

### Policy-as-code & runtime guardrail engines

*Added June 2026 from a dedicated deep-research pass (closes the policy-as-code gap below). The enforcement **engines** are production-grade; the **agent-specific wiring** is early and largely vendor-described.*

- [Permit.io MCP Gateway](other/permit-io-mcp-gateway-agent-authorization.md) — commercial/managed; proxies MCP tool calls from named clients (Claude Code, Cursor, VS Code) and authorizes each agent/tool/server combination at runtime. The most direct coding-agent policy-as-code evidence.
- [Microsoft Agent Governance Toolkit](microsoft-github/agent-governance-toolkit-opa-cedar-policy-backends.md) — MIT; pluggable OPA/Rego **and** Cedar backends behind one `ExternalPolicyBackend` protocol.
- [Open Policy Agent (OPA/Rego) & OPAL](other/open-policy-agent-opa-rego-and-opal.md) — Apache-2.0 / MIT; the foundational PDP primitive under the products above (OPAL distributes live policy/data).
- [AWS Cedar & Amazon Verified Permissions](other/aws-cedar-amazon-verified-permissions.md) — Apache-2.0 / managed; formally-verified authorization (default-deny, forbid-overrides-permit, deterministic).
- [NVIDIA NeMo Guardrails](other/nvidia-nemo-guardrails.md) — Apache-2.0; programmable runtime rails (Colang); content/behavioral guardrailing, not authorization.
- [Guardrails AI](other/guardrails-ai-validators-and-guards.md) — Apache-2.0; composable Input/Output Guards; content validation, not authorization.

### Incident response, kill switches & rollback

*Added June 2026 from a dedicated deep-research pass (closes the incident-response gap below). All four major vendors now ship concrete controls — most Microsoft capabilities are **Preview**.*

- [Claude Code checkpointing & /rewind](anthropic/claude-code-checkpointing-and-rewind.md) — session-level rollback of model-driven edits; **cannot** undo bash `rm`/`mv`/`cp` or external edits.
- [Microsoft Defender AI agent runtime protection](microsoft-github/defender-ai-agent-runtime-protection.md) — pre-tool-call interception that can **block** unsafe actions before execution (Claude Code, Copilot CLI), plus incident/blast-radius graphs. Preview.
- [Microsoft Entra Agent ID governance](microsoft-github/entra-agent-id-governance-and-kill-switch.md) — disable an agent identity (kill switch), access-package auto-expiry, least privilege, Conditional Access. Preview/licensed.
- [Google SCC agent-credential detection & revocation](google/scc-agent-credential-detection-and-revocation.md) — Event Threat Detection on agent-credential abuse; delete service account + rotate keys.

---

## Patterns / Techniques

Named patterns and techniques for safe autonomy and governance, grouped by evidence quality (mirroring the [context-engineering Patterns](../context-engineering/index.md#patterns) treatment). All groups below carry primary sources; confidence is high for standards-body and vendor-primary patterns and medium for the single-preprint research group.

### Well-evidenced — governance frameworks (standards bodies & vendors)

- [OWASP Agentic AI: Threats & Mitigations (T1–T15)](other/owasp-agentic-ai-threats-and-mitigations.md) — 15 numbered agentic threats each paired with mitigations; the canonical agentic threat vocabulary.
- [OWASP Top 10 for Agentic Applications (2026)](other/owasp-top-10-for-agentic-applications-2026.md) — prioritized agent risks (ASI01 Goal Hijacking, ASI02 Tool Misuse, ASI03 Identity & Privilege Abuse); released 10 Dec 2025.
- [NIST AI Risk Management Framework](other/nist-ai-risk-management-framework.md) — Govern/Map/Measure/Manage; the GenAI profile (NIST AI 600-1) names direct **and indirect** prompt injection as a cybersecurity risk.
- [Google SAIF 2.0 — "Focus on Agents"](google/saif-focus-on-agents.md) — agent risk map naming Prompt Injection and Rogue Actions.

### Well-evidenced — defensive patterns in shipping coding agents

*Failure mode each mitigates is noted in italics; links point to the primary source documenting the pattern.*

- **Human-in-the-loop / approval gate** — explicit human confirmation before state-changing or risky actions. *Mitigates rogue/over-eager actions (OWASP T13).* → [Claude Code permissions](anthropic/claude-code-permissions-and-permission-modes.md), [Codex approvals](other/openai-codex-agent-approvals-and-sandboxing.md).
- **Permission modes / progressive autonomy** (plan → auto-accept → auto → YOLO) — discrete autonomy tiers; plan mode is a read-only dry-run, the YOLO flag removes gating. *Mitigates the blast radius of autonomous error.* → [Claude Code permissions](anthropic/claude-code-permissions-and-permission-modes.md), [Codex approvals](other/openai-codex-agent-approvals-and-sandboxing.md).
- **Command/tool allow · ask · deny lists** — three-tier rules evaluated deny → ask → allow (first match wins; deny carries no exceptions). *Mitigates tool misuse (OWASP T2 / ASI02).* → [Claude Code permissions](anthropic/claude-code-permissions-and-permission-modes.md), [Copilot allowlist reference](microsoft-github/copilot-allowlist-reference.md).
- **Sandboxing as defense-in-depth** — OS-level filesystem/network restriction so injected commands cannot reach out-of-boundary resources even when the model is fooled. *Mitigates prompt-injection blast radius.* → [Claude Code sandboxing](anthropic/making-claude-code-more-secure-and-autonomous-with-sandboxing.md), [Codex approvals](other/openai-codex-agent-approvals-and-sandboxing.md).
- **Least-privilege & tool/capability scoping** — minimize allowed tools/actions; e.g. drop arbitrary-code-execution rules on entering auto mode. *Mitigates privilege compromise (OWASP T3 / ASI03).* → [Claude auto mode](anthropic/claude-code-auto-mode-model-graded-approvals.md).
- **Out-of-model (harness-enforced) permission enforcement** — rules enforced by the harness, not the LLM, so prompt or CLAUDE.md instructions cannot widen them. *A structural prompt-injection defense.* → [Claude Code permissions](anthropic/claude-code-permissions-and-permission-modes.md).
- **Tool-output sanitization / content-vs-instruction separation** — scan file/web/shell/tool outputs for hijack attempts before they enter context. *Mitigates indirect prompt injection.* → [Claude auto mode](anthropic/claude-code-auto-mode-model-graded-approvals.md).
- **Model-graded approval substitute** — a model-based transcript classifier approves each action (a middle ground between manual review and skip-permissions). *Scales HITL without disabling it.* → [Claude auto mode](anthropic/claude-code-auto-mode-model-graded-approvals.md).
- **Audit trails & action traceability** — log every tool call for anomaly detection and post-incident review. *Mitigates repudiation/untraceability (OWASP T8).* → [OWASP Agentic AI Threats & Mitigations](other/owasp-agentic-ai-threats-and-mitigations.md).
- **Policy-as-code authorization (out-of-model PDP)** — a Policy Decision Point authorizes each tool/MCP call against declarative policy (Rego/Cedar), enforced outside the model. *Mitigates tool misuse & privilege abuse (OWASP T2/T3, ASI02/ASI03).* → [Permit.io MCP Gateway](other/permit-io-mcp-gateway-agent-authorization.md), [Microsoft Agent Governance Toolkit](microsoft-github/agent-governance-toolkit-opa-cedar-policy-backends.md), [OPA/OPAL](other/open-policy-agent-opa-rego-and-opal.md), [Cedar](other/aws-cedar-amazon-verified-permissions.md).
- **Automatic session kill switch / denial-threshold auto-halt** — terminate or escalate to a human after N permission denials (Claude Code: 3 consecutive / 20 total). *Mitigates a compromised or over-eager agent.* → [Claude auto mode](anthropic/claude-code-auto-mode-model-graded-approvals.md).
- **Session-level checkpoint rollback** — revert agent file edits to a prior checkpoint. *Limits blast radius of bad edits; bounded to model-driven edits, not bash/external changes.* → [Claude Code checkpointing & /rewind](anthropic/claude-code-checkpointing-and-rewind.md).
- **Agent identity & credential revocation** — disable the agent identity or rotate its service-account keys to cut off a misbehaving agent. *Mitigates credential compromise & lateral movement (OWASP T3 / ASI03).* → [Entra Agent ID](microsoft-github/entra-agent-id-governance-and-kill-switch.md), [Google SCC](google/scc-agent-credential-detection-and-revocation.md).
- **Runtime pre-tool-call interception** — inspect and block unsafe tool calls before they execute. *Runtime defense-in-depth beyond static permissions.* → [Microsoft Defender agent runtime protection](microsoft-github/defender-ai-agent-runtime-protection.md).

### Research-stage (single preprint — medium confidence)

- [AAGATE: a NIST AI RMF-aligned agentic governance platform](other/aagate-nist-rmf-aligned-agentic-governance-platform.md) — framework-per-RMF-function governance, **guardrails-as-code** (NL→Rego/OPA), and the **Janus shadow-monitor** plan-then-execute pattern (continuous in-loop red teaming). Describes design, not measured efficacy.
- [Guardrails-as-code: natural-language → Rego/policy (research)](other/guardrails-as-code-nl-to-rego-research.md) — ARPaCCino (NL→Rego for IaC), Apple **Prose2Policy** (95.3% compile rate on the ACRE access-control set), and **Policy-as-Prompt**. NL→policy is feasible and quantified but still research-stage and aimed at IaC/access-control, not coding-agent action policies.

---

## Gaps identified during research

From the two June 2026 research passes — mirroring the [context-engineering](../context-engineering/index.md#patterns) treatment of weak-evidence material, nothing is silently dropped. Items here either failed adversarial verification, surfaced only as editorial framing (catalogued from primary sources but not independently verified this pass), or mark categories the passes under-covered.

### Refuted / excluded by verification

- **Daytona "complete per-sandbox isolation (dedicated kernel)" claim** — refuted 0-3; the strong-isolation wording was unsupported. (Daytona may still be a valid sandbox option; only the specific claim failed.)
- **LlamaFirewall indirect/obfuscated-injection-in-tool-responses coverage** — refuted 1-2; do not overstate its indirect-injection coverage beyond the verified PromptGuard 2 / Agent Alignment / CodeShield guardrails.

### Surfaced but not verified this pass (catalogued from primary sources; promote after a verification pass)

The well-known prompt-injection design patterns appeared in the corpus only as editorial framing, so they were sourced separately rather than as verified findings:

- [Design patterns for securing LLM agents (lethal trifecta, dual-LLM, plan-then-execute)](other/design-patterns-for-securing-llm-agents-against-prompt-injection.md) — Simon Willison.
- [CaMeL: defeating prompt injections by design](other/camel-defeating-prompt-injections-by-design.md) — arXiv (Google DeepMind et al.).

### Thinly-covered tool categories (candidates for a follow-up pass)

The Tools pass returned little verified coverage here; these surfaced as primary sources but were not run through verification — treat as leads, not endorsements:

- ~~**Policy-as-code guardrail engines**~~ — **now covered** (June 2026 deep-research pass): see [Tools › Policy-as-code & runtime guardrail engines](#policy-as-code-runtime-guardrail-engines). OPA/Rego, Cedar, Permit.io MCP Gateway, the Microsoft Agent Governance Toolkit, NeMo Guardrails, and Guardrails AI are catalogued and verified; TrueFoundry OPA Guardrails surfaced as a single-vendor source (medium confidence, not promoted).
- **Audit logging / tracing / observability of agent actions:** OpenTelemetry GenAI semantic conventions, Langfuse, MLflow Tracing, Arize Phoenix, Helicone. *(Note: the [evals-observability theme](../evals-observability/index.md) covers the observability/tracing tooling in depth.)*
- **Supply-chain / SAST / SCA for AI-generated code:** Semgrep, Socket, GitHub Copilot Autofix (code scanning), OSV-Scanner, Snyk Code, CodeQL, Endor Labs.

### Open questions

- How do the new OWASP **Top 10 for Agentic Applications (ASI01–ASI10)** map item-by-item onto the older **15-threat T1–T15** taxonomy?
- What concrete network-egress-restriction and ephemeral-workspace mechanisms (default-deny egress, throwaway containers) do major coding agents actually enforce, beyond the general sandboxing claims?
- ~~Are guardrails-as-code (NL→Rego/OPA) deployed in any **production** coding agent, or research-only?~~ **Answered (June 2026):** NL→Rego (ARPaCCino, [Prose2Policy](other/guardrails-as-code-nl-to-rego-research.md)) is **still research-only** and aimed at IaC/access-control; the underlying engines (OPA/Cedar) are production-ready and now reached into the agent path by Permit.io / the Microsoft toolkit with **hand-written** policy. (Shadow-monitor/plan-shadowing remains research-only — AAGATE.)
- Is the former Invariant Labs **MCP-Scan** OSS (Apache-2.0) status stable post-Snyk-acquisition, or moving toward a proprietary product?
- **Incident response (partially answered, June 2026):** all four vendors ship kill-switch / revoke / blast-radius controls (see [Tools › Incident response](#incident-response-kill-switches-rollback)), but two gaps remain — (a) **no NIST AI RMF / OWASP Agentic primary-source guidance** on agent incident response surfaced (the CSA "Agentic NIST AI RMF Profile" is a lead), and (b) **rollback of destructive bash-driven or already-merged changes** has no agent-native answer; do *halt* (kill switch) and *revoke* (credential rotation) compose automatically, or are they separate manual steps with an exploitable latency gap?

---

## Recommendations (reading path)
1. **Start with primitives & guardrails:** Anthropic *Security – Claude Code Docs* + *sandboxing* blog, and GitHub *coding-agent firewall* docs — these define the concrete permission/allowlist/sandbox models you can enforce today.
2. **Adopt a governance framework:** Google *SAIF 2.0 / Focus on Agents* + the *Implementing SAIF Controls* PDF, and Microsoft's *Agentic AI maturity model* + *Cloud Adoption Framework* governance pages.
3. **Operationalize safe autonomy in engineering practice:** Martin Fowler/Böckeler *Harness engineering* + *sensors* articles and Thoughtworks *VibeSec reckoning* — translate "tell the model to be safe" into deterministic gates.
4. **For PCI/regulated environments:** Read the two PCI SSC blogs first, retrieve the *Integrating AI in PCI Assessments* Guidelines v1.0 from the Document Library, then use the Fieldguide/VGS practitioner pieces to map controls to Requirements 3, 6, 7, and 10. Treat any AI coding agent with access to the cardholder data environment as **in-scope** and apply least privilege (Req. 7), secure development (Req. 6), data protection (Req. 3), and logging/auditability (Req. 10).
5. **Benchmarks that would change your approach:** If your agents gain write access to CI/CD, branch protections, or production secrets, escalate to org-enforced firewalls/managed settings, mandatory pre-commit security gates, and runtime monitoring (see the Microsoft CI/CD Claude Code GitHub Action case and StepSecurity Harden-Runner pattern referenced in Microsoft/GitHub materials).

---

## Caveats
- **Recency/versioning:** Several vendor pages are living docs (Claude Code settings, GitHub firewall docs, Microsoft Copilot Studio) and change frequently; verify version/date at time of use. The "latest" Thoughtworks Radar URL resolves to the current volume.
- **PCI guidance document access:** The official *Integrating AI in PCI Assessments* Guidelines v1.0 PDF is publicly available but is most reliably reached through the PCI SSC Document Library (the direct PDF path on the PCI SSC document-hosting subdomain is consistent with their scheme but was not independently fetch-verified); avoid third-party mirrors when an audit trail matters.
- **PCI DSS scope nuance:** PCI DSS v4.0.1 text does not name "AI" explicitly; the in-scope determination comes from the PCI SSC's AI principles/guidance plus standard scoping logic. The PCI SSC's *Integrating AI in PCI Assessments* doc concerns assessors' use of AI, which is distinct from securing AI coding agents that handle cardholder data — read both lenses.
- **Non-primary sources:** A few links (e.g., the Thoughtworks Agent/works press release on PR Newswire, arXiv preprints) are included for substance but are not peer-reviewed or are promotional; weight them accordingly. All five named sources you requested are represented with first-party links wherever they exist.
- **Tools & Patterns catalogs (June 2026 research):** entries in the [Tools](#tools) and [Patterns / Techniques](#patterns-techniques) sections were compiled by adversarially-verified research passes and **hand-catalogued** (annotations and excerpts written by hand, not auto-fetched/archived) — verify each source's current name, version, and license before relying on it. The agent-security space churns fast (e.g. MCP-Scan → Snyk "Agent Scan"; Claude Code auto mode is a research preview, with Anthropic disclosing ~17% of over-eager actions slip through).
- **Policy-as-code & incident-response passes (June 2026):** the [policy-as-code engines](#policy-as-code-runtime-guardrail-engines) and [incident-response](#incident-response-kill-switches-rollback) catalogs rest largely on **first-party vendor documentation** describing the vendors' own products (authoritative for behavior, not independent of vendor framing). Two specifics worth weighting: most **Microsoft** agent-IR capabilities (Defender runtime protection, Entra Agent ID disable/expiry, Conditional Access) are **Preview** as of June 2026 with Agent 365 / Entra Suite licensing and a 1 July 2026 subscription requirement for Defender cloud-agent blocking; and agent-coding-*specific* production evidence for the policy-as-code engines is thin and mostly vendor-asserted rather than independently benchmarked.
- **Open-core / vendor nuance:** several "open-source" tools are open-core or have proprietary hosted counterparts (E2B's repo is Apache-2.0 but its cloud is proprietary; Infisical Agent Vault is MIT except its `ee` directory; Auth0, Arcade, and HashiCorp Vault are commercial). Vendor product pages are marketing language accepted as descriptive, not as benchmarked efficacy.
