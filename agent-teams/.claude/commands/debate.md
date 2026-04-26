---
description: Run a structured 5-round multi-agent debate on a design question. Defaults to REST vs GraphQL if no argument is given.
---

Run a structured multi-agent debate. The question is:

$ARGUMENTS

(If the line above is empty, use this default question: **"Should we use REST or GraphQL for our new public-facing API?"**)

In one turn, do the following:

1. Call `TeamCreate` with:
   - `team_name`: `"debate"`
   - `agent_type`: `"lead"`
   - `description`: `"Structured 5-round debate"`

2. **Spawn the two debaters first, in parallel** (two `Agent` tool calls in a single message). Team-lead must spawn them; the in-process subagent backend does not expose the `Agent` tool to the judge at runtime, so the judge cannot spawn them itself.

   - `name: "debater-pro"`, `subagent_type: "debater-pro"`, `team_name: "debate"`, `prompt`: brief on the question; tell it to argue **affirmative**; wait for the judge's `SendMessage` before speaking; follow its house rules (log decisions, mirror chats).
   - `name: "debater-con"`, `subagent_type: "debater-con"`, `team_name: "debate"`, `prompt`: same shape, but argue **negative**.

3. **After both debater spawns return `Spawned successfully`**, spawn the judge with one more `Agent` call. Spawning order matters: when the judge's first turn fires, both debaters must already be members of the team or the judge's opening `SendMessage` will land in an inbox before the recipient is fully registered.

   - `name: "judge"`, `subagent_type: "judge"`, `team_name: "debate"`, `prompt`: brief on the question; tell the judge that `debater-pro` and `debater-con` are already on the team; instruct it to drive 5 rounds via `SendMessage` (openings → cross-exam → witness consultation → closings → verdict), log every decision, mirror every chat with `bin/log-chat`, and SendMessage the final verdict to `team-lead`. Remind it that for Round 3 it should send a `witness-summons` to you (see step 4) instead of trying to invoke `Agent` itself. Remind it to follow its house rules.

4. **During the debate, watch for a `witness-summons` from the judge.** Subagents can't invoke `Agent` themselves under the in-process backend ([Claude Code issue #50306](https://github.com/anthropics/claude-code/issues/50306)), so they delegate spawning to you. The judge's request will look like:

   > `"witness-summons: Need expert-witness on <topic>. Seed prompt: <one-paragraph dispute summary>. Please spawn expert-witness and reply when it's on the team."`

   When you see one:
   - Call `Agent({subagent_type: "expert-witness", name: "expert-witness", team_name: "debate", prompt: <judge's seed prompt verbatim>})`. Wait for `Spawned successfully`.
   - `bin/log-chat "team-lead" "expert-witness" "spawn-request" "<seed prompt summary>"`.
   - SendMessage → `judge`: `"expert-witness is on the team. You can SendMessage them directly with the dispute."` Then `bin/log-chat "team-lead" "judge" "note" "<same body>"`.

   The judge handles the rest of the witness exchange (peer-to-peer SendMessage with `expert-witness`). This is the **delegated spawn** pattern — see the README's "Spawning model" section for the full rationale.

After the verdict arrives in your inbox:

5. For each teammate currently on the team (`judge`, `debater-pro`, `debater-con`, and `expert-witness` if it was spawned), send `SendMessage` with `message: {type: "shutdown_request", request_id: "<unique>", reason: "debate complete"}`.
6. Call `TeamDelete`.

The audience watches the four-panel visualization at http://localhost:5173/ throughout — make sure `node viz/server.js` is running in another terminal first.
