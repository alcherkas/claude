---
title: Security, Governance & Safe Autonomy for AI Coding Agents
---

# Security, Governance & Safe Autonomy for AI Coding Agents


## TL;DR
- This is a link-first reference delivering the most authoritative and recent materials from **Anthropic, Google, Microsoft/GitHub, Thoughtworks, and Martin Fowler** on securing and governing AI coding agents across the SDLC, plus dedicated **PCI DSS** sources.
- The strongest primary sources are vendor docs and engineering blogs (Anthropic Claude Code security, sandboxing & managed settings; GitHub coding-agent firewall docs; Microsoft Learn agent governance; Google **SAIF 2.0**, announced October 2025 with an agent risk map, alongside CodeMender and the new AI Vulnerability Reward Program) plus Thoughtworks/Martin Fowler "harness engineering."
- For PCI DSS, the authoritative primary sources are the **PCI SSC's 2025 AI principles** and **AI-in-assessments guidance**; AI tools touching cardholder data are in-scope under **PCI DSS v4.0.1** (published 11 June 2024 as a limited revision with no added/deleted requirements; v4.0 retired 31 December 2024; future-dated v4.0 requirements became mandatory 31 March 2025).

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
- [The 2025 AI Agent Index](other/the-2025-ai-agent-index.md) — arXiv; documents safety/guardrail disclosures across deployed agents incl. Claude Code, Copilot, Gemini
- [SCGAgent: Recreating the Benefits of Reasoning Models for Secure Code Generation with Agentic Workflows](other/scgagent-recreating-the-benefits-of-reasoning-models-for-secure-code-generation.md) — arXiv
- [Are AI-assisted Development Tools Immune to Prompt Injection?](other/are-ai-assisted-development-tools-immune-to-prompt-injection.md) — arXiv survey of agentic coding editor injection attacks

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
