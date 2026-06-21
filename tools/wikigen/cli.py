"""wikigen CLI — run the wiki build pipeline.

    python -m tools.wikigen parse           # extract link records (no network)
    python -m tools.wikigen manifest        # dedupe -> manifest.json (no network)
    python -m tools.wikigen fetch [opts]     # download into archive/ (network)
    python -m tools.wikigen build            # render docs/ + report (no network)
    python -m tools.wikigen all [opts]       # manifest -> fetch -> build
    python -m tools.wikigen review [opts]     # approve/reject added articles (workbench)
    python -m tools.wikigen gate             # pre-commit check (exit 1 if unreviewed)
    python -m tools.wikigen install-hook     # install the pre-commit gate

Fetch options: --limit N, --only-host H, --refresh, --refresh-failed
"""

from __future__ import annotations

import typer

from . import manifest as manifest_mod
from . import report as report_mod
from . import review as review_mod
from .fetch import run_fetch
from .parse import parse_all
from .render import render_all

app = typer.Typer(add_completion=False, help="Build the AI-agent engineering knowledge wiki.")


@app.command()
def parse() -> None:
    """Parse todo/*.md into raw link records (diagnostics only)."""
    records = parse_all()
    typer.echo(f"parsed {len(records)} link records from todo/*.md")


@app.command()
def manifest() -> None:
    """Build and save the deduped manifest.json."""
    m = manifest_mod.build_manifest(parse_all())
    manifest_mod.save_manifest(m)
    typer.echo(f"manifest: {len(m)} unique sources -> {manifest_mod.settings.MANIFEST_PATH}")


@app.command()
def fetch(
    limit: int = typer.Option(None, help="Only fetch the first N sources."),
    only_host: str = typer.Option(None, help="Only fetch URLs whose host contains this string."),
    refresh: bool = typer.Option(False, help="Refetch everything, ignoring the cache."),
    refresh_failed: bool = typer.Option(False, help="Refetch only previously failed sources."),
) -> None:
    """Politely download sources into the local-only archive/."""
    m = manifest_mod.load_manifest()
    run_fetch(m, refresh=refresh, refresh_failed=refresh_failed, limit=limit, only_host=only_host)
    manifest_mod.save_manifest(m)
    typer.echo("fetch complete; manifest fetch-status updated")


@app.command()
def build() -> None:
    """Render the public docs/ tree and write the build report."""
    m = manifest_mod.load_manifest()
    render_all(m)
    report_mod.print_summary(report_mod.build_report(m))


@app.command()
def all(
    limit: int = typer.Option(None, help="Only fetch the first N sources."),
    only_host: str = typer.Option(None, help="Only fetch URLs whose host contains this string."),
    refresh: bool = typer.Option(False, help="Refetch everything, ignoring the cache."),
    refresh_failed: bool = typer.Option(False, help="Refetch only previously failed sources."),
) -> None:
    """manifest -> fetch -> build."""
    m = manifest_mod.build_manifest(parse_all())
    manifest_mod.save_manifest(m)
    typer.echo(f"manifest: {len(m)} unique sources")
    run_fetch(m, refresh=refresh, refresh_failed=refresh_failed, limit=limit, only_host=only_host)
    manifest_mod.save_manifest(m)
    render_all(m)
    report_mod.print_summary(report_mod.build_report(m))


def _print_article(i: int, n: int, art: dict) -> None:
    typer.echo("")
    typer.secho(f"[ {i}/{n} ]  {art['theme']} / {art['org']}", fg="cyan", bold=True)
    typer.secho(f"  {art['title']}", bold=True)
    meta = f"  status: {art['fetch_status']}   {art['url_external']}"
    typer.echo(meta)
    if art["subtopic"]:
        typer.echo(f"  subtopic: {art['subtopic']}")


