# wikigen

Builds the **AI-Agent Engineering Knowledge Wiki** (an MkDocs Material site) from
the curated link libraries in [`todo/`](../../todo). Run locally; commit the
generated `docs/`. CI only builds + deploys (it never fetches).

## Setup

```bash
python3 -m venv .venv
./.venv/bin/python -m pip install -r requirements.txt -r requirements-fetch.txt
```

## Pipeline

```bash
# From the repo root:
python -m tools.wikigen manifest    # parse todo/*.md -> deduped manifest.json (no network)
python -m tools.wikigen fetch       # download all sources into the local-only archive/
python -m tools.wikigen build       # render docs/ (annotations + excerpts + links) + report
python -m tools.wikigen all         # manifest -> fetch -> build (idempotent; uses the cache)
```

Fetch options: `--limit N`, `--only-host HOST`, `--refresh` (ignore cache),
`--refresh-failed` (retry only previously-failed sources).

Then preview / validate the site:

```bash
./.venv/bin/mkdocs serve            # http://127.0.0.1:8000
./.venv/bin/mkdocs build --strict   # the same gate CI runs
```

## Review workbench (approve before commit)

`build` renders every source into `docs/`. Before committing, review the
**added/changed article pages** and approve or reject each one:

```bash
python -m tools.wikigen install-hook   # one-time: install the pre-commit gate
python -m tools.wikigen review         # opens the visual approval page
```

`review` starts a live `mkdocs serve` preview plus a local **approval page**
(opened in your browser). Each added article shows its rendered page in an
iframe next to a queue and Approve / Reject / Skip controls (keyboard:
`a` / `r` / `s`, `↑` `↓` to move). Decisions apply immediately:

- **approve** → records the decision in `review_ledger.json` and stages the page.
- **reject** → records a persistent exclusion (keyed by URL), removes the page,
  and strips its bullet from the landing pages. `render` and `report` skip
  ledger-rejected sources, so a future `build` never resurrects them.
- **skip** → leaves it for later; the gate keeps blocking the commit.

Click **Finish** (or `Ctrl+C` in the terminal) when done — that stages the
generated landing pages + nav once nothing is left unreviewed.

`review_ledger.json` is committed so rejections are reproducible. The
pre-commit gate (`python -m tools.wikigen gate`) aborts a commit while any added
article is unreviewed — bypass once with `git commit --no-verify` if needed.
Flags: `--all` re-reviews everything, `--no-browser` won't auto-open the tab,
`--cli` falls back to a terminal prompt (handy over SSH).

## What gets published vs. kept local

- **`docs/`** (committed, published): per-source pages with the curated
  annotation, a short excerpt, and the canonical link — plus the five theme
  landing pages. Copyright-safe.
- **`archive/`** (git-ignored, local only): full extracted text, raw responses,
  and the fetch cache. Never published.
- **`build_report.md`** (committed): fetch status counts, link rot, and integrity
  checks. Review after each run.

## Layout

| File | Stage |
|------|-------|
| `settings.py` | paths, theme/org maps, fetch politeness, excerpt cap |
| `parse.py`    | todo/*.md → link records |
| `manifest.py` | canonicalize + dedupe → `manifest.json` |
| `fetch.py`    | polite async download → `archive/` (robots, retries, arXiv API, PDF, cache) |
| `extract.py`  | HTML → markdown (trafilatura → readability fallback) + excerpt selection |
| `render.py`   | manifest → public `docs/` pages, landing pages, nav `.pages` (skips rejected) |
| `report.py`   | `build_report.md` + integrity check (skips rejected) |
| `review.py`   | review workbench: ledger, browser preview, pre-commit gate |
| `cli.py`      | `parse` / `manifest` / `fetch` / `build` / `all` / `review` / `gate` / `install-hook` |
