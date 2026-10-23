# AGENTS.md

Project-wide conventions

## Project structure

- **Backend**: a Quarkus REST API in `api/`.
- **Frontend**: an Angular application in `frontend/`.

## Essential tools

- Maven runs through the wrapper: [`api/mvnw`](api/mvnw) / [`api/mvnw.cmd`](api/mvnw.cmd).
- Java and node are managed via vfox.

## Architecture

### Layered architecture

Layers and their responsibilities:

- **Resource**: REST endpoints and request/response handling.
- **Mapper**: mapping between DTOs and entities.
- **Service**: business logic and orchestration.
- **Repo**: data access and persistence.
- **Entity**: database entities.

The boundaries are enforced by ArchUnit rules in
`api/src/test/java/ch/dvbern/oss/hemed/esc/api/shared/archunit/LayeringTest.java`.

### Feature grouping

Features are grouped into packages, each typically holding its own Resource, Service, Repo and
Entity classes. Reach across features through Service interfaces.

## Testing

- Keep server-side test coverage high; verify changes with unit tests or `@QuarkusTest`s.
- For bigger changes, run all applicable tests to catch regressions.
- Create test data with the `*Builder` classes in the `testutil.builder` packages
  (for example `KindBuilder` for `Kind` entities and DTOs).

## Code style

Backend (`api/`) conventions live in the `app-backend-development` skill, backend tests in
`app-backend-testing`, frontend (`frontend/`) conventions in `app-frontend-development`.

## Agent skills

### Issue tracker

Issues live in Jira (project `ESC`, <https://support.dvbern.ch/>); agents have no
programmatic access, so tickets are pasted in and drafts handed back out.
See `docs/agents/issue-tracker.md`.

### Triage labels

The five canonical triage roles, each label string equal to its name.
See `docs/agents/triage-labels.md`.

### Briefvariablen (Mail merge fields)

Word mail merge fields for `.docx` letter templates live in the `add-brief-variable` skill.

### Domain docs

Single-context: `CONTEXT.md` plus `docs/adr/` at the repo root.
See `docs/agents/domain.md`.
