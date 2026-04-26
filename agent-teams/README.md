# Agent Debate Stage

A live, four-panel visualization of a multi-agent debate, built on Claude
Code's **Agent Teams** (experimental). Designed for showing students how
agents collaborate, communicate peer-to-peer, and form teams dynamically.

## What you'll see

A `judge` teammate moderates a 5-round debate between `debater-pro` and
`debater-con` on a design question. Debaters spawn `researcher` teammates
to verify factual claims; the judge spawns `expert-witness` teammates to
settle technical disputes. Teammates communicate via Claude Code's native
`SendMessage` tool (asynchronous, mailbox-style); the same messages are
mirrored to JSONL log files for the audience-facing visualization. Four
browser panels render the activity in real time:

```
┌────────────────────────────┬────────────────────────────┐
│ A · Debate Chat            │ B · Live Roster &          │
│   Slack-style bubbles per  │   Spawn Graph              │
│   message, color-coded     │   Mermaid graph that       │
│   per agent                │   grows as agents appear   │
├────────────────────────────┼────────────────────────────┤
│ C · Decision Log           │ D · Witness Stand          │
│   "Why each agent acted,"  │   New agent files appear   │
│   chronological            │   as they're spawned       │
└────────────────────────────┴────────────────────────────┘
```

## Run the demo

You need Node.js (any recent version) and Claude Code.

> **Required:** Agent Teams is an experimental feature. The flag
> `CLAUDE_CODE_EXPERIMENTAL_AGENT_TEAMS=1` is already set at project level
> in `.claude/settings.json`. **Start a fresh Claude Code session** in
> this directory so it picks up the flag and loads the agent definitions.

**Terminal 1 — start the visualization server:**

```bash
node viz/server.js
```

Open `http://localhost:5173/` in a browser. You should see four empty panels.

**Terminal 2 — start a fresh Claude Code session in this directory** and
type the slash command:

```
/debate
```

That uses the default question (REST vs GraphQL). To run a different
debate, pass it as the argument:

```
/debate Should we adopt a strict no-mocks policy for integration tests?
```

The four panels populate live as the debate progresses. Total runtime:
~5–7 minutes. Other pre-staged questions are in
`prompts/debate-questions.md`.

The command itself lives at `.claude/commands/debate.md` — open it if you
want to see or tune what the lead actually does.

## Verify the harness without running real agents

To dry-run the visualization with canned data (useful before the lecture):

```bash
node viz/server.js                # terminal 1
node viz/seed.js 1500             # terminal 2 — 1500ms between events
```

This injects a scripted REST-vs-GraphQL debate at human-readable pace.
Useful as a backup if a live run wedges.

## How it works

### Two channels: agent-facing vs audience-facing

Agent Teams provides peer-to-peer messaging via the `SendMessage` tool:
asynchronous, mailbox-style. Messages auto-deliver as new conversation
turns; teammates don't poll an inbox. That's the **agent-facing channel** —
how teammates actually coordinate.

The mailbox is opaque to the visualization. So every agent is also instructed
to **mirror every SendMessage to a JSONL log** via two helper scripts — the
**audience-facing channel**:

- `bin/log-decision <agent> <decision> <reason>` → `logs/decisions.jsonl`
- `bin/log-chat <from> <to> <kind> <body>` → `logs/team-chat.jsonl`

The viz server (`viz/server.js`, ~120 LOC, no deps) tails both files +
watches `.claude/agents/`, and pushes events to the browser via SSE. The
result: every action a teammate takes shows up on screen within ~half a
second of it happening.

### The cast

Defined as `.claude/agents/*.md` — these are `subagent_type` templates that
the `Agent` tool instantiates. Each instance gets a `name` (its addressable
identifier on the team) and joins the team via `team_name="debate"`.

