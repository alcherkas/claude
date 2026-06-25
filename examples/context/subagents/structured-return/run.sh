#!/usr/bin/env bash
# Mitigation for ../overflow: instead of one agent holding every file's MARK code
# (and losing the early ones when it compacts), fan out one bounded `reader` per
# file. Each reader's context stays small, so nothing overflows and each returns a
# structured {"NNN":"code"}. The parent merges them — ALL codes survive, early ones
# included.
set -euo pipefail
cd "$(dirname "$0")"

N="${N:-12}"
LINES="${LINES:-2000}"

# Identical scenario to ../overflow so the contrast is apples-to-apples.
SENTENCES=(
"The build pipeline retries flaky integration tests up to three times before failing."
"Latency budgets for the checkout path are tracked per region on the ops dashboard."
"Each order event is published to the ledger topic and consumed by the billing worker."
"Cache entries expire after fifteen minutes unless a background sweep refreshes them."
"Retry storms are dampened with jittered exponential backoff and a circuit breaker."
)
filler() { local i; for ((i = 0; i < $1; i++)); do echo "${SENTENCES[i % ${#SENTENCES[@]}]}"; done; }

rm -rf filler logs && mkdir -p filler logs
for i in $(seq 0 $((N - 1))); do
  printf -v key '%03d' "$i"
  code="$(printf '%s-%s' "$$" "$key" | shasum | cut -c1-8)"   # distinct per file, unguessable
  { echo "MARK-$key = $code"; filler "$LINES"; } > "filler/$key.txt"
  echo "$key $code" >> logs/truth.txt
done
files="$(cd filler && ls ./*.txt | sort | sed 's#\./#filler/#' | tr '\n' ' ')"
echo "Generated $N filler files. Fanning out one reader per file (each gets its own context)."
echo

prompt="Launch one \`reader\` subagent for EACH file listed below — each reads only its
own file and returns {\"NNN\":\"code\"}. Run them, merge every result into ONE JSON
object covering all files, and print only that merged JSON object.
Files: $files"

claude -p "$prompt" \
  --permission-mode bypassPermissions \
  --allowedTools Task Read \
  2>&1 | tee logs/run.out

echo
echo "===== Per-file MARK recall: truth vs. merged fan-out result ====="
json="$(grep -o '{[^{}]*}' logs/run.out | tail -1 || true)"
kept=0; lost=0; wrong=0
while read -r key code; do
  got="$(jq -r --arg k "$key" '.[$k] // "—"' <<<"$json" 2>/dev/null || echo '—')"
  if   [ "$got" = "$code" ]; then printf '  %s  %-10s kept\n'  "$key" "$code"; kept=$((kept + 1))
  elif [ "$got" = "—"     ]; then printf '  %s  %-10s LOST\n'  "$key" "$code"; lost=$((lost + 1))
  else                            printf '  %s  %-10s WRONG (got %s)\n' "$key" "$code" "$got"; wrong=$((wrong + 1))
  fi
done < logs/truth.txt
echo "kept=$kept lost=$lost wrong=$wrong  — expect ALL kept; no single reader overflowed."
