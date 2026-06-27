# Semantic code access (LSP / IDE-MCP) as a context-efficiency strategy for Claude Code

> **Source:** deep-research run for Option **#11** in
> [`../research-prompts.md`](../research-prompts.md). 5 search angles fanned out in
> parallel (~30 sources surfaced), then the 4 load-bearing first-party claims were
> re-fetched and verified verbatim against `code.claude.com`. Current as of
> **mid-2026** (Claude Code v2.1.143–v2.1.174; LSP 3.17; Opus 4.7/4.8, Sonnet 4.6).
>
> ⚙️ **Methodology note (honest):** the packaged `deep-research` workflow aborted
> in its *scoping* stage (a StructuredOutput retry-cap loop before any searching
> happened — a harness bug, not a dead end). The research was re-run as a manual
> 5-agent fan-out with the same intent (multi-source, cited, adversarially
> spot-checked). The five angles *are* the prompt's five questions.

**Bottom line.** Semantic access is **selective reading taken to its limit**: you
replace a *file body* (hundreds–thousands of tokens) with a *structured answer to
a precise question* (tens of tokens) — a definition's location, a symbol's type,
the list of real call sites, the three compiler errors. The codebase's structure
lives in an index **outside** the context window; the agent queries it. Anthropic
**endorses this in its own docs**: *"A single 'go to definition' call replaces what
might otherwise be a grep followed by reading multiple candidate files"*
([/costs](https://code.claude.com/docs/en/costs)).

The single most important finding — and a **correction** to the common belief that
"Claude Code has no LSP" — is that **Claude Code ships a built-in LSP tool**,
activated by installing an official **Code intelligence** plugin (11 languages) +
the language-server binary: *"Code intelligence plugins enable Claude Code's
built-in LSP tool, giving Claude the ability to jump to definitions, find
references, and see type errors immediately after edits"*
([/discover-plugins](https://code.claude.com/docs/en/discover-plugins#code-intelligence)).
So the **primary** wire-up path today is *first-party plugins*, not a third-party
MCP bridge. serena, the JetBrains MCP, and code-graph indexers are the
**alternatives/extensions** — useful for languages or operations the built-in tool
doesn't cover.

**The honest boundary:** semantic access returns *locations, types, references,
diagnostics* — **not implementation bodies**. When you genuinely need to read code
(novel logic, a diff, writing new code in context), you still read. It also needs a
working language server (weak on dynamic/metaprogrammed code), and an MCP-server
backend carries a standing baseline cost. Where it fits the three chosen levers:
this is **selective reading** maxed out, with a flavor of **offloading** (the index
holds the code) — and it *cooperates* with **prompt caching** only if the tool
surface stays stable (adding/removing MCP servers mid-session invalidates the
cache — see the master report's cache-invalidation tension).

---

## 1. Which semantic operations most reduce context

**The core arithmetic (documented).** An LSP `Location` is one URI plus two
`{line, character}` pairs — *tens of tokens*. The file it points into is hundreds
to thousands of lines. Code tokenizes at **~10 tokens/line** (Python ~10, JS ~7,
SQL ~11.5 — vendor guide, [16x Prompt](https://prompt.16x.engineer/blog/code-to-tokens-conversion)),
so a 300-line file ≈ 2–3k tokens and a whole module can be 30k. Every operation
below swaps "read the file(s)" for a compact structured response. Response shapes
are from the **LSP 3.17 specification**
([microsoft.github.io/language-server-protocol](https://microsoft.github.io/language-server-protocol/specifications/lsp/3.17/specification/)).

**Ranked, most → least context saved:**

| # | Operation (LSP request) | What the response returns (documented shape) | Est. tokens returned **vs.** the read it replaces | Why it ranks here |
|---|---|---|---|---|
| 1 | **workspace symbol search** (`workspace/symbol`) | `WorkspaceSymbol[]` — each `{name, kind, location}` | ~20–40 tok/hit **vs.** grep + opening many candidate files repo-wide (tens of k tok) | Biggest fan-out avoided: replaces the whole "where is X?" exploration |
| 2 | **find references** (`textDocument/references`) | `Location[]` | ~20–40 tok **per ref** **vs.** reading every referencing file (~2–3k tok each). 10 refs ≈ ~300 tok vs ~20–30k | Savings scale with reference count; the classic grep-replacement |
| 3 | **document symbols / overview** (`textDocument/documentSymbol`; serena `get_symbols_overview`) | `DocumentSymbol` tree `{name, detail, kind, range, children}` — **names/kinds, no bodies** | a file outline ≈ few hundred tok **vs.** the full file body (serena reports ~**30k raw → few hundred tok**, community) | "Read the shape of a file without reading the file" |
| 4 | **call hierarchy** (`callHierarchy/incomingCalls`, `outgoingCalls`) | `CallHierarchyIncomingCall {from: Item, fromRanges}` / `OutgoingCall {to, fromRanges}` | ~30–60 tok per caller/callee **vs.** reading each caller/callee body to trace flow | Replaces multi-file control-flow tracing |
| 5 | **go to definition** (`textDocument/definition`) | one `Location` / `LocationLink {targetUri, targetRange, targetSelectionRange}` | ~15–40 tok **vs.** the defining file (2–5k+ tok) | Large per-call win, but a single jump |
| 6 | **hover / type info** (`textDocument/hover`) | `Hover {contents: MarkupContent {kind, value}, range}` | ~30–150 tok (signature + doc) **vs.** reading the declaration site (hundreds–1k+ tok) | Answers "what is this symbol?" without opening it |
| 7 | **diagnostics** (`textDocument/publishDiagnostics`) | `Diagnostic {range, severity, code, source, message, relatedInformation}` | ~30–80 tok/issue **vs.** reading code to guess the bug, or a full compiler/test dump (thousands of tok) | Kills a *top* overflow source (see Option #12) |
| 8 | **rename / format** (`textDocument/rename`, `…/formatting`) | `WorkspaceEdit` / `TextEdit[] {range, newText}` — **edit deltas only** | range+newText per site **vs.** reading every affected file *and* re-emitting full rewrites | Saves both **input** and **output** tokens; touches files the agent never reads |

**Call/type hierarchy & implementations** are prepared via
`textDocument/prepareCallHierarchy` / `prepareTypeHierarchy`, returning
`CallHierarchyItem` / `TypeHierarchyItem` (analogous compact `{name, kind, uri,
range}` structs). ⚠️ The exact `TypeHierarchyItem` response struct was **not
quoted from primary source** in this run — treat its shape as *unverified* (the
request exists in LSP 3.17; serena exposes `type_hierarchy`, JetBrains-backend only).

**How agents actually call these (the MCP layer).** serena surfaces them as
`get_symbols_overview` (classes/functions/methods, *no bodies*), `find_symbol`
(returns a body only when `include_body=true`; default off — name/kind/location
first), and `find_referencing_symbols` (real call sites from the LS reference
index). The documented pattern is **progressive disclosure**: `list_dir →
get_symbols_overview → find_symbol (bodies off) → read a body only when needed`
([oraios/serena](https://github.com/oraios/serena)) — "send the smallest artifact
that answers the question."

> ⚠️ **Confidence flags for §1:** the *response shapes* are documented (LSP 3.17).
> The *token numbers* are my own order-of-magnitude estimates from the shapes ×
> ~10 tok/line — direction and magnitude are solid; exact counts vary by tokenizer
> (Anthropic's vs tiktoken), file size, and result count. The "30k → few hundred"
> figure is a single community/secondary source (serena via DeepWiki) — illustrative,
> not a benchmark. `include_body` default-off is well-corroborated but community-sourced.

---

## 2. How to wire this into Claude Code TODAY

### 2a. Native LSP — **YES, there is a built-in LSP tool** (the headline correction)

Two distinct native surfaces exist; don't conflate them:

**(i) Built-in LSP tool, via official Code-intelligence plugins (the primary path).**
The CLI has a **built-in LSP tool** that is *dormant until you install a
code-intelligence plugin* + its language-server binary. Documented verbatim:
*"Code intelligence plugins enable Claude Code's built-in LSP tool… These plugins
configure Language Server Protocol connections, the same technology that powers
VS Code's code intelligence"*
([/discover-plugins](https://code.claude.com/docs/en/discover-plugins#code-intelligence)).
Install from the official marketplace with `/plugin install <name>@claude-plugins-official`.
The 11 shipped plugins:

| Language | Plugin | Binary required |
|---|---|---|
| C/C++ | `clangd-lsp` | `clangd` |
| C# | `csharp-lsp` | `csharp-ls` |
| Go | `gopls-lsp` | `gopls` |
| Java | `jdtls-lsp` | `jdtls` |
| Kotlin | `kotlin-lsp` | `kotlin-language-server` |
| Lua | `lua-lsp` | `lua-language-server` |
| PHP | `php-lsp` | `intelephense` |
| Python | `pyright-lsp` | `pyright-langserver` |
| Rust | `rust-analyzer-lsp` | `rust-analyzer` |
| Swift | `swift-lsp` | `sourcekit-lsp` |
| TypeScript | `typescript-lsp` | `typescript-language-server` |

Other languages: *"create your own LSP plugin"*
([/plugins-reference#lsp-servers](https://code.claude.com/docs/en/plugins-reference)).
**What Claude gains (documented):**
- **Automatic diagnostics** — *"after every file edit Claude makes, the language
  server analyzes the changes and reports errors and warnings back automatically…
  If Claude introduces an error, it notices and fixes the issue in the same turn."*
  (Inline via **Ctrl+O** when the "diagnostics found" indicator appears.)
- **Code navigation** — *"jump to definitions, find references, get type info on
  hover, list symbols, find implementations, and trace call hierarchies… more
  precise navigation than grep-based search, though availability may vary by
  language and environment."* (This single sentence is first-party confirmation of
  the entire §1 operation list **and** the §3 precision claim.)

**(ii) Built-in `ide` MCP server, when running inside an IDE (complementary).**
Running `claude` in VS Code's integrated terminal (or `/ide` from an external
terminal) auto-connects to the extension's local MCP server: *"The CLI
automatically integrates with your IDE for features like diff viewing and
diagnostic sharing"* ([/vs-code](https://code.claude.com/docs/en/vs-code)). The
server is named **`ide`**, hidden from `/mcp`, and hosts ~a dozen RPC tools but
**only two are visible to the model**:

| Tool (as seen by hooks) | What it does | Writes? |
|---|---|---|
| `mcp__ide__getDiagnostics` | Returns language-server diagnostics — VS Code's **Problems panel** errors/warnings; optionally one file. | No |
| `mcp__ide__executeCode` | Runs Python in the active Jupyter kernel (always Quick-Pick confirms first). | Yes |

The rest (open diffs, read selection, save) are filtered out before the tool list
reaches Claude. Transport binds `127.0.0.1` on a random port with a fresh token in
`~/.claude/ide/`. **Know it exists if a `PreToolUse` hook allowlists `mcp__*`
tools** (ties to Option #13).

> **Net for Q2:** *"Claude Code has no LSP"* is **false** as of mid-2026. It has a
> built-in LSP tool (plugin-gated, 11 languages) **and** an IDE-diagnostics bridge.
> What it lacks is a *zero-config, all-language* LSP client — hence the bridges below.

### 2b. LSP-to-MCP bridges (for languages/ops the built-in tool doesn't cover)

| Tool | Semantic tools exposed | Languages & how | Maturity | Install (Claude Code) |
|---|---|---|---|---|
| **serena** ([oraios/serena](https://github.com/oraios/serena)) | `find_symbol`, `find_referencing_symbols`, `get_symbols_overview`, `goto_definition`, `find_implementations`, `hover`, `type_hierarchy`, diagnostics; **symbolic edits** (`replace_symbol_body`, `insert_after/before_symbol`), `rename_symbol`, `search_for_pattern` | **40+** via LSP servers (pyright, tsserver, gopls, rust-analyzer, jdtls, clangd, …) | MIT, **~25.7k★**, very active (v1.5.3, May 2026). **Most mature bridge.** | see snippet below |
| **mcp-language-server** ([isaacphi](https://github.com/isaacphi/mcp-language-server)) | `definition`, `references`, `diagnostics`, `hover`, `rename_symbol`, `edit_file` (pure LSP proxy, 6 tools) | any LSP over stdio; guides for Go/Rust/Py/TS/C++ | BSD-3, ~1.6k★, **beta**, one instance per language | `claude mcp add lsp-go -- mcp-language-server --workspace "$(pwd)" --lsp gopls` |
| **lsp-mcp** (Tritlo, jonrad, et al.) | hover/definition/references/diagnostics/code-actions — **varies by fork** | per-LSP; fragmented | **smaller, version-unverified** — check the specific repo | per-repo README, same `claude mcp add … -- <bridge> --lsp <server>` pattern |
| **multilspy** | *library* powering serena's LSP layer; not a turnkey server | 10+ LSPs | research lib (Microsoft) | use serena instead |

**Ready-to-paste serena (zero-install, uvx-from-git, per project):**
```bash
claude mcp add serena -- \
  uvx --from git+https://github.com/oraios/serena \
  serena start-mcp-server --context claude-code --project "$(pwd)"
```
⚠️ **Version-sensitive (serena):** use `--context claude-code` — `--context
ide-assistant` is **deprecated** (still shown in many third-party guides). Requires
Python `uv`/`uvx` (no npm package; `npm install @oraios/serena` is wrong). README
warns *"Do not install Serena via an MCP/plugin marketplace."* Optional: launch
`claude --system-prompt="$(serena prompts print-cc-system-prompt-override)"` to
bias the model toward symbolic tools over built-in grep/Read.

### 2c. JetBrains / IntelliJ IDEA MCP

**Official — bundled "MCP Server" plugin (the live path).** IntelliJ-based IDEs
**2025.2+** ship an integrated MCP server: *"Starting with version 2025.2,
IntelliJ IDEA comes with an integrated MCP server, allowing external clients such
as Claude… to access tools provided by the IDE"*
([jetbrains.com/help/idea/mcp-server](https://www.jetbrains.com/help/idea/mcp-server.html)).
Relevant tools it exposes:

- **Search:** `search_symbol`, `search_file`, `search_text`, `search_regex`
- **Code insight:** `get_symbol_info` (name/signature/type/docs — same data as Quick Documentation)
- **Analysis / diagnostics:** **`get_file_problems`** (inspections — the JetBrains diagnostics surface), `build_project`, `get_project_dependencies`/`_modules`
- **Refactoring:** `rename_refactoring`
- **Files/text (⚠️ bulk-returning):** `get_file_text_by_path`, `replace_text_in_file`, `list_directory_tree`, `find_files_by_glob`
- **Execution / debug / DB / VCS / PSI:** `execute_run_configuration`, `execute_terminal_command`, XDebug tools, DB tools; docs reference *"PSI tree analysis"* tools but the **exact tool name is underspecified** — flag.

**Setup (auto-config):** `Settings | Tools | MCP Server` → **Enable MCP Server** →
under **Clients Auto-Configuration** click **Auto-Configure** for **Claude Code**
(JetBrains writes Claude Code's config for you) → restart Claude Code. Protocols:
SSE / Stdio / HTTP Stream.

**Deprecated — don't use for new setups:** the `@jetbrains/mcp-proxy`
(`github.com/JetBrains/mcp-jetbrains`) npm bridge is **deprecated**, its *"core
functionality integrated directly into all IntelliJ-based IDEs starting with
2025.2."* Prefer the bundled plugin.

> ⚠️ **Flags (Q2):** exact star/release counts drift (directional only). Whether the
> JetBrains plugin is *bundled-and-enabled-by-default* differs across JetBrains
> pages by build (2025.2 vs 2026.1 phrasing) — **confirm it's enabled in your
> build**. `mcp__ide__getDiagnostics` can time out in JetBrains if the file isn't
> the active tab, and is reported inconsistent inside the VS Code *extension panel*
> session (community: anthropics/claude-code #3085, #8635, #40766, #21705) — solid
> when running the **CLI in the integrated terminal**.

---

## 3. Precision: why semantic nav beats grep, and how that avoids wasted reads

grep/text search matches a name **wherever the characters appear**; a language
server resolves the **actual binding** by parsing to an AST and consulting a
compiler-accurate index. Sourcegraph rates the two head-to-head: search-based
accuracy *"Moderate (some false positives)"* vs precise *"Perfect
(compiler-accurate)"*
([github-vs-sourcegraph](https://sourcegraph.com/docs/getting-started/github-vs-sourcegraph)).
Four grep failure modes semantic nav fixes:

1. **Common-name collisions.** Search-based *"can return false positive references
   for symbols with common names"* and errs more *"for tokens with common names
   (such as `Get`)"*; precise nav is *"not susceptible to… symbols with the same
   name"*
   ([search-based explanation](https://4.4.sourcegraph.com/code_navigation/explanations/search_based_code_navigation)).
2. **Name in comments / strings / docs.** LSP *"distinguishes between `process` the
   function, `process` the variable, and 'process' in a comment"*; grep's false
   positives include comments, string literals, and doc references
   ([amirteymoori](https://amirteymoori.com/lsp-language-server-protocol-ai-coding-tools/), community).
3. **Scope / shadowing.** A shadowed or same-named variable in another scope is a
   grep false hit, correctly excluded by the LS (it uses *"search-based heuristics
   rather than parsing… into an abstract syntax tree"* — Sourcegraph).
4. **Overload / interface-impl / cross-repo.** serena's `find_implementations` /
   `find_declaration` give interface↔impl navigation; precise nav adds
   *cross-repository* resolution that search-based (single-repo) lacks. *(A
   verbatim "which overload" statement wasn't found — this is well-grounded
   inference from the impl/declaration/AST evidence, not a direct quote.)*

**The precision → fewer-wasted-reads → fewer-tokens chain (state it explicitly):**
grep returns a pile of candidate matches → the agent **opens several files to judge
which match is the real binding** → those disambiguating reads are wasted context.
Semantic nav lands on the exact declaration/call sites, so the agent reads only the
right slice or nothing. Anthropic frames the cost principle: *"an agent can waste
context by misusing tools, chasing dead-ends, or failing to identify key
information,"* and good practice is *"finding the smallest possible set of
high-signal tokens"*
([effective-context-engineering](https://www.anthropic.com/engineering/effective-context-engineering-for-ai-agents)).
And the conclusion, in Anthropic's own words: *"A single 'go to definition' call
replaces what might otherwise be a grep followed by reading multiple candidate
files"* ([/costs](https://code.claude.com/docs/en/costs)).

**Honest counterpoint — where grep ties or wins** (Anthropic is itself a
counterweight here):
- **No language server / no index → grep is the floor.** Sourcegraph itself *"will
  fall back to search-based code navigation"* when no index exists; search-based
  *"works out of the box… with no setup."*
- **Anthropic actively endorses grep/glob for agents:** Claude Code uses *"primitives
  like glob and grep… to retrieve files just-in-time, effectively bypassing the
  issues of stale indexing and complex syntax trees"* (effective-context-engineering).
  Indexes can be stale; grep reads ground truth.
- **Non-code / string / config search** (a literal in YAML, a log line, a comment):
  no symbol to bind to — grep's job. *(reasoning, from the AST-parsing precondition)*
- **Dynamic / metaprogrammed code** (reflection, `eval`, monkey-patching, DI):
  static resolution is blind; a textual sweep may catch sites the LS misses.
- **Rare/unique names:** false-positive risk collapses; grep is instant, zero-index.

---

## 4. The "IDE index as offloaded context"

**The concept.** A persistent, pre-built project index (an LSP/IDE index, or a
code-graph store) holds the *knowledge of the codebase* — symbols, types,
references, call graph — **outside the model's context window**. The agent issues a
tiny query and gets back only the relevant answer, instead of reading files in and
reconstructing structure by hand. This is the code instance of Anthropic's
**just-in-time** principle: *"Rather than pre-processing all relevant data up
front, agents… maintain lightweight identifiers (file paths, stored queries, web
links) and use these references to dynamically load data into context at runtime"* —
and Anthropic frames such a store as **external memory** that lets agents *"build
up knowledge bases… outside the context window"*
([effective-context-engineering](https://www.anthropic.com/engineering/effective-context-engineering-for-ai-agents)).
The index *is* that knowledge base for code; the model offloads "knowing the repo."

**Concrete indexers** (what they index / what a query returns):

| Indexer | Indexes | A query returns | Source |
|---|---|---|---|
| **Built-in LSP tool** (Claude Code plugin) | the LS's own per-project index | def/refs/hover/symbols/impls/call-hierarchy; auto-diagnostics post-edit | [/discover-plugins](https://code.claude.com/docs/en/discover-plugins#code-intelligence) |
| **Sourcegraph SCIP** (SCIP Code Intelligence Protocol) | symbols as string IDs + relations + source locations (Protobuf) | precomputed, *"compiler-accurate,"* cross-repo go-to-def / find-refs | [announcing-scip](https://sourcegraph.com/blog/announcing-scip) |
| **LSIF** (legacy, replaced by SCIP) | same graph via numeric IDs | same nav; *"~4x larger gzip-compressed"* than SCIP | [announcing-scip](https://sourcegraph.com/blog/announcing-scip) |
| **CodeGraph family** (tree-sitter → SQLite/graph) | functions/classes/methods + edges (calls, imports, extends, implements) | callers/callees, call chains, **blast radius** — *"which grep cannot find"* | [colbymchenry/codegraph](https://github.com/colbymchenry/codegraph), [CodeGraphContext](https://github.com/CodeGraphContext/CodeGraphContext) |

> **CodeGraph note.** Every tool named **"CodeGraph"** is a semantic graph/index
> MCP tool — *none is a per-file token-budget scanner* — so it belongs in **this
> option**, not in #2(c). The variants (pin the exact repo; all the same category):

| Project | What it is | Token counting? |
|---|---|---|
| [codegraph-ai/CodeGraph](https://github.com/codegraph-ai/CodeGraph) | Rust semantic graph + MCP server (~45 MCP tools, 37–38 tree-sitter langs, VS Code ext, persistent memory) | **No per-file budget** — only `GITHUB_TOKEN` / `get_ai_context` return-budgeting |
| [colbymchenry/codegraph](https://github.com/colbymchenry/codegraph) ([codegraph.codes](https://codegraph.codes/)) | TS, tree-sitter → SQLite/FTS5, single `codegraph_explore` tool | **No tokenizer** — governs output by **character** budgets; CLAUDE.md: *"don't optimize for token cost"* |
| [CodeGraphContext](https://github.com/CodeGraphContext/CodeGraphContext) | Python MCP + CLI → graph DB (FalkorDB/KuzuDB/Neo4j), 23 langs | **No** token references |
| [@er77/code-graph-rag-mcp](https://www.npmjs.com/package/@er77/code-graph-rag-mcp) | npm MCP, tree-sitter + SQLite + sqlite-vec, 10 langs | **No** (JSCPD clone "tokens" only). ⚠️ **ARCHIVED 2026-06-12** → successor [OntoIndex](https://github.com/ontograph/ontoindex) |
| [sdsrss/code-graph-mcp](https://github.com/sdsrss/code-graph-mcp) | AST knowledge-graph MCP for Claude Code, 10 langs | Category solid; internal "token-aware extraction" detail **uncertain** |
| [tirth8205/code-review-graph](https://github.com/tirth8205/code-review-graph) | Local-first graph, MCP + CLI; selects by **blast-radius** | Surfaces tokens only as an **after-the-fact savings metric** (char heuristic; `cl100k_base` via opt-in `--verify`). Site: *"Not a token counter."* |

> ⚠️ **Don't overstate:** the blanket "CodeGraph never mentions tokens" was
> refuted — the accurate claim is *none provides a pre-flight per-file token
> budget*; some reference tokens in other ways. The Claude-native budgeting
> baseline remains `count_tokens` (model-specific). CodeGraph sidesteps token
> counting entirely.

**Mapping to the series' three levers.** "Query a pre-built index" is the
**offloading** lever applied to the *codebase representation itself* — a sibling of
subagent offloading and of RAG:
- **vs. selective reading** (glob/grep/head/tail): same family; the index is the
  *precise/precomputed* upgrade — grep finds text and the model still reconstructs
  call paths; the index returns the resolved relationship directly.
- **vs. subagent offloading**: structurally identical — heavy work happens outside
  the main window, only a distilled answer returns (Anthropic: subagents return
  *"a condensed… summary (often 1,000–2,000 tokens)"*). The index is "offloading
  without the second model."
- **vs. prompt caching**: orthogonal but complementary — caching reuses tokens
  *already in context*; the index keeps those tokens *out of context entirely*.
  ⚠️ But an index delivered as an **MCP server** can *fight* caching: adding/removing
  it mid-session invalidates the cached prefix (see master report §3).

**Trade-offs (the standing cost):**
1. **Cold-start build.** Precise nav *"requires you to upload indexes for each
   repository"* (Sourcegraph); large repos pay real upfront indexing time.
2. **Sync / staleness drift.** Code-graph tools rely on file watchers (codegraph
   re-indexes after a *"2000ms"* debounce and *"prepend[s] a staleness warning
   banner"* during the window). Without re-indexing, the index lies.
3. **Schema/baseline tax.** Tool descriptions *"are loaded into your agents'
   context"* and *"more tools don't always lead to better outcomes"*
   ([writing-tools-for-agents](https://www.anthropic.com/engineering/writing-tools-for-agents)).
   An index MCP server's tool surface is paid every turn — **mitigated by Claude
   Code's deferred MCP tool loading** (§5).
4. **Bulk responses.** "Semantic" ≠ "small": some index tools can still return
   large payloads; expose/prefer concise variants.

---

## 5. Measured savings, how to prove it, and where it does NOT help

### Concrete numbers (label every one by provenance)

| Figure | What it measures | Source | Label |
|---|---|---|---|
| **57% fewer tokens · 71% fewer tool calls · 35% cheaper · 46% faster** (median, 7 repos, Opus 4.7/4.8) | CodeGraph MCP A/B, with vs without | CodeGraph README / developersdigest | **VENDOR** |
| Per-repo token cut: VS Code 64% · Django 60% · Alamofire 64% · OkHttp 54% · Tokio 38% · Gin 23% · Excalidraw 25% | token reduction by repo | CodeGraph README | **VENDOR** |
| Earlier headline **"94% fewer tool calls, 77% faster"** | looser 6-repo run, *later revised down* | developersdigest 2026-05-20 | **VENDOR (superseded)** |
| **Tool calls −55% (14→6.3)** reproduced; **tokens only −23%**; **cost +6.8% (MORE expensive)** | independent A/B on Hono (~280 files) | [HarrisonSec](https://harrisonsec.com/blog/i-tested-codegraph-on-hono-benchmark/) | **COMMUNITY (independent)** |
| serena "~30k raw → few hundred tok overview" | qualitative round-trip | serena/DeepWiki | **COMMUNITY (secondary)** |
| "grep ~2000 tok → LSP ~500 tok; 45s → 50ms" | author estimate | amirteymoori | **COMMUNITY (single author)** |
| **"50–60% fewer exploration tokens" (RepoMix + CodeGraph)** | claimed pairing | — **no primary source found** | **UNVERIFIABLE — do not cite as measured** |

**Skeptical read.** Every favorable token/cost number is **vendor-measured on
designer-chosen repos** — HarrisonSec names *"designer bias… the #1 risk in any
retrieval benchmark,"* and the vendor's own headline fell from "94%" to "71%" once
methodology tightened. The **most robust** finding is **tool-call reduction** (it
reproduces directionally). The **weakest** is **cost**: independent testing
*flipped its sign* (+6.8% vs −35%) — because adding an MCP server changes the cached
prefix (watch `cache_read_input_tokens`). Notably, **serena — the bridge you'd most
likely deploy — publishes no quantitative token figures**, only qualitative claims.

### How to PROVE the savings yourself (A/B protocol)

**Data sources (all first-party, documented):**
- **`/context`** — live per-category breakdown (System prompt, System tools, MCP
  tools, Memory, Messages, Free space). Your peak-context gauge.
- **`/usage`** — per-session token totals; on a plan it *attributes usage to skills,
  subagents, and **individual MCP servers*** ([/costs](https://code.claude.com/docs/en/costs)) — so you can read the
  semantic server's share directly.
- **Plugin "Context cost" estimate** — the `/plugin` detail pane shows *"how many
  tokens the plugin will add to your context window every turn"* (v2.1.143+) — read
  the baseline tax of a code-intelligence plugin *before* installing.
- **Session transcript JSONL** (`~/.claude/projects/…`) — each assistant message
  carries `message.usage`: `input_tokens`, `cache_read_input_tokens`,
  `cache_creation_input_tokens`, `output_tokens`. Sum for ground-truth per-task accounting.
- **Token-counting API** `POST /v1/messages/count_tokens` — free, accepts the same
  `tools` array as create; count a request **with vs without** the MCP tool schemas
  to isolate the standing schema tax ([token-counting](https://platform.claude.com/docs/en/build-with-claude/token-counting)).

**The A/B (mirrors how CodeGraph itself benchmarks):**
1. Fix a task + repo. **Arm A:** semantic tool enabled. **Arm B:** empty MCP config
   (`--strict-mcp-config`), both keeping built-in Read/Grep/Bash.
2. Run each arm **N times and report the median** (CodeGraph uses median-of-4 to
   tame variance) — not best-case.
3. Compare **peak context %** (`/context`) and, from JSONL, total
   `input_tokens + cache_creation + cache_read` and **tool-call count**.
4. Separately, `count_tokens` with vs without the server's schemas → the
   **per-prompt baseline tax** paid every turn regardless of whether the tools fire.
5. Watch `cache_read_input_tokens`: the likely cause of cost moving *independently*
   of raw token counts.

*(This is the same harness Option #9's A/B thrash-test proposes; reuse it.)*

### Where it does NOT help (failure modes — be thorough)

- **Weak/absent language server.** Quality varies by language; serena flags *"find
  implementations: only available for some languages"* and *"find declaration:
  generally will not work for external dependencies."* No LS → no win.
- **Dynamic / reflective / metaprogrammed code.** Static analysis is blind to
  runtime code-gen, reflection, dynamic dispatch, `eval`, monkey-patching, DI
  (acute in Python/Ruby/JS) — the symbols aren't in the graph.
- **When you genuinely must READ the body.** Semantic nav returns *locations,
  signatures, types, references — not implementation.* Novel logic, diff review,
  writing new code in surrounding context still need whole-region reads.
- **Standing baseline tax — but Claude Code defers it by default.** MCP tool
  schemas would load every request; instead **tool search is on by default**: *"Only
  tool names and server instructions load at session start, so adding more MCP
  servers has minimal impact."* `ENABLE_TOOL_SEARCH=auto` loads schemas only when
  they fit within **10% of context**; `=false` loads everything; it's **disabled by
  default on Vertex AI / non-first-party `ANTHROPIC_BASE_URL`**, and **Haiku lacks
  `tool_reference`** ([/mcp](https://code.claude.com/docs/en/mcp)). That this feature
  exists is itself proof the schema tax is real.
- **Bulk-returning IDE/MCP tools.** "Semantic" ≠ "small": JetBrains
  `get_file_text_by_path` returns whole files; `build_project` returns full build
  output; large SQL results are bulky. Claude Code warns above **10,000 tokens** of
  MCP output (`MAX_MCP_OUTPUT_TOKENS`, **default cap 25,000**) — gate these (Option #13).
- **Cold-start / small repos.** Index build + sync is pure overhead; on small repos
  grep is faster and cheaper. HarrisonSec: at Hono's ~280 files, CodeGraph was
  **~7% more expensive** despite −55% tool calls.
- **Heavyweight backends.** serena's JetBrains path needs a full IntelliJ running;
  `rust-analyzer`/`pyright` *"can consume significant memory on large projects"* —
  Anthropic's own remedy: *"disable the plugin… and rely on Claude's built-in search
  tools instead."* Monorepos can also throw **false-positive diagnostics**.
- **Net break-even.** The vendor concedes savings are *"scale-dependent: small and
  noisy on a modest codebase, material only once a repo is large and tangled."*
  Rule of thumb: **high-hundreds-to-low-thousands of files and up**; below that, the
  server's baseline + index cost can exceed what it saves.

---

## Comparison table — the three wire-up paths

| | **Built-in LSP plugin** (official) | **serena / lsp-mcp bridge** | **JetBrains MCP** |
|---|---|---|---|
| Native to Claude Code? | **Yes** (built-in LSP tool) | No (external MCP server) | No (IDE-hosted MCP) |
| Setup | `/plugin install <lang>-lsp` + LS binary | `claude mcp add … uvx serena …` | enable plugin → Auto-Configure |
| Languages | **11 shipped** + DIY | **40+** (serena) | whatever the IDE supports |
| Ops | def, refs, hover, symbols, impls, call-hierarchy, **auto-diagnostics** | same + **symbolic edit/rename** + type-hierarchy | `search_symbol`, `get_symbol_info`, `get_file_problems`, `rename_refactoring`, PSI |
| Baseline context cost | low (plugin; "Context cost" shown pre-install) | **MCP schema tax** (deferred by default) | **MCP schema tax** + full IDE running |
| Refactors files agent never reads | partial (rename) | **yes** (symbolic edits) | **yes** (IDE refactorings) |
| Cache-safe? | yes (no mid-session MCP churn) | only if server stays loaded all session | only if stays loaded |
| Best when | typed language, you want zero-friction + post-edit diagnostics | exotic language / symbolic editing / 40+ langs | already living in IntelliJ; want PSI/inspections/debugger |
| Maturity | **documented/official** | documented/3rd-party (serena very mature) | documented/official (2025.2+) |

**Decision:** typed language → **built-in code-intelligence plugin first** (lowest
friction, cache-safe, Anthropic-endorsed). Need an op/language it lacks, or symbolic
editing → add **serena**. Already in IntelliJ → the **JetBrains MCP** gives PSI +
inspections + debugger, at a heavier baseline.

---

## Version-sensitivity & open questions

- **All findings ~mid-2026** (Claude Code v2.1.143–v2.1.174, LSP 3.17, Opus 4.7/4.8).
  The built-in-LSP-plugin system, `/plugin` "Context cost" (v2.1.143+), the `ide`
  MCP server, and tool-search deferral are **documented** but evolving — re-check.
- **"No LSP" is outdated.** The built-in LSP tool + 11 official plugins is the single
  most version-sensitive correction here; verify the plugin list in your build.
- **JetBrains "bundled & enabled by default"** phrasing differs by IDE build
  (2025.2 vs 2026.1) — confirm it's *enabled*, not just present.
- **serena `--context`**: `claude-code` (not deprecated `ide-assistant`); no npm pkg.
- **Unverified shapes:** `TypeHierarchyItem` response struct (not quoted from
  primary); serena `include_body` default (community-corroborated, not primary).
- **Open questions:** (1) an *independent* token-savings benchmark for the **built-in
  plugin** (all numbers so far are CodeGraph/serena, vendor or single-community);
  (2) whether the built-in LSP tool's *navigation* (not just diagnostics) is exposed
  to the model as callable tools or only used internally — docs describe the
  capability, not a named tool the agent invokes; (3) real break-even repo size;
  (4) how offload-via-index composes with clear+cache (additive or cancel — see
  master report open Q3).

---

## Sources

**Primary — Anthropic (verified verbatim this run):**
[code.claude.com /costs](https://code.claude.com/docs/en/costs) ·
[/vs-code](https://code.claude.com/docs/en/vs-code) ·
[/mcp](https://code.claude.com/docs/en/mcp) ·
[/discover-plugins#code-intelligence](https://code.claude.com/docs/en/discover-plugins#code-intelligence) ·
[/context-window](https://code.claude.com/docs/en/context-window) ·
[platform … token-counting](https://platform.claude.com/docs/en/build-with-claude/token-counting) ·
[anthropic.com effective-context-engineering](https://www.anthropic.com/engineering/effective-context-engineering-for-ai-agents) ·
[anthropic.com writing-tools-for-agents](https://www.anthropic.com/engineering/writing-tools-for-agents)

**Primary — LSP / Sourcegraph:**
[LSP 3.17 spec](https://microsoft.github.io/language-server-protocol/specifications/lsp/3.17/specification/) ·
[Sourcegraph github-vs-sourcegraph](https://sourcegraph.com/docs/getting-started/github-vs-sourcegraph) ·
[search-based code nav](https://4.4.sourcegraph.com/code_navigation/explanations/search_based_code_navigation) ·
[precise code nav](https://sourcegraph.com/docs/code-search/code-navigation/precise_code_navigation) ·
[announcing SCIP](https://sourcegraph.com/blog/announcing-scip)

**Bridges / IDE MCP (third-party, documented):**
[oraios/serena](https://github.com/oraios/serena) ·
[serena clients/install](https://oraios.github.io/serena/02-usage/030_clients.html) ·
[isaacphi/mcp-language-server](https://github.com/isaacphi/mcp-language-server) ·
[JetBrains MCP Server](https://www.jetbrains.com/help/idea/mcp-server.html) ·
[JetBrains/mcp-jetbrains (deprecated)](https://github.com/JetBrains/mcp-jetbrains)

**Code-graph indexers:**
[colbymchenry/codegraph](https://github.com/colbymchenry/codegraph) ·
[CodeGraphContext](https://github.com/CodeGraphContext/CodeGraphContext)

**Benchmarks / community (treat per labels in §5):**
[HarrisonSec independent A/B](https://harrisonsec.com/blog/i-tested-codegraph-on-hono-benchmark/) ·
[developersdigest CodeGraph](https://www.developersdigest.tech/blog/github-trending-codegraph-2026-05-20) ·
[amirteymoori LSP-for-AI](https://amirteymoori.com/lsp-language-server-protocol-ai-coding-tools/) ·
[16x Prompt code→tokens](https://prompt.16x.engineer/blog/code-to-tokens-conversion) ·
serena symbol-retrieval (DeepWiki, secondary) ·
[Marr et al., execution- vs parse-based language servers](https://stefan-marr.de/papers/dls-marr-et-al-execution-vs-parse-based-language-servers/) (LSP limits on dynamic languages)
