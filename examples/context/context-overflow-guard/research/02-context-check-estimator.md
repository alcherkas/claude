# Pre-flight context estimator: a `/context-check` slash command

> **Source:** deep-research run for **Option #2** in
> [`../research-prompts.md`](../research-prompts.md). 5 search angles, 19 sources
> fetched, 92 claims extracted, 25 adversarially verified (21 confirmed, 4
> killed → 9 synthesized findings). Current as of **mid-2026** (Claude Code
> v2.1.x, Opus 4.8 / Sonnet 4.6, 200k default / 1M extended context).

**Bottom line.** A `/context-check` pre-flight estimator is **fully buildable
today with documented primitives**, and it complements the Option #1 input-gate
hook: the hook *reactively blocks* one oversized `Read`; this command
*proactively surveys* a subtree you're about to dive into and ranks the
expensive files so you read selectively. Define it as a **skill**
(`.claude/skills/context-check/SKILL.md`; the legacy `.claude/commands/*.md`
form still works), accept a path/glob via `$ARGUMENTS`/`$0`, pre-approve the
estimator with `allowed-tools: Bash(python3 *)`, and inline the script's ranked
report **before Claude reads anything** using the `` !`…` `` bash-execution
syntax — the official "inject dynamic context" mechanism.

The one real engineering tension is **accuracy vs. speed**. The *only*
ground-truth-accurate Claude counter is Anthropic's `count_tokens` API — but it
is network-bound and rate-limited (100 req/min at Tier 1), so it does not scale
to scanning hundreds of files interactively. Every *fast local* scanner in the
ecosystem (repomix, code2prompt, files-to-tokens, repo-tokens-calculator)
counts with **OpenAI's BPE** (`o200k_base`/`cl100k_base`), **not Anthropic's
tokenizer** — so their counts are only approximations for Claude, and have been
observed to diverge from `count_tokens` by ~1.5–2× (community figure). The
pragmatic design: estimate locally with a cheap, zero-dependency heuristic
(byte÷4, same as the Option #1 hook) for the interactive scan, and **calibrate
and prove it once** against `count_tokens` as ground truth.

---

## ⚠️ Refuted claims — do NOT repeat (killed 1–2 in verification)

- **ai-digest does *not* (verifiably) use Anthropic's official tokenizer.** The
  claim that it counts Claude tokens via a real `@anthropic-ai/tokenizer` BPE
  (rather than tiktoken) was **refuted**. Do not assert any tool uses a genuine
  Anthropic local tokenizer — there is no usable public one.
- **ai-digest's "bar-chart / sort-largest-first per-file UX" was refuted.** Use
  **repomix's `--token-count-tree`** as the confirmed prior-art instead.
- **The specific "traffic-light" thresholds were refuted.** The claim that Claude
  Code's context bar turns *red >75% / gold >50% / green <50%* did not survive.
  Traffic-light UX is a fine *design choice for your command*, but do **not** cite
  those percentages as documented Claude Code behavior.
- **"code2prompt has no per-file breakdown" was refuted** (treat its per-file
  behavior as unsettled; don't assert the negative).

---

## (a) How custom slash commands work today — **DOCUMENTED**

**Custom commands have merged into skills** (~Claude Code v2.1.3). A skill at
`.claude/skills/<name>/SKILL.md` and a legacy command at
`.claude/commands/<name>.md` *both* create `/<name>` and work the same way; the
directory name (or filename minus `.md`) becomes the command. Both are Markdown
with YAML frontmatter between `---` markers. Skills are the recommended form and
**take precedence on a name collision**. Existing `.claude/commands/` files keep
working. [[1]](https://code.claude.com/docs/en/slash-commands)
[[2]](https://docs.claude.com/en/docs/claude-code/slash-commands)
[[3]](https://code.claude.com/docs/en/agent-sdk/slash-commands)

| Scope | Path | Availability |
|---|---|---|
| Personal | `~/.claude/skills/<name>/SKILL.md` | all your projects |
| Project | `.claude/skills/<name>/SKILL.md` | this repo (commit it) |

**Arguments** — multiple substitution forms, applied to the skill body before it
reaches Claude: [[1]](https://code.claude.com/docs/en/slash-commands)
[[3]](https://code.claude.com/docs/en/agent-sdk/slash-commands)

| Placeholder | Meaning |
|---|---|
| `$ARGUMENTS` | the entire argument string as typed |
| `$ARGUMENTS[N]` / `$N` | a single arg by **0-based** index (`$0` = first) |
| `$name` | a named arg declared in the `arguments` frontmatter list |

Indexed args use **shell-style quoting**, so a glob with `*` or spaces must be
quoted: `/context-check 'src/**/*.ts'`. Add `argument-hint: [path-or-glob]` for
autocomplete. ⚠️ **VERSION-SENSITIVE:** the index is **0-based (`$0` = first)** in
current Claude Code, but Anthropic's own docs are internally inconsistent on
0- vs 1-based (issue #19355, closed unresolved) and many older blogs say
`$1`=first — verify against your installed version.

**Running scripts — the key capability.** The `` !`<command>` `` syntax runs a
shell command **before the skill content is sent to Claude**; its stdout
*replaces the placeholder*, so Claude receives the rendered data, not the command
— "this is preprocessing, not something Claude executes." For multi-line, open a
fenced block with ```` ```! ````. (Inline `` ! `` only triggers at line start or
after whitespace.) This is exactly how a command shells out to an estimator and
inlines its ranked report. [[1]](https://code.claude.com/docs/en/slash-commands)
[[3]](https://code.claude.com/docs/en/agent-sdk/slash-commands)

**Pre-approving the script.** `allowed-tools: Bash(python3 *)` grants the tool
*without a per-use prompt* while the skill is active (it **grants, it does not
gate** — and a bare `Bash` is rejected with "Missing command filter"). Anthropic's
own `codebase-visualizer` skill uses precisely `allowed-tools: Bash(python3 *)`
to run a bundled Python script — the exact `/context-check` scenario.
[[1]](https://code.claude.com/docs/en/slash-commands)
⚠️ **WORKAROUND ZONE:** open issues (#14956 on v2.0.75, #5598, #3662) report
`allowed-tools` *intermittently failing* to suppress prompts in some
versions/headless modes. Treat prompt-free execution as best-effort, not
guaranteed.

---

## (b) Counting tokens for a subtree: accuracy vs. speed — **DOCUMENTED**

**Ground truth: `count_tokens`.** `POST https://api.anthropic.com/v1/messages/count_tokens`
(SDK `messages.count_tokens`) accepts the same structured inputs as message
creation (system, tools, images, PDFs) and returns `{"input_tokens": N}`. It is
**free** but **rate-limited per minute by tier**, on a limit *independent* of
message creation: [[4]](https://platform.claude.com/docs/en/build-with-claude/token-counting)
[[5]](https://docs.claude.com/en/api/messages-count-tokens)

| Tier | count_tokens RPM |
|---|---|
| 1 | 100 |
| 2 | 2,000 |
| 3 | 4,000 |
| 4 | 8,000 |

Caveats that matter for a pre-flight tool: the count is itself **"an estimate"**
(may differ from billed create-message tokens by a small amount; may include
non-billed system-added tokens; applies no prompt-caching logic), it costs a
**network round-trip per request**, and the **tokenizer is model-dependent**
(newer models — Opus 4.7+/Fable 5 — produce noticeably more tokens than older
ones), so you must pin the target model.
[[4]](https://platform.claude.com/docs/en/build-with-claude/token-counting)
→ Accurate, but **too slow / too rate-limited to scan a large subtree
interactively**. Use it for *validation and calibration*, not the hot path.

**Fast local counters all use the wrong (OpenAI) tokenizer.** There is **no
usable public Anthropic tokenizer library**, so every local scanner approximates
Claude with OpenAI BPE: [[6]](https://github.com/yamadashy/repomix)
[[7]](https://pypi.org/project/code2prompt/)
[[8]](https://github.com/fullmeta-dev/files-to-tokens)
[[9]](https://github.com/WilliamAGH/repo-tokens-calculator)

| Tool | Tokenizer | Default encoding |
|---|---|---|
| repomix | `gpt-tokenizer` | `o200k_base` (GPT-4o) |
| code2prompt | tiktoken | `cl100k_base` (GPT-4/3.5) |
| files-to-tokens | tiktoken, else regex fallback (±10%) | OpenAI |
| repo-tokens-calculator | tiktoken, local, no network/keys | `o200k_base` → `cl100k_base` fallback; labels Claude *"approximate via cl100k_base"* |

⚠️ **COMMUNITY/SECONDARY:** OpenAI-BPE counts have been observed to diverge from
Anthropic `count_tokens` by **~1.57×–2.08×**
([Propel Code, 2025](https://www.propelcode.ai/blog/token-counting-tiktoken-anthropic-gemini-guide-2025)).
Implication: a local tiktoken count is **not reliably closer to Claude reality
than a calibrated byte÷4 heuristic** — and it adds a dependency. For a fast,
zero-dependency pre-flight scan, **byte÷4 (the same heuristic the Option #1 hook
already uses) is a defensible default**, with a one-time calibration factor
derived against `count_tokens` (see Proof, below).

> **The recommended trade-off:** *byte÷4 (calibrated)* for the interactive scan
> → *`count_tokens`* as the offline oracle that proves and tunes it. Skip local
> tiktoken: it's a dependency that buys you a *different* tokenizer's wrong
> answer.

---

## (c) Prior art: how scanners present per-file budgets — **DOCUMENTED**

The confirmed, emulate-this pattern is **repomix's `--token-count-tree [threshold]`**:
a file tree annotated with **per-file and per-directory token counts**, with an
optional numeric threshold to show only items at or above N tokens —
`repomix --token-count-tree 1000` shows only files/dirs ≥1000 tokens, letting you
"focus on larger files." [[6]](https://github.com/yamadashy/repomix)
[[10]](https://repomix.com/guide/command-line-options) Encoding is selectable via
`--token-count-encoding <o200k_base|cl100k_base|…>`. Note repomix's docs provide
**no accuracy comparison against Claude/Anthropic** — so even the canonical
prior art is, for Claude, an unverified approximation.

This is the design to copy: a **ranked tree/table of the expensive files**, not a
single lump total. (Contrast: tools that emit only an overall count force you to
guess *which* file is the problem.)

---

## (d) Good UX for "these N files cost X tokens — read selectively"

`/context-check` is **complementary to the built-in `/context`**, not a
replacement. `/context` shows a *live, authoritative* breakdown of **actual**
loaded usage by category (system prompt, tools, MCP, memory/CLAUDE.md, messages,
free space) as a colored grid with optimization suggestions — the real
accounting *after* loading. `/context-check` answers the *different,
pre-flight* question: *if I were to read this subtree, what would it cost?*
[[11]](https://code.claude.com/docs/en/context-window)
[[12]](https://code.claude.com/docs/en/commands) Anthropic itself stamps its
per-file/per-event token figures as **"illustrative … actual values vary with
your CLAUDE.md size, MCP servers, and file lengths"** — primary-source
confirmation that *any* pre-read estimate is inherently approximate and must be
validated. [[11]](https://code.claude.com/docs/en/context-window)

**UX principles (synthesized; the percentages here are this command's design,
not documented Claude behavior):**

1. **Rank, don't lump.** Sort files descending by token cost; the top 5 usually
   dominate.
2. **Show % of the window** alongside raw tokens, and a **running cumulative**
   so the user sees where the budget tips over.
3. **Traffic-light by cumulative budget**, e.g. 🟢 <40% / 🟡 40–70% / 🔴 >70% of
   the target window. (Thresholds are yours — flag them as design, since the
   real Claude Code thresholds were not verifiable.)
4. **Always end with the next move**, mapped to the same levers the Option #1
   hook nudges toward: `grep -n`/`rg` for the symbol, `Read(file, offset, limit)`
   for a slice, or fan the heavy reading to an **`Explore`/`Task` subagent** so
   the tokens land in *its* context, not yours.

### Output mockup

```
/context-check src/api

Pre-flight estimate · target window 200,000 tokens · model claude-opus-4-8
heuristic: byte÷4 ×1.05 (calibrated)  ·  18 files, 3 skipped (binary/minified)

  TOKENS    %WIN   CUM%   FILE
  ───────────────────────────────────────────────
  24,800    12.4%  12.4%  🔴 src/api/schema.generated.ts   ← generated; grep it
  11,200     5.6%  18.0%  🟡 src/api/client.ts
   8,600     4.3%  22.3%  🟡 src/api/handlers.ts
   3,100     1.6%  23.9%  🟢 src/api/types.ts
   …
  ───────────────────────────────────────────────
  ~58,400 tokens total  ·  29% of window  ·  🟡 read selectively

Suggestions:
  • schema.generated.ts is 12% of the window on its own — don't Read it whole;
    `rg "export (type|interface)" src/api/schema.generated.ts` or read a slice.
  • Reading all 18 files would consume ~29% before any work. Offload the survey
    to an Explore subagent; pull back only the parts you need.
```

---

## The command design

### `.claude/skills/context-check/SKILL.md`

```markdown
---
description: Pre-flight token-cost estimate for files/dirs before you read them.
argument-hint: "[path-or-glob]   (e.g. 'src/**/*.ts')"
allowed-tools: Bash(python3 *)
---

# Pre-flight context estimate for: $ARGUMENTS

The estimate below was computed BEFORE you read anything. Use it to read
selectively — do not blindly `Read` whole files flagged 🔴/🟡.

```!
python3 "$CLAUDE_PROJECT_DIR/.claude/skills/context-check/estimate.py" $ARGUMENTS
```

Based on the ranked estimate:
1. If the total is a large fraction of the window, do NOT read everything inline.
2. For any 🔴 file, prefer `rg`/`grep -n` to locate, then `Read(file, offset, limit)`
   for just the relevant window — or hand the file to an Explore/Task subagent so
   the tokens land in its context, not this one.
3. Read 🟢 files normally; treat 🟡 with care.
```

> **Notes.** `$CLAUDE_PROJECT_DIR` resolves to the repo root, so the script path
> works regardless of cwd. `$ARGUMENTS` is substituted into the `` ! `` command
> at preprocessing; quote globs at the call site. The fenced ```` ```! ```` form
> is the documented multi-line bash block.
> ⚠️ **OPEN (could not verify):** whether a skill-bundled script is reachable via
> a `$CLAUDE_SKILL_DIR`-style var (as the `codebase-visualizer` example implies)
> vs. requiring `$CLAUDE_PROJECT_DIR` + a fixed path. Using `$CLAUDE_PROJECT_DIR`
> as above is the safe, documented choice.

### `estimate.py` (sketch — fast, zero-dependency, fail-soft)

```python
#!/usr/bin/env python3
"""Pre-flight token estimator. byte/4 with a calibration factor; fail-soft."""
import os, sys, glob, math

WINDOW   = int(os.environ.get("CC_CONTEXT_CHECK_WINDOW", "200000"))
CALIB    = float(os.environ.get("CC_CONTEXT_CHECK_CALIB", "1.0"))  # see Proof
BYTES_PER_TOKEN = 4.0
GREEN, YELLOW = 0.40, 0.70  # cumulative-budget thresholds (design choice)

def looks_binary(path):
    try:
        with open(path, "rb") as f:
            chunk = f.read(4096)
        return b"\x00" in chunk
    except OSError:
        return True

def est_tokens(path):
    try:
        size = os.path.getsize(path)
    except OSError:
        return None
    return math.ceil(size / BYTES_PER_TOKEN * CALIB)

def expand(args):
    files = []
    for a in args:
        if os.path.isdir(a):
            for root, _, names in os.walk(a):
                files += [os.path.join(root, n) for n in names]
        else:
            files += glob.glob(a, recursive=True) or [a]
    return sorted(set(p for p in files if os.path.isfile(p)))

def main(argv):
    files = expand(argv or ["."])
    rows, skipped = [], 0
    for p in files:
        if looks_binary(p):
            skipped += 1; continue
        t = est_tokens(p)
        if t is None:
            skipped += 1; continue
        rows.append((t, p))
    rows.sort(reverse=True)
    total = sum(t for t, _ in rows)
    print(f"Pre-flight estimate · window {WINDOW:,} · byte/4 ×{CALIB} (calibrated)")
    print(f"{len(rows)} files, {skipped} skipped (binary/unreadable)\n")
    print(f"  {'TOKENS':>8} {'%WIN':>6} {'CUM%':>6}  FILE")
    cum = 0
    for t, p in rows:
        cum += t
        pct, cpct = t / WINDOW, cum / WINDOW
        light = "🟢" if cpct < GREEN else ("🟡" if cpct < YELLOW else "🔴")
        print(f"  {t:>8,} {pct:>5.1%} {cpct:>5.1%}  {light} {p}")
    tier = "🟢 fine" if total/WINDOW < GREEN else \
           ("🟡 read selectively" if total/WINDOW < YELLOW else "🔴 too much — offload/slice")
    print(f"\n  ~{total:,} tokens total · {total/WINDOW:.0%} of window · {tier}")

if __name__ == "__main__":
    main(sys.argv[1:])
```

Design choices, all deliberate and consistent with Option #1:
- **byte÷4 (same heuristic as the hook)** keeps the two tools agreeing and needs
  no install. `CC_CONTEXT_CHECK_CALIB` lets the proof step inject the measured
  correction factor.
- **Fail-soft:** binary/unreadable files are skipped and counted, never crash.
- **No network in the hot path** — the slow, rate-limited `count_tokens` API is
  reserved for the offline proof.

---

## Proof that the estimate is accurate — **SYNTHESIS / author-derived method**

> This validation *design* is grounded in documented facts (`count_tokens` is the
> closest-to-truth Claude counter; Anthropic calls its own per-file figures
> "illustrative"), but the **specific metrics, thresholds, and corpus are
> author-proposed**, not from a cited source. Run it to *earn* the accuracy claim.

**Oracle:** `count_tokens` with the **target model pinned** (e.g.
`claude-opus-4-8`). Per file, send the file body as a single user message and
read `input_tokens`. [[4]](https://platform.claude.com/docs/en/build-with-claude/token-counting)

**Test corpus** — deliberately diverse, because Anthropic warns counts vary with
content: TypeScript/JS, Python, Markdown, JSON, a **minified bundle**, a
**long-single-line** file, a CSV/data file, and a near-binary — plus a few files
from *this very repo* so the proof reflects real usage.

**Metrics** — per file compute signed error `e = (estimate − truth) / truth`,
then report the **distribution**, not a single average:

| Metric | What it tells you |
|---|---|
| mean / median signed error | systematic **bias** (byte÷4 typically *under*-counts) |
| P95 / P99 absolute error % | worst-case behavior on outliers (minified, long-line) |
| max absolute error % | the pathological file the UX must survive |

**Calibration:** set `CC_CONTEXT_CHECK_CALIB = median(truth/estimate)` to remove
the systematic bias, then re-measure to confirm the median signed error → ~0.
Re-derive when the **target model changes** (the tokenizer is model-dependent).

**Validation harness** (offline; respects the 100 RPM Tier-1 limit):

```python
# proof/validate.py — compare estimate.py against count_tokens (ground truth)
import os, statistics, time, anthropic
from estimate import est_tokens, looks_binary, expand

client = anthropic.Anthropic()
MODEL = os.environ.get("CC_VALIDATE_MODEL", "claude-opus-4-8")

def truth(path):
    body = open(path, encoding="utf-8", errors="replace").read()
    r = client.messages.count_tokens(
        model=MODEL, messages=[{"role": "user", "content": body}])
    return r.input_tokens

errs, ratios = [], []
for p in expand(["test_corpus"]):
    if looks_binary(p):
        continue
    t = truth(p); e = est_tokens(p)
    errs.append((e - t) / t); ratios.append(t / e)
    print(f"{(e-t)/t:+7.1%}  est={e:>6} truth={t:>6}  {p}")
    time.sleep(0.7)                    # ~85 req/min, under the Tier-1 100 RPM cap

a = sorted(abs(x) for x in errs)
print(f"\nmedian signed {statistics.median(errs):+.1%} · "
      f"P95 |err| {a[int(len(a)*0.95)]:.1%} · max |err| {a[-1]:.1%}")
print(f"suggested CC_CONTEXT_CHECK_CALIB = {statistics.median(ratios):.3f}")
```

A green proof is: *"on an N-file diverse corpus, calibrated byte÷4 has median
signed error X% and P95 absolute error Y% vs. `count_tokens` on `claude-opus-4-8`"*
— a defensible, reproducible accuracy claim, exactly the bar the README sets for
Option #1. Pair it with a tiny unit test asserting the **ranking** is stable
(the estimator must put the genuinely-biggest file on top, since the UX is about
*which* file to avoid, not the exact number).

---

## Caveats & version-sensitivity (flag these in any build)

- **Skills-merge (~v2.1.3):** prefer the `SKILL.md` form; legacy
  `.claude/commands/*.md` still works but isn't the recommended shape. *DOCUMENTED.*
- **Argument indexing is 0-based (`$0`=first)** in current Claude Code, but docs
  are inconsistent and older guides disagree — verify on your version. *DOCUMENTED
  but contradictory.*
- **`allowed-tools` prompt-suppression is best-effort:** open bugs report it
  intermittently failing in some versions/headless modes. *WORKAROUND ZONE.*
- **The Claude tokenizer is model-dependent:** both the `count_tokens` oracle and
  the calibration factor must pin the target model and be re-derived when it
  changes. *DOCUMENTED.*
- **`count_tokens` is itself "an estimate"** and `/context` measures
  *already-loaded* usage — a different quantity from a pre-read estimate. "Ground
  truth" here means *closest available reference*, not a billed number. *DOCUMENTED.*
- **The ~1.5–2× tiktoken-vs-Claude divergence** is a single community blog
  figure. *COMMUNITY/SECONDARY — do not treat as exact.*

## Open questions (not answered by available sources)

1. **The actual byte÷4 (and o200k/cl100k) error distribution vs. `count_tokens`
   for Opus 4.8** is unpublished — it must be *measured* (the Proof section) to
   claim accuracy and to set the calibration factor.
2. **Skill-bundled script resolution:** is there a real `$CLAUDE_SKILL_DIR`-type
   var for scripts shipped beside `SKILL.md`, or is `$CLAUDE_PROJECT_DIR` + a
   fixed path the only reliable route?
3. **Practical subtree size under the 100 RPM Tier-1 limit:** can multiple files
   be concatenated into one `count_tokens` message to amortize requests without
   distorting per-file attribution?
4. **Is there any official OpenAI-BPE → Claude mapping factor**, or is per-model
   empirical calibration via `count_tokens` the only sanctioned path? (Evidence
   points to the latter.)

---

## Sources

**Primary (official):**
1. [Slash commands — code.claude.com](https://code.claude.com/docs/en/slash-commands)
2. [Slash commands — docs.claude.com](https://docs.claude.com/en/docs/claude-code/slash-commands)
3. [Agent SDK slash commands](https://code.claude.com/docs/en/agent-sdk/slash-commands)
4. [Token counting — platform.claude.com](https://platform.claude.com/docs/en/build-with-claude/token-counting)
5. [Messages count_tokens API](https://docs.claude.com/en/api/messages-count-tokens)
6. [repomix (GitHub)](https://github.com/yamadashy/repomix) ·
   10. [repomix CLI options](https://repomix.com/guide/command-line-options)
11. [Context window / `/context` — code.claude.com](https://code.claude.com/docs/en/context-window)
12. [Commands reference — code.claude.com](https://code.claude.com/docs/en/commands)

**Secondary / community (flagged inline):**
7. [code2prompt (PyPI)](https://pypi.org/project/code2prompt/) ·
8. [files-to-tokens](https://github.com/fullmeta-dev/files-to-tokens) ·
9. [repo-tokens-calculator](https://github.com/WilliamAGH/repo-tokens-calculator) ·
[Propel Code — token counting guide, 2025](https://www.propelcode.ai/blog/token-counting-tiktoken-anthropic-gemini-guide-2025)
(the ~1.5–2× divergence figure) ·
[tiktoken (OpenAI)](https://github.com/openai/tiktoken)

**Refuted-claim sources (kept for transparency):**
[ai-digest](https://github.com/khromov/ai-digest) (claimed Anthropic tokenizer /
bar-chart UX — both refuted).
