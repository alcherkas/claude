# Bad examples — context-overflow anti-patterns

Two concrete "wrong way" moves that blow up a Claude Code session's context,
each with the fix. [`show_bad_vs_good.py`](show_bad_vs_good.py) runs every call
below through the real guard and asserts the verdict, so this is proof, not
just advice:

```bash
python3 examples/show_bad_vs_good.py
# -> every bad example denied, every good example allowed. ✓
```

---

## ❌ Bad example 1 — "just read the whole file to understand it"

```text
Read(file_path="dist/bundle.js")          # 300 KB generated bundle, ~75k tokens
```

**Why it's bad.** A generated/minified/vendored file is almost all noise. One
read can consume a third of the window, and you keep paying for it every
subsequent turn. The agent "looked at the file" and learned almost nothing.

**✅ Good.** Locate, then read the hit only:

```text
Bash: grep -n "createStore" dist/bundle.js     # find the line
Read(file_path="dist/bundle.js", offset=120, limit=60)   # read the 60-line window
```

Or hand the whole-file question to a subagent (`Explore`/`Task`) — the bundle
lands in *its* context and you get back a one-paragraph answer.

---

## ❌ Bad example 2 — "cat the log to find the error"

```text
Bash: cat logs/app.log                    # 2 MB, ~500k tokens — instant overflow
```

**Why it's bad.** Logs are the worst offender: huge, mostly irrelevant lines,
and the part you need is a handful of them. `cat` dumps all of it verbatim into
the transcript.

**✅ Good.** Let the shell filter *before* the text reaches the model:

```text
Bash: grep -n "ERROR" logs/app.log        # only matching lines come back
Bash: tail -n 100 logs/app.log            # only the recent tail
```

The source file can be any size — what matters is how much reaches the window.
The guard allows these because the pipeline's output is bounded.

---

## The pattern behind both

> **Don't move bytes into the conversation that you could filter, locate, or
> offload first.** Pull the *answer*, not the *haystack*.

The guard ([`../hooks/guard_context.py`](../hooks/guard_context.py)) denies the
bad calls and, in its deny message, points the agent at exactly these fixes —
selective read, grep, or subagent offloading.

### Two more anti-patterns the guard can't catch (so watch for them yourself)

- **Death by a thousand reads** — re-reading the same file every turn instead
  of trusting what's already in context. Each call is small, so no single one
  trips the guard; cumulatively they bloat history. Fix: read once; use
  `/compact` when history genuinely drifts.
- **Pasting bulk into the prompt** — dropping a whole spec/schema inline every
  turn. Fix: put stable bulk in a cached prefix (`cache_control`) so it's read
  from cache, or reference a file the agent reads selectively on demand.
