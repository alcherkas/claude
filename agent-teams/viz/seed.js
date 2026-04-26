// Seed canned data into logs/ to verify the viz harness works
// without launching real agents. Useful for dry-runs and as a fallback.
//
// Usage: node viz/seed.js [delayMs]
//   delayMs: ms between entries (default 1500)

const fs = require('fs');
const path = require('path');

const ROOT = path.resolve(__dirname, '..');
const CHAT = path.join(ROOT, 'logs', 'team-chat.jsonl');
const DECS = path.join(ROOT, 'logs', 'decisions.jsonl');
const DELAY = Number(process.argv[2] || 1500);

fs.mkdirSync(path.dirname(CHAT), { recursive: true });

function append(file, obj) {
  fs.appendFileSync(file, JSON.stringify({ ts: new Date().toISOString(), ...obj }) + '\n');
}

const events = [
  { kind: 'decision', agent: 'judge',
    decision: 'open the debate', reason: 'Question received: REST vs GraphQL for the new API.' },
  { kind: 'chat', from: 'judge', to: 'debater-pro', kind_: 'opening',
    body: 'You have the affirmative. Argue for REST. 2-3 sentence opening.' },
  { kind: 'chat', from: 'judge', to: 'debater-con', kind_: 'opening',
    body: 'You have the negative. Argue for GraphQL. 2-3 sentence opening.' },

  { kind: 'decision', agent: 'debater-pro',
    decision: 'lead with operational simplicity',
    reason: 'REST wins on cache friendliness and tooling maturity — strongest opening angle.' },
  { kind: 'chat', from: 'debater-pro', to: 'judge', kind_: 'opening',
    body: 'REST is the boring, correct choice. HTTP semantics map cleanly to CRUD, every CDN caches it for free, and every junior dev on the team can debug it with curl.' },

  { kind: 'decision', agent: 'debater-con',
    decision: 'open with client-driven data fetching',
    reason: 'GraphQL\'s real edge is over-fetch elimination — frame it that way.' },
  { kind: 'chat', from: 'debater-con', to: 'judge', kind_: 'opening',
    body: 'REST forces clients to either over-fetch or chain N requests. GraphQL eliminates round-trips by letting the client describe exactly what it needs — that\'s a 10x win on mobile networks.' },

  { kind: 'decision', agent: 'debater-pro',
    decision: 'challenge the 10x claim',
    reason: 'Specific numeric claim — easy to ask for evidence.' },
  { kind: 'chat', from: 'debater-pro', to: 'debater-con', kind_: 'cross-exam',
    body: 'You said "10x win on mobile networks." Source? That smells like a vendor benchmark.' },

  { kind: 'decision', agent: 'debater-con',
    decision: 'spawn a researcher to verify the claim',
    reason: 'I made a number up. Need to back it with a real study or retract.' },
  { kind: 'chat', from: 'debater-con', to: 'researcher', kind_: 'evidence-request',
    body: 'Find a real-world study on GraphQL vs REST mobile network savings. Cite it.' },

  { kind: 'chat', from: 'researcher', to: 'debater-con', kind_: 'evidence-result',
    body: 'Closest I found: a 2018 Netflix engineering post reporting ~30% payload reduction on key endpoints. "10x" is overstated; "meaningful single-digit-x" is defensible.' },

  { kind: 'decision', agent: 'debater-con',
    decision: 'retract the 10x and substitute the Netflix figure',
    reason: 'Better to walk back a number than get caught lying about it under cross-exam.' },
  { kind: 'chat', from: 'debater-con', to: 'debater-pro', kind_: 'cross-exam',
    body: 'Fair — I overstated. Netflix reported ~30% payload reduction, not 10x. Still material on slow mobile, still my point.' },

  { kind: 'decision', agent: 'judge',
    decision: 'call an expert witness on caching cost',
    reason: 'Both debaters keep talking past each other on cache. Need a neutral technical answer.' },
  { kind: 'chat', from: 'judge', to: 'expert-witness', kind_: 'witness-summons',
    body: 'On a typical e-commerce read path, what fraction of requests hit a CDN cache for REST? Can GraphQL achieve similar?' },

  { kind: 'chat', from: 'expert-witness', to: 'judge', kind_: 'witness-testimony',
    body: 'REST GET responses cache trivially via HTTP headers — typical CDN hit rate 70-90% for hot product pages. GraphQL queries are POSTs by default; achieving comparable cache hit rates requires persisted queries plus custom CDN logic. Possible, but not free.' },

  { kind: 'decision', agent: 'judge',
    decision: 'render the verdict',
    reason: 'Both sides made their case; evidence and witness testimony favor a contextual answer.' },
  { kind: 'chat', from: 'judge', to: 'orchestrator', kind_: 'verdict',
    body: 'Verdict: REST for the public read-heavy path (cacheability dominates). GraphQL for the authenticated client-app path (over-fetch elimination dominates). Don\'t pick one for both.' },
];

function tick(i) {
  if (i >= events.length) {
    console.log('seed: done');
    return;
  }
  const e = events[i];
  if (e.kind === 'chat') {
    const { kind, kind_, ...rest } = e;
    append(CHAT, { ...rest, kind: kind_ });
    console.log(`chat: ${rest.from} -> ${rest.to}`);
  } else if (e.kind === 'decision') {
    const { kind, ...rest } = e;
    append(DECS, rest);
    console.log(`decision: ${rest.agent}`);
  }
  setTimeout(() => tick(i + 1), DELAY);
}

console.log(`seed: writing ${events.length} events at ${DELAY}ms intervals`);
console.log(`seed: chat -> ${CHAT}`);
console.log(`seed: decisions -> ${DECS}`);
tick(0);
