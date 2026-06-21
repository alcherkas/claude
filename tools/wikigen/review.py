"""Stage 7 — the review workbench: approve/reject newly added wiki articles.

`build` renders every manifest source into ``docs/``. Before those pages are
committed, ``wikigen review`` walks each *added or changed article page* (a docs
page carrying a ``canonical_url`` — landing pages, ``.pages`` and about/tags are
not articles), opens it in the browser via a live mkdocs server, and records an
approve/reject decision in ``review_ledger.json`` (keyed by ``canonical_url``).

Rejections are persistent: :func:`rejected_canonicals` is consulted by the
renderer and the report, so a rebuild never resurrects a rejected source.
``wikigen gate`` (the pre-commit hook) refuses the commit while any added
article is still unreviewed.

This module is import-safe (no network, no side effects at import time) so the
renderer and report can depend on the ledger helpers.
"""

from __future__ import annotations

import datetime
import hashlib
import json
import posixpath
import re
import shutil
import socket
import subprocess
import sys
import threading
import time
import webbrowser
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import urlsplit

import yaml

from . import settings

PREVIEW_HOST = "127.0.0.1"
PREVIEW_PORT = 8000
PREVIEW_BASE = f"http://{PREVIEW_HOST}:{PREVIEW_PORT}"

# Installed into .git/hooks/pre-commit by `wikigen install-hook`.
PRE_COMMIT_HOOK = """#!/bin/sh
# wikigen review gate — managed by `python -m tools.wikigen install-hook`.
# Blocks a commit while any added/changed wiki article is still unreviewed.
ROOT="$(git rev-parse --show-toplevel)"
PY="$ROOT/.venv/bin/python"
[ -x "$PY" ] || PY="$(command -v python3 || command -v python)"
exec "$PY" -m tools.wikigen gate
"""


# --------------------------------------------------------------------- ledger
def load_ledger() -> dict:
    if settings.REVIEW_LEDGER.exists():
        return json.loads(settings.REVIEW_LEDGER.read_text(encoding="utf-8"))
    return {}


def save_ledger(ledger: dict) -> None:
    ordered = {k: ledger[k] for k in sorted(ledger)}
    settings.REVIEW_LEDGER.write_text(
        json.dumps(ordered, indent=2, ensure_ascii=False) + "\n", encoding="utf-8"
    )


def rejected_canonicals(ledger: dict | None = None) -> set[str]:
    """Canonical URLs the reviewer has rejected — skipped by render + report."""
    ledger = load_ledger() if ledger is None else ledger
    return {url for url, rec in ledger.items() if rec.get("decision") == "rejected"}


def is_approved(ledger: dict, canonical_url: str, content_hash: str) -> bool:
    rec = ledger.get(canonical_url)
    return bool(
        rec
        and rec.get("decision") == "approved"
        and rec.get("review_hash") == content_hash
    )


def today() -> str:
    return datetime.date.today().isoformat()


# ----------------------------------------------------------------- page parsing
_FM_RE = re.compile(r"^---\n(.*?)\n---\n", re.DOTALL)
_FETCHED_AT_RE = re.compile(r"^fetched_at:.*$\n?", re.MULTILINE)


def read_frontmatter(text: str) -> dict:
    m = _FM_RE.match(text)
    if not m:
        return {}
    try:
        return yaml.safe_load(m.group(1)) or {}
    except yaml.YAMLError:
        return {}


def review_hash(text: str) -> str:
    """Content hash of a docs page, ignoring the volatile ``fetched_at`` line.

    A re-fetch (new timestamp) must not invalidate an approval, but a changed
    annotation, excerpt, classification or fetch status must.
    """
    normalized = _FETCHED_AT_RE.sub("", text)
    return hashlib.sha1(normalized.encode("utf-8")).hexdigest()


def is_article_page(path: Path) -> bool:
    """True for a per-source page (has ``canonical_url``), not a landing/nav page."""
    if path.suffix != ".md" or path.name in {"index.md", "about.md", "tags.md"}:
        return False
    try:
        head = path.read_text(encoding="utf-8")[:4096]
    except OSError:
        return False
    return "canonical_url:" in head


