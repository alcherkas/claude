#!/usr/bin/env python3
"""
Proof that the context-overflow guard works.

Each test invokes the hook EXACTLY the way Claude Code does — as a subprocess
with the tool call as JSON on stdin — and asserts the deny/allow decision. That
faithfulness is the point: if these pass, the real hook behaves the same in a
live session.

Run:  python3 -m unittest discover -s test -v
  or: ./run_demo.sh
"""

import json
import os
import subprocess
import sys
import tempfile
import unittest

HERE = os.path.dirname(os.path.abspath(__file__))
HOOK = os.path.join(HERE, "..", "hooks", "guard_context.py")


def run_hook(payload: dict, env_extra: dict | None = None):
    """Invoke the hook with `payload` on stdin; return (decision_or_None)."""
    env = os.environ.copy()
    if env_extra:
        env.update(env_extra)
    proc = subprocess.run(
        [sys.executable, HOOK],
        input=json.dumps(payload),
        capture_output=True,
        text=True,
        env=env,
    )
    assert proc.returncode == 0, f"hook must always exit 0, got {proc.returncode}: {proc.stderr}"
    out = proc.stdout.strip()
    if not out:
        return None  # no output == allow
    return json.loads(out)["hookSpecificOutput"]["permissionDecision"]


class ContextGuardTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.tmp = tempfile.TemporaryDirectory()
        d = cls.tmp.name
        # ~200 KB -> ~50k tokens, over the 25k default budget.
        cls.big = os.path.join(d, "big.txt")
        with open(cls.big, "w") as f:
            f.write("x" * 200_000)
        # ~1 KB -> ~250 tokens, well under budget.
        cls.small = os.path.join(d, "small.txt")
        with open(cls.small, "w") as f:
            f.write("y" * 1_000)

    @classmethod
    def tearDownClass(cls):
        cls.tmp.cleanup()

    # --- Read tool -------------------------------------------------------
    def test_big_unbounded_read_is_denied(self):
        d = run_hook({"tool_name": "Read", "tool_input": {"file_path": self.big}})
        self.assertEqual(d, "deny")

    def test_small_read_is_allowed(self):
        d = run_hook({"tool_name": "Read", "tool_input": {"file_path": self.small}})
        self.assertIsNone(d)

    def test_bounded_read_of_big_file_is_allowed(self):
        # Explicit small limit == selective read == fine even on a huge file.
        d = run_hook({"tool_name": "Read",
                      "tool_input": {"file_path": self.big, "offset": 0, "limit": 100}})
        self.assertIsNone(d)

    def test_missing_file_is_allowed(self):
        d = run_hook({"tool_name": "Read", "tool_input": {"file_path": "/no/such/file"}})
        self.assertIsNone(d)

    # --- Bash tool -------------------------------------------------------
    def test_bash_cat_big_is_denied(self):
        d = run_hook({"tool_name": "Bash", "tool_input": {"command": f"cat {self.big}"}})
        self.assertEqual(d, "deny")

    def test_bash_cat_small_is_allowed(self):
        d = run_hook({"tool_name": "Bash", "tool_input": {"command": f"cat {self.small}"}})
        self.assertIsNone(d)

    def test_bash_cat_big_piped_to_head_is_allowed(self):
        # Output is bounded by head -> safe, even though cat reads the whole file.
        d = run_hook({"tool_name": "Bash",
                      "tool_input": {"command": f"cat {self.big} | head -n 20"}})
        self.assertIsNone(d)

    def test_bash_grep_big_is_allowed(self):
        d = run_hook({"tool_name": "Bash",
                      "tool_input": {"command": f"grep -n needle {self.big}"}})
        self.assertIsNone(d)

    # --- Config + robustness --------------------------------------------
    def test_threshold_is_configurable(self):
        # Lower the budget below the small file's size -> now it's denied.
        d = run_hook({"tool_name": "Read", "tool_input": {"file_path": self.small}},
                     env_extra={"CC_CONTEXT_GUARD_MAX_TOKENS": "100"})
        self.assertEqual(d, "deny")

    def test_fails_open_on_garbage_stdin(self):
        proc = subprocess.run(
            [sys.executable, HOOK], input="this is not json",
            capture_output=True, text=True,
        )
        self.assertEqual(proc.returncode, 0)
        self.assertEqual(proc.stdout.strip(), "")  # no deny

    def test_other_tools_are_ignored(self):
        d = run_hook({"tool_name": "Write",
                      "tool_input": {"file_path": self.big, "content": "..."}})
        self.assertIsNone(d)


if __name__ == "__main__":
    unittest.main(verbosity=2)
