export const meta = {
  name: 'openspec-core',
  description: 'OpenSpec lean lifecycle: propose ALL artifacts (Opus) → apply/implement (Sonnet) → archive. Each OpenSpec command runs as its own agent. Pass args.description (and optionally args.name).',
  whenToUse: 'Drive a single OpenSpec change end-to-end on the happy path with minimal agents.',
  phases: [
    { title: 'Propose', detail: 'Opus — create the change + all apply-required artifacts', model: 'opus' },
    { title: 'Apply',   detail: 'Sonnet — implement every task, tick tasks.md',            model: 'sonnet' },
    { title: 'Archive', detail: 'Sonnet — sync delta specs then archive the change',        model: 'sonnet' },
  ],
}

// ─── Inputs (a background workflow cannot prompt — everything comes from args) ──
const A           = args || {}
const DESCRIPTION = A.description || '(no description supplied — pass args.description)'
const NAME        = A.name        || null            // kebab change name; derived if absent
const PROJECT     = A.projectPath || '.'
const STORE       = A.store       || null            // optional OpenSpec store id

const storeFlag  = STORE ? ` --store ${STORE}` : ''
const storeNote  = STORE
  ? `This work lives in OpenSpec store "${STORE}" — pass${storeFlag} on every store-aware command (new change, status, instructions, list, archive).`
  : `No store specified — commands act on the nearest local openspec/ root.`

// The non-interactive contract every agent must honor.
const NONINTERACTIVE = (name) => `
NON-INTERACTIVE WORKFLOW — you are a background agent and CANNOT reach the user:
- Do NOT call AskUserQuestion / TodoWrite-for-input. Make reasonable decisions and keep momentum.
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
    summary:       { type: 'string', description: '1-3 sentence summary of the artifacts created' },
  },
}

// ─── Phase 1: PROPOSE — Opus (create change + all planning artifacts) ──────────
phase('Propose')
const proposed = await agent(
  `Read and FOLLOW the steps in ${PROJECT}/.claude/skills/openspec-propose/SKILL.md to create a new
OpenSpec change and generate ALL artifacts required for implementation (proposal, specs, design, tasks)
for the work described below. Work from the ${PROJECT} directory.
${NONINTERACTIVE(NAME)}

Concretely: run \`openspec new change "<name>"${storeFlag}\`, then loop
\`openspec status --change "<name>" --json${storeFlag}\` and
\`openspec instructions <artifact-id> --change "<name>" --json${storeFlag}\`, writing each ready artifact
to its resolvedOutputPath until every id in applyRequires has status "done". If a change with the name
already exists, continue it instead of failing.

Return the change name you used, its applyRequires list, and a short summary.`,
  { model: 'opus', effort: 'high', phase: 'Propose', schema: CHANGE_SCHEMA, label: 'propose:opus' }
)
const changeName = proposed.changeName
log(`Proposed change "${changeName}" — applyRequires: [${(proposed.applyRequires || []).join(', ')}]`)

// ─── Phase 2: APPLY — Sonnet (implement the tasks = the code change) ───────────
phase('Apply')
await agent(
  `Read and FOLLOW the steps in ${PROJECT}/.claude/skills/openspec-apply-change/SKILL.md to implement
change "${changeName}" in the ${PROJECT} project.
${NONINTERACTIVE(changeName)}

Concretely: \`openspec status --change "${changeName}" --json${storeFlag}\` and
\`openspec instructions apply --change "${changeName}" --json${storeFlag}\`, read every contextFile,
then implement EACH task: make the minimal focused code change, flip its checkbox \`- [ ]\` → \`- [x]\`
in tasks.md, and record any intentional spec/design departures in deviations.md as you go. Keep going
until all tasks are complete. Return a short summary of what you implemented and the final N/M task count.`,
  { model: 'sonnet', effort: 'xhigh', phase: 'Apply', label: 'apply:sonnet' }
)

// ─── Phase 3: ARCHIVE — Sonnet (sync specs, then move to archive) ──────────────
phase('Archive')
const archived = await agent(
  `Read and FOLLOW the steps in ${PROJECT}/.claude/skills/openspec-archive-change/SKILL.md to finalize
change "${changeName}" in the ${PROJECT} project.
${NONINTERACTIVE(changeName)}

Because this is non-interactive: if the change has delta specs, SYNC them into the main specs first
(follow openspec-sync-specs/SKILL.md), THEN archive — do not pause to ask whether to sync. Move the
change into openspec/changes/archive/YYYY-MM-DD-${changeName}/. Return the archive path.`,
  { model: 'sonnet', effort: 'high', phase: 'Archive', label: 'archive:sonnet' }
)
log(`Archived "${changeName}".`)

return { changeName, applyRequires: proposed.applyRequires, archived: true, archiveResult: archived }
