---
name: frontend-development
description: Conventions for frontend work in `frontend/` — Angular components, TypeScript libs, HTML templates and their tests. Use it whenever the task touches `frontend/` — "add this component", "fix this Angular service", "write a test for this store",  — even when style isn't mentioned.
argument-hint: "<frontend scope or file>"
---

# Frontend development

Use this skill for Angular, TypeScript and HTML work in [`../../../frontend`](../../../frontend).

## Types

* Prefer strict types for variables, parameters, and return values.
* Enforce explicit passing of all properties of an object — including the optional ones —
  with `ensureProps<Foo>()` from
  [`ensure.ts`](../../../frontend/libs/shared/util/util-shared/src/lib/ensure.ts).
  Always pass the type argument; without it the call compiles but ensures nothing.

## Nullability

Helpers live in
[`nullish.ts`](../../../frontend/libs/shared/util/util-shared/src/lib/nullish.ts).

* Prefer `requireValue()` over `!`.
* Prefer `hasValue()` over `!= null && != undefined`.
* Prefer `isNullish()` over `== null || == undefined`.

## Style

* Prefer single quotes over double quotes.
* Keep lines short and wrap as described in the project code style: one chained method call per
  line with the dot on the new line, one argument per line, one builder continuation per line,
  a newline before each `&&`, `||`, `?` and `:`.

## HTML and accessibility

* Templates must conform to A11y WCAG 2.2 AA.

## Tests

* Prefer running tests via IntelliJ MCP over the CLI.
* Use Vitest for tests, assertions and coverage.
* Use Playwright with `@playwright/a11y` for accessibility testing; the Playwright setup lives in
  [`../../../frontend/playwright`](../../../frontend/playwright) and per-app
  `playwright.config.ts` files.
* Prefer `@ts-expect-error` over `@ts-ignore`.
* Separate ARRANGE, ACT and ASSERT with blank lines, and put each assertion on its own line
  after the `expect()` clause:

  ```ts
  expect(actual)
    .toBe(expected);
  ```

* Mock sparingly, and never mock DTOs.

### Legacy Jest

Jest is still wired up in the workspace (`../../../frontend/jest.config.ts`, `frontend/jest.preset.js`,
`@nx/jest`, `jest-preset-angular`) and older suites run on it. Keep those suites working, write new
tests with Vitest, and ask before migrating an existing suite.

If a lib has neither a Vitest nor a Jest setup, or `vitest` / `@playwright/a11y` are missing from
`../../../frontend/package.json`, ask before adding tooling.

## Done

Work is complete when types are strict, the nullability and `ensureProps` helpers are used instead
of their raw equivalents, templates meet WCAG 2.2 AA, and the affected tests and lint pass.
