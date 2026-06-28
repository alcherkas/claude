---
title: Reference architecture — an end-to-end spec-driven & test-driven system
---

# Reference architecture — an end-to-end spec-driven & test-driven system

!!! note "What this page is"
    A **synthesis**, not a single external source. No vendor has published an end-to-end reference architecture for building software the spec-driven way — Böckeler's own survey finds the field is [semantically diffused](martin-fowler/understanding-spec-driven-development-kiro-spec-kit-and-tessl.md) and most tools only reach the shallowest tier. So this page stitches one pipeline together from the primary sources catalogued in this topic — the [Tools](index.md#tools) parts list and the [Patterns & Techniques](index.md#patterns-techniques) catalog — and cross-links the [evals-observability](../evals-observability/index.md) and [security-governance](../security-governance/index.md) topics. The governing principle throughout is **match spec rigor to the task**: every step from a prompt to a full executable spec adds ceremony, so each layer must earn its place. Layers that remain immature are marked **⚠ seam**. Assembled June 2026.

## The pipeline

A spec-driven system is a gated loop, not a waterfall. A **decision** at the top ("how much rigor does this task need?") chooses how deep to go; a **specify → constrain → plan → implement → verify** spine turns intent into code; **review gates** and an **evaluate** feedback loop close it back onto the spec. Most layers are covered by vendor-primary sources; two are genuine seams.

```
  DECIDE   how much rigor?  vibe ─► spec-first ─► spec-anchored ─► spec-as-source ⚠
  ┌──────────────────────────────────────────────────────────────────┐
  │  1 SPECIFY    intent → written spec / requirements                │
  │               Spec Kit /specify · Kiro Specs · OpenSpec           │
  │  2 CONSTRAIN  non-negotiables that ride along every step       ⚠  │
  │               constitution · steering files · SPDD Safeguards ·   │
  │               guardrail/policy engines                            │
  │  3 PLAN       spec → design → discrete tasks                      │
  │               /plan · /tasks · Explore→Plan→Implement             │
  │  4 IMPLEMENT  agent generates code against the plan               │
  │               /implement · Copilot agent mode · Antigravity       │
  │  5 VERIFY     self-closing test/build/lint loop (TDD)             │
  │               "give the agent a check it can run" · feedback      │
  │               sensors · generate-validate-repair                  │
  │  6 REVIEW     human + automated checkpoints before merge          │
  └──────────────────────────────────────────────────────────────────┘
            ▲                                                      │
            └──── EVALUATE: does the code still satisfy the spec?  ┘
                  spec-adherence & long-horizon benchmarks
```

## Layer by layer

