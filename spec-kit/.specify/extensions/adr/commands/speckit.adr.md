---
description: "Capture an architectural decision as a numbered ADR file under specs/<feature>/adr/"
---

# Capture Architecture Decision Record

Create a numbered ADR file recording an architectural decision for the active feature. Invoked any time after `/speckit.specify`; does not require `plan.md`.

## User Input

```text
$ARGUMENTS
```

You **MUST** consider the user input before proceeding (if not empty).

## Prerequisites

- The active feature must have `spec.md` (run `/speckit.specify` first if missing).
- `plan.md` and `research.md` are intentionally not consulted — this step captures decisions before deep planning.

## Execution

Run the appropriate script from the project root:

- **Bash**: `.specify/extensions/adr/scripts/bash/setup-adr.sh --json`

Parse the JSON output for: `FEATURE_DIR`, `FEATURE_SPEC`, `ADR_DIR`, `ADR_TEMPLATE`, `NEXT_ADR_NUM`, `BRANCH`, `HAS_GIT`.

If the script exits with `ERROR: spec.md not found`, report it back to the user and stop.

## Argument forms

- `<title>` → write a single ADR with that title (Status: Proposed by default).
- `<title> --status accepted` → write with Status: Accepted.
- `<title> --supersede NNNN` → write the new ADR, then patch the older ADR's Status to `Superseded by <new-NNNN>-<new-slug>` and fill its `Superseded by` field.
- empty → prompt user for a title.

## Output

- `${ADR_DIR}/<NEXT_ADR_NUM>-<kebab-title>.md`
- Refreshed `${ADR_DIR}/README.md` index (header + one row per ADR, sorted by number).

## Graceful Degradation

- If `.specify/memory/constitution.md` is the placeholder, write `No constitution defined — gate skipped` in the Constitution Check section instead of erroring.
- If git is unavailable, the optional `before_adr` / `after_adr` auto-commit hooks are skipped silently.

## Output format

Report each ADR written: number, title, status, absolute path. If `--supersede` was used, also report the older ADR's path and its new Status line.
