"""wikigen CLI — run the wiki build pipeline.

    python -m tools.wikigen parse           # extract link records (no network)
    python -m tools.wikigen manifest        # dedupe -> manifest.json (no network)
    python -m tools.wikigen fetch [opts]     # download into archive/ (network)
    python -m tools.wikigen build            # render docs/ + report (no network)
    python -m tools.wikigen all [opts]       # manifest -> fetch -> build

Fetch options: --limit N, --only-host H, --refresh, --refresh-failed
"""

from __future__ import annotations

import typer

from . import manifest as manifest_mod
from . import report as report_mod
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


if __name__ == "__main__":
    app()