@app.command()
def review(
    all_: bool = typer.Option(False, "--all", help="Re-review every added article, even approved ones."),
    cli: bool = typer.Option(False, "--cli", help="Use the terminal prompt instead of the visual page."),
    no_browser: bool = typer.Option(False, "--no-browser", help="Don't auto-open the browser."),
) -> None:
    """Workbench: review added/changed articles in a visual approval page, pre-commit."""
    ledger = review_mod.load_ledger()
    pending = review_mod.pending_articles(ledger, include_all=all_)
    if not pending:
        typer.echo("Nothing to review — every added article is approved.")
        return

    if not cli:
        summary = review_mod.serve_workbench(pending, ledger, open_browser=not no_browser)
        typer.echo("")
        typer.secho(
            f"approved {summary['approved']}   rejected {summary['rejected']}   "
            f"skipped {summary['skipped'] + summary['pending']}",
            bold=True,
        )
        if summary["skipped"] + summary["pending"]:
            typer.secho(
                "  ⏸ some articles are still unreviewed — the pre-commit gate will block.",
                fg="yellow",
            )
        return

    server = review_mod.PreviewServer()
    serving = False
    if not no_browser:
        typer.echo("Starting preview server (mkdocs serve)…")
        serving = server.start()
        if serving and server.reused:
            typer.echo(f"Using the server already running at {review_mod.PREVIEW_BASE}")
        elif serving:
            typer.echo(f"Preview server up at {review_mod.PREVIEW_BASE}")
        else:
            typer.secho("Couldn't start a preview server; showing URLs instead.", fg="yellow")

    approved: list[dict] = []
    rejected: list[dict] = []
    skipped: list[dict] = []
    n = len(pending)
    try:
        i = 0
        while i < n:
            art = pending[i]
            _print_article(i + 1, n, art)
            if serving:
                server.open(art["preview_url"])
            else:
                typer.echo(f"  preview: {art['preview_url']}")
            choice = typer.prompt(
                "  [a]pprove  [r]eject  [s]kip  [o]pen  [q]uit", default="s"
            ).strip().lower()
            if choice in ("o", "open"):
                if serving:
                    server.open(art["preview_url"])
                else:
                    typer.echo(f"  {art['preview_url']}")
                continue  # same article again
            if choice in ("q", "quit"):
                skipped.extend(pending[i:])
                break
            if choice in ("a", "approve"):
                ledger[art["canonical_url"]] = {
                    "decision": "approved",
                    "review_hash": art["review_hash"],
                    "title": art["title"],
                    "dest_path": art["rel"],
                    "reviewed_at": review_mod.today(),
                }
                approved.append(art)
            elif choice in ("r", "reject"):
                reason = typer.prompt("  reason (optional)", default="").strip()
                ledger[art["canonical_url"]] = {
                    "decision": "rejected",
                    "reason": reason or None,
                    "review_hash": art["review_hash"],
                    "title": art["title"],
                    "dest_path": art["rel"],
                    "reviewed_at": review_mod.today(),
                }
                rejected.append(art)
            else:  # skip / unrecognized
                skipped.append(art)
            i += 1
    finally:
        server.stop()

    review_mod.save_ledger(ledger)
    review_mod.finalize(approved, rejected, skipped)

    typer.echo("")
    typer.secho(
        f"approved {len(approved)}   rejected {len(rejected)}   skipped {len(skipped)}",
        bold=True,
    )
    if approved:
        typer.echo("  ✓ approved pages are staged for commit.")
    if rejected:
        typer.echo("  ✗ rejected pages were removed and recorded in the ledger.")
    if skipped:
        typer.secho(
            f"  ⏸ {len(skipped)} still unreviewed — the pre-commit gate will block until done.",
            fg="yellow",
        )


@app.command()
def gate() -> None:
    """Pre-commit check: exit 1 if any added article is still unreviewed."""
    raise typer.Exit(review_mod.run_gate())


@app.command(name="install-hook")
def install_hook() -> None:
    """Install the pre-commit gate into .git/hooks/pre-commit."""
    hooks_dir = review_mod.settings.REPO_ROOT / ".git" / "hooks"
    if not hooks_dir.exists():
        typer.secho("No .git/hooks directory — not a git repository?", fg="red")
        raise typer.Exit(1)
    dest = hooks_dir / "pre-commit"
    if dest.exists() and dest.read_text(encoding="utf-8") != review_mod.PRE_COMMIT_HOOK:
        backup = hooks_dir / "pre-commit.bak"
        backup.write_text(dest.read_text(encoding="utf-8"), encoding="utf-8")
        typer.echo(f"Backed up the existing hook -> {backup}")
    dest.write_text(review_mod.PRE_COMMIT_HOOK, encoding="utf-8")
    dest.chmod(0o755)
    typer.echo(f"Installed pre-commit gate -> {dest}")


if __name__ == "__main__":
    app()
