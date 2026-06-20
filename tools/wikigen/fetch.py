"""Stage 3 — politely download every source into the local-only archive/.

Per source we save the raw response under archive/raw/, the full extracted
markdown under archive/text/, and a small archive/extract/<sha1>.json (title +
excerpt + metadata) that the renderer consumes. The manifest's `fetch` block is
updated in place. Results are cached (ETag/Last-Modified) so re-runs are cheap;
use refresh / refresh_failed to override.
"""

from __future__ import annotations

import asyncio
import io
import json
import re
import time
from datetime import date
from urllib.parse import urlsplit
from urllib.robotparser import RobotFileParser

import httpx
from tenacity import (
    AsyncRetrying,
    retry_if_exception_type,
    stop_after_attempt,
    wait_exponential,
)

from . import settings
from .extract import extract_html, pick_excerpt

_ARXIV_ID = re.compile(r"arxiv\.org/(?:abs|pdf|html)/([^/?#\s]+?)(?:v\d+)?(?:\.pdf)?$", re.I)


class _RetryableStatus(Exception):
    def __init__(self, status: int):
        self.status = status
        super().__init__(f"retryable HTTP {status}")


def _load_cache() -> dict:
    if settings.CACHE_INDEX.exists():
        return json.loads(settings.CACHE_INDEX.read_text(encoding="utf-8"))
    return {}


def _save_cache(cache: dict) -> None:
    settings.CACHE_INDEX.write_text(
        json.dumps(cache, indent=2, ensure_ascii=False), encoding="utf-8"
    )


def _ensure_dirs() -> None:
    for d in (settings.RAW_DIR, settings.TEXT_DIR, settings.ARCHIVE_DIR / "extract"):
        d.mkdir(parents=True, exist_ok=True)


def _extract_json_path(sha1: str):
    return settings.ARCHIVE_DIR / "extract" / f"{sha1}.json"


