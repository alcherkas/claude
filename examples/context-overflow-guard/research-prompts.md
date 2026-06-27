# Context-overflow safeguards — options & deep-research prompts

Iteration scratchpad for the original request: *"avoid context overflows → build
a safeguard in Claude Code with proof it works → efficiently pass the context."*

**Status:** Option 1 (input-gate hook) is built in this folder. Everything else
below is candidate work, not yet implemented.

**Chosen efficiency levers** (from earlier clarification): **prompt caching**,
**subagent offloading**, **selective reading**. Compaction was *not* picked.

### How to use this file
Each topic has a copy-paste **deep-research prompt**. Run one with the research
skill — `/deep-research <paste the prompt>` (or pass it as `args`) — to get a
cited report before committing to a build. Start with the **master prompt** for
the lay of the land, then drill into specific options.

**Shared constraints (apply to every prompt below):**
- Focus on Claude Code (CLI) and the Anthropic ecosystem; current as of mid-2026.
- Prefer official docs (code.claude.com, docs.claude.com) and primary sources;
  cite every non-obvious claim with a URL.
- Flag anything version-sensitive or that you could not verify; separate
  "documented" from "community workaround" from "speculation."
- Where relevant, return concrete config/snippets, a concrete way to *prove* the
  technique works, and the trade-offs / failure modes — not just description.

---

## ⭐ Master prompt — solutions & guardrails for context overflow

📄 **Researched** → [`research/00-master-context-overflow.md`](research/00-master-context-overflow.md)

```text
Research the full landscape of solutions and guardrails for preventing LLM
context-window overflow in agentic coding tools, centered on Claude Code (CLI)
but informed by general agent-framework practice (Claude Agent SDK, LangGraph,
and others).

Answer:
1. CAUSES — what actually fills an agent's context in practice? Rank the
   offenders (whole-file reads, log/command dumps, repeated re-reads, large tool
   results, verbose system/CLAUDE.md, long histories). Cite evidence.
2. SYMPTOMS & DETECTION — how do you know you're approaching overflow? What
   signals exist in Claude Code (statusline, auto-compaction triggers, transcript
   token accounting) and how accurate are they?
3. MITIGATION TAXONOMY — organize every known technique into categories:
   prevention (gate inputs), reaction (trim/summarize results), offloading
   (subagents), reuse (prompt caching), curation (lean baseline / CLAUDE.md),
   compaction (auto + manual + PreCompact), observation (meters). For each
   category: the mechanism, the Claude Code feature that implements it, maturity,
   and how to verify it works.
4. EFFECTIVENESS — which techniques give the biggest token reduction per unit of
   effort? Any benchmarks or measured numbers?
5. PITFALLS — where do guardrails backfire (lossy compaction, summary drift,
   subagent round-trip cost, over-aggressive blocking)?

Deliverable: a structured, cited report with a decision framework ("if your
overflow is caused by X, reach for Y first") and a comparison table.
Follow the shared constraints.
```

---

## Option catalog

> Grouped by intent. ★ = fit to the chosen levers (caching / offloading /
> selective reading).

### 1. Input-gate hook — `PreToolUse` denies oversized reads ✅ built · ★★★
Blocks `Read`/`cat` of files over a token budget; steers to selective reads.
Proof: unit tests on hook I/O.
📄 **Researched** → [`research/01-pretooluse-input-gate.md`](research/01-pretooluse-input-gate.md)

```text
Research best practices for Claude Code PreToolUse hooks used as context
safeguards that block or redirect oversized file reads.
Answer: (a) the current, exact PreToolUse contract — stdin fields, the
permissionDecision allow/deny/ask/defer JSON, exit-code semantics, matcher
syntax for Read|Bash; (b) reliable ways to ESTIMATE a file's token cost cheaply
inside a hook (byte/4 heuristic vs. the Anthropic token-counting API vs. tiktoken
— accuracy and latency trade-offs); (c) existing open-source examples of read-
size / context guards for Claude Code and what they get right or wrong; (d)
deny-vs-ask UX and "fail-open" safety design; (e) failure modes (long single
lines, binary files, generated bundles). Deliverable: a verified spec plus a
hardened reference design with its proof method. Follow the shared constraints.
```