| Subagent type     | Role                                    | Spawned by                       | Tools                   |
|-------------------|-----------------------------------------|----------------------------------|-------------------------|
| `judge`           | Moderates the 5 rounds                  | `team-lead`                      | Bash, Read, SendMessage |
| `debater-pro`     | Affirmative                             | `team-lead`                      | Bash, Read, SendMessage |
| `debater-con`     | Negative                                | `team-lead`                      | Bash, Read, SendMessage |
| `expert-witness`  | Settles a technical dispute (Round 3)   | `team-lead` (on judge's request) | Bash, Read, SendMessage |
| `researcher`      | Verifies factual claims (unused)        | — (reserved for future)          | Bash, Read, SendMessage |

The runtime team starts with three actors — `judge`, `debater-pro`,
`debater-con` — all spawned by `team-lead` (the orchestrator session
running `/debate`) before the judge's first turn. A fourth, the
`expert-witness`, is spawned mid-debate by `team-lead` *on the
judge's request* via the **delegated spawn** pattern (see below).
`researcher` is defined but unused in the current flow — kept in
`.claude/agents/` for future use.

### Spawning model — the delegated-spawn pattern

The platform constraint: **subagents inside Claude Code's Agent
Teams runtime do not get the `Agent` tool registered**, regardless
of frontmatter declaration or model. This is design-level filtering
tracked upstream as
[anthropics/claude-code#50306 — Nested Agent Capability for Subagents](https://github.com/anthropics/claude-code/issues/50306)
(open feature request, no maintainer response yet). A related issue,
[#52251 — Agent-Teams Sub-agents with model: opus](https://github.com/anthropics/claude-code/issues/52251),
reports overlapping symptoms for other deferred tools under Opus.
Concrete way to verify: `ToolSearch("select:Task")` returns "No
matching deferred tools found" inside a subagent but succeeds in the
parent session.

So in practice, only `team-lead` (the top-level session) can call
`Agent({...})`. Everyone else has to delegate. The pattern this demo
deliberately teaches:

```
Round 3 timeline:

  judge ────── witness-summons ──────► team-lead
                                            │
                                            │ Agent({subagent_type:"expert-witness", ...})
                                            ▼
                                       expert-witness
                                            ▲
                                            │ peer-to-peer SendMessage (the dispute)
                                            │
  judge ──────────────────────────────────────┘
                                            │
                                            │ witness-testimony
                                            ▼
                                          judge
```

1. The judge identifies a technical dispute it wants settled.
2. The judge SendMessages `team-lead` with a `witness-summons` body
   containing the seed prompt it would have given the witness.
3. Team-lead invokes `Agent` to spawn `expert-witness` with that
   exact seed prompt, then SendMessages the judge: "witness is on
   the team."
4. The judge SendMessages `expert-witness` directly — peer-to-peer,
   no intermediary — with the actual dispute question.
5. Witness replies with calibrated testimony.

This is **not** a hack. It mirrors how real engineering teams work:
a developer asks a manager (or recruiter) to bring on a specialist,
then collaborates with the specialist directly once they're on the
team. Worth teaching as a coordination pattern in its own right —
and the only viable shape for "agent calls agent" workflows under
current Claude Code semantics.

**Do not "fix" this back to having the judge invoke `Agent`
itself.** Multiple prior attempts have confirmed the failure mode is
silent: the `SendMessage` lands in an inbox file with no live process
to read it, the judge idles, and the demo wedges. If/when #50306
lands and subagents can opt into `Agent`, the design can be
simplified — but until then, delegated spawn is the answer.

### House rules

`prompts/house-rules.md` is the canonical version of the logging contract.
Each agent's system prompt embeds an inline summary of the same rules.
If you change the contract, update both.

## Files

```
agent-teams/
├── .claude/
│   ├── settings.json         # CLAUDE_CODE_EXPERIMENTAL_AGENT_TEAMS=1
│   ├── commands/
│   │   └── debate.md         # /debate slash command — runs the demo
│   └── agents/               # 5 sub-agent definitions
│       ├── judge.md
│       ├── debater-pro.md
│       ├── debater-con.md
│       ├── researcher.md
│       └── expert-witness.md
├── bin/
│   ├── log-chat              # writes one line to logs/team-chat.jsonl
│   └── log-decision          # writes one line to logs/decisions.jsonl
├── viz/
│   ├── server.js             # SSE server that tails logs/ and watches .claude/agents/
│   ├── index.html            # 4-panel layout, Mermaid, SSE client
│   └── seed.js               # canned data for dry-runs / fallback
├── prompts/
│   ├── house-rules.md        # canonical logging contract
│   └── debate-questions.md   # pre-staged questions
└── logs/                     # gitignored; created at runtime
    ├── team-chat.jsonl
    └── decisions.jsonl
```

## Recovery / fallback

- **`Agent` or `TeamCreate` not found / "agent type not found"**: you're in
  an old Claude Code session that pre-dates the flag or the agent files.
  Exit and start a fresh session in this directory.
- **Teammate goes silent / appears idle**: idle is normal between turns,
  not an error. If you expected a reply, send the teammate a follow-up
  `SendMessage` to wake them.
- **Demo wedges mid-run**: stop the agents (TeamDelete, kill the session),
  clear logs (`: > logs/team-chat.jsonl; : > logs/decisions.jsonl`), then
  run `node viz/seed.js 1500` to play back a canned debate at speaking
  pace.
- **A panel stops updating**: refresh the browser. SSE replays the full
  history from the log files.
- **A teammate forgets to log a decision or chat**: the demo still works,
  but that teammate's activity won't appear in panels A or C. The
  authoritative state lives in Claude Code's mailbox; the JSONL is just
  a mirror.

## Tuning for the lecture

- **Font legibility**: the panels render dark-mode by default. Test from
  the back row of the actual venue.
- **Pacing**: the canned `seed.js` runs at ~1.5s per event by default —
  fast enough to feel live, slow enough to read. Tune the second arg.
- **Question choice**: pick one with a real, evidence-based disagreement
  (see `prompts/debate-questions.md`). Vague philosophical questions
  produce vague debates.
