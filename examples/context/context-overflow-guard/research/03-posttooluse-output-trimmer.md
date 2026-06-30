# PostToolUse as an output-trimmer: yes, a hook can rewrite a tool result before the model sees it

> **Source:** deep-research run for the PostToolUse output-trimmer/summarizer
> prompt (#3) in [`../research-prompts.md`](../research-prompts.md). Search
> angles: (a) the official PostToolUse output schema, (b) the rewrite-capability
> crux (adversarial verify/kill), (c) version-sensitivity & changelog, (d)
> sanctioned shrink patterns, (e) auto-summarization via subagent + info loss.
> 17 sources fetched → 74 claims → 25 verified (3-vote adversarial, need 2/3 to
> kill) → 17 confirmed, 8 killed. Current as of **mid-2026** (Claude Code
> v2.1.x; local install v2.1.186, Opus 4.8 / Sonnet 4.6 / Haiku 4.5, 1M
> context). Crux cross-checked **live** against the raw `code.claude.com/docs/en/hooks`
> page and `anthropics/claude-code` `CHANGELOG.md` on 2026-06-23.

**Bottom line — YES.** This was filed as "capability UNVERIFIED." It is now
**verified**: a `PostToolUse` hook **can fully replace the tool's result before
it reaches the model**, via `hookSpecificOutput.updatedToolOutput`. The official
hooks doc states it plainly: *"A few events can also rewrite content rather than
only allow or block it: `PostToolUse` — `updatedToolOutput` replaces the tool's
result."* This works for **all tools** (built-in `Bash`/`Read`/`WebFetch` and
MCP), not append-only. The original output is **discarded**, not shown alongside.
This reverses what was true a few releases earlier (rewriting was **MCP-only**
via the now-deprecated `updatedMCPToolOutput`), which is why the catalog and
several still-circulating GitHub issues say it's impossible — those are stale.
**What is NOT provided:** any native summarizer. The hook gets a sink
(`updatedToolOutput`) and must produce the smaller string itself. For a
context-overflow guard, **deterministic trim-with-pointer beats LLM
summarization** — it is lossless-by-reference, adds ~0 latency, and composes with
the existing PreToolUse gate.

**Key OSS findings (2026-06 scan).** No public OSS project found implements the
full target pattern end-to-end (`PostToolUse` + `updatedToolOutput` + deterministic
head/tail + pointer + raw-output spool + fail-open). The closest reusable pieces
are: `dropdevrahul/nexum` for a stdlib-only fail-open Python PostToolUse trimmer,
`aokumablue/bluecore` for correctly preserving Bash's structured output shape
while rewriting `stdout`, and `OmniNode-ai/omniclaude` for the strongest
capture-before-suppress spool/pointer design. Frameworks such as OpenHands,
smolagents, SWE-agent, Letta, LangChain, Aider, and AutoGen provide useful
truncation or compaction patterns, but none are a drop-in Claude Code hook with
mandatory raw-output spool. `fim-ai/fim-one` is the closest conceptual hook
architecture, but its source-available non-OSI license makes it design inspiration
only, not OSS code to reuse.

---

## (a) The verified PostToolUse contract

### stdin — the JSON delivered to the hook command

PostToolUse fires **after** a tool completes. The hook receives, on stdin, the
same envelope as PreToolUse **plus the tool's result**:

| Field | Meaning | Source |
|---|---|---|
| `session_id` | Current session id | `/hooks` (primary) |
| `transcript_path` | Path to conversation JSONL | `/hooks` (primary) |
| `cwd` | Working directory | `/hooks` (primary) |
| `hook_event_name` | `"PostToolUse"` | `/hooks` (primary) |
| `tool_name` | `Read`, `Bash`, `WebFetch`, `mcp__server__tool`, … | `/hooks` (primary) |
| `tool_input` | The args the tool was called with | `/hooks` (primary) |
| `tool_response` | **The tool's result** — the thing you trim | `/hooks` (primary) |

> Flag: the result field is `tool_response`. Its **shape varies by tool** — a
> string for `Bash` stdout, a structured object for some built-ins and MCP tools.
> This matters for the replacement (see caveats). Confirm the exact shape for
> your target tool by logging one real payload before writing the trimmer.

### stdout — what a PostToolUse hook can return

Documented (`code.claude.com/docs/en/hooks`), in order of relevance to this task:

```json
{
  "hookSpecificOutput": {
    "hookEventName": "PostToolUse",
    "updatedToolOutput": "…the smaller string the model will actually see…"
  }
}
```