class Fetcher:
    def __init__(self, client: httpx.AsyncClient):
        self.client = client
        self.sem = asyncio.Semaphore(settings.CONCURRENCY)
        self._host_locks: dict[str, asyncio.Lock] = {}
        self._host_last: dict[str, float] = {}
        self._robots: dict[str, RobotFileParser | None] = {}
        self._arxiv_lock = asyncio.Lock()
        self._arxiv_last = 0.0

    def _host_lock(self, host: str) -> asyncio.Lock:
        self._host_locks.setdefault(host, asyncio.Lock())
        return self._host_locks[host]

    async def _throttle(self, host: str) -> None:
        last = self._host_last.get(host, 0.0)
        wait = settings.PER_HOST_DELAY - (time.monotonic() - last)
        if wait > 0:
            await asyncio.sleep(wait)
        self._host_last[host] = time.monotonic()

    async def _robots_ok(self, url: str) -> bool:
        host = urlsplit(url).netloc
        if host not in self._robots:
            rp: RobotFileParser | None = RobotFileParser()
            robots_url = f"{urlsplit(url).scheme}://{host}/robots.txt"
            try:
                r = await self.client.get(robots_url, timeout=10.0)
                if r.status_code == 200:
                    rp.parse(r.text.splitlines())
                else:
                    rp = None  # no robots / not found -> allow
            except Exception:
                rp = None
            self._robots[host] = rp
        rp = self._robots[host]
        return True if rp is None else rp.can_fetch(settings.USER_AGENT, url)

    async def _get(self, url: str, headers: dict) -> httpx.Response:
        async for attempt in AsyncRetrying(
            retry=retry_if_exception_type((httpx.TransportError, httpx.TimeoutException, _RetryableStatus)),
            wait=wait_exponential(multiplier=2, max=settings.RETRY_MAX_WAIT),
            stop=stop_after_attempt(settings.RETRY_ATTEMPTS),
            reraise=True,
        ):
            with attempt:
                resp = await self.client.get(url, headers=headers, follow_redirects=True)
                if resp.status_code == 429 or resp.status_code >= 500:
                    retry_after = resp.headers.get("Retry-After")
                    if retry_after and retry_after.isdigit():
                        await asyncio.sleep(min(int(retry_after), 30))
                    raise _RetryableStatus(resp.status_code)
                return resp
        raise RuntimeError("unreachable")

    async def _wayback(self, url: str) -> str | None:
        try:
            r = await self.client.get(
                "http://archive.org/wayback/available", params={"url": url}, timeout=15.0
            )
            snap = r.json().get("archived_snapshots", {}).get("closest", {})
            if snap.get("available"):
                return snap.get("url")
        except Exception:
            pass
        return None

    async def fetch_arxiv(self, entry: dict) -> dict:
        m = _ARXIV_ID.search(entry["canonical_url"]) or _ARXIV_ID.search(entry["url"])
        fetch = entry["fetch"]
        if not m:
            fetch.update(status="error", error="could not parse arXiv id")
            return entry
        arxiv_id = m.group(1)
        async with self._arxiv_lock:
            wait = settings.ARXIV_DELAY - (time.monotonic() - self._arxiv_last)
            if wait > 0:
                await asyncio.sleep(wait)
            try:
                r = await self.client.get(
                    "https://export.arxiv.org/api/query",
                    params={"id_list": arxiv_id, "max_results": 1},
                    timeout=45.0,
                    follow_redirects=True,
                )
            finally:
                self._arxiv_last = time.monotonic()
        import feedparser

        feed = feedparser.parse(r.text)
        if not feed.entries:
            fetch.update(status="not_found", http_status=r.status_code, error="no arXiv entry")
            return entry
        e = feed.entries[0]
        title = getattr(e, "title", entry["title"]).strip()
        summary = re.sub(r"\s+", " ", getattr(e, "summary", "")).strip()
        authors = ", ".join(a.name for a in getattr(e, "authors", []))
        published = getattr(e, "published", "")[:10]
        self._write_extract(
            entry,
            title=title,
            full_md=f"# {title}\n\n*{authors}* — {published}\n\n## Abstract\n\n{summary}\n",
            text=summary,
            author=authors,
            pub_date=published,
            sitename="arXiv",
        )
        fetch.update(status="arxiv", http_status=200, fetched_at=date.today().isoformat())
        return entry

    async def fetch_one(self, entry: dict, *, refresh: bool, cache: dict) -> dict:
        url = entry["url"]
        canonical = entry["canonical_url"]
        sha1 = entry["content_sha1"]
        fetch = entry["fetch"]

        cached = cache.get(canonical)
        if cached and not refresh and cached.get("status") not in (None, "pending"):
            fetch.update(
                status=cached["status"],
                http_status=cached.get("http_status"),
                fetched_at=cached.get("fetched_at"),
                archived_path=cached.get("raw_path"),
                wayback_url=cached.get("wayback_url"),
            )
            return entry

        if entry["source_type"] == "arxiv":
            async with self.sem:
                await self.fetch_arxiv(entry)
            cache[canonical] = {**fetch, "raw_path": None}
            return entry

        host = urlsplit(url).netloc
        async with self.sem:
            async with self._host_lock(host):
                await self._throttle(host)
                try:
                    if not await self._robots_ok(url):
                        fetch.update(status="skipped_robots", error="disallowed by robots.txt")
                        cache[canonical] = {**fetch}
                        return entry
                    headers = {}
                    if cached and not refresh:
                        if cached.get("etag"):
                            headers["If-None-Match"] = cached["etag"]
                        if cached.get("last_modified"):
                            headers["If-Modified-Since"] = cached["last_modified"]
                    resp = await self._get(url, headers)
                except Exception as exc:  # noqa: BLE001 - record and move on
                    fetch.update(status="error", error=f"{type(exc).__name__}: {exc}")
                    fetch["wayback_url"] = await self._wayback(url)
                    cache[canonical] = {**fetch}
                    return entry

        status_code = resp.status_code
        fetch["http_status"] = status_code
        fetch["fetched_at"] = date.today().isoformat()

        if status_code == 304 and cached:
            fetch.update(status=cached["status"], archived_path=cached.get("raw_path"))
            return entry
        if status_code in (401, 402):
            fetch["status"] = "paywall"
        elif status_code == 403:
            fetch["status"] = "forbidden"
        elif status_code in (404, 410):
            fetch["status"] = "not_found"
            fetch["wayback_url"] = await self._wayback(url)
        elif status_code >= 400:
            fetch["status"] = "error"
            fetch["error"] = f"HTTP {status_code}"
        else:
            await self._handle_ok(entry, resp)

        cache[canonical] = {
            **fetch,
            "etag": resp.headers.get("ETag"),
            "last_modified": resp.headers.get("Last-Modified"),
            "content_type": resp.headers.get("Content-Type", ""),
        }
        return entry

    async def _handle_ok(self, entry: dict, resp: httpx.Response) -> None:
        fetch = entry["fetch"]
        sha1 = entry["content_sha1"]
        content_type = resp.headers.get("Content-Type", "").lower()
        is_pdf = entry["source_type"] == "pdf" or "application/pdf" in content_type

        if is_pdf:
            raw_path = settings.RAW_DIR / f"{sha1}.pdf"
            raw_path.write_bytes(resp.content)
            title = entry["title"]
            try:
                from pypdf import PdfReader

                meta = PdfReader(io.BytesIO(resp.content)).metadata
                if meta and meta.title:
                    title = str(meta.title)
            except Exception:
                pass
            self._write_extract(
                entry, title=title,
                full_md=f"# {title}\n\n*(PDF — metadata + link only)*\n",
                text="", sitename=urlsplit(entry["url"]).netloc,
            )
            fetch.update(status="pdf", archived_path=str(raw_path.relative_to(settings.REPO_ROOT)))
            return

        raw_path = settings.RAW_DIR / f"{sha1}.html"
        raw_path.write_text(resp.text, encoding="utf-8")
        ex = extract_html(resp.text, entry["url"])
        if not ex.ok:
            fetch.update(status="extraction_failed", archived_path=str(raw_path.relative_to(settings.REPO_ROOT)))
            self._write_extract(entry, title=ex.title or entry["title"], full_md="", text="")
            return
        self._write_extract(
            entry, title=ex.title or entry["title"], full_md=ex.markdown, text=ex.text,
            description=ex.description,
            author=ex.author, pub_date=ex.date, sitename=ex.sitename, method=ex.method,
        )
        fetch.update(status="ok", archived_path=str(raw_path.relative_to(settings.REPO_ROOT)))

    def _write_extract(self, entry: dict, *, title: str, full_md: str, text: str,
                        description: str | None = None,
                        author=None, pub_date=None, sitename=None, method="none") -> None:
        sha1 = entry["content_sha1"]
        if full_md:
            (settings.TEXT_DIR / f"{sha1}.md").write_text(full_md, encoding="utf-8")
        payload = {
            "title": (title or entry["title"]).strip(),
            "excerpt": pick_excerpt(text, description, settings.EXCERPT_WORDS),
            "author": author,
            "date": pub_date,
            "sitename": sitename,
            "method": method,
            "text_chars": len(text or ""),
        }
        _extract_json_path(sha1).write_text(
            json.dumps(payload, ensure_ascii=False, indent=2), encoding="utf-8"
        )


