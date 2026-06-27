# Live context-budget statusline / meter

> **Source:** deep-research run for **Option #8** in
> [`../research-prompts.md`](../research-prompts.md). 5 search angles, 19+ sources
> fetched, 25 claims extracted, 17 adversarially verified (17 confirmed across the
> statusline/transcript/model surface, 8 killed → 6 synthesized findings), plus
> **two firsthand empirical proofs** run against a real local transcript and the
> version-matched bundled `claude-api` skill. Current as of **mid-2026** (Claude
> Code **v2.1.186** verified on this machine; Opus 4.8 / Sonnet 4.6, 200k default /
> 1M extended context).

**Bottom line.** A live context meter is the *cheapest, most reliable* of the
eight options because **you do not have to compute the percentage yourself** —
Claude Code now ships the number to your statusline script. The statusline stdin
JSON carries a fully-populated `context_window` block with a **pre-calculated
`used_percentage`**, the raw `current_usage` token breakdown, and
`context_window_size` (200000 or 1000000). The statusline **runs locally and
consumes no API tokens** ([statusline doc][1]), so a meter is pure observation
with zero overflow cost — the exact opposite of Option #1's PreToolUse gate,
which blocks. The headline arithmetic, quoted verbatim from the doc:

> "The `used_percentage` field is calculated from input tokens only:
> `input_tokens + cache_creation_input_tokens + cache_read_input_tokens`. It does
> not include `output_tokens`. If you calculate context percentage manually from
> `current_usage`, use the same input-only formula to match `used_percentage`." [1]

