# Prompt caching: paying ~10% to re-read the part of the prompt that didn't change

> **Source:** deep-research run for the prompt-caching prompt (#5) in
> [`../research-prompts.md`](../research-prompts.md). Search angles: (a) cache_control
> breakpoints / minimum cacheable sizes / TTL / pricing, (b) how Claude Code caches
> automatically and what invalidates the prefix, (c) how to verify cache hits from
> the usage fields (transcript JSONL / `/context` / status line / raw API), (d)
> prompt-structuring best practices to maximize reuse. Provenance:
> **sources fetched** → `platform.claude.com/docs/en/build-with-claude/prompt-caching`,
> `platform.claude.com/docs/en/about-claude/pricing`, `code.claude.com/docs/en/prompt-caching`,
> `code.claude.com/docs/en/statusline`, `claude.com/blog/lessons-from-building-claude-code-prompt-caching-is-everything`,
> plus 2026-dated third-party corroboration (mager.co, aicheckerhub.com, technspire.com,
> ofox.ai, finout.io, GitHub `anthropics/claude-code#46829`/#48629) →
> **claims raised** (mechanics, Claude-Code behavior, verification, structuring) →
> **adversarially verified** (3-vote, need 2/3 to kill) → **17 confirmed, 8 killed** →
> synthesized into the findings below. Sources are overwhelmingly **primary**
> (two official Anthropic doc domains). Current as of **mid-2026** (Claude Code
> v2.1.x — docs reference v2.1.86 / v2.1.132 / v2.1.163; models Opus 4.8 / 4.7 / 4.6 /
> 4.5, Sonnet 4.6 / 4.5, Haiku 4.5 / 3.5, Fable 5). The §(c) transcript layout
> and the `message.usage` field shape were **directly observed** on a live v2.1.x
> session (2026-06-23) — that observed record happened to show the **1-hour TTL in
> use** (`ephemeral_1h_input_tokens` populated, `ephemeral_5m_input_tokens: 0`).

> **⚠️ Editor's correction (post-research reconciliation, 2026-06-23).** The
> web-search pass — degraded by API rate-limiting mid-run (8 of 25 verifier votes
> *abstained*, not refuted) — produced a **wrong per-model minimum-cacheable-size
> table** (it claimed Opus 4.8 = 1,024 and invented a "512 / Mythos" tier) and then
> inverted the truth by calling the version-matched first-party source "stale." I
> reconciled §(a)/§(b) against the **bundled `claude-api` skill shipped with this
> exact Claude Code build (v2.1.186)** — the canonical Anthropic quick-reference —
> and corrected: **(1)** the minimum-size table (current Opus family 4.5–4.8 + Haiku
> 4.5 are **4,096**, not 1,024/2,048); **(2)** Proof 2's system-prompt size (it was
> below Opus 4.8's real 4,096 minimum and would have silently cached *nothing*);
> **(3)** the per-parameter invalidation hierarchy and the break-even rule, both of
> which the rate-limited run wrongly "killed" but are in fact documented. Where this
> file now disagrees with the original web-research votes, **the version-matched
> bundled skill governs** and the conflict is flagged inline.

**Bottom line.** Prompt caching lets the API **re-read an unchanged prompt prefix at
~0.1× the base input price** instead of reprocessing it every turn — you pay a
one-time **write premium** (1.25× for the 5-minute cache, 2× for the 1-hour cache)
to plant the cache, then bank **10%-priced reads** for as long as the prefix stays
byte-identical and the TTL keeps getting refreshed. The cache is a **strict prefix
match in the order `tools → system → messages`**: a change at any level invalidates
that level and everything after it, and a hit requires **100% identical** content up
to and including the cached block. **Claude Code turns this on automatically with no
config** — it re-sends the full context each turn (system prompt + tool defs, then
project context like CLAUDE.md/memory, then the conversation), appends new content at
the end, and reuses the unchanged prefix; the only knobs are *opt-out* / TTL tuning,
never *opt-in*. You **prove** caching is happening with real numbers from the two
usage fields the API reports on every response — `cache_creation_input_tokens`
(tokens written) and `cache_read_input_tokens` (tokens served from cache) — split by
TTL into `cache_creation.ephemeral_5m_input_tokens` / `ephemeral_1h_input_tokens`.
In a live Claude Code session you read them turn-over-turn from the transcript JSONL
(`~/.claude/projects/<slug>/<session-id>.jsonl`, `message.usage`), from `/context`,
or from the status-line `context_window.current_usage`; **rising cache_read across
turns while creation stays low is the proof.** Via the raw Messages API, two identical
requests show **creation on the first, read on the second.** The classic ways to
silently break it: editing earlier (cached) prompt content instead of appending,
changing tool definitions (which invalidates *everything*), or letting the 5-minute
TTL lapse between turns.

---

## (a) The current mechanics — breakpoints, minimums, TTL, pricing

**Cache breakpoints (up to 4).** You mark cacheable content by attaching
`cache_control: {"type": "ephemeral"}` to a content block. A request may define
**up to 4 cache breakpoints**, so different sections that change at different
frequencies can be cached separately ("tools rarely change, but context updates
daily"). Place the breakpoint **"on the last block whose prefix is identical across
the requests you want to share a cache"** — verbatim from the docs. Everything from
the start of the request up to and including that block becomes the cached prefix.
If **all 4 explicit block-level breakpoints already exist, the API returns a 400
error** (no slots left for automatic caching); automatic caching, when used together
with explicit breakpoints, consumes one of the 4 slots
([prompt-caching](https://platform.claude.com/docs/en/build-with-claude/prompt-caching),
3-0). *Confidence: high.*

**Minimum cacheable size is model-specific.** Prompts shorter than the model's
minimum are **processed without caching, and no error is returned**. The full
per-model tier table (from the version-matched bundled `claude-api` skill, v2.1.186):

| Minimum cacheable tokens | Models |
|---|---|
| **1,024** | **Sonnet 4.5**, Sonnet 4.1, Sonnet 4, Sonnet 3.7 |
| **2,048** | **Sonnet 4.6**, **Fable 5**, **Haiku 3.5**, Haiku 3 |
| **4,096** | **Opus 4.8**, **Opus 4.7**, Opus 4.6, Opus 4.5, **Haiku 4.5** |

**⚠️ Corrected.** The prompt's "1024 vs 2048" framing is **wrong for the current
flagships**: the entire current **Opus family (4.5–4.8) and Haiku 4.5 sit at the
4,096 tier**, not 1,024/2,048. The authoritative, version-matched source is the
**bundled `claude-api` skill** (`claude-api/shared/prompt-caching.md`, shipped with
Claude Code **v2.1.186**), whose table the values above reproduce verbatim; its own
worked example is decisive: *"A 3K-token prompt caches on Sonnet 4.5 and Fable 5 but
silently won't on Opus 4.8."* The original web-research pass reported lower numbers
(Opus 4.8 = 1,024, plus a fabricated "512 / Mythos" tier) — those votes were taken
while the verifier was rate-limited and are **superseded** here. Prompts below the
model minimum are **processed without caching, no error** — you detect it because
**both** `cache_creation_input_tokens` *and* `cache_read_input_tokens` read 0
(`cache_creation_input_tokens: 0` is the tell). **Amazon Bedrock** additionally
pins several models to 4,096 per-checkpoint. *(Source: bundled `claude-api` skill
v2.1.186 — authoritative/version-matched. Confidence: high; supersedes the
rate-limited 3-0 web vote.)*

**TTL: 5-minute default, 1-hour optional.** By default the cache has a **5-minute
lifetime**, and **"the cache is refreshed for no additional cost each time the cached
content is used"** (each hit slides the expiry window forward; the refresh itself is
free — though the read still costs the 0.1× read rate). To get the extended cache,
include `ttl` in the `cache_control`:

```json
"cache_control": { "type": "ephemeral", "ttl": "1h" }
```

([prompt-caching](https://platform.claude.com/docs/en/build-with-claude/prompt-caching),
3-0). *Confidence: high.*

**Pricing multipliers (relative to base input price).** Confirmed verbatim on **two
independent primary pages** (the prompt-caching doc *and* the pricing page), and
internally consistent with the per-model dollar table:

| Operation | Multiplier | Opus 4.8 worked example ($5/MTok base) |
|---|---|---|
| **5-minute cache write** | **1.25×** | $6.25 / MTok |
| **1-hour cache write** | **2×** | $10 / MTok |
| **Cache read (hit / refresh)** | **0.1×** | $0.50 / MTok |
| Base input (uncached) | 1× | $5 / MTok |

The math checks out across models (Sonnet 4.6 $3 → $3.75 / $6 / $0.30; Haiku 4.5 $1
→ $1.25 / $2 / $0.10). One non-default nuance: for Opus 4.6+ a `us` data-residency
flag (`inference_geo`) adds a 1.1× multiplier on top — the table above is standard
global pricing
([prompt-caching](https://platform.claude.com/docs/en/build-with-claude/prompt-caching)
+ [pricing](https://platform.claude.com/docs/en/about-claude/pricing), 3-0).
*Confidence: high.*

**Break-even (corrected — this rule is documented, not "over-simplified").** The
web run *killed* the break-even claim (vote 0-0), but the version-matched bundled
`claude-api` skill states it outright with the arithmetic: *"with 5-minute TTL, two
requests break even (1.25× + 0.1× = 1.35× vs 2× uncached); with 1-hour TTL, you need
at least three requests (2× + 0.2× = 2.2× vs 3× uncached)."* So the rule of thumb
holds: the **5-minute** cache pays for itself **after the first read** (the 2nd
request), the **1-hour** cache **after the second read** (the 3rd request). The
doubled write cost is exactly why the 1-hour TTL needs more reads to amortize —
use it only when traffic has gaps longer than 5 minutes. *(Source: bundled
`claude-api` skill v2.1.186. Confidence: high; supersedes the rate-limited 0-0
vote.)*

---

## (b) How Claude Code uses prompt caching automatically

**It's on by default, no config required.** *"Claude Code manages prompt caching
automatically"* and *"handles prompt caching for you, unless you disable it."* The
only configuration is for **opting out or tuning the TTL** — `DISABLE_PROMPT_CACHING`,
`ENABLE_PROMPT_CACHING_1H`, `FORCE_PROMPT_CACHING_5M` — never to *enable* it. The
mechanism follows from the API being **stateless**: *"The model doesn't remember
anything between requests, so Claude Code re-sends the full context: the system
prompt, your project context, every prior message and tool result, and your new
message. New content is appended at the end, which means most of each request is
identical to the one before it."* The API *"caches by matching the start of each
request, called the prefix… On a normal turn, the prefix is the entire previous
request and only the latest exchange is new"*
([code.claude.com/prompt-caching](https://code.claude.com/docs/en/prompt-caching),
3-0). *Confidence: high — documented.*

**What sits in the stable cached prefix (three layers).** Claude Code orders each
request so rarely-changing content comes first, forming three cache layers:

| Layer (in prefix order) | Contents | Changes when |
|---|---|---|
| **System prompt** | Core instructions, **tool definitions**, output style | Tool defs change; system prompt changes |
| **Project context** | **CLAUDE.md**, auto memory, unscoped rules | Session starts, or after `/clear` or `/compact` |
| **Conversation** | Your messages, Claude's responses, tool results | Every turn (this is what grows) |

Built-in tool definitions load **into the system-prompt layer**. *"The match is
exact, so a change anywhere in the prefix recomputes everything after it. There is no
per-file or per-segment caching."* Therefore *"A change to the conversation layer
leaves the system prompt and project context cached. A change to the system prompt
invalidates everything, because all later content now sits behind a different prefix"*
([code.claude.com/prompt-caching](https://code.claude.com/docs/en/prompt-caching),
3-0). *Confidence: high — documented.*

**What invalidates the cache.** The byte-order is `tools → system → messages` and
invalidation **cascades downward** — a change at one level invalidates that level and
all subsequent levels ([prompt-caching](https://platform.claude.com/docs/en/build-with-claude/prompt-caching),
3-0). Concretely:

- **Tool definition change** (names, descriptions, parameters) → invalidates
  **everything** (tools + system + messages). *(Documented.)*
- **System-prompt / output-style change** → invalidates system + project context +
  conversation. *(Documented.)*
- **Any change earlier in the message history** (editing/rewriting a past turn rather
  than appending) → invalidates from that point on. This is the direct consequence of
  exact prefix matching. *(Documented mechanism; the "editing earlier content" framing
  is a reasoned application of it.)*
- **5-minute TTL expiring between turns** → the prefix falls out of cache, so the next
  turn re-pays a cache **write** instead of a read. *(Reasoned implication of the
  documented 5-min default + refresh-on-use behavior — if you pause longer than the
  TTL between turns, you lose the warm cache.)*

**The CLAUDE.md mid-session subtlety (documented, counter-intuitive).** Editing your
**project-root or user-level CLAUDE.md mid-session does *not* invalidate the cache —
but the edit also doesn't apply**: those files are *"read once at session start and
held in memory… Claude keeps working with the version that was loaded at session
start. The new content loads on the next `/clear`, `/compact`, or restart"*
([code.claude.com/prompt-caching](https://code.claude.com/docs/en/prompt-caching),
3-0). So you neither break the cache nor get the new content until a reload boundary.
(Scope note: this is **project-root/user-level** CLAUDE.md only — nested/subdirectory
CLAUDE.md and `paths:`-frontmatter rules load on demand and *do* pick up edits; one
community source notes opening the file via `/memory` also reloads it.) *Confidence:
high — documented.*

**The per-parameter invalidation hierarchy (corrected — documented, not "unverified").**
The web run *killed* the per-parameter table (vote 0-0, rate-limited abstains), but
the version-matched bundled `claude-api` skill publishes it explicitly. Changes
invalidate only **their own tier and below** (✅ = that tier's cache survives, ❌ =
rebuilt):

| Change | Tools | System | Messages |
|---|:---:|:---:|:---:|
| **Tool definitions** (add / remove / reorder) | ❌ | ❌ | ❌ |
| **Model switch** (caches are model-scoped) | ❌ | ❌ | ❌ |
| `speed`, web-search, citations toggle | ✅ | ❌ | ❌ |
| **System-prompt content** | ✅ | ❌ | ❌ |
| `tool_choice`, images present/absent, `thinking` enable/disable | ✅ | ✅ | ❌ |
| **Message content** (a new turn) | ✅ | ✅ | ❌ |

The practical implication, verbatim from the skill: *"you can change `tool_choice`
per-request or toggle `thinking` without losing the tools+system cache… only
tool-definition and model changes force a full rebuild."* This **supersedes** the
report's earlier "per-parameter specifics are unverified" hedge. *(Source: bundled
`claude-api` skill v2.1.186 — authoritative/version-matched. Confidence: high.)*

**Two more silent-miss mechanics the skill documents (both acutely relevant to a
context-overflow guard, since agentic loops trigger them):**

- **20-block lookback window.** Each breakpoint walks backward **at most 20 content
  blocks** to find a prior cache entry. A single turn that appends **>20 blocks**
  (common when one turn fires many `tool_use`/`tool_result` pairs — exactly what a
  heavy agentic step does) pushes the previous cache out of reach and the next
  request **silently misses**. Fix: place an intermediate breakpoint roughly every
  ~15 blocks in long turns. *(Bundled skill v2.1.186.)*
- **Concurrent-request timing.** A cache entry is readable only **after the first
  response begins streaming**. Fire N identical requests in parallel and **all N pay
  full price** — none can read what the others are still writing. For fan-out: send
  1, await its first streamed token, then fire the rest. *(Bundled skill v2.1.186.)*

---

## (c) How to verify cache hits — the usage fields

Every Messages-API response (and every Claude Code assistant turn) reports a `usage`
object. The cache-relevant fields:

| Field | Meaning | Billed at |
|---|---|---|
| `input_tokens` | Uncached input tokens — those **after the last cache breakpoint** | base input (1×) |
| `cache_creation_input_tokens` | Tokens **written** to the cache creating a new entry (this turn) | write rate (1.25× / 2×) |
| `cache_read_input_tokens` | Tokens **retrieved** from cache for this request | read rate (0.1×) |
| `cache_creation.ephemeral_5m_input_tokens` | The 5-minute-TTL portion of the write | 1.25× |
| `cache_creation.ephemeral_1h_input_tokens` | The 1-hour-TTL portion of the write | 2× |

`cache_creation_input_tokens` **equals the sum** of the two `ephemeral_*` values
(the docs' example: `248 = 148 + 100`). If **both** `cache_creation_input_tokens` and
`cache_read_input_tokens` are 0, the prompt was **not cached** (typically: below the
model minimum)
([prompt-caching](https://platform.claude.com/docs/en/build-with-claude/prompt-caching),
3-0). *Confidence: high.*

**The interpretation rule (from the Claude Code doc, verbatim):** *"A high
read-to-creation ratio means caching is working well. If creation stays high turn
after turn, something is changing in your prefix"*
([code.claude.com/prompt-caching](https://code.claude.com/docs/en/prompt-caching),
3-0). *Confidence: high.*

**Three places to read these in Claude Code:**

1. **Transcript JSONL** — `~/.claude/projects/<project-slug>/<session-id>.jsonl`;
   each `assistant` record carries `message.usage`. **Directly observed shape**
   (live v2.1.x session, 2026-06-23 — note this one is on the **1-hour** cache):
   ```json
   { "input_tokens": 3015,
     "cache_creation_input_tokens": 3639,
     "cache_read_input_tokens": 15840,
     "cache_creation": { "ephemeral_5m_input_tokens": 0, "ephemeral_1h_input_tokens": 3639 },
     "output_tokens": 249,
     "service_tier": "standard" }
   ```
   Across turns you'll see `cache_read_input_tokens` **rise** (the prefix grows and
   keeps getting re-read at 0.1×) while `cache_creation_input_tokens` stays **small**
   (only the newly-appended tail is written) — that's the proof. *(Field names and
   on-disk layout: directly observed, not formally documented — version-sensitive.)*
2. **`/context`** (interactive) — human-readable current context-window breakdown;
   eyeball it before/after a turn. *(Interactive only; no documented headless
   equivalent.)*
3. **Status line JSON** — `context_window.current_usage` exposes *"the same token
   counts"* the Messages API returns, broken out per component:
   `input_tokens`, `output_tokens`, `cache_creation_input_tokens`,
   `cache_read_input_tokens` — *"use this when you need cache hits separate from fresh
   input."* Example: `{input_tokens:8500, output_tokens:1200,
   cache_creation_input_tokens:5000, cache_read_input_tokens:2000}`
   ([code.claude.com/statusline](https://code.claude.com/docs/en/statusline), 3-0).
   *(Version note: `total_input_tokens`/`total_output_tokens` semantics changed in
   v2.1.132, but the per-component `current_usage` fields are unaffected.)* *Confidence:
   high.*

**Via the raw Messages API:** the same `usage` object appears on the response —
first identical request shows `cache_creation_input_tokens > 0` (the write), the
second shows `cache_read_input_tokens > 0` with `cache_creation_input_tokens` back to
0 (the read). See the proof script in the recipe below. *Confidence: high.*

---

## (d) Prompt-structuring best practices to maximize reuse

The governing principle, stated verbatim in Anthropic's engineering blog: **"Static
content first, dynamic content last."** Because caching is a prefix match, *anything
that varies must live after the last breakpoint* — editing earlier content destroys
the downstream cache, while appending preserves it.

1. **Order: `tools → system → messages`.** Put the most stable, globally-shared
   content (tool definitions, system instructions) at the very front; project context
   (CLAUDE.md) next; the volatile conversation last. The blog's hierarchy: static
   system prompt + tools (globally cached) → CLAUDE.md (cached within a project) →
   session context (cached within a session) → conversation messages — *"This way we
   maximize how many sessions share cache hits"*
   ([claude.com blog](https://claude.com/blog/lessons-from-building-claude-code-prompt-caching-is-everything),
   2-0). *Confidence: high (primary blog + corroborating API docs).*
2. **Keep the prefix byte-identical.** A hit requires *"100% identical prompt segments…
   up to and including the block marked with cache control"* — a **single character**
   difference breaks the match
   ([prompt-caching](https://platform.claude.com/docs/en/build-with-claude/prompt-caching),
   3-0). *Confidence: high.*
3. **Put the breakpoint on the last stable block.** Place `cache_control` on the last
   block whose prefix is identical across the requests you want to share a cache. Use
   multiple breakpoints (≤4) when sections change at different cadences. *Confidence:
   high.*
4. **Pass new info as a *new message*, not an edit.** Anthropic's own technique:
   *"Consider if you can pass in this information via messages in the agent's next turn
   instead. In Claude Code, we add a `<system-reminder>` tag in the next user message or
   tool result with the updated information for the model, which helps preserve the
   cache."* ([claude.com blog](https://claude.com/blog/lessons-from-building-claude-code-prompt-caching-is-everything),
   2-0). *Confidence: high.*

**Pitfalls that silently break caching** (no error — you only see it as
`cache_creation` staying high turn after turn):

- **A non-deterministic stable prefix** — a precise timestamp / UUID baked into the
  system prompt, or non-deterministic JSON serialization (`json.dumps` without
  `sort_keys=True`, iterating a `set`). **Documented** in the bundled skill's "Silent
  invalidators" table: *"`datetime.now()` … in system prompt → Prefix changes every
  request"*; *"Non-deterministic serialization → prefix bytes differ."* Serialize tool
  definitions deterministically (sort by name). *(Bundled skill v2.1.186 — corrects the
  rate-limited 0-0 vote; this is documented, not merely "engineering judgment.")*
- **Mutating a tool definition mid-session** (add / remove / **reorder**) → invalidates
  the **entire** cache. **Documented verbatim:** *"Tools render at position 0; adding,
  removing, or reordering a tool invalidates the entire cache."* Same for a **model
  switch** (caches are model-scoped). *(Bundled skill v2.1.186 — corrects the 0-0 vote.)*
- **Forks / sub-agents that rebuild the prefix** — a side call (summarization,
  compaction, a sub-agent) that reconstructs `system`/`tools`/`model` with *any*
  difference misses the parent's cache entirely. Copy the parent's `system`, `tools`,
  and `model` **verbatim**, then append fork-specific content last. *(Bundled skill
  v2.1.186 — pairs directly with the offloading guard in [`04-subagent-offloading.md`](04-subagent-offloading.md).)*
- **Editing earlier conversation turns** instead of appending → invalidates from that
  point.
- **Falling below the model minimum** → silently no caching, both fields 0.
- **Letting the 5-minute TTL lapse** between turns → next turn re-writes instead of
  reads. Use the 1-hour TTL for sessions with long pauses.

---

## Recipe + how to PROVE it

### Proof 1 — live Claude Code session (transcript JSONL, turn-over-turn)

No setup beyond a running Claude Code session. Send **two or three turns** in the same
session (e.g. ask a question, then a short follow-up), then dump `message.usage` per
assistant turn and watch `cache_read_input_tokens` climb while `cache_creation_input_tokens`
stays small.

```bash
# 1. Find the most-recent transcript for THIS project (slug = cwd with / -> -)
SLUG=$(echo "$PWD" | sed 's#/#-#g')
F=$(ls -t ~/.claude/projects/"$SLUG"/*.jsonl | head -1)
echo "transcript: $F"

# 2. Print per-assistant-turn cache usage in order
python3 - "$F" <<'PY'
import json, sys
turn = 0
for line in open(sys.argv[1]):
    try: o = json.loads(line)
    except: continue
    if o.get("type") != "assistant": continue
    u = (o.get("message") or {}).get("usage")
    if not u: continue
    turn += 1
    cc = u.get("cache_creation_input_tokens", 0)
    cr = u.get("cache_read_input_tokens", 0)
    inp = u.get("input_tokens", 0)
    ratio = (cr / cc) if cc else float("inf")
    print(f"turn {turn:2d}: input={inp:6d}  cache_create={cc:6d}  "
          f"cache_read={cr:7d}  read/create={ratio:.1f}")
PY
```

**Expected shape (the thing that proves it):**

```
turn  1: input=  3015  cache_create=  9500  cache_read=      0   read/create=0.0
turn  2: input=  3200  cache_create=  1800  cache_read=   9500   read/create=5.3
turn  3: input=  3100  cache_create=  1500  cache_read=  11300   read/create=7.5
```

Turn 1 writes the prefix (`cache_read=0`, big `cache_create`). Turn 2+ **read** the
prior prefix (`cache_read` jumps to roughly the previous turn's prefix size) and only
**write** the small newly-appended tail. **Rising `cache_read` + small steady
`cache_create` = caching confirmed.** If `cache_create` stays large every turn,
something in your prefix is changing (a tool def, the system prompt, or you edited an
earlier turn). Cross-check live with `/context` (interactive) or the status-line
`context_window.current_usage` fields.

### Proof 2 — raw Anthropic Messages API (two identical requests)

Minimal script: build a system prompt long enough to clear the model's minimum
cacheable size — **4,096 tokens for Opus 4.8** (the script's default model; *not*
1,024 — see the corrected §(a) table), or pick a model with a lower minimum
(Sonnet 4.5 = 1,024, Sonnet 4.6 = 2,048). Mark it with `cache_control`, fire the
**same request twice**, and read `usage`. (Requires `pip install anthropic` and
`ANTHROPIC_API_KEY`.) **If you under-size the system prompt, both cache fields stay
0 and the proof silently fails — that's the #1 gotcha this script is sized to avoid.**

```python
import os, anthropic
client = anthropic.Anthropic(api_key=os.environ["ANTHROPIC_API_KEY"])

# A system block well over the 4,096-token min for Opus 4.8.
# "You are a precise assistant. " ≈ 6 tokens; ×1200 ≈ 7,200 tokens — comfortably
# above 4,096. (Replace with your real, STABLE system text. NOTE: a ~3K-token prompt
# would silently NOT cache on Opus 4.8 — it's below the 4,096 minimum.)
BIG_SYSTEM = ("You are a precise assistant. " * 1200)

def ask():
    r = client.messages.create(
        model="claude-opus-4-8",                 # 4,096-token min (NOT 1,024)
        max_tokens=16,
        system=[{
            "type": "text",
            "text": BIG_SYSTEM,
            "cache_control": {"type": "ephemeral"},   # 5-min default; add "ttl":"1h" for 1-hour
        }],
        messages=[{"role": "user", "content": "Reply with the single word: ok"}],
    )
    u = r.usage
    print(f"input={u.input_tokens}  "
          f"cache_create={u.cache_creation_input_tokens}  "
          f"cache_read={u.cache_read_input_tokens}")

print("request 1:"); ask()     # expect cache_create > 0, cache_read = 0
print("request 2:"); ask()     # expect cache_create = 0, cache_read > 0  (within 5 min)
```

**Expected output:**

```
request 1: input=12  cache_create=7204  cache_read=0
request 2: input=12  cache_create=0     cache_read=7204
```

(Your exact `cache_create`/`cache_read` will be the real token count of `BIG_SYSTEM`
— ~7,200 here — not a round number; the *shape* is what matters.) Request 1 **writes**
the cache (`cache_creation_input_tokens > 0`, billed 1.25×). Request 2, issued within
the 5-minute TTL with a **byte-identical** system prefix, **reads** it
(`cache_read_input_tokens > 0`, billed 0.1×; `cache_creation` back to 0).
That swap — creation on the first, read on the second — *is* the proof. To prove the
TTL split, add `"ttl": "1h"` to `cache_control` and inspect
`r.usage.cache_creation.ephemeral_1h_input_tokens` (it will carry the write instead of
`ephemeral_5m_input_tokens`). To prove the **minimum-size** behavior, shrink
`BIG_SYSTEM` below the model minimum and observe **both** cache fields read 0 with **no
error** — caching silently skipped.

> **The two proofs are complementary:** Proof 1 shows caching working **automatically,
> for free, inside a real Claude Code session** (you write no `cache_control` — Claude
> Code does); Proof 2 shows the **underlying API mechanic** you control directly, with
> the write→read transition isolated to two identical calls.

---

## Version sensitivity & open questions

**Version-sensitive (scoped to mid-2026 / Claude Code v2.1.x):**

- **Model minimums shift with the model lineup** (1,024 / 2,048 / 4,096 tiers).
  Re-check the table for your exact model — **do not assume 1,024**; the current Opus
  family (4.5–4.8) and Haiku 4.5 are **4,096**. **Bedrock** pins several to 4,096.
- **Source-of-truth note (corrected).** The **bundled `claude-api` skill**
  (`shared/prompt-caching.md`, shipped with the running v2.1.186 build) is the
  **authoritative, version-matched** reference for the minimums and the invalidation
  hierarchy — *it is not stale.* The original web-research pass had this backwards
  (it claimed the skill was stale and Opus 4.8 = 1,024); that was a rate-limited
  misread and has been reversed throughout this file. If a future live doc and the
  bundled skill ever genuinely diverge, prefer whichever matches your **installed**
  Claude Code version.
- **Whether Claude Code requests the 5-min or 1-hour TTL internally is contested.**
  GitHub issue `anthropics/claude-code#46829` (Mar 2026) disputes Claude Code's
  *internal choice* of TTL — orthogonal to the API mechanics, but it means you can't
  assume which TTL a given session uses without inspecting `cache_creation.ephemeral_*`
  (our observed 2026-06-23 session was on **1h**). The `ENABLE_PROMPT_CACHING_1H` /
  `FORCE_PROMPT_CACHING_5M` env vars exist to control it.
- **Status-line token semantics changed at v2.1.132** (`total_*` fields); the
  per-component `current_usage` cache fields are stable.
- **Transcript `message.usage` field names / on-disk JSONL layout** in §(c) are
  **directly observed, not documented** — re-verify on your version.
- **A Feb 5, 2026 change introduced workspace-level cache isolation** — it does **not**
  change the breakpoint count, placement rule, or pricing, but it affects *who* shares
  a cache.

**"Killed" votes that were actually rate-limit artifacts — now re-confirmed.** The
8 claims the web run "killed" all scored **0-0 (3 abstain)** — the verifier agents
hit `Server is temporarily limiting requests` and *abstained*, which is **not a
refutation**. Cross-checked against the version-matched bundled `claude-api` skill,
**most were true** and have been restored into the body above:

- *"Caching pays off after one read (5m) / two reads (1h)"* — **re-confirmed**; the
  bundled skill documents the exact break-even arithmetic (see §(a)).
- The per-parameter invalidation table (`tool_choice`, `thinking`, images,
  web-search, citations, system content, tool defs, model) — **re-confirmed**; the
  bundled skill publishes the full hierarchy (see §(b)).
- *"Add/remove a tool invalidates the whole conversation"* and *"timestamp /
  non-deterministic prefix silently breaks caching"* — **re-confirmed/documented**;
  bundled skill, "Don't change tools mid-conversation" + the "Silent invalidators"
  table (see §(d)).
- *"Two ways to enable caching: top-level auto `cache_control` vs per-block explicit
  breakpoints"* — **substantially confirmed**: the bundled skill states *"Top-level
  `cache_control` on `messages.create()` auto-places on the last cacheable block."*
  So both modes exist; only the precise marketing-style "two ways" wording was never
  the point.

*(Genuine non-findings remain genuine: nothing here was refuted on the merits — the
only real residual uncertainty is the contested default-TTL question below.)*

**Open questions:**

1. **Which TTL does Claude Code request by default in v2.1.x**, and under what
   conditions does it auto-promote to 1h? (Disputed in #46829; observed session showed
   1h — but n=1.)
2. ~~Exact per-parameter invalidation table~~ — **resolved** via the bundled
   `claude-api` skill v2.1.186 (`tool_choice` / `thinking` / images invalidate
   `messages` only; web-search / citations invalidate `system` + `messages`; only
   tool-definition or model changes force a full rebuild). See §(b).
3. **Stability of the transcript `message.usage` schema and JSONL path layout**
   across Claude Code versions (observed, not documented).
4. **Headless `/context` equivalent** — is there a documented non-interactive way to
   read current context-window cache usage other than parsing the JSONL / status line?

---

## Sources

**Authoritative / version-matched (governs on conflict):**
- **Bundled `claude-api` skill — `claude-api/shared/prompt-caching.md`, shipped with
  Claude Code v2.1.186** (`/private/tmp/claude-501/bundled-skills/2.1.186/.../claude-api/shared/prompt-caching.md`;
  upstream `github.com/anthropics/skills/blob/main/skills/claude-api/shared/prompt-caching.md`).
  The decisive source for the **per-model minimum-cacheable sizes** (Opus 4.5–4.8 +
  Haiku 4.5 = 4,096; Sonnet 4.6/Fable 5/Haiku 3.5 = 2,048; Sonnet 4.5 = 1,024), the
  **per-parameter invalidation hierarchy**, the **break-even arithmetic**, the
  **20-block lookback window**, **concurrent-request timing**, **fork prefix reuse**,
  and the `max_tokens: 0` **pre-warming** pattern. Used to **correct** the web-research
  pass wherever they conflicted (minimums, invalidation table, break-even).

**Primary (Anthropic) — documented:**
- [`platform.claude.com/docs/en/build-with-claude/prompt-caching`](https://platform.claude.com/docs/en/build-with-claude/prompt-caching)
  (canonical redirect target of `docs.claude.com/.../prompt-caching`) — breakpoints
  (≤4, placement, 400 on overflow), per-model minimums *(but for the actual values
  trust the bundled skill above — the web read of this table was wrong)*, TTL syntax +
  5-min default + free refresh, the 1.25× / 2× / 0.1× multipliers, the `usage` field definitions and
  the `cache_creation.ephemeral_5m/1h` split, the `tools → system → messages` hierarchy
  + cascade + "100% identical prefix" rule.
- [`platform.claude.com/docs/en/about-claude/pricing`](https://platform.claude.com/docs/en/about-claude/pricing)
  — the three-multiplier pricing table + per-model dollar amounts (independent
  confirmation).
- [`code.claude.com/docs/en/prompt-caching`](https://code.claude.com/docs/en/prompt-caching)
  — Claude Code auto-caching (on by default, full-context resend, append-at-end), the
  three cache layers + contents, exact-prefix invalidation, the CLAUDE.md mid-session
  behavior, the `cache_creation`/`cache_read` interpretation rule.
- [`code.claude.com/docs/en/statusline`](https://code.claude.com/docs/en/statusline)
  — `context_window.current_usage` fields = the same Messages-API usage fields.
- [`claude.com/blog/lessons-from-building-claude-code-prompt-caching-is-everything`](https://claude.com/blog/lessons-from-building-claude-code-prompt-caching-is-everything)
  — "static first, dynamic last," the project/session/global cache hierarchy, the
  `<system-reminder>`-in-next-message technique.

**Community / corroborating (directional, 2026-dated):** mager.co (2026-04-29),
aicheckerhub.com, technspire.com, gu-log, finout.io, ofox.ai, MiniMax
Anthropic-compatible API docs, Amazon Bedrock prompt-caching docs (Bedrock-specific
minimum overrides). GitHub `anthropics/claude-code#46829` (TTL-choice dispute —
*workaround/contested*).

**Directly observed (not documented — version-sensitive):** the `message.usage` shape
and `~/.claude/projects/<slug>/<session-id>.jsonl` layout in §(c), inspected on a live
Claude Code v2.1.x session on 2026-06-23 (the observed record was on the **1-hour TTL**).

**Quality split per the shared constraints:** the breakpoint rules, TTL syntax/default,
pricing multipliers, usage-field semantics, the `tools→system→messages` cascade, Claude
Code's auto-caching + three layers + CLAUDE.md-mid-session behavior, the status-line
fields, and "static-first / pass-new-info-as-a-message" are **documented**. The
**per-model minimums**, the **per-parameter invalidation table**, the **break-even
arithmetic**, the **20-block lookback**, **concurrent-request timing**, and the
**timestamp/tool-order silent-invalidator** items are **documented in the
version-matched bundled `claude-api` skill** — and were used to **correct** the
web-research pass, which got the minimums wrong (it had Opus 4.8 = 1,024; the truth is
4,096) and wrongly "killed" the invalidation/break-even claims under rate-limiting. The
only genuinely **contested** item is *which TTL Claude Code requests by default*
(GitHub #46829; observed n=1 on 1h). The transcript JSONL field names and on-disk
layout in §(c) are **directly observed**, not formally documented — version-sensitive.

> **Provenance note.** Original web run: 17 sources → 84 claims → 25 verified → "17
> confirmed, 8 killed." But the 8 "kills" were all `0-0 (3 abstain)` rate-limit
> artifacts, not refutations, and the surviving minimum-size table was confabulated.
> A post-run reconciliation against the **version-matched bundled `claude-api` skill
> (v2.1.186)** corrected the minimums + Proof 2 sizing and restored the wrongly-killed
> invalidation/break-even findings. Net: the doc-level mechanics were always solid;
> the *numbers* needed the authoritative local source to get right.