### 2. Pre-flight context estimator — `/context-check` slash command · ★★☆ 🔎 researched → [`research/02-context-check-estimator.md`](research/02-context-check-estimator.md)
Scans files/dirs you're about to touch, ranks by token cost, flags the
expensive ones before you dive in. Proof: ranking assertion.

```text
Research how to build a Claude Code slash command that estimates the token cost
of files/directories BEFORE the agent reads them, to prevent context overflow.
Answer: (a) how custom slash commands work today (definition format, arguments,
where they live, whether they can run scripts); (b) the most accurate practical
way to count/estimate tokens for a repo subtree (Anthropic count_tokens API vs.
local tokenizers) and the speed/accuracy trade-off; (c) how tools like repomix,
ai-digest, or tiktoken-based scanners present per-file token budgets; (d) good
UX for surfacing "these N files would cost X tokens — read selectively." 
Deliverable: a design for the command + how to prove its estimates are accurate.
Follow the shared constraints.
```

### 3. Output-trimmer / summarizer — `PostToolUse` hook · ★★☆ (capability VERIFIED — researched: [`research/03-posttooluse-output-trimmer.md`](research/03-posttooluse-output-trimmer.md))
Catches a tool result that came back huge and truncates or subagent-summarizes
it. Acts on the *result*, not the call. **YES, a PostToolUse hook can fully
replace a tool result via `hookSpecificOutput.updatedToolOutput`** (all tools,
Claude Code v2.1.121+; previously MCP-only). No native summarizer — prefer a
deterministic trim-with-pointer (spool full output to disk) over a lossy
in-hook LLM summarizer. Bounds the one overflow source the PreToolUse gate can't
predict: `Bash`/`WebFetch`/MCP output sizes.

```text
Research whether and how a Claude Code PostToolUse hook can REDUCE the size of a
large tool result before it enters the model's context.
Answer precisely: (a) what a PostToolUse hook can actually do to a tool result —
can it replace/rewrite the output, or only append additionalContext / block?
Quote the docs on the exact output schema (decision, additionalContext,
hookSpecificOutput). (b) If full rewriting isn't supported, what are the
sanctioned patterns to shrink big outputs (wrapper scripts, MCP tools that
paginate, truncation-with-pointer)? (c) Patterns for AUTO-SUMMARIZING large tool
outputs via a secondary model/subagent and the information-loss risk. 
Deliverable: a clear yes/no on the rewrite capability with citations, plus the
best viable design given the answer. Follow the shared constraints.
```

### 4. Subagent-offloading harness — `/investigate` command or Workflow · ★★★ (your lever #2)
Always fan heavy reading out to `Task`/`Explore` subagents; only summaries
return. Proof: measured token A/B (inline vs offloaded).
📄 **Researched** → [`research/04-subagent-offloading.md`](research/04-subagent-offloading.md)

```text
Research subagent-based context offloading in Claude Code as a defense against
context overflow.
Answer: (a) exactly how context isolation works for the Task/subagent tool and
custom agents — what the parent sends down, what comes back, and whether the
subagent's file reads ever land in the parent context; (b) how to MEASURE the
token savings of offloading a heavy investigation to a subagent vs. doing it
inline (experimental design + where to read the token counts); (c) design
patterns for "investigator" subagents that return tight summaries, including the
Explore agent and Workflow fan-out; (d) when offloading BACKFIRES (round-trip
latency, summary loses needed detail, too many subagents). Deliverable: a recipe
+ a concrete A/B measurement protocol that proves the savings. Follow the shared
constraints.
```

### 5. Prompt-caching demonstrator · ★★☆ (your lever #1; automatic in CLI)
Prove a stable prefix is read-from-cache. Proof: `cache_read_input_tokens`
rising across turns. In CLI this is automatic — implementation version is Agent
SDK/API.
📄 **Researched** → [`research/05-prompt-caching.md`](research/05-prompt-caching.md)
**Key finding:** caching re-reads the unchanged prefix at ~0.1× input price after a
one-time write premium (1.25× for 5-min, 2× for 1-hour); Claude Code does it
automatically (`tools→system→messages` prefix, append-only). **Correction baked in:**
the web pass got the per-model *minimum cacheable sizes* wrong — the current Opus
family (4.5–4.8) and Haiku 4.5 are **4,096 tokens, not 1,024** (authoritative source:
the version-matched bundled `claude-api` skill, which also documents the per-parameter
invalidation table, 20-block lookback, and break-even). Two reproducible proofs:
transcript-JSONL `message.usage` turn-over-turn, and a two-call raw Messages-API script.

