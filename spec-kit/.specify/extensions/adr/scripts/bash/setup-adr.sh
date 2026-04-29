#!/usr/bin/env bash

set -e

# Setup script for /speckit.adr (after-specify position).
#
# Resolves feature paths, validates that spec.md exists (NOT plan.md — this
# step intentionally runs after /speckit.specify and before any planning),
# ensures specs/<feature>/adr/ exists, computes the next ADR number, and
# resolves the ADR template path. Output is consumed by the speckit-adr skill.
#
# Usage:
#   setup-adr.sh [--json]
#   setup-adr.sh --help

JSON_MODE=false
ARGS=()

for arg in "$@"; do
    case "$arg" in
        --json)
            JSON_MODE=true
            ;;
        --help|-h)
            echo "Usage: $0 [--json]"
            echo "  --json    Output results in JSON format"
            echo "  --help    Show this help message"
            exit 0
            ;;
        *)
            ARGS+=("$arg")
            ;;
    esac
done

SCRIPT_DIR="$(CDPATH="" cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Walk up to find the .specify project root, then source core common.sh.
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

_paths_output=$(get_feature_paths) || { echo "ERROR: Failed to resolve feature paths" >&2; exit 1; }
eval "$_paths_output"
unset _paths_output

# If feature.json pins an existing feature directory, branch naming is not required.
if ! feature_json_matches_feature_dir "$REPO_ROOT" "$FEATURE_DIR"; then
    check_feature_branch "$CURRENT_BRANCH" "$HAS_GIT" || exit 1
fi

# ADR generation requires spec.md. plan.md / research.md are intentionally ignored.
if [[ ! -f "$FEATURE_SPEC" ]]; then
    echo "ERROR: spec.md not found in $FEATURE_DIR" >&2
    echo "Run /speckit.specify first to create the feature specification before generating ADRs." >&2
    exit 1
fi

ADR_DIR="$FEATURE_DIR/adr"
mkdir -p "$ADR_DIR"

ADR_TEMPLATE=$(resolve_template "adr-template" "$REPO_ROOT") || true
if [[ -z "$ADR_TEMPLATE" ]] || [[ ! -f "$ADR_TEMPLATE" ]]; then
    echo "Warning: ADR template not found (expected adr-template.md under .specify/extensions/adr/templates/ or .specify/templates/)" >&2
    ADR_TEMPLATE=""
fi

NEXT_ADR_NUM=$("$SCRIPT_DIR/list-adrs.sh" --next-num "$ADR_DIR")

if $JSON_MODE; then
    if has_jq; then
        jq -cn \
            --arg feature_dir "$FEATURE_DIR" \
            --arg feature_spec "$FEATURE_SPEC" \
            --arg adr_dir "$ADR_DIR" \
            --arg adr_template "$ADR_TEMPLATE" \
            --arg next_adr_num "$NEXT_ADR_NUM" \
            --arg branch "$CURRENT_BRANCH" \
            --arg has_git "$HAS_GIT" \
            '{FEATURE_DIR:$feature_dir,FEATURE_SPEC:$feature_spec,ADR_DIR:$adr_dir,ADR_TEMPLATE:$adr_template,NEXT_ADR_NUM:$next_adr_num,BRANCH:$branch,HAS_GIT:$has_git}'
    else
        printf '{"FEATURE_DIR":"%s","FEATURE_SPEC":"%s","ADR_DIR":"%s","ADR_TEMPLATE":"%s","NEXT_ADR_NUM":"%s","BRANCH":"%s","HAS_GIT":"%s"}\n' \
            "$(json_escape "$FEATURE_DIR")" \
            "$(json_escape "$FEATURE_SPEC")" \
            "$(json_escape "$ADR_DIR")" \
            "$(json_escape "$ADR_TEMPLATE")" \
            "$(json_escape "$NEXT_ADR_NUM")" \
            "$(json_escape "$CURRENT_BRANCH")" \
            "$(json_escape "$HAS_GIT")"
    fi
else
    echo "FEATURE_DIR: $FEATURE_DIR"
    echo "FEATURE_SPEC: $FEATURE_SPEC"
    echo "ADR_DIR: $ADR_DIR"
    echo "ADR_TEMPLATE: $ADR_TEMPLATE"
    echo "NEXT_ADR_NUM: $NEXT_ADR_NUM"
    echo "BRANCH: $CURRENT_BRANCH"
    echo "HAS_GIT: $HAS_GIT"
fi
