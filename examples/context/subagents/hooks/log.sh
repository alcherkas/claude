#!/usr/bin/env bash
# Records that a hook fired. $1 = event name; the raw JSON payload arrives on stdin.
# Self-locates so it writes to <demo>/logs regardless of cwd.
here="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
logs="$here/logs"; mkdir -p "$logs"
cat > "$logs/last-$1.json"          # exact payload this hook received
printf '%s\n' "$1" >> "$logs/fired.log"
exit 0
