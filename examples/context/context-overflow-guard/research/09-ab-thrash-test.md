# Overflow thrash-test A/B harness: empirically PROVING the guard works

> **Source:** deep-research run for the thrash-test prompt (**#9**) in
> [`../research-prompts.md`](../research-prompts.md). Search angles: (a) headless /
> scriptable runs (`claude -p`, `--output-format`, session/settings flags,
> determinism levers), (b) per-turn token accounting from the transcript JSONL
> (`usage` fields, cumulative reconstruction, `-p --output-format json` totals,
> `ccusage` / `/context`, byte/4 vs real), (c) scripting the overflow scenario WITH
> vs WITHOUT the guard while controlling confounders, (d) the single chartable
> metric. **Provenance:** sources fetched →
> `code.claude.com/docs/en/{cli-reference,headless,settings,permission-modes,hooks,model-config,statusline}`,
> `github.com/ryoppippi/ccusage`, `ccusage.com/guide/cost-modes`,
> GitHub `anthropics/claude-code#{28167,11392,24589}` → **claims raised** (headless
> flags, JSONL schema, A/B scripting, metric) → **adversarially verified** (3-vote,
> need 2/3 to kill) → **20 confirmed, 6 killed** → synthesized into the findings
> below. Sources are overwhelmingly **primary** (official `code.claude.com` docs +
> the ccusage repo). Current as of **mid-2026** (Claude Code v2.1.x — docs
> reference v2.1.119 / v2.1.132 / v2.1.154 / v2.1.170 / v2.1.186, models Opus 4.8 /
> Sonnet 4.6 / Haiku 4.5 / Fable 5, 200k default / 1M extended context). The §(b)
> transcript `message.usage` schema was **directly observed** on this project's own
> live JSONL (2026-06-23) — the on-disk record carries exactly
> `input_tokens`, `output_tokens`, `cache_creation_input_tokens`,
> `cache_read_input_tokens` plus the `cache_creation.ephemeral_5m/1h` split.

**Bottom line.** You can prove the guard ([`hooks/guard_context.py`](../hooks/guard_context.py))
works with a clean two-arm A/B in **headless print mode** (`claude -p`), and the
proof is credible because every input and output is a documented, scriptable
surface. Run the **identical overflow-inducing prompt twice** — once WITH the
PreToolUse guard injected via `--settings`, once WITHOUT — holding the model
(`--model claude-opus-4-8`), repo state, turn cap (`--max-turns`), and permission
mode (`--permission-mode dontAsk` or `--dangerously-skip-permissions`) constant,
and disabling auto-compaction (`DISABLE_AUTO_COMPACT=1`) so raw context growth
isn't masked. Capture two data streams per arm: the **per-turn `message.usage`**
from the session JSONL (`~/.claude/projects/<slug>/<session-id>.jsonl`) and the
**final `--output-format json`** envelope (`.result`, `.session_id`,
`.total_cost_usd`, `.num_turns`, `.usage`). The single cleanest chartable metric is
**peak input-side context tokens** — `max over assistant turns of
(input_tokens + cache_creation_input_tokens + cache_read_input_tokens)` — which is
the *exact* input-only formula Claude Code uses for its own `used_percentage`
([statusline doc][stl]; corroborated by [issue #28167][i28167]). The guard arm
should show a **markedly lower peak** (oversized Reads/`cat`s are denied before
their bytes ever enter context) and **more turns before hitting the limit**. Be
honest about two fuzz sources: **`-p` is not bit-deterministic** (sampling +
agentic branching vary run-to-run, so run n≥3 per arm and report medians), and the
guard's **own byte/4 estimate is a deliberate under-count**, not ground truth — the
JSONL `usage` is the real measurement, and the two should be reported separately.

---

## ⚠️ Refuted claims — do NOT repeat (killed 2–3 in verification)

- **"`--settings` *overrides* same-key values so you can toggle the hook by swapping
  the file."** **REFUTED (1-2).** `--settings` **merges** array fields like `hooks`
  rather than replacing them ([issue #11392][i11392], has-repro): a hook in
  `~/.claude/settings.json` still fires even when `--settings` passes an empty
  `hooks` array. **Consequence for the harness:** you cannot *subtract* a guard hook
  via `--settings`. Build the A/B **additively** instead — guard ON = `--settings`
  that *adds* the PreToolUse hook; guard OFF = a settings file (or `--bare`) that
  omits it — and ensure **no guard hook lives in `~/.claude` or managed settings**.
  See §(c).
- **"Settings precedence is Managed > CLI args > Local > Project > User."**
  **REFUTED (0-3)** as stated. The merge behavior (above) makes "which file wins"
  per-key/per-array, not a clean single ordering. Rely on the additive design, not
  on subtractive override precedence.
- **"`--dangerously-skip-permissions` / `bypassPermissions` disables all permission
  prompts *and safety checks* so calls execute immediately."** **REFUTED (0-3)** —
  the "and safety checks" / "immediately" overreach was not supportable. The flag
  *is* documented as equivalent to `--permission-mode bypassPermissions` (skips
  prompts) — see Finding 2 — but don't claim it disables safety machinery.
- **"PreToolUse hooks live at three settings levels, so you toggle the guard by
  pointing at a file that registers-vs-omits the hook."** **REFUTED (1-2).** Same
  merge caveat: omitting it from one file does NOT remove it if another file has it.
- **"`used_percentage` is exposed in the transcript and is the cleanest peak-%
  signal."** **REFUTED (0-3).** `used_percentage` is a **statusline** field, not a
  transcript field; for headless A/B you **recompute** the identical input-only
  formula from the JSONL `usage` (see Finding 6).

---

## (a) Headless / scriptable runs — the reproducibility surface

**Print mode is the whole foundation.** `claude -p` (alias `--print`) "Print[s]
response without interactive mode" and exits, and piped content is first-class
(`cat logs.txt | claude -p "explain"`) — exactly the shape an A/B harness needs
([cli-reference][cli], 3-0). The headless doc reinforces it: "Add the `-p` (or
`--print`) flag to any `claude` command to run it non-interactively. **All CLI
options work with `-p`**" ([headless][hl], 3-0). *Confidence: high — documented.*
The one literal exception: built-in interactive slash commands such as `/login`
"are not available in `-p` mode" — irrelevant to flag-driven runs.

**Output capture.** `--output-format` accepts `text | json | stream-json`;
`--input-format` accepts `text | stream-json` ([cli-reference][cli], 3-0). For the
harness, **`--output-format json`** is the per-run envelope: it returns "structured
JSON with result, session ID, and metadata," with the text answer in `.result`, the
session id in `.session_id`, and crucially **`total_cost_usd` plus a per-model cost
breakdown** "so scripted callers can track spend per invocation" ([headless][hl],
3-0). For token-level granularity, **`--output-format stream-json --verbose
--include-partial-messages`** emits newline-delimited JSON events (one event per
line, jq-filterable) "to receive tokens as they're generated" ([headless][hl], 3-0)
— but for an A/B you usually don't need the partial stream; the **per-message
`usage` is already in the JSONL** (§b), and the final `json` envelope gives the
roll-up. *Confidence: high — documented.*

**Determinism / pinning levers** (each documented in [cli-reference][cli], 3-0):

| Lever | Flag / mechanism | What it pins | Caveat |
|---|---|---|---|
| **Model** | `--model claude-opus-4-8` (full name, not the `opus` alias) | Same model both arms | Aliases "point to the recommended version… and update over time"; **pin the full name** ([model-config][mc], 3-0). Precedence: `/model` > `--model` > `ANTHROPIC_MODEL` > settings `model` ([model-config][mc], 3-0). |
| **Turn budget** | `--max-turns N` | Bounds agentic turns; "print mode only… **Exits with an error when the limit is reached.** No limit by default" | Errors (non-zero exit) at the cap — the harness must tolerate that exit code. |
| **Permissions** | `--permission-mode dontAsk` (or `bypassPermissions` / `--dangerously-skip-permissions`) | Unattended runs don't block on prompts | For genuinely unattended `-p`, `dontAsk`/`bypassPermissions` are the practical modes; `auto` can abort on repeated classifier blocks ([permission-modes][pm], 3-0). `--dangerously-skip-permissions` = `--permission-mode bypassPermissions` ([cli-reference][cli], 3-0). |
| **Config isolation** | `--bare` + explicit `--settings` | "the same result on every machine" — skips auto-discovery of hooks, skills, plugins, MCP, auto memory, CLAUDE.md | "Only flags you pass explicitly take effect." **A teammate's `~/.claude` hook won't run** — so in `--bare` the guard MUST be supplied via `--settings`. The headless doc says `--bare` "will become the default for `-p` in a future release" ([headless][hl], 3-0). |

**`--bare` is the cleanest confounder-killer** for this exact test: with it, the
project's `.claude/settings.json` guard does **not** auto-load, so both arms start
from an identical empty baseline and the guard is present **only** when you pass it
via `--settings`. *Confidence: high — documented.*

**Honest limit on determinism.** None of these flags make `-p` *bit-reproducible*.
Model sampling and agentic branching (which file it reads first, whether it retries)
vary run to run. The docs only promise `--bare` gives "the same result on every
machine" with respect to **configuration auto-discovery**, not LLM output. Treat the
harness as **statistical**: n≥3 runs/arm, report medians/spreads.

---

## (b) Per-turn token accounting from the transcript JSONL

**Where it lives.** Each session writes a JSONL transcript at
`~/.claude/projects/<project-slug>/<session-id>.jsonl`, where `<project-slug>` is the
cwd with `/` → `-`. Every `assistant` record carries a nested **`message.usage`**
object. *(Path layout + field names: directly observed on this project's live
transcript 2026-06-23 — not formally documented, version-sensitive.)*

**The fields** (confirmed against the live API `usage` schema, the statusline doc,
and this project's on-disk JSONL — claims [14][18], 3-0; observed record below):

```json
{ "input_tokens": 2,
  "cache_creation_input_tokens": 1493,
  "cache_read_input_tokens": 112840,
  "cache_creation": { "ephemeral_5m_input_tokens": 0, "ephemeral_1h_input_tokens": 1493 },
  "output_tokens": 1808,
  "service_tier": "standard" }
```

| Field | Meaning | Role in the A/B metric |
|---|---|---|
| `input_tokens` | Uncached input tokens this turn (after the last cache breakpoint) | part of context size |
| `cache_creation_input_tokens` | Tokens written to cache this turn | part of context size |
| `cache_read_input_tokens` | Tokens served from cache this turn | part of context size (dominant term as context grows) |
| `output_tokens` | Tokens generated this turn | **excluded** from `used_percentage`; counted on the limit-trigger side |

**Reconstructing "context window used" per turn — the trap.** Current context size
is **the last assistant message's input-side usage, NOT a running sum across turns.**
As of **v2.1.132**, `context_window.total_input_tokens`/`total_output_tokens` "reflect
current context usage, not cumulative session totals" — before that version they were
cumulative ([statusline][stl], 3-0, claim [15]). The sibling statusline report
([`08-statusline-context-meter.md`](08-statusline-context-meter.md)) proved that a
naive running-sum **overcounts by ~39×** on a real transcript. So the correct
per-turn "context used" is, for each assistant turn:

```
context_used(turn) = input_tokens + cache_creation_input_tokens + cache_read_input_tokens
```

This is **input-only** (no `output_tokens`) — the exact formula Claude Code uses for
its displayed `used_percentage` ([statusline][stl], 3-0; [issue #28167][i28167]
corroborated by 6+ convergent issues). *(Subtlety for the limit side: the actual
"Context limit reached" trigger DOES count output tokens — the `exceeds_200k_tokens`
statusline field counts "input, cache, and output tokens combined" — so the
displayed % can read low while a turn still trips the limit. Report peak input-side %
as the headline, but note this when explaining near-limit behavior.)* *Confidence:
high.*

**What `claude -p --output-format json` returns at the end.** A single JSON envelope
with `.result` (text), `.session_id`, `.total_cost_usd` + per-model cost breakdown,
and `.num_turns`; a `.usage` roll-up is present on real records ([headless][hl], 3-0,
claim [4]). Use `.total_cost_usd` and `.num_turns` as the **per-run summary row**;
use the JSONL `usage` for the **per-turn curve**. `total_cost_usd` is a client-side
estimate (see [`04-subagent-offloading.md`](04-subagent-offloading.md)), fine for
relative A/B comparison.

**Tooling.** `ccusage` ("Analyze coding (agent) CLI token usage and costs from local
data," run via `npx ccusage@latest` / `bunx ccusage`) reads the same Claude Code
session JSONL and **tracks cache creation vs cache read tokens separately**
([ryoppippi/ccusage][ccu], 3-0, claims [16][17][18]) — useful for cache-adjusted
session roll-ups, but it reports **session/daily** totals, not the per-turn peak the
A/B needs, so for the metric **parse the JSONL directly**. `/context` gives the same
breakdown interactively, but there is **no documented headless `/context`
equivalent** — parsing the JSONL is the headless substitute. *Confidence: high
(ccusage); medium (no headless /context — documented absence).*

**byte/4 vs real usage — how fuzzy.** The guard estimates `tokens ≈ bytes / 4`
([`guard_context.py`](../hooks/guard_context.py) `CHARS_PER_TOKEN = 4`), deliberately
chosen so it "**under**-counts so the guard is not trigger-happy" (Anthropic's
tokenizer averages ~3.5–4 chars/token). This is a **decision input** for the guard,
**not** a measurement: it can mis-estimate on code/Unicode/whitespace by tens of
percent. The **A/B proof must measure with the JSONL `usage`** (real tokens from the
API), and may *separately* report the guard's byte/4 estimate to show the guard's
threshold logic firing — but never conflate the two. *Confidence: high (the byte/4
heuristic is in-repo; its inaccuracy vs the tokenizer is the documented reason for
the under-count margin).*

---

## (c) Scripting the overflow scenario — WITH vs WITHOUT the guard

**Design the scenario to *tend toward* overflow.** Generate several large files up
front, then give a fixed prompt that forces the agent to ingest them whole — the
exact behavior the guard intercepts (oversized `Read`, or `cat`/`bat`/`nl` of a big
file; see `DUMP_COMMANDS` in [`guard_context.py`](../hooks/guard_context.py)). Keep
the prompt **byte-identical** across arms.

**Toggle the guard ADDITIVELY (the verified-correct way).** Because `--settings`
*merges* `hooks` and cannot subtract a hook ([issue #11392][i11392]), do **not** try
to disable the guard by passing an empty hooks array. Instead:

- **Guard OFF (control):** run with `--bare` (skips auto-discovery of the project's
  `.claude/settings.json` hook) and **no** guard `--settings`. Confirm no guard hook
  exists in `~/.claude/settings.json` or managed settings.
- **Guard ON (treatment):** run with `--bare` **plus** `--settings guard.json` where
  `guard.json` registers the PreToolUse hook under the top-level `hooks` key
  ([settings][set] + [hooks][hk], 3-0). The hook denies oversized Read/Bash by
  exiting 0 and emitting `hookSpecificOutput.permissionDecision:"deny"` — the exact
  mechanism in [`guard_context.py`](../hooks/guard_context.py) lines 56–64 (claim
  [12], 3-0; the hook can also *redirect* via `updatedInput`, claim [13]).

**Control the confounders** (each maps to a documented lever from §a):

| Confounder | How to hold it constant |
|---|---|
| Model drift | `--model claude-opus-4-8` (full name, both arms) |
| Auto-compaction masking growth | `DISABLE_AUTO_COMPACT=1` in the run env (or `autoCompactEnabled:false` in `--settings`) — documented at min-version 2.1.119 ([settings][set], claim [8]). Otherwise compaction silently truncates context and hides the difference. |
| Permission prompts stalling | `--permission-mode dontAsk` both arms |
| Stray config | `--bare` both arms; guard injected only via `--settings` in the ON arm |
| Repo state | `git stash`/clean checkout + regenerate the big files identically before each run |
| Cache warmth | run arms in the **same order** repeatedly, or alternate and report medians; cache reads inflate context size identically in both arms but cold-vs-warm timing differs run-to-run |
| Prompt variance | one fixed prompt file, fed via `-p "$(cat prompt.txt)"` |

### Runnable bash + jq sketch

```bash
#!/usr/bin/env bash
set -uo pipefail
PROJ="$PWD"
SLUG=$(printf '%s' "$PROJ" | sed 's#/#-#g')
TX_DIR="$HOME/.claude/projects/$SLUG"
OUT=./ab-out; mkdir -p "$OUT"
MODEL="claude-opus-4-8"
N=3                                   # runs per arm (statistical, not deterministic)

# guard.json: additively registers the PreToolUse guard (the ON arm only)
cat > "$OUT/guard.json" <<JSON
{ "hooks": { "PreToolUse": [ { "matcher": "Read|Bash",
    "hooks": [ { "type": "command",
      "command": "python3 \"$PROJ/hooks/guard_context.py\"" } ] } ] },
  "autoCompactEnabled": false }
JSON

# Fixed overflow prompt: force whole-file ingestion of pre-generated big files.
PROMPT='Read big1.log, big2.log, and big3.log in full and summarize each.'

run_arm () {                          # $1=label  $2(optional)=--settings file
  local label="$1"; shift
  for i in $(seq 1 "$N"); do
    local sid; sid=$(uuidgen)
    DISABLE_AUTO_COMPACT=1 \
      claude -p "$PROMPT" \
        --model "$MODEL" \
        --permission-mode dontAsk \
        --max-turns 30 \
        --bare \
        --session-id "$sid" \
        --output-format json \
        "$@" \
      > "$OUT/${label}-${i}.json" 2>"$OUT/${label}-${i}.err"
    # final-envelope roll-up
    jq -r '"[\($ARGS.positional[0])] cost=\(.total_cost_usd) turns=\(.num_turns)"' \
       --args "${label}-${i}" "$OUT/${label}-${i}.json"
    # per-turn peak input-side context from the session JSONL
    peak_context "$TX_DIR/${sid}.jsonl" "$label-$i"
  done
}

peak_context () {                     # jq: peak (input+cache_creation+cache_read) over assistant turns
  local f="$1" tag="$2"
  [ -f "$f" ] || { echo "$tag: no transcript"; return; }
  jq -s --arg tag "$tag" '
    [ .[] | select(.type=="assistant") | .message.usage
      | select(. != null)
      | (.input_tokens + .cache_creation_input_tokens + .cache_read_input_tokens) ]
    | { tag:$tag, turns:length, peak_context:(max // 0) }
  ' "$f"
}

run_arm "OFF"                          # control: no --settings, guard absent
run_arm "ON"  --settings "$OUT/guard.json"   # treatment: guard injected
```

Notes: `--session-id` (a documented session-control flag) lets you locate the exact
transcript per run; `--max-turns 30` exits non-zero at the cap (expected when the
OFF arm overflows — don't treat it as failure). `--resume`/`--continue` exist for
multi-step scripted flows but aren't needed here. *Confidence: high (every flag and
field is documented); medium (exact run-to-run numbers are non-deterministic).*

---

## (d) The metric — one clean chartable signal

**Headline = peak input-side context tokens** (and its % of the window):

```
peak_context = max over assistant turns of
               (input_tokens + cache_creation_input_tokens + cache_read_input_tokens)
peak_pct      = peak_context / context_window_size * 100   # window = 200000 or 1000000
```

This is **identical to Claude Code's own `used_percentage` formula** ([statusline][stl],
3-0) — using it means your chart matches what the product reports, and it's computed
straight from the JSONL `usage` (Finding/§b), so it's fully headless. The guard arm
should peak **well below** the control because denied oversized Reads/`cat`s never
deposit their bytes into context. *Confidence: high.*

**Supporting metrics (small set):**

| Metric | Compute from | Why it complements peak% |
|---|---|---|
| **Turns-to-limit / turns-to-error** | `.num_turns` in the `json` envelope; or whether the run exited non-zero at `--max-turns` | With `DISABLE_AUTO_COMPACT=1`, the OFF arm hits the context limit (or the turn cap while thrashing) in **fewer** turns; the ON arm survives **longer**. This is the "turns-to-overflow" signal the prompt asks for. |
| **Total input tokens** | sum of `(input+cache_creation+cache_read)` over turns, or `.usage` in the envelope | Cumulative load; rises faster in the OFF arm. |
| **Cache-adjusted context** | split `cache_read_input_tokens` (0.1× priced) from `input_tokens`/`cache_creation` | Shows the *billed* difference, not just the raw token difference — large reads are mostly cache-cold writes in the OFF arm. |
| **Cost per run** | `.total_cost_usd` | Client-side estimate; directionally lower for the guarded arm. |
| **Guard activations** | count `permissionDecision:"deny"` events in the guard's own log, or denied-tool markers in the transcript | Confirms the *mechanism* (the guard actually fired), separate from the *effect* (lower peak%). |

**The cleanest chart:** per-turn `context_used` (y) vs turn index (x), two lines
(ON vs OFF). The OFF line climbs steeply toward `context_window_size` and may stop
at the limit; the ON line plateaus far below. Annotate the OFF line's turns where
the guard *would* have denied (the byte/4 estimate from the guard, plotted as a
secondary series) to visually tie cause (denied reads) to effect (flat curve).
*Confidence: high.*

---

## Version sensitivity & open questions

**Version-sensitive (scoped to mid-2026 / Claude Code v2.1.x):**

- **`--bare` semantics + "future default for `-p`"** ([headless][hl]). If it becomes
  the `-p` default, runs that *rely on* auto-discovered hooks will silently change
  behavior — pin config via `--settings` explicitly and don't depend on
  auto-discovery in scripts.
- **`--settings` merges, doesn't replace** ([issue #11392][i11392]) — the entire
  additive A/B design in §(c) exists to route around this. Re-test if the merge
  behavior changes.
- **`total_input_tokens`/`total_output_tokens` flipped cumulative→current at
  v2.1.132** ([statusline][stl]). On older builds the per-turn reconstruction in
  §(b) would over-count — verify your version.
- **`DISABLE_AUTO_COMPACT` / `autoCompactEnabled`** documented at min-version 2.1.119
  ([settings][set]); the linked env-vars page does **not** itself list
  `DISABLE_AUTO_COMPACT` (a doc cross-ref inconsistency), and it was earlier a
  community-discovered method ([issue #24589][i24589], since officially documented on
  the settings page). Cite the settings page, not env-vars.
- **Transcript `message.usage` schema + JSONL path layout** in §(b) are **directly
  observed**, not formally documented — re-verify on your version.
- **`updatedInput` redirect** (claim [13]) has had version-specific bugs with
  multiple hooks; the single-guard case is the supported path.

**Honest fuzz (the prompt asked for this explicitly):**

1. **Headless determinism is statistical, not bit-exact.** `-p` + fixed model/prompt
   still varies via sampling and agentic branching. **Mitigation:** n≥3 per arm,
   report medians + spread; the guard's effect should dominate the noise (peak% gap
   is large), but a single-run "proof" is not credible.
2. **Token accounting has two layers that must not be conflated.** The guard's
   **byte/4** is a deliberate under-count *decision input*; the JSONL **`usage`** is
   the real *measurement*. Prove the effect with `usage`; show the mechanism with the
   guard's estimate/deny log.
3. **CLI JSONL can undercount vs the raw API** (blog-quality observation, carried
   from sibling reports) — fine for relative A/B, suspect for absolute billing.
4. **The displayed-% vs limit-trigger asymmetry** (output excluded from %, included
   in the trigger) means a run can "look" under-budget yet still trip the limit —
   note it when interpreting near-limit turns.

**Open questions:**

1. **Is there a documented headless equivalent of `/context`?** Still none found —
   parsing the JSONL is the substitute, but a first-class headless context readout
   would make the proof cleaner.
2. **Does `-p` write the session JSONL at the same path/schema as interactive runs
   in v2.1.x, and is `--session-id` honored as the transcript filename?** Observed to
   hold for interactive sessions on this machine; not separately confirmed for every
   `-p` permutation (`--bare`, `--output-format json`).
3. **Exact auto-compaction trigger threshold** — undocumented as a percentage
   (sibling report killed the "~83%" claim as speculation); we disable it entirely
   for the test rather than reason about its threshold.
4. **Per-run cost/usage stability** — `total_cost_usd` is a client estimate; whether
   it tracks real billing closely enough for absolute (not relative) claims is
   unverified.

---

## Sources

**Primary (Anthropic) — documented:**
- [`code.claude.com/docs/en/cli-reference`][cli] — `-p`/`--print`, piping,
  `--output-format`/`--input-format`, `--max-turns`, `--model`,
  `--permission-mode` (six modes), `--dangerously-skip-permissions`, `--bare`,
  `--settings`, `--session-id`/`--resume`/`--continue`.
- [`code.claude.com/docs/en/headless`][hl] — "all CLI options work with `-p`,"
  `--output-format json` envelope (`result`/`session_id`/`total_cost_usd`),
  `stream-json --verbose --include-partial-messages`, `--bare` ("same result on
  every machine," future `-p` default).
- [`code.claude.com/docs/en/settings`][set] — top-level `hooks` key,
  `autoCompactEnabled` / `DISABLE_AUTO_COMPACT` (min-version 2.1.119).
- [`code.claude.com/docs/en/hooks`][hk] — PreToolUse deny via
  `hookSpecificOutput.permissionDecision:"deny"`; `updatedInput` redirect.
- [`code.claude.com/docs/en/permission-modes`][pm] — `--permission-mode` works with
  `-p`; six modes; `dontAsk`/`bypassPermissions` for unattended runs.
- [`code.claude.com/docs/en/model-config`][mc] — pin full model name vs alias;
  `/model` > `--model` > `ANTHROPIC_MODEL` > settings precedence.
- [`code.claude.com/docs/en/statusline`][stl] — `current_usage` four fields;
  `used_percentage` input-only formula; `total_*` cumulative→current at v2.1.132.

**Primary (tooling):**
- [`github.com/ryoppippi/ccusage`][ccu] + [`ccusage.com/guide/cost-modes`](https://ccusage.com/guide/cost-modes)
  — reads Claude Code session JSONL; tracks cache-creation vs cache-read separately;
  the nested `message.usage` schema.

**GitHub issues (caveats / corroboration):**
- [`anthropics/claude-code#11392`][i11392] — `--settings` merges hooks (forces the
  additive A/B design).
- [`anthropics/claude-code#28167`][i28167] — `used_percentage` input-only formula
  (corroborates the statusline doc; community report, doc-confirmed).
- [`anthropics/claude-code#24589`][i24589] — `DISABLE_AUTO_COMPACT` history
  (community-discovered → since documented on the settings page).

**Directly observed (not documented — version-sensitive):** the `message.usage`
field set and `~/.claude/projects/<slug>/<session-id>.jsonl` layout in §(b),
inspected on **this project's own live transcript, 2026-06-23** (record carried
`input_tokens`/`cache_creation_input_tokens`/`cache_read_input_tokens`/`output_tokens`
+ the `cache_creation.ephemeral_5m/1h` split).

**Quality split per the shared constraints:** the headless flags, output formats,
model/permission/turn levers, `--bare` isolation, the top-level `hooks` key,
PreToolUse deny/redirect mechanics, `autoCompactEnabled`, the `used_percentage`
input-only formula, the v2.1.132 cumulative→current change, and ccusage's
cache-aware reading are **documented**. The **additive A/B toggle design** (routing
around the `--settings` merge bug) and the **statistical n≥3 protocol** are
**engineering judgment** built on documented primitives. The **transcript path +
`message.usage` schema** are **directly observed**, not formally documented —
version-sensitive. The **byte/4-vs-real fuzz** and **headless non-determinism** are
**honestly flagged limitations**, not claims.

[cli]: https://code.claude.com/docs/en/cli-reference
[hl]: https://code.claude.com/docs/en/headless
[set]: https://code.claude.com/docs/en/settings
[hk]: https://code.claude.com/docs/en/hooks
[pm]: https://code.claude.com/docs/en/permission-modes
[mc]: https://code.claude.com/docs/en/model-config
[stl]: https://code.claude.com/docs/en/statusline
[ccu]: https://github.com/ryoppippi/ccusage
[i11392]: https://github.com/anthropics/claude-code/issues/11392
[i28167]: https://github.com/anthropics/claude-code/issues/28167
[i24589]: https://github.com/anthropics/claude-code/issues/24589
