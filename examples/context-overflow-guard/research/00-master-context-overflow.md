# Context-window overflow in Claude Code: causes, detection, and guardrails

> **Source:** deep-research run for the ⭐ master prompt in
> [`../research-prompts.md`](../research-prompts.md). 5 search angles, 20 sources
> fetched, 100 claims extracted, 25 adversarially verified (23 confirmed, 2
> killed → 14 synthesized findings). Current as of **mid-2026** (Claude Code
> v2.1.163–v2.1.174, Opus 4.8, Sonnet 4.6, 1M context).

**Bottom line.** Overflow is managed by a *layered* defense, not one feature.
Order of impact: **prevent/curate (cheap, lossless) → offload → reuse (caching)
→ react/compact (lossy, last resort).** Prompt caching is the highest-leverage,
zero-effort default (automatic, 90% read discount); compaction is the survival
mechanism you reach for last because it throws information away. A key structural
tension runs through everything: **any technique that trims or clears context
invalidates prompt-cache prefixes** — so trimming and caching pull against each
other.

**Scope trap (flagged throughout).** Several of the hardest numbers (100k
clear-trigger, 150k compaction-trigger, keep=3, the 58.6% figure) come from the
**Claude API / cookbook**, *not* the Claude Code CLI. The CLI's auto-compaction
uses a separate, less-precisely-documented threshold and a multi-stage process.
Do not conflate API defaults with CLI behavior.

---

## 1. CAUSES — what actually fills the context (ranked)

