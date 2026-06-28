---
title: Evaluations, Observability & Quality Gates
---

# Evaluations, Observability & Quality Gates


**TL;DR**
- The strongest single primary sources are Anthropic's "Demystifying evals for AI agents" (Jan 2026), Martin Fowler's "Exploring Gen AI" series (especially Böckeler's *Harness Engineering* and Doernenburg's internal-quality memo), ThoughtWorks' Technology Radar entries (Feedback sensors, Complacency, LLM-as-a-judge), Google's Gemini Enterprise Agent Platform + ADK eval/observability, and Microsoft Foundry's tracing/evals plus GitHub Copilot code review.
- Links are grouped by source (Anthropic, Martin Fowler, ThoughtWorks, Google, Microsoft/GitHub), then by subtopic (Evaluations / Observability / Quality gates), prioritizing 2024–2026 material.
- All five named sources are well covered; a short list of high-value adjacent primary sources (Eval-Driven Development, OpenTelemetry GenAI conventions, SWE-bench Verified) appears at the end.
- Two cross-cutting categories follow the source list: **Tools** (concrete eval/observability/quality-gate products) and **Patterns / Techniques** (named methods), both populated from two adversarially-verified research passes (June 2026), with a **Gaps** section recording what those passes could *not* substantiate.

---

## ANTHROPIC (anthropic.com / docs.claude.com)

### Evaluations
- [Demystifying evals for AI agents](anthropic/demystifying-evals-for-ai-agents.md) — Jan 2026; multi-turn evals, graders, transcripts, agent harness, eval-as-regression-sentinel
- [Building Effective AI Agents](../security-governance/anthropic/building-effective-ai-agents.md) — start simple, evaluate, add complexity only when needed
- [Skill authoring best practices](anthropic/skill-authoring-best-practices.md) — create evaluations *before* writing docs; baseline-and-iterate

