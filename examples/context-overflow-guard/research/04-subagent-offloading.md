# Subagent offloading: moving the heavy reads into someone else's context window

> **Source:** deep-research run for the subagent-offloading prompt (#4) in
> [`../research-prompts.md`](../research-prompts.md). Search angles: (a) Task/Agent
> tool & custom-agent context isolation, (b) Agent SDK `query()` subagents & the
> result `usage` object, (c) token measurement (transcript JSONL / `/context` /
> `ccusage`), (d) investigator patterns (Explore, Plan, fan-out, structured
> returns), (e) when offloading backfires. 20 sources fetched → 98 claims → 25
> verified (3-vote adversarial, need 2/3 to kill) → **23 confirmed, 2 killed** →
> synthesized into 10 findings. Sources are overwhelmingly **primary**: `code.claude.com`
> `/agent-sdk/subagents`, `/sub-agents`, `/context-window`, `/workflows`,
> `/agent-sdk/structured-outputs`, `/agent-sdk/cost-tracking`, `/agent-sdk/{typescript,python}`;
> `claude.com/blog/subagents-in-claude-code`; `anthropic.com/engineering/building-agents-with-the-claude-agent-sdk`;
> `ccusage.com/guide/cost-modes` + ccusage issue #313; with `inference.net` (secondary)
> re-checked against the primary SDK docs. Current as of **mid-2026** (Claude Code
> v2.1.x — docs reference the v2.1.63 Task→Agent rename, v2.1.117/2.1.161 forks,
> v2.1.154+ workflows, v2.1.172 nested subagents; Opus 4.8 / Sonnet 4.6 / Haiku 4.5,
> 1M context). Cross-checked against the live primary docs on 2026-06-23. A second
> independent deep-research run (17 sources → 85 claims → 25 confirmed, 0 killed)
> was merged in: it corroborated every load-bearing claim below with no
> contradictions, and contributed the **directly-observed transcript layout and
> `usage` field names** in §(b) (inspected on a live v2.1.x session, 2026-06-23),
> which partially close two of the open questions.

**Bottom line.** Offloading works because **a non-fork subagent runs in its own
fresh, isolated context window.** The parent's only *conversational* channel
downward is the **Agent tool's prompt string**; on the way back, **only the
subagent's final message returns — verbatim — as the Agent `tool_result`**, and
the subagent's intermediate file reads and tool outputs **never enter the
parent's context window.** That single fact is the whole mechanism: a research
subagent can read dozens of files while the parent absorbs a 400-token summary
(the docs' canonical illustration is **6,100 tokens read → a 420-token return**).
You *prove* the saving with an A/B run of the same investigation — inline vs
offloaded — reading **parent-context tokens** from `/context` and the session
JSONL `usage` fields, and **total tokens billed** from the Agent SDK result
message's cumulative `usage` + per-model `modelUsage`. The two diverge on
purpose: offloading **drops parent-context tokens** (what causes overflow) while
**raising total tokens billed** (you now pay for the subagent's reads, cache-cold,
on top of the parent). The strongest pattern is **Workflow fan-out**, where
intermediate results live in *script variables* and never touch Claude's context
at all. It backfires when many subagents each return *detailed* results (which
re-bloats the parent), when a too-tight summary drops detail the parent later
needs (forcing re-investigation), or when fan-out multiplies total cost ~4–7×.

---

## (a) Context isolation — what goes down, what comes back, what never lands

A non-fork subagent (the built-in `Explore`/`Plan` agents, anything in
`.claude/agents`, or an Agent-SDK `query()` subagent) **starts with a fresh,
isolated context window**. It does **not** see the parent conversation. The
[`/agent-sdk/subagents`](https://code.claude.com/docs/en/agent-sdk/subagents) doc
publishes an explicit "what subagents inherit" table; here it is collapsed into
down / back / never:

| Direction | What crosses the boundary |
|---|---|
| **Parent → subagent (down)** | The **Agent tool's prompt string** (the only payload derived from the parent's conversation). The subagent *independently* boots: its **own** system prompt + environment details (**not** the full Claude Code system prompt), the task message, **CLAUDE.md / memory** (loaded via `settingSources`), **tool definitions** (inherited, or the subset named in `tools`), a **git-status snapshot taken at the start of the parent session**, and any **preloaded skills** listed in the agent's `skills` field. |
| **Subagent → parent (back)** | **Only the subagent's final message**, returned **verbatim** as the Agent `tool_result`. The parent may then summarize it in its own user-facing reply. |
| **Never crosses (either way)** | The parent's **conversation history**, the parent's **tool results**, the parent's **system prompt**, files the parent already read, skills the parent already invoked, unlisted preloaded-skill content — and, crucially, **all of the subagent's intermediate tool calls and results.** |

**The crux (the thing the whole technique rests on):** the primary doc states it
plainly — *"Each subagent runs in its own fresh conversation. Intermediate tool
calls and results stay inside the subagent; only its final message returns to the
parent."* The [`context-window`](https://code.claude.com/docs/en/context-window)
doc quantifies it: *"The subagent read 6,100 tokens of files. You got a 420-token
result. That's the context savings."* Anthropic's engineering post frames the same
mechanism: subagents *"use their own isolated context windows, and only send
relevant information back to the orchestrator, rather than their full context"*
([building-agents-with-the-claude-agent-sdk](https://www.anthropic.com/engineering/building-agents-with-the-claude-agent-sdk)).

**Surface-by-surface (all behave the same way):**

- **Interactive CLI Agent tool** (the old `Task` tool, renamed at v2.1.63). You
  delegate via the Agent tool; the subagent's reads are absorbed in its window;
  you get the final message back as the tool result.
- **Custom agents in `.claude/agents`.** Same isolation. You shape *what comes
  back* with the agent's `prompt` (its system prompt) and *what it can do* with
  its `tools` allowlist. `Explore` and `Plan` are the **only** subagents that
  omit CLAUDE.md and the git-status snapshot from their boot context (they're
  deliberately lean).
- **Agent SDK `query()` subagents.** Identical model: the parent passes a prompt;
  the subagent runs its own loop; the SDK streams the subagent's intermediate
  messages to the *host application* (tagged with `parent_tool_use_id`) so a UI
  can show live progress — **but that stream does not enter the parent model's
  context window**, so it doesn't contradict isolation.
- **The one exception — forks.** A *fork* (v2.1.117/2.1.161) inherits the **full
  parent conversation**. Forks are the opposite of offloading; don't reach for
  one when your goal is to keep tokens *out* of the parent.

**Two honest caveats** (both *confirm* isolation is the intended default rather
than undermining it):

1. We **refuted** the stronger claim that *"the prompt string is the ONLY context
   the subagent has"* (verifier vote 1–2). It isn't — the subagent also boots
   CLAUDE.md, tool defs, git status, and listed skills on its own. The accurate
   statement: the prompt is the only channel for **conversation-derived** facts
   (what you just discussed, what the parent read). So you must inline *those*
   into the prompt; you do **not** need to re-paste CLAUDE.md.
2. Isolation can leak in specific reported bugs — GitHub #14118 (a background
   subagent's output exposed via `getOutput`) and #23463 (an oversized subagent
   result overflowing on return). These are filed *as bugs against the default*,
   which is exactly the point: isolation is the documented, intended behavior.

---

## (b) Measuring the saving — and the distinction that trips everyone up

There are **two different quantities** and conflating them is the classic
mistake:

- **Parent-context tokens** — how full the *parent's* window is. This is what
  causes overflow. Offloading **reduces** it.
- **Total tokens billed** — everything every agent consumed. Offloading **raises**
  it: you now pay for the subagent's reads (cache-cold) *plus* the parent. The
  [`workflows`](https://code.claude.com/docs/en/workflows) doc says it outright —
  *"A workflow spawns many agents, so a single run can use meaningfully more
  tokens than working through the same task in conversation. Runs count toward
  your plan's usage and rate limits like any other session."* Community estimates
  put multi-agent/agent-team runs at **~4–7× a standard session**, scaling roughly
  linearly with subagent count.

**A credible measurement must report BOTH.** Where to read each:

| Data source | What it gives you | Use it for | Caveats |
|---|---|---|---|
| **Session JSONL** `usage` on each assistant message: `input_tokens`, `output_tokens`, `cache_creation_input_tokens`, `cache_read_input_tokens` | The parent model's per-turn context load; the last assistant turn's `input_tokens` (+ cache reads) ≈ the parent context currently carried | **Parent-context tokens** | Subagent transcripts persist in **separate** JSONL files (`…/<session-id>/subagents/`), so the parent file alone is *already* parent-only — sum it for parent-context, add the `subagents/` files for total (recipe in the block below). Blog reports note CLI JSONL can *undercount* tokens vs the API (`gille.ai`, blog-quality). |
| **`/context`** (interactive) | A human-readable breakdown of current context-window usage | **Parent-context tokens**, eyeball before/after | Interactive only; no documented programmatic/headless equivalent (open question). |
| **Agent SDK result message** — cumulative `usage`, `total_cost_usd`, and the **`modelUsage`** map (per model: `inputTokens`, `outputTokens`, `cacheReadInputTokens`, `cacheCreationInputTokens`, `costUSD`) | The cleanest, scriptable surface; `modelUsage` lets you **attribute tokens per model** | **Both** — total via cumulative `usage`; parent-vs-subagent via `modelUsage` if they run different models | `total_cost_usd` is a **client-side estimate**, not authoritative billing ([cost-tracking](https://code.claude.com/docs/en/agent-sdk/cost-tracking)). Per-step usage is on `message.message.usage` (TS) / `message.usage` (Python). |
| **`ccusage`** | Daily/weekly/monthly/session token & cost rollups | Coarse session totals only | **Does not attribute sub-task tokens.** `cost-modes` documents only `auto`/`calculate`/`display`; per-subagent splitting is an *open* feature request (issue #313). We **refuted** (vote 0–3) the claim that ccusage reads the four JSONL fields for this purpose — **read the JSONL/SDK fields directly instead.** |

**The practical trick for clean attribution:** run the **subagents on Haiku** and
the **parent on Opus** (or any two distinct models). Then the SDK `modelUsage` map
splits the bill for you — the Opus row is (approximately) parent-context work, the
Haiku row is offloaded work — without having to disentangle merged JSONL files.
This is the documented purpose of `modelUsage`: *"useful when you run multiple
models (for example, Haiku for subagents and Opus for the main agent) and want to
see where tokens are going."*

**Reading it straight from the transcripts (observed on a live v2.1.x session,
2026-06-23 — not documented, verify on your version).** You don't need the SDK to
separate parent from subagent tokens: the CLI already writes them to *different
files*, which turns the "merged JSONL" caveat above into an advantage.

- **Parent session transcript:** `~/.claude/projects/<project-slug>/<session-id>.jsonl`
- **Subagent transcripts:** `~/.claude/projects/<project-slug>/<session-id>/subagents/**/agent-<id>.jsonl`,
  each with a sibling `agent-<id>.meta.json` (e.g. `{"agentType":"workflow-subagent"}`).

Each `assistant` record carries `message.usage` with these exact fields:

```json
{ "input_tokens": 3269,
  "cache_creation_input_tokens": 6681,
  "cache_read_input_tokens": 0,
  "cache_creation": { "ephemeral_5m_input_tokens": 6681, "ephemeral_1h_input_tokens": 0 },
  "output_tokens": 1,
  "service_tier": "standard" }
```

So **parent-only** = sum over the `<session-id>.jsonl` alone; **total billed** =
that plus every file under `subagents/`; **offloaded volume** = the `subagents/`
files alone. Total any one transcript with:

```bash
python3 - "$T" <<'PY'
import json,sys
inp=cc=cr=out=0
for l in open(sys.argv[1]):
    l=l.strip()
    if not l: continue
    u=(json.loads(l).get("message") or {}).get("usage")
    if u:
        inp+=u.get("input_tokens",0); cc+=u.get("cache_creation_input_tokens",0)
        cr+=u.get("cache_read_input_tokens",0); out+=u.get("output_tokens",0)
print(f"input={inp} cache_create={cc} cache_read={cr} output={out}")
PY
```

This is the headless A/B harness's data source when the SDK `modelUsage` split
isn't available (e.g. both arms on the same model).

---

## (c) Investigator patterns that return tight summaries

The goal of every pattern below is the same: **do the heavy reading in the
subagent's window, hand back the minimum the parent needs.**

| Pattern | What it is | What comes back | Best for | Source |
|---|---|---|---|---|
| **`Explore` (built-in)** | Fast, **read-only** Haiku agent; **denied Write/Edit**; "keeps exploration results out of your main conversation context" | Locations / summaries, **not file dumps** | "Where is X? How does Y flow?" codebase recon | [`/sub-agents`](https://code.claude.com/docs/en/sub-agents) |
| **`Plan` (built-in)** | Read-only research agent used in plan mode; "exploration output stays in a separate context window while the main conversation remains read-only" | A plan / findings, parent stays clean | Designing a change before touching files | [`/sub-agents`](https://code.claude.com/docs/en/sub-agents) |
| **Custom investigator** (`.claude/agents`) | Constrained `tools` allowlist + a "**return only X**" output contract in the agent prompt | Whatever the contract dictates | Domain-specific recon you run repeatedly | [`/sub-agents`](https://code.claude.com/docs/en/sub-agents) |
| **Parallel search fan-out** | Multiple search subagents, each a different query, each returning **only relevant excerpts** | A handful of excerpts per agent | "Sift a large corpus where most of it won't be useful" (Anthropic's exact phrasing) | [engineering blog](https://www.anthropic.com/engineering/building-agents-with-the-claude-agent-sdk) |
| **Workflow fan-out** ⭐ | Orchestration **script** runs in an isolated runtime; intermediate agent results live in **script variables** | **Only the final answer** reaches Claude's context | Large parallel investigations; the strongest parent-context saver | [`/workflows`](https://code.claude.com/docs/en/workflows) |

**Structured output is the enforcement mechanism.** An investigator can use *any*
tools it likes mid-flight (chain `Grep`, then `Bash`, over many steps) yet be
forced to return validated JSON matching a caller-defined schema at the end. In
the Agent SDK, pass `outputFormat` (TS) / `output_format` (Python) to `query()`
with `{ type: 'json_schema', schema }`; the validated object arrives on the result
message's **`structured_output`** field (populated only when
`subtype === 'success'`; on failure the subtype is
`error_max_structured_output_retries`). The SDK **validates and re-prompts on
mismatch** ([structured-outputs](https://code.claude.com/docs/en/agent-sdk/structured-outputs)).
This is precisely how you guarantee a *tight* return: heavy tool work happens
internally, but the parent only ever receives a schema-bounded object. (Inside a
`Workflow` script, the `agent(prompt, { schema })` call does the same thing —
see the recipe.)

**Why Workflow fan-out is the strongest lever.** For plain subagents and agent
teams, the final result *does* land in *a* context window. In a Workflow, the
[`/workflows`](https://code.claude.com/docs/en/workflows) doc is explicit: *"The
workflow runtime executes the script in an isolated environment, separate from
your conversation. Intermediate results stay in script variables instead of
landing in Claude's context"* — its comparison table lists "Where intermediate
results live" as **Script variables** (Workflows) vs **Claude's context window**
(plain Subagents/Skills). So a fan-out of 12 file-readers can each burn 5k tokens
and the parent sees only the merged conclusion.

---

## (d) When offloading backfires

Offloading is not free, and several failure modes are documented or follow
directly from the mechanics above.

1. **Re-bloating the parent (documented).** The [`/sub-agents`](https://code.claude.com/docs/en/sub-agents)
   doc carries a warning box: *"When subagents complete, their results return to
   your main conversation. Running many subagents that each return detailed
   results can consume significant context."* Offloading 8 investigators that each
   return 3k tokens dumps 24k back into the parent — you've moved the overflow,
   not removed it. **Mitigation:** schema-bound the returns; for sustained
   parallelism or work exceeding the window, use **agent teams** (each worker gets
   its *own* independent context) or a **Workflow** (returns stay in script vars).
2. **Total token / $ blowup (documented).** ~4–7× total tokens, counted against
   plan rate limits. A blog post documents a subagent "cost explosion" at ~887k
   tokens/minute (`aicosts.ai`, blog-quality — directional, not authoritative).
3. **Round-trip latency / task too small (reasoned).** A subagent pays a
   cache-cold boot (its system prompt, CLAUDE.md, tool defs) before doing any
   work. For a 2-file glance, that boot costs more than just reading inline. Below
   some break-even task size, offloading is pure overhead.
4. **Summary drops detail the parent later needs (reasoned).** A too-tight "return
   only X" contract can omit a `file:line` or an edge case the parent then has to
   **re-investigate** — a second round trip that erases the saving. Tune the
   schema to return *pointers* (paths, line ranges, symbol names) the parent can
   cheaply re-open on demand, not prose the parent must trust blindly.
5. **Re-establishing context the subagent already held (reasoned).** The
   subagent's window is discarded except for its final message. If the parent's
   next step needs something the subagent saw but didn't return, that knowledge is
   gone — the parent must re-derive it. Return enough *handles* to avoid this.

> Failure modes 1–2 are **documented**; 3–5 are **reasoned implications** of the
> documented isolation mechanics (subagent context is discarded except the final
> message; the parent never saw the intermediate reads), not verbatim warnings.

---

## Recipe: an investigator offloading harness (and how to PROVE it)

### The harness

Five rules, each mapped to a verified finding:

1. **Make the prompt self-contained.** The prompt string is the only
   conversation-derived channel down — inline the file paths, the error text, the
   decisions made so far. (Don't re-paste CLAUDE.md; the subagent boots that
   itself.)
2. **Constrain the tools.** Give the investigator a **read-only** subset, or just
   use the built-in `Explore`/`Plan`. Cheap, safe, and it *can't* wander into edits.
3. **Enforce a "return only X" contract.** A JSON schema that returns **pointers,
   not prose** — paths, line ranges, symbol names, a 3-sentence finding — so the
   heavy reads are consumed internally and the parent absorbs a tight object.
4. **Fan out in a Workflow** when the investigation is wide (many files / queries),
   so intermediate results stay in script variables and never reach Claude's context.
5. **Escalate to agent teams** only for sustained parallelism that exceeds the
   context window.

**A custom investigator agent** (`.claude/agents/investigator.md`):

```markdown
---
name: investigator
description: Read-only codebase investigator. Reads heavy material in its own
  context and returns a tight, pointer-based summary. Use for "how does X work?"
  recon where the parent must not absorb the file bodies.
tools: Read, Grep, Glob
model: haiku            # cheap; and makes modelUsage attribution clean (see proof)
---
You are an investigator. Do the heavy reading HERE, in your own context.

Return ONLY:
- **Answer:** ≤5 sentences addressing the question.
- **Evidence:** up to 8 lines of `path:line — one-line note` pointers.
- **Open:** anything you could not determine.

Never paste file bodies or full function source. Return pointers the caller can
re-open on demand, not prose they must trust blindly.
```

**An `/investigate` slash command** (`.claude/commands/investigate.md`) that just
delegates to it:

```markdown
---
description: Offload a heavy investigation to a read-only subagent; only a tight summary returns.
---
Use the Agent tool with subagent_type "investigator" to answer: $ARGUMENTS

Pass a self-contained prompt (paths, errors, decisions). Do NOT read the files
yourself — let the subagent's context absorb them and report back only its summary.
```

**A Workflow fan-out** (when many files must be surveyed in parallel — strongest
parent-context saver, because returns stay in script variables):

```js
export const meta = {
  name: 'investigate-fanout',
  description: 'Survey N areas in parallel; only the merged summary reaches context.',
  phases: [{ title: 'Investigate' }, { title: 'Merge' }],
}
const FINDING = { type: 'object', required: ['answer', 'pointers'], properties: {
  answer:   { type: 'string' },
  pointers: { type: 'array', items: { type: 'string' } },  // "path:line — note"
}}
const areas = args   // e.g. ["auth", "session store", "token refresh", ...]
phase('Investigate')
const found = await parallel(areas.map(a => () =>
  agent(`Investigate "${a}". Read what you need; return only answer + pointers.`,
        { label: `inv:${a}`, schema: FINDING, model: 'haiku' })))   // reads stay in the agent
phase('Merge')
return found.filter(Boolean)   // tight objects only — never the file bodies
```

### The A/B proof protocol

Run the **same heavy investigation twice**, identical parent deliverable, and show
parent-context tokens drop while total billed rises.

- **Arm A — inline.** The parent reads all the files itself, then answers.
- **Arm B — offloaded.** The parent spawns the `investigator` / Workflow, which
  reads the files; the parent answers from the returned summary.

**Measure (the metric that matters = parent-context tokens):**

1. **Interactive CLI.** Run `/context` immediately **before** and **after** the
   task in each arm; the delta is parent-context growth. Cross-check against the
   parent session JSONL: the last assistant turn's `usage.input_tokens`
   (+ `cache_read_input_tokens`) ≈ the parent context carried. Parent JSONL lives
   under `~/.claude/projects/<project-slug>/<session-id>.jsonl`; **subagent
   transcripts are separate files** under `…/<session-id>/subagents/` — read the
   parent's only for this number (use the totaling snippet in §b).
2. **Agent SDK (cleanest, scriptable).** Wrap each arm in a `query()` and read the
   **result message**: cumulative `usage` (total), `total_cost_usd` (estimate),
   and **`modelUsage`** (per-model split). Run the subagent on **Haiku**, the
   parent on **Opus**, so `modelUsage['…opus…'].inputTokens` is your parent-context
   proxy and `modelUsage['…haiku…']` is the offloaded work.

**Expected result (the shape that proves it):** Arm B's **parent-context tokens
drop sharply** (the subagent's reads never hit the parent — the docs' 6,100 → 420
ratio is the canonical shape) while Arm B's **total billed tokens rise** (you now
pay for the subagent's cache-cold reads on top of the parent). If parent-context
*doesn't* drop, your subagent is returning too much — tighten the schema. If total
billed *doesn't* rise, you probably didn't actually offload.

| Metric | Where to read it | Arm A (inline) | Arm B (offloaded) — expected |
|---|---|---|---|
| **Parent-context tokens** | `/context` delta; parent JSONL `usage.input_tokens` (last turn); SDK `modelUsage[parent-model].inputTokens` | high (all file bodies) | **much lower** (summary only) |
| **Total tokens billed** | SDK result cumulative `usage`; `total_cost_usd` (estimate); sum over `modelUsage` | baseline | **higher** (subagent reads + boot) |
| **Cache behavior** | `cache_creation_input_tokens` / `cache_read_input_tokens` | parent re-reads warm-cached | subagent boot is **cache-cold** (writes, not reads) |

---

## Version sensitivity & open questions

**Version-sensitive (all scoped to Claude Code v2.1.x / mid-2026):** the **Task →
Agent** tool rename landed at v2.1.63 (older "Task tool" material maps to today's
Agent tool); **forks** at v2.1.117/2.1.161; **workflows** require v2.1.154+;
**nested subagents** v2.1.172. Subagent and workflow APIs are actively evolving —
re-check field names against the live docs before relying on them. The
`structured_output` field, `modelUsage` shape, and exact cache-pricing multipliers
(~1.25× writes, ~0.1× reads) are TTL- and version-sensitive.

**Refuted (kept for transparency):**
- *"The prompt string is the **only** context a subagent has"* — vote **1–2**. It
  also boots CLAUDE.md, tool defs, git status, and listed skills. Accurate version:
  the prompt is the only **conversation-derived** channel.
- *"`ccusage` reads the four JSONL `usage` fields to split parent-vs-subagent
  tokens"* — vote **0–3**. It doesn't; per-subagent attribution is an open feature
  request (#313). Read the JSONL/SDK fields directly.

**Open questions (could not be resolved from the docs):**
- **Magnitude.** The docs give one illustrative ratio (6,100 → 420). No benchmark
  establishes typical savings, the break-even task size below which a round trip
  isn't worth it, or how savings scale with file count. **Measure it** with the
  A/B protocol.
- **Clean JSONL attribution.** *Partially resolved* (observed 2026-06-23, not
  documented): the CLI writes the parent and each subagent to **separate** files
  (`<session-id>.jsonl` vs `…/<session-id>/subagents/**/agent-<id>.jsonl`), so
  summing `message.usage` per file already splits *parent-only* vs *total* (see
  §b) — no `modelUsage` trick required, though that remains the documented route.
  What's still undocumented: whether this on-disk layout and the `usage` field
  set are stable across versions.
- **Programmatic `/context`.** Still no confirmed non-interactive equivalent of the
  `/context` *command*. But the A/B harness no longer needs one: parsing the parent
  `<session-id>.jsonl` (last assistant turn's `input_tokens` + `cache_read_input_tokens`)
  gives a captureable parent-context proxy headlessly.
- **Concurrency caps / rate-limit interaction** for fan-out subagents and agent
  teams in v2.1.x are not documented beyond "counts like any other session."

---

## Sources

**Primary (Anthropic):** `code.claude.com/docs/en/agent-sdk/subagents` (the
"what subagents inherit" table — load-bearing for §a), `/sub-agents` (Explore/Plan
definitions, the "results return to your main conversation" warning),
`/context-window` (the 6,100 → 420 quantification), `/workflows` (script-variable
isolation; the total-token cost caveat), `/agent-sdk/structured-outputs`
(`outputFormat`/`structured_output`/re-prompt), `/agent-sdk/cost-tracking`
(`usage`, `total_cost_usd`, `modelUsage`; the client-side-estimate warning),
`/agent-sdk/{typescript,python}` (per-step `usage` location);
`claude.com/blog/subagents-in-claude-code`;
`anthropic.com/engineering/building-agents-with-the-claude-agent-sdk` (isolated
context windows; parallel search subagents returning only excerpts);
`ccusage.com/guide/cost-modes` + GitHub `ccusage` **#313** (sub-task attribution
is an open request). GitHub `anthropics/claude-code` **#14118**, **#23463**
(isolation-leak bugs that confirm the default), **#38443/#25754/#15139** (requests
to inherit parent context — confirm isolation is the default), **#29768/#45357**
(Explore = Haiku default). **Secondary:** `inference.net/content/claude-agent-sdk-production-guide`
(re-verified against primary SDK docs). **Community/blog (directional only):**
`gille.ai` (CLI JSONL can undercount tokens), `jdhodges.com` (`/context` usage),
`aicosts.ai` (subagent cost-explosion anecdote), `richsnapp.com`, `builder.io`,
`mindstudio.ai`, `nimbalyst.com`.

**Quality split per the shared constraints:** the isolation mechanics (down/back/never),
the measurement fields (`usage`/`modelUsage`/cache fields/`total_cost_usd`), the
investigator patterns, and the "many detailed returns re-bloat the parent" /
"workflows cost more total tokens" failure modes are **documented**. The
different-models `modelUsage` attribution trick and the schema-returns-pointers
discipline are **community workaround / engineering judgment**. The latency,
dropped-detail-re-investigation, and re-establish-context failure modes, and the
empirical magnitude of savings for any specific task, are **reasoned / unverified —
measure them.** The on-disk transcript layout and exact `usage` field names in §b
are **directly observed** (a live v2.1.x session, 2026-06-23), not documented —
treat them as version-sensitive.
