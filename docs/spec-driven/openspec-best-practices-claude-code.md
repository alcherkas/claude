---
title: "OpenSpec with Claude Code: Best Practices & Efficiency Tips"
theme: spec-driven
source_type: synthesis
subtopic: Details
tags:
  - spec-driven
  - OpenSpec
  - Claude Code
  - report
fetch_status: ok
fetched_at: '2026-06-24'
also_in: null
---

# OpenSpec with Claude Code: Best Practices & Efficiency Tips

!!! abstract "About this page"
    A synthesized, source-checked research report on **[OpenSpec](https://github.com/Fission-AI/OpenSpec)** — the lightweight, tool-agnostic spec-driven development (SDD) layer — focused on running it efficiently inside **Claude Code**. Structural facts (workflow, directory layout, commands, install) are drawn from and re-verified against OpenSpec's own primary docs; authoring/efficiency guidance is a mix of documented behavior and credible practitioner reports, labelled as such. Claims were extracted and put through 3-vote adversarial verification; see [Caveats](#caveats) for what is *not* firmly established.

## TL;DR

- **What it is.** OpenSpec (`github.com/Fission-AI/OpenSpec`, npm `@fission-ai/openspec`) is a **brownfield-first, tool-agnostic** SDD layer that keeps a **source-of-truth spec** in `openspec/specs/` and proposes work through self-contained **change folders** in `openspec/changes/<name>/`. Completed changes are *merged into* the main spec and *moved* to `openspec/changes/archive/`.
- **The model that matters.** Changes are written as **delta specs** (`## ADDED / MODIFIED / REMOVED Requirements`) — you describe only what changes, not the whole spec. And the artifact flow `proposal → specs → design → tasks → implement → archive` is treated as **"actions, not phases"**: *"Dependencies are enablers, not gates."* You can skip `design`, or write `specs` before it.
- **Setup is two commands.** `npm install -g @fission-ai/openspec@latest` then `openspec init` (needs Node.js **20.19.0+**). For CI/scripted setup: `openspec init --tools claude`.
- **In Claude Code** OpenSpec ships **colon-namespaced** slash commands — `/opsx:propose`, `/opsx:apply`, `/opsx:sync`, `/opsx:archive`, and the optional `/opsx:explore` — delivered as **Skills** (`.claude/skills/openspec-*/SKILL.md`) and/or **commands** (`.claude/commands/opsx/<id>.md`). Cursor/Windsurf surface the same commands with hyphens (`/opsx-propose`).
- **Biggest efficiency wins:** start every non-trivial change with `/opsx:propose` so the *spec* (not the chat) carries intent; **archive aggressively** to keep `openspec/specs/` small and context lean; reach for OpenSpec when a change is multi-file or must be reviewed/persisted, and stay in Claude Code's built-in **plan mode** for quick, throwaway tasks.
- **Biggest pitfall:** OpenSpec adds real ceremony — practitioners report it *hurts* on trivial changes, where a plain `Instructions.md` or plan mode is faster. Match the tool to the size of the change.

## Key Findings

- **OpenSpec separates a stable source of truth from in-flight changes.** `openspec/specs/<domain>/spec.md` describes current behavior; each proposed change lives in its own folder and ships a **delta** against those specs, so the agent reviews *exactly what's changing* without diffing whole documents in its head.[^concepts][^getting-started]
- **The workflow is deliberately non-linear.** OpenSpec's `opsx` schema declares dependencies (`tasks` requires `specs`+`design`; `design`/`specs` require `proposal`) but frames them as enablers: *"Actions, not phases … do any of them anytime."* This is the central efficiency lever — and the clearest contrast with Spec Kit's rigid gates.[^concepts][^opsx]
- **It positions itself as the lightweight option.** OpenSpec's own README contrasts it with **GitHub Spec Kit** (*"Thorough but heavyweight. Rigid phase gates, lots of Markdown, Python setup"*) and **AWS Kiro** (*"locked into their IDE and limited to Claude models"*). Treat these as OpenSpec's *self-positioning* — the Kiro "Claude-only" characterization is not strictly accurate (see [Caveats](#caveats)).[^readme]
- **Claude Code support is first-class but currency-sensitive.** OpenSpec generates both Skills and subdirectory commands for Claude Code. A reported regression (Issue #1076) broke `.claude/commands/opsx/` subdirectory commands on Claude Code 2.1.119, making **Skills delivery the safer mechanism** at that point — verify which surfaces on your version.[^supported][^issue1076]
- **It's actively, rapidly developed.** At verification time npm `latest` was **1.4.1**; Kiro support landed in v1.2.0; the supported-tool matrix had grown to **~31 assistants**. Pin/verify version-specific details before relying on them.[^supported][^npm]

---

## 1. What OpenSpec is and how the change workflow works

OpenSpec is an SDD workflow you add to an existing repository. After `openspec init`, your repo gains an `openspec/` tree:[^getting-started]

```
openspec/
├── specs/              # Source of truth (your system's current behavior)
│   └── <domain>/       #   e.g. auth/, payments/, notifications/, ui/
│       └── spec.md
├── changes/            # Proposed updates — one folder per change
│   └── <change-name>/
│       ├── proposal.md #   Why + what (intent, scope, approach)
│       ├── design.md   #   How (technical approach) — optional
│       ├── tasks.md    #   Implementation checklist ([ ] / [x])
│       └── specs/      #   Delta specs (only what's changing)
│           └── <domain>/
│               └── spec.md
└── config.yaml         # Project configuration (optional)
```

**The artifact flow** is `proposal → specs → design → tasks → implement → archive`:[^concepts]

```
proposal ──► specs ──► design ──► tasks ──► implement
   why        what       how       steps
 + scope    changes    approach   to take
```

**Delta specs** are the distinguishing feature. Instead of restating a whole specification, a change describes only the difference using three (validated: four) requirement sections, each applied to the main spec on archive:[^concepts]

| Section | Meaning | On archive |
| --- | --- | --- |
| `## ADDED Requirements` | New behavior | Appended to main spec |
| `## MODIFIED Requirements` | Changed behavior | Replaces the existing requirement |
| `## REMOVED Requirements` | Deprecated behavior | Deleted from main spec |
| `## RENAMED Requirements` | (supported by the validator) | — |

**Archiving** does three things: it merges each delta section into the corresponding `openspec/specs/` file, moves the change folder to `openspec/changes/archive/` with a `YYYY-MM-DD-<name>/` date prefix, and leaves all original artifacts intact for history. After archive, *"Specs now describe the new behavior."*[^concepts]

### How it differs from Spec Kit and Kiro

| Dimension | **OpenSpec** | **GitHub Spec Kit** | **AWS Kiro** |
| --- | --- | --- | --- |
| Footprint | Lightweight; delta specs; brownfield-first | Heavyweight; per-feature spec set | Full IDE experience |
| Flow | "Actions, not phases" — iterate freely | Rigid phase gates (`/specify → /plan → /tasks → implement`) | Requirements → design → tasks (IDE-driven) |
| Setup | `npm i -g`, Node 20.19+ | Python toolchain | Install the Kiro IDE |
| Tooling | Tool-agnostic (~31 assistants) | Copilot-centric, multi-agent | IDE-locked (OpenSpec claims "Claude-only" — see caveat) |
| Sweet spot | Brownfield change management without ceremony | Greenfield, process-heavy teams | Teams adopting Kiro's IDE |

*Source: OpenSpec's own README positioning, corroborated by third-party comparisons; read it as OpenSpec's stance, not a neutral benchmark.*[^readme][^intent-vs][^spec-compare]

## 2. Installation & setup with Claude Code

```bash
# 1. Install globally (also available via pnpm / yarn / bun / Nix)
npm install -g @fission-ai/openspec@latest      # requires Node.js 20.19.0+

# 2. Initialize inside your project
cd your-project && openspec init

# Non-interactive / CI setup — select tools explicitly:
openspec init --tools claude                    # Claude Code only
openspec init --tools claude,cursor             # comma-separated
openspec init --tools all                        # or: --tools none
```

Claude Code is referenced by the tool ID **`claude`**.[^supported] `openspec init` scaffolds the `openspec/` tree above and writes the per-tool command/skill files.

**How the slash commands reach Claude Code.** OpenSpec's tool directory lists, for Claude Code:[^supported]

- **Skills:** `.claude/skills/openspec-*/SKILL.md` (e.g. `openspec-propose`, `openspec-explore`, `openspec-apply-change`)
- **Commands:** `.claude/commands/opsx/<id>.md`

Because Claude Code namespaces subdirectory commands with a **colon**, the commands appear as `/opsx:propose`, `/opsx:apply`, etc. Cursor/Windsurf flatten the same files to **hyphens** (`/opsx-propose`).[^commands]

!!! warning "Command-delivery is the moving target"
    Issue #1076 reports that a Claude Code "commands→skills merge" (around v2.1.119) broke subdirectory commands placed in `.claude/commands/opsx/`. Where that bites, the **Skills** delivery (`.claude/skills/`) is the working mechanism. If `/opsx:*` doesn't autocomplete, check which of the two OpenSpec generated and re-run `openspec update` after upgrading.[^issue1076]

### Slash command reference

**Core profile (default):**[^commands][^getting-started]

```
/opsx:explore  →  /opsx:propose  →  /opsx:apply  →  /opsx:sync  →  /opsx:archive
```

| Command | What it does |
| --- | --- |
| `/opsx:explore` | No-stakes thinking partner *before* committing to a change (optional) |
| `/opsx:propose <what to build>` | Create a change **and** generate planning artifacts in one step (the default start) |
| `/opsx:apply` | Read `tasks.md`, implement the incomplete tasks, tick `[x]` as it goes |
| `/opsx:sync` | Merge the change's delta specs into the main specs |
| `/opsx:archive` | Finalize and move the change to `changes/archive/` |

**Expanded profile** adds finer-grained commands — enable with `openspec config profile`, then `openspec update`:[^commands][^readme]

```
/opsx:new  →  /opsx:ff  or  /opsx:continue  →  /opsx:apply  →  /opsx:verify  →  /opsx:archive
```

(`/opsx:bulk-archive` and `/opsx:onboard` are also part of the expanded set.)

## 3. Best practices for writing proposals, specs & tasks

*These combine documented behavior with practitioner guidance; treat the prose-level tips as heuristics, not spec.*

- **Let `/opsx:propose` write the spec, not the chat.** The whole point is to move intent out of the ephemeral conversation and into `proposal.md` + delta specs that survive context compaction. Start non-trivial work there rather than free-typing requirements.[^readme][^getting-started]
- **Write true deltas.** Put *only* what changes under `ADDED`/`MODIFIED`/`REMOVED`. For `MODIFIED`, include the **complete revised requirement text** (it *replaces* the old one on archive — a partial edit silently drops the rest).[^concepts]
- **Keep specs domain-scoped.** Mirror your system in `specs/<domain>/spec.md` (e.g. `auth/`, `payments/`, `ui/`). Domain separation keeps each spec small enough to load without flooding context.[^getting-started]
- **Make requirements behavioral and scenario-shaped.** Requirements that read as observable behavior + scenarios are what agents implement most reliably; vague "the system should be fast" requirements produce vague code.[^openspec-bp]
- **Size tasks to single, checkable units.** `tasks.md` is a literal checklist `/opsx:apply` walks; small, independently verifiable tasks let the agent (and you) track real progress and resume cleanly after interruption.[^commands]
- **Skip `design.md` when the change is obvious.** Because design isn't a gate, generating it for a trivial change is pure overhead — reserve it for changes with real technical choices.[^concepts][^opsx]

## 4. Efficiency tips specific to Claude Code

- **OpenSpec vs. plan mode — pick by blast radius.** Claude Code's built-in **plan mode** produces an in-memory plan for a single turn; OpenSpec persists reviewable artifacts across sessions and people. Use plan mode for quick, single-session changes you *"could describe in one sentence"*; reach for OpenSpec when the change is multi-file, multi-session, or review-worthy. *(Practitioner-sourced boundary — OpenSpec's docs don't define a "small change" threshold; see [Caveats](#caveats).)*[^ranthebuilder]
- **Use `/opsx:explore` as a cheap thinking pass.** It's explicitly a no-stakes partner — explore the shape of a change before `/opsx:propose` locks artifacts, instead of burning a full proposal cycle on a half-formed idea.[^commands][^readme]
- **Archive aggressively to protect the context window.** Every archived change shrinks active `changes/` and folds behavior into the (smaller, canonical) specs. A lean `openspec/specs/` is cheaper to load — directly aligned with Claude Code's "context fills fast, performance degrades as it fills" guidance.[^concepts]
- **Treat `openspec/specs/` as the agent's source of truth.** Point Claude Code at the spec for "what the system does today" rather than re-reading code; the spec is the compressed, authoritative summary that `/opsx:archive` keeps current.[^concepts]
- **Prefer Skills delivery on current Claude Code.** Per Issue #1076, `/opsx:*` command *files* under `.claude/commands/opsx/` may not appear in the menu; the structural fix is **skills delivery** (`.claude/skills/openspec-*/SKILL.md`). Switch the `delivery` setting to skills, then run `openspec update` — note `openspec update` alone does **not** resolve the commands→skills incompatibility. See [Pitfalls](#5-pitfalls-failure-modes-and-how-to-avoid-them) below.[^issue1076][^supported]
- **Re-run `openspec update` after changing profiles or upgrading.** Switching `core → expanded` (`openspec config profile`) only takes effect after `openspec update` regenerates the command/skill files; OpenSpec also surfaces a drift warning recommending `openspec update` when project files fall out of sync with your profile.[^readme][^commands][^cli]

## 5. Pitfalls, failure modes and how to avoid them

*Grouped by category, each as **symptom → root cause → fix**. The integration/regression items are version-pinned and fast-moving — verify against your current CLI/tool versions (see [Caveats](#caveats)).*

### Claude Code / tooling integration

- **`/opsx:*` commands don't appear in Claude Code.** **Symptom:** typing `/opsx:` shows *"No commands match"* with an empty menu (reproduced on OpenSpec 1.3.1 + Claude Code 2.1.119). **Root cause:** OpenSpec can deliver commands as subdirectory **command files** (`.claude/commands/opsx/<id>.md`), which are incompatible with Claude Code's newer **commands→skills merge** (the exact causal chain is contested; the symptom is confirmed). **Fix:** use **skills delivery** — OpenSpec also installs `.claude/skills/openspec-*/SKILL.md`; switch the `delivery` setting to skills, then run `openspec update`. `openspec update` *alone* does **not** resolve the incompatibility.[^issue1076][^supported]
- **Same breakage on codex-cli.** **Symptom:** on codex-cli ≥ 0.117.0, every `/opsx:*` command is treated as plain text — no dropdown, no error (regression from 0.116.x; still broken in 0.118.0). **Root cause:** upstream codex-cli removed/broke custom prompt-based slash commands (Issue #890; closed "not planned"). **Fix:** use the **`$openspec-*` skills** instead — OpenSpec is deprecating the `/opsx:*` prompt commands in favor of skills (Issue #1129).[^issue890][^issue1129]
- **Profile/upgrade changes don't take effect.** **Symptom:** you changed the profile or upgraded the CLI, but the new commands/guidance don't show up in a project. **Root cause:** the global selection is saved immediately, but **project files aren't rewritten until you regenerate them**. **Fix:** `npm install -g @fission-ai/openspec@latest`, then **`openspec update`** in each project (or accept *"Apply changes to this project now?"*). To enable the expanded workflow: `openspec config profile` → `openspec update`.[^readme][^cli][^supported]

### Workflow / process

- **Over-ceremony on small changes.** **Symptom:** the propose→apply→archive cycle adds overhead without better output; one practitioner found a plain `Instructions.md` *"faster, cheaper, and easier to iterate on."* **Root cause:** OpenSpec is built for multi-step/persisted work; small single-session edits don't amortize the structure. **Fix:** reserve OpenSpec for multi-file / multi-session / review-worthy work; use Claude Code **Plan Mode** for one-sentence changes.[^incomplete][^ranthebuilder]
- **Skipping review (no gates).** **Symptom:** a half-baked proposal flows straight to implementation. **Root cause:** OpenSpec is deliberately frictionless — *"no review gates between phases,"* *"dependencies are enablers, not gates."* Review is recommended, **not enforced**. **Fix:** always review `proposal.md` and the delta specs before `/opsx:apply`; OpenSpec 1.2+ ships an opt-in `openspec-git-discipline` skill that imposes propose/apply/archive gates (off by default).[^ranthebuilder][^concepts]
- **Forgetting to archive → spec drift.** **Symptom:** changes pile up in `changes/`, `openspec/specs/` stops matching reality, context bloats, developers stop trusting the spec ("specification rot"). **Root cause:** specs only stay authoritative if completed work is merged back. **Fix:** treat `/opsx:archive` (or `/opsx:bulk-archive`) as part of "done"; archive auto-prompts `/opsx:sync` to merge deltas into the main spec.[^arxiv][^concepts]

### Authoring / spec quality

- **Stale or partial `MODIFIED` blocks (silent loss).** **Symptom:** requirement text disappears from the main spec after archive. **Root cause:** on **archive**, a `MODIFIED` block is applied as a **replacement** of the matching requirement, so anything you didn't restate is dropped — and `concepts.md` carries no explicit warning. **Fix:** always include the **complete revised requirement** (prior text + change) and review the archived spec. *(Nuance: `/opsx:sync` is documented as content-preserving — "intelligent, not copy-paste" — which softens this on the happy path, but don't rely on it for partial blocks.)*[^concepts][^commands]
- **Invented rationale / intent misrepresentation.** **Symptom:** generated specs justify *"decisions you didn't make."* **Root cause:** the model fills gaps by assuming context and adding rationale. **Fix:** review generated proposals/specs for invented justifications before applying; correct the intent, not just the wording.[^ranthebuilder]
- **Vague requirements / oversized tasks.** **Symptom:** the agent produces vague code or stalls mid-task. **Root cause:** non-behavioral requirements ("should be fast") and large, non-atomic tasks give the agent nothing checkable. **Fix:** write behavioral, scenario-shaped requirements; size `tasks.md` items to single, independently verifiable units.[^openspec-bp][^commands]

### Adoption / team

- **Process theater.** **Symptom:** the team games or quietly abandons the workflow. **Root cause:** the spec process adds overhead without improving understanding or quality. **Fix:** keep specs "minimum viable"; if a step doesn't demonstrably improve outcomes, drop it.[^arxiv]
- **Trusting marketing comparisons literally.** **Symptom:** tool choice driven by README framing. **Root cause:** OpenSpec's positioning oversells rivals' rigidity (and mis-states Kiro as "Claude-only"). **Fix:** evaluate against your actual workflow; cross-check independent comparisons.[^readme][^intent-vs]

### Versioning / currency

- **1.0.0 removed the legacy `/openspec:*` commands.** **Symptom:** `/openspec:proposal`, `/openspec:apply`, `/openspec:archive` no longer exist after upgrade; workflows/docs referencing them break. **Root cause:** 1.0.0 replaced the linear model with the action-based `/opsx:*` set — a breaking change, not 1:1 renames. **Fix:** migrate references to `/opsx:*`; run `openspec init`/`openspec update` to regenerate.[^changelog][^commands]
- **1.0.0 stopped generating `CLAUDE.md` / `AGENTS.md`.** **Symptom:** expected auto-generated tool-config files (`CLAUDE.md`, `.cursorrules`, `AGENTS.md`, `project.md`) aren't created. **Root cause:** guidance moved to the **skills** standard — a single `.claude/skills/` directory replaces 8+ scattered config files. **Fix:** stop expecting auto-generated instruction files; rely on the skills directory. Hand-written content in existing `CLAUDE.md`/`AGENTS.md` is preserved (only OpenSpec marker blocks are stripped); `project.md` isn't auto-deleted (migrate its context to `openspec/config.yaml`).[^changelog][^migration][^issue614]

## Recommendations

- **Adopt OpenSpec for brownfield, multi-file, review-worthy changes in Claude Code**; keep a lighter path (plan mode / `Instructions.md`) for small edits.
- **Standardize the loop:** `/opsx:explore` (optional) → `/opsx:propose` → `/opsx:apply` → `/opsx:sync` → `/opsx:archive`, and treat archive as part of "done" so specs stay authoritative and context stays small.
- **Verify version-specific details at the source** (commands, paths, supported tools, delivery mechanism) before relying on them — OpenSpec ships fast and Claude Code's command/skill handling is itself in flux.
- **Cross-reference** this topic's [Spec Kit](microsoft-github/github-spec-kit-repository.md) and [Kiro](other/kiro-specs.md) material and the [ThoughtWorks Tools quadrant](thoughtworks/tools-quadrant.md) (which lists OpenSpec) when choosing between SDD tools.

## Caveats

- **Currency (the dominant caveat).** OpenSpec ships fast and the integration breakages are **version-pinned**: Issue #1076 (Claude Code 2.1.119 / CLI 1.3.1, ~May 2026) and Issues #890/#1129 (codex-cli 0.117.0–0.118.0, ~Mar–Apr 2026) may already be resolved or shifted. npm `latest` was **1.4.1** at verification; **1.0.0 was a breaking release** (removed the legacy `/openspec:*` commands and stopped generating `CLAUDE.md`/`AGENTS.md`). Re-verify versions, paths, and the command-delivery mechanism before relying on them.
- **Marketing vs. fact.** The vs-Spec-Kit / vs-Kiro framing is OpenSpec's **self-positioning**. The "Kiro is Claude-only" claim is **inaccurate** about Kiro itself; "lightweight / brownfield-first / without ceremony" is partly vendor language.
- **Refuted during verification (do *not* rely on):** from the best-practices pass — (a) that `openspec init` auto-detects Skills across Claude Code/Cursor/Windsurf via `.claude/skills/`; (b) that OpenSpec consolidates into a single living document while Spec Kit fragments per-feature. From the pitfalls pass — (c) that the *exact* causal mechanism of Issue #1076 is established (the symptom is confirmed; the mechanism is contested); (d) that `openspec update` by itself *fixes* the missing-slash-command regression (it regenerates artifacts — the cure is skills delivery).
- **Doc inconsistencies.** "20+ assistants" vs "~31 tools"; `concepts.md` documents three delta operations (ADDED/MODIFIED/REMOVED) while the validation regex also accepts `RENAMED`.
- **Source strength.** Structural and integration facts (workflow, layout, commands, install, delta format, archive, the #1076/#890 regressions, the 1.0.0 breaking changes) rest on **primary** sources (README, `docs/*`, CHANGELOG, GitHub issues) and are high-confidence. The *invented-rationale* and *Plan-Mode-boundary* pitfalls rest on a single practitioner review (medium-confidence); the *over-ceremony* and *spec-rot* pitfalls come from an arXiv **preprint** on SDD generally (not OpenSpec-specific), though corroborated by multiple practitioners. The `MODIFIED`-data-loss and invented-rationale findings each passed only 2-1 — treat their interpretive tails with extra caution.

## Sources

**Primary (OpenSpec official docs & repo):**

- OpenSpec README — install, slash commands, positioning, profiles: <https://github.com/Fission-AI/OpenSpec>[^readme]
- `docs/concepts.md` — directory model, delta specs, artifact flow, "actions not phases," archive: <https://github.com/Fission-AI/OpenSpec/blob/main/docs/concepts.md>[^concepts]
- `docs/getting-started.md` — install + init + directory tree + command workflow: <https://github.com/Fission-AI/OpenSpec/blob/main/docs/getting-started.md>[^getting-started]
- `docs/commands.md` — `/opsx:*` reference, core vs expanded profile, colon-vs-hyphen syntax: <https://github.com/Fission-AI/OpenSpec/blob/main/docs/commands.md>[^commands]
- `docs/opsx.md` — `opsx` artifact schema and dependency semantics: <https://github.com/Fission-AI/OpenSpec/blob/main/docs/opsx.md>[^opsx]
- `docs/supported-tools.md` — Claude Code Skills/Commands paths, `--tools` flag, tool matrix: <https://github.com/Fission-AI/OpenSpec/blob/main/docs/supported-tools.md>[^supported]
- `docs/cli.md` — `openspec update`, profile application, drift warning: <https://github.com/Fission-AI/OpenSpec/blob/main/docs/cli.md>[^cli]
- `CHANGELOG.md` — 1.0.0 breaking changes (legacy commands removed; no more `CLAUDE.md`/`AGENTS.md`): <https://github.com/Fission-AI/OpenSpec/blob/main/CHANGELOG.md>[^changelog]
- `docs/migration-guide.md` — skills standard; `project.md` → `config.yaml` migration: <https://github.com/Fission-AI/OpenSpec/blob/main/docs/migration-guide.md>[^migration]
- GitHub Issue #1076 — Claude Code command/skill delivery regression: <https://github.com/Fission-AI/OpenSpec/issues/1076>[^issue1076]
- GitHub Issue #890 — codex-cli ≥0.117.0 `/opsx:*` regression: <https://github.com/Fission-AI/OpenSpec/issues/890>[^issue890]
- GitHub Issue #1129 — `/opsx:*` prompt commands deprecated in favor of skills: <https://github.com/Fission-AI/OpenSpec/issues/1129>[^issue1129]
- GitHub Issue #614 — `openspec init` no longer creates `AGENTS.md`/`CLAUDE.md`: <https://github.com/Fission-AI/OpenSpec/issues/614>[^issue614]
- arXiv 2602.00180v1 — "Spec-Driven Development: From Code to Contract" (§IX Common Pitfalls; general SDD): <https://arxiv.org/html/2602.00180v1>[^arxiv]
- npm registry — `@fission-ai/openspec` (latest 1.4.1 at verification): <https://www.npmjs.com/package/@fission-ai/openspec>[^npm]

**Secondary / practitioner (corroborating, not authoritative):**

- intent-driven.dev — Spec Kit vs OpenSpec comparison: <https://intent-driven.dev/knowledge/spec-kit-vs-openspec/>[^intent-vs]
- cameronsjo/spec-compare — SDD tool comparison matrix: <https://github.com/cameronsjo/spec-compare>[^spec-compare]
- openspec.pro — best-practices guide: <https://openspec.pro/best-practices/>[^openspec-bp]
- ranthebuilder.cloud — hands-on test of three SDD tools (OpenSpec scored highest, yet self-critical): <https://ranthebuilder.cloud/blog/i-tested-three-spec-driven-ai-tools-here-s-my-honest-take/>[^ranthebuilder]
- dev.to / incomplete_developer — critical write-up ("Instructions.md was simpler and faster"): <https://dev.to/incomplete_developer/openspec-spec-driven-development-failed-my-experiment-instructionsmd-was-simpler-and-faster-3a5d>[^incomplete]

*Synthesized via the deep-research harness across two passes — best practices (5 angles → 17 sources → 84 claims → 25 verified, 23 confirmed) and pitfalls (5 angles → 20 sources → 99 claims → 25 verified, 20 confirmed), each via 3-vote adversarial verification. Compiled 2026-06-24.*

[^readme]: OpenSpec README, `github.com/Fission-AI/OpenSpec` — re-fetched and confirmed 2026-06-24.
[^concepts]: OpenSpec `docs/concepts.md` — re-fetched and confirmed 2026-06-24.
[^getting-started]: OpenSpec `docs/getting-started.md` — re-fetched and confirmed 2026-06-24.
[^commands]: OpenSpec `docs/commands.md`.
[^opsx]: OpenSpec `docs/opsx.md`.
[^supported]: OpenSpec `docs/supported-tools.md` — re-fetched and confirmed 2026-06-24.
[^cli]: OpenSpec `docs/cli.md` — `openspec update`, profile application, drift warning.
[^changelog]: OpenSpec `CHANGELOG.md` — 1.0.0 breaking changes.
[^migration]: OpenSpec `docs/migration-guide.md` — skills standard; `project.md` → `config.yaml`.
[^issue1076]: OpenSpec GitHub Issue #1076 (reported regression on Claude Code ~2.1.119, CLI 1.3.1).
[^issue890]: OpenSpec GitHub Issue #890 (codex-cli ≥0.117.0 `/opsx:*` regression; closed "not planned").
[^issue1129]: OpenSpec GitHub Issue #1129 (`/opsx:*` prompt commands deprecated toward `$openspec-*` skills).
[^issue614]: OpenSpec GitHub Issue #614 (`openspec init` no longer creates `AGENTS.md`/`CLAUDE.md`).
[^arxiv]: arXiv 2602.00180v1, §IX "Common Pitfalls" — general SDD preprint (not OpenSpec-specific), corroborated by practitioners.
[^ranthebuilder]: ranthebuilder.cloud, "I Tested Three Spec-Driven AI Tools" — OpenSpec scored highest (4.0/5) yet self-critical (medium-confidence).
[^npm]: npm registry, `@fission-ai/openspec` (latest 1.4.1 observed at verification).
[^intent-vs]: intent-driven.dev, "Spec Kit vs OpenSpec" (secondary).
[^spec-compare]: cameronsjo/spec-compare (secondary).
[^openspec-bp]: openspec.pro best-practices (blog).
[^incomplete]: dev.to, "OpenSpec … failed my experiment, Instructions.md was simpler and faster" (practitioner opinion).
