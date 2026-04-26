# Pre-staged debate questions

Pick one of these as the input to the `judge` agent. The first is the
recommended primary; the others are fallbacks if the first run drifts.

## Primary

> **Should we use REST or GraphQL for our new public-facing API?**
> Both debaters should consider: caching/CDN behavior, mobile network cost,
> developer ergonomics, and tooling maturity.

Why this works as a demo: there's a real, well-documented disagreement; both
sides have strong empirical arguments; it produces natural moments for the
researcher (verifying numeric claims) and the expert witness (settling cache
behavior).

## Fallback 1

> **For a 5-engineer startup building their first SaaS backend, should they
> ship a monolith or microservices?**
> Consider operational overhead, team size, deployment story, and the cost
> of refactoring later.

Good for visibly forcing the debaters to make context-dependent claims.

## Fallback 2

> **Should we adopt a strict "no-mocks" policy for integration tests, or
> allow mocks where the dependency is expensive to spin up?**
> Consider test reliability, CI cost, and what happens when the real
> dependency drifts from the mock.

Good if the audience is more developer-focused; less hand-wavy than
architecture debates.

---

## How to invoke (from the main Claude Code session)

Start the viz server (`node viz/server.js`), open
`http://localhost:5173/`, and start a **fresh** Claude Code session in
this directory. Then in the chat:

```
/debate
```

Uses the default question (REST vs GraphQL). To run any of the questions
above, pass it as the argument:

```
/debate For a 5-engineer startup building their first SaaS backend, should they ship a monolith or microservices?
```

The slash-command source is at `.claude/commands/debate.md`.
