// Tiny SSE server for the agent debate stage.
// Pure Node.js stdlib — no external deps.
//
// Tails:
//   ../logs/team-chat.jsonl     — every inter-agent message
//   ../logs/decisions.jsonl     — every agent's pre-action reasoning
//   ../.claude/agents/*.md      — the agent roster (file watcher)
//
// Serves index.html + a single SSE stream at /events.

const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = Number(process.env.PORT || 5173);
const ROOT = path.resolve(__dirname, '..');
const LOGS_DIR = path.join(ROOT, 'logs');
const CHAT_FILE = path.join(LOGS_DIR, 'team-chat.jsonl');
const DECISIONS_FILE = path.join(LOGS_DIR, 'decisions.jsonl');
const AGENTS_DIR = path.join(ROOT, '.claude', 'agents');
const POLL_MS = 400;
const INDEX_HTML = path.join(__dirname, 'index.html');

fs.mkdirSync(LOGS_DIR, { recursive: true });
fs.mkdirSync(AGENTS_DIR, { recursive: true });
for (const f of [CHAT_FILE, DECISIONS_FILE]) {
  if (!fs.existsSync(f)) fs.writeFileSync(f, '');
}

const clients = new Set();

function broadcast(event, data) {
  const payload = `event: ${event}\ndata: ${JSON.stringify(data)}\n\n`;
  for (const res of clients) {
    try { res.write(payload); } catch { /* ignore */ }
  }
}

// Tail a JSONL file. New lines → broadcast(eventName, parsedJson).
function tailJsonl(file, eventName) {
  let pos = 0;
  let buf = '';
  let reading = false;

  const flush = (chunk) => {
    buf += chunk;
    let idx;
    while ((idx = buf.indexOf('\n')) >= 0) {
      const line = buf.slice(0, idx).trim();
      buf = buf.slice(idx + 1);
      if (!line) continue;
      try { broadcast(eventName, JSON.parse(line)); }
      catch (e) { broadcast(eventName, { _parse_error: e.message, _raw: line }); }
    }
  };

  setInterval(() => {
    if (reading) return;
    fs.stat(file, (err, stat) => {
      if (err) return;
      if (stat.size < pos) { pos = 0; buf = ''; }
      if (stat.size === pos) return;
      reading = true;
      const start = pos;
      const end = stat.size - 1;
      const stream = fs.createReadStream(file, { start, end, encoding: 'utf8' });
      stream.on('data', flush);
      stream.on('end', () => { pos = end + 1; reading = false; });
      stream.on('error', () => { reading = false; });
    });
  }, POLL_MS);
}

// Watch agents dir for new/removed .md files.
function watchAgents() {
  let known = new Set();
  setInterval(() => {
    fs.readdir(AGENTS_DIR, (err, files) => {
      if (err) return;
      const current = new Set(files.filter(f => f.endsWith('.md')));
      for (const f of current) {
        if (!known.has(f)) {
          broadcast('agent-file', { name: f.replace(/\.md$/, ''), file: f });
        }
      }
      for (const f of known) {
        if (!current.has(f)) {
          broadcast('agent-file-removed', { name: f.replace(/\.md$/, ''), file: f });
        }
      }
      known = current;
    });
  }, POLL_MS);
}

function readJsonlLines(file) {
  try {
    return fs.readFileSync(file, 'utf8')
      .split('\n')
      .map(l => l.trim())
      .filter(Boolean)
      .map(l => { try { return JSON.parse(l); } catch { return null; } })
      .filter(Boolean);
  } catch { return []; }
}

const server = http.createServer((req, res) => {
  if (req.url === '/' || req.url === '/index.html') {
    fs.readFile(INDEX_HTML, (err, data) => {
      if (err) { res.writeHead(500); return res.end('index.html missing'); }
      res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
      res.end(data);
    });
    return;
  }
  if (req.url === '/events') {
    res.writeHead(200, {
      'Content-Type': 'text/event-stream',
      'Cache-Control': 'no-cache',
      'Connection': 'keep-alive',
      'X-Accel-Buffering': 'no',
    });
    res.write(': connected\n\n');
    clients.add(res);

    // Replay history so a late-joining browser sees the full state.
    for (const m of readJsonlLines(CHAT_FILE)) {
      res.write(`event: chat\ndata: ${JSON.stringify(m)}\n\n`);
    }
    for (const d of readJsonlLines(DECISIONS_FILE)) {
      res.write(`event: decision\ndata: ${JSON.stringify(d)}\n\n`);
    }
    try {
      const files = fs.readdirSync(AGENTS_DIR).filter(f => f.endsWith('.md'));
      for (const f of files) {
        const payload = { name: f.replace(/\.md$/, ''), file: f };
        res.write(`event: agent-file\ndata: ${JSON.stringify(payload)}\n\n`);
      }
    } catch { /* ignore */ }

    req.on('close', () => clients.delete(res));
    return;
  }
  res.writeHead(404);
  res.end('not found');
});

tailJsonl(CHAT_FILE, 'chat');
tailJsonl(DECISIONS_FILE, 'decision');
watchAgents();

server.listen(PORT, () => {
  console.log(`stage: http://localhost:${PORT}`);
  console.log(`tailing: ${CHAT_FILE}`);
  console.log(`tailing: ${DECISIONS_FILE}`);
  console.log(`watching: ${AGENTS_DIR}`);
});