| Rank | Offender | Why it's expensive | Evidence |
|---|---|---|---|
| 1 | **Large/unfiltered tool outputs** (whole-file reads, log/command dumps) | A single 10,000-line log can cost *tens of thousands* of tokens in one call | [/costs](https://code.claude.com/docs/en/costs): "Instead of Claude reading a 10,000-line log file… a hook can grep for ERROR… reducing context from tens of thousands of tokens to hundreds" |
| 2 | **Always-on baseline** (CLAUDE.md, tool definitions) | CLAUDE.md is "loaded in full at launch" and persists *even during unrelated work* | [/memory](https://code.claude.com/docs/en/memory): "loaded into the context window at the start of every session, consuming tokens alongside your conversation" |
| 3 | **Long histories** | Accumulate full tool outputs + intermediate reasoning turn after turn | [/context-window](https://code.claude.com/docs/en/context-window): compaction exists precisely to discard "full tool outputs and intermediate reasoning" |

⚠️ **Refuted claim — do not repeat:** The intuitive "file reads *dominate*,
~1,100–2,400 tokens each" was **refuted (1–2 votes)**. Whole-file reads are a
*major* offender (they sit inside rank #1), but the evidence does **not** support
asserting per-file reads are *the* single dominant cost with those specific
figures. Treat as plausible-but-unverified.

---

## 2. SYMPTOMS & DETECTION

- **`/context`** — primary signal. Per-category breakdown: system prompt, system
  tools, MCP tools, memory/CLAUDE.md, messages, free space. *"Run `/context` to
  see what's consuming space"* ([/costs](https://code.claude.com/docs/en/costs),
  [/context-window](https://code.claude.com/docs/en/context-window)).
- **Cache health meter** — `cache_read_input_tokens` vs
  `cache_creation_input_tokens`. *"A high read-to-creation ratio means caching is
  working well. If creation stays high turn after turn, something is changing in
  your prefix"* ([/prompt-caching](https://code.claude.com/docs/en/prompt-caching)).
- **Auto-compaction as a backstop** — approaching the limit triggers compaction
  rather than ending the session.

**Accuracy caveat (documented).** Detection is *not* perfectly reliable. GitHub
[#66144](https://github.com/anthropics/claude-code/issues/66144) (June 2026,
v2.1.168) reports auto-compact **failing to trigger at 100%**. And a documented
*thrashing* mode: if one output is so large that context refills immediately
after each summary, Claude Code "stops auto-compacting after a few attempts and
shows an error" ([/context-window](https://code.claude.com/docs/en/context-window)).
The CLI's exact numeric trigger is **not published** (open question).

---

## 3. MITIGATION TAXONOMY + comparison table

| Category | Mechanism | Feature (CLI vs API) | Lossy? | Breaks cache? | Maturity | How to verify |
|---|---|---|---|---|---|---|
| **Prevention** (gate inputs) | Hook rewrites a command / replaces tool output before Claude sees it; grep/filter logs | CLI: `PreToolUse` `updatedInput.command`, `PostToolUse` `updatedToolOutput`. MCP tool defs **deferred by default** | No | No | **Documented / stable** | `/context` before vs after; inspect hook stdin/stdout |
| **Reaction** (trim results) | Server clears *oldest* tool results past a trigger, replaces with placeholder | **API only**: `clear_tool_uses_20250919`, trigger **100k**, keep **3**, `exclude_tools`, `clear_at_least` | Yes | **Yes** | **Beta** (`context-management-2025-06-27`) | Watch input tokens drop after trigger; look for placeholder text |
| **Reaction/Compaction (API)** | Summarize older context, drop prior blocks, continue from summary | **API only**: `compact_20260112`, trigger **150k** (min 50k) | Yes | Yes | **Beta** (`compact-2026-01-12`) | Check for compaction block; confirm earlier blocks ignored |
| **Offloading** (subagents) | Heavy read happens in a *separate* context; only final text + small metadata returns | CLI: Task/subagents | No (main ctx) | No | **Documented / stable** | Example: subagent read **6,100 tokens → 420-token** result returned |
| **Reuse** (prompt caching) | Stable prefix read from cache at **0.1× input** (90% off) | CLI: **automatic/default-on** (disable via `DISABLE_PROMPT_CACHING=1`) | No | n/a | **Documented / stable** | `cache_read : cache_creation` ratio |
| **Curation** (lean baseline) | Keep CLAUDE.md **<200 lines**; move workflows to on-demand **skills**; path-scoped rules | CLI: CLAUDE.md + skills | No | No | **Documented / stable** | `/context` memory line; longer files also "reduce adherence" |
| **Compaction (CLI)** | Multi-stage: clear older tool outputs first, then summarize conversation | CLI: auto + `/compact` | **Yes** | Yes | **Documented / stable** | Session continues past full window; summary retains intent/files/errors |
| **Compaction control** | Inspect/block compaction before it runs | CLI: `PreCompact` hook (matchers `manual`/`auto`); block via `{"decision":"block"}` or exit 2 | n/a | n/a | **Documented; block sub-capability weaker** | Fires before compaction; receives `transcript_path` |

**Cache invalidation hierarchy (why curation + caching reinforce each other).**
`tools → system → messages`. Changing **tool definitions invalidates the
*entire* cache**; changing only messages invalidates just that layer
([API prompt-caching](https://platform.claude.com/docs/en/build-with-claude/prompt-caching),
[/prompt-caching](https://code.claude.com/docs/en/prompt-caching)). Practical
consequence: **adding/removing MCP servers mid-session silently destroys your
cache savings.** Editing CLAUDE.md mid-session *is* cache-safe (it sits in the
project-context layer below system).

---

## 4. EFFECTIVENESS — biggest reduction per unit of effort

1. **Prompt caching — best effort-adjusted lever.** Automatic, no config, **cache
   reads = 0.1× base input = 90% discount** (Opus 4.8 $5→$0.50/MTok; Sonnet 4.6
   $3→$0.30; Haiku 4.5 $1→$0.10)
   ([pricing](https://platform.claude.com/docs/en/about-claude/pricing)).
   Largest input discount Anthropic offers (vs 50% batch).
2. **Offloading** — quantified example: **6,100 tokens read → 420 returned** to
   main context ([/context-window](https://code.claude.com/docs/en/context-window)).
   *Caveat:* illustrative simulation figure; it saves the **main** context, not
   total spend — the subagent still burns those tokens.
3. **Compaction** — the one headline number: Anthropic cookbook shows **58.6%
   total-token reduction** (208,838 → 86,446) over 5 support tickets.
   ⚠️ **Heavily caveated:** used a **5k-token threshold "for demonstration
   purposes,"** which the cookbook itself warns against (recommends ~100k); the
   headline survived only a **2–1 vote**. A teaching demo, **not** a
   generalizable production benchmark.

**Honest gap:** no rigorous, independent third-party benchmark surfaced for any
technique. All numbers above are Anthropic's own demo/illustrative figures.

### External signal — practitioner field data (community, treat as anecdote)

A mid-2026 practitioner post on agent "tokenomics" (Klyshevich, *Tokenomics in
Action: What Running an AI Dark Factory Actually Costs*, LinkedIn) adds angles
this report's sources don't cover. All are **community / single-project /
no-methodology**, and the post's tokenizer claims (e.g. *"hello" = 8 tokens on
Claude*; *"1M context is practically 500K"*) are demonstrably wrong — `hello` is
1 token in modern tokenizers — so **quarantine the tokenizer/routing claims**
from the verified findings above. What survives scrutiny:

- **Loop-count as a context/cost lever (new angle, not in the taxonomy).** A
  weaker model that thrashes many turns can fill context — and cost — *more* than
  a stronger model that one-shots. Reported loops-per-task: Opus 4.6 = 1,
  Sonnet 4.6 = 3–5, a "Mini" model = 10. Implication: total tokens ≈ per-call
  tokens × agent loops, so **model capability is itself a context lever**, not
  just a price knob. (Tension noted: this directly undercuts the same post's
  "avoid Anthropic for bulk workflows" advice.)
- **Real-world proof of prompt caching (corroborates §3 Reuse / §4).** The post's
  May 2026 usage table shows 16.77B input tokens vs **15.91B cache reads**
  (~95% served from cache) costing **$454.51** — a live, at-scale illustration of
  the §3 "Reuse" verify method (`cache_read : cache_creation` ratio) and the
  0.1× read discount.
- **Tooling pointer (corroborates selective/semantic access).** Pairs **RepoMix**
  with **CodeGraph** for "find the exact 10 lines, then read only that fragment,"
  claiming **50–60% fewer exploration tokens** — backing research-prompts #11
  (semantic access), where the CodeGraph tooling is evaluated in full.

Source: Klyshevich, "Tokenomics in Action…," LinkedIn, 2026 — *community,
unverified; tokenizer/routing claims rejected*.

---

## 5. PITFALLS — where guardrails backfire

- **Compaction is lossy *by design*.** Discards "full tool outputs and
  intermediate reasoning… won't have the exact code it read earlier"
  ([/context-window](https://code.claude.com/docs/en/context-window)). Loses
  "full knowledge base article text, complete drafted response text, detailed
  classification reasoning"
  ([API compaction](https://platform.claude.com/docs/en/build-with-claude/compaction)).
  On coding tasks where *exact* code matters, this is the sharp edge.
- **Trim/clear vs cache — direct conflict.** Tool-result clearing "invalidates
  cached prompt prefixes when content is cleared." Mitigate with `clear_at_least`
  so each clear is large enough to be worth the cache loss
  ([context-editing](https://platform.claude.com/docs/en/build-with-claude/context-editing)).
- **API compaction uses the *same* (expensive) model** — "no option to use a
  different (e.g., cheaper) model for the summary." And it can **fail when tools
  are defined**: the model may call a tool during the internal summary step,
  yielding a compaction block with `content: null` (workaround: instruct it not
  to call tools). ⚠️ A claim that a cheaper-`model` param + `summary_prompt`
  exists was **refuted (1–2)** — the docs explicitly say there is **no**
  cheaper-model option.
- **PreCompact blocking is permanent, not deferred.** Community sources (GitHub
  MemPalace #941/#856) note blocking is a *cancel*, not a retry/resume — blocking
  indefinitely can itself cause overflow. The official
  [hooks blog](https://claude.com/blog/how-to-configure-hooks) frames PreCompact
  only as *preservation* and doesn't mention blocking, so the block capability is
  the **weakest/most version-sensitive** part *(community workaround territory)*.
- **Over-aggressive curation drift.** GitHub #16616 reported USER skills in
  `~/.claude/skills/` being loaded *fully* (~10k tokens) instead of
  frontmatter-only — so "move instructions to skills keeps base context lean" was
  **version/format-sensitive** (plugin-format workaround). Verify with
  `/context`, don't assume.
- **Subagent round-trip cost.** Offloading saves the *main* window but adds
  latency and total token spend; over-fanning out trades one problem for another.

---

## 6. DECISION FRAMEWORK — "if overflow is caused by X, reach for Y first"

1. **Verbose command/log output** → **Prevention.** `PreToolUse`/`PostToolUse`
   hook that greps/filters before it lands. (Cheap, lossless, biggest single-call
   win.) → *This is exactly what the guard in this folder does.*
2. **Many MCP servers/tools** → rely on **default MCP tool deferral**; verify
   with `/context`. (Caveat: deferral default is disabled on Vertex AI /
   non-first-party `ANTHROPIC_BASE_URL` proxies; Haiku lacks tool_reference
   support.)
3. **Bloated always-on baseline (CLAUDE.md, workflow instructions)** →
   **Curation.** Trim <200 lines, move workflows to on-demand skills, path-scoped
   rules.
4. **One large read/exploration** → **Offloading.** Subagent returns only a
   summary.
5. **Stable prefix re-billed every turn** → **Reuse.** Keep tools/system/
   CLAUDE.md stable so caching holds; watch the `cache_read:cache_creation` ratio.
6. **Long accumulated history near the limit** → **Reaction/Compaction**, *last.*
   API context-editing to clear old tool results, or CLI auto/manual `/compact`,
   gated by `PreCompact` if state is critical.

**Governing principle:** prevent/curate (lossless) before you react/compact
(lossy). Caching is the free default underneath all of it.

This maps onto this folder's thesis: the built hook is **branch #1
(prevention)**, and the README's order — *selective reading → offloading →
caching → compaction* — matches the framework above. The research **confirms**
that ordering and adds the cache-invalidation tension as the main thing to watch
when layering #1 with compaction.

---

## Version-sensitivity & open questions

- **All findings current ~mid-2026** (Claude Code v2.1.163–v2.1.174, Opus 4.8,
  Sonnet 4.6, 1M context). API context-editing and compaction are both **BETA** —
  headers/defaults may change.
- **Don't conflate API defaults with CLI behavior.** The 100k/150k/keep=3/58.6%
  figures are API/cookbook; the CLI threshold is unpublished.
- **Open questions:** (1) the CLI's actual auto-compaction trigger; (2) any
  independent benchmark of the *quality* cost of lossy compaction on coding
  tasks; (3) whether offload + clear + cache compose additively or cancel out;
  (4) whether the skills-loaded-at-startup bug (#16616) is fixed or still needs
  the plugin workaround.

---

## Sources (20 fetched)

**Primary (Anthropic docs):** code.claude.com `/costs`, `/context-window`,
`/memory`, `/hooks`, `/prompt-caching`, `/mcp`, `/how-claude-code-works`;
platform.claude.com `compaction`, `context-editing`, `prompt-caching`,
`pricing`, cookbook `automatic-context-compaction` &
`context-engineering-tools`;
[anthropic.com effective-context-engineering](https://www.anthropic.com/engineering/effective-context-engineering-for-ai-agents);
[claude.com how-to-configure-hooks](https://claude.com/blog/how-to-configure-hooks);
[claude.com multi-agent-systems](https://claude.com/blog/building-multi-agent-systems-when-and-how-to-use-them);
LangChain `short-term-memory` & `deepagents/context-engineering` (cross-framework).

**Forum:** GitHub anthropics/claude-code #66144, #15923, #36068, #16616.

**Blog (corroborating / community):** barazany.dev (compaction engine),
morphllm (compaction vs summarization), bytebell.ai (compaction losing work),
philschmid.de (context engineering pt.2); Klyshevich, "Tokenomics in Action"
(LinkedIn, 2026 — agent token-cost field data; tokenizer/routing claims
unreliable, see *External signal* in §4).
