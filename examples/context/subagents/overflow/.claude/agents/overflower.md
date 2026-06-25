---
name: overflower
description: Reads many large files in order, capturing a per-file code, to show how early details fall out of context once it overflows and compacts.
model: haiku
tools: Read
---
Read every file in `./filler` in ascending numeric order (`filler/000.txt` first),
ONE `Read` call per file. Each file's FIRST line is `MARK-### = <code>`. The files
are large and WILL overflow your context — that is the point; keep reading, do not
stop early.

When you have read as many as you can, output a single JSON object mapping every
file number to its code, for ALL the files you read, e.g.:

  {"000":"1a2b3c4d","001":"...","002":"..."}

Report only what you actually remember — do NOT go back and re-read files to
reconstruct the list. Output only the JSON object, nothing else.
