# Defense in depth: stacking context-management safeguards into one coherent harness

> **Source:** deep-research run for the **Defense-in-depth bundle** prompt (#10 —
> the capstone) in [`../research-prompts.md`](../research-prompts.md). Search
> angles: (a) how the layers stack and **in what order they fire** across the
> request lifecycle; (b) reference architectures / case studies with cited
> specifics (Anthropic's *Effective context engineering*, *Effective harnesses
> for long-running agents*, *Building agents with the Claude Agent SDK*, the
> context-engineering cookbook, the hooks / sub-agents / memory docs, the Agent
> SDK); (c) inter-layer **interactions and conflicts** (blocking hook vs.
> auto-compaction; caching invalidated by curated loaders / CLAUDE.md edits;
> PostToolUse trimming vs. the cache prefix; a hook that mutates history; meter
> accuracy after `/compact`); (d) how to **integration-test** a whole stack, not
> just unit-test each hook. **22 claims survived 3-vote adversarial verification**
> (21 at 3-0, one at 2-1; 3 refuted, listed below), merged here and **built on the
> sibling reports** rather than re-derived. Hook-lifecycle ordering cross-checked
> **live** against [`code.claude.com/docs/en/hooks`](https://code.claude.com/docs/en/hooks)
> on 2026-06-23. Current as of **mid-2026** (Claude Code v2.1.x — v2.1.117 forks,
> v2.1.121 `updatedToolOutput`, v2.1.132 statusline semantics, v2.1.59 auto-memory,
> local install v2.1.186; Opus 4.8 / Sonnet 4.6 / Haiku 4.5, 200k default / 1M
> extended context; API betas `compact-2026-01-12`, `context-management-2025-06-27`).

**Bottom line.** No single feature keeps a long agentic coding session under its
window — production harnesses **stack** safeguards and let each cover the failure
mode the others structurally can't see. Anthropic's own guidance is explicit that
the techniques are **complementary, chosen per workload** (compaction for
back-and-forth, note-taking/memory for milestone work, sub-agents for parallel
exploration), and that **compaction alone is insufficient** for long-running
agents — it must be paired with external state offloading. The layers fire in a
fixed lifecycle order — **SessionStart (curate/load) → UserPromptSubmit (gate
input) → PreToolUse (prevent oversized pulls) → tool exec → PostToolUse (trim
output) → Stop → [near the limit] PreCompact → compaction → PostCompact** — with
**prompt caching running underneath every API call** and the **statusline meter
observing out-of-band, consuming no tokens**. The governing principle from the
master report holds: **prevent / curate (cheap, lossless) → offload → reuse
(caching) → react / compact (lossy, last resort)**. The single structural tension
that runs through the whole stack is the same one the master report flagged:
**every layer that trims, clears, or rewrites context invalidates a prompt-cache
prefix** — so the *reaction* layers (PostToolUse rewrite, tool-result clearing,
compaction) and the *reuse* layer (caching) pull against each other, and the art
is sequencing them so a clear/compact is large enough to be worth the cache it
burns. You **integration-test** the stack — not just the hooks — with headless
`claude -p` A/B runs (guard-on vs guard-off, additive config because `--settings`
*merges* and can't subtract a hook), transcript-JSONL token accounting (peak
input-side context = the documented `used_percentage` formula), and order/
no-deadlock assertions from the per-event hook logs.

**Scope trap (flagged throughout, inherited from the master report).** The hard
API numbers — clear-trigger **100K** / keep **3**, compaction min **50K** /
default **150K**, the typed `compact_20260112` / `clear_tool_uses_20250919` /
`memory_20250818` strategies — are **Claude Developer Platform / API** features
behind beta headers. The **Claude Code CLI** wires several of these in for you
(auto-compaction, the two memory systems) but its exact auto-compaction *trigger*
is **unpublished** (the "83% / ~167k" figure is **refuted** — see
[`08-statusline-context-meter.md`](08-statusline-context-meter.md)). Do not
conflate API defaults with CLI behavior.

---

## 1. The seven layers, mapped to the prompt's taxonomy

Every layer below is a *distinct* control point with a *distinct* failure mode it
covers. The defense-in-depth thesis is that you run several at once because none
is sufficient alone — and Anthropic states this directly: the long-horizon
techniques are **complementary, matched to task type**, not applied identically
(*Effective context engineering for AI agents*: "Compaction maintains
conversational flow for tasks requiring extensive back-and-forth; Note-taking
excels for iterative development with clear milestones; Multi-agent architectures
handle complex research and analysis where parallel exploration pays dividends"
— **3-0, primary**).

| # | Layer (prompt's term) | Mechanism | CLI vs API | Lossy? | Breaks cache? | Sibling report |
|---|---|---|---|:---:|:---:|---|
| 1 | **Curation** (lean baseline / loaders) | Keep CLAUDE.md lean; `SessionStart` hook prints context to stdout (becomes model-visible context); two memory systems (CLAUDE.md + auto memory) | CLI | No | See §4 | [`02`](02-context-check-estimator.md) (pre-flight survey) |
| 2 | **Prevention** (input gate) | `PreToolUse` deny/rewrite an oversized `Read`/`Bash` before bytes land | CLI | No | No | [`01-pretooluse-input-gate.md`](01-pretooluse-input-gate.md) |
| 3 | **Offloading** (subagents / memory) | Heavy read runs in a fresh isolated context; only a distilled summary returns; memory tool persists state outside the window | CLI + API | No (main ctx) | Mostly no | [`04-subagent-offloading.md`](04-subagent-offloading.md) |
| 4 | **Reuse** (prompt caching) | Stable `tools→system→messages` prefix read at ~0.1× | CLI (auto) + API | No | n/a | [`05-prompt-caching.md`](05-prompt-caching.md) |
| 5 | **Observation** (meter / `/context`) | Statusline `context_window.used_percentage`; `/context` breakdown — out-of-band, **zero API tokens** | CLI | No | No | [`08-statusline-context-meter.md`](08-statusline-context-meter.md) |
| 6 | **Reaction** (output trim) | `PostToolUse` `updatedToolOutput` replaces a bulky result with head+tail+pointer; API tool-result clearing | CLI + API | Partial (spool to recover) | **Yes** | [`03-posttooluse-output-trimmer.md`](03-posttooluse-output-trimmer.md) |
| 7 | **Compaction** (summarize history) | Whole-transcript summary into a typed compaction block; CLI auto + `/compact`, gated by `PreCompact` | CLI + API | **Yes** | Yes | [`00-master-context-overflow.md`](00-master-context-overflow.md) §3 |

**Why each earns a slot (the non-overlap argument).** The prevention gate (#2)
*can't size what it can't predict* — it sees a `Bash` command, not its output;
a `WebFetch` URL, not the page size. The reaction trimmer (#6) is the *only*
documented point to bound those *after* the bytes exist but *before* they hit
context (per [`03`](03-posttooluse-output-trimmer.md): "intercept at `PreToolUse`
for outbound tool inputs and `PostToolUse` for inbound tool results"). Offloading
(#3) keeps a *whole investigation* out of the main window. Compaction (#7) is the
survival mechanism for *accumulated history* that none of the above touch. Caching
(#4) makes the unavoidable re-send cheap. Observation (#5) tells you which lever to
pull and when. This is exactly the master report's decision framework, now run as a
*stack* instead of a *choice tree*.

---

## 2. The firing-order timeline (the deliverable's part 1)

Hook events fire at fixed, documented lifecycle points
([`claude.com/blog/how-to-configure-hooks`](https://claude.com/blog/how-to-configure-hooks),
**3-0, primary**; cross-checked live against
[`code.claude.com/docs/en/hooks`](https://code.claude.com/docs/en/hooks)
2026-06-23): SessionStart "when a session begins or resumes"; UserPromptSubmit
"when you submit a prompt"; PreToolUse "after Claude chooses a tool to use but
before the tool actually executes"; PostToolUse "after a tool completes
successfully"; PreCompact "before Claude compacts the conversation context"; Stop
"when Claude finishes responding." The live fetch added three more the blog
omits: **PostCompact** ("after context compaction completes"), **SubagentStop**
("when a subagent finishes"), and **SessionEnd** ("when a session terminates") —
all observability-only except SubagentStop, which can block.

> ⚠️ **Refuted, do not repeat:** "Claude Code provides **eight** hook events
> including a `PermissionRequest` event" was **refuted (0-3)**. The real event set
> in mid-2026 is SessionStart, UserPromptSubmit, PreToolUse, PostToolUse,
> PreCompact, PostCompact, Stop, SubagentStop, SessionEnd (plus a `PostToolUseFailure`
> companion to PostToolUse). There is no `PermissionRequest` event.

### The lifecycle, with each layer slotted in

```
┌─ SESSION BEGINS / RESUMES ─────────────────────────────────────────────┐
│ [L1 CURATE]  SessionStart hook fires.                                   │
│              · stdout on exit 0 is ADDED AS CONTEXT the model sees      │
│                (a loader can print plain text — no JSON needed).        │
│              · CLAUDE.md loaded in full; auto memory (MEMORY.md) loaded.│
│              · This is the prompt's "curation / SessionStart loaders".  │
│ [L4 REUSE]   First API call WRITES the cache (tools→system→messages     │
│              prefix). cache_creation_input_tokens > 0, cache_read = 0.  │
└─────────────────────────────────────────────────────────────────────────┘
        │  (statusline [L5] renders here and after every assistant msg —
        │   out-of-band, consuming NO API tokens)
        ▼
┌─ USER SUBMITS A PROMPT ────────────────────────────────────────────────┐
│ [L1/gate] UserPromptSubmit hook fires BEFORE Claude processes it.       │
│           stdout on exit 0 is added as context; exit 2 blocks the       │
│           prompt entirely. (Cheapest place to inject a budget reminder  │
│           or block a known-overflowing request.)                        │
└─────────────────────────────────────────────────────────────────────────┘
        ▼
┌─ AGENT LOOP (repeats per tool call) ───────────────────────────────────┐
│  Claude chooses a tool.                                                  │
│  [L2 PREVENT]  PreToolUse hook fires (after choice, before exec).       │
│               · deny + permissionDecisionReason  → tool blocked,        │
│                 reason fed to the MODEL so it self-corrects.            │
│               · updatedInput (+allow/ask)        → rewrite the call.    │
│               · exit 0 empty / {}                → allow unchanged.     │
│               · multi-hook precedence: deny > defer > ask > allow.      │
│         ──────  TOOL EXECUTES  ──────                                    │
│  [L3 OFFLOAD] If the tool is the Agent/Task tool, the subagent runs in  │
│               a FRESH isolated context window; only its final summary   │
│               returns. Its heavy reads never enter the parent.          │
│  [L6 REACT]   PostToolUse hook fires (after success).                   │
│               · updatedToolOutput → REPLACES the result before the      │
│                 model sees it (spool full output to disk first).        │
│               · original is DISCARDED, not shown alongside.             │
│  [L4 REUSE]   Each loop turn re-sends the prefix; cache_read rises,     │
│               cache_creation stays small  →  caching is working.        │
└─────────────────────────────────────────────────────────────────────────┘
        ▼
┌─ CLAUDE FINISHES THE RESPONSE ─────────────────────────────────────────┐
│ [Stop]     Stop hook fires (and SubagentStop when a subagent finishes). │
│            Can block to force more work, or inject additionalContext.   │
└─────────────────────────────────────────────────────────────────────────┘
        ▼
┌─ CONTEXT APPROACHES THE LIMIT (CLI auto, or manual /compact) ──────────┐
│ [L7 COMPACT] PreCompact hook fires FIRST (matchers: manual | auto).     │
│              · Can BLOCK via {"decision":"block"} or exit 2  ⚠ §3.      │
│              · Receives transcript_path — preserve critical state here. │
│         ──────  COMPACTION RUNS  ──────                                  │
│              CLI multi-stage: clears OLDER tool outputs first, THEN     │
│              summarizes the conversation if still needed.               │
│ [PostCompact] fires after; statusline current_usage goes null in the    │
│              gap, repopulates on the next API call (§3 meter conflict). │
└─────────────────────────────────────────────────────────────────────────┘
```

**Three things that do NOT sit on this single timeline:**

- **Caching (L4)** is not an event — it is the *substrate* under every API call
  in every box above. Order matters only in that anything which mutates the
  prefix (a SessionStart loader, a PostToolUse rewrite, a compaction) changes what
  is cacheable on the *next* call.
- **Observation (L5)** runs **out-of-band**: the statusline script is invoked
  "after each new assistant message, after `/compact` finishes, when the
  permission mode changes, or when vim mode toggles," debounced at 300ms, and
  "**does not consume API tokens**" ([`08`](08-statusline-context-meter.md)).
  It never blocks the loop.
- **Offloading (L3)** spawns a *parallel* lifecycle: each subagent runs its **own**
  SessionStart→loop→Stop in a separate context window and a **separate transcript
  file**, unaffected by the parent's compaction (§3).

---

## 3. Inter-layer interactions and conflicts (the deliverable's part 2)

This is the heart of a defense-in-depth design: the layers are not independent.
Below, *which layer breaks which* — each tagged **documented** / **community** /
**speculation**, with the load-bearing claim's vote.

### 3.1 Reaction vs. Reuse — the central tension (DOCUMENTED)

**Any layer that trims / clears / rewrites context invalidates a prompt-cache
prefix.** This is the structural conflict the master report names as running
through everything, and it has three concrete instances:

- **PostToolUse `updatedToolOutput` (L6) vs. the cache (L4).** A rewrite changes a
  message-layer block. Because the cache is an exact prefix match
  (`tools→system→messages`), the rewrite invalidates the **messages** layer from
  that point on ([`05`](05-prompt-caching.md): "a change anywhere in the prefix
  recomputes everything after it"). The rewrite happens *before* the turn is
  cached, so the cost is paying to re-write the (now smaller) tail — usually a net
  win because the trimmed output is smaller than the original, but **flagged as
  not-documented** in [`03`](03-posttooluse-output-trimmer.md) ("Whether replacing
  tool output interacts badly with prompt caching… not documented; assume low
  risk but verify"). *(Reaction beats cache locally; net-positive because the
  shrink outweighs the re-write.)*
- **API tool-result clearing (L6) vs. the cache (L4).** Documented head-on: the
  context-engineering cookbook flags it as the cheapest primitive — "no inference
  cost, just a mechanical edit to the message list" (**3-0, primary**) — *but* the
  clear "invalidates cached prompt prefixes when content is cleared," which is why
  the `clear_at_least` parameter exists: make each clear large enough that the
  cache you burn is worth it. Defaults: **trigger 100K, keep 3 tool uses**,
  parameters `trigger` / `keep` / `clear_at_least` / `exclude_tools` (e.g.
  `['memory']`) (**3-0, primary**; type `clear_tool_uses_20250919`, beta
  `context-management-2025-06-27`).
- **Compaction (L7) vs. the cache (L4).** Compaction "reinitiates a new context
  window with the summary," so the entire prior prefix is gone — the next call is
  a cold cache write. Unavoidable; it is the last-resort layer precisely because
  it is the most destructive to both information *and* cache.

> **Sequencing rule that falls out of this:** order the reaction layers from
> *cheapest-to-cache-impact* to *most-destructive* — PostToolUse trim (smallest
> blast radius, message-tail only) → tool-result clearing (message-layer, gated by
> `clear_at_least`) → compaction (full prefix reset). Pull the destructive levers
> as late as possible. This is the master report's "prevent/curate → offload →
> reuse → react/compact" ordering restated as a cache-cost gradient.

### 3.2 Curation / SessionStart loaders vs. the cache (DOCUMENTED, counter-intuitive)

The prompt asks specifically about "prompt caching invalidated by curated
SessionStart loaders or CLAUDE.md edits." Two distinct behaviors, both documented
in [`05`](05-prompt-caching.md):

- **A SessionStart loader changes the *project-context* layer, not tools/system.**
  Content a SessionStart hook prints to stdout becomes model-visible context for
  that session. Because it loads *once at session start*, before the first API
  call writes the cache, it is **baked into the cached prefix from the start** — it
  does not invalidate anything mid-session. The risk is the *opposite*: a
  **non-deterministic** loader (printing a timestamp, a UUID, unsorted JSON) makes
  the prefix differ every session, so cross-session cache sharing silently breaks
  (the "silent invalidators" failure mode). **Keep SessionStart output
  deterministic.**
- **Editing project-root/user CLAUDE.md mid-session is cache-safe *but inert*.**
  Those files are "read once at session start and held in memory… the new content
  loads on the next `/clear`, `/compact`, or restart." So you neither break the
  cache nor get the edit until a reload boundary. (Nested/`paths:`-scoped CLAUDE.md
  *do* pick up edits on demand.) **Documented**, 3-0.
- **The cache-invalidation hierarchy that governs all of this:**
  `tools → system → messages`, cascading downward. **Changing tool definitions
  invalidates the *entire* cache** — so the highest-impact curation mistake is
  toggling MCP servers / tools mid-session, which silently destroys all cache
  savings ([`00`](00-master-context-overflow.md) §3, [`05`](05-prompt-caching.md)).

### 3.3 Blocking PreToolUse / PreCompact hook vs. auto-compaction (DOCUMENTED + COMMUNITY)

The prompt's marquee conflict: "a blocking PreToolUse hook vs. auto-compaction."

- **PreToolUse deny does not deadlock with compaction** — it operates earlier in
  the lifecycle (before a tool runs) and on a *different* signal (one oversized
  call), whereas compaction fires on *accumulated* context near the limit. They
  compose: the gate keeps single pulls from spiking context; compaction handles
  the slow drip the gate can't see ([`00`](00-master-context-overflow.md): "Leave
  cumulative-growth to the layered defense — it is out of scope for a per-call
  gate"). The real hazard is a **fail-closed** gate: if the guard errored and
  blocked every call, the session bricks. Hence **fail open everywhere** is a
  hard invariant ([`01`](01-pretooluse-input-gate.md)).
- **A blocking PreCompact hook is the genuine deadlock risk.** PreCompact *can*
  block compaction via `{"decision":"block"}` or exit 2 (confirmed live, 2026-06-23).
  But blocking is documented as a **cancel, not a deferral** — there is no
  retry/resume — so a PreCompact hook that blocks indefinitely can *itself* cause
  the overflow it was meant to guard. The official hooks blog frames PreCompact
  only as *preservation* (save state via `transcript_path`) and does **not**
  mention blocking, so the block capability is the **weakest / most
  version-sensitive** part of the stack — **community-workaround territory**
  ([`00`](00-master-context-overflow.md) §5, citing GitHub MemPalace #941/#856).
  **Design rule:** use PreCompact to *preserve* (write to memory / a progress
  file), never to *block*.

### 3.4 A hook that mutates history mid-session vs. everything downstream (DOCUMENTED mechanism)

- **PostToolUse `updatedToolOutput` discards the original** — the model never sees
  it and it is not recoverable from the turn ([`03`](03-posttooluse-output-trimmer.md)).
  If the hook is the *only* copy, you have a lossy mutation masquerading as a
  lossless one. The fix is non-optional: **spool the full output to disk first**,
  so the trimmed view is an *index into* truth (a pointer the agent can re-read —
  guarded, in turn, by the PreToolUse gate). The spool *is* the audit trail.
- **Structured-output tools can silently no-op a rewrite** — for tools whose
  result is a structured object, `updatedToolOutput` reportedly must match the
  expected shape or it is silently ignored (**community, not in the doc**).
  Validate per-tool; the safe target is string-output `Bash`.

### 3.5 Meter accuracy after `/compact` (DOCUMENTED)

The prompt asks about "meter accuracy after /compact." Two documented facts from
[`08`](08-statusline-context-meter.md):

- `used_percentage` / `current_usage` are **`null` right after `/compact`** (and
  before the first API call), repopulating on the next API response. A meter
  **must** fall back gracefully (the reference script degrades to a transcript
  read or `ctx --`). This is the *only* gap to guard around compaction.
- The old "auto-compaction does not adjust the counters so the meter diverges
  permanently" behavior was a **pre-v2.1.132 bug — REFUTED (0-3)** and fixed:
  counters now reflect *current* context, not cumulative session totals, and the
  "last assistant message, input-side only" rule self-corrects after compaction
  with no special handling. (Naive turn-summing over-counts by **39×** — proven
  firsthand in [`08`](08-statusline-context-meter.md).)

### 3.6 Offloading vs. compaction and caching (DOCUMENTED)

- **Subagent transcripts are unaffected by main-conversation compaction** —
  "they're stored in separate files," and main + subagent compaction operate
  independently (same logic/thresholds, separate triggering on each window's own
  token count) (**3-0, primary**, [`code.claude.com/docs/en/sub-agents`](https://code.claude.com/docs/en/sub-agents)).
  So compacting the parent does not corrupt an in-flight subagent's state.
- **Offloading interacts with caching via the fork-vs-named-subagent split**
  (**3-0, primary**): a **fork** shares the parent's prompt cache — "its first
  request reuses the parent's prompt cache… cheaper than spawning a fresh
  subagent" — whereas a **named subagent** uses a **separate cache** and starts
  with no cache hits. So *which* offload primitive you choose is a direct
  caching-cost decision: forks are cache-cheap but **inherit the full parent
  context** (the opposite of offloading, per [`04`](04-subagent-offloading.md));
  named subagents are cache-cold but truly isolate. **Pick the named subagent when
  the goal is keeping tokens out of the parent; pick the fork only when you need
  the parent's context and want cache reuse.**
- **The offloading double-edge** ([`04`](04-subagent-offloading.md)): offloading
  *drops parent-context tokens* (what causes overflow) while *raising total tokens
  billed* (~4–7× for fan-out — the subagent's reads are cache-cold). A
  defense-in-depth design accepts higher total spend to protect the scarce
  resource (parent context).

### 3.7 Conflict-resolution matrix (which layer wins / breaks which)

| Interaction | Outcome | Tag | Vote |
|---|---|---|---|
| PostToolUse rewrite → cache | Invalidates messages-layer tail; net-positive (shrink > re-write) | documented mech. / interaction undocumented | [3] 0-3 mech.; interaction unflagged |
| Tool-result clearing → cache | Invalidates prefix; gate with `clear_at_least` | **documented** | 3-0 |
| Compaction → cache | Full prefix reset, cold next call | **documented** | 3-0 |
| SessionStart loader → cache | Baked into prefix at start; non-determinism breaks cross-session sharing | **documented** | 3-0 (caching) |
| CLAUDE.md edit mid-session → cache | Cache-safe but inert until reload boundary | **documented** | 3-0 |
| Tool/MCP def change → cache | Invalidates **entire** cache | **documented** | 3-0 |
| PreToolUse deny vs auto-compaction | Compose (different signals); fail-closed bricks session | **documented** | 3-0 |
| PreCompact block vs overflow | Block = cancel, not defer; can self-cause overflow | **community** | — |
| PostToolUse rewrite (no spool) | Lossy; original unrecoverable | **documented** | 3-0 |
| Meter after `/compact` | `null` gap, then self-corrects | **documented** | 3-0 |
| Main compaction vs subagent transcript | Independent; subagent unaffected | **documented** | 3-0 |
| Fork vs named subagent → cache | Fork reuses parent cache; named = separate cache | **documented** | 3-0 |

---

## 4. Reference architectures with cited specifics (the deliverable's part 2, evidence)

### 4.1 Anthropic — *Effective context engineering for AI agents* (primary)

The canonical statement that the layers are complementary and per-workload
(§1 table). Three load-bearing, verbatim-sourced findings:

- **Sub-agent offloading (3-0):** "Each subagent might explore extensively, using
  tens of thousands of tokens or more, but returns only a condensed, distilled
  summary of its work" — "the detailed search context remains isolated within
  sub-agents." This *is* the offloading layer.
- **Tool-result clearing as the lightest-touch reaction (3-0):** "One of the
  safest lightest touch forms of compaction is tool result clearing, most recently
  launched as a feature on the Claude Developer Platform."
- **Compaction's failure mode (3-0):** "The art of compaction lies in the
  selection of what to keep versus what to discard, as overly aggressive
  compaction can result in the loss of subtle but critical context whose
  importance only becomes apparent later." → the conflict note for L7.

### 4.2 Anthropic — *Effective harnesses for long-running agents* (primary) — the strongest defense-in-depth case study

This is the closest thing to a **published reference architecture** for stacking:

- **Compaction alone is insufficient (3-0):** "However, compaction isn't
  sufficient. Out of the box, even a frontier coding model like Opus 4.5 running
  on the Claude Agent SDK in a loop across multiple context windows will fall short
  of building a production-quality web app if it's only given a high-level prompt."
- **The fix is external-state offloading layered on top (3-0):** the reference
  harness offloads state to **durable external artifacts** — a `claude-progress.txt`
  log, a feature-requirements file, and **git history** — so "agents [can] quickly
  understand the state of work when starting with a fresh context window." This is
  the *curation + offloading* layers doing what compaction can't: surviving a
  *full context reset* across windows.
- **The Agent SDK provides built-in compaction (3-0):** "It has context management
  capabilities such as compaction, which enables an agent to work on a task without
  exhausting the context window" — i.e. the SDK ships L7 for you; you add the
  external-state layers around it.

### 4.3 Anthropic — context-engineering cookbook & API primitives (primary)

The cookbook ("Context engineering: memory, compaction, and tool clearing") is the
clearest **three-primitive map**, and confirms Claude Code itself stacks them:

- **Three first-party primitives, distinct headers (3-0):** compaction
  (`compact_20260112`, beta `compact-2026-01-12`, whole-transcript, min 50K /
  default 150K, **incurs inference cost** — the summarizer model runs);
  tool-result clearing (`clear_tool_uses_20250919`, beta
  `context-management-2025-06-27`, sub-transcript surgical removal, **no inference
  cost**, default trigger 100K / keep 3); memory tool (`memory_20250818`, **no
  beta header required**, just-in-time persistent external storage).
- **They map to different workload problems (3-0):** "compaction compresses the
  whole window when it grows too large, clearing drops stale re-fetchable data
  inside the window, and memory moves information out of the window so it survives
  across sessions" — *the* defense-in-depth thesis in one sentence.
- **Claude Code stacks them in production (3-0):** "Claude Code employs multiple of
  these strategies in production: compaction for long conversations and **two
  complementary memory systems** for cross-session persistence" — corroborated by
  [`code.claude.com/docs/en/memory`](https://code.claude.com/docs/en/memory):
  CLAUDE.md (user-written, loaded every session) + auto memory (Claude-written
  `MEMORY.md`, v2.1.59+). This is a **shipped product** running the stack, not a
  hypothetical.

### 4.4 The memory tool as an explicitly-stacked layer (primary)

- **Client-side offloading primitive (3-0):** "The memory tool operates
  client-side… Claude makes tool calls to perform memory operations, and your
  application executes those operations locally" (commands: view / create /
  str_replace / insert / delete / rename on a `/memories` directory). You control
  the storage backend.
- **Just-in-time retrieval (3-0):** "the key primitive for just-in-time context
  retrieval: rather than loading all relevant information upfront, agents store
  what they learn in memory and pull it back on demand… critical for long-running
  workflows where loading everything at once would overwhelm the context window."
- **Designed to be paired with compaction (3-0):** "consider using both:
  compaction keeps the active context manageable… and memory persists important
  information across compaction boundaries so that nothing critical is lost in the
  summary." This is Anthropic *explicitly prescribing a two-layer stack*. (The
  literal phrase "defense in depth" is the report author's gloss, not Anthropic's
  — flagged.)

### 4.5 Agent SDK / sub-agents docs (primary)

- **Subagent isolation (3-0):** "Each subagent starts with a fresh, isolated
  context window. It does not see your conversation history, the skills you've
  already invoked, or the files Claude has already read" — and "only the relevant
  summary returns to your main conversation." Forks are the documented exception
  (`CLAUDE_CODE_FORK_SUBAGENT` / v2.1.117+), inheriting the full parent context.
- **OSS / cross-framework corroboration (community, directional):** the master
  and sibling reports surveyed OSS agents and found the same pattern recurring —
  LangChain short-term-memory + deepagents context-engineering; the truncation /
  compaction patterns in OpenHands, SWE-agent, Letta, Aider, AutoGen
  ([`03`](03-posttooluse-output-trimmer.md)); the RepoMix + CodeGraph
  selective-access pairing ([`00`](00-master-context-overflow.md) §4). **No public
  OSS project implements the *full* Claude Code stack end-to-end** (PreToolUse gate
  + PostToolUse trim + spool + offload + cache + meter); the closest reusable hook
  pieces are `dropdevrahul/nexum`, `aokumablue/bluecore`, `OmniNode-ai/omniclaude`
  (trimmer designs) and `hansipie/ecotokens` (transform-style PreToolUse). Treat
  all as **community / design-inspiration**, not drop-in.

### 4.6 The version-sensitive caching case study (the one 2-1 claim)

[`05`](05-prompt-caching.md)'s cross-layer warning, surfaced via GitHub
**#27048** (the single **2-1** vote, the rest 3-0): on `claude --resume`,
**tool-use content (Read outputs) is not retrieved from cache** — in heavy-read
sessions cache hit rate collapsed **99% → 17%**, ~91K new tokens rewritten instead
of 98K previously-cached read. **Tag: documented bug, community-measured numbers,
not Anthropic-confirmed.** The numbers are one reporter's at v2.1.49; the issue was
auto-closed as stale (not "won't-fix on merits"). Corroborated by #42338, #34629,
#46829. **Defense-in-depth consequence:** session *resume* is itself a cache-hostile
boundary, so a stack that leans on warm caches should verify cache health
(`cache_read:cache_creation` ratio) after every `--resume`, not assume it survives.

---

## 5. The layered reference design (the deliverable's part 1, assembled)

A copy-pasteable stack for a production agentic coding session. Each layer cites
its sibling report for the hardened single-layer build.

### 5.1 `settings.json` — the four hook layers + meter

```json
{
  "statusLine": {
    "type": "command",
    "command": "python3 ~/.claude/context-meter.py",
    "padding": 1
  },
  "hooks": {
    "SessionStart": [
      { "matcher": "startup|resume",
        "hooks": [ { "type": "command",
          "command": "python3 \"$CLAUDE_PROJECT_DIR/hooks/load_context.py\"" } ] } ],
    "PreToolUse": [
      { "matcher": "Read|Bash",
        "hooks": [ { "type": "command",
          "command": "python3 \"$CLAUDE_PROJECT_DIR/hooks/guard_context.py\"" } ] } ],
    "PostToolUse": [
      { "matcher": "Bash|WebFetch",
        "hooks": [ { "type": "command",
          "command": "python3 \"$CLAUDE_PROJECT_DIR/hooks/trim_output.py\"" } ] } ],
    "PreCompact": [
      { "matcher": "auto|manual",
        "hooks": [ { "type": "command",
          "command": "python3 \"$CLAUDE_PROJECT_DIR/hooks/preserve_state.py\"" } ] } ]
  }
}
```

### 5.2 The seven layers, instantiated

| Layer | Concrete build | Hard invariant |
|---|---|---|
| **L1 Curate** | Lean CLAUDE.md (<200 lines); `load_context.py` SessionStart loader prints a deterministic project digest to stdout; rely on the two memory systems | Loader output must be **deterministic** (no timestamps/UUIDs) or it breaks cross-session cache (§3.2) |
| **L2 Prevent** | [`guard_context.py`](../hooks/guard_context.py) PreToolUse deny on oversized `Read`/`Bash`, `bytes//4`, `deny`+reason | **Fail open everywhere** (§3.3); never `ask` for a mechanical size rule |
| **L3 Offload** | `Explore`/`Plan` or a `.claude/agents/investigator.md` returning **pointers, not prose**; Workflow fan-out for wide surveys; memory tool for cross-session state | Named subagent (not fork) when isolating; schema-bound returns or you re-bloat the parent (§3.6) |
| **L4 Reuse** | Automatic in CLI; keep `tools→system→messages` stable; static-first / dynamic-last; pass updates as a new message, not an edit | Don't mutate tool defs / MCP set mid-session (full cache wipe) |
| **L5 Observe** | `context-meter.py` statusline; `/context` for spot-checks | Fall back gracefully on `null` (post-`/compact`); never sum turns |
| **L6 React** | `trim_output.py` PostToolUse `updatedToolOutput` = head+tail+pointer, **spool full output first** | Spool is non-optional (it's the audit trail, §3.4); default deterministic, no in-hook LLM summarizer |
| **L7 Compact** | CLI auto + `/compact`; `preserve_state.py` PreCompact writes critical state to memory/progress file via `transcript_path` | **Preserve, never block** (§3.3); accept it's lossy + cache-cold |

### 5.3 The ordering principle (restated)

Run them in the lifecycle order of §2, but *reach for* them in the master
report's cost-gradient order: **prevent / curate (lossless) → offload → reuse
(caching) → react / compact (lossy)**. The reaction layers themselves are ordered
by cache-blast-radius: **PostToolUse trim → tool-result clearing → compaction**
(§3.1). Observation (L5) is always-on and free; it tells you when to escalate
down the gradient.

---

## 6. Integration-testing the stack (the deliverable's part 3)

Unit-testing each hook is necessary but **not sufficient** — the prompt asks how to
test the *stack*. Three test tiers, building directly on
[`09-ab-thrash-test.md`](09-ab-thrash-test.md) (the headless A/B harness) and
[`01`](01-pretooluse-input-gate.md)'s subprocess-fidelity pattern.

### Tier 1 — Per-layer unit tests (fidelity, fail-open)

Each hook tested as a subprocess with the exact stdin/stdout/exit-code contract
Claude Code uses (mirroring [`test/test_guard.py`](../test/test_guard.py)):

- **L2 gate:** big unbounded Read → `deny`; small / `limit:100` → allow; missing
  file / garbage stdin → **exit 0** (fail-open). (Existing 11-test suite.)
- **L6 trimmer:** oversized `tool_response` → `updatedToolOutput` smaller than
  input + pointer line present + **spool file exists with the full original**;
  under-budget → passthrough (empty stdout); garbage → passthrough (fail-open).
- **L1 loader:** deterministic output across two runs (byte-identical) — the cache
  guarantee (§3.2).
- **L5 meter:** tier boundaries (69→green, 70→yellow, 89→yellow, 90→red); `null`
  `current_usage` → transcript fallback, never blank.
- **L7 PreCompact:** asserts it **preserves** (writes the state file) and **does
  not** emit `{"decision":"block"}` (the deadlock guard, §3.3).

### Tier 2 — Order & no-deadlock assertions (the new, stack-level tests)

Unit tests don't prove the layers *fire in the right order and don't deadlock*.
Make every hook **append a timestamped line to a shared log** (`hook_event_name`,
`tool_name`, `pid`, monotonic ms), then run a scripted session headless and assert
the log:

```jsonl
{"t": 0,    "event": "SessionStart"}
{"t": 12,   "event": "UserPromptSubmit"}
{"t": 40,   "event": "PreToolUse",  "tool": "Read"}
{"t": 51,   "event": "PostToolUse", "tool": "Read"}
{"t": 88,   "event": "PreToolUse",  "tool": "Bash"}
{"t": 95,   "event": "PostToolUse", "tool": "Bash"}
{"t": 130,  "event": "Stop"}
```

Assertions (the order contract from §2):
1. `SessionStart` precedes the first `UserPromptSubmit`.
2. Every `PreToolUse` for a given tool-call id precedes its `PostToolUse`.
3. No `PreToolUse` ever follows the `tool exec` it gates (timestamps prove ordering).
4. **No deadlock:** every hook process exits within a bounded wall-clock budget
   (e.g. each line's `pid` terminates; no PreCompact line without a matching
   compaction/PostCompact line within N seconds).
5. **PreCompact never blocks:** assert no `{"decision":"block"}` was emitted (parse
   the hook's own stdout log) — this is the §3.3 deadlock regression test.

### Tier 3 — End-to-end A/B with vs. without the whole stack

The credibility test. Run the **identical overflow-inducing prompt** twice, holding
model / repo / turn-cap / permission-mode constant — per [`09`](09-ab-thrash-test.md):

- **Stack ON:** `claude -p --bare --settings full-stack.json` (the §5.1 config) —
  additive registration, because **`--settings` *merges* `hooks` and cannot
  *subtract* a hook** (GitHub #11392, **refuted** that it overrides; **confirmed**
  it merges). Confirm no stack hook lives in `~/.claude`.
- **Stack OFF:** `claude -p --bare` with no `--settings` (the empty baseline).
- **Pin confounders:** `--model claude-opus-4-8` (full name), `--permission-mode
  dontAsk`, `--max-turns 30`, `--session-id <uuid>` (to locate the transcript),
  `--output-format json`, and **`DISABLE_AUTO_COMPACT=1`** so raw context growth
  isn't masked by L7 (you test L7 separately, with compaction *enabled*, in a
  second pair of arms).
- **Statistical, not bit-exact:** `-p` is not deterministic (sampling + agentic
  branching); run **n≥3 per arm**, report medians + spread.

**The single chartable metric** ([`09`](09-ab-thrash-test.md), [`08`](08-statusline-context-meter.md)):

```
peak_context = max over assistant turns of
               (input_tokens + cache_creation_input_tokens + cache_read_input_tokens)
```

This is **identical to Claude Code's own `used_percentage` formula** (input-only,
no output tokens) — read it straight from the session JSONL
`~/.claude/projects/<slug>/<session-id>.jsonl`. **Do not sum turns** (39× over-count
— [`08`](08-statusline-context-meter.md)); current context = the *last* assistant
turn's input-side total, and `max` over turns is the peak.

```bash
peak_context () {   # jq over one session transcript
  jq -s '[ .[] | select(.type=="assistant") | .message.usage | select(. != null)
           | (.input_tokens + .cache_creation_input_tokens + .cache_read_input_tokens) ]
         | { turns: length, peak_context: (max // 0) }' "$1"
}
```

**Per-layer attribution within the A/B (proving each layer carries weight):**

| Layer | Signal it fired | Where to read it |
|---|---|---|
| L2 gate | count of `permissionDecision:"deny"` | hook's own log / denied-tool markers in transcript |
| L3 offload | parent peak_context drops while **subagent JSONLs** (`…/subagents/`) carry the reads | parent vs subagent transcript split ([`04`](04-subagent-offloading.md)) |
| L4 cache | `cache_read` rises, `cache_creation` stays small | `message.usage` per turn |
| L6 trim | `updatedToolOutput` smaller than original; spool file present | trimmer log + `.cc-spool/` |
| L7 compact | a `compact_boundary` event in the transcript; `current_usage`→null then recovers | transcript + meter capture |

**Expected shape that proves the stack:** Stack-ON peaks **markedly lower** and
survives **more turns** before the limit; total tokens billed may be **higher**
(offloading's subagent reads are cache-cold — the deliberate trade). If parent
peak doesn't drop, your subagent returns too much (tighten the schema); if total
billed doesn't rise, you didn't actually offload.

> **Honest fuzz** (carried from [`09`](09-ab-thrash-test.md)): headless determinism
> is statistical, not bit-exact; the guard's `bytes//4` is a *decision input*, not
> a *measurement* (prove the effect with JSONL `usage`, show the mechanism with the
> guard's estimate); CLI JSONL can undercount vs the raw API (fine for relative
> A/B); and the displayed-% vs limit-trigger asymmetry (output excluded from %,
> included in the trigger) means a run can look under-budget yet still trip the
> limit.

---

## 7. Documented vs. community vs. speculation (summary)

- **Documented (primary, mostly 3-0):** the hook lifecycle + exit-code semantics;
  the three API context primitives (compaction / clearing / memory) with their
  types/headers/defaults; sub-agent isolation + separate transcripts +
  fork-vs-named-subagent cache split; the memory+compaction pairing; the
  `tools→system→messages` cache hierarchy and its invalidators; the CLAUDE.md
  mid-session inertness; the statusline `used_percentage` formula and post-`/compact`
  `null`; `--settings` merges (not overrides); the peak-context metric. Anthropic's
  *Effective context engineering* and *Effective harnesses for long-running agents*
  as the reference architectures.
- **Community / workaround:** PreCompact *blocking* as cancel-not-defer (GitHub
  MemPalace #941/#856); the `claude --resume` cache-collapse numbers (#27048, the
  one 2-1 claim — community-measured, not Anthropic-confirmed); structured-output
  `updatedToolOutput` silent no-op; OSS hook fragments (`nexum`, `bluecore`,
  `omniclaude`, `ecotokens`); the ~4–7× fan-out token multiplier; `DISABLE_AUTO_COMPACT`
  (now documented but originally community-discovered).
- **Speculation (do not cite as documented):** the CLI auto-compaction *threshold
  percentage* (the "83% / ~167k" figure is **refuted**); the exact post-`/compact`
  `null` duration; whether the PostToolUse-rewrite × prompt-cache interaction is
  ever *net-negative* (assume low risk, verify); the empirical magnitude of
  stack-level savings (measure with Tier 3).

---

## 8. Version sensitivity & open questions

**Version-sensitive (scoped to mid-2026 / Claude Code v2.1.x):** API
context-editing + compaction are **BETA** (headers/defaults may change); the
`updatedToolOutput` capability requires **≥ v2.1.121** (capability doc-confirmed,
exact intro version secondary); forks **v2.1.117+**; auto memory **v2.1.59+**;
statusline `total_*` semantics flipped cumulative→current at **v2.1.132**;
`--bare` "will become the default for `-p`"; transcript JSONL path + `message.usage`
field set are **directly observed, not formally documented**.

**Open questions (not resolved by available sources):**

1. **The CLI's actual auto-compaction trigger** (percentage / reserve, 200k vs 1M)
   — undocumented; the test plan *disables* compaction rather than reason about it.
2. **Whether the layers compose additively or partially cancel** — does
   `offload + clear + cache` net out better than any one? No benchmark; Tier 3 A/B
   is the way to measure it for a given workload.
3. **The PostToolUse-rewrite × prompt-cache interaction** under heavy reliance on
   caching — not documented; assume low risk, verify with the `cache_read:creation`
   ratio across rewritten turns.
4. **Whether the `claude --resume` cache-collapse (#27048) is fixed** in current
   builds — community still patching as of mid-2026; re-verify on your version.
5. **A documented headless `/context` equivalent** — still none; parsing the JSONL
   is the substitute.

---

## Sources

**Primary (Anthropic) — documented:**
- [`anthropic.com/engineering/effective-context-engineering-for-ai-agents`](https://www.anthropic.com/engineering/effective-context-engineering-for-ai-agents)
  — complementary per-workload techniques; sub-agent distilled summaries;
  tool-result clearing as lightest-touch compaction; compaction's selection
  tradeoff.
- [`anthropic.com/engineering/effective-harnesses-for-long-running-agents`](https://www.anthropic.com/engineering/effective-harnesses-for-long-running-agents)
  — "compaction isn't sufficient"; external-state harness (`claude-progress.txt` +
  feature-requirements file + git history); Agent SDK built-in compaction.
- [`anthropic.com/engineering/building-agents-with-the-claude-agent-sdk`](https://www.anthropic.com/engineering/building-agents-with-the-claude-agent-sdk)
  (→ [`claude.com/blog/building-agents-with-the-claude-agent-sdk`](https://claude.com/blog/building-agents-with-the-claude-agent-sdk))
  — subagents in isolated context windows return only relevant info.
- [`platform.claude.com/cookbook/tool-use-context-engineering-context-engineering-tools`](https://platform.claude.com/cookbook/tool-use-context-engineering-context-engineering-tools)
  — three first-party primitives; their per-workload mapping; clearing defaults
  (100K / keep 3) + `clear_at_least`/`exclude_tools`; compaction whole-transcript /
  min 50K / default 150K / inference cost; "Claude Code employs multiple of these
  strategies in production."
- [`platform.claude.com/docs/en/build-with-claude/compaction`](https://platform.claude.com/docs/en/build-with-claude/compaction),
  [`/context-editing`](https://platform.claude.com/docs/en/build-with-claude/context-editing),
  [`/agents-and-tools/tool-use/memory-tool`](https://platform.claude.com/docs/en/agents-and-tools/tool-use/memory-tool)
  — typed strategies, beta headers, memory client-side mechanics + just-in-time
  retrieval + "using with compaction".
- [`code.claude.com/docs/en/hooks`](https://code.claude.com/docs/en/hooks) +
  [`claude.com/blog/how-to-configure-hooks`](https://claude.com/blog/how-to-configure-hooks)
  — event lifecycle, exit-code semantics, SessionStart/UserPromptSubmit
  stdout-as-context, PreCompact matchers + block, PostCompact/SubagentStop/SessionEnd
  (live-checked 2026-06-23).
- [`code.claude.com/docs/en/sub-agents`](https://code.claude.com/docs/en/sub-agents)
  — fresh isolated context; separate transcripts unaffected by main compaction;
  fork-vs-named-subagent cache split.
- [`code.claude.com/docs/en/memory`](https://code.claude.com/docs/en/memory) — two
  complementary memory systems (CLAUDE.md + auto memory).

**Sibling reports (built on, not re-derived):**
[`00-master-context-overflow.md`](00-master-context-overflow.md),
[`01-pretooluse-input-gate.md`](01-pretooluse-input-gate.md),
[`02-context-check-estimator.md`](02-context-check-estimator.md),
[`03-posttooluse-output-trimmer.md`](03-posttooluse-output-trimmer.md),
[`04-subagent-offloading.md`](04-subagent-offloading.md),
[`05-prompt-caching.md`](05-prompt-caching.md),
[`08-statusline-context-meter.md`](08-statusline-context-meter.md),
[`09-ab-thrash-test.md`](09-ab-thrash-test.md).

**Community / workaround (flagged inline):** GitHub `anthropics/claude-code`
[#27048](https://github.com/anthropics/claude-code/issues/27048) (resume cache
collapse, 2-1, community numbers), #42338 / #34629 / #46829 (corroborating),
[#11392](https://github.com/anthropics/claude-code/issues/11392) (`--settings`
merges), MemPalace #941/#856 (PreCompact block = cancel); OSS hook fragments
`dropdevrahul/nexum`, `aokumablue/bluecore`, `OmniNode-ai/omniclaude`,
[`hansipie/ecotokens`](https://github.com/hansipie/ecotokens).

**Refuted in adversarial pass (do not repeat):** an "eight hook events incl.
`PermissionRequest`" set (0-3); "the Agent SDK `compact` feature *automatically*
summarizes… preventing the agent from running out of context" as stated (1-2 —
compaction is built-in but *not sufficient alone*, per §4.2); "toggling `/plugin`
mid-session invalidates 96–115K of cached user content" (0-3). Plus the master/
statusline refutations carried forward: the "83% auto-compaction" threshold
(speculation), per-file reads "dominate" at 1,100–2,400 tokens each (1-2), and the
pre-v2.1.132 cumulative-counter / 169% statusline behavior (0-3, fixed).
