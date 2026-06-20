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
| `render.py`   | manifest → public `docs/` pages, landing pages, nav `.pages` |
| `report.py`   | `build_report.md` + integrity check |
| `cli.py`      | `parse` / `manifest` / `fetch` / `build` / `all` |
