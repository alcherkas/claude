---
name: "speckit-adr"
description: "Capture architectural decisions as numbered, status-tracked ADR files under specs/<feature>/adr/. Invoked after /speckit.specify."
argument-hint: "Decision title, optional flags: --supersede NNNN, --status accepted|proposed"
compatibility: "Requires spec-kit project structure with .specify/ directory and an existing spec.md for the active feature"
metadata:
  author: "local-team"
  source: ".specify/extensions/adr/commands/speckit.adr.md"
user-invocable: true
disable-model-invocation: false
---


## User Input

```text
$ARGUMENTS
```

You **MUST** consider the user input before proceeding (if not empty).

## Pre-Execution Checks

**Check for extension hooks (before ADR generation)**:
- Check if `.specify/extensions.yml` exists in the project root.
- If it exists, read it and look for entries under the `hooks.before_adr` key
- If the YAML cannot be parsed or is invalid, skip hook checking silently and continue normally
- Filter out hooks where `enabled` is explicitly `false`. Treat hooks without an `enabled` field as enabled by default.
- For each remaining hook, do **not** attempt to interpret or evaluate hook `condition` expressions:
  - If the hook has no `condition` field, or it is null/empty, treat the hook as executable
  - If the hook defines a non-empty `condition`, skip the hook and leave condition evaluation to the HookExecutor implementation
- When constructing slash commands from hook command names, replace dots (`.`) with hyphens (`-`). For example, `speckit.git.commit` → `/speckit-git-commit`.
- For each executable hook, output the following based on its `optional` flag:
  - **Optional hook** (`optional: true`):
    ```
    ## Extension Hooks

    **Optional Pre-Hook**: {extension}
    Command: `/{command}`
    Description: {description}

    Prompt: {prompt}
    To execute: `/{command}`
    ```
  - **Mandatory hook** (`optional: false`):
    ```
    ## Extension Hooks

    **Automatic Pre-Hook**: {extension}
    Executing: `/{command}`
    EXECUTE_COMMAND: {command}

    Wait for the result of the hook command before proceeding to the Outline.
    ```
- If no hooks are registered or `.specify/extensions.yml` does not exist, skip silently

## Outline

1. **Setup**: Run `.specify/extensions/adr/scripts/bash/setup-adr.sh --json` from repo root and parse JSON for `FEATURE_DIR`, `FEATURE_SPEC`, `ADR_DIR`, `ADR_TEMPLATE`, `NEXT_ADR_NUM`, `BRANCH`, `HAS_GIT`. If `setup-adr.sh` exits with `ERROR: spec.md not found`, report it back to the user and stop. For single quotes in args like "I'm Groot", use escape syntax: e.g 'I'\''m Groot' (or double-quote if possible: "I'm Groot").

2. **Load context**: Read `FEATURE_SPEC` (required) and `.specify/memory/constitution.md` (optional). Read `ADR_TEMPLATE` so you have the placeholder shape. Do **not** read `plan.md` or `research.md` even if they exist — this step intentionally captures decisions before deep planning.

3. **Parse `$ARGUMENTS`**:
   - **Empty `$ARGUMENTS`**: prompt the user for a decision title before proceeding. Do not auto-extract from any other artifact.
   - **`--supersede NNNN`**: resolve the older ADR via `.specify/extensions/adr/scripts/bash/list-adrs.sh --find "$ADR_DIR" NNNN`. Capture its filename and title. The new ADR's `Supersedes` field gets `NNNN-<old-slug>`; after writing the new ADR, patch the older file: set `**Status**: Superseded by <new-NNNN>-<new-slug>` and fill its `Superseded by` field with the same value.
   - **`--status accepted|proposed`**: control the new ADR's Status (default `Proposed`). Reject any other status value at this entry point.
   - **Otherwise**: treat the remaining `$ARGUMENTS` text (with flags stripped) as the decision title. Generate a kebab-case slug from the title (lowercase, alphanumerics and hyphens only, collapse repeats, trim hyphens, max ~60 chars).

