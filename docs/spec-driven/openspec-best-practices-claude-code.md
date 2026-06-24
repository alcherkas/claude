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

- **OpenSpec vs. plan mode — pick by blast radius.** Claude Code's built-in **plan mode** is ideal for quick, throwaway, single-file changes where you just want a plan before edits. Reach for **OpenSpec** when the change is multi-file, needs review, or should leave a durable spec behind. *(This split is practitioner guidance — OpenSpec's docs don't prescribe it; see [Caveats](#caveats).)*
- **Use `/opsx:explore` as a cheap thinking pass.** It's explicitly a no-stakes partner — explore the shape of a change before `/opsx:propose` locks artifacts, instead of burning a full proposal cycle on a half-formed idea.[^commands][^readme]
- **Archive aggressively to protect the context window.** Every archived change shrinks active `changes/` and folds behavior into the (smaller, canonical) specs. A lean `openspec/specs/` is cheaper to load — directly aligned with Claude Code's "context fills fast, performance degrades as it fills" guidance.[^concepts]
- **Treat `openspec/specs/` as the agent's source of truth.** Point Claude Code at the spec for "what the system does today" rather than re-reading code; the spec is the compressed, authoritative summary that `/opsx:archive` keeps current.[^concepts]
- **Prefer Skills delivery on current Claude Code.** Given Issue #1076, if `/opsx:*` commands misbehave, rely on the generated Skills in `.claude/skills/` and re-run `openspec update` after a Claude Code upgrade.[^issue1076]
- **Re-run `openspec update` after changing profiles or upgrading.** Switching `core → expanded` (`openspec config profile`) only takes effect after `openspec update` regenerates the command/skill files.[^readme][^commands]

## 5. Common pitfalls & how to avoid them

