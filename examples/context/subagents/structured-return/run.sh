#!/usr/bin/env bash
# Mitigation demo: same Haiku overflow as ../overflow, but a structured output
# contract makes the canary survive — the subagent returns a validated JSON object
# filled from the source, not a recollection from a compacted transcript.
set -euo pipefail
cd "$(dirname "$0")"

N="${N:-40}"
LINES="${LINES:-1500}"
CANARY="CANARY=magpie-$$"

rm -rf filler logs && mkdir -p filler logs
{ echo "$CANARY"; yes "filler line — ignore me, keep reading" | head -n "$LINES"; } > filler/000.txt
for i in $(seq 1 $((N - 1))); do
  printf -v f 'filler/%03d.txt' "$i"
  yes "filler line — ignore me, keep reading" | head -n "$LINES" > "$f"
done
echo "Generated $N filler files. Planted: $CANARY"
echo

claude -p "$(cat prompt.md)" \
  --permission-mode bypassPermissions \
  --allowedTools Task Read \
  2>&1 | tee logs/run.out

echo
echo "===== Verify the structured return ====="
json="$(grep -o '{[^{}]*"canary"[^{}]*}' logs/run.out | tail -1 || true)"
got="$(jq -r '.canary' <<<"$json" 2>/dev/null || true)"
echo "returned JSON : ${json:-<none found>}"
echo "canary field  : ${got:-<unparseable>}"
echo "planted truth : $CANARY"
if [ "$got" = "$CANARY" ]; then
  echo "RESULT: PASS — fact survived overflow via the output contract."
else
  echo "RESULT: FAIL — contract/instructions need tightening (or bump N to overflow harder)."
fi
