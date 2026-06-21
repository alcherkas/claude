"""Stage 6 — summarize the build: status counts, failures, link rot, integrity.

Pure (no network). Reads the manifest's fetch statuses and the rendered docs/
tree, writes build_report.md (committed) + build_report.json. Review this before
committing docs/ — it surfaces paywalls, dead links, and thin extractions.
"""

from __future__ import annotations

import json
from collections import Counter

from . import review, settings

_OK = {"ok", "arxiv", "pdf"}


def build_report(manifest: list[dict]) -> dict:
    # Report on the published set only — rejected sources aren't rendered, so
    # counting them (or flagging their absent pages) would be misleading.
    rejected = review.rejected_canonicals()
    manifest = [e for e in manifest if e["canonical_url"] not in rejected]

    by_status = Counter(e["fetch"]["status"] for e in manifest)
    by_org = Counter(e["org"] for e in manifest)
    by_theme = Counter(e["theme"] for e in manifest)

    failures = [e for e in manifest if e["fetch"]["status"] not in _OK]
    link_rot = [e for e in failures if e["fetch"]["status"] == "not_found"]
    missing_pages = [
        e["dest_path"] for e in manifest if not (settings.DOCS_DIR / e["dest_path"]).exists()
    ]

    summary = {
        "total": len(manifest),
        "ok": sum(by_status[s] for s in _OK),
        "failed": len(failures),
        "by_status": dict(by_status),
        "by_org": dict(by_org),
        "by_theme": dict(by_theme),
        "link_rot": [{"title": e["title"], "url": e["url"], "wayback": e["fetch"].get("wayback_url")} for e in link_rot],
        "missing_pages": missing_pages,
    }
    settings.REPORT_JSON.write_text(json.dumps(summary, indent=2, ensure_ascii=False), encoding="utf-8")

    md = ["# Wiki build report\n", f"**{summary['ok']}/{summary['total']}** sources archived OK.\n"]
    md.append("## By fetch status\n")
    for status, n in by_status.most_common():
        md.append(f"- `{status}`: {n}")
    md.append("\n## By organization\n")
    for org in settings.ORG_ORDER:
        if by_org.get(org):
            md.append(f"- {org}: {by_org[org]}")
    md.append("\n## By theme\n")
    for theme in settings.THEME_ORDER:
        md.append(f"- {settings.THEMES[theme]['title']}: {by_theme.get(theme, 0)}")

    if failures:
        md.append("\n## Needs review (no full text)\n")
        for e in sorted(failures, key=lambda x: x["fetch"]["status"]):
            wb = e["fetch"].get("wayback_url")
            md.append(f"- `{e['fetch']['status']}` — [{e['title'][:70]}]({e['url']})" + (f" · [wayback]({wb})" if wb else ""))
    if missing_pages:
        md.append("\n## ⚠️ Missing rendered pages (build integrity)\n")
        for p in missing_pages:
            md.append(f"- {p}")

    settings.REPORT_MD.write_text("\n".join(md) + "\n", encoding="utf-8")
    return summary


def print_summary(summary: dict) -> None:
    print(f"\n  {summary['ok']}/{summary['total']} OK   failed={summary['failed']}")
    print("  status:", summary["by_status"])
    if summary["missing_pages"]:
        print(f"  ⚠️  {len(summary['missing_pages'])} missing rendered pages")
    print(f"  report -> {settings.REPORT_MD}")
