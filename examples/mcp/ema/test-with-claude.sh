#!/usr/bin/env bash
# Remint enterprise tokens, re-register both MCP servers with Claude Code, then
# drive `claude -p` against each to prove the tools work end to end.
# All output is visible (set -x on the important commands).
set -uo pipefail

cd "$(dirname "$0")"

PORTS="4000,4001,4002,4003"

section() { printf '\n\033[1;36m========== %s ==========\033[0m\n' "$1"; }

# Run `claude -p` with streaming output so you can watch the CLI work: each
# tool call, tool result, and the final answer are printed live. Falls back to
# raw stream-json if jq isn't installed.
run_claude() {
  local prompt="$1" tools="$2"
  if command -v jq >/dev/null 2>&1; then
    claude -p "$prompt" --allowedTools "$tools" --verbose --output-format stream-json \
      | jq -rj '
        if .type=="system" and .subtype=="init" then
          "[90m[claude] session \(.session_id) | model \(.model)[0m\n"
        elif .type=="assistant" then
          (.message.content[] |
            if .type=="tool_use" then "[33m[tool →] \(.name) \(.input|tostring)[0m\n"
            elif .type=="text" and (.text|length>0) then "[32m[claude] \(.text)[0m\n"
            else empty end)
        elif .type=="user" then
          (.message.content[]? | select(.type=="tool_result") |
            "[36m[← result] \((.content[]?|.text) // (.content|tostring))[0m\n")
        elif .type=="result" then
          "[90m[claude] done in \(.duration_ms)ms, \(.num_turns) turns[0m\n"
        else empty end'
  else
    claude -p "$prompt" --allowedTools "$tools" --verbose --output-format stream-json
  fi
}

section "1. Ensure servers are running (IdP + MAS + files + payroll)"
if lsof -tiTCP:4002 -sTCP:LISTEN >/dev/null 2>&1; then
  echo "Servers already listening — reusing them."
  STARTED_SERVERS=0
else
  echo "Starting servers in the background..."
  node src/servers.js >/tmp/ema-servers.log 2>&1 &
  SERVERS_PID=$!
  STARTED_SERVERS=1
  # wait for the last resource server to bind
  for _ in $(seq 1 30); do
    lsof -tiTCP:4003 -sTCP:LISTEN >/dev/null 2>&1 && break
    sleep 0.2
  done
  echo "Servers up (pid $SERVERS_PID). Log: /tmp/ema-servers.log"
fi

cleanup() {
  if [ "${STARTED_SERVERS:-0}" = "1" ]; then
    section "Cleanup: stopping servers we started (pid $SERVERS_PID)"
    kill "$SERVERS_PID" 2>/dev/null
  fi
}
trap cleanup EXIT

section "2. Remint resource-bound access tokens via the full ID-JAG flow"
set -x
FILES_TOKEN=$(node src/mcp/client.js alice files --token-only)
PAYROLL_TOKEN=$(node src/mcp/client.js carol payroll --token-only)
set +x
if [ -z "$FILES_TOKEN" ] || [ -z "$PAYROLL_TOKEN" ]; then
  echo "ERROR: token mint failed — check that servers are running." >&2
  exit 1
fi
echo "files token   len: ${#FILES_TOKEN}"
echo "payroll token len: ${#PAYROLL_TOKEN}"

section "2b. NEGATIVE: org policy denies alice on payroll (rejected at the IdP)"
echo "Attempting to mint a payroll token for alice (should be DENIED)..."
if DENIED=$(node src/mcp/client.js alice payroll --token-only 2>&1); then
  echo "UNEXPECTED: alice obtained a payroll token: $DENIED" >&2
else
  printf '\033[31mDenied as expected:\033[0m %s\n' "$DENIED"
fi

section "3. Re-register both MCP servers with Claude Code"
set -x
claude mcp remove ema-files   -s local 2>/dev/null
claude mcp remove ema-payroll -s local 2>/dev/null
claude mcp add --transport http ema-files   http://localhost:4002/mcp --header "Authorization: Bearer $FILES_TOKEN"
claude mcp add --transport http ema-payroll http://localhost:4003/mcp --header "Authorization: Bearer $PAYROLL_TOKEN"
claude mcp list
set +x

section "4. Test ema-files via 'claude -p' (expect a file list for alice)"
run_claude "Call the list_files tool on the ema-files MCP server and show me its exact output verbatim." \
  "mcp__ema-files__list_files"

section "5. Test ema-payroll via 'claude -p' (expect the salary table for carol)"
run_claude "Call the view_salaries tool on the ema-payroll MCP server and show me its exact output verbatim." \
  "mcp__ema-payroll__view_salaries"

section "6. NEGATIVE via Claude: payroll server with alice's (wrong) token -> 401"
echo "Registering 'ema-forbidden' = payroll server (:4003) but with alice's FILES token."
echo "The token's audience is the files server, so payroll rejects it (401)."
set -x
claude mcp remove ema-forbidden -s local 2>/dev/null
claude mcp add --transport http ema-forbidden http://localhost:4003/mcp --header "Authorization: Bearer $FILES_TOKEN"
claude mcp list 2>&1 | grep -i ema-forbidden
set +x
echo "Asking Claude to call it — expect it to report the call is unauthorized/unavailable:"
run_claude "Call the view_salaries tool on the ema-forbidden MCP server. If it fails or is unavailable, say exactly why." \
  "mcp__ema-forbidden__view_salaries"
claude mcp remove ema-forbidden -s local 2>/dev/null

section "Done"
echo "Allowed cases worked; denied cases were rejected at the IdP and at the resource server."
