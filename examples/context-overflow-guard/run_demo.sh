#!/usr/bin/env bash
# Proof harness for the context-overflow guard.
#   1. Shows the REAL deny JSON the hook emits for an oversized read.
#   2. Runs the full automated test suite.
set -euo pipefail
cd "$(dirname "$0")"

HOOK="hooks/guard_context.py"
BIG="$(mktemp)"
SMALL="$(mktemp)"
trap 'rm -f "$BIG" "$SMALL"' EXIT
python3 -c "open('$BIG','w').write('x'*200000)"   # ~50k tokens
python3 -c "open('$SMALL','w').write('y'*1000)"   # ~250 tokens

echo "=============================================================="
echo " LIVE: oversized read  ->  hook output"
echo "=============================================================="
printf '{"tool_name":"Read","tool_input":{"file_path":"%s"}}' "$BIG" \
  | python3 "$HOOK" | python3 -m json.tool

echo
echo "=============================================================="
echo " LIVE: small read  ->  hook output (empty == allowed)"
echo "=============================================================="
printf '{"tool_name":"Read","tool_input":{"file_path":"%s"}}' "$SMALL" \
  | python3 "$HOOK"
echo "(no output -> allowed)"

echo
echo "=============================================================="
echo " AUTOMATED TEST SUITE"
echo "=============================================================="
python3 -m unittest discover -s test -p 'test_*.py' -v

echo
echo "=============================================================="
echo " BAD vs GOOD EXAMPLES (anti-patterns, asserted)"
echo "=============================================================="
python3 examples/show_bad_vs_good.py