def _site_base_path() -> str:
    """Path prefix the dev server serves under, taken from mkdocs.yml's site_url.

    MkDocs >=1.x mounts ``mkdocs serve`` under the path component of ``site_url``
    (e.g. ``/claude/``); pages at the bare root would 404. Returns a leading-slash
    path with no trailing slash (``/claude``), or ``""`` when no path is set.
    """
    cfg = settings.REPO_ROOT / "mkdocs.yml"
    try:
        for line in cfg.read_text(encoding="utf-8").splitlines():
            if line.startswith("site_url:"):
                url = line.split(":", 1)[1].strip().strip("'\"")
                return urlsplit(url).path.rstrip("/")
    except OSError:
        pass
    return ""


def site_url_for(rel_docs_path: str) -> str:
    """docs-relative ``.md`` path -> live preview URL (use_directory_urls default)."""
    p = rel_docs_path[:-3] if rel_docs_path.endswith(".md") else rel_docs_path
    if p.endswith("/index") or p == "index":
        p = p[: -len("index")]
    else:
        p = p + "/"
    return f"{PREVIEW_BASE}{_site_base_path()}/{p}"


def _body_bits(text: str) -> tuple[str, str]:
    """Pull the curated annotation (first paragraph) and excerpt from a page body."""
    body = _FM_RE.sub("", text, count=1)
    annotation = ""
    excerpt = ""
    para: list[str] = []
    for raw in body.splitlines():
        line = raw.strip()
        if line.startswith("> **Excerpt"):
            excerpt = line.split("**")[-1].strip()
            continue
        if annotation:
            continue
        if not line:
            if para:
                annotation = " ".join(para)
                para = []
            continue
        if line.startswith(("#", ">", "[", "!!!", "*", "|")):
            continue
        para.append(line)
    if not annotation and para:
        annotation = " ".join(para)
    return annotation, excerpt


def load_article(path: Path) -> dict:
    text = path.read_text(encoding="utf-8")
    fm = read_frontmatter(text)
    rel = path.relative_to(settings.DOCS_DIR).as_posix()
    annotation, excerpt = _body_bits(text)
    return {
        "path": path,
        "rel": rel,
        "text": text,
        "canonical_url": fm.get("canonical_url") or fm.get("url") or "",
        "url_external": fm.get("url") or "",
        "title": fm.get("title") or path.stem,
        "org": fm.get("org") or "",
        "theme": fm.get("theme") or "",
        "subtopic": fm.get("subtopic") or "",
        "fetch_status": fm.get("fetch_status") or "",
        "annotation": annotation,
        "excerpt": excerpt,
        "review_hash": review_hash(text),
        "preview_url": site_url_for(rel),
        "status": "pending",
        "reason": None,
    }


# -------------------------------------------------------------------------- git
def _git(*args: str) -> str:
    res = subprocess.run(
        ["git", *args], cwd=settings.REPO_ROOT, capture_output=True, text=True
    )
    if res.returncode != 0:
        raise RuntimeError(f"git {' '.join(args)} failed:\n{res.stderr.strip()}")
    return res.stdout


def added_article_paths() -> list[Path]:
    """Article pages that are new or modified vs HEAD (staged or not)."""
    rels: set[str] = set()
    diff = _git("diff", "--name-only", "--diff-filter=ACMR", "HEAD", "--", "docs")
    rels.update(ln for ln in diff.splitlines() if ln.endswith(".md"))
    others = _git("ls-files", "--others", "--exclude-standard", "--", "docs")
    rels.update(ln for ln in others.splitlines() if ln.endswith(".md"))
    paths = []
    for rel in sorted(rels):
        p = settings.REPO_ROOT / rel
        if p.exists() and is_article_page(p):
            paths.append(p)
    return paths