### Observability / multi-agent
- [How we built our multi-agent research system — observability, eval design, and prompt engineering; reports the orchestrator-plus-subagent design (Claude Opus 4 lead + Claude Sonnet 4 subagents) outperformed single-agent Claude Opus 4 by 90.2% on Anthropic's internal research eval, that multi-agent systems use ~15× more tokens than chat, and that token usage explained ~80% of performance variance on their BrowseComp evals](../orchestration/anthropic/how-we-built-our-multi-agent-research-system.md)

### Quality gates
- [Best practices for Claude Code](../spec-driven/anthropic/best-practices.md) — verification via `/code-review` subagent, `/goal` conditions, Stop hooks, Writer/Reviewer pattern
- [Claude Code GitHub Actions](anthropic/claude-code-github-actions.md) — CI automation, PR review, structured JSON outputs
- [anthropics/claude-code-action](anthropic/anthropics-claude-code-action.md) — official GitHub Action; code review, security review, test generation recipes

---

## MARTIN FOWLER (martinfowler.com)

### Evaluations
- [Emerging Patterns in Building GenAI Products (Evals, Scoring & Judging, Evals & Benchmarking, Guardrails, LLM-as-judge vs. self-evaluation) — by Bird, Joshi, Parsons, Fowler et al.](../orchestration/martin-fowler/emerging-patterns-in-building-genai-products.md)

### Quality gates / harness engineering
- [Exploring Generative AI](../context-engineering/martin-fowler/exploring-generative-ai.md) — series index — entry point to all memos below
- [Harness engineering for coding agent users — Birgitta Böckeler (Apr 2026); the "cybernetic governor" mental model combining feed-forward and feedback to regulate codebases](../context-engineering/martin-fowler/harness-engineering-for-coding-agent-users.md)
- [Context Engineering for Coding Agents — Böckeler](../context-engineering/martin-fowler/context-engineering-for-coding-agents.md) — Feb 2026
- [Assessing internal quality while coding with an agent — Erik Doernenburg (Jan 2026); non-functional/internal quality of agent-generated code](../context-engineering/martin-fowler/assessing-internal-quality-while-coding-with-an-agent.md)
- [Autonomous coding agents: A Codex example — Böckeler](martin-fowler/autonomous-coding-agents-a-codex-example-bockeler.md) — detailed agent-run logs

---

## THOUGHTWORKS (thoughtworks.com — Insights & Technology Radar)

### Evaluations
- [LLM benchmarks, evals and tests: A mental model](thoughtworks/llm-benchmarks-evals-and-tests-a-mental-model.md) — benchmarks = comparison, evals = system behavior, tests = validation
- [How to evaluate an LLM system](thoughtworks/how-to-evaluate-an-llm-system.md) — shift-left/evals-driven approach, LLM-as-judge, ground-truth metrics
- [AI testing, benchmarks and evals](thoughtworks/ai-testing-benchmarks-and-evals.md) — podcast; benchmarks vs. evals vs. tests, guardrails, LLM-as-judge trust
- [AI evals for MCP in AIOps](thoughtworks/ai-evals-for-mcp-in-aiops.md) — embedding evals in agent architectures; LLM-as-judge + expert review
- [LLM Evaluations](thoughtworks/llm-evaluations.md) — service overview; PoC-to-production feedback loops

### Quality gates / Technology Radar
- [Technology Radar](../security-governance/thoughtworks/technology-radar.md) — landing
- [Feedback sensors for coding agents](thoughtworks/feedback-sensors-for-coding-agents.md) — deterministic quality gates wired into agent loops for auto-correction
- [Complacency with AI-generated code](thoughtworks/complacency-with-ai-generated-code.md) — GitClear churn data; risks of large agent change-sets
- [Measuring collaboration quality with coding agents](thoughtworks/measuring-collaboration-quality-with-coding-agents.md) — first-pass acceptance, iteration cycles, post-merge rework
- [LLM as a judge](thoughtworks/llm-as-a-judge.md) — Radar technique, moved Trial→Assess due to bias/contamination risks
- [Techniques quadrant](../context-engineering/thoughtworks/techniques-technology-radar.md) — architecture drift reduction with LLMs + deterministic tools
- [Tools quadrant](../orchestration/thoughtworks/technology-radar-tools.md) — Claude Code, Codex, mutation/fuzz/quality tools as feedback sensors
- [What is harness engineering?](../security-governance/thoughtworks/what-is-harness-engineering.md) — podcast with Böckeler
- [Tech Radar vol. 33 news](../orchestration/thoughtworks/technology-radar-vol-33-news.md) — AI coding workflows, AI antipatterns, MCP

---

## GOOGLE (Cloud, Research, DeepMind, Developers)

### Evaluations
- [Introducing Gemini Enterprise Agent Platform](../orchestration/google/introducing-gemini-enterprise-agent-platform.md) — Agent Simulation, Agent Evaluation with multi-turn autoraters, Agent Observability, Agent Optimizer
- [More ways to build and scale AI agents with Vertex AI Agent Builder](google/more-ways-to-build-and-scale-ai-agents-with-vertex-ai-agent-builder.md) — new Evaluation Layer + User Simulator; observability dashboard
- [Evaluate agents](google/evaluate-agents-adk.md) — official ADK trajectory/tool-use and final-response evaluation guide
- [Announcing User Simulation in ADK Evaluation](google/announcing-user-simulation-in-adk-evaluation.md) — LLM-powered multi-turn user simulator for goal-oriented agent eval
- [Agent Development Kit: making it easy to build multi-agent applications](../orchestration/google/agent-development-kit-announcement.md) — built-in eval framework: trajectory + response scoring, `adk eval`
- [AI in software engineering at Google: progress and the path ahead](google/ai-in-software-engineering-at-google-progress-and-the-path-ahead.md) — internal AI-SWE measurement and direction
- [AI-assisted Assessment of Coding Practices in Industrial Code Review](google/ai-assisted-assessment-of-coding-practices-in-industrial-code-review.md) — AutoCommenter, arXiv:2405.13565; deployed to all Google developers July 2022–Oct 2023 across C++/Java/Python/Go — its best-practice URL set "covers 68% of historical human comments," and filtering comments on unchanged lines cut the comment ratio in changed files to 1.3%

### Observability
- [Vertex AI Agent Engine overview](google/vertex-ai-agent-engine-overview.md) — Cloud Trace with OpenTelemetry, Cloud Monitoring, Cloud Logging; integrated Gen AI Evaluation service
- [Agents CLI in Agent Platform: create to production in one CLI](google/agents-cli-in-agent-platform-create-to-production-in-one-cli.md) — `agents-cli eval run` LLM-as-judge harness, `eval compare` for regressions, observability skill
- [I/O '26 news for agent developers on Google Cloud](google/i-o-26-news-for-agent-developers-on-google-cloud.md) — eval suite, synthetic user simulation, autoraters, trace logging
- [Vibe Coding AI Agents: Managing the Agent Lifecycle with Agents CLI and ADK 2.0](google/vibe-coding-ai-agents-managing-the-agent-lifecycle-with-agents-cli-and-adk-2-0.md) — Codelab; lint → eval → observability

### Quality gates / DeepMind
- [Introducing CodeMender: an AI agent for code security](../security-governance/google/introducing-codemender-an-ai-agent-for-code-security.md) — uses an LLM-based critique/judge tool to validate patches and avoid regressions before human review
- [AlphaEvolve: a Gemini-powered coding agent](google/alphaevolve-a-gemini-powered-coding-agent.md) — pairs Gemini with automated evaluators that verify/score candidate programs
- [google/agents-cli](google/google-agents-cli.md) — CLI + skills for scaffold, ADK code, eval, deploy, observability, publish

---

## MICROSOFT / GITHUB

### Evaluations
- [Announcing public preview of the Microsoft 365 Copilot Agent Evaluations tool](microsoft-github/announcing-public-preview-of-the-microsoft-365-copilot-agent-evaluations-tool.md) — LLM-judge evaluators, HTML scorecards usable in inner-loop, code review, and CI/CD
- [Build agents you can trust across any framework with open evals and a control standard](microsoft-github/build-agents-you-can-trust-across-any-framework-with-open-evals-and-a-control.md) — ASSERT policy-driven eval, Rubric evaluator, Agent Control Specification, Guided Guardrail Setup
- [What's new in Microsoft Foundry | Build Edition](microsoft-github/what-s-new-in-microsoft-foundry-build-edition.md) — ASSERT, ACS, Rubric, tracing, Agent Optimizer, tracing/evals for any framework

### Observability
- [Observability in Generative AI - Microsoft Foundry](microsoft-github/observability-in-generative-ai-microsoft-foundry.md) — Microsoft Learn concept page for traces, monitoring, evals, and CI/CD quality gates
- [Observability for AI Systems: Strengthening visibility](microsoft-github/observability-for-ai-systems-strengthening-visibility.md) — Microsoft Security Blog, Mar 2026; observability as a release requirement, trust-boundary violations
- [Observability for Generative AI and agentic AI systems](microsoft-github/observability-for-generative-ai-and-agentic-ai-systems.md) — Microsoft Learn / Secure Future Initiative; AI-native logs/metrics/traces, OTel standardization
- [Designing AI-Driven Observability for Trustworthy Agentic AI Systems](microsoft-github/designing-ai-driven-observability-for-trustworthy-agentic-ai-systems.md) — non-deterministic execution, LLM-as-judge evaluators, runaway-loop cost control
- [Agent Factory: Top 5 agent observability best practices for reliable AI](microsoft-github/agent-factory-top-5-agent-observability-best-practices-for-reliable-ai.md) — tracing + evaluation + governance across the lifecycle
- [Build and run agents at scale with Microsoft Foundry at Build 2026](microsoft-github/build-and-run-agents-at-scale-with-microsoft-foundry-at-build-2026.md) — single OpenTelemetry pipeline; tracing GA; score-to-trace debugging
- [Build 2026: From observability to ROI for AI agents on any framework](microsoft-github/build-2026-from-observability-to-roi-for-ai-agents-on-any-framework.md) — Trace/Evaluate/Monitor/Optimize loop; AZD observability dev experience

### Quality gates (GitHub Copilot / SDLC)
- [About GitHub Copilot code review](microsoft-github/about-github-copilot-code-review.md) — review effort levels, agentic context gathering, agentic capabilities
- [Using GitHub Copilot code review](microsoft-github/using-github-copilot-code-review.md) — `copilot-instructions.md`, agent skills, MCP servers
- [Copilot code review now runs on an agentic architecture](microsoft-github/copilot-code-review-now-runs-on-an-agentic-architecture.md) — Mar 2026 changelog
- [60 million Copilot code reviews and counting — continuous-evaluation loop tuned on accuracy/signal/speed; usage has "grown 10X" since the April 2025 launch, "now accounting for more than one in five code reviews on GitHub," surfaces actionable feedback in 71% of reviews (~5.1 comments/review), and the agentic-architecture shift alone drove an initial 8.1% increase in positive feedback](microsoft-github/60-million-copilot-code-reviews-and-counting-continuous-evaluation-loop-tuned-on.md)
- [GitHub Copilot: Meet the new coding agent — branch protections still apply; agent PRs require human approval before CI/CD runs.](../spec-driven/microsoft-github/github-copilot-meet-the-new-coding-agent.md) — Microsoft's .NET Blog retrospective on dotnet/runtime reports the agent created 878 PRs and merged 535, a 67.9% success rate vs. 87.1% for human PRs, with only 3 of 535 merged PRs reverted — 0.6% vs. a 0.8% human baseline.
- [What's new with GitHub Copilot coding agent](microsoft-github/what-s-new-with-github-copilot-coding-agent.md) — self-review via Copilot code review before opening PR, built-in code/secret/dependency scanning, custom agents
- [GitHub Copilot app: the agent-native desktop experience](../spec-driven/microsoft-github/github-copilot-app-the-agent-native-desktop-experience.md) — `/security-review`, `/rubberduck`, Copilot SDK
- [Agentic Platform Engineering with GitHub Copilot](microsoft-github/agentic-platform-engineering-with-github-copilot.md) — Azure DevBlogs; observe → diagnose → remediate, Copilot CLI in CI/CD
- [DevOps Playbook for the Agentic Era](microsoft-github/devops-playbook-for-the-agentic-era.md) — pipelines as verifiers not gatekeepers; spec-driven development; agentic maturity model
- [microsoft/agentic-sdlc-starter](microsoft-github/microsoft-agentic-sdlc-starter.md) — spec-driven AI SDLC reference architecture; assessor → specifier → generator → validator gates
- [Agentic DevOps: Evolving software development with GitHub Copilot and Microsoft Azure](microsoft-github/agentic-devops-evolving-software-development-with-github-copilot-and-microsoft.md) — agentic DevOps overview; agents across the full lifecycle

### GitHub Next (githubnext.com)
- [TestPilot](microsoft-github/testpilot.md) — Copilot-model-driven unit-test generation with meaningful assertions; measures statement coverage
- [Copilot Workspace](microsoft-github/copilot-workspace.md) — plan/brainstorm/repair agents; discusses the "evaluation cost" of large AI-generated changes; repair agent fixes failing tests
- [Copilot for Pull Requests](microsoft-github/copilot-for-pull-requests.md) — AI test-gap detection / "GenTest", AI-powered PR repair for missing tests, docs, linter errors

---

## ADJACENT PRIMARY SOURCES (not among the five named, but high-value)
- [Eval-Driven Development](other/eval-driven-development.md) — manifesto/landing site; "build evals first; code is generated, evals are engineered"
- [OpenTelemetry GenAI semantic conventions](other/opentelemetry-genai-semantic-conventions.md) — vendor-neutral `gen_ai.*` agent telemetry standard referenced across Google, Microsoft, Databricks, and LangChain agent-observability tooling
- [OpenTelemetry GenAI semantic conventions repository](other/opentelemetry-semantic-conventions-genai-repository.md) — primary OpenTelemetry repo for GenAI spans, metrics, and events
- [LangSmith evaluation concepts](other/langsmith-evaluation-concepts.md) — offline/online eval concepts for curated datasets and production traces
- [Langfuse Observability and Application Tracing](other/langfuse-observability-application-tracing.md) — traces for prompts, responses, tool calls, cost, latency, and nested steps
- [Langfuse Evaluation Overview](other/langfuse-evaluation-overview.md) — datasets, experiments, automated evaluators, scores, and CI/CD eval workflows
- [OpenAI Evals guide](other/openai-evals-guide.md) — eval construction concepts, data sources, and graders
- [openai/evals](other/openai-evals-repository.md) — primary OpenAI eval repository and historical framework material
- [SWE-bench: Can Language Models Resolve Real-World GitHub Issues?](other/swe-bench-paper-openreview.md) — peer-reviewed ICLR 2024 benchmark paper
- [SWE-bench repository](other/swe-bench-repository.md) — primary benchmark repository
- [Introducing SWE-bench Verified (OpenAI, Aug 13, 2024) — the standard coding-agent benchmark used by all major labs; a 500-sample human-verified subset of the original 2,294-instance SWE-bench across 12 Python repositories](other/introducing-swe-bench-verified-openai-aug-13-2024-the-standard-coding-agent-used.md)
- [Ragas available metrics](other/ragas-available-metrics.md) — concrete RAG, agent/tool-use, factuality, semantic similarity, rubric, and exact-match metrics

---

## Tools
Concrete eval/observability/quality-gate products, from two adversarially-verified research passes (June 2026). Most are **general LLM/agent platforms** adaptable to coding agents; the few genuinely coding-agent-targeted signals are flagged. (Vendor-native eval/observability — Google Vertex/ADK, Microsoft Foundry, GitHub Copilot — lives in the source sections above.)

### Evaluation frameworks & harnesses
- [DeepEval](other/deepeval.md) — Apache-2.0, Pytest-style; 50+ metrics (G-Eval/DAG LLM-judge, RAG, agentic/trajectory); `deepeval test run` CI gates; **MCP server for Claude Code/Cursor** *(coding-agent-targeted)*
- [Ragas available metrics](other/ragas-available-metrics.md) — open-source RAG/agent eval; AWS uses it to evaluate Bedrock Agents *(existing page)*
- [Promptfoo](other/promptfoo.md) — MIT eval runner; CLI + GitHub Action *(also a CI quality gate, below)*
- [Braintrust eval-action](other/braintrust-eval-action.md) — official GitHub Action; PR-comment regression surfacing *(also a CI quality gate)*
- [OpenAI Evals guide](other/openai-evals-guide.md) · [openai/evals](other/openai-evals-repository.md) — eval construction concepts + repo *(existing pages; not re-verified this pass)*

### Observability & tracing
- [Inside the LLM Call: GenAI Observability with OpenTelemetry](other/opentelemetry-genai-observability.md) — vendor-neutral span model; **VS Code Copilot, OpenAI Codex, and Claude Code emit OTel GenAI telemetry** *(coding-agent-targeted)*
- [OpenTelemetry GenAI semantic conventions](other/opentelemetry-genai-semantic-conventions.md) · [repository](other/opentelemetry-semantic-conventions-genai-repository.md) — the standardized `gen_ai.*` attribute/metric backbone *(existing pages)*
- [Datadog LLM Observability](other/datadog-llm-observability.md) — commercial; native OTel GenAI semconv (v1.37+), OTLP ingest
- [OpenLLMetry (Traceloop)](other/openllmetry.md) — Apache-2.0 OTel instrumentation emitting `gen_ai.*` spans
- [LangSmith observability](other/langsmith-observability.md) — commercial; framework-agnostic agent tracing + OTel · [Langfuse tracing](other/langfuse-observability-application-tracing.md) *(existing)*

### Quality gates & guardrails (CI/CD)
- [Promptfoo](other/promptfoo.md) — fail-on-threshold / pass-rate gates across GitHub Actions, GitLab CI, Jenkins, Azure Pipelines, CircleCI, Bitbucket, Travis
- [Braintrust eval-action](other/braintrust-eval-action.md) — runs `braintrust eval` in CI, posts a Score/Improvements/Regressions PR comment
- [Guardrails AI](other/guardrails-ai.md) — Apache-2.0 Input/Output Guards; runtime validation
- [NVIDIA NeMo Guardrails](other/nemo-guardrails.md) — Apache-2.0 programmable rails (Colang) *(surfaced in search; not verified this pass)*

## Patterns / Techniques
Named methods for evaluating, observing, and gating agentic systems — framed for context engineering (curating/measuring the context an agent works from). Grouped by evidence strength.

### Well-evidenced (vendor-primary + peer-reviewed)
- **Eval-driven (evaluation-first) development; capability-vs-regression split** — build evals before the agent can pass them; capability evals start low and "graduate" into a continuously-run regression suite. [Demystifying evals for AI agents](anthropic/demystifying-evals-for-ai-agents.md) · [Eval-Driven Development](other/eval-driven-development.md)
- **Grade the outcome/end-state, not the exact path** (use trajectory eval for *diagnosis*) — [Vertex AI agent evaluation](google/introducing-agent-evaluation-in-vertex-ai-gen-ai-evaluation-service.md) (final-response vs. trajectory metrics) · [LangSmith trajectory evals](other/langsmith-trajectory-evals.md) · [Foundry practical guide](microsoft-github/evaluating-ai-agents-a-practical-guide-with-microsoft-foundry.md)
- **Two-loop offline/online evaluation; continuous production eval** — inner pre-ship loop (dev + CI) / outer post-ship loop (live-traffic sampling + scheduled golden runs). [Foundry practical guide](microsoft-github/evaluating-ai-agents-a-practical-guide-with-microsoft-foundry.md) · [Continuous evaluation of agents](microsoft-github/continuous-evaluation-of-agents-microsoft-foundry.md)
- **CI quality gates that block merges on eval regressions** — failing eval blocks the merge, with significance testing to separate regressions from noise. [Foundry practical guide](microsoft-github/evaluating-ai-agents-a-practical-guide-with-microsoft-foundry.md) · [Feedback sensors for coding agents](thoughtworks/feedback-sensors-for-coding-agents.md)
- **Trace/transcript inspection; eval linked to traces** — "read the transcripts"; production eval scores link straight to the trace for root-cause. [Continuous evaluation of agents](microsoft-github/continuous-evaluation-of-agents-microsoft-foundry.md) · [Demystifying evals](anthropic/demystifying-evals-for-ai-agents.md)
- **LLM-as-a-judge as a formal paradigm** (E ← P_LLM(x ⊕ C)) — [A Survey on LLM-as-a-Judge](other/a-survey-on-llm-as-a-judge.md) · [LLM as a judge (ThoughtWorks Radar, Trial→Assess)](thoughtworks/llm-as-a-judge.md)
- **Quantified LLM-judge biases & mitigations** — twelve bias types; position bias affects all judges; mitigate via human calibration and order-swapping. [CALM: Quantifying Biases in LLM-as-a-Judge](other/calm-quantifying-biases-in-llm-as-a-judge.md) · [Judging the Judges](other/judging-the-judges.md)
- **Evaluating context quality itself** — "lost in the middle" (U-shaped utilization) and "context rot" (degradation with input length / lower query-target similarity); focused context beats full. [Lost in the Middle](../context-engineering/other/lost-in-the-middle-how-language-models-use-long-contexts.md) · [Context Rot](other/context-rot.md) *(vendor-primary; see caveat)*
- **Maintainability / feedback "sensors" for coding agents** — deterministic quality gates wired into the agent loop for self-correction. [Maintainability sensors for coding agents](../context-engineering/martin-fowler/maintainability-sensors-for-coding-agents.md) · [Feedback sensors](thoughtworks/feedback-sensors-for-coding-agents.md)

## Gaps
What the June 2026 research passes searched for but could **not** substantiate with a verified primary source — recorded for honesty, mirroring the context-engineering topic's evidence discipline. Absence here reflects the verified-claim corpus, not evidence these are unimportant.

- **RAG-specific metrics** (faithfulness, groundedness, context precision/recall in the RAGAS sense) — requested but no claim survived verification in the Patterns pass; partially covered by the existing [Ragas metrics](other/ragas-available-metrics.md) page. (Vertex's precision/recall are *tool-trajectory*, not retrieval, metrics.)
- **Guardrails / validation-assertion gates** and **dedicated human-in-the-loop review gates** (beyond LLM-judge calibration) — not substantiated as distinct, well-evidenced *patterns* this pass, though [Guardrails AI](other/guardrails-ai.md) / [NeMo Guardrails](other/nemo-guardrails.md) cover the tooling.
- **OpenTelemetry GenAI semantic conventions are still evolving** — several pages remain "Development/experimental," the spec relocated to a dedicated `semantic-conventions-genai` repo, and token attribute names are mid-rename (`prompt_tokens`→`usage.input_tokens`).
- **Named-but-unverified tools** (real, relevant, but no verified claim surfaced this pass): **Arize Phoenix, Langfuse\*, Helicone, W&B Weave, Galileo, Patronus, Inspect (UK AISI), OpenAI Evals\***. *(\*Langfuse and OpenAI Evals already have pages above.)* A dedicated follow-up research pass on these is in progress.
- **Refuted in verification** (for transparency): the Ragas↔Bedrock LLM-as-judge *integration mechanism* (1–2 vote) and the claim that *Traceloop leads the OTel LLM semantic-convention working group* (0–3).
- **Coding-agent-specific vs. general:** only OTel telemetry emission (VS Code Copilot / OpenAI Codex / Claude Code) and DeepEval's MCP server were confirmed genuinely coding-agent-targeted; everything else is general LLM/agent tooling adaptable to coding agents.

---

## Recommendations (how to use this list)
- **Start here for the conceptual frame:** Read ThoughtWorks' "LLM benchmarks, evals and tests: A mental model" + Anthropic's "Demystifying evals for AI agents" together — they establish the vocabulary (benchmark vs. eval vs. test; task vs. trial vs. transcript vs. outcome) you will reuse everywhere.
- **For SDLC quality gates specifically:** Pair Martin Fowler/Böckeler's "Harness engineering for coding agent users" with ThoughtWorks' "Feedback sensors for coding agents" and GitHub's "What's new with Copilot coding agent" (self-review + security scanning) and "60 million Copilot code reviews." These four describe the same pattern — deterministic gates (linters, type checks, tests, scanners) wired into the agent loop, plus an LLM/second-model reviewer — from complementary vendor and practitioner angles.
- **For production observability:** Use the OpenTelemetry GenAI conventions as the neutral baseline, then map to your platform: Google (Vertex AI Agent Engine / Agent Observability), Microsoft (Foundry Trace→Evaluate→Monitor→Optimize loop), and Microsoft's SFI/Security observability guidance for the governance/security overlay.
- **Escalate to a subagent/PoC when:** you need eval-driven CI gating (see Eval-Driven Development + ADK `eval compare` + Microsoft 365 Copilot Agent Evaluations CLI). Adopt these once you have ≥1 recurring agent-failure mode worth a regression dataset.
- **Thresholds that should change your approach:** treat LLM-as-judge with caution (ThoughtWorks moved it Trial→Assess) — require human verification and consider LLM-as-jury/chain-of-thought when judges feed training or release decisions; and re-baseline benchmarks frequently, since saturated/contaminated coding benchmarks (the SWE-bench Verified caveat) can mask real regressions.

## Caveats
- **Recency vs. permanence:** Several ThoughtWorks Radar pages, Google Cloud product pages, and Microsoft Foundry blogs are living/product documents whose contents and "preview vs. GA" status change frequently; verify status at time of reading. Many Foundry capabilities cited (Rubric, Agent Optimizer, tracing for any framework, ROI for agents) were in **public/private preview** as of Build 2026, not GA.
- **Vendor framing:** Google Cloud, Microsoft/GitHub, and Anthropic posts are partly product marketing; the quantitative claims (90.2% multi-agent lift, 67.9% coding-agent merge rate, 71% actionable-review rate, 68% AutoCommenter coverage) come from the vendors' own internal evals and should be read as directional, not independently audited.
- **Benchmark limitations:** SWE-bench Verified remains the de facto coding-agent benchmark, but multiple labs now report training-data contamination and saturation concerns — scores are useful for broad directional comparison only.
- A few cited URLs point to docs/landing indexes (e.g., the Radar techniques/tools quadrants, the Exploring Gen AI series index) that aggregate multiple sub-articles; drill into the linked sub-pages for the specific technique you need.
- **Tools / Patterns evidence basis:** the **Tools** and **Patterns / Techniques** sections come from adversarially-verified research passes (3-vote, June 2026), but evidence quality is mixed: open-source frameworks (DeepEval, Promptfoo, Guardrails AI, Ragas) and the OTel standard rest on primary repos/specs, whereas **LangSmith and Datadog capability claims are vendor self-description** (appropriate for a capability census, not independent verification), and **Context Rot is vendor-primary** (Chroma sells a retrieval product) though its core is corroborated by peer-reviewed lost-in-the-middle work. See **Gaps** for what the passes could not substantiate.