The single most important *conceptual* finding for anyone parsing the transcript
instead: **current context size is the LAST assistant message's input-side usage,
NOT a running sum of every turn.** I proved this on a real transcript — the naive
running-sum overcounts by **39×** (1,551,269 vs. the true 39,518). See
[the proof](#proof-that-the-meter-is-accurate--firsthand-empirical).

Two version-sensitive landmines to respect: (1) **`used_percentage`/`current_usage`
are `null`** before the first API call and again right after `/compact` — your
script must fall back gracefully; (2) the `total_input_tokens`/`total_output_tokens`
meaning **changed in v2.1.132** (cumulative → current). Both are documented.

---

## ⚠️ Refuted claims — do NOT repeat (killed 1–3 in verification)

- **"The statusline `context_window` block holds CUMULATIVE session totals that
  can exceed the window (e.g. 330k/200k = 169%)."** **REFUTED (0-3).** This was a
  pre-v2.1.132 bug ([issue #13783](https://github.com/anthropics/claude-code/issues/13783)).
  As of v2.1.132 the doc is explicit:
  "`total_input_tokens` and `total_output_tokens` reflect current context usage,
  not cumulative session totals." [1] Do not design around the old behavior.
- **"Auto-compaction does NOT adjust the statusline counters, so the value diverges
  from `/context` permanently."** **REFUTED (0-3).** Same root cause as above; fixed
  in v2.1.132. `current_usage` is now explicitly `null`ed after `/compact` and
  repopulated from the next API response. [1]
- **"Auto-compaction triggers at ~83% of the window (~167k), reserving ~33k."**
  **REFUTED (1-2) — treat as SPECULATION.** No Anthropic doc states a percentage.
  The official text is only directional: Claude Code "manages context automatically
  as you approach the limit. It clears older tool outputs first, then summarizes the
  conversation if needed." ([how-it-works][6]) Pick your own warning thresholds;
  do **not** cite 83% as documented behavior.
- **"`count_tokens` can PROVE a live statusline number by replaying the
  transcript."** **REFUTED (0-3).** `count_tokens` returns only `input_tokens` for
  a *constructed* message; it cannot reconstruct Claude Code's exact system prompt,
  tool set, cache state, or post-compaction history, so it is not a faithful oracle
  for the *live* context number. (It remains the right oracle for Option #2's
  *pre-read file estimate* — a different quantity.) Prove the meter with the
  **transcript cross-check** below instead.
- **"The `statusLine` config and changes-take-effect-after-restart description"**
  was killed (0-3) only because the cited blog got the reload detail wrong: the doc
  says "Settings reload automatically, but changes won't appear until your next
  interaction with Claude Code." [1] The config object shape itself is correct and
  is given below from the primary doc.
- **"Context grows linearly/cumulatively, preserving every prior turn in full as
  the window fills."** **REFUTED (0-3)** as a basis for summing turns — see the
  39× over-count proof; cache-read dominates and only the latest turn's input-side
  total is the live size.

---

## (a) The statusline API — what the script receives on stdin — **DOCUMENTED**

Claude Code "runs your script and pipes JSON session data to it via stdin. Your
script reads the JSON, extracts what it needs, and prints text to stdout." [1] The
full schema (from the doc's "Full JSON schema" accordion, current example
`version: 2.1.90`) — the fields that matter for a context meter are **bolded**:

```jsonc
{
  "cwd": "/current/working/directory",
  "session_id": "abc123...",
  "session_name": "my-session",                 // absent unless --name/-rename set
  "transcript_path": "/path/to/transcript.jsonl",
  "model": { "id": "claude-opus-4-8", "display_name": "Opus" },   // ← model id + name
  "workspace": {
    "current_dir": "...", "project_dir": "...",
    "added_dirs": [], "git_worktree": "feature-xyz",
    "repo": { "host": "github.com", "owner": "anthropics", "name": "claude-code" }
  },
  "version": "2.1.90",
  "output_style": { "name": "default" },
  "cost": {                                       // ← session cost/time, client-side estimate
    "total_cost_usd": 0.01234, "total_duration_ms": 45000,
    "total_api_duration_ms": 2300,
    "total_lines_added": 156, "total_lines_removed": 23
  },
  "context_window": {                             // ← THE meter source
    "total_input_tokens": 15500,
    "total_output_tokens": 1200,
    "context_window_size": 200000,                //   200000 | 1000000
    "used_percentage": 8,                         //   PRE-CALCULATED, input-only
    "remaining_percentage": 92,
    "current_usage": {                            //   raw breakdown (may be null)
      "input_tokens": 8500, "output_tokens": 1200,
      "cache_creation_input_tokens": 5000, "cache_read_input_tokens": 2000
    }
  },
  "exceeds_200k_tokens": false,                   // ← FIXED 200k threshold, not window %
  "effort": { "level": "high" },                  // absent if model lacks effort param
  "thinking": { "enabled": true },
  "rate_limits": {                                // Pro/Max only, after 1st API response
    "five_hour": { "used_percentage": 23.5, "resets_at": 1738425600 },
    "seven_day": { "used_percentage": 41.2, "resets_at": 1738857600 }
  },
  "vim": { "mode": "NORMAL" },
  "agent": { "name": "security-reviewer" },
  "pr": { "number": 1234, "url": "...", "review_state": "pending" },
  "worktree": { "name": "...", "path": "...", "branch": "...",
                "original_cwd": "...", "original_branch": "main" }
}
```

**Answering the prompt's checklist directly** [1]:

| The prompt asked… | Present? | Field |
|---|---|---|
| token / context usage block | **YES** | `context_window.*` (and a pre-computed `used_percentage`) |
| `transcript_path` | **YES** | `transcript_path` (`/path/to/transcript.jsonl`) |
| model id | **YES** | `model.id` (`claude-opus-4-8`) |
| model display name | **YES** | `model.display_name` (`Opus`) |
| `session_id` | **YES** | `session_id` |
| workspace / cwd | **YES** | `cwd`, `workspace.current_dir`, `workspace.project_dir` |
| output style | **YES** | `output_style.name` |
| context-window size field | **YES** | `context_window.context_window_size` |
| `cost` block | **YES** | `cost.total_cost_usd`, durations, lines changed |
| `exceeds_200k_tokens` | **YES** | top-level boolean (see caveat below) |

`exceeds_200k_tokens` is a **fixed 200k tripwire regardless of the actual window**:
"Whether the total token count (input, cache, and output tokens combined) from the
most recent API response exceeds 200k. **This is a fixed threshold regardless of
actual context window size.**" [1] So on a 1M window it flips at 20% used — useful
as a "you've left the cheap zone" flag, useless as a window-relative gauge. Note it
*does* include output tokens, unlike `used_percentage`.

**Refresh / invocation / performance — DOCUMENTED [1]:**

- **When it runs:** "after each new assistant message, after `/compact` finishes,
  when the permission mode changes, or when vim mode toggles."
- **Debounce:** "Updates are debounced at 300ms… If a new update triggers while
  your script is still running, the in-flight execution is cancelled." → **keep the
  script fast**; a slow script causes stale or blank output, and slow git calls
  should be cached (the doc shows a `session_id`-keyed cache pattern).
- **Idle:** triggers "go quiet when the main session is idle"; add
  `refreshInterval` (min `1`s) for time-based segments.
- **Cost:** "The status line runs locally and **does not consume API tokens**." [1]
  This is what makes the meter a free observer.
- **Terminal width:** since v2.1.153, read `COLUMNS`/`LINES` env vars (`tput cols`
  won't work — output is captured, not a TTY). [1]
- **Trust gate:** because it executes a shell command, it needs workspace trust;
  `disableAllHooks: true` also disables it. [1]

**`settings.json` config object** (from the doc) [1]:

```json
{
  "statusLine": {
    "type": "command",
    "command": "~/.claude/statusline.sh",
    "padding": 2
  }
}
```

`type` must be `"command"`; `command` is a script path *or* an inline shell
command; optional `padding` (default 0), `refreshInterval` (seconds, min 1),
`hideVimModeIndicator`. There is also a sibling `subagentStatusLine` for per-subagent
rows. *(The Option #2 report's "config lives in settings.json, restart to apply"
phrasing was the killed claim — the accurate detail is "reloads automatically,
appears on next interaction.")*

---

## (b) Computing a reliable "percent of context used" — **DOCUMENTED + PROVEN**

### Path 1 (preferred): trust the pre-calculated field

Just read `context_window.used_percentage`. It is the *same number* Claude Code
itself uses, computed input-only. The doc's own one-liner config does exactly this:

```bash
jq -r '"[\(.model.display_name)] \(.context_window.used_percentage // 0)% context"'
```

The `// 0` matters — the field can be `null`.

### Path 2: recompute from `current_usage` (must match Path 1)

```
used_input_tokens = current_usage.input_tokens
                  + current_usage.cache_creation_input_tokens
                  + current_usage.cache_read_input_tokens   # NO output_tokens
used_percentage   = used_input_tokens / context_window_size * 100
```

This is the doc's exact formula. Equivalently, `total_input_tokens` already *is*
that sum: "`total_input_tokens` is the sum of `input_tokens`,
`cache_creation_input_tokens`, and `cache_read_input_tokens`." [1]

### Path 3 (fallback): parse the transcript JSONL

When the statusline block is `null` (very early in a session, or transiently after
`/compact`), derive the number from the transcript at `transcript_path`. The
transcript is JSONL, one JSON object per line, under
`~/.claude/projects/<encoded-cwd>/<session-id>.jsonl` ([sessions doc][7]).

**Verified schema** (firsthand, on a real v2.1.119 transcript on this machine, and
matching the official Usage object). Each **assistant** entry carries:

```jsonc
{
  "type": "assistant",                     // discriminator: user | assistant | system | ...
  "uuid": "...", "parentUuid": "...", "timestamp": "...",
  "sessionId": "...", "cwd": "...", "gitBranch": "...", "version": "2.1.119",
  "message": {
    "role": "assistant", "model": "claude-opus-4-7",
    "usage": {                             // ← per-message token accounting
      "input_tokens": 1,
      "cache_creation_input_tokens": 118,
      "cache_read_input_tokens": 39399,
      "output_tokens": 356
      // (+ server_tool_use, service_tier, cache_creation TTL split, iterations, …)
    }
  }
}
```

**THE CRUCIAL RULE — current context = the LAST assistant message, input-side
only:**

```
current_context = last_assistant.usage.input_tokens
                + last_assistant.usage.cache_creation_input_tokens
                + last_assistant.usage.cache_read_input_tokens
```

**Do NOT sum usage across turns.** Each API call re-sends the entire prior context
(mostly as `cache_read_input_tokens`), so summing double-counts every turn. I
proved this empirically (below): the per-turn input-side total **rises
monotonically** (24,675 → 39,518) as the conversation grows — *that* trajectory is
the real context size — while a naive running sum balloons to 1.55M, **39× too
large**.

**How compaction resets it:** after `/compact` (manual or auto), Claude Code
"replaces the conversation with a structured summary" and reloads startup content
([context-window doc][2]). The next assistant message's `usage` reflects the new,
smaller context, so the last-assistant rule self-corrects automatically — no
special handling needed beyond reading the latest entry. (The statusline block
goes `null` in the gap; that is the only thing to guard.)

### vs. the byte/4 heuristic and `/context`

- **byte/4** (Option #1/#2) estimates the cost of *content you are about to read* —
  it has no knowledge of system prompt, tools, cache, or history, so it is the
  wrong tool for *live* usage. Use the statusline block for "how full am I now,"
  byte/4 for "how expensive is this file."
- **`/context`** is the in-CLI authoritative live breakdown by category (system
  prompt, tools, MCP, memory, messages, free space) with optimization tips
  ([context-window doc][2]). The statusline meter is the *always-on, glanceable*
  version of the same underlying accounting. The doc explicitly warns they can
  differ slightly: "Context percentage may differ from `/context` output due to
  when each is calculated." [1] Treat `/context` as ground truth for spot-checks
  (see Proof), the meter as the continuous gauge.

---

## (c) Context-window sizes & how Claude Code accounts for them — **DOCUMENTED**

Reconciled against the **version-matched bundled `claude-api` skill**
(`2.1.186/.../claude-api/shared/models.md`), the prompt's designated authority, and
the official model/context docs. **All agree.**

| Model | Alias | Context window | Source agreement |
|---|---|---|---|
| Claude Fable 5 | `claude-fable-5` | **1M** | skill ✓ · [context-windows][9] ✓ · [model-config][3] ✓ |
| Claude Mythos 5 | `claude-mythos-5` | **1M** (Project Glasswing) | skill ✓ · [context-windows][9] ✓ |
| Claude Opus 4.8 | `claude-opus-4-8` | **1M** | skill ✓ · [context-windows][9] ✓ |
| Claude Opus 4.7 | `claude-opus-4-7` | **1M** | skill ✓ · [context-windows][9] ✓ |
| Claude Opus 4.6 | `claude-opus-4-6` | **1M** | skill ✓ · [model-config][3] ✓ |
| Claude Sonnet 4.6 | `claude-sonnet-4-6` | **1M** | skill ✓ · [context-windows][9] ✓ |
| Claude Haiku 4.5 | `claude-haiku-4-5` | **200K** | skill ✓ |
| Claude Sonnet 4.5 | `claude-sonnet-4-5` | **200K** | [context-windows][9] ✓ · skill lists Active, omits window |
| Claude Opus 4.5 (legacy) | `claude-opus-4-5` | 200K *(unconfirmed)* | skill lists Active but **does not state window** — see flag |

The platform doc states verbatim: "Claude Opus 4.8, Claude Mythos Preview, Claude
Opus 4.7, Claude Opus 4.6, and Claude Sonnet 4.6 have a 1M-token context window on
the Claude API, Amazon Bedrock, and Vertex AI. **On Microsoft Foundry, Claude Opus
4.8 has a 200k-token context window.** Other Claude models, including Claude Sonnet
4.5, have a 200k-token context window." [9] (Fable 5 / Mythos 5 are 1M per the
next sentence + the skill.)

> ⚠️ **Could not confirm:** **Opus 4.5's** exact window — the skill lists it Active
> but omits the size, and `/context` in a real session was *reported* (issue #13783,
> a refuted claim's transcript) as `claude-opus-4-5-20251101 · …/200k`, suggesting
> 200K, but I did not verify this against a primary table. **Flag it; query the
> Models API at runtime rather than assuming.**

**How Claude Code maps a model id → a limit.** Don't hardcode a model→size table —
the statusline gives you `context_window_size` directly, "**200000 by default, or
1000000 for models with extended context**." [1] For runtime detection outside the
statusline, the authoritative source is the **Models API** (per the bundled skill):

```python
client.models.retrieve("claude-opus-4-8").max_input_tokens   # → 1000000
```

**Detecting which window is active (200K vs 1M).** The `[1m]` suffix (`opus[1m]`,
`sonnet[1m]`, `claude-opus-4-8[1m]`) selects 1M, and `CLAUDE_CODE_DISABLE_1M_CONTEXT=1`
removes 1M variants from the picker [3] — but **the suffix is NOT a reliable
detector**, because 1M can be on without it:

> "On Max, Team, and Enterprise plans, Opus is automatically upgraded to 1M context
> with no additional configuration… On the Anthropic API, Fable 5, Opus 4.8, and
> Opus 4.7 always run with the 1M window. Sonnet with 1M context is not part of the
> automatic upgrade and requires usage credits… on every subscription plan,
> including Max." [3]

So the **only** reliable runtime signal is the statusline's `context_window_size`
itself (or `/status`, which "displays… your account information," or the Models
API). **Build the meter against `context_window_size`, never against an inferred
suffix.** Pricing footnote: "The 1M context window uses standard model pricing with
no premium for tokens beyond 200K." [3]

**Auto-compaction threshold.** Documented **directionally only** — "as you
approach the limit," clearing old tool outputs first, then summarizing
([how-it-works][6]; [context-window doc][2]). **No percentage is published**; the
"83%/~167k" figure is community speculation (refuted). The official simulation
hard-codes `MAX = 200000` for its 200k illustration [2]. One real failure mode is
documented: if a single huge output refills context after every summary, Claude
Code "stops auto-compacting after a few attempts and shows an error instead of
looping" (the *thrashing* error) [6] — a meter pinned at ~99% is the early warning
for exactly this.

---

## (d) Threshold / warning UX — meter design — **DOCUMENTED tiers + design**

Anthropic's own multi-line example keys colors on **percent USED** with **green
< 70% / yellow 70–89% / red 90%+** [1]:

> "This example combines several techniques: **threshold-based colors (green under
> 70%, yellow 70-89%, red 90%+)**, a progress bar, and git branch info." [1]

These are the **documented** tiers — use them. (A community blog instead keys tiers
on percent *remaining* — green ≥50%, yellow 21–49%, red ≤20% — which is the inverse
convention; fine as a personal choice but **label it community workaround, not
documented**, and don't mix the two scales.) The doc also confirms a built-in
**"context-low warning"** already "cycles through" the status row's notification
area [1] — your meter complements, not replaces, that.

**What to show, in priority order:**

1. **Color tier** (green/yellow/red at 70/90) — the at-a-glance signal.
2. **Percent** (`used_percentage`, rounded) — the number.
3. **A bar glyph** — the doc uses a 10–12 cell `█`/`░` run.
4. **Raw `usedk/sizek`** — so 70% reads differently on 200k vs 1M.
5. **Tokens-to-compaction** — the actionable nudge ("~Nk to compact"). Since the
   real threshold is undocumented, base it on a tunable guess (default ~92%) and
   **label it an estimate**, or omit it and rely on the color tier alone.

**This is observation, not prevention.** Unlike Option #1's PreToolUse gate (which
*denies* an oversized read), the meter never blocks — it nudges the human (or the
agent reading its own statusline) to `/compact`, `/clear`, offload to a subagent
(Option #4), or wrap up. Zero false-positive cost, zero API cost; the trade-off is
it only *informs*.

### Output mockup

```
[Opus] 🌿 main +3 ~2
████████░░░░ 71% · 142k/200k · ~42k to compact     ← yellow

# fuller, on a 1M window:
[Opus] ████████████ 94% · 940k/1000k · ~0k to compact   ← red, "/compact now"

# clean start:
[Opus] █░░░░░░░░░░░ 4% · 39k/1000k                  ← green
```

---

## The meter design

### `~/.claude/settings.json`

```json
{
  "statusLine": {
    "type": "command",
    "command": "python3 ~/.claude/context-meter.py",
    "padding": 1
  }
}
```

### `~/.claude/context-meter.py` (reference script — tested, fail-soft)

Prefers the pre-calculated field, falls back through `current_usage`, then the
transcript's last-assistant input-side total. Tiers at the **documented** 70/90.

```python
#!/usr/bin/env python3
"""Live context-window meter for the Claude Code statusline.
Primary: stdin context_window block. Fallback: parse the transcript JSONL.
Renders a colored bar with documented 70%/90% USED tiers + tokens-to-compaction."""
import json, os, sys

GREEN, YELLOW, RED, DIM, RESET = "\033[32m", "\033[33m", "\033[31m", "\033[2m", "\033[0m"
BAR_W = 12
COMPACT_AT = 0.92  # HEURISTIC ONLY — no documented auto-compact %; tune or remove

def from_transcript(path):
    """Authoritative fallback: LAST assistant usage, input-side only.
    Matches Claude Code's used_percentage formula."""
    if not path or not os.path.exists(path):
        return None
    last = None
    try:
        with open(path, encoding="utf-8", errors="replace") as f:
            for line in f:
                line = line.strip()
                if not line:
                    continue
                try:
                    e = json.loads(line)
                except json.JSONDecodeError:
                    continue
                if e.get("type") == "assistant":
                    u = e.get("message", {}).get("usage")
                    if u:
                        last = u  # keep overwriting -> ends on the LAST one
    except OSError:
        return None
    if not last:
        return None
    return (last.get("input_tokens", 0)
            + last.get("cache_creation_input_tokens", 0)
            + last.get("cache_read_input_tokens", 0))

def main():
    try:
        d = json.load(sys.stdin)
    except (json.JSONDecodeError, ValueError):
        print("ctx ?"); return

    cw = d.get("context_window") or {}
    size = cw.get("context_window_size") or 200000          # 200k default / 1M extended
    model = (d.get("model") or {}).get("display_name", "?")

    used_tok, pct = None, cw.get("used_percentage")         # pre-calculated, input-only
    if pct is not None:
        used_tok = round(pct / 100 * size)
    else:
        cu = cw.get("current_usage")                        # null pre-1st-call / post-/compact
        if cu:
            used_tok = (cu.get("input_tokens", 0)
                        + cu.get("cache_creation_input_tokens", 0)
                        + cu.get("cache_read_input_tokens", 0))
        if used_tok is None:
            used_tok = from_transcript(d.get("transcript_path"))

    if used_tok is None:
        print(f"[{model}] ctx --"); return
    if pct is None:
        pct = used_tok / size * 100

    frac = used_tok / size
    color = RED if pct >= 90 else YELLOW if pct >= 70 else GREEN   # documented tiers
    filled = min(BAR_W, round(frac * BAR_W))
    bar = color + "█" * filled + DIM + "░" * (BAR_W - filled) + RESET
    to_compact = max(0, round(COMPACT_AT * size) - used_tok)
    print(f"[{model}] {bar} {color}{pct:.0f}%{RESET} "
          f"{DIM}{used_tok//1000}k/{size//1000}k · ~{to_compact//1000}k to compact{RESET}")

if __name__ == "__main__":
    main()
```

Design choices, all deliberate:

- **Pre-calc first** — match Claude Code's own number with zero recomputation.
- **Three-level fallback** — survives the documented `null` windows (early session,
  post-`/compact`) by reading `current_usage`, then the transcript.
- **`context_window_size`-driven** — automatically correct on 200k *or* 1M; never
  hardcodes a model→size map (which the [1m]-detection caveat shows is unsafe).
- **Fail-soft** — bad JSON, missing transcript, or null fields print a degraded but
  non-blank line (a blank line / non-zero exit makes the statusline disappear [1]).
- **`COMPACT_AT` is flagged as a guess** — the threshold is undocumented.

---

## Proof that the meter is accurate — **FIRSTHAND EMPIRICAL**

Two proofs were actually run on this machine (Claude Code v2.1.186), not merely
proposed.

### Proof 1 — the last-assistant rule, and why summing is wrong (39× over-count)

Parsing a real 46-turn transcript
(`~/.claude/projects/<…>-agent-teams/2af21b44-…jsonl`) and computing, per assistant
turn, the input-side total `input + cache_read + cache_creation`:

```
turn  0: ctx(input-side) =  24,675   NAIVE-running-sum =    24,874
turn  5: ctx(input-side) =  29,358   NAIVE-running-sum =   167,552
turn 22: ctx(input-side) =  33,423   NAIVE-running-sum =   706,932
turn 45: ctx(input-side) =  39,518   NAIVE-running-sum = 1,551,269
---------------------------------------------------------------------
AUTHORITATIVE current context (last assistant, input-side) =    39,518
NAIVE running sum of ALL turns                              = 1,551,269   ← 39.3× too big
```

The input-side per-turn total **rises monotonically** (24,675 → 39,518) — that *is*
the live context filling up. The naive sum is meaningless. **This is the proof that
"current context = last assistant message, input-only" — not a running sum.**

### Proof 2 — the script's transcript-fallback equals an independent recompute

Feeding the script a statusline JSON with the `context_window` block omitted (only
`transcript_path` + `context_window_size: 1000000`), it must derive from the
transcript:

```
script output: [Opus] ░░░░… 4% · 39k/1000k · ~880k to compact
independent recompute from file: last-assistant input-side = 39,518 (3.95% of 1M)
```

The script's `used_tok` (39,518) and percentage (4%) **exactly match** the
independent recompute. The fallback path is correct.

### Proof 3 (recommended for your environment) — cross-check vs. `/context`

The two empirical proofs above validate the *arithmetic and the fallback*. To prove
the *whole pipeline end-to-end in your session*, cross-check against Claude Code's
own authoritative `/context`:

1. In a live session, run `/context` and note `…/<size> tokens (<P>%)`.
2. Capture the exact statusline stdin: set `command` to
   `tee ~/.claude/last-statusline.json | python3 ~/.claude/context-meter.py`,
   trigger a render, then inspect the captured JSON.
3. Assert `context-meter.py`'s percent and `used_percentage` from the capture both
   land within ±1–2 pts of `/context`. The doc itself warns of small timing-based
   drift ("may differ from `/context` output due to when each is calculated" [1]),
   so **±1–2 pts is a pass, not a bug** — do not chase exact equality.
4. Trigger a `/compact` and confirm the script degrades to `ctx --` (or the
   transcript fallback) during the `null` window, then recovers on the next message.

A green proof reads: *"across N renders spanning a `/compact`, `context-meter.py`
tracked `used_percentage` exactly, matched a transcript recompute to the token, and
stayed within ±2 pts of `/context`, never going blank."* Pair it with a unit test
on mock stdin asserting the **tier boundaries** (69→green, 70→yellow, 89→yellow,
90→red) and the **null fallbacks**.

> **Why not `count_tokens`?** It returns `input_tokens` only for a *message you
> construct*; it cannot reproduce Claude Code's live system prompt, tool schemas,
> cache state, or post-compaction summary, so it is **not** a faithful oracle for
> the live number (this was a refuted claim). `/context` + the transcript are.

---

## Caveats & version-sensitivity (flag these in any build)

- **`null` windows.** `used_percentage`/`remaining_percentage`/`current_usage` are
  `null` before the first API call and again right after `/compact` until the next
  call. Always use `// 0` (jq) / `or 0` / the transcript fallback. *DOCUMENTED.* [1]
- **v2.1.132 semantics flip.** Before v2.1.132, `total_input_tokens`/`total_output_tokens`
  were *cumulative session totals* (could read >100% of the window — the famous
  169% bug); from v2.1.132 they are *current context*. If you must support old
  versions, branch on `version`. *DOCUMENTED.* [1]
- **`exceeds_200k_tokens` is a fixed 200k tripwire** (and *includes* output tokens),
  not a window-relative gauge — on a 1M window it fires at 20%. Don't use it as the
  meter. *DOCUMENTED.* [1]
- **`used_percentage` excludes output tokens** by design; if you want an
  output-inclusive number you must add `current_usage.output_tokens` yourself, but
  then you will **not** match Claude Code's own figure. *DOCUMENTED.* [1]
- **Auto-compaction threshold is NOT a published number.** "Approaching the limit"
  only. Any "Nk to compact" you display is a tunable guess — label it. *SPECULATION
  zone.* [6][2]
- **`[1m]` suffix is not a window detector.** 1M can be active without it (auto-
  upgrade tiers; always-on for Opus 4.8/4.7/Fable 5 on the API). Read
  `context_window_size`. *DOCUMENTED.* [3]
- **Opus 4.5 window unconfirmed** (skill omits it). Query the Models API at runtime.
  *FLAGGED.*
- **Keep the script fast.** 300ms debounce; slow scripts go stale/blank and in-flight
  runs are cancelled; cache slow git calls keyed on `session_id`. *DOCUMENTED.* [1]
- **Transcript may be absent/relocated.** `~/.claude/projects/` moves with
  `CLAUDE_CONFIG_DIR`; writes are suppressed by `CLAUDE_CODE_SKIP_PROMPT_HISTORY` /
  `--no-session-persistence`; files auto-delete after 30 days (`cleanupPeriodDays`).
  The fallback must handle a missing file. *DOCUMENTED ([sessions][7]).*
- **`/context` drift.** Expect ±1–2 pts vs. the statusline due to calculation
  timing. *DOCUMENTED.* [1]
- **The documented 70/90 USED tiers vs. the community 50/20 REMAINING tiers** are
  inverse conventions — pick one, label community ones as such.

## Open questions (not answered by available sources)

1. **What exact percentage triggers auto-compaction** (and how much buffer it
   reserves) for 200k vs. 1M — undocumented; only "approaching the limit." Would
   need empirical measurement (drive a session up and watch when `/compact` fires).
2. **Opus 4.5's exact context window** — confirm via the Models API
   (`max_input_tokens`); the bundled skill omits it.
3. **Whether `used_percentage` denominator is the raw `context_window_size` or a
   smaller *usable* window** (after the compaction reserve). The script assumes the
   raw size; if Claude Code reserves headroom, real "tokens to compact" is smaller.
4. **Exact post-`/compact` `null` duration** — how many renders the block stays
   null before the next API call repopulates it (affects how often the fallback path
   actually fires).

---

## Sources

**Primary (official, current as of v2.1.186):**
1. [Customize your status line — code.claude.com](https://code.claude.com/docs/en/statusline) — the full stdin schema, `context_window` fields, the input-only `used_percentage` formula, v2.1.132 note, null-field list, 300ms debounce / "no API tokens", 70/90 color tiers, config object.
2. [Explore the context window — code.claude.com](https://code.claude.com/docs/en/context-window) — `MAX = 200000`, "compacts automatically as you approach the limit", `/context` as the live authoritative breakdown, 1M extended-context note.
3. [Model configuration — code.claude.com](https://code.claude.com/docs/en/model-config) — Extended context: 1M model list, auto-upgrade, Sonnet usage-credit rule, `[1m]` suffix, `CLAUDE_CODE_DISABLE_1M_CONTEXT`, "no premium beyond 200K", suffix stripped before send; `/status` shows current model.
4. [Manage costs effectively — code.claude.com](https://code.claude.com/docs/en/costs) — auto-compaction "summarizes conversation history when approaching context limits" (no %), `/usage`, status-line pointer.
6. [How Claude Code works — code.claude.com](https://code.claude.com/docs/en/how-claude-code-works) — "When context fills up": clears tool outputs first then summarizes; the thrashing-error failure mode; transcript under `~/.claude/projects/`.
7. [Manage sessions — code.claude.com](https://code.claude.com/docs/en/sessions) — transcript path `~/.claude/projects/<project>/<session-id>.jsonl`, JSONL line format, relocation/suppression/retention caveats.
9. [Context windows — platform.claude.com](https://platform.claude.com/docs/en/build-with-claude/context-windows) — which models are 1M vs 200k across API/Bedrock/Vertex/Foundry; Sonnet 4.5 = 200k; Foundry Opus 4.8 = 200k.
- [Token counting — platform.claude.com](https://platform.claude.com/docs/en/build-with-claude/token-counting) and [count_tokens API](https://platform.claude.com/docs/en/api/messages-count-tokens) — `{"input_tokens": N}` single-field response; "should be considered an estimate." (Relevant only to the *refuted* count_tokens-as-oracle claim and to Option #2.)
- **Version-matched bundled `claude-api` skill** (`2.1.186/.../claude-api/shared/models.md`, `platform-availability.md`) — authoritative model→context-window table (Fable 5/Opus 4.8/4.7/4.6/Sonnet 4.6 = 1M; Haiku 4.5/Sonnet 4.5 = 200K) and the Models-API runtime-discovery pattern (`max_input_tokens`). **Designated authority; web claims reconciled against it — all agree.**

**Primary empirical (this machine, v2.1.186):**
- Real transcript `~/.claude/projects/-Users-…-agent-teams/2af21b44-…jsonl` — confirmed the assistant-entry envelope and `message.usage` four-field schema; **Proof 1** (39× over-count) and **Proof 2** (script fallback = recompute) were run against it.

**Secondary / community (flagged inline):**
5. [israynotarray — Claude Code status line setup guide, 2026-03](https://israynotarray.com/en/ai/2026/03/26/claude-code-status-line-setup-guide/) — the *remaining-percentage* tier convention (green ≥50 / yellow 21–49 / red ≤20), labeled community workaround (inverse of the documented used-% tiers).
- [dev.to newtorob — reading local token usage](https://dev.to/newtorob/claude-code-and-codex-are-logging-your-token-usage-locally-here-is-how-to-read-it-580) and [dev.to slima4 — tracing tokens](https://dev.to/slima4/where-do-your-claude-code-tokens-actually-go-we-traced-every-single-one-423e) — corroborate the `message.usage` four-field transcript layout (the "~83% auto-compact" figure from slima4 is **refuted** — see below).
- [ccstatusline](https://github.com/sirmalloc/ccstatusline), [starship-claude](https://github.com/martinemde/starship-claude) — prebuilt statusline projects the doc itself links.

**Refuted-claim sources (kept for transparency):**
- [GitHub issue #13783](https://github.com/anthropics/claude-code/issues/13783) — the pre-v2.1.132 "cumulative totals / 169% / diverges from /context" reports (fixed; **do not repeat**).
- [dev.to slima4](https://dev.to/slima4/where-do-your-claude-code-tokens-actually-go-we-traced-every-single-one-423e) — "~83% auto-compaction threshold" (**SPECULATION; no doc supports a %**).
