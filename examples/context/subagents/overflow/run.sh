#!/usr/bin/env bash
# Demo: a Haiku subagent overflows its context, loses a mid-task detail, and we
# observe that PreCompact/PostCompact never fire for it — only SubagentStop does.
set -euo pipefail
cd "$(dirname "$0")"                       # demo dir == project root for this run

N="${N:-40}"                              # filler files (each ~12k tokens)
LINES="${LINES:-1500}"                    # lines per file
CANARY="CANARY=magpie-$$"                 # planted in filler/000.txt, recalled at the end

rm -rf filler logs && mkdir -p filler logs
{ echo "$CANARY"; yes "filler line — ignore me, keep reading" | head -n "$LINES"; } > filler/000.txt
for i in $(seq 1 $((N - 1))); do
  printf -v f 'filler/%03d.txt' "$i"
  yes "filler line — ignore me, keep reading" | head -n "$LINES" > "$f"
done
echo "Generated $N filler files (~$((N * LINES)) lines total). Planted: $CANARY"
echo

claude -p "$(cat prompt.md)" \
  --permission-mode bypassPermissions \
  --allowedTools Task Read \
  2>&1 | tee logs/run.out

echo
echo "===== Hooks that fired (logs/fired.log) ====="
sort -u logs/fired.log 2>/dev/null || echo "(none)"
echo "Expected: SubagentStop present; PreCompact and PostCompact absent."
echo
echo "===== Exact payload passed to SubagentStop ====="
jq . logs/last-SubagentStop.json 2>/dev/null || echo "(SubagentStop did not fire)"
echo
echo "The subagent's final line above should be a garbled canary or 'CANARY LOST'"
echo "($CANARY was the truth) — mid-task detail dropped when its context compacted."