4. **Render the ADR**: copy `ADR_TEMPLATE` content and fill placeholders:
   - Filename: `${ADR_DIR}/${NEXT_ADR_NUM}-<slug>.md`.
   - `[NNNN]` → the zero-padded number; `[SHORT TITLE OF DECISION]` → human title.
   - `**Status**` → `Proposed` by default, `Accepted` if `--status accepted` was passed.
   - `**Date**` → today's date (UTC, `YYYY-MM-DD`).
   - `**Feature**` → relative link to `${FEATURE_DIR}/spec.md` (e.g., `../spec.md`).
   - `**Spec reference**` → relative link to `${FEATURE_DIR}/spec.md` plus the section/user story/acceptance criterion that motivated the decision when identifiable; otherwise a bare link.
   - `**Supersedes**` → `NNNN-<old-slug>` when `--supersede` was passed, else `N/A`.
   - `**Superseded by**` → `N/A` (filled in only when a future ADR supersedes this one).
   - `## Context and Problem Statement` — populate from the user's prose; reference the spec section that motivated the decision; remove the `<!-- ACTION REQUIRED -->` comment.
   - `## Considered Options` — populate from the user's prose. Always 2+ options when defensible.
   - `## Decision Outcome` — chosen option + justification.
   - `### Consequences` — capture all three (Good / Bad / Neutral). Empty bullets are not acceptable; prompt the user for missing sections.
   - `## Pros and Cons of the Options` — fill only when the trade-off is non-trivial; otherwise delete the section.
   - `## More Information` — link to spec.md sections or external references; delete if no links.
   - `## Constitution Check` — evaluate the decision against `.specify/memory/constitution.md`; ERROR if a violation has no justification under Consequences. If the constitution is the placeholder template (no concrete principles), write `No constitution defined — gate skipped`.

5. **Update the ADR index**: ensure `${ADR_DIR}/README.md` exists. If creating it for the first time, write:
   ```markdown
   # Architecture Decision Records

   This directory holds ADRs for the current feature. New ADRs are created via `/speckit.adr`.

   | NNNN | Title | Status | Date | File |
   |------|-------|--------|------|------|
   ```
   Add (or replace) the row for the ADR written this run, plus the row for any ADR patched via `--supersede`. Sort the table by NNNN ascending.

6. **Update agent context**: append a one-liner (idempotent — only insert if not already present) inside the existing `<!-- SPECKIT START --> … <!-- SPECKIT END -->` markers in `CLAUDE.md` so future agents know to consult ADRs. Example: `Architecture decisions for the active feature live under specs/<feature>/adr/.` Use absolute paths for filesystem operations; relative for documentation references.

7. **Stop and report**: list the ADR written (number, title, status, absolute path) and any older ADR patched via `--supersede`.

8. **Check for extension hooks**: After reporting, check if `.specify/extensions.yml` exists in the project root.
   - If it exists, read it and look for entries under the `hooks.after_adr` key
   - If the YAML cannot be parsed or is invalid, skip hook checking silently and continue normally
   - Filter out hooks where `enabled` is explicitly `false`. Treat hooks without an `enabled` field as enabled by default.
   - For each remaining hook, do **not** attempt to interpret or evaluate hook `condition` expressions:
     - If the hook has no `condition` field, or it is null/empty, treat the hook as executable
     - If the hook defines a non-empty `condition`, skip the hook and leave condition evaluation to the HookExecutor implementation
   - When constructing slash commands from hook command names, replace dots (`.`) with hyphens (`-`). For example, `speckit.git.commit` → `/speckit-git-commit`.
   - For each executable hook, output the following based on its `optional` flag:
     - **Optional hook** (`optional: true`):
       ```
       ## Extension Hooks

       **Optional Hook**: {extension}
       Command: `/{command}`
       Description: {description}

       Prompt: {prompt}
       To execute: `/{command}`
       ```
     - **Mandatory hook** (`optional: false`):
       ```
       ## Extension Hooks

       **Automatic Hook**: {extension}
       Executing: `/{command}`
       EXECUTE_COMMAND: {command}
       ```
   - If no hooks are registered or `.specify/extensions.yml` does not exist, skip silently

## Phases

### Phase 0: Source the decision

- Always exactly one decision per invocation. Empty `$ARGUMENTS` → prompt the user for a title; non-empty `$ARGUMENTS` → treat as the title.

### Phase 1: Render & gate

- Render the ADR per the placeholder mapping in step 4.
- Run the Constitution Check gate. ERROR if a violation lacks a justification under Consequences.
- Update `${ADR_DIR}/README.md` index in numeric order.
- Patch the superseded ADR when `--supersede` was provided.

### Phase 2: Wire into agent context

- Idempotently inject the ADR pointer into `CLAUDE.md` between the SPECKIT markers.

## Key rules

- Use absolute paths for filesystem operations; project-relative paths for cross-references inside ADR content (e.g., `../spec.md`).
- ERROR with "Run /speckit.specify first" if `setup-adr.sh` reports `spec.md not found`.
- ERROR on Constitution Check violations that lack justification under Consequences.
- ERROR if `--supersede NNNN` references a number with no matching file.
- Never reuse an ADR number. The numeric prefix is monotonic; superseded ADRs stay in place with their original number.
- Filename slugs are kebab-case, max ~60 chars; do not include the number prefix in the slug.
- Status values are restricted to `Proposed`, `Accepted`, `Deprecated`, `Superseded by NNNN-<slug>`. Reject other values.
- Do not read `plan.md` or `research.md`. This step intentionally captures decisions before deep planning.
