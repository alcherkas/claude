#!/usr/bin/env python3
"""
A couple of BAD examples (context-overflow anti-patterns) contrasted with the
GOOD fix — and proof, since each case is run through the real guard and checked
against the decision we expect. Exits non-zero if any case misbehaves, so this
doubles as a regression test for the anti-patterns.

Run:  python3 examples/show_bad_vs_good.py
"""

import json
import os
import subprocess
import sys
import tempfile

HERE = os.path.dirname(os.path.abspath(__file__))
HOOK = os.path.join(HERE, "..", "hooks", "guard_context.py")


def decide(tool_name: str, tool_input: dict):
    """Run the guard the way Claude Code does; return ('deny'|'allow', reason)."""
    proc = subprocess.run(
        [sys.executable, HOOK],
        input=json.dumps({"tool_name": tool_name, "tool_input": tool_input}),
        capture_output=True, text=True,
    )
    out = proc.stdout.strip()
    if not out:
        return "allow", ""
    spec = json.loads(out)["hookSpecificOutput"]
    return spec["permissionDecision"], spec["permissionDecisionReason"]


def main() -> int:
    tmp = tempfile.TemporaryDirectory()
    d = tmp.name
    # A 300 KB generated bundle (~75k tokens) and a 2 MB log (~500k tokens):
    # the kind of files an agent should never load whole.
    bundle = os.path.join(d, "bundle.js")
    open(bundle, "w").write("/*min*/" + ("a();" * 75_000))
    log = os.path.join(d, "app.log")
    open(log, "w").write(("2026-06-22 INFO request handled\n" * 64_000))

    # (title, expectation, [ (verdict_label, tool, input) ... ])
    cases = [
        ("BAD EXAMPLE 1 — read a generated bundle whole to 'see what's in it'",
         "deny",
         "Read", {"file_path": bundle}),
        ("  GOOD: grep for the symbol, read only the hit",
         "allow",
         "Bash", {"command": f"grep -n createStore {bundle}"}),
        ("  GOOD: bounded Read (offset/limit) of just the region",
         "allow",
         "Read", {"file_path": bundle, "offset": 120, "limit": 60}),

        ("BAD EXAMPLE 2 — cat a 2 MB log into the chat to find an error",
         "deny",
         "Bash", {"command": f"cat {log}"}),
        ("  GOOD: grep the signal (output is bounded, source can be huge)",
         "allow",
         "Bash", {"command": f"grep -n ERROR {log}"}),
        ("  GOOD: tail the tail",
         "allow",
         "Bash", {"command": f"tail -n 100 {log}"}),
    ]

    print("=" * 70)
    print(" CONTEXT-OVERFLOW ANTI-PATTERNS  (guard verdict vs. expected)")
    print("=" * 70)
    failures = 0
    for title, expected, tool, tinput in cases:
        verdict, reason = decide(tool, tinput)
        ok = verdict == expected
        failures += not ok
        mark = "✓" if ok else "✗ MISMATCH"
        tag = "❌" if expected == "deny" else "✅"
        print(f"\n{tag} {title}")
        print(f"     call:    {tool}({json.dumps(tinput)})")
        print(f"     guard:   {verdict.upper():5} (expected {expected.upper()})  {mark}")
        if reason:
            print(f"     reason:  {reason.splitlines()[0]}")

    print("\n" + "=" * 70)
    if failures:
        print(f" RESULT: {failures} case(s) misbehaved — guard is NOT protecting these.")
        return 1
    print(" RESULT: every bad example denied, every good example allowed. ✓")
    return 0


if __name__ == "__main__":
    sys.exit(main())
