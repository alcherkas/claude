"""Stage 2 — dedupe + canonicalize link records into a manifest.

The manifest is the build's source of truth (committed). One entry per unique
URL; URLs cited in multiple themes are recorded once with the extra themes in
`also_in`, so the renderer can cross-link instead of duplicating pages.
"""

from __future__ import annotations

import hashlib
import json
from urllib.parse import urlsplit, urlunsplit, parse_qsl, urlencode

from slugify import slugify

from . import settings
from .parse import LinkRecord

_TRACKING_PREFIXES = ("utm_",)
_TRACKING_KEYS = {"ref", "fbclid", "gclid", "mc_cid", "mc_eid", "ref_src", "spm"}


def canonicalize(url: str) -> str:
    parts = urlsplit(url)
    scheme = (parts.scheme or "https").lower()
    host = parts.hostname or ""
    if host.startswith("www."):
        host = host[4:]
    netloc = host
    if parts.port and not (
        (scheme == "https" and parts.port == 443)
        or (scheme == "http" and parts.port == 80)
    ):
        netloc = f"{host}:{parts.port}"
    query = urlencode(
        [
            (k, v)
            for k, v in parse_qsl(parts.query, keep_blank_values=True)
            if not k.lower().startswith(_TRACKING_PREFIXES)
            and k.lower() not in _TRACKING_KEYS
        ]
    )
    path = parts.path.rstrip("/") or "/"
    return urlunsplit((scheme, netloc, path, query, ""))  # fragment dropped


def _short_hash(text: str) -> str:
    return hashlib.sha1(text.encode("utf-8")).hexdigest()[:6]


def _slug_for(record: LinkRecord, canonical: str) -> str:
    base = slugify(record.title, max_length=80, word_boundary=True)
    if not base:
        base = slugify(urlsplit(canonical).path.replace("/", " ")) or "source"
    return base


def build_manifest(records: list[LinkRecord]) -> list[dict]:
    by_canonical: dict[str, dict] = {}
    used_slugs: set[tuple[str, str]] = set()

    for rec in records:
        canonical = canonicalize(rec.url)
        occurrence = {"theme": rec.theme, "org": rec.org, "subtopic": rec.subtopic}
        if canonical in by_canonical:
            entry = by_canonical[canonical]
            if occurrence not in entry["occurrences"]:
                entry["occurrences"].append(occurrence)
            continue

        org_slug = slugify(rec.org)
        slug = _slug_for(rec, canonical)
        if (org_slug, slug) in used_slugs:
            slug = f"{slug}-{_short_hash(canonical)}"
        used_slugs.add((org_slug, slug))

        dest_path = f"{rec.theme}/{org_slug}/{slug}.md"
        by_canonical[canonical] = {
            "url": rec.url,
            "canonical_url": canonical,
            "slug": slug,
            "org_slug": org_slug,
            "title": rec.title,
            "annotation": rec.annotation,
            "source_type": settings.classify_source_type(rec.url),
            "theme": rec.theme,
            "org": rec.org,
            "subtopic": rec.subtopic,
            "source_file": rec.source_file,
            "occurrences": [occurrence],
            "dest_path": dest_path,
            "content_sha1": hashlib.sha1(canonical.encode("utf-8")).hexdigest(),
            # filled in by later stages:
            "fetch": {
                "status": "pending",
                "http_status": None,
                "fetched_at": None,
                "archived_path": None,
                "wayback_url": None,
                "error": None,
            },
        }

    manifest = list(by_canonical.values())
    # Cross-references: themes a URL appears in beyond its canonical one.
    for entry in manifest:
        themes = [o["theme"] for o in entry["occurrences"]]
        entry["also_in"] = sorted({t for t in themes if t != entry["theme"]})
    return manifest


def save_manifest(manifest: list[dict]) -> None:
    settings.MANIFEST_PATH.write_text(
        json.dumps(manifest, indent=2, ensure_ascii=False) + "\n", encoding="utf-8"
    )


def load_manifest() -> list[dict]:
    return json.loads(settings.MANIFEST_PATH.read_text(encoding="utf-8"))