- **Ceremony on small changes.** The most-cited criticism: for trivial work, OpenSpec's structured cycle *"introduced a lot of overhead without delivering better results"* — one practitioner found a plain `Instructions.md` faster and cheaper. **Fix:** use OpenSpec for substantive, multi-step changes; drop to plan mode / a simple instructions file for one-liners.[^incomplete]
- **Stale or partial `MODIFIED` blocks.** Because `MODIFIED` *replaces* the existing requirement on archive, an incomplete edit deletes the parts you didn't restate. **Fix:** always paste the full revised requirement.[^concepts]
- **Slash commands not appearing.** Usually the command-delivery regression (Issue #1076) or a profile change that wasn't followed by `openspec update`. **Fix:** check `.claude/skills/` vs `.claude/commands/opsx/`, re-run `openspec update`.[^issue1076]
- **Forgetting to archive.** Unarchived changes pile up in `changes/`, the main spec drifts from reality, and context bloats. **Fix:** make `/opsx:archive` (or `/opsx:bulk-archive`) part of "done."[^concepts]
- **Trusting marketing comparisons literally.** OpenSpec's own positioning oversells rivals' rigidity (and mis-states Kiro as "Claude-only"). **Fix:** evaluate against your actual workflow, not the README's framing.[^readme]

## Recommendations

- **Adopt OpenSpec for brownfield, multi-file, review-worthy changes in Claude Code**; keep a lighter path (plan mode / `Instructions.md`) for small edits.
- **Standardize the loop:** `/opsx:explore` (optional) → `/opsx:propose` → `/opsx:apply` → `/opsx:sync` → `/opsx:archive`, and treat archive as part of "done" so specs stay authoritative and context stays small.
- **Verify version-specific details at the source** (commands, paths, supported tools, delivery mechanism) before relying on them — OpenSpec ships fast and Claude Code's command/skill handling is itself in flux.
- **Cross-reference** this topic's [Spec Kit](microsoft-github/github-spec-kit-repository.md) and [Kiro](other/kiro-specs.md) material and the [ThoughtWorks Tools quadrant](thoughtworks/tools-quadrant.md) (which lists OpenSpec) when choosing between SDD tools.

## Caveats

- **Currency.** OpenSpec is under rapid development (npm `latest` was **1.4.1** at verification; Kiro support added in v1.2.0; tool matrix ~31). The **command-delivery mechanism for Claude Code is the most volatile detail** (Issue #1076). Re-verify before quoting versions or paths.
- **Marketing vs. fact.** The vs-Spec-Kit / vs-Kiro framing is OpenSpec's **self-positioning**. The "Kiro is Claude-only" claim is **inaccurate** about Kiro itself; "lightweight / brownfield-first / without ceremony" is partly vendor language.
- **Refuted during verification (do *not* rely on):** (a) that `openspec init` auto-detects Skills across Claude Code/Cursor/Windsurf via `.claude/skills/`; (b) that OpenSpec consolidates into a single living document while Spec Kit fragments per-feature.
- **Doc inconsistencies.** "20+ assistants" vs "~31 tools"; `concepts.md` documents three delta operations (ADDED/MODIFIED/REMOVED) while the validation regex also accepts `RENAMED`.
- **Thinly verified areas.** Prose-level authoring style, the exact contents OpenSpec writes into `AGENTS.md`/`CLAUDE.md`, the OpenSpec-vs-plan-mode boundary, and any measured efficiency gains are **practitioner-level guidance**, not established from primary docs. The structural facts (workflow, layout, commands, install, delta format, archive) are primary-source-verified.

## Sources

**Primary (OpenSpec official docs & repo):**

- OpenSpec README — install, slash commands, positioning, profiles: <https://github.com/Fission-AI/OpenSpec>[^readme]
- `docs/concepts.md` — directory model, delta specs, artifact flow, "actions not phases," archive: <https://github.com/Fission-AI/OpenSpec/blob/main/docs/concepts.md>[^concepts]
- `docs/getting-started.md` — install + init + directory tree + command workflow: <https://github.com/Fission-AI/OpenSpec/blob/main/docs/getting-started.md>[^getting-started]
- `docs/commands.md` — `/opsx:*` reference, core vs expanded profile, colon-vs-hyphen syntax: <https://github.com/Fission-AI/OpenSpec/blob/main/docs/commands.md>[^commands]
- `docs/opsx.md` — `opsx` artifact schema and dependency semantics: <https://github.com/Fission-AI/OpenSpec/blob/main/docs/opsx.md>[^opsx]
- `docs/supported-tools.md` — Claude Code Skills/Commands paths, `--tools` flag, tool matrix: <https://github.com/Fission-AI/OpenSpec/blob/main/docs/supported-tools.md>[^supported]
- GitHub Issue #1076 — Claude Code command/skill delivery regression: <https://github.com/Fission-AI/OpenSpec/issues/1076>[^issue1076]
- npm registry — `@fission-ai/openspec` (latest 1.4.1 at verification): <https://www.npmjs.com/package/@fission-ai/openspec>[^npm]

**Secondary / practitioner (corroborating, not authoritative):**

- intent-driven.dev — Spec Kit vs OpenSpec comparison: <https://intent-driven.dev/knowledge/spec-kit-vs-openspec/>[^intent-vs]
- cameronsjo/spec-compare — SDD tool comparison matrix: <https://github.com/cameronsjo/spec-compare>[^spec-compare]
- openspec.pro — best-practices guide: <https://openspec.pro/best-practices/>[^openspec-bp]
- ranthebuilder.cloud — hands-on test of three SDD tools (OpenSpec scored highest): <https://ranthebuilder.cloud/blog/i-tested-three-spec-driven-ai-tools-here-s-my-honest-take/>
- dev.to / incomplete_developer — critical write-up ("Instructions.md was simpler and faster"): <https://dev.to/incomplete_developer/openspec-spec-driven-development-failed-my-experiment-instructionsmd-was-simpler-and-faster-3a5d>[^incomplete]

*Synthesized via the deep-research harness (5 search angles → 17 sources fetched → 84 claims → 25 verified by 3-vote adversarial verification, 23 confirmed). Compiled 2026-06-24.*

[^readme]: OpenSpec README, `github.com/Fission-AI/OpenSpec` — re-fetched and confirmed 2026-06-24.
[^concepts]: OpenSpec `docs/concepts.md` — re-fetched and confirmed 2026-06-24.
[^getting-started]: OpenSpec `docs/getting-started.md` — re-fetched and confirmed 2026-06-24.
[^commands]: OpenSpec `docs/commands.md`.
[^opsx]: OpenSpec `docs/opsx.md`.
[^supported]: OpenSpec `docs/supported-tools.md` — re-fetched and confirmed 2026-06-24.
[^issue1076]: OpenSpec GitHub Issue #1076 (reported regression on Claude Code ~2.1.119).
[^npm]: npm registry, `@fission-ai/openspec` (latest 1.4.1 observed at verification).
[^intent-vs]: intent-driven.dev, "Spec Kit vs OpenSpec" (secondary).
[^spec-compare]: cameronsjo/spec-compare (secondary).
[^openspec-bp]: openspec.pro best-practices (blog).
[^incomplete]: dev.to, "OpenSpec … failed my experiment, Instructions.md was simpler and faster" (practitioner opinion).