| Field / form | Effect | For our use |
|---|---|---|
| `hookSpecificOutput.updatedToolOutput` | **Replaces** the tool result before it reaches Claude. Original is discarded. Works for **all** tools. | ✅ **This is the lever.** |
| `hookSpecificOutput.additionalContext` | **Injects** extra context *alongside* the (unchanged, full) result. Append-only. | ❌ Adds tokens — wrong direction for shrinking. |
| `decision: "block"` + `reason` (top-level) | Blocks the result from being shown; `reason` is fed to the model. | Blunt — kills the whole output, no trimmed substitute. |
| `continue`, `systemMessage`, `suppressOutput`, `terminalSequence` | Universal fields. `suppressOutput` only hides hook **stdout** from the **transcript** — it does **not** shrink what the model sees. | Not a shrink mechanism (common confusion — see killed claims). |

**Direct doc quotes** (fetched live, `code.claude.com/docs/en/hooks`,
2026-06-23):

> "A few events can also rewrite content rather than only allow or block it:
> `PostToolUse` — `updatedToolOutput` replaces the tool's result."

> "For redaction or transformation use cases, intercept at `PreToolUse` for
> outbound tool inputs and `PostToolUse` for inbound tool results."

That second quote is the design blessing for exactly this idea: **PreToolUse
gates the call; PostToolUse transforms the result.** The two are explicitly
positioned as a matched pair.

### Eight "you can't do this" claims were investigated and killed

The adversarial pass specifically tried to *confirm* the pessimistic reading and
**failed** — these were all refuted (vote in brackets):

- "No field replaces built-in tool output; only MCP via `updatedMCPToolOutput`"
  — issue #36843 **[0-3]**
- "A PostToolUse hook can only add warnings alongside the original, which still
  reaches the model" — issue #36843 **[0-3]**
- "Rewriting built-in output is a not-yet-implemented enhancement" — issue
  #36843 **[0-3]**
- "Rewrite is MCP-only; no mechanism for built-in tools" — issue #41087 **[1-2]**
- "Schema exposes only `continue`/`suppressOutput`/`systemMessage`; none replace
  the result; `suppressOutput` just hides from transcript" — SKILL.md **[0-3]**
- "PostToolUse purpose is react/feedback/log, not replace" — SKILL.md **[0-3]**
- "Only `updatedInput` (PreToolUse) modifies tool data; PostToolUse has no
  output-rewriting field" — SKILL.md **[0-3]**
- "Changelog has no tool-result-rewrite entry; only Stop/SubagentStop
  `additionalContext` appends" — CHANGELOG.md **[0-3]**

All of these describe a **previous version** of Claude Code. They are the reason
this prompt was marked unverified. They are now wrong.

---

## Version sensitivity (read this before you build)

| Capability | Status | Evidence / flag |
|---|---|---|
| `updatedToolOutput` replaces result for **all** tools | **Documented & live** in `/hooks` | Confirmed by live fetch 2026-06-23. Present in local v2.1.186. |
| Introduced **v2.1.121** (Agent SDK 0.2.121); previously MCP-only | **Secondary sources** (`claudeupdates.dev/version/2.1.121`, `wotai.co`) | ⚠️ I could **not** find a `2.1.121` line for `updatedToolOutput` in the raw `CHANGELOG.md` on the crux re-fetch. Treat the exact introduction version as *reported, not changelog-confirmed*. The **capability itself is doc-confirmed and present**, which is what matters for a build on v2.1.186. |
| `updatedMCPToolOutput` (old MCP-only field) | **Deprecated**, superseded by `updatedToolOutput` | Don't write new code against it. |
| `Read` returns a truncated **"PARTIAL view"** first page instead of a hard error on oversized whole-file reads | **Changelog-confirmed: v2.1.145** | Live CHANGELOG fetch: *"Improved the Read tool to return a truncated first page with a 'PARTIAL view' notice instead of a hard error when a whole-file read exceeds the token limit."* This answers the prior open question on the native truncation. |

**Pin a minimum version.** Anything ≥ **v2.1.121** is safe per the secondary
sources; the local environment (**v2.1.186**) is well past it. If you ship this
in the example, document the floor and fail-open on older builds (a hook that
emits `updatedToolOutput` on a version that ignores it simply has no effect — the
full output passes through, which is the safe failure).

---

## (b) Sanctioned patterns to shrink big outputs