def pending_articles(ledger: dict, *, include_all: bool = False) -> list[dict]:
    """Added articles still needing a decision (or every added article if all)."""
    out = []
    for p in added_article_paths():
        art = load_article(p)
        if not art["canonical_url"]:
            continue
        if include_all or not is_approved(ledger, art["canonical_url"], art["review_hash"]):
            out.append(art)
    return out


# -------------------------------------------------------------- apply rejection
def _strip_landing_bullets(dest_rel: str) -> None:
    """Drop any landing-page bullet whose link resolves to a rejected page."""
    for theme in settings.THEME_ORDER:
        landing = settings.DOCS_DIR / theme / "index.md"
        if not landing.exists():
            continue
        link_token = f"]({posixpath.relpath(dest_rel, theme)})"
        lines = landing.read_text(encoding="utf-8").splitlines()
        kept = [ln for ln in lines if link_token not in ln]
        if len(kept) != len(lines):
            landing.write_text("\n".join(kept) + "\n", encoding="utf-8")


def apply_rejection(article: dict) -> None:
    if article["path"].exists():
        article["path"].unlink()
    _strip_landing_bullets(article["rel"])


def _stage(paths) -> None:
    pathspecs = [str(p) for p in paths if p]
    if pathspecs:
        _git("add", "-A", "--", *pathspecs)  # -A so deletions (rejections) stage too


def stage_mechanical() -> None:
    """Stage the generated landing pages + nav — mechanical byproducts of the set."""
    targets = []
    for theme in settings.THEME_ORDER:
        for name in ("index.md", ".pages"):
            f = settings.DOCS_DIR / theme / name
            if f.exists():
                targets.append(f)
    root_pages = settings.DOCS_DIR / ".pages"
    if root_pages.exists():
        targets.append(root_pages)
    _stage(targets)


def approve(ledger: dict, article: dict) -> None:
    ledger[article["canonical_url"]] = {
        "decision": "approved",
        "review_hash": article["review_hash"],
        "title": article["title"],
        "dest_path": article["rel"],
        "reviewed_at": today(),
    }
    save_ledger(ledger)
    _stage([article["path"], settings.REVIEW_LEDGER])


def reject(ledger: dict, article: dict, reason: str = "") -> None:
    ledger[article["canonical_url"]] = {
        "decision": "rejected",
        "reason": reason or None,
        "review_hash": article["review_hash"],
        "title": article["title"],
        "dest_path": article["rel"],
        "reviewed_at": today(),
    }
    save_ledger(ledger)
    apply_rejection(article)
    _stage([article["path"], settings.REVIEW_LEDGER])  # path now gone; -A stages the delete


def finalize(approved: list[dict], rejected: list[dict], skipped: list[dict]) -> None:
    """Apply rejections to the tree and stage decided work; leave skipped alone."""
    for art in rejected:
        apply_rejection(art)

    targets = {str(settings.REVIEW_LEDGER)}
    targets.update(str(a["path"]) for a in approved)
    targets.update(str(a["path"]) for a in rejected)  # `add -A` stages deletions
    if not skipped:
        # Landing pages + nav are mechanical byproducts; only commit them once
        # the whole added set is resolved, so we never stage a half-reviewed theme.
        for theme in settings.THEME_ORDER:
            for name in ("index.md", ".pages"):
                f = settings.DOCS_DIR / theme / name
                if f.exists():
                    targets.add(str(f))
        root_pages = settings.DOCS_DIR / ".pages"
        if root_pages.exists():
            targets.add(str(root_pages))
    _git("add", "-A", "--", *sorted(targets))


# ------------------------------------------------------------------------- gate
def run_gate() -> int:
    """Pre-commit check: non-zero (and a hint) while any added article is unreviewed."""
    pend = pending_articles(load_ledger())
    if not pend:
        return 0
    lines = [f"\n  ✗ {len(pend)} added wiki article(s) are unreviewed:"]
    lines += [f"      - {a['rel']}" for a in pend]
    lines += [
        "",
        "  Review them before committing:",
        "      python -m tools.wikigen review",
        "  (or bypass once with: git commit --no-verify)\n",
    ]
    print("\n".join(lines), file=sys.stderr)
    return 1


