#!/usr/bin/env bash
# Demo: a Haiku subagent overflows its context, loses a mid-task detail, and we
# observe that PreCompact/PostCompact never fire for it — only SubagentStop does.
set -euo pipefail
cd "$(dirname "$0")"                       # demo dir == project root for this run

N="${N:-20}"                              # filler files (a subagent often reads ~10 then stops)
LINES="${LINES:-2000}"                    # lines per file
CANARY="CANARY=magpie-$$"                 # planted in filler/000.txt, recalled at the end

# Long lines so a handful of Reads alone blow past Haiku's window (~45k tokens/file),
# forcing overflow well within the ~10 files the subagent actually reads.
LINE="filler filler filler — ignore me, the canary lives only in file 000, keep reading the rest"
filler() {                                # print $1 filler lines (no pipe -> safe under pipefail)
  local i; for ((i = 0; i < $1; i++)); do echo "$LINE"; done
}

rm -rf filler logs && mkdir -p filler logs
{ echo "$CANARY"; filler "$LINES"; } > filler/000.txt
for i in $(seq 1 $((N - 1))); do
  printf -v f 'filler/%03d.txt' "$i"
  filler "$LINES" > "$f"
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