Even with rewrite confirmed, the doc-sanctioned shrink toolbox is broader than
the hook. Ranked by how cleanly they fit a context-overflow guard:

1. **Deterministic trim-with-pointer inside a PostToolUse hook** *(documented
   mechanism, the recommended build).* Measure `tool_response`; if over budget,
   emit `updatedToolOutput` = `head + tail + a pointer line`, and **spool the full
   output to disk** so nothing is lost. The model can then selectively re-read the
   spool (`Read(path, offset, limit)` / `grep`) — and the **existing PreToolUse
   gate protects that re-read**. This is "selective reading" (lever #3) applied to
   *outputs*. Lossless by reference, ~0 added latency, no second model.

2. **Native `Read` PARTIAL view** *(documented, automatic, v2.1.145+).* For
   whole-file reads the runtime already trims to a first page with a notice.
   You get this for free; the hook is for the cases `Read` truncation doesn't
   cover (`Bash` stdout, `WebFetch`, MCP results).

3. **Wrapper scripts** *(community best practice).* Wrap noisy commands so only
   the signal returns: `… | tail -n 50`, `pytest -q`, `tsc --noEmit | head`,
   `eslint --format json | jq '.[].messages'`. Cited token-reduction write-ups
   (composio.dev; the "90% reduction" Medium piece) lean on this. Pairs with the
   existing Bash heuristic in [`../hooks/guard_context.py`](../hooks/guard_context.py).

4. **Paginating / bounded MCP tools** *(community best practice).* Prefer tools
   that return a page or a structured slice over ones that dump
   (`preview_table_data` over a full SQL dump; symbol search over
   `get_file_text_by_path`). Out of scope for *this* hook but the right reach for
   the MCP surface (see catalog #13).

> **Why PostToolUse earns its place even though the PreToolUse gate exists:** the
> gate can't size what it can't predict. `PreToolUse` sees a `Bash` *command*,
> not its output length; it sees a `WebFetch` *URL*, not the page size. Those are
> exactly the unbounded sources the input-gate **cannot** stop. PostToolUse is the
> only documented point to bound them *after* the bytes exist but *before* they
> hit context. That is the real, non-overlapping value-add.

---

## (c) Auto-summarizing via a secondary model/subagent — and the loss risk

**There is no native summarizer primitive.** `updatedToolOutput` is just a string
sink; any summarization is *your* code calling a model. Two shapes:

- **In-hook secondary model.** The hook calls a cheap model (e.g. Haiku 4.5) on
  the oversized `tool_response`, then emits the summary as `updatedToolOutput`.
  Compact, but: **synchronous** — it blocks the tool's return, adding network
  latency to *every* matching call; costs tokens/$ per fire; and is **lossy** in
  a dangerous way (the verified info-loss finding: summarizers preferentially
  **drop error lines, stack traces, and exact identifiers** — precisely what an
  agent needs from build/test output).
- **Subagent summarizer.** Hand the spool to a `Task`/`Explore` subagent. This is
  really **lever #2 (subagent offloading)** wearing a hook costume — and offloading
  is better done *deliberately at the call site*, not reactively from inside a
  blocking hook. A hook can't cleanly spawn-and-await a subagent anyway.

**Recommendation: default to deterministic trim; gate any summarizer behind an
opt-in flag and never let it be the only copy.** Summary drift / memory-
compression failure modes are well documented (indium.tech agent-memory piece;
the master report's pitfalls section). If you do summarize, **always spool the
raw output to disk first** so the summary is an *index into* truth, not a
*replacement for* it.

---

## Best viable design (given YES)

A `PostToolUse` companion to the existing gate. Deterministic, fail-open,
lossless-by-reference. **No second model by default.**

**Matcher** (`.claude/settings.json`): start with `Bash` (and optionally
`WebFetch`, `mcp__.*` read tools). Leave `Read` to the native PARTIAL view.

```json
{ "hooks": { "PostToolUse": [ { "matcher": "Bash|WebFetch",
  "hooks": [ { "type": "command",
    "command": "python3 \"$CLAUDE_PROJECT_DIR/hooks/trim_output.py\"" } ] } ] } }
```

**Hook logic** (`hooks/trim_output.py`):

1. Read stdin JSON; pull `tool_response` (handle both the string and
   structured-object shapes — bail to passthrough on an unexpected shape).
2. Estimate tokens (`len(text)//4`, same undercount-biased heuristic as the gate;
   share the budget env var, e.g. `CC_CONTEXT_GUARD_MAX_TOKENS`).
3. **Under budget →** emit nothing, exit 0 (original passes through unchanged).
4. **Over budget →** spool full output to
   `$CLAUDE_PROJECT_DIR/.cc-spool/<tool>-<n>.txt`, build
   `head (first K lines) + "\n…[trimmed M of N lines — full output: <path>; re-read selectively with Read(offset,limit)/grep]…\n" + tail (last J lines)`,
   and emit:
   ```json
   {"hookSpecificOutput":{"hookEventName":"PostToolUse","updatedToolOutput":"<trimmed>"}}
   ```
5. **Fail-open:** any exception / unparseable input / unknown version → emit
   nothing → full output passes through. A trimmer that bricks a result is worse
   than a big result.

**Why this is the right call for this project specifically:** it extends the
*same philosophy* as the PreToolUse gate (bound the cheap mistake, redirect to
selective access) to the *one class of overflow the gate structurally can't see*
(unpredictable command/fetch/MCP output sizes), using a **now-documented**
mechanism, with **zero added model calls** and a **pointer the agent can follow
back to full truth** — guarded, in turn, by the gate. It reinforces levers #2
(offloading, by spooling) and #3 (selective reading, by pointer) rather than
introducing lossy compaction, which the project deliberately declined.

### How to prove it works

- **Unit test (mirror `test/test_guard.py`):** pipe a PostToolUse JSON payload
  with an oversized `tool_response` to the hook on stdin; assert the emitted
  `updatedToolOutput` is (i) smaller than input, (ii) contains the pointer line,
  (iii) the spool file exists and holds the full original. Add an under-budget
  case → asserts empty stdout (passthrough). Add a garbage-stdin case → asserts
  passthrough (fail-open).
- **Live check:** run a real oversized command (e.g. `find / -type f` capped, or
  `cat` a big generated file via Bash) in-session and confirm the model receives
  the trimmed view with the pointer, then successfully `Read`s a slice of the
  spool.

---

## Caveats & failure modes (honest)

- **Structured-output tools can silently no-op.** For tools whose result is a
  structured object, `updatedToolOutput` reportedly must match the **expected
  shape** or it is **silently ignored** (community-level claim, not in the doc —
  *flagged as workaround/unverified*). Safest target is string-output `Bash`;
  validate per-tool before trusting it on MCP/structured built-ins.
- **No audit trail.** `updatedToolOutput` **discards** the original — the model
  never sees it and it isn't recoverable from the turn. The spool-to-disk step is
  not optional; it *is* the audit trail.
- **Latency.** A deterministic trim is ~free. An **in-hook model summarizer is
  synchronous and blocks every matching tool return** — measure before shipping.
- **Lossy summarizers drop exactly the wrong tokens** (errors/traces/ids) —
  verified info-loss risk. Keep summarization opt-in and raw-spooled.
- **Open / unverified:**
  - Exact `2.1.121` introduction line for `updatedToolOutput` not found in the
    raw changelog (capability is doc-confirmed regardless).
  - Whether replacing tool output interacts badly with **prompt caching** or
    **auto-compaction** — not documented; assume low risk (replacement happens
    before the turn is cached) but verify if you lean on caching heavily.
  - Precise token threshold that triggers the native `Read` PARTIAL view.

---

## Sources

**Primary (Anthropic):** `code.claude.com/docs/en/hooks` (the `updatedToolOutput`
quote — live-verified); `platform.claude.com/docs/en/agent-sdk/hooks`;
`anthropics/claude-code` `CHANGELOG.md` (PARTIAL view @ v2.1.145 — live-verified);
`anthropics/claude-code/.../skills/hook-development/SKILL.md`;
`anthropics/claude-agent-sdk-typescript` `CHANGELOG.md`; issues #36843, #41087,
#15897, #32105, #26968 (mostly *stale* "can't do it" reports — useful as the
before-picture). **Secondary:** `claudeupdates.dev/version/2.1.121`, `wotai.co`
(introduction version — uncorroborated by changelog). **Community/blog:**
`agentpatterns.ai` (output-replacement pattern), `composio.dev`, the "90%
reduction" Medium piece (wrapper-script token cuts), `indium.tech` (memory-
compression failure modes). Quality split per the shared constraints: rewrite
capability = **documented**; introduction version = **secondary/unconfirmed**;
structured-shape no-op = **community workaround**; caching/compaction interaction
= **speculation**.