```text
Research Anthropic prompt caching as a context-efficiency mechanism, and how it
applies to Claude Code.
Answer: (a) the current mechanics — cache_control breakpoints, minimum cacheable
token sizes per model, TTL (5-min vs extended), max breakpoints, and pricing of
cache-write vs cache-read; (b) how Claude Code uses prompt caching automatically
in a session and what invalidates the cache (tool definitions, system prompt,
CLAUDE.md edits, history changes); (c) how to VERIFY cache hits — which usage
fields (cache_creation_input_tokens, cache_read_input_tokens) and how to inspect
them from a Claude Code session or via the API; (d) prompt-structuring best
practices to maximize cache reuse (stable prefix first, volatile content last).
Deliverable: an explanation + a reproducible way to prove caching is happening
with real numbers. Follow the shared constraints.
```

### 6. SessionStart curated loader / CLAUDE.md discipline · ★★☆
Inject a tight project brief + pointers at session start so the agent doesn't
re-discover everything. Proof: baseline-token measurement, curated vs naive.

```text
Research how to minimize a Claude Code session's BASELINE context via curated
loading and CLAUDE.md discipline, to leave more headroom before overflow.
Answer: (a) what consumes context at session start (system prompt, CLAUDE.md,
memory, tool defs) and typical sizes; (b) SessionStart hook capabilities — can it
inject curated context, and how; (c) CLAUDE.md best practices for being terse-yet-
sufficient (pointers over full content, hierarchical/imported memory) with cited
guidance; (d) how to measure baseline token usage and compare a curated brief vs.
letting the agent explore from scratch. Deliverable: a curation playbook + a
before/after measurement method. Follow the shared constraints.
```

### 7. PreCompact preservation hook + compaction tuning · ★☆☆ (not a chosen lever)
Make compaction lossless for critical state. Proof: state survives a simulated
compact.

```text
Research Claude Code context compaction and how to make it lossless for critical
state. Answer: (a) how auto-compaction and /compact actually work — triggers,
thresholds, what gets summarized vs dropped, and any "microcompact" behavior; (b)
the PreCompact hook — when it fires and what it can influence; (c) techniques to
ensure key working state (current task, file list, decisions) survives a compact;
(d) documented failure modes where compaction loses important context.
Deliverable: a compaction-tuning guide + a way to prove preserved state survives.
Follow the shared constraints.
```

### 8. Live context-budget statusline / meter · ★★☆
Statusline estimating current consumption with threshold warnings (70%/90%).
Doesn't block; triggers self-correction. Proof: simulated transcript → correct
%/tier.
📄 **Researched** → [`research/08-statusline-context-meter.md`](research/08-statusline-context-meter.md)
**Key finding:** you barely have to estimate — Claude Code now pipes a fully
populated `context_window` block to the statusline stdin, including a
**pre-calculated `used_percentage`** (input-only: `input + cache_creation +
cache_read`, *excludes* output) and `context_window_size` (200000 | 1000000). The
meter just reads it; the transcript JSONL is the *fallback* for the `null` windows
(pre-first-call and right after `/compact`). The one conceptual trap proven
firsthand: current context = the **LAST assistant message's input-side usage**,
**not** a running sum of turns (naive summing over-counted by **39×**). The
documented color tiers are **green <70 / yellow 70–89 / red 90+**; the "~83%
auto-compaction" figure is **refuted/speculation** (no published %). Reference
script + `settings.json` config included and **functionally tested** (tier
boundaries, all null fallbacks).

```text
Research building a Claude Code statusline that shows live context-window usage
as an overflow early-warning system. Answer: (a) the statusline API — what JSON
data the statusline script receives each render (does it include token/context
usage, transcript path, model context limit?); (b) how to compute a reliable
"percent of context used" from that data or from the transcript JSONL; (c) the
context-window sizes of current Claude models and how Claude Code accounts for
them; (d) good threshold/warning UX. Deliverable: a meter design + how to prove
its percentage is accurate against a known transcript. Follow the shared
constraints.
```

