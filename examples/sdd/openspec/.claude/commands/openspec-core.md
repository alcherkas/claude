---
name: "OpenSpec: Core workflow"
description: Run the full OpenSpec lean lifecycle (propose → apply → archive) as one orchestrated workflow
category: Workflow
tags: [workflow, orchestration, openspec]
---

Launch the **`openspec-core`** dynamic workflow: propose ALL artifacts (Opus) → apply/implement
(Sonnet) → archive. Each OpenSpec command runs as its own agent.

**Input**: The text after `/openspec-core` describes what to build (or a kebab-case change name).

**Steps**

1. **Resolve the description.**
   - If `$ARGUMENTS` is non-empty, use it as the work description.
   - If empty, ask the user (AskUserQuestion, open-ended): "What change do you want to build?" Do NOT
     launch the workflow without a description.

2. **Launch the workflow.** Call the **Workflow** tool:
   ```
   Workflow({ name: 'openspec-core', args: { description: '<the description>' } })
   ```
   Pass `args.name` (kebab-case) too if the user gave an explicit change name, and `args.store` if they
   named an OpenSpec store. The workflow runs in the background — watch progress with `/workflows`.

3. **Report the result.** When the workflow completes, summarize the returned `changeName` and archive
   location, and surface anything the workflow flagged.

**Notes**
- The workflow itself is non-interactive (it cannot prompt mid-run), so make sure the description is
  clear before launching.
- For exploration + per-artifact agents + verify + spec-sync, use `/openspec-extended` instead.