# --------------------------------------------------------------- preview server
def _port_open(host: str, port: int, timeout: float = 0.5) -> bool:
    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
        s.settimeout(timeout)
        return s.connect_ex((host, port)) == 0


class PreviewServer:
    """Reuse a running mkdocs server on :8000, or start one for the session."""

    def __init__(self) -> None:
        self.proc: subprocess.Popen | None = None
        self.reused = False

    @staticmethod
    def _find_mkdocs() -> str | None:
        cand = Path(sys.executable).with_name("mkdocs")
        if cand.exists():
            return str(cand)
        return shutil.which("mkdocs")

    def start(self, timeout: float = 45.0) -> bool:
        if _port_open(PREVIEW_HOST, PREVIEW_PORT):
            self.reused = True
            return True
        mkdocs = self._find_mkdocs()
        if not mkdocs:
            return False
        self.proc = subprocess.Popen(
            [mkdocs, "serve", "-a", f"{PREVIEW_HOST}:{PREVIEW_PORT}"],
            cwd=settings.REPO_ROOT,
            stdout=subprocess.DEVNULL,
            stderr=subprocess.DEVNULL,
        )
        deadline = time.monotonic() + timeout
        while time.monotonic() < deadline:
            if self.proc.poll() is not None:
                return False  # server died on startup
            if _port_open(PREVIEW_HOST, PREVIEW_PORT):
                return True
            time.sleep(0.4)
        return False

    def open(self, url: str) -> None:
        try:
            webbrowser.open(url)
        except Exception:
            pass

    def stop(self) -> None:
        if self.proc and self.proc.poll() is None:
            self.proc.terminate()
            try:
                self.proc.wait(timeout=5)
            except subprocess.TimeoutExpired:
                self.proc.kill()


# ----------------------------------------------------------------- web workbench
_PUBLIC_FIELDS = (
    "canonical_url", "rel", "title", "org", "theme", "subtopic",
    "fetch_status", "url_external", "preview_url", "annotation", "excerpt",
    "status", "reason",
)


class Workbench:
    """Shared, thread-safe state behind the visual approval page."""

    def __init__(self, articles: list[dict], ledger: dict, preview_ok: bool) -> None:
        self.lock = threading.Lock()
        self.articles = articles
        self.ledger = ledger
        self.preview_ok = preview_ok
        self.by_id = {a["canonical_url"]: a for a in articles}

    @staticmethod
    def _public(a: dict) -> dict:
        return {k: a[k] for k in _PUBLIC_FIELDS}

    def _counts(self) -> dict:
        c = {"pending": 0, "approved": 0, "rejected": 0, "skipped": 0, "total": len(self.articles)}
        for a in self.articles:
            c[a["status"]] = c.get(a["status"], 0) + 1
        return c

    def snapshot(self) -> dict:
        with self.lock:
            return {
                "articles": [self._public(a) for a in self.articles],
                "counts": self._counts(),
                "preview_ok": self.preview_ok,
            }

    def decide(self, cid: str, decision: str, reason: str = "") -> dict | None:
        with self.lock:
            art = self.by_id.get(cid)
            if art is None:
                return None
            if decision == "approve":
                approve(self.ledger, art)
                art["status"], art["reason"] = "approved", None
            elif decision == "reject":
                reject(self.ledger, art, reason)
                art["status"], art["reason"] = "rejected", (reason or None)
            elif decision == "skip":
                art["status"], art["reason"] = "skipped", None
            else:
                return None
            return self._public(art)

    def finish(self) -> dict:
        with self.lock:
            remaining = [a for a in self.articles if a["status"] in ("pending", "skipped")]
            if not remaining:
                stage_mechanical()
            return {"remaining": len(remaining), "staged_mechanical": not remaining}

    def summary(self) -> dict:
        with self.lock:
            return self._counts()


