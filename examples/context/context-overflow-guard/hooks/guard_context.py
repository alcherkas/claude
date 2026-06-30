#!/usr/bin/env python3
"""
Claude Code PreToolUse safeguard against context-window overflow.

Claude Code runs this command *before* a Read or Bash tool call, passing the
tool call as JSON on stdin. The hook estimates how much text the call would
pour into the conversation and, when that exceeds a token budget, returns a
`deny` decision that steers the agent toward a cheaper move instead:

  * a *selective* read (offset/limit on Read, or grep for the symbol), or
  * *subagent offloading* (run the heavy read inside a Task/Explore agent so
    the bulk text stays in that agent's context and only its summary returns).

Wiring lives in ../.claude/settings.json.

Decision protocol (verified against Claude Code hooks docs):
  * To BLOCK: print JSON on stdout and exit 0:
        {"hookSpecificOutput": {"hookEventName": "PreToolUse",
                                "permissionDecision": "deny",
                                "permissionDecisionReason": "..."}}
  * To ALLOW: print nothing and exit 0 (normal permission flow continues).

DESIGN RULE — FAIL OPEN. Any error, unparseable input, or missing file results
in "allow". A monitoring safeguard must never brick a session.
"""

import json
import os
import shlex
import sys

# Rough chars/token for English + code. Anthropic's tokenizer averages
# ~3.5-4 chars/token; 4 deliberately *under*-counts so the guard is not
# trigger-happy. Tune via CC_CONTEXT_GUARD_MAX_TOKENS.
CHARS_PER_TOKEN = 4
DEFAULT_MAX_TOKENS = 25_000          # ~100 KB of text in one pull is a smell
LINE_LIMIT_OK = 2_000                # an explicit Read limit <= this is "selective"

# Commands that slurp an entire file verbatim into the transcript.
DUMP_COMMANDS = {"cat", "bat", "nl", "less", "more"}
# Commands whose output is bounded/filtered — a pipeline ending here is fine.
LIMITER_COMMANDS = {"head", "tail", "grep", "rg", "ag", "wc", "cut", "sort", "uniq"}


def max_tokens() -> int:
    try:
        return max(1, int(os.environ.get("CC_CONTEXT_GUARD_MAX_TOKENS", "")))
    except (ValueError, TypeError):
        return DEFAULT_MAX_TOKENS


def est_tokens(num_bytes: int) -> int:
    return num_bytes // CHARS_PER_TOKEN


def deny(reason: str) -> None:
    print(json.dumps({
        "hookSpecificOutput": {
            "hookEventName": "PreToolUse",
            "permissionDecision": "deny",
            "permissionDecisionReason": reason,
        }
    }))
    sys.exit(0)


def allow() -> None:
    # No stdout -> Claude Code proceeds with the normal permission flow.
    sys.exit(0)


def advice(target: str, tokens: int) -> str:
    return (
        f"Reading {target} would pull ~{tokens:,} tokens into the conversation "
        f"(budget is {max_tokens():,}). Don't load the whole file. Instead:\n"
        f"  - Read a slice: pass offset/limit to the Read tool, or\n"
        f"  - grep -n for the symbol/string you actually need, or\n"
        f"  - delegate the full read to a subagent (Task/Explore) so the bulk "
        f"text stays out of THIS conversation and only the answer returns."
    )


def check_read(tool_input: dict) -> None:
    path = tool_input.get("file_path")
    if not path or not os.path.isfile(path):
        allow()

    # An explicit, bounded line limit means the agent is already reading
    # selectively — let it through regardless of total file size.
    limit = tool_input.get("limit")
    if isinstance(limit, int) and 0 < limit <= LINE_LIMIT_OK:
        allow()

    tokens = est_tokens(os.path.getsize(path))
    if tokens > max_tokens():
        deny(advice(path, tokens))
    allow()


def _segments(command: str):
    """Split a shell command into pipeline segments of [token, ...]."""
    tokens = shlex.split(command)
    seg, out = [], []
    for tok in tokens:
        if tok == "|":
            if seg:
                out.append(seg)
            seg = []
        else:
            seg.append(tok)
    if seg:
        out.append(seg)
    return out


def check_bash(tool_input: dict) -> None:
    command = tool_input.get("command", "")
    if not command:
        allow()

    try:
        segments = _segments(command)
    except ValueError:
        allow()  # unparseable (e.g. unbalanced quotes) -> fail open
    if not segments:
        allow()

    # If the pipeline ENDS in a limiter (head/grep/wc/...), the text reaching
    # the transcript is already bounded — allow no matter how big the source is.
    last_cmd = os.path.basename(segments[-1][0]) if segments[-1] else ""
    if last_cmd in LIMITER_COMMANDS:
        allow()

    # Otherwise look for a whole-file dump (cat/bat/nl/...) of an oversized file.
    worst = None
    for seg in segments:
        if not seg:
            continue
        cmd = os.path.basename(seg[0])
        if cmd not in DUMP_COMMANDS:
            continue
        for arg in seg[1:]:
            if arg.startswith("-"):
                continue
            if os.path.isfile(arg):
                tokens = est_tokens(os.path.getsize(arg))
                if tokens > max_tokens() and (worst is None or tokens > worst[1]):
                    worst = (arg, tokens)
    if worst:
        deny(advice(worst[0], worst[1]))
    allow()


def main() -> None:
    try:
        event = json.load(sys.stdin)
    except (json.JSONDecodeError, ValueError):
        allow()  # not JSON -> fail open

    tool_name = event.get("tool_name", "")
    tool_input = event.get("tool_input", {}) or {}

    if tool_name == "Read":
        check_read(tool_input)
    elif tool_name == "Bash":
        check_bash(tool_input)
    allow()


if __name__ == "__main__":
    try:
        main()
    except Exception:
        # Absolutely never let a bug in the guard block a tool call.
        allow()
