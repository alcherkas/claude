# PreToolUse as a context safeguard: the input-gate contract and a hardened read-size guard

> **Source:** deep-research run for the PreToolUse input-gate prompt in
> [`../research-prompts.md`](../research-prompts.md). Search angles: (a) the
> exact PreToolUse stdin/stdout contract, (b) cheap in-hook token estimation,
> (c) existing open-source context guards, (d) deny-vs-ask + fail-open design,
> (e) heuristic failure modes. Sources fetched: code.claude.com `/hooks` &
> `/agent-sdk/hooks`, platform.claude.com `/agent-sdk/hooks`,
> `/build-with-claude/token-counting`; anthropics/claude-code issues #6910,
> #41791, #52822, #24327, #39344; github.com/hansipie/ecotokens. 19 claims
> survived 3-vote adversarial verification (6 refuted, listed below). Current as
> of **mid-2026** (Claude Code v2.1.x, Opus 4.8 / Sonnet 4.6 / Haiku 4.5, 1M
> context). Cross-checked live against the raw `/hooks` page on 2026-06-23.

**Bottom line.** The PreToolUse contract is small, stable, and fully documented:
a JSON object on **stdin** (`session_id`, `transcript_path`, `cwd`,
`permission_mode`, `hook_event_name`, `tool_name`, `tool_input`; `agent_id` /
`agent_type` inside subagents), and a decision returned via a
`hookSpecificOutput` object on **stdout with exit 0**, whose `permissionDecision`
takes **four** values — `allow | deny | ask | defer` (`defer` *does* exist, added
in v2.1.89, but only functions in headless/`-p`/SDK contexts). The older
`decision: "approve"|"block"` form is **not** documented for PreToolUse and
carries no deprecation note — use `permissionDecision`. For an in-hook size
guard, **never call the network**: a blocking hook adds latency to every Read, so
estimate tokens locally with an **undercount-biased `bytes / 4`** heuristic and
**fail open** on every error. The existing
[`guard_context.py`](../hooks/guard_context.py) gets the load-bearing decisions
right (deny-shape, fail-open, `bytes//4`, selective-read & limiter-pipeline
passthrough); the gaps are all *coverage* (no MCP-tool matching, no Bash
redirect/`xargs`/heredoc handling, byte-size ≠ post-decode tokens for binary/PDF
reads) rather than correctness.

---

## (a) The verified PreToolUse contract

### stdin — the JSON delivered to the hook command

| Field | Always present? | Meaning | Source |
|---|---|---|---|
| `session_id` | yes | Current session identifier | `/hooks` (primary) |
| `transcript_path` | yes | Path to the conversation JSONL | `/hooks` (primary) |
| `cwd` | yes | Current working directory | `/hooks` (primary) |
| `permission_mode` | yes (PreToolUse) | `"default" \| "plan" \| "acceptEdits" \| "auto" \| "dontAsk" \| "bypassPermissions"` | `/hooks` (primary) |
| `hook_event_name` | yes | `"PreToolUse"` | `/hooks` (primary) |
| `tool_name` | yes | Name of the tool (`Read`, `Bash`, `mcp__server__tool`, …) | `/hooks` (primary) |
| `tool_input` | yes | Object of the tool's input args (e.g. `{"file_path": …}`, `{"command": …}`) | `/hooks` (primary) |
| `agent_id`, `agent_type` | only inside a subagent / `--agent` | Subagent identity (Python: on PreToolUse/PostToolUse/PostToolUseFailure only) | `/agent-sdk/hooks` (primary) |

Note: `permission_mode` reports **six** runtime values including `"auto"`, but
the user-settable `defaultMode` setting only exposes five (no `"auto"`) — `auto`
is a runtime-only mode. A guard hook should **not** branch on `permission_mode`
for a size check (it must guard equally in every mode), but it is available.

### stdout — the decision (`hookSpecificOutput`)

```json
{
  "hookSpecificOutput": {
    "hookEventName": "PreToolUse",
    "permissionDecision": "deny",
    "permissionDecisionReason": "…why, written for the model…"
  }
}
```

