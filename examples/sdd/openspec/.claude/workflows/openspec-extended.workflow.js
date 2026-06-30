export const meta = {
  name: 'openspec-extended',
  description: 'OpenSpec full lifecycle, granular + QA: explore (Opus) → proposal/specs/design/tasks as SEPARATE Opus agents → apply/implement (Sonnet) → verify (Opus) → sync-specs (Opus) → archive. Each OpenSpec command runs as its own agent. Pass args.description (and optionally args.name).',
  whenToUse: 'Drive an OpenSpec change end-to-end with one agent per artifact plus exploration, verification, and spec sync.',
  phases: [
    { title: 'Explore',   detail: 'Opus — investigate the codebase, surface risks (no code)', model: 'opus' },
    { title: 'Artifacts', detail: 'Opus — one agent per artifact: proposal → specs → design → tasks', model: 'opus' },
    { title: 'Apply',     detail: 'Sonnet — implement every task, tick tasks.md',             model: 'sonnet' },
    { title: 'Verify',    detail: 'Opus — completeness/correctness/coherence; fix-then-recheck', model: 'opus' },
    { title: 'Sync',      detail: 'Opus — merge delta specs into main specs',                 model: 'opus' },
    { title: 'Archive',   detail: 'Sonnet — archive the change (specs already synced)',        model: 'sonnet' },
  ],
}

// ─── Inputs (a background workflow cannot prompt — everything comes from args) ──
const A           = args || {}
const DESCRIPTION = A.description || '(no description supplied — pass args.description)'
const NAME        = A.name        || null            // kebab change name; derived if absent
const PROJECT     = A.projectPath || '.'
const STORE       = A.store       || null            // optional OpenSpec store id

const storeFlag = STORE ? ` --store ${STORE}` : ''
const storeNote = STORE
  ? `This work lives in OpenSpec store "${STORE}" — pass${storeFlag} on every store-aware command (new change, status, instructions, list, archive).`
  : `No store specified — commands act on the nearest local openspec/ root.`

const NONINTERACTIVE = (name) => `
NON-INTERACTIVE WORKFLOW — you are a background agent and CANNOT reach the user:
- Do NOT call AskUserQuestion. Make reasonable decisions and keep momentum.
- The change is already selected${name ? `: "${name}"` : ' (derive a kebab-case name from the description below and use it consistently)'}.
- Description of the work: ${DESCRIPTION}
- ${storeNote}
- Verify each file you write actually exists before reporting success.`

const CHANGE_SCHEMA = {
  type: 'object',
  additionalProperties: false,
  required: ['changeName', 'applyRequires', 'summary'],
  properties: {
    changeName:    { type: 'string', description: 'the kebab-case change name actually used' },
    applyRequires: { type: 'array', items: { type: 'string' }, description: 'artifact ids required before apply (from openspec status)' },
    summary:       { type: 'string', description: '1-3 sentence summary of the proposal created' },
  },
}

const VERIFY_SCHEMA = {
  type: 'object',
  additionalProperties: false,
  required: ['criticalCount', 'warningCount', 'incompleteTasks', 'report'],
  properties: {
    criticalCount:   { type: 'integer', description: 'number of CRITICAL issues (e.g. incomplete tasks, missing requirements)' },
    warningCount:    { type: 'integer', description: 'number of WARNING issues' },
    incompleteTasks: { type: 'array', items: { type: 'string' }, description: 'descriptions of tasks still incomplete' },
    report:          { type: 'string', description: 'concise verification report (completeness/correctness/coherence)' },
  },
}

// ─── Phase 1: EXPLORE — Opus (thinking only, no code) ──────────────────────────
phase('Explore')
const exploreNotes = await agent(
  `Read and FOLLOW the stance in ${PROJECT}/.claude/skills/openspec-explore/SKILL.md to investigate the
${PROJECT} codebase for the work described below. Map the relevant architecture, integration points,
constraints, options, and risks. DO NOT write code and DO NOT create OpenSpec artifacts yet — produce
notes that will ground the proposal.
${NONINTERACTIVE(NAME)}

Return concise markdown notes: problem framing, where it touches the codebase (real paths), 1-2 viable
approaches with tradeoffs, and open risks/unknowns.`,
  { model: 'opus', effort: 'high', phase: 'Explore', label: 'explore:opus' }
)
log('Explore complete — notes captured.')

// ─── Phase 2: ARTIFACTS — Opus, one agent per artifact (sequential, dep-ordered) ─
phase('Artifacts')

// 2a. Scaffold the change + create the FIRST artifact (proposal).
const proposed = await agent(
  `Create a new OpenSpec change for the work below, then create its FIRST artifact (the proposal).
Work from ${PROJECT}. First run \`openspec new change "<name>"${storeFlag}\` (if a change with that name
already exists, continue it instead of failing). Then read and FOLLOW
${PROJECT}/.claude/skills/openspec-continue-change/SKILL.md to create the proposal — the first "ready"
artifact reported by \`openspec status --change "<name>" --json${storeFlag}\`. Ground the proposal in the
exploration notes below.
${NONINTERACTIVE(NAME)}

EXPLORATION NOTES:
${exploreNotes}

Return the change name you used, its applyRequires list, and a short summary of the proposal.`,
  { model: 'opus', effort: 'high', phase: 'Artifacts', schema: CHANGE_SCHEMA, label: 'artifact:proposal' }
)
const changeName = proposed.changeName
log(`Change "${changeName}" scaffolded + proposal created — applyRequires: [${(proposed.applyRequires || []).join(', ')}]`)

