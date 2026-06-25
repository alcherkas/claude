#!/usr/bin/env bash
# Demo: a Haiku subagent overflows its context and loses EARLY details, while we
# observe that PreCompact/PostCompact never fire for it — only SubagentStop does.
set -euo pipefail
cd "$(dirname "$0")"                       # demo dir == project root for this run

N="${N:-12}"                              # filler files (a subagent reads ~9 before stopping)
LINES="${LINES:-2000}"                    # lines per file (~40k tokens each)

# Realistic, varied filler so the planted MARK codes read as genuine details, not flags.
SENTENCES=(
"The build pipeline retries flaky integration tests up to three times before failing."
"Latency budgets for the checkout path are tracked per region on the ops dashboard."
"Each order event is published to the ledger topic and consumed by the billing worker."
"Cache entries expire after fifteen minutes unless a background sweep refreshes them."
"Retry storms are dampened with jittered exponential backoff and a circuit breaker."
)
filler() {                                # print $1 varied lines (no pipe -> safe under pipefail)
  local i; for ((i = 0; i < $1; i++)); do echo "${SENTENCES[i % ${#SENTENCES[@]}]}"; done
}

rm -rf filler logs && mkdir -p filler logs
for i in $(seq 0 $((N - 1))); do
  printf -v key '%03d' "$i"
  code="$(printf '%s-%s' "$$" "$key" | shasum | cut -c1-8)"   # distinct per file, unguessable
  { echo "MARK-$key = $code"; filler "$LINES"; } > "filler/$key.txt"
  echo "$key $code" >> logs/truth.txt
done
echo "Generated $N filler files (~$((N * LINES)) lines). Each hides a unique MARK code on line 1."
echo

claude -p "$(cat prompt.md)" \
  --permission-mode bypassPermissions \
  --allowedTools Task Read \
  2>&1 | tee logs/run.out

echo
echo "===== Per-file MARK recall: truth vs. what the subagent returned ====="
json="$(grep -o '{[^{}]*}' logs/run.out | tail -1 || true)"
kept=0; lost=0; wrong=0
while read -r key code; do
  got="$(jq -r --arg k "$key" '.[$k] // "—"' <<<"$json" 2>/dev/null || echo '—')"
  if   [ "$got" = "$code" ]; then printf '  %s  %-10s kept\n'  "$key" "$code"; kept=$((kept + 1))
  elif [ "$got" = "—"     ]; then printf '  %s  %-10s LOST\n'  "$key" "$code"; lost=$((lost + 1))
  else                            printf '  %s  %-10s WRONG (got %s)\n' "$key" "$code" "$got"; wrong=$((wrong + 1))
  fi
done < logs/truth.txt
echo "kept=$kept lost=$lost wrong=$wrong  — expect the EARLY files LOST, the LATER ones kept."
echo
echo "===== Hooks that fired (logs/fired.log) ====="
sort -u logs/fired.log 2>/dev/null || echo "(none)"
echo "Expected: SubagentStop present; PreCompact and PostCompact absent."
echo
echo "===== Exact payload passed to SubagentStop ====="
jq . logs/last-SubagentStop.json 2>/dev/null || echo "(SubagentStop did not fire)"