| # | Layer | What to use | Maturity |
|---|---|---|---|
| 0 | **Decide rigor** | Böckeler's [spec-first / spec-anchored / spec-as-source](martin-fowler/understanding-spec-driven-development-kiro-spec-kit-and-tessl.md) tiers; [ThoughtWorks Radar](thoughtworks/spec-driven-development.md) places SDD in **Assess**; [Beyond Vibe Coding](other/beyond-vibe-coding-amazon-introduces-kiro-the-spec-driven-agentic-ai-ide.md) for the vibe-vs-spec boundary. | Strong — the most-cited framing in the field |
| 1 | **Specify** | [Spec Kit `/specify`](microsoft-github/spec-kit-agents-md.md) + [spec-template](microsoft-github/spec-kit-spec-template.md); [Kiro Specs](other/kiro-specs.md) (requirements/design/tasks); [OpenSpec](other/openspec-spec-driven-development-for-ai-coding-assistants.md) delta specs; [Design-First Collaboration](martin-fowler/design-first-collaboration.md). | Strong — vendor-primary |
| 2 | **Constrain** | [Spec Kit constitution](microsoft-github/spec-kit-spec-driven-md.md); [Kiro steering files](other/kiro-steering.md); [SPDD Safeguards](martin-fowler/structured-prompt-driven-development.md); runtime [NeMo Guardrails](other/nvidia-nemo-guardrails-programmable-guardrails-for-llm-systems.md) / [Guardrails AI](other/guardrails-ai-validation-framework-for-llm-applications.md). | **⚠ seam** — real in practice, no canonical name (see seam 1) |
| 3 | **Plan / decompose** | [Spec Kit `/plan` + `/tasks`](microsoft-github/spec-kit-agents-md.md); [Kiro design/tasks](other/kiro-specs.md); Anthropic [Explore → Plan → Implement → Commit](anthropic/best-practices.md); [Knowledge Priming](martin-fowler/knowledge-priming.md). | Strong |
| 4 | **Implement** | [Spec Kit `/implement`](microsoft-github/spec-kit-agents-md.md); [Copilot agent mode](microsoft-github/agent-mode-101-all-about-github-copilot-s-powerful-mode.md); [Google Antigravity](google/build-with-google-antigravity-our-new-agentic-development-platform.md); [Conductor for Gemini CLI](../context-engineering/google/conductor-introducing-context-driven-development-for-gemini-cli.md). | Strong |
| 5 | **Verify (TDD)** | Anthropic [self-closing verification loop](anthropic/best-practices.md) ("give the agent a check it can run") — framed as *the single strongest pattern*; [feedback sensors for coding agents](../evals-observability/thoughtworks/feedback-sensors-for-coding-agents.md) (Radar **Trial**); [TDD with Copilot](microsoft-github/github-for-beginners-test-driven-development-tdd-with-github-copilot.md); [generate-validate-repair survey](other/large-language-models-for-unit-test-generation-survey.md); [Qodo-Cover](other/qodo-cover-ai-test-generation-and-coverage-agent.md). | Strong — best-documented layer |
| 6 | **Review gates** | [Effective review gates for AI-assisted development](thoughtworks/how-to-implement-effective-review-gates-for-ai-assisted-development.md); [Casper](thoughtworks/casper-helping-developers-work-effectively-with-coding-assistants.md); [Cultivating trust with AI coding assistants](thoughtworks/how-to-cultivate-trust-with-ai-coding-assistants.md). | Medium — Insights-blog, not a confirmed Radar blip |
| ↺ | **Evaluate (spec adherence)** | Instruction-following ([IFEval](https://arxiv.org/abs/2311.07911), [FollowBench](https://arxiv.org/abs/2310.20410), [ComplexBench](https://arxiv.org/abs/2407.03978)) for *does code obey the spec's constraints*; long-horizon spec-to-code ([SWE-Bench Pro](https://arxiv.org/abs/2509.16941), [FeatBench](https://arxiv.org/abs/2509.22237), [NL2Repo-Bench](https://arxiv.org/abs/2512.12730)); [Constraint Decay](other/constraint-decay-the-fragility-of-llm-agents-in-backend-code-generation.md) for drift; plus the whole [evals-observability topic](../evals-observability/index.md). | Strong on benchmarks; **⚠ no single spec-adherence metric** |
| ▣ | **Govern** | When the spec layer must encode security non-negotiables ([Constitutional SDD](https://arxiv.org/abs/2602.02584)), wire to the [security-governance reference architecture](../security-governance/reference-architecture.md). | Covered there |

## How the canonical implementations map on

Each flagship tool implements a *subset* of the pipeline — which is exactly why no single one is a complete blueprint:

- **[GitHub Spec Kit](microsoft-github/github-spec-kit-repository.md)** — the most complete spine: `/constitution` (2) → `/specify` (1) → `/plan` → `/tasks` (3) → `/implement` (4), with `/clarify`, `/analyze`, `/checklist` added for refinement. Strong on 1–4; verification and review are left to your own harness (5–6).
- **[Amazon Kiro](other/kiro-documentation.md)** — strongest on 1–3 as an IDE: [Specs](other/kiro-specs.md) drive requirements → design → tasks, [steering files](other/kiro-steering.md) carry the constraint layer (2) persistently. Rated *spec-first* — the spec seeds code but does not stay the source of truth.
- **[Google Antigravity](google/build-with-google-antigravity-our-new-agentic-development-platform.md) / [Conductor](../context-engineering/google/conductor-introducing-context-driven-development-for-gemini-cli.md)** — strongest on 4–↺: verification-centric "Artifacts" and a persistent `plan.md` lean into implement + evaluate rather than a native spec/TDD format.
- **Anthropic Claude Code** — strongest on 3–5: [Explore → Plan → Implement → Commit](anthropic/best-practices.md) plus the self-closing verification loop is the cleanest articulation of the TDD half, but it ships no native spec format (the constraint layer lives in `CLAUDE.md` / steering-style files).

## The two seams

1. **The constraint layer (2) is real but has no canonical name.** This is the "Constraints-Driven Development" the topic is named for — and a focused research pass found **no CDD claim survived verification**: it is a [third-party coinage](other/constraint-driven-development-a-new-paradigm-for-ai-assisted-software.md), not a methodology any priority source publishes. In practice the layer *does* exist, scattered across Spec Kit's [constitution](microsoft-github/spec-kit-spec-driven-md.md), Kiro [steering](other/kiro-steering.md), SPDD [Safeguards](martin-fowler/structured-prompt-driven-development.md), and general-purpose guardrail engines. Treat "constrain" as a layer you assemble, not a tool you install — and watch [constraint decay](other/constraint-decay-the-fragility-of-llm-agents-in-backend-code-generation.md): constraints degrade over long generations, so they must be re-asserted, not stated once.
2. **Spec-as-source is aspirational — the loop rarely closes.** Böckeler's central finding: most tools reach only *spec-first* (spec seeds code, then drifts); *spec-anchored* (spec stays linked) is rare; *spec-as-source* (code regenerated from spec, à la [code-to-contract "power inversion"](other/spec-driven-development-from-code-to-contract-in-the-age-of-ai-coding-assistants.md)) is essentially unrealized. The ↺ arrow back to the spec is the weakest edge in the diagram — without it, "executable specs" is framing, and critics call the whole thing "reinvented waterfall." Build the feedback loop with eval gates and human review; do not assume the tool maintains spec↔code coherence for you.

## Governing principle: match spec rigor to the task

The strongest cross-cutting finding is that **rigor is a dial, not a default**. Böckeler's three tiers are a cost ladder: a written spec, a constitution, a task breakdown, and a regeneration loop each add ceremony that only pays off as stakes and longevity rise. A throwaway script wants vibe coding; a shared library wants spec-first + a constitution; a long-lived, multi-contributor system is where spec-anchored discipline and feedback sensors start to earn their keep. ThoughtWorks parks SDD in **Assess** for exactly this reason — promising, not yet proven at every scale. Spend rigor where drift would hurt.

## A pragmatic starting path

1. **Pick the rigor tier first** (layer 0) — most tasks need *spec-first*, not *spec-as-source*. Don't pay for a regeneration loop you won't close.
2. **Write the spec and the constitution together** (layers 1–2) — a one-page [spec template](microsoft-github/spec-kit-spec-template.md) plus a short [constitution](microsoft-github/spec-kit-spec-driven-md.md)/[steering](other/kiro-steering.md) file of non-negotiables. Re-assert the constraints in the plan and implement prompts (constraint decay is real).
3. **Decompose before generating** (layer 3) — `/plan → /tasks`, or Explore→Plan→Implement; a planned spec implements far more reliably than a one-shot prompt.
4. **Make verification self-closing** (layer 5) — this is the highest-leverage, best-evidenced layer: [give the agent a test/build/lint it can run](anthropic/best-practices.md) and let it iterate to green. Adopt TDD here even if you skip it elsewhere.
5. **Gate the merge and evaluate against the spec** (layer 6 + ↺) — human/[automated review gates](thoughtworks/how-to-implement-effective-review-gates-for-ai-assisted-development.md) plus spec-adherence checks; and **govern** via the [security-governance reference architecture](../security-governance/reference-architecture.md) when the spec encodes security guarantees.
