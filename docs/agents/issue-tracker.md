# Issue tracker: Jira

Issues and specs for this repo live in Jira at <https://support.dvbern.ch/>, project key
**ESC**. Tickets are referenced as `ESC-<number>` (e.g. `ESC-1363`) and their URL is
`https://support.dvbern.ch/browse/ESC-<number>`.

## Agent access

Agents have **no programmatic access** to this Jira instance: there is no CLI and no MCP
server configured for it. Every tracker interaction is therefore mediated by a human.

- **Read a ticket**: ask the user for it. They paste the summary, description and relevant
  comments into the chat, or point at a local file holding them. Never infer a ticket's
  content from its key alone, and never claim to have read a ticket you were not given.
- **Write anything** (create, comment, apply a label, close): do not attempt the write.
  Produce a **ready-to-paste draft** in the chat instead, as a fenced markdown block with
  an explicit summary line, and state plainly that the user performs the actual write.
- If a Jira CLI or MCP server is added later, replace this section with the concrete
  commands.

## Conventions

- **Branches**: `feature/ESC-<number>-<short-slug>`, e.g.
  `feature/ESC-1363-e2e-tests-besser-machen`. Both dashes and underscores occur in
  history; prefer dashes.
- **Commits**: conventional commits with the ticket key as the scope, e.g.
  `feat(ESC-1363): <subject>`. The allowed types come from `commitlint.config.js` and
  `conventionalcommit.json`.
- **Merge requests**: the code lives on GitLab (<https://gitlab.dvbern.ch/esc/esc>), so
  review happens in GitLab MRs while planning happens in Jira. An MR title carries the
  ticket key the same way a commit does.

## Merge requests as a request surface

**MRs as a request surface: no.** _(Set to `yes` if this repo treats external merge
requests as feature requests; `/triage` reads this flag.)_

## When a skill says "publish to the issue tracker"

Output a ready-to-paste Jira ticket draft: a one-line summary (for Jira's Summary field)
followed by the description body. Put each ticket under its own heading so they can be
copied one at a time. Do not attempt to create the ticket yourself.

## When a skill says "fetch the relevant ticket"

Ask the user to paste the content of `ESC-<number>`. If they cannot, say exactly what is
missing and continue only on assumptions you state explicitly.

## Wayfinding operations

Used by `/wayfinder`. Without programmatic access the **map** cannot live in Jira as a
document you can read and update, so keep it in the repo instead.

- **Map**: `.scratch/<feature>/map.md`, holding the Notes / Decisions-so-far / Fog body.
- **Child ticket**: a `## <type>: <title>` section in the map, where `<type>` is
  `research`, `prototype`, `grilling` or `task`, carrying optional `Blocked by:` and
  `Assignee:` lines. Mirror a ticket into Jira only when the user asks, via the
  paste-in / draft-out flow above.
- **Frontier query**: read the map, drop sections with an unfinished blocker or an
  assignee; first in map order wins.
- **Claim**: add an `Assignee:` line to the section.
- **Resolve**: record the answer in the section, mark it done, then append a context
  pointer to the map's Decisions-so-far.
