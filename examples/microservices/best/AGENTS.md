# AGENTS.md — QuickBite workspace

Tool-agnostic orientation for any coding agent (Claude Code reads `CLAUDE.md`; GitHub
Copilot reads `.github/copilot-instructions.md`; both mirror this file).

**This is the spine of a 24-repo polyrepo.** Read `CLAUDE.md` in this folder for the full
operating model. The essentials:

1. **`PLATFORM_SPEC.md` is canonical.** Ports, packages, routes, Feign URLs, Kafka topics,
   Terraform inputs — all defined there. The spec wins over any repo's code.
2. **Each repo has its own `<repo>/CLAUDE.md`.** When you edit a repo, follow that file.
   It lists the repo's port, sync dependencies, who calls it, and the events it
   produces/consumes.
3. **Write to one repo at a time.** Read across all repos to understand; never edit a
   sibling repo as a side effect.
4. **Contract / event / `/internal` changes are cross-repo.** Update `PLATFORM_SPEC.md` +
   the producer + every consumer, run `tools/check-contracts.sh`, and follow
   `CONTRIBUTING-cross-repo.md`.
5. **Create files only** — do not run `mvn`, `npm install`, or `terraform`.

Bootstrap the workspace with `meta git clone <spine-repo-url>` (see `.meta`) or open
`quickbite.code-workspace` in VS Code.