| `permissionDecision` | Effect | Notes | Source |
|---|---|---|---|
| `allow` | Bypasses the permission system, runs the tool | Equivalent to emitting nothing + exit 0 | `/hooks`, issue #52822 |
| `deny` | **Stops** the tool call; `permissionDecisionReason` is fed to the model | The mechanism a size-guard uses | `/agent-sdk/hooks`, `/hooks` |
| `ask` | Escalates to a human permission prompt | For "maybe, ask the user" cases | `/hooks` |
| `defer` | **Ends the query** so it can be resumed later (`claude -p --resume`) | Added v2.1.89; headless/SDK only — ignored with a warning in interactive sessions; `updatedInput` ignored under defer | `/agent-sdk/hooks`, issue #41791 |

- **`permissionDecisionReason` goes to the MODEL**, so it self-corrects ("tells
  the model why, so it avoids retrying"). **`systemMessage`** (top-level field)
  goes to the **USER**, not the model. **`additionalContext`** is wrapped in a
  system-reminder and read by the model on its next request.
- **Input rewrite:** `updatedInput` requires `permissionDecision: "allow"` (auto-
  approve the rewrite) or `"ask"`; it is **ignored under `defer`**.
- **Return `{}`** (or nothing) to allow the operation unchanged.
- **Multi-hook precedence:** `deny > defer > ask > allow`. Any single hook
  returning `deny` blocks the call regardless of the others; completion order is
  non-deterministic, so each hook must act independently.

### `decision: approve/block` vs `permissionDecision`

The live `/hooks` page documents **only** the `hookSpecificOutput` /
`permissionDecision` form for PreToolUse, with **no** deprecation or backward-
compatibility notice tying PreToolUse to a legacy `approve`/`block` form. The
older `decision: "block"` shape is associated with other events, not the
PreToolUse permission contract. **Recommendation: use `permissionDecision`
exclusively** (as `guard_context.py` does) and do not rely on `approve`/`block`
for PreToolUse. *(This is the disposition the adversarial pass reached — the
claim asserting `decision` applies to PreToolUse was refuted 1–2.)*

### Exit-code semantics

| Exit | stdout | stderr | Effect on the tool call |
|---|---|---|---|
| **0** | **Parsed as JSON** (decision control) | debug log | Decision applied; empty stdout ⇒ normal permission flow |
| **2** | **Ignored** | **Fed to Claude as an error** | **Blocked** (PreToolUse) |
| other | (not parsed) | shown / debug log | Non-blocking error; execution continues |

So there are **two** ways to block a Read: emit the `deny` JSON on stdout +
exit 0 (preferred — lets you write a structured `permissionDecisionReason`), or
exit 2 with the reason on stderr. The JSON form is strictly richer; use it.

### Matcher syntax (registering on `Read|Bash` and MCP tools)

Matchers filter **by tool name only**, never by file path or args (check
`tool_input.file_path` inside the hook for that).

| Matcher | Interpreted as | Example |
|---|---|---|
| `*`, `""`, or omitted | **match all tools** | fires on every tool |
| only letters/digits/`_`/`\|` | **exact string**, `\|` = alternation | `Read\|Bash` matches exactly Read or Bash |
| contains any other char | **JavaScript regex** | `^mcp__` matches every MCP tool |

- **Case-sensitive.** `Bash` matches, `bash` does not; `Edit|Write|multiEdit`
  does not match `MultiEdit`.
- **MCP gotcha:** a bare `mcp__memory` is an *exact string* and matches **no
  tool**. To match all of a server's tools you must append `.*` →
  `mcp__memory__.*` (or use a regex like `^mcp__` / `mcp__.*__read.*`).

---

## (b) Cheap, reliable in-hook token estimation

A PreToolUse hook **blocks the tool call while it runs**, so estimator latency is
paid on every guarded Read/Bash. That single constraint settles the trade-off.

| Method | Accuracy vs Claude's real tokenizer | Latency | Offline? | Failure modes |
|---|---|---|---|---|
| **`bytes / 4`** (used by the guard) | Rough; deliberately biased to **undercount** | ~0 (a `stat` call) | **Yes** | Wrong for binary/multibyte/PDF; on-disk bytes ≠ post-decode tokens |
| **`/v1/messages/count_tokens` API** | Closest, but still an **estimate** by Anthropic's own statement; may include unbilled system tokens | **Network RTT (tens–hundreds of ms), per call** | **No** | Network/auth failure, rate limits (Tier 1 = 100 RPM), needs an API key, leaks file content off-box |
| **Local tokenizer (tiktoken / Anthropic tokenizer)** | Good for the model it ships for; not guaranteed to match the *current* server tokenizer | ms-to-tens-of-ms + import cost | Yes | Extra dependency; version drift vs server tokenizer; must read full file into memory to count |

**Conclusion: for a blocking guard, use `bytes / 4` and never touch the
network.** The `count_tokens` endpoint (`POST
https://api.anthropic.com/v1/messages/count_tokens`, returns `{"input_tokens":
N}`) is excellent for *offline budgeting / CI*, but it is the wrong tool inside a
latency-sensitive PreToolUse path: it is itself only an estimate, it adds a
network round-trip to every read, it can rate-limit, and it ships file contents
off the machine.

**Why `/ 4` and why undercount-bias is the safe default for a *blocking* guard.**
Anthropic's tokenizer averages roughly **3.5–4 chars/token**, but the real ratio
varies by content:

- **English prose** ≈ 4 chars/token (close to the heuristic).
- **Source code** is often **denser** (more punctuation/short tokens) → ~3–3.5
  chars/token → *more* tokens per byte than `/4` predicts.
- **Minified JS / JSON / lockfiles** are denser still and frequently *fewer*
  chars/token.

Because real code/JSON usually yields **more** tokens than `bytes/4`, dividing by
4 systematically **undercounts**. For a *deny* guard that is the **safe**
direction: a guard that undercounts is *conservative about blocking* — it errs
toward **allowing** borderline reads rather than falsely denying a read the model
legitimately needs. (If you instead overcounted, you'd block reads that actually
fit, which is the more damaging false positive for a monitoring safeguard.) The
flip side — many genuinely huge code/JSON files will tokenize **above** the
budget the guard thinks they're at — is acceptable because the budget
(`25_000`) already sits far below the window; an undercount near the threshold
still trips on anything truly oversized.

---

## (c) Existing open-source context guards

| Project | What it does | What it gets right | What to learn from / watch |
|---|---|---|---|
| **`guard_context.py`** (this repo) | PreToolUse deny-guard on `Read`/`Bash` | Fail-open; `bytes//4`; denies unbounded oversized Read & `cat`-family Bash dumps; passes selective reads (offset/limit) and limiter-terminated pipelines (`… \| head/grep/wc`); self-correcting deny reason | Coverage gaps below in (e); does not match MCP tools |
| **[hansipie/ecotokens](https://github.com/hansipie/ecotokens)** | PreToolUse hook that **compresses/transforms Bash output** (not a deny-guard) | Demonstrates the *transform/`updatedInput`* lever rather than blocking; tracks token/USD savings in a TUI | Different model entirely — **rewrite-not-block**. The adversarial pass **refuted** the claim that ecotokens is fail-open, so do **not** cite it as a fail-open exemplar |

The notable contrast: **deny-style guards** (this repo) *block* an oversized read
and push the agent to a cheaper move; **transform-style hooks** (ecotokens)
*rewrite* the output so the bulk text never reaches the model. They are
complementary layers, not competitors. No third-party deny-style read-size guard
surfaced that materially improves on the design here; the main differentiators in
the wild are (1) byte-vs-token estimation and (2) whether Bash coverage extends
beyond `cat`.

---

## (d) Deny-vs-ask UX and fail-open safety

**When `deny` is right vs `ask`.** A size guard is a *deterministic, mechanical*
rule ("this single call would exceed the budget"), and the agent has a strictly
better alternative (selective read / subagent offload). That is the textbook
`deny` case: blocking with a reason that teaches the fix is faster and cheaper
than interrupting a human. Reserve **`ask`** for judgment calls where a human,
not the model, should decide (destructive ops, ambiguous intent). A monitoring
size guard should almost never `ask` — it should `deny` with a corrective reason
or get out of the way.

**Where the reason is surfaced.** `permissionDecisionReason` is delivered to the
**model** (so it can self-correct), whereas `systemMessage` is shown to the
**user**. A self-correcting guard therefore puts its actionable advice in
`permissionDecisionReason` (exactly what `guard_context.py.advice()` does), not in
`systemMessage`.

**Write the reason so the agent self-corrects, not retries.** The deny text must
(1) name the cost, (2) forbid the naive retry, and (3) enumerate concrete cheaper
moves — selective `offset/limit` read, `grep -n` for the symbol, or delegate the
full read to a Task/Explore subagent so the bulk text stays out of the main
context. The current `advice()` does all three.

**Why fail open.** This is a *monitoring* safeguard, not a security control. A
fail-**closed** guard that errors on a parse failure, a missing file, an
unexpected `tool_input` shape, or its own bug would **brick the session** —
turning a convenience layer into an outage. Fail-**open** (allow on any error)
caps the worst case at "the guard did nothing," which is exactly the pre-guard
status quo. `guard_context.py` is fail-open at every layer: bad JSON → allow,
missing file → allow, unparseable shell → allow, and a top-level `except
Exception: allow()` so no bug can block a call. The accepted cost is occasional
**false negatives** (an oversized read slips through), which is strictly safer
than false-positive lockups.

---

## (e) Failure modes of a byte/heuristic guard

These are the boundaries of any size-based PreToolUse guard, with current status
for `guard_context.py`:

1. **Long single lines defeat *line*-based limits.** Minified JS / one-line JSON
   can be one 2 MB line. A guard keyed on **line count** would wave it through.
   → **`guard_context.py` is robust here**: it gates on **byte size**, not line
   count, so a single huge line still trips the budget.
2. **`tool_input.limit` is a *line* count, not a byte/token bound.** A selective
   read with `limit: 100` of a file whose lines are each 50 KB is still enormous.
   → **Gap:** the guard treats any `limit ≤ 2000` as "selective" and allows it
   regardless of line width. Acceptable in practice (most files aren't 50 KB/line)
   but a known hole.
3. **The Read tool's own 2000-line default is *not* reliably enforced.** Issue
   [#6910](https://github.com/anthropics/claude-code/issues/6910) shows a
   20,010-line file tokenizing the **whole** file (59,038 tokens) and hitting the
   hard 25,000-token Read ceiling rather than capping at 2000 lines — the token
   guard fires on full content **before** the line truncation applies. So you
   **cannot** assume "no limit ⇒ only 2000 lines get read." → This *justifies* the
   guard's choice to deny unbounded oversized reads outright. *(Closed
   not-planned, so behavior is version-sensitive.)*
4. **Binary / PDF / image reads: on-disk bytes ≠ post-decode tokens.** The Read
   tool ingests PDFs and images specially (a 1 MB PDF is not 250k text tokens; an
   image is tokenized by dimensions). `bytes/4` is meaningless for these. → **Gap:**
   the guard applies `bytes/4` uniformly; it may falsely deny a large-but-cheap
   binary or under-estimate a dense PDF. A hardened version should skip or
   special-case known binary extensions.
5. **Cumulative growth is invisible.** The guard inspects **one call at a time**;
   fifty 10 KB reads each pass but together flood the window. PreToolUse has no
   running total. → **Inherent limitation** of a per-call gate (would need
   transcript-size tracking via `transcript_path`).
6. **Bash coverage is narrower than the shell.** Only `cat/bat/nl/less/more` are
   treated as dumps; **`grep -r` with no filter, `find … -exec cat`, `xargs cat`,
   heredocs, `python -c "print(open(f).read())"`, and output redirected from a
   subshell** can all dump a large file without a recognized dump command. A
   pipeline *ending* in a limiter is allowed, but a limiter in the **middle**
   followed by an unbounded tail is mis-scored. → **Gap / inherent:** shell
   parsing is unbounded; `shlex` + a command allowlist is a pragmatic 80% solution
   that fails open on the rest.
7. **Symlinks / missing files / `os.path.isfile`.** The guard uses
   `os.path.isfile` (follows symlinks; false for dirs, sockets, `/dev/*`) and
   fails open when the path doesn't resolve — correct and safe.
8. **MCP and non-Read/Bash tools are unguarded.** The matcher is `Read|Bash`
   only; an MCP file-reader (`mcp__fs__read_file`) bypasses the guard entirely.
   → **Gap:** add `mcp__.*read.*`-style matchers (regex) if MCP readers are in use.

---

## Hardened reference design (and how to PROVE it)

**Design invariants** (all satisfied by `guard_context.py`, retained):

1. **Fail open everywhere.** Bad JSON, missing file, unparseable shell, or any
   exception ⇒ `allow()`. Top-level `except Exception: allow()`.
2. **Local estimation only.** `est_tokens(bytes) = bytes // 4`. No network in the
   hot path. Undercount-biased on purpose.
3. **Block via `permissionDecision: "deny"` + JSON on stdout + exit 0**, with a
   model-facing `permissionDecisionReason` that names the cost and the cheaper
   moves. Allow via empty stdout + exit 0.
4. **Pass selective work through.** Read with bounded `limit`; Bash pipeline
   ending in a limiter (`head/tail/grep/rg/wc/…`).
5. **Configurable budget** via `CC_CONTEXT_GUARD_MAX_TOKENS` (default 25_000).

**Recommended hardening deltas** (coverage, not correctness):

- **Skip/whitelist binary & PDF extensions** before `bytes/4` (fixes failure
  mode #4); optionally cap by `limit × est-line-width` for very wide files (#2).
- **Add MCP-reader matchers** (`mcp__.*read.*` / `^mcp__` as appropriate) if MCP
  file tools are configured (#8) — remembering the regex requirement.
- **Broaden Bash dump detection** to `xargs`/`-exec cat`/redirected reads, while
  keeping the **fail-open default** for anything unparsed (#6).
- Leave cumulative-growth (#5) to the layered defense in
  [`00-master-context-overflow.md`](./00-master-context-overflow.md)
  (compaction/offload) — it is out of scope for a per-call gate.

**Proof method — invoke the hook exactly as Claude Code does.** The fidelity
claim is the whole point: the
[`test/test_guard.py`](../test/test_guard.py) suite runs the hook **as a
subprocess with the tool-call JSON piped to stdin**, asserts the process **always
exits 0** (fail-open invariant), and reads the decision back off **stdout** as
`hookSpecificOutput.permissionDecision` — byte-for-byte the protocol Claude Code
uses. Each case maps to a contract clause:

- big unbounded Read ⇒ `deny`; small Read ⇒ allow (None).
- big Read with `limit:100` ⇒ allow (selective-read passthrough).
- missing file ⇒ allow (fail-open).
- `cat BIG` ⇒ `deny`; `cat SMALL` ⇒ allow; `cat BIG | head -n 20` ⇒ allow
  (limiter-terminated pipeline); `grep -n needle BIG` ⇒ allow.
- `CC_CONTEXT_GUARD_MAX_TOKENS` lowers the budget and flips a small read to
  `deny` (config proof).
- garbage stdin ⇒ exit 0, empty stdout (fail-open proof).
- non-Read/Bash tool (`Write`) ⇒ allow (matcher/scope proof).

Because the harness reproduces the stdin/stdout/exit-code contract verified in
(a), a green suite is direct evidence the live hook behaves identically. To extend
the proof to the hardening deltas, add cases for: a `.pdf`/binary file (should
not be `bytes/4`-denied), an MCP-reader `tool_name` (if matchers added), and a
`find … -exec cat` Bash command.

---

## Cross-check verdict on `guard_context.py`

**Correct against the verified contract:**

- Reads `tool_name` / `tool_input` from stdin JSON — exactly the two PreToolUse-
  specific fields. ✔
- Deny shape (`hookSpecificOutput` + `hookEventName:"PreToolUse"` +
  `permissionDecision:"deny"` + `permissionDecisionReason`) on stdout + exit 0
  matches the documented schema and issue #52822's accepted payload. ✔
- Allow = empty stdout + exit 0 (normal permission flow). ✔
- `permissionDecisionReason` carries model-facing, self-correcting advice (cost +
  three cheaper moves) — the right field for the model audience. ✔
- Fail-open at every layer incl. top-level `except`. ✔
- `bytes//4` local estimation, undercount-biased — correct for a blocking guard. ✔
- Byte-size gate (not line-count) survives the minified-one-liner failure mode. ✔

**Gaps vs the contract / robustness (none are correctness bugs):**

- **No MCP coverage** — matcher is `Read|Bash`; MCP file-readers bypass it. ✦
- **Binary/PDF reads** mis-estimated by `bytes/4` (on-disk bytes ≠ post-decode
  tokens). ✦
- **Bash dump detection** misses `xargs`/`-exec cat`/redirects/heredocs/`python
  -c`. ✦
- **`limit ≤ 2000` allowed unconditionally** ignores very wide lines. ✦
- **No cumulative-growth awareness** (inherent to a per-call gate). ✦
- Does not read or use `permission_mode` — fine for a size guard, but worth a
  comment that the guard intentionally fires in all modes. ✦

---

## Version-sensitivity & open questions

- **`defer` is real but context-dependent.** Documented value (v2.1.89+,
  confirmed live), but operative only in headless/`-p`/Agent-SDK flows; interactive
  sessions log a warning and ignore it. Irrelevant to a fail-open size guard
  (which only ever emits allow/deny) but must be in the spec table.
- **`permission_mode` enum mismatch.** Runtime reports six values (incl. `auto`);
  `defaultMode` setting exposes five (no `auto`). Version-sensitive.
- **Read 2000-line default not enforced** (#6910, closed not-planned) — could
  change in a future release; the guard's deny-unbounded stance is the safe hedge.
- **`permissionDecision:"allow"` prompt-suppression had a regression** in
  v2.1.119 (#52822): the schema parsed but didn't always suppress the native
  prompt. Schema is correct; runtime allow-suppression is version-sensitive. (Not
  an issue for this guard, which never emits `allow` JSON — it allows by silence.)
- **`ask` once bypassed settings `deny` rules** (#39344, closed bug) — a past
  deviation from the documented `deny > defer > ask > allow` precedence, not the
  current design.
- **Open questions:** (1) the exact post-decode token cost of PDF/image Read
  ingestion (no public formula) — needed to size failure mode #4 precisely; (2)
  whether any MCP file-reader tools in a given install warrant `^mcp__` matchers;
  (3) whether a cheap *cumulative* transcript-size signal could be derived from
  `transcript_path` without re-reading the whole JSONL each call; (4) whether the
  `limit` line-count vs byte-width hole is worth closing given real-world file
  shapes.

---

## Sources

**Primary (Anthropic docs):**
[code.claude.com `/hooks`](https://code.claude.com/docs/en/hooks) (input fields,
exit codes, matcher table, `permissionDecision` enum, `systemMessage` vs
`additionalContext`);
[code.claude.com `/agent-sdk/hooks`](https://code.claude.com/docs/en/agent-sdk/hooks)
(four-value `permissionDecision`, `defer` semantics, `deny>defer>ask>allow`
precedence, `updatedInput` rules, `agent_id`/`agent_type`);
[platform.claude.com `/agent-sdk/hooks`](https://platform.claude.com/docs/en/agent-sdk/hooks)
(redirect target; same contract);
[platform.claude.com token-counting](https://platform.claude.com/docs/en/build-with-claude/token-counting)
(`POST /v1/messages/count_tokens`, `{"input_tokens":N}`, estimate caveat, free +
RPM tiers).

**Forum (anthropics/claude-code):**
[#6910](https://github.com/anthropics/claude-code/issues/6910) (Read 2000-line
default not enforced);
[#41791](https://github.com/anthropics/claude-code/issues/41791) (`defer` docs /
`--resume`);
[#52822](https://github.com/anthropics/claude-code/issues/52822) (allow schema
accepted; v2.1.119 prompt-suppression regression);
[#24327](https://github.com/anthropics/claude-code/issues/24327) (exit-2 / stderr
mechanics);
[#39344](https://github.com/anthropics/claude-code/issues/39344) (`ask`
vs settings `deny`, closed bug).

**Community / OSS:** [hansipie/ecotokens](https://github.com/hansipie/ecotokens)
(transform-style PreToolUse Bash-output compressor).

**Refuted in adversarial pass (do not repeat):** that `decision:
approve/block` applies to PreToolUse (1–2); that PreToolUse stdin includes
`tool_result` (0–3); that the condensed SKILL.md schema (no
`permissionDecisionReason`, no `defer`) is the canonical contract (0–3); that
exit-0 stdout is shown in the transcript for PreToolUse (0–3); that `allow` is
documented to "bypass permission system" verbatim on the hooks page (1–2); that
ecotokens is fail-open (0–3).
