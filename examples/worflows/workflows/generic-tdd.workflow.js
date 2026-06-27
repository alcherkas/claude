export const meta = {
  name: 'generic-tdd',
  description: 'GENERIC, any project/task: plan (Opus/max) with a human APPROVAL GATE → dependency-aware breakdown (Opus/medium) → implement + verify, fix-until-green (Sonnet/xhigh)',
  phases: [
    { title: 'Plan',      detail: 'Opus, MAX — design; returns to caller for approval', model: 'opus' },
    { title: 'Breakdown', detail: 'Opus, MEDIUM — dep graph (only after approval)',      model: 'opus' },
    { title: 'Implement', detail: 'Sonnet, XHIGH — run tasks by dependency level',        model: 'sonnet' },
    { title: 'Verify',    detail: 'Sonnet, XHIGH — run the suite, fix until green',       model: 'sonnet' },
  ],
}

// ─── Inputs — everything project/task-specific comes from `args` ────────────
const A           = args || {}
const PROJECT     = A.projectPath || '.'
const TASK        = A.task        || '(no task supplied — pass args.task)'
const VERIFY_CMD  = A.verifyCmd   || null   // optional explicit build+test command; else discovered
const CONSTRAINTS = A.constraints || '(discover from README / CLAUDE.md / CONTRIBUTING)'
const APPROVED    = A.approvedPlan || null  // presence == "plan approved" → skip the Plan phase

// ─── Phase 1: PLAN — Opus, MAX effort (skipped once a plan is approved) ──────
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

  // ─── APPROVAL GATE ─────────────────────────────────────────────────────
  // A background workflow cannot block for interactive input. So stop here and
  // hand the plan back to the main loop, which shows it to the user, collects
  // approval (AskUserQuestion), then RE-INVOKES this same script with
  // args.approvedPlan set to the (possibly edited) approved text.
  return { stage: 'awaiting-approval', plan }
}
log('Plan approved — proceeding to breakdown + implementation.')

// ─── Phase 2: BREAKDOWN — Opus, MEDIUM effort ───────────────────────────────
phase('Breakdown')
const TASKS_SCHEMA = {
  type: 'object',
  additionalProperties: false,
  required: ['tasks'],
  properties: {
    tasks: {
      type: 'array',
      items: {
        type: 'object',
        additionalProperties: false,
        required: ['id', 'title', 'instructions', 'dependsOn', 'files', 'kind'],
        properties: {
          id:           { type: 'string', description: 'stable short id, e.g. t1' },
          title:        { type: 'string' },
          instructions: { type: 'string', description: 'self-contained instructions for an implementer editing the real files' },
          dependsOn:    { type: 'array', items: { type: 'string' }, description: 'task ids that must finish first (blocking edges)' },
          files:        { type: 'array', items: { type: 'string' }, description: 'ABSOLUTE path(s) this task edits' },
          kind:         { type: 'string', enum: ['test', 'impl', 'refactor', 'docs', 'config'] },
        },
      },
    },
  },
}

const breakdown = await agent(
  `Convert this APPROVED plan into a dependency-ordered work breakdown for the project at ${PROJECT}.

PLAN:
${plan}

Rules:
- Small, independently-actionable tasks (ideally one red/green step or one cohesive change).
- dependsOn encodes BLOCKING order (e.g. an impl task depends on its failing-test task; tasks editing the same
  file are chained). files lists the ABSOLUTE path(s) each task edits — discover the real paths under ${PROJECT}.
  The scheduler runs file-disjoint, dependency-free tasks in PARALLEL and serializes same-file tasks, so be accurate.
- ids short and stable (t1, t2, ...). Return only the structured tasks.`,
  { model: 'opus', effort: 'medium', phase: 'Breakdown', schema: TASKS_SCHEMA, label: 'breakdown:opus-medium' }
)
const tasks = breakdown.tasks
log(`Breakdown: ${tasks.length} tasks — ${tasks.map(t => `${t.id}(<-${(t.dependsOn || []).join(',') || 'none'})`).join('  ')}`)

// ─── Phase 3: IMPLEMENT — Sonnet, XHIGH (generic dependency scheduler) ───────
phase('Implement')
const done = new Set()
const summaries = {}
let remaining = [...tasks]
let round = 0
while (remaining.length) {
  round++
  const ready = remaining.filter(t => (t.dependsOn || []).every(d => done.has(d)))
  if (!ready.length) {
    log(`Stuck — unmet dependencies among [${remaining.map(t => t.id).join(', ')}]; aborting`)
    break
  }
  const usedFiles = new Set()
  const batch = []
  const deferred = []
  for (const t of ready) {
    const files = t.files || []
    if (files.some(f => usedFiles.has(f))) { deferred.push(t.id); continue }
    files.forEach(f => usedFiles.add(f))
    batch.push(t)
  }
  log(`Round ${round}: parallel [${batch.map(t => t.id).join(', ')}]` +
      (deferred.length ? `  | deferred (file conflict) [${deferred.join(', ')}]` : ''))

  const ran = await parallel(batch.map(t => () =>
    agent(
      `Implement this task by editing the REAL files in ${PROJECT}. Honor the project's conventions and constraints
(${CONSTRAINTS}). Read each target file before editing; make ONLY this task's change.

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

// ─── Phase 4: VERIFY + iterate — Sonnet, XHIGH ──────────────────────────────
phase('Verify')
const VERIFY_SCHEMA = {
  type: 'object', additionalProperties: false,
  required: ['passed', 'report'],
  properties: {
    passed: { type: 'boolean' },
    report: { type: 'string', description: 'key build/test output lines' },
  },
}
const runInstruction = VERIFY_CMD
  ? `Run exactly this build + test command and report the result:\n\n    ${VERIFY_CMD}\n`
  : `Determine this project's build + test command (from its build files / README) and run it.`

let green = false
let last = null
for (let attempt = 1; attempt <= 3 && !green; attempt++) {
  const v = await agent(
    `${runInstruction}\nReport passed=true ONLY if the build succeeds AND all tests pass. Put the decisive output lines in 'report'.`,
    { model: 'sonnet', effort: 'xhigh', phase: 'Verify', schema: VERIFY_SCHEMA, label: `verify:#${attempt}` }
  )
  last = v
  log(`Verify #${attempt}: passed=${v.passed}`)
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
