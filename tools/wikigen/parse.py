"""Stage 1 — extract link records from the curated todo/*.md files.

Each link lives on a bullet line shaped like:
    - **Title** (annotation) — https://example.com/path
URLs are bare and at the end of the line. We locate the last URL on the line,
treat everything before it as the title block, then peel a trailing parenthetical
as the annotation. Org/subtopic come from the enclosing markdown headings.
"""

from __future__ import annotations

import re
from dataclasses import dataclass, asdict
from pathlib import Path

from . import settings

# Last URL on a line; we strip trailing punctuation that isn't part of a URL.
_URL_RE = re.compile(r"https?://[^\s<>\"]+")
_BULLET_RE = re.compile(r"^\s*[-*+]\s+")
_TRAILING_PAREN_RE = re.compile(r"^(.*?)\s*\(([^()]*)\)\s*$")
_HEADING_RE = re.compile(r"^(#{1,6})\s+(.*?)\s*#*\s*$")


@dataclass
class LinkRecord:
    theme: str
    org: str
    subtopic: str
    title: str
    annotation: str
    url: str
    source_file: str
    line_no: int


def _clean_url(raw: str) -> str:
    """Strip trailing punctuation a URL would not legitimately end with."""
    url = raw.rstrip()
    while url and url[-1] in ".,;:)]}>\"'":
        # Keep a trailing ')' only if the URL contains a matching '(' (e.g. wiki links).
        if url[-1] == ")" and url.count("(") > url.count(")") - 1:
            break
        url = url[:-1]
    return url


def _split_title_annotation(title_block: str) -> tuple[str, str]:
    """Separate a clean title from a trailing-parenthetical annotation."""
    text = title_block.replace("**", "").strip()
    text = re.sub(r"[\s—–:-]+$", "", text).strip()  # drop trailing dash/colon
    m = _TRAILING_PAREN_RE.match(text)
    if m and m.group(1).strip():
        return m.group(1).strip(), m.group(2).strip()
    return text, ""


def parse_file(path: Path) -> list[LinkRecord]:
    theme = settings.theme_for_file(path)
    if theme is None:
        return []
    records: list[LinkRecord] = []
    h2 = h3 = h4 = None
    for line_no, line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
        heading = _HEADING_RE.match(line)
        if heading:
            level, text = len(heading.group(1)), heading.group(2).strip()
            if level == 2:
                h2, h3, h4 = text, None, None
            elif level == 3:
                h3, h4 = text, None
            elif level == 4:
                h4 = text
            continue

        urls = _URL_RE.findall(line)
        if not urls:
            continue
        url = _clean_url(urls[-1])
        title_block = line[: line.rfind(urls[-1])]
        title_block = _BULLET_RE.sub("", title_block)
        title, annotation = _split_title_annotation(title_block)
        if not title:
            title = url

        org = settings.detect_org(h4, h3, h2)
        # Subtopic = the deepest heading that is NOT the one we used as the org.
        subtopic = ""
        for cand in (h4, h3, h2):
            if cand and settings.detect_org(cand) != org:
                subtopic = re.sub(r"^\s*\d+[.)]\s*", "", cand).strip()
                break

        records.append(
            LinkRecord(
                theme=theme,
                org=org,
                subtopic=subtopic,
                title=title,
                annotation=annotation,
                url=url,
                source_file=path.name,
                line_no=line_no,
            )
        )
    return records


def parse_all() -> list[LinkRecord]:
    """Parse every todo/*.md in theme order."""
    records: list[LinkRecord] = []
    files = sorted(
        settings.TODO_DIR.glob("*.md"),
        key=lambda p: settings.THEME_ORDER.index(settings.theme_for_file(p))
        if settings.theme_for_file(p) is not None
        else 99,
    )
    for path in files:
        records.extend(parse_file(path))
    return records


def records_as_dicts(records: list[LinkRecord]) -> list[dict]:
    return [asdict(r) for r in records]