def _select(manifest: list[dict], *, refresh_failed: bool, limit: int | None,
            only_host: str | None) -> list[dict]:
    sel = manifest
    if only_host:
        sel = [e for e in sel if only_host in urlsplit(e["url"]).netloc]
    if refresh_failed:
        ok = {"ok", "arxiv", "pdf"}
        sel = [e for e in sel if e["fetch"]["status"] not in ok]
    if limit:
        sel = sel[:limit]
    return sel


async def _run(manifest, refresh, refresh_failed, limit, only_host) -> None:
    _ensure_dirs()
    cache = _load_cache()
    targets = _select(manifest, refresh_failed=refresh_failed, limit=limit, only_host=only_host)
    timeout = httpx.Timeout(
        connect=settings.CONNECT_TIMEOUT, read=settings.READ_TIMEOUT, write=10.0, pool=30.0
    )
    headers = {"User-Agent": settings.USER_AGENT}
    async with httpx.AsyncClient(http2=True, headers=headers, timeout=timeout) as client:
        fetcher = Fetcher(client)
        tasks = [fetcher.fetch_one(e, refresh=refresh, cache=cache) for e in targets]
        done = 0
        for coro in asyncio.as_completed(tasks):
            await coro
            done += 1
            if done % 10 == 0 or done == len(tasks):
                print(f"  fetched {done}/{len(tasks)}")
    _save_cache(cache)


def run_fetch(manifest: list[dict], *, refresh: bool = False, refresh_failed: bool = False,
              limit: int | None = None, only_host: str | None = None) -> list[dict]:
    asyncio.run(_run(manifest, refresh, refresh_failed, limit, only_host))
    return manifest