// 2b. Remaining spec-driven artifacts, ONE separate Opus agent each, in dependency order.
//     openspec-continue-change auto-picks the next "ready" artifact, so calling it in sequence
//     yields specs → design → tasks. These have hard dependencies — run sequentially, not in parallel.
const REMAINING_ARTIFACTS = ['specs', 'design', 'tasks']
for (const artifactId of REMAINING_ARTIFACTS) {
  await agent(
    `Create the NEXT OpenSpec artifact for change "${changeName}" in ${PROJECT}. Read and FOLLOW
${PROJECT}/.claude/skills/openspec-continue-change/SKILL.md: check
\`openspec status --change "${changeName}" --json${storeFlag}\`, take the FIRST artifact whose status is
"ready" (expected: "${artifactId}"), read its completed dependency artifacts for context, and write it
to its resolvedOutputPath. Create exactly ONE artifact.
${NONINTERACTIVE(changeName)}

Return a one-line confirmation of which artifact you created and its path.`,
    { model: 'opus', effort: 'high', phase: 'Artifacts', label: `artifact:${artifactId}` }
  )
  log(`Artifact "${artifactId}" created for "${changeName}".`)
}

// ─── Phase 3: APPLY — Sonnet (implement the tasks = the code change) ───────────
phase('Apply')
const applyPrompt = (extra) =>
  `Read and FOLLOW the steps in ${PROJECT}/.claude/skills/openspec-apply-change/SKILL.md to implement
change "${changeName}" in the ${PROJECT} project.
${NONINTERACTIVE(changeName)}

Run \`openspec instructions apply --change "${changeName}" --json${storeFlag}\`, read every contextFile,
then implement EACH pending task: minimal focused code change, flip its checkbox \`- [ ]\` → \`- [x]\` in
tasks.md, and log intentional spec/design departures in deviations.md as you go. Keep going until all
tasks are complete.${extra || ''} Return a short summary and the final N/M task count.`

await agent(applyPrompt(), { model: 'sonnet', effort: 'xhigh', phase: 'Apply', label: 'apply:sonnet' })

// ─── Phase 4: VERIFY — Opus, with one bounded fix-then-recheck pass ────────────
phase('Verify')
const verifyPrompt = `Read and FOLLOW the steps in ${PROJECT}/.claude/skills/openspec-verify-change/SKILL.md
to verify the implementation of change "${changeName}" in ${PROJECT} against its artifacts. Assess
completeness, correctness, and coherence.
${NONINTERACTIVE(changeName)}
Return the structured counts, the list of any still-incomplete tasks, and a concise report.`

let verify = await agent(verifyPrompt, { model: 'opus', effort: 'high', phase: 'Verify', schema: VERIFY_SCHEMA, label: 'verify:opus' })
log(`Verify: ${verify.criticalCount} critical, ${verify.warningCount} warning, ${verify.incompleteTasks.length} incomplete tasks.`)

// One bounded remediation round: if tasks remain incomplete, do a Sonnet apply pass then re-verify once.
if (verify.incompleteTasks.length > 0) {
  log(`Remediating ${verify.incompleteTasks.length} incomplete task(s) with a Sonnet apply pass.`)
  await agent(
    applyPrompt(` Focus on these still-incomplete tasks: ${verify.incompleteTasks.join('; ')}.`),
    { model: 'sonnet', effort: 'xhigh', phase: 'Verify', label: 'apply:fix' }
  )
  verify = await agent(verifyPrompt, { model: 'opus', effort: 'high', phase: 'Verify', schema: VERIFY_SCHEMA, label: 'verify:recheck' })
  log(`Re-verify: ${verify.criticalCount} critical, ${verify.warningCount} warning, ${verify.incompleteTasks.length} incomplete tasks.`)
}

// ─── Phase 5: SYNC — Opus (merge delta specs into main specs) ──────────────────
phase('Sync')
await agent(
  `Read and FOLLOW the steps in ${PROJECT}/.claude/skills/openspec-sync-specs/SKILL.md to merge the delta
specs from change "${changeName}" into the main specs under ${PROJECT}/openspec/specs/. Apply
ADDED/MODIFIED/REMOVED/RENAMED operations intelligently and preserve existing content.
${NONINTERACTIVE(changeName)}
Return a short summary of which capabilities were updated. If the change has no delta specs, say so and do nothing.`,
  { model: 'opus', effort: 'high', phase: 'Sync', label: 'sync:opus' }
)
log(`Specs synced for "${changeName}".`)

// ─── Phase 6: ARCHIVE — Sonnet (specs already synced → just archive) ───────────
phase('Archive')
const archived = await agent(
  `Read and FOLLOW the steps in ${PROJECT}/.claude/skills/openspec-archive-change/SKILL.md to archive
change "${changeName}" in ${PROJECT}. The delta specs have ALREADY been synced to main specs in this
workflow, so do NOT sync again — archive directly: move the change into
openspec/changes/archive/YYYY-MM-DD-${changeName}/.
${NONINTERACTIVE(changeName)}
Return the archive path.`,
  { model: 'sonnet', effort: 'high', phase: 'Archive', label: 'archive:sonnet' }
)
log(`Archived "${changeName}".`)

return {
  changeName,
  applyRequires: proposed.applyRequires,
  verify,
  archived: true,
  archiveResult: archived,
}
