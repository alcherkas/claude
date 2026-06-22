"""Stage 5 — write the PUBLIC docs/ tree from the manifest + extract metadata.

Published pages contain only: the curated annotation, a short excerpt, the
canonical link, and attribution — never the full archived text. The renderer
also rebuilds each theme's landing page from the original todo file (prose kept
verbatim; link bullets rewritten to point at the internal source pages) and
emits awesome-pages .pages files for nav ordering.
"""

from __future__ import annotations

import json
import posixpath
import shutil

import yaml

from . import review, settings
from .manifest import canonicalize
from .parse import _BULLET_RE, _URL_RE, _clean_url, _split_title_annotation

_STATUS_NOTE = {
    "paywall": "The original appears to be behind a login/paywall, so only the link is provided.",
    "forbidden": "The source blocked automated access (HTTP 403); follow the link to read it.",
    "not_found": "The original URL could not be reached (HTTP 404/410) — it may have moved.",
    "error": "The original could not be fetched automatically; follow the link to read it.",
    "skipped_robots": "The site's robots.txt disallows archiving, so only the link is provided.",
    "extraction_failed": "Automated text extraction failed (often JS-rendered); follow the link to read it.",
}
_OK_STATUSES = {"ok", "arxiv", "pdf"}


def _extract_meta(sha1: str) -> dict:
    path = settings.ARCHIVE_DIR / "extract" / f"{sha1}.json"
    if path.exists():
        return json.loads(path.read_text(encoding="utf-8"))
    return {}


def _frontmatter(data: dict) -> str:
    return "---\n" + yaml.safe_dump(data, sort_keys=False, allow_unicode=True) + "---\n"


def _rel(from_doc: str, to_doc: str) -> str:
    """Relative markdown link from one docs-relative path to another."""
    return posixpath.relpath(to_doc, posixpath.dirname(from_doc))


def render_source_page(entry: dict) -> None:
    meta = _extract_meta(entry["content_sha1"])
    fetch = entry["fetch"]
    title = meta.get("title") or entry["title"]
    dest = settings.DOCS_DIR / entry["dest_path"]
    dest.parent.mkdir(parents=True, exist_ok=True)

    fm = {
        "title": title,
        "url": entry["url"],
        "canonical_url": entry["canonical_url"],
        "org": entry["org"],
        "theme": entry["theme"],
        "subtopic": entry["subtopic"] or None,
        "source_type": entry["source_type"],
        "tags": [entry["org"], entry["theme"]],
        "fetch_status": fetch["status"],
        "http_status": fetch["http_status"],
        "fetched_at": fetch["fetched_at"],
        "also_in": entry["also_in"] or None,
    }
    lines = [_frontmatter(fm), f"# {title}\n"]

    if entry["annotation"]:
        lines.append(f"{entry['annotation']}\n")

    excerpt = meta.get("excerpt")
    if excerpt and fetch["status"] in _OK_STATUSES:
        lines.append(f"> **Excerpt from the original:** {excerpt}\n")

    if fetch["status"] not in _OK_STATUSES:
        note = _STATUS_NOTE.get(fetch["status"], "Full text is not archived for this source.")
        wb = fetch.get("wayback_url")
        wb_line = f"\n\n    [View an archived snapshot on the Wayback Machine]({wb})" if wb else ""
        lines.append(f'!!! warning "Full text not archived"\n    {note}{wb_line}\n')

    lines.append(f"[Read the original →]({entry['url']})\n")

    bits = [entry["org"]]
    if meta.get("sitename"):
        bits.append(meta["sitename"])
    if meta.get("date"):
        bits.append(meta["date"])
    if fetch.get("fetched_at"):
        bits.append(f"archived {fetch['fetched_at']}")
    lines.append(f"*Source: {' · '.join(str(b) for b in bits if b)}.*\n")

    if entry["also_in"]:
        refs = ", ".join(
            f"[{settings.THEMES[t]['title']}]({_rel(entry['dest_path'], t + '/index.md')})"
            for t in entry["also_in"]
        )
        lines.append(f"*Also referenced in: {refs}.*\n")

    dest.write_text("\n".join(lines), encoding="utf-8")


