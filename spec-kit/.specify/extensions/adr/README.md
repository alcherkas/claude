# Architecture Decision Records Extension

Captures architectural decisions for the active feature as numbered, status-tracked Markdown ADR files. Designed to be invoked **after `/speckit.specify`** — before deep planning — so framing decisions (monolith vs microservice, sync vs async, single-tenant vs multi-tenant) are recorded up front rather than buried in `research.md` or implicit in `plan.md`.

## Overview

This is a deliberate divergence from spec-kit's default flow, where decisions are captured inside `/speckit.plan`'s `research.md`. The ADR step is opt-in and runs anytime once `spec.md` exists for a feature; it does not require, read, or modify `plan.md` or `research.md`.

ADRs follow a MADR-minimal layout (Status / Date / Context / Considered Options / Decision Outcome / Consequences) with a Constitution Check gate.

## Commands

| Command | Description |
|---------|-------------|
| `speckit.adr` | Capture an architectural decision as a numbered ADR file under `specs/<feature>/adr/` |

## Hooks

This extension does not register any hooks of its own. Hooks for `before_adr` and `after_adr` are registered project-wide in `.specify/extensions.yml` against the `git` extension's `speckit.git.commit` command, so auto-commit before/after ADR creation behaves consistently with other steps.

## Output

For each invocation, the step writes:

- `specs/<feature>/adr/NNNN-<kebab-title>.md` — a single ADR file
- `specs/<feature>/adr/README.md` — an index table refreshed with the new row

## Argument forms

- `/speckit.adr "<title>"` — write a new ADR with the given title (Status: Proposed)
- `/speckit.adr "<title>" --status accepted` — Status: Accepted
- `/speckit.adr "<title>" --supersede NNNN` — write the new ADR and patch the older ADR's Status to `Superseded by ...`
- `/speckit.adr` (no args) — prompt for a title

## Graceful Degradation

- If `spec.md` is missing for the active feature, the step errors and tells the user to run `/speckit.specify` first.
- If the project's `.specify/memory/constitution.md` is the placeholder template (no concrete principles), the Constitution Check section records `No constitution defined — gate skipped`.
- If git is unavailable, the auto-commit hook is skipped silently — same behavior as the other steps.

## Configuration

This extension has no per-extension configuration file. Auto-commit behavior on the `before_adr` / `after_adr` hooks is controlled via the existing `.specify/extensions/git/git-config.yml`'s `auto_commit` section.

## Scripts

- `scripts/bash/setup-adr.sh` — resolves feature paths, validates `spec.md`, computes the next ADR number, locates the template, emits JSON for the skill
- `scripts/bash/list-adrs.sh` — `--next-num`, `--list`, `--find` helpers (used by the skill and by `setup-adr.sh`)

Both scripts source the core `.specify/scripts/bash/common.sh` for shared path-resolution helpers (`get_feature_paths`, `resolve_template`, `feature_json_matches_feature_dir`, `has_jq`, `json_escape`).
