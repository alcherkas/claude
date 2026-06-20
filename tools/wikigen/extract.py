"""Stage 4 — turn fetched HTML/PDF bytes into clean markdown + metadata.

Primary extractor is trafilatura (best boilerplate removal, native markdown).
Falls back to readability-lxml + markdownify. Full text returned here is written
ONLY to the local archive; the public site uses excerpt() instead.
"""

from __future__ import annotations

import re
from dataclasses import dataclass, field

import trafilatura


@dataclass
class Extraction:
    markdown: str = ""          # full body, local-only
    text: str = ""              # plain text, for the excerpt
    title: str | None = None
    description: str | None = None  # meta/og description — cleanest excerpt source
    author: str | None = None
    date: str | None = None
    sitename: str | None = None
    ok: bool = False
    method: str = "none"
    meta: dict = field(default_factory=dict)


_MIN_CHARS = 200  # below this we treat extraction as failed and fall back


def _trafilatura(html: str, url: str) -> Extraction:
    ex = Extraction()
    md = trafilatura.extract(
        html,
        url=url,
        output_format="markdown",
        include_links=True,
        include_tables=True,
        include_comments=False,
        favor_precision=True,
    )
    txt = trafilatura.extract(
        html, url=url, output_format="txt", include_comments=False, favor_precision=True
    )
    if md and len(md.strip()) >= _MIN_CHARS:
        ex.markdown = md.strip()
        ex.text = (txt or md).strip()
        ex.ok = True
        ex.method = "trafilatura"
    try:
        meta = trafilatura.extract_metadata(html, default_url=url)
        if meta is not None:
            ex.title = getattr(meta, "title", None)
            ex.description = getattr(meta, "description", None)
            ex.author = getattr(meta, "author", None)
            ex.date = getattr(meta, "date", None)
            ex.sitename = getattr(meta, "sitename", None)
    except Exception:
        pass
    return ex


def _readability(html: str) -> Extraction:
    ex = Extraction()
    try:
        from readability import Document
        from markdownify import markdownify
    except Exception:
        return ex
    try:
        doc = Document(html)
        ex.title = doc.short_title() or None
        body_html = doc.summary(html_partial=True)
        md = markdownify(body_html, heading_style="ATX").strip()
        if len(md) >= _MIN_CHARS:
            ex.markdown = md
            ex.text = re.sub(r"\n{3,}", "\n\n", md)
            ex.ok = True
            ex.method = "readability"
    except Exception:
        pass
    return ex


def extract_html(html: str, url: str) -> Extraction:
    ex = _trafilatura(html, url)
    if ex.ok:
        return ex
    fallback = _readability(html)
    if fallback.ok:
        # keep any metadata trafilatura found
        fallback.title = fallback.title or ex.title
        return fallback
    return ex  # not ok; caller emits a stub


def _truncate(text: str, max_words: int) -> str:
    words = [w for w in re.sub(r"\s+", " ", (text or "").strip()).split(" ") if w]
    if not words:
        return ""
    if len(words) <= max_words:
        return " ".join(words)
    return " ".join(words[:max_words]) + " …"


# Lead paragraphs we never want as an excerpt (nav/footer/cookie chrome).
_BOILERPLATE = (
    "subscribe", "newsletter", "sign up", "signup", "delivered to your inbox",
    "cookie", "all systems operational", "skip to", "table of contents",
    "product updates, how-tos", "privacy policy", "accept all", "your inbox",
)


def _is_boilerplate(para: str) -> bool:
    low = para.lower()
    return any(b in low for b in _BOILERPLATE)


def pick_excerpt(text: str, description: str | None, max_words: int) -> str:
    """Choose the best short excerpt.

    Prefer the article's first substantive (non-boilerplate, reasonably long)
    paragraph; fall back to the page's meta description; then to the raw lead.
    """
    for para in (text or "").split("\n"):
        para = para.strip().lstrip("#> *-").strip()
        if len(para) >= 120 and not _is_boilerplate(para):
            return _truncate(para, max_words)
    if description and description.strip():
        return _truncate(description, max_words)
    return _truncate(text, max_words)


def excerpt(text: str, max_words: int) -> str:
    """Back-compat: first `max_words` words of plain text, clearly truncated."""
    return _truncate(text, max_words)
