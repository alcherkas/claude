"""Constants and small pure helpers shared across wikigen stages."""

from __future__ import annotations

import re
from pathlib import Path

# --- Paths (resolved relative to the repo root: tools/wikigen/ -> repo) ---
REPO_ROOT = Path(__file__).resolve().parents[2]
TODO_DIR = REPO_ROOT / "todo"
DOCS_DIR = REPO_ROOT / "docs"
THEMES_DIR = DOCS_DIR / "themes"
ARCHIVE_DIR = REPO_ROOT / "archive"          # git-ignored: full text, raw responses, cache
RAW_DIR = ARCHIVE_DIR / "raw"
TEXT_DIR = ARCHIVE_DIR / "text"              # extracted full markdown (local-only)
CACHE_INDEX = ARCHIVE_DIR / "cache_index.json"
WIKIGEN_DIR = Path(__file__).resolve().parent
MANIFEST_PATH = WIKIGEN_DIR / "manifest.json"
REPORT_MD = REPO_ROOT / "build_report.md"
REPORT_JSON = WIKIGEN_DIR / "build_report.json"

# --- Theme mapping: keyed off the source filename's UUID prefix (H1 titles vary) ---
# slug -> {title (full, page H1), nav (short tab label), prefix (filename UUID)}
THEMES: dict[str, dict] = {
    "context-engineering": {
        "title": "Solution-Level Context Engineering Across Multiple Repositories",
        "nav": "Context Engineering",
        "prefix": "6d87f7be",
    },
    "security-governance": {
        "title": "Security, Governance & Safe Autonomy for AI Coding Agents",
        "nav": "Security & Governance",
        "prefix": "3bd81473",
    },
    "orchestration": {
        "title": "Agentic Orchestration & Workflows",
        "nav": "Orchestration",
        "prefix": "b31d71b8",
    },
    "spec-driven": {
        "title": "Spec-Driven, Constraints-Driven & Test-Driven Development",
        "nav": "Spec-Driven Dev",
        "prefix": "9b07b939",
    },
    "evals-observability": {
        "title": "Evaluations, Observability & Quality Gates",
        "nav": "Evals & Observability",
        "prefix": "5549b666",
    },
}
THEME_ORDER = list(THEMES.keys())
_PREFIX_TO_SLUG = {v["prefix"]: slug for slug, v in THEMES.items()}

# --- Organization detection / canonical names + ordering ---
# Matched as lowercase substring against a heading; first hit wins (order matters).
ORG_RULES: list[tuple[str, str]] = [
    ("anthropic", "Anthropic"),
    ("deepmind", "Google"),
    ("google", "Google"),
    ("microsoft", "Microsoft/GitHub"),
    ("github", "Microsoft/GitHub"),
    ("thoughtworks", "ThoughtWorks"),
    ("fowler", "Martin Fowler"),
    ("pci", "PCI DSS"),
]
ORG_ORDER = [
    "Anthropic",
    "Google",
    "Microsoft/GitHub",
    "ThoughtWorks",
    "Martin Fowler",
    "PCI DSS",
    "Other",
]
OTHER_ORG = "Other"

# --- Fetch politeness / robustness (tuned gentle for slow/unreliable networks) ---
USER_AGENT = "claude-wiki-archiver/1.0 (+https://github.com/alcherkas/claude)"
CONCURRENCY = 3               # global cap on simultaneous requests (low: don't saturate the link)
PER_HOST_DELAY = 2.0          # min seconds between requests to the same host
CONNECT_TIMEOUT = 30.0        # generous: slow link may take a while to connect
READ_TIMEOUT = 60.0           # generous: large pages over a slow link
ARXIV_DELAY = 3.0             # arXiv API etiquette: <= 1 req / 3s
RETRY_ATTEMPTS = 5            # patient retries for flaky connections
RETRY_MAX_WAIT = 60.0

# --- Rendering ---
EXCERPT_WORDS = 75            # hard cap on the public excerpt (copyright-safe)


def theme_for_file(path: Path) -> str | None:
    """Return the theme slug for a todo/ source file, by UUID prefix."""
    for prefix, slug in _PREFIX_TO_SLUG.items():
        if prefix in path.name:
            return slug
    return None


_NUM_PREFIX = re.compile(r"^\s*\d+[.)]\s*")


def detect_org(*headings: str | None) -> str:
    """Pick the canonical org from the nearest matching heading.

    Pass headings most-specific-first (e.g. h4, h3, h2); the first heading that
    matches a known org wins. Returns OTHER_ORG if none match.
    """
    for heading in headings:
        if not heading:
            continue
        text = _NUM_PREFIX.sub("", heading).lower()
        for needle, org in ORG_RULES:
            if needle in text:
                return org
    return OTHER_ORG


def classify_source_type(url: str) -> str:
    """Classify a URL as 'arxiv', 'pdf', or 'html'."""
    low = url.lower()
    if "arxiv.org" in low:
        return "arxiv"
    if low.endswith(".pdf") or "/pdf/" in low:
        return "pdf"
    return "html"