class _WBServer(ThreadingHTTPServer):
    daemon_threads = True
    allow_reuse_address = True

    def __init__(self, addr, workbench: Workbench) -> None:
        super().__init__(addr, _Handler)
        self.workbench = workbench


class _Handler(BaseHTTPRequestHandler):
    def log_message(self, *args) -> None:  # keep the terminal quiet
        pass

    @property
    def _wb(self) -> Workbench:
        return self.server.workbench  # type: ignore[attr-defined]

    def _send(self, code: int, body, ctype: str = "application/json") -> None:
        data = body.encode("utf-8") if isinstance(body, str) else body
        self.send_response(code)
        self.send_header("Content-Type", ctype)
        self.send_header("Content-Length", str(len(data)))
        self.send_header("Cache-Control", "no-store")
        self.end_headers()
        self.wfile.write(data)

    def do_GET(self) -> None:
        if self.path in ("/", "/index.html") or self.path.startswith("/?"):
            self._send(200, _INDEX_HTML, "text/html; charset=utf-8")
        elif self.path == "/api/state":
            self._send(200, json.dumps(self._wb.snapshot()))
        else:
            self._send(404, "not found", "text/plain")

    def do_POST(self) -> None:
        length = int(self.headers.get("Content-Length", 0) or 0)
        raw = self.rfile.read(length) if length else b""
        try:
            payload = json.loads(raw or b"{}")
        except json.JSONDecodeError:
            payload = {}
        if self.path == "/api/decision":
            res = self._wb.decide(
                payload.get("id", ""), payload.get("decision", ""), payload.get("reason", "")
            )
            self._send(200 if res else 400, json.dumps(res or {"error": "unknown article"}))
        elif self.path == "/api/finish":
            self._send(200, json.dumps(self._wb.finish()))
            threading.Thread(target=self.server.shutdown, daemon=True).start()
        else:
            self._send(404, "{}")


def _free_port(start: int = 8001, end: int = 8061) -> int:
    for port in range(start, end):
        if not _port_open(PREVIEW_HOST, port):
            return port
    return start


def serve_workbench(articles: list[dict], ledger: dict, *, open_browser: bool = True) -> dict:
    """Start the live preview + the visual approval page; block until finished."""
    preview = PreviewServer()
    preview_ok = preview.start()
    wb = Workbench(articles, ledger, preview_ok)
    server = _WBServer((PREVIEW_HOST, _free_port()), wb)
    url = f"http://{PREVIEW_HOST}:{server.server_address[1]}/"

    print(f"\n  Review workbench: {url}")
    if preview_ok:
        print(f"  Live preview:     {PREVIEW_BASE}{_site_base_path()}/")
    else:
        print("  (mkdocs preview unavailable — pages shown from their metadata only)")
    print("  Approve/reject in the browser, then click Finish (or press Ctrl+C here).\n")

    if open_browser:
        try:
            webbrowser.open(url)
        except Exception:
            pass
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        pass
    finally:
        preview.stop()
    return wb.summary()


