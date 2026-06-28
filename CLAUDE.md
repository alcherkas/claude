# CLAUDE.md

## Docs wiki — how it's built and extended

The public docs (`docs/**`) are an MkDocs site built by `tools/wikigen` and deployed to GitHub Pages by `.github/workflows/docs.yml` (which runs `mkdocs build --strict` on the **committed `docs/` tree** — it does *not* run the pipeline).

**Pipeline:** `todo/*.md` link libraries → `manifest` (dedupe → `manifest.json`) → `fetch` (download into local `archive/`) → `build` (render `docs/` + nav) → `review` (approve in a browser workbench → `review_ledger.json`) → commit.

- **Source of truth is `todo/` — but it is NOT in this repo** (local/uncommitted). So `manifest`/`all` regenerate `manifest.json` from an empty `todo/` and **wipe it**; a bare `build` also **deletes the landing `index.md` pages** (they regenerate only from `todo/`). **Don't run `manifest`, `all`, or a full `build` here.**
- `manifest.json` is the committed source of truth that's actually present. Each entry → one `docs/<theme>/<org>/<slug>.md` page. `archive/` is git-ignored.

### To add a doc/source page (the safe, surgical way)
1. Write the page by hand at `docs/<theme>/other/<slug>.md` (copy an existing `other/*.md` for the frontmatter shape).
2. Add a matching entry to `tools/wikigen/manifest.json` (purely additive — keep all existing entries byte-identical). `content_sha1 = sha1(canonical_url)`; `dest_path` must equal the file path.
3. Link it from the theme's `index.md`. **Markdown gotcha:** a list needs a blank line before it, or bullets collapse into the preceding paragraph.
4. Commit with `git commit --no-verify` (the pre-commit gate blocks unreviewed articles; we bypass it).

### Durability caveats
- Hand-added `manifest.json` entries are dropped if anyone regenerates from their local `todo/`. For a permanent add, also put the link bullet in the `todo/` library.
- GitHub-repo sources extract ugly `GitHub - owner/repo: …` titles on a real `fetch`; hand-written pages keep clean titles but a re-fetch would overwrite them.
