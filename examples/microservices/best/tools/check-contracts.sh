#!/usr/bin/env bash
# check-contracts.sh — flag breaking OpenAPI changes across the polyrepo before review.
#
# For every <repo>/api/openapi.yaml it diffs the committed version (git HEAD) against the
# working tree using oasdiff (https://www.oasdiff.com/) and fails on a BREAKING change.
# This is the contract-drift gate the naive folder has no equivalent of.
#
# Requires: oasdiff  ->  go install github.com/oasdiff/oasdiff@latest   (or: brew install oasdiff)
# Note: oasdiff only DETECTS the diff; this script is the CI/PR wiring around it.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

if ! command -v oasdiff >/dev/null 2>&1; then
  echo "ERROR: oasdiff not installed. See https://www.oasdiff.com/ (go install / brew)." >&2
  exit 2
fi

fail=0
shopt -s nullglob
for spec in */api/openapi.yaml; do
  repo="${spec%%/*}"
  # Compare committed contract (base) vs working tree (revision).
  if git cat-file -e "HEAD:$spec" 2>/dev/null; then
    base="$(mktemp)"; git show "HEAD:$spec" > "$base"
  else
    echo "• $repo: new contract (no committed baseline) — skipping breaking check"
    continue
  fi
  echo "• $repo: checking $spec for breaking changes…"
  if ! oasdiff breaking "$base" "$spec" --fail-on ERR; then
    echo "  ✗ BREAKING change in $repo — update every consumer (see $repo/CLAUDE.md 'Called by') and PLATFORM_SPEC.md" >&2
    fail=1
  fi
  rm -f "$base"
done

if [ "$fail" -ne 0 ]; then
  echo "Contract check FAILED — breaking changes must be coordinated as a change set (CONTRIBUTING-cross-repo.md)." >&2
  exit 1
fi
echo "Contract check passed: no breaking OpenAPI changes."
