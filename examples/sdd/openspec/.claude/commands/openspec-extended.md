---
name: "OpenSpec: Extended workflow"
description: Run the full OpenSpec lifecycle granularly (explore → per-artifact → apply → verify → sync → archive) as one orchestrated workflow
category: Workflow
tags: [workflow, orchestration, openspec]
---

Launch the **`openspec-extended`** dynamic workflow: explore (Opus) → proposal/specs/design/tasks as
SEPARATE Opus agents → apply/implement (Sonnet) → verify (Opus) → sync-specs (Opus) → archive. Each
OpenSpec command runs as its own agent.

**Input**: The text after `/openspec-extended` describes what to build (or a kebab-case change name).

**Steps**

1. **Resolve the description.**
   - If `$ARGUMENTS` is non-empty, use it as the work description.
   - If empty, ask the user (AskUserQuestion, open-ended): "What change do you want to build?" Do NOT
     launch the workflow without a description.

2. **Launch the workflow.** Call the **Workflow** tool:
   ```
   Workflow({ name: 'openspec-extended', args: { description: '<the description>' } })
   ```
   Pass `args.name` (kebab-case) too if the user gave an explicit change name, and `args.store` if they
   named an OpenSpec store. The workflow runs in the background — watch progress with `/workflows`.

3. **Report the result.** When the workflow completes, summarize the returned `changeName`, the verify
   report, and the archive location.

**Notes**
- The workflow itself is non-interactive (it cannot prompt mid-run), so make sure the description is
  clear before launching.
- For the lean happy path (one propose agent, no verify/sync), use `/openspec-core` instead.