### 9. Overflow "thrash-test" A/B harness · ★★★ (strongest "proof it works")
Drive a session toward overflow with vs without the safeguard; chart bounded vs
runaway token growth. Empirical before/after, not a unit test.
📄 **Researched** → [`research/09-ab-thrash-test.md`](research/09-ab-thrash-test.md)

```text
Research how to empirically PROVE a context-overflow safeguard works in Claude
Code via an A/B experiment. Answer: (a) how to run Claude Code headlessly/
scriptably (claude -p / print mode, output formats) for reproducible runs; (b)
how to read per-turn token accounting from the session transcript (JSONL schema,
usage fields) to chart context growth over a run; (c) how to design a scripted
scenario that deliberately tends toward overflow (forced big reads) and run it
WITH vs WITHOUT a guard hook; (d) what metric cleanly demonstrates the
difference (peak context %, turns-to-overflow, total input tokens). Deliverable:
an experimental protocol + the exact data sources that make the proof credible.
Follow the shared constraints.
```

### 10. Defense-in-depth bundle · ★★★
Combine gate (#1) + meter (#8) + offload (#4) + curated load (#6) as layered
safeguards with an integration test.
📄 **Researched** → [`research/10-defense-in-depth.md`](research/10-defense-in-depth.md)

```text
Research layered ("defense in depth") context-management architectures for
production agentic coding systems, to combine multiple overflow safeguards
coherently. Answer: (a) how teams stack prevention + offloading + caching +
observability + compaction, and in what order they fire; (b) reference
architectures or case studies (Anthropic's own agent guidance, notable OSS agent
projects) with cited specifics; (c) interactions and conflicts between layers
(e.g., a blocking hook vs. auto-compaction, caching invalidated by curated
loaders); (d) how to integration-test a stack of safeguards. Deliverable: a
layered reference design with the firing order, conflict notes, and a test plan.
Follow the shared constraints.
```

---

## Semantic-access additions (LSP & IDE MCP)

These came out of asking "are LSP and the IntelliJ IDEA MCP relevant?" — they
are, strongly. The premise: stop pulling *raw text* into context and query
*meaning* instead. The IDE/language-server index is a pre-built store that lives
*outside* the model's window; you query it with tiny prompts. This is selective
reading taken to its limit, with a side of offloading (the code stays in the
index). Two-sided, though: some IDE/MCP tools still return bulk, and a big MCP
server taxes baseline context with its tool schemas.

### 11. Semantic code access via LSP / IDE-MCP — navigate & understand without reading · ★★★ (selective reading, maxed)
📄 **Researched** → [`research/11-semantic-access-lsp-ide-mcp.md`](research/11-semantic-access-lsp-ide-mcp.md)
Go-to-definition, find-references, hover/type-info, document/workspace symbols,
call/type hierarchy, diagnostics, and IDE rename/reformat replace whole-file
reads. Returns locations/structure/types (tens of tokens) instead of file bodies
(thousands), and is *more precise* than grep, so fewer wasted reads. IDE-driven
refactors mutate many files the agent never reads.
**Key finding:** "Claude Code has no LSP" is *false* as of mid-2026 — it ships a
**built-in LSP tool** activated by official **code-intelligence plugins** (11
languages); serena / JetBrains-MCP / CodeGraph are the alternatives. Anthropic's
own docs endorse it: *"a single 'go to definition' call replaces … a grep followed
by reading multiple candidate files."*

```text
Research using semantic code intelligence — Language Server Protocol (LSP) and
IDE Model Context Protocol servers (especially the JetBrains / IntelliJ IDEA
MCP) — as a context-efficiency strategy for Claude Code, replacing raw file
reads with semantic queries.
Answer:
1. Which semantic operations most reduce context — go-to-definition, find-
   references, hover/type-info, document & workspace symbols, call/type
   hierarchy, diagnostics, rename/reformat. For each: what it returns and rough
   token cost vs. reading the file(s) it replaces.
2. How to wire this into Claude Code TODAY: any native LSP support? LSP-to-MCP
   bridges (e.g. serena, lsp-mcp and similar) — maturity and language coverage?
   The JetBrains/IntelliJ MCP — what tools it exposes (symbol search,
   get_symbol_info, get_file_problems, PSI tree, refactorings) and setup.
3. PRECISION benefit: how semantic navigation beats grep/whole-file reads in
   accuracy (scope- and overload-aware) and therefore avoids wasted reads.
4. The "IDE index as offloaded context" idea — querying a pre-built project
   index instead of holding code in-context (e.g. an LSP/IDE index, or a
   code-graph indexer like CodeGraph).
5. Measured or estimated token savings, and where it does NOT help.
Deliverable: a practical guide to semantic-access context reduction in Claude
Code plus a way to measure the savings. Follow the shared constraints.
```

### 12. Diagnostics instead of build/test dumps · ★★★ (selective reading; kills a top overflow source)
Pasting compiler/test/log output is a leading overflow cause. Pull structured
diagnostics instead — LSP `publishDiagnostics` or IntelliJ `get_file_problems` →
a handful of errors with `file:line`, not thousands of log lines. Plus debugger
inspection (evaluate-expression, frame values) instead of print-and-dump.

```text
Research replacing verbose build/compile/test/log output with structured
diagnostics to prevent context overflow in Claude Code.
Answer:
1. Sources of compiler-grade signal without dumping output: LSP textDocument/
   publishDiagnostics, IntelliJ MCP get_file_problems / inspections, tsc JSON,
   eslint --format json, etc. What each returns and how compact.
2. How to surface ONLY diagnostics for changed files/regions rather than a
   whole-project build log.
3. Interactive debugging as a context-saver: IDE/MCP debugger tools (evaluate-
   expression, frame values, conditional breakpoints) vs. adding print
   statements and re-running with full stdout capture.
4. Patterns to wrap noisy build/test commands so only failures/summaries reach
   the model (relation to a PostToolUse trimmer or a wrapper script).
Deliverable: a "diagnostics over dumps" playbook with concrete tool invocations
and token-cost comparisons. Follow the shared constraints.
```

### 13. Guard & budget the MCP/IDE tool surface · ★★☆ (safeguard extension + baseline cost)
Two-sided. (a) Some IDE/MCP tools still return bulk (`read_file`,
`get_file_text_by_path`, `build_project`, large SQL results) — extend the
PreToolUse guard to match `mcp__*` read tools and prefer bounded variants
(`preview_table_data` over full dumps). (b) A big MCP server taxes every prompt
with its tool schemas — manage it with deferred/lazy tool loading (this session
loads `mcp__idea__*` on demand) so the baseline doesn't balloon.

```text
Research the context COSTS of MCP servers (using the IntelliJ IDEA / JetBrains
MCP as the case study) and how to guard them, in Claude Code.
Answer:
1. Tool-definition overhead: how much context do a server's tool schemas consume,
   and how does Claude Code's deferred/lazy tool loading (loading MCP tool
   schemas on demand vs. all upfront) reduce the baseline? How to enable/verify.
2. Which IDE/MCP tools can still flood context (read_file, get_file_text_by_path,
   build_project, execute_terminal_command, large SQL result sets) vs. which are
   inherently bounded (preview_table_data, symbol search, get_file_problems).
3. Can a PreToolUse hook match and gate MCP tools (matcher patterns like
   "mcp__idea__read_file" or "mcp__.*")? Confirm the matcher syntax and design a
   guard that extends a read-size limit to MCP read tools and large query results.
4. Net trade-off: at what point does a heavy MCP server cost more baseline
   context than it saves per task, and how to decide.
Deliverable: a guard + budgeting design for the MCP tool surface, with the
matcher/deferred-loading specifics verified. Follow the shared constraints.
```

---

## Suggested order to research
1. **Master prompt** — map the territory.
2. **#11 (semantic access / LSP & IDE-MCP)** — likely the biggest single win for
   selective reading, and you already have the IntelliJ MCP wired.
3. **#4 (offloading)** and **#5 (caching)** — your other two unbuilt levers.
4. **#9 (A/B proof)** — upgrade "proof it works" from unit test to empirical.
5. **#12 / #13** — diagnostics-over-dumps and guarding the MCP surface pair
   naturally with #11.
6. Then **#10** to package everything; #2/#3/#6/#8 as needed; #7 last.