def render_theme_landing(theme: str, manifest: list[dict], rejected: set[str]) -> None:
    """Rebuild a theme's landing page from its source todo file.

    Prose is kept verbatim; each link bullet is rewritten to link to the internal
    source page (resolved via the manifest), preserving the curated annotation.
    Bullets for reviewer-rejected sources are dropped entirely.
    """
    src = next(
        (p for p in settings.TODO_DIR.glob("*.md") if settings.theme_for_file(p) == theme),
        None,
    )
    if src is None:
        return  # no todo source available (e.g. a docs-only checkout) — leave as-is
    by_canonical = {e["canonical_url"]: e for e in manifest}
    landing_path = f"{theme}/index.md"

    out = [_frontmatter({"title": settings.THEMES[theme]["title"]})]
    out.append(f"# {settings.THEMES[theme]['title']}\n")

    seen_h1 = False
    for line in src.read_text(encoding="utf-8").splitlines():
        if line.startswith("# ") and not seen_h1:
            seen_h1 = True
            continue  # drop the original H1; we set our own above
        urls = _URL_RE.findall(line)
        if not urls:
            out.append(line)
            continue
        url = _clean_url(urls[-1])
        canonical = canonicalize(url)
        if canonical in rejected:
            continue  # reviewer rejected this source — omit its bullet
        title_block = _BULLET_RE.sub("", line[: line.rfind(urls[-1])])
        title, annotation = _split_title_annotation(title_block)
        entry = by_canonical.get(canonical)
        if entry is None:
            out.append(line)
            continue
        link = _rel(landing_path, entry["dest_path"])
        suffix = f" — {annotation}" if annotation else ""
        out.append(f"- [{title or entry['title']}]({link}){suffix}")

    (settings.DOCS_DIR / landing_path).parent.mkdir(parents=True, exist_ok=True)
    (settings.DOCS_DIR / landing_path).write_text("\n".join(out) + "\n", encoding="utf-8")


def _write_pages(rel_dir: str, data: dict) -> None:
    path = (settings.DOCS_DIR / rel_dir / ".pages") if rel_dir else settings.DOCS_DIR / ".pages"
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(yaml.safe_dump(data, sort_keys=False, allow_unicode=True), encoding="utf-8")


def render_nav(manifest: list[dict]) -> None:
    # Root nav order: Home, the generated themes, any hand-authored static themes, Tags, About.
    _write_pages("", {"nav": ["index.md", *settings.THEME_ORDER, *settings.STATIC_THEMES, "tags.md", "about.md"]})

    orgs_by_theme: dict[str, list[str]] = {t: [] for t in settings.THEME_ORDER}
    org_label: dict[str, str] = {}
    for e in manifest:
        if e["org_slug"] not in orgs_by_theme[e["theme"]]:
            orgs_by_theme[e["theme"]].append(e["org_slug"])
        org_label[e["org_slug"]] = e["org"]

    for theme, org_slugs in orgs_by_theme.items():
        ordered = sorted(
            org_slugs,
            key=lambda s: settings.ORG_ORDER.index(org_label[s])
            if org_label[s] in settings.ORG_ORDER
            else 99,
        )
        _write_pages(theme, {"title": settings.THEMES[theme]["nav"], "nav": ["index.md", *ordered]})
        for s in org_slugs:
            _write_pages(f"{theme}/{s}", {"title": org_label[s]})


def render_all(manifest: list[dict]) -> None:
    # Reviewer-rejected sources are never published (persistent exclusion).
    rejected = review.rejected_canonicals()
    visible = [e for e in manifest if e["canonical_url"] not in rejected]
    # Clean rebuild of the generated theme trees (leaves index.md/about.md/tags.md).
    for theme in settings.THEME_ORDER:
        shutil.rmtree(settings.DOCS_DIR / theme, ignore_errors=True)
    for entry in visible:
        render_source_page(entry)
    for theme in settings.THEME_ORDER:
        render_theme_landing(theme, visible, rejected)
    render_nav(visible)
