# Context-overflow guard for Claude Code

A working **safeguard** that stops a Claude Code session from blowing past its
context window — plus an automated test that **proves it works**.

The single biggest cause of context overflow in agentic coding is one
oversized pull: `Read`-ing a 5 000-line file, `cat`-ing a generated bundle, or
dumping a log. One such call can swallow tens of thousands of tokens and crowd
out the working state. This guard catches that *before* it happens.

---

## How it works

Claude Code fires a [`PreToolUse` hook](https://code.claude.com/docs/en/hooks)
before every `Read` and `Bash` call. The hook
([`hooks/guard_context.py`](hooks/guard_context.py)) receives the tool call as
JSON on stdin, estimates how many tokens it would dump into the conversation,
and decides:

| Situation | Decision |
|---|---|
| `Read` a file estimated over the token budget, no `limit` | **deny** + advice |
| `Read` with an explicit `limit ≤ 2000` (selective) | allow |
| `Bash` `cat`/`bat`/`nl`/`less`/`more` of an oversized file | **deny** + advice |
| `Bash` pipeline ending in `head`/`grep`/`wc`/... (bounded output) | allow |
| anything under budget, missing file, other tools | allow |

A **deny** returns the documented control payload (exit 0):

```json
{"hookSpecificOutput": {"hookEventName": "PreToolUse",
  "permissionDecision": "deny",
  "permissionDecisionReason": "Reading … would pull ~50,000 tokens …"}}
```

The reason string is fed back to the model, so the agent doesn't just get
stopped — it gets *redirected* to a cheaper move (read a slice, grep, or hand
the file to a subagent).

**Fail-open by design.** Unparseable input, a missing file, or any internal
exception → *allow*. A safeguard that bricks the session is worse than the
problem, so the guard only ever blocks when it is confident.

Budget is configurable: `CC_CONTEXT_GUARD_MAX_TOKENS` (default `25000`).

---

## Proof that it works

```bash
./run_demo.sh
```

Output (abridged) — the live deny payload followed by the suite:

```
 LIVE: oversized read  ->  hook output
 { "permissionDecision": "deny", "permissionDecisionReason": "Reading … ~50,000 tokens …" }

 AUTOMATED TEST SUITE
 ... Ran 11 tests in 0.25s — OK
```

[`test/test_guard.py`](test/test_guard.py) invokes the hook **exactly the way
Claude Code does** — as a subprocess with JSON on stdin — so a green suite means
the real hook behaves identically in a live session. It covers: big read denied,
small read allowed, bounded read allowed, `cat big` denied, `cat big | head`
allowed, `grep` allowed, configurable threshold, fail-open on garbage, and other
tools ignored.

Run just the tests: `python3 -m unittest discover -s test -v`

**Bad examples / anti-patterns.** [`examples/`](examples/) holds a couple of
"wrong way" moves (reading a generated bundle whole; `cat`-ing a 2 MB log) each
contrasted with the fix, and asserted against the guard:
`python3 examples/show_bad_vs_good.py`.

**Where this goes next.** [`research-prompts.md`](research-prompts.md) is the
iteration scratchpad: 13 candidate safeguards beyond this hook (subagent
offloading, prompt-caching proof, A/B thrash-test, semantic access via LSP /
IntelliJ MCP, diagnostics-over-dumps, defense-in-depth, …), each with a ready-to-
run deep-research prompt, plus a master prompt on context-overflow guardrails.

---

## Install it in your own project

1. Copy `hooks/guard_context.py` into your repo.
2. Merge [`.claude/settings.json`](.claude/settings.json) into your project (or
   user) settings — it registers the hook for `Read|Bash`:

   ```json
   { "hooks": { "PreToolUse": [ { "matcher": "Read|Bash",
     "hooks": [ { "type": "command",
       "command": "python3 \"$CLAUDE_PROJECT_DIR/hooks/guard_context.py\"" } ] } ] } }
   ```
3. Start Claude Code; `/hooks` should list it. Optionally set a budget:
   `export CC_CONTEXT_GUARD_MAX_TOKENS=20000`.

---

## Efficiently passing context

The guard *blocks* the cheap mistake. These three techniques are how you *keep*
context lean — and the deny message points the agent straight at them.

### 1. Selective reading (search over read)
The principle the guard enforces: **never load what you can locate.**
- Read line ranges, not whole files: `Read(file, offset, limit)`.
- `grep -n` / `rg` for the symbol, then read only the matching window.
- Prefer the `Explore` agent to *find* code; it returns locations, not dumps.
- Default Claude Code `Read` already caps at 2000 lines — don't override that
  with a giant `limit` on a long-line file.

### 2. Subagent offloading
Heavy reading should happen in **someone else's context.** Spawn a subagent
(`Task` / `Explore` / a `Workflow` agent) to read the bulky material; its
context absorbs the tokens and only the **conclusion** returns to the main
thread. A 40k-token file survey becomes a 300-token summary in your window.
Fan out independent investigations in parallel — each isolates its own dump.
This is the most powerful lever: it changes *where* tokens live, not just how
many.

### 3. Prompt caching
For tokens you genuinely must keep resending (a large system prompt, a spec, a
schema), mark a stable prefix with `cache_control` so it is **read from cache**
instead of reprocessed. It still occupies the window, but you stop paying to
re-encode it every turn and latency drops. Claude Code reuses cached prefixes
automatically across a session; in your own Agent SDK / API apps, place the
unchanging bulk first and cache it, with the volatile turn-by-turn content last.

> Order of impact for staying under the limit:
> **selective reading** (don't pull it) → **subagent offloading** (pull it
> elsewhere) → **prompt caching** (cheapen what must stay) → **compaction**
> (last-resort cleanup of history). This guard automates the first and nudges
> toward the second.

---

## Limitations (honest)

- Token count is a byte/4 **estimate**, not the real tokenizer — deliberately
  conservative so it under-blocks rather than over-blocks.
- The `Bash` heuristic covers the common `cat`-family dumps and pipe-to-limiter
  cases, not every shell construct (process substitution, `xargs`, etc.). When
  unsure it allows.
- It guards single oversized calls; it does not track cumulative growth across
  many small reads. Pair it with Claude Code's `/compact` (or auto-compaction)
  for the slow-drip case.
