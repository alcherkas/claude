---
title: "Workflow recipe: plan → approve → implement (Claude Code)"
theme: examples
tags:
- examples
- orchestration
---

# Workflow recipe: plan → approve → implement

!!! info "About this page"
    A **practitioner recipe**, not an external source: a reusable Claude Code [`Workflow`](https://docs.claude.com/en/docs/claude-code/) script that **plans** a change, **pauses for human approval**, then **breaks the plan into a dependency graph** and **implements + verifies** it. It demonstrates three knobs worth sharing with a team: per-agent **model** selection, per-agent **reasoning effort**, and a **blocking/parallel dependency scheduler** written in plain JS. Drop the script into a session and run it with the `Workflow` tool, or adapt it.

## TL;DR

- A Claude Code workflow is a deterministic JS script that orchestrates subagents. Each `agent()` call takes `model` (`'opus'`/`'sonnet'`/`'haiku'`/`'fable'`) and `effort` (`'low'`→`'max'`) — so you can spend a top-tier model at max effort where it matters (planning) and a cheaper tier elsewhere (mechanical edits).
- **Dependencies** are just data: the breakdown agent emits tasks with `dependsOn` (blocking edges) and `files` (what each edits). A ~15-line scheduler runs dependency-free, file-disjoint tasks **in parallel** and **serializes** anything that shares a file.
- **Human approval mid-run** isn't a built-in pause — a background workflow can't block for input. You implement the gate by **splitting at the plan boundary**: the script returns the plan, the main loop asks the user to approve/edit, then re-invokes the same script with `args.approvedPlan` set.
- Workflows are **opt-in**: a teammate triggers one by saying *"use a workflow"* (or invoking a skill that calls it). They can spend many agents' worth of tokens, so that opt-in is deliberate.

## When to use this

Reach for it when a change is big enough that you want a reviewed plan before code is written, and the work decomposes into steps with real ordering (TDD red→green, migrations, multi-file refactors). For a quick one-file edit, just ask Claude directly — the orchestration overhead isn't worth it. See Anthropic's [Building Effective Agents](../orchestration/anthropic/building-effective-agents.md) for the workflow-vs-agent distinction and the orchestrator-worker pattern this builds on, and [How we built our multi-agent research system](../orchestration/anthropic/how-we-built-our-multi-agent-research-system.md) for a production planner/sub-agent case study.

## The four phases

| Phase | Model · effort | What it does |
|---|---|---|
| **Plan** | Opus · `max` | Explores the repo, writes a precise implementation plan. Returns to the caller for approval. |
| **Breakdown** | Opus · `medium` | Converts the *approved* plan into a `dependsOn` + `files` task graph (schema-validated). |
| **Implement** | Sonnet · `xhigh` | Dependency scheduler runs tasks level-by-level: parallel where file-disjoint, serialized on conflict. |
| **Verify** | Sonnet · `xhigh` | Runs the project's build/tests; on failure, a fix agent edits and the suite re-runs (up to 3×). |

## How the approval gate works

A background workflow is non-interactive, and `AskUserQuestion` is a main-loop tool — a subagent can't call it. So the gate lives at the boundary between **two invocations**, toggled by `args.approvedPlan`:

1. **Call 1 — plan only.** The script runs the Plan phase and `return { stage: 'awaiting-approval', plan }`.
2. **Main loop.** Claude shows the plan and asks you to approve / edit / reject.
3. **Call 2 — run the rest.** Re-invoke the *same script* with `args.approvedPlan = <approved or edited text>`. The `if (!plan)` guard skips planning; breakdown → implement → verify run.

```text
Workflow({ scriptPath, args: { projectPath, task, verifyCmd } })
        └─► { stage: "awaiting-approval", plan }      ← Call 1 (Plan only)
                │
         AskUserQuestion: approve / edit / reject       ← main loop, human in the loop
                │
Workflow({ scriptPath, args: { projectPath, task, verifyCmd, approvedPlan } })
        └─► breakdown → implement → verify → green      ← Call 2
```

!!! tip "Edit-before-approve, for free"
    Because the approved plan is passed back as plain text, the human can **edit** it first — the edited markdown simply becomes `approvedPlan`. If you don't need edits, use `resumeFromRunId` on Call 2 instead and the cached Plan result is reused at no extra cost.

## How dependencies become parallel/serial work

The breakdown agent returns, per task: `id`, `dependsOn` (ids that must finish first), and `files` (absolute paths it edits). The scheduler then loops:

- **`ready`** = tasks whose `dependsOn` are all done → *blocking* respected.
- Among `ready`, greedily pick a batch with **pairwise-disjoint `files`** → run that batch with `parallel()`.
- Tasks that would touch an already-claimed file are **deferred** to the next round → *serialized* (no two agents clobber one file).

For TDD this naturally serializes the red→green chain while letting genuinely independent work (e.g. a docs edit in another file) run alongside it.

!!! warning "Parallel edits to the same file"
    The scheduler keeps two agents from editing the *same* file at once. If you want tasks editing *different* files to also be fully isolated, give each implement agent `isolation: 'worktree'` — but that's heavier (a git worktree per agent) and the results then need merging, so only reach for it when parallel writers would genuinely conflict.

## The script

Save as `plan-approve-implement.workflow.js`, then run it with the `Workflow` tool (`scriptPath`) — or paste it inline. It is project-agnostic: everything specific comes from `args` (`projectPath`, `task`, optional `verifyCmd`, optional `constraints`, and `approvedPlan` for Call 2).

````js
export const meta = {
  name: 'plan-approve-implement',
  description: 'GENERIC: plan (Opus/max) with a human APPROVAL GATE → dependency-aware breakdown (Opus/medium) → implement + verify, fix-until-green (Sonnet/xhigh)',
  phases: [
    { title: 'Plan',      detail: 'Opus, MAX — design; returns to caller for approval', model: 'opus' },
    { title: 'Breakdown', detail: 'Opus, MEDIUM — dep graph (only after approval)',      model: 'opus' },
    { title: 'Implement', detail: 'Sonnet, XHIGH — run tasks by dependency level',        model: 'sonnet' },
    { title: 'Verify',    detail: 'Sonnet, XHIGH — run the suite, fix until green',       model: 'sonnet' },
  ],
}

// ── Inputs — everything project/task-specific comes from `args` ─────────────
const A           = args || {}
const PROJECT     = A.projectPath || '.'
const TASK        = A.task        || '(no task supplied — pass args.task)'
const VERIFY_CMD  = A.verifyCmd   || null   // optional explicit build+test command; else discovered
const CONSTRAINTS = A.constraints || '(discover from README / CLAUDE.md / CONTRIBUTING)'
const APPROVED    = A.approvedPlan || null  // presence == "plan approved" → skip the Plan phase

// ── Phase 1: PLAN — Opus, MAX effort (skipped once a plan is approved) ───────
let plan = APPROVED
if (!plan) {
  phase('Plan')
  plan = await agent(
    `You are a senior engineer planning a change in the project at ${PROJECT}.

TASK:
${TASK}

CONSTRAINTS: ${CONSTRAINTS}

First EXPLORE to ground the plan in reality: read the README and build files (package.json, pom.xml,
build.gradle, Makefile, pyproject.toml, Cargo.toml, etc.), any CLAUDE.md / CONTRIBUTING docs, and sample
the key source and test files. Infer the language, layout, conventions, and how tests are built and run.

Then produce a precise implementation plan: design decisions; the exact change per file (real paths + symbols);
and an ordered red/green TDD step list. This plan is shown to a HUMAN for approval and then handed to a
breakdown agent, so be concrete and self-contained. Return markdown.`,
    { model: 'opus', effort: 'max', phase: 'Plan', label: 'plan:opus-max' }
  )

  // APPROVAL GATE: a background workflow can't block for input, so stop here and
  // hand the plan back. The main loop shows it, gets approval, then RE-INVOKES
  // this script with args.approvedPlan set to the (possibly edited) text.
  return { stage: 'awaiting-approval', plan }
}
log('Plan approved — proceeding to breakdown + implementation.')

// ── Phase 2: BREAKDOWN — Opus, MEDIUM effort ────────────────────────────────
phase('Breakdown')
const TASKS_SCHEMA = {
  type: 'object', additionalProperties: false, required: ['tasks'],
  properties: {
    tasks: { type: 'array', items: {
      type: 'object', additionalProperties: false,
      required: ['id', 'title', 'instructions', 'dependsOn', 'files', 'kind'],
      properties: {
        id:           { type: 'string', description: 'stable short id, e.g. t1' },
        title:        { type: 'string' },
        instructions: { type: 'string', description: 'self-contained instructions for an implementer editing the real files' },
        dependsOn:    { type: 'array', items: { type: 'string' }, description: 'task ids that must finish first (blocking edges)' },
        files:        { type: 'array', items: { type: 'string' }, description: 'ABSOLUTE path(s) this task edits' },
        kind:         { type: 'string', enum: ['test', 'impl', 'refactor', 'docs', 'config'] },
      },
    } },
  },
}

const breakdown = await agent(
  `Convert this APPROVED plan into a dependency-ordered work breakdown for the project at ${PROJECT}.

PLAN:
${plan}

Rules:
- Small, independently-actionable tasks (ideally one red/green step or one cohesive change).
- dependsOn encodes BLOCKING order. files lists the ABSOLUTE path(s) each task edits — discover the real paths
  under ${PROJECT}. The scheduler runs file-disjoint, dependency-free tasks in PARALLEL and serializes same-file
  tasks, so be accurate. ids short + stable. Return only the structured tasks.`,
  { model: 'opus', effort: 'medium', phase: 'Breakdown', schema: TASKS_SCHEMA, label: 'breakdown:opus-medium' }
)
const tasks = breakdown.tasks
log(`Breakdown: ${tasks.length} tasks — ${tasks.map(t => `${t.id}(<-${(t.dependsOn || []).join(',') || 'none'})`).join('  ')}`)

// ── Phase 3: IMPLEMENT — Sonnet, XHIGH (dependency scheduler) ────────────────
phase('Implement')
const done = new Set()
const summaries = {}
let remaining = [...tasks]
let round = 0
while (remaining.length) {
  round++
  const ready = remaining.filter(t => (t.dependsOn || []).every(d => done.has(d)))
  if (!ready.length) { log(`Stuck — unmet deps among [${remaining.map(t => t.id).join(', ')}]`); break }
  const usedFiles = new Set(); const batch = []; const deferred = []
  for (const t of ready) {
    const files = t.files || []
    if (files.some(f => usedFiles.has(f))) { deferred.push(t.id); continue }
    files.forEach(f => usedFiles.add(f)); batch.push(t)
  }
  log(`Round ${round}: parallel [${batch.map(t => t.id).join(', ')}]` +
      (deferred.length ? `  | deferred (file conflict) [${deferred.join(', ')}]` : ''))
  const ran = await parallel(batch.map(t => () =>
    agent(
      `Implement this task by editing the REAL files in ${PROJECT}. Honor the project's conventions and
constraints (${CONSTRAINTS}). Read each target file before editing; make ONLY this task's change.

TASK ${t.id} — ${t.title} [${t.kind}]
${t.instructions}

Editable file(s): ${(t.files || []).join(', ')}
Return a 1–3 sentence summary of exactly what you changed.`,
      { model: 'sonnet', effort: 'xhigh', phase: 'Implement', label: `impl:${t.id}` }
    )
  ))
  batch.forEach((t, i) => { done.add(t.id); summaries[t.id] = ran[i] })
  remaining = remaining.filter(t => !batch.includes(t))
}

// ── Phase 4: VERIFY + iterate — Sonnet, XHIGH ───────────────────────────────
phase('Verify')
const VERIFY_SCHEMA = {
  type: 'object', additionalProperties: false, required: ['passed', 'report'],
  properties: { passed: { type: 'boolean' }, report: { type: 'string' } },
}
const runInstruction = VERIFY_CMD
  ? `Run exactly this build + test command and report the result:\n\n    ${VERIFY_CMD}\n`
  : `Determine this project's build + test command (from its build files / README) and run it.`

let green = false, last = null
for (let attempt = 1; attempt <= 3 && !green; attempt++) {
  const v = await agent(
    `${runInstruction}\nReport passed=true ONLY if the build succeeds AND all tests pass. Put the decisive output lines in 'report'.`,
    { model: 'sonnet', effort: 'xhigh', phase: 'Verify', schema: VERIFY_SCHEMA, label: `verify:#${attempt}` }
  )
  last = v; log(`Verify #${attempt}: passed=${v.passed}`)
  if (v.passed) { green = true; break }
  if (attempt < 3) {
    await agent(
      `The change in ${PROJECT} does not build/pass yet. Fix it (red → green), editing real files; honor the
project's constraints (${CONSTRAINTS}) and do not weaken existing tests.\n\nFailure output:\n${v.report}\n
After editing, briefly state the fix.`,
      { model: 'sonnet', effort: 'xhigh', phase: 'Verify', label: `fix:#${attempt}` }
    )
  }
}

return { green, taskCount: tasks.length, rounds: round, finalVerify: last, implementationSummaries: summaries }
````

## Running it (any project / any language)

The same script drives any repo — only `args` change:

| Project | `task` | `verifyCmd` |
|---|---|---|
| Node | `"Add rate-limiting to the public API"` | `"npm test"` |
| Python | `"Add a --json flag to the CLI"` | `"pytest -q"` |
| Go | `"Cache the resolver results"` | `"go test ./..."` |
| Java (no deps) | `"Support vinculum notation in RomanNumerals"` | `"javac src/*.java test/*.java && java -cp . TestRunner"` |

Leave `verifyCmd` out and the verify agent infers the build/test command from the repo.

## Caveats

- **Cost & opt-in.** A full run spends an Opus/max plan, an Opus/medium breakdown, and several Sonnet/xhigh agents. Workflows only start on explicit opt-in (*"use a workflow"*).
- **Effort isn't free, and more isn't always better.** Match effort to the task; on some long agentic tasks higher effort doesn't help (see the [Opus 4.8 system-card analysis](../system-card-analysis/opus-4-8.md), recommendation **T7**).
- **Verify out-of-band.** Don't let an implement agent self-certify "tests pass" — the Verify phase runs the suite independently and gates on real output (cf. the same analysis, **T3**/**T4**).
- **Same-file parallelism** is prevented by the scheduler; cross-file isolation needs `isolation: 'worktree'` and a merge step.

## Related sources in this wiki

- [Building Effective Agents](../orchestration/anthropic/building-effective-agents.md) — workflows vs agents; orchestrator-workers; prompt chaining; evaluator-optimizer
- [How we built our multi-agent research system](../orchestration/anthropic/how-we-built-our-multi-agent-research-system.md) — lead planner + parallel subagents in production
- [Developer's guide to multi-agent patterns in ADK](../context-engineering/google/developer-s-guide-to-multi-agent-patterns-in-adk.md) — sequential / parallel / loop / coordinator primitives
- [Claude Opus 4.8 — System Card Analysis](../system-card-analysis/opus-4-8.md) — effort/budget tuning (**T7**) and verify-don't-trust (**T3**/**T4**)