_INDEX_HTML = """<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Wiki review workbench</title>
<style>
  :root { --ok:#1a7f37; --no:#cf222e; --skip:#9a6700; --bg:#f6f7f9; --line:#d0d7de; }
  * { box-sizing: border-box; }
  body { margin:0; font:14px/1.5 -apple-system,BlinkMacSystemFont,"Segoe UI",sans-serif; color:#1f2328; }
  header { display:flex; align-items:center; gap:16px; padding:10px 16px;
           background:#1f2430; color:#fff; position:sticky; top:0; z-index:5; }
  header b { font-size:15px; }
  header .counts span { margin-right:10px; opacity:.9; }
  .spacer { flex:1; }
  button { font:inherit; border:0; border-radius:6px; padding:8px 14px; cursor:pointer; }
  .btn-finish { background:#2563eb; color:#fff; }
  main { display:flex; height:calc(100vh - 53px); }
  aside { width:380px; min-width:380px; overflow:auto; background:var(--bg);
          border-right:1px solid var(--line); padding:14px; }
  .card { background:#fff; border:1px solid var(--line); border-radius:10px; padding:14px; }
  .chips { margin:6px 0 10px; }
  .chip { display:inline-block; background:#eef1f4; border-radius:20px; padding:2px 10px;
          margin:2px 4px 2px 0; font-size:12px; }
  .chip.s-ok { background:#dafbe1; color:var(--ok); }
  .chip.s-bad { background:#ffebe9; color:var(--no); }
  h2.title { font-size:17px; margin:2px 0 4px; }
  .anno { color:#333; }
  blockquote { margin:10px 0; padding:8px 12px; border-left:3px solid var(--line);
               background:#fafbfc; color:#555; font-size:13px; }
  .actions { display:flex; gap:8px; margin:12px 0 8px; }
  .actions button { flex:1; color:#fff; font-weight:600; }
  .a-ok { background:var(--ok); } .a-no { background:var(--no); } .a-skip { background:#6e7781; }
  .reason { width:100%; padding:7px 9px; border:1px solid var(--line); border-radius:6px; margin-top:4px; }
  .ext { font-size:12px; }
  ol.queue { list-style:none; margin:14px 0 0; padding:0; }
  ol.queue li { display:flex; align-items:center; gap:8px; padding:7px 8px; border-radius:6px;
                cursor:pointer; font-size:13px; }
  ol.queue li.cur { background:#dbeafe; }
  ol.queue li:hover { background:#eaeef2; }
  .dot { width:9px; height:9px; border-radius:50%; flex:none; background:#c9d1d9; }
  .dot.approved { background:var(--ok); } .dot.rejected { background:var(--no); }
  .dot.skipped { background:var(--skip); }
  .qtitle { white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }
  section.preview { flex:1; position:relative; }
  iframe { width:100%; height:100%; border:0; background:#fff; }
  .note { padding:24px; color:#555; }
  .done { position:fixed; inset:0; background:rgba(31,36,48,.96); color:#fff; display:none;
          flex-direction:column; align-items:center; justify-content:center; gap:14px; z-index:10;
          text-align:center; padding:24px; }
  .kbd { font:12px monospace; background:#eef1f4; border:1px solid var(--line);
         border-radius:4px; padding:1px 5px; }
</style>
</head>
<body>
<header>
  <b>Wiki review</b>
  <span class="counts" id="counts"></span>
  <span class="spacer"></span>
  <span style="opacity:.7;font-size:12px">
    <span class="kbd">a</span> approve · <span class="kbd">r</span> reject ·
    <span class="kbd">s</span> skip · <span class="kbd">↑↓</span> move
  </span>
  <button class="btn-finish" onclick="finish()">Finish</button>
</header>
<main>
  <aside>
    <div class="card" id="card"></div>
    <ol class="queue" id="queue"></ol>
  </aside>
  <section class="preview">
    <iframe id="frame"></iframe>
    <div class="note" id="note" style="display:none"></div>
  </section>
</main>
<div class="done" id="done"></div>
<script>
let S = null, cur = 0;

async function load() {
  S = await (await fetch('/api/state')).json();
  cur = Math.max(0, S.articles.findIndex(a => a.status === 'pending'));
  render();
}
function counts() { return S.counts; }
function nextPending(from) {
  for (let k = 1; k <= S.articles.length; k++) {
    const i = (from + k) % S.articles.length;
    if (S.articles[i].status === 'pending') return i;
  }
  return -1;
}
function esc(s) { return (s||'').replace(/[&<>"]/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;'}[c])); }

function renderCounts() {
  const c = counts();
  document.getElementById('counts').innerHTML =
    `<span>${c.approved}✓</span><span>${c.rejected}✗</span>` +
    `<span>${c.skipped}⏸</span><span>${c.pending} left</span>`;
}
function renderQueue() {
  const q = document.getElementById('queue');
  q.innerHTML = S.articles.map((a, i) =>
    `<li class="${i===cur?'cur':''}" onclick="go(${i})">
       <span class="dot ${a.status}"></span>
       <span class="qtitle">${esc(a.title)}</span></li>`).join('');
}
function renderCard() {
  const a = S.articles[cur];
  const okClass = (a.fetch_status==='ok'||a.fetch_status==='arxiv'||a.fetch_status==='pdf')?'s-ok':'s-bad';
  document.getElementById('card').innerHTML = `
    <div class="chips">
      <span class="chip">${esc(a.theme)}</span>
      <span class="chip">${esc(a.org)}</span>
      <span class="chip ${okClass}">${esc(a.fetch_status||'?')}</span>
      <span class="chip">${esc(a.status)}</span>
    </div>
    <h2 class="title">${esc(a.title)}</h2>
    ${a.subtopic ? `<div style="color:#666;font-size:12px">${esc(a.subtopic)}</div>` : ''}
    <p class="anno">${esc(a.annotation)||'<em>No annotation.</em>'}</p>
    ${a.excerpt ? `<blockquote>${esc(a.excerpt)}</blockquote>` : ''}
    <div class="actions">
      <button class="a-ok" onclick="decide('approve')">Approve</button>
      <button class="a-no" onclick="decide('reject')">Reject</button>
      <button class="a-skip" onclick="decide('skip')">Skip</button>
    </div>
    <input class="reason" id="reason" placeholder="reason for rejection (optional)">
    <div style="margin-top:8px"><a class="ext" target="_blank" href="${esc(a.url_external)}">Open the original ↗</a></div>
  `;
}
function renderPreview() {
  const a = S.articles[cur];
  const f = document.getElementById('frame'), note = document.getElementById('note');
  if (S.preview_ok) {
    f.style.display = 'block'; note.style.display = 'none';
    if (f.dataset.src !== a.preview_url) { f.src = a.preview_url; f.dataset.src = a.preview_url; }
  } else {
    f.style.display = 'none'; note.style.display = 'block';
    note.innerHTML = 'Live preview unavailable. Review from the details on the left, or ' +
      `<a target="_blank" href="${esc(a.url_external)}">open the original ↗</a>.`;
  }
}
function render() { renderCounts(); renderQueue(); renderCard(); renderPreview(); }
function go(i) { cur = i; render(); }

async function decide(decision) {
  const a = S.articles[cur];
  const reason = decision === 'reject' ? (document.getElementById('reason').value || '') : '';
  const r = await fetch('/api/decision', {
    method: 'POST', headers: {'Content-Type': 'application/json'},
    body: JSON.stringify({id: a.canonical_url, decision, reason})
  });
  const upd = await r.json();
  if (upd && upd.canonical_url) S.articles[cur] = upd;
  // recompute counts locally
  S.counts = {pending:0,approved:0,rejected:0,skipped:0,total:S.articles.length};
  for (const x of S.articles) S.counts[x.status]++;
  const n = nextPending(cur);
  if (n >= 0) cur = n;
  render();
}
async function finish() {
  const res = await (await fetch('/api/finish', {method:'POST'})).json();
  const d = document.getElementById('done');
  d.style.display = 'flex';
  d.innerHTML = res.remaining
    ? `<h1>${res.remaining} still unreviewed</h1>
       <p>The pre-commit gate will block the commit until they're approved or rejected.</p>
       <p>You can close this tab and re-run <span class="kbd">wikigen review</span> later.</p>`
    : `<h1>All set ✓</h1><p>Approved pages and landing nav are staged.</p>
       <p>You can close this tab and commit.</p>`;
}
addEventListener('keydown', e => {
  if (['INPUT','TEXTAREA'].includes(e.target.tagName)) return;
  if (e.key === 'a') decide('approve');
  else if (e.key === 'r') decide('reject');
  else if (e.key === 's') decide('skip');
  else if (e.key === 'ArrowDown' || e.key === 'j') { cur = Math.min(cur+1, S.articles.length-1); render(); }
  else if (e.key === 'ArrowUp' || e.key === 'k') { cur = Math.max(cur-1, 0); render(); }
});
load();
</script>
</body>
</html>
"""
