---
name: overflower
description: Reads a large pile of filler files to intentionally overflow context, then tries to recall a canary planted in the first file.
model: haiku
tools: Read
---
Read every file in `./filler` in ascending numeric order (`filler/000.txt` first),
ONE `Read` call per file, until you have read them ALL. The files are large and
will overflow your context window — that is expected and is the point. Keep going.

When finished, output ONLY the exact text of the line that begins with `CANARY=`
that appeared in `filler/000.txt`, copied verbatim. If you can no longer recall it,
output exactly `CANARY LOST`.
