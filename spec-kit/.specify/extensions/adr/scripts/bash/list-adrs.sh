#!/usr/bin/env bash

# ADR helpers for /speckit.adr workflow.
#
# Usage:
#   list-adrs.sh --next-num <ADR_DIR>          # print zero-padded next ADR number (0001 if dir empty)
#   list-adrs.sh --list <ADR_DIR>              # print JSON array of {number,title,status,file}
#   list-adrs.sh --find <ADR_DIR> <NNNN>       # print absolute path of ADR with that number, or exit 1
#   list-adrs.sh --help

set -e

SCRIPT_DIR="$(CDPATH="" cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Walk up to find the .specify project root, then source core common.sh
# (only `has_jq` and `json_escape` are needed from it).
_find_project_root() {
    local dir="$1"
    while [ "$dir" != "/" ]; do
        if [ -d "$dir/.specify" ]; then
            echo "$dir"
            return 0
        fi
        dir="$(dirname "$dir")"
    done
    return 1
}

REPO_ROOT_HINT=$(_find_project_root "$SCRIPT_DIR") || {
    echo "ERROR: Cannot locate .specify project root from $SCRIPT_DIR" >&2
    exit 1
}
source "$REPO_ROOT_HINT/.specify/scripts/bash/common.sh"

usage() {
    cat <<'EOF'
Usage:
  list-adrs.sh --next-num <ADR_DIR>
  list-adrs.sh --list <ADR_DIR>
  list-adrs.sh --find <ADR_DIR> <NNNN>
  list-adrs.sh --help
EOF
}

# Iterate over ADR files in $1 matching ^NNNN-*.md, in numeric order.
# Calls $2 (a function name) once per file with: number title status file
_each_adr() {
    local dir="$1"
    local fn="$2"
    [[ -d "$dir" ]] || return 0

    local file base num title status
    while IFS= read -r -d '' file; do
        base=$(basename "$file")
        [[ "$base" =~ ^([0-9]{4})- ]] || continue
        num="${BASH_REMATCH[1]}"

        title=$(grep -m1 -E '^# [0-9]{4}\. ' "$file" 2>/dev/null \
            | sed -E 's/^# [0-9]{4}\. //' || true)
        [[ -z "$title" ]] && title="(untitled)"

        status=$(grep -m1 -E '^\*\*Status\*\*:' "$file" 2>/dev/null \
            | sed -E 's/^\*\*Status\*\*:[[:space:]]*//' || true)
        [[ -z "$status" ]] && status="Unknown"

        "$fn" "$num" "$title" "$status" "$file"
    done < <(find "$dir" -maxdepth 1 -type f -name '[0-9][0-9][0-9][0-9]-*.md' -print0 2>/dev/null \
                | sort -z)
}

cmd_next_num() {
    local dir="$1"
    local highest=0
    if [[ -d "$dir" ]]; then
        local file base num
        for file in "$dir"/[0-9][0-9][0-9][0-9]-*.md; do
            [[ -e "$file" ]] || continue
            base=$(basename "$file")
            [[ "$base" =~ ^([0-9]{4})- ]] || continue
            num=$((10#${BASH_REMATCH[1]}))
            (( num > highest )) && highest=$num
        done
    fi
    printf '%04d\n' $((highest + 1))
}

cmd_list() {
    local dir="$1"
    local entries=()

    _collect() {
        local num="$1" title="$2" status="$3" file="$4"
        if has_jq; then
            entries+=("$(jq -cn \
                --arg number "$num" \
                --arg title "$title" \
                --arg status "$status" \
                --arg file "$file" \
                '{number:$number,title:$title,status:$status,file:$file}')")
        else
            entries+=("$(printf '{"number":"%s","title":"%s","status":"%s","file":"%s"}' \
                "$(json_escape "$num")" \
                "$(json_escape "$title")" \
                "$(json_escape "$status")" \
                "$(json_escape "$file")")")
        fi
    }
    _each_adr "$dir" _collect

    if [[ ${#entries[@]} -eq 0 ]]; then
        echo "[]"
    else
        local joined
        joined=$(IFS=,; echo "${entries[*]}")
        echo "[$joined]"
    fi
}

cmd_find() {
    local dir="$1"
    local target="$2"
    [[ -d "$dir" ]] || { echo "ERROR: ADR directory not found: $dir" >&2; return 1; }

    # Normalize: accept "1", "01", "001", "0001"
    if [[ ! "$target" =~ ^[0-9]+$ ]]; then
        echo "ERROR: ADR number must be numeric, got: $target" >&2
        return 1
    fi
    local padded
    padded=$(printf '%04d' $((10#$target)))

    local match
    match=$(find "$dir" -maxdepth 1 -type f -name "${padded}-*.md" 2>/dev/null | head -n1)
    if [[ -z "$match" ]]; then
        echo "ERROR: No ADR found with number $padded in $dir" >&2
        return 1
    fi
    echo "$match"
}

# Parse arguments
case "${1:-}" in
    --help|-h|"")
        usage
        [[ -z "${1:-}" ]] && exit 1 || exit 0
        ;;
    --next-num)
        [[ -n "${2:-}" ]] || { echo "ERROR: --next-num requires <ADR_DIR>" >&2; exit 1; }
        cmd_next_num "$2"
        ;;
    --list)
        [[ -n "${2:-}" ]] || { echo "ERROR: --list requires <ADR_DIR>" >&2; exit 1; }
        cmd_list "$2"
        ;;
    --find)
        [[ -n "${2:-}" && -n "${3:-}" ]] || { echo "ERROR: --find requires <ADR_DIR> <NNNN>" >&2; exit 1; }
        cmd_find "$2" "$3"
        ;;
    *)
        echo "ERROR: Unknown command: $1" >&2
        usage
        exit 1
        ;;
esac
