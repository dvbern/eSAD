---
name: backend-testing
description: Guidance for Java/Quarkus backend test work in `api/` — writing, extending, reviewing and fixing unit tests, @QuarkusTest integration tests, RestAssured endpoint tests, Hibernate ORM/PostgreSQL persistence tests and WireMock tests for external services, with AssertJ, SonarQube and JaCoCo coverage. Use it whenever the task involves backend tests or test failures in `api/`, including "write tests for X", "cover this class", "add a regression test", "why does ImpfungServiceTest fail", raising coverage, or implementing a backend bug fix or feature that needs tests — even if tests aren't mentioned explicitly.
argument-hint: "<test scope or class>"
---

# Backend testing

Use this skill for backend tests and test failures in `../../../api`.

## Before writing tests

1. Read the issue, relevant production code and tests,
   [backend conventions](../app-backend-development/SKILL.md),
   [project-wide conventions](../../../AGENTS.md), and `../../../api/pom.xml`.
2. Ask focused questions when behavior, seams, test data, contracts, or acceptance criteria
   are unclear. If you can't ask (autonomous run), state the assumption explicitly in the
   result instead of silently guessing.
3. Reuse existing builders, fixtures, resources, and package conventions.
4. Use the smallest suitable boundary:
   • **Unit test** for isolated logic (no Quarkus boot).
   • **`@QuarkusTest`** for CDI, persistence, transactions, configuration, or application
     behavior that cannot be isolated.
   • **REST/integration test** for routing, serialization, validation, security, or HTTP.
   • **WireMock** for external HTTP calls; verify requests and relevant responses or failures.
   • **jqwik** property tests only for plain code without CDI — jqwik doesn't run inside
     `@QuarkusTest`. See [references/jqwik.md](references/jqwik.md).

## Test data

All `@QuarkusTest` classes share one running application and one database, and data from
other tests is present. Therefore:
• Every test creates its own test data and asserts only against that data.
• Never assume an empty database or a known row count; don't assert on global lists
  without scoping them to your own data.
• Use unique values (UUIDs, generated GLN/names) so tests don't collide on unique
  constraints or on each other's data.
• Don't delete data you didn't create. `@TestTransaction` is the exception, not the default.

## Test design

• Use AssertJ for assertions and keep expected values independent of the implementation.
  Exception: inside RestAssured's `.then()` chain, Hamcrest matchers (`.body("name", equalTo(...))`,
  `.header(...)`) are fine — that's the RestAssured way. Outside that chain, use AssertJ only.
  For complex or nested responses, `.extract().as(...)` plus AssertJ is often clearer.
• Mocks: external HTTP → WireMock. Other genuine boundaries → Mockito. Never mock DTOs or
  internal beans just to isolate; use real values, builders, or fixtures. Inject a `Clock`
  instead of mocking time.
• Use RestAssured for HTTP tests and WireMock for external services.
• Add `@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)` to test classes.
• Name test methods in snake_case so the display name reads as a sentence. Domain terms in
  German as in the code, generic verbs in English:
  `create_returns_201_for_valid_Impfung`, `rejects_Korrektur_for_archived_Impfung`.
• Group scenarios with `@Nested`; use `@ParameterizedTest` for repeated cases (with a
  `name` that identifies each case).
• Test classes, `@Nested` classes and test methods package-private (Sonar S5786).
• Keep injected fields such as `@Inject` and `@TestHTTPResource` at least `protected`.
• Separate `ARRANGE`, `ACT`, and `ASSERT` with blank lines. Prefer behavior-focused names and
  assertions. Put each assertion on its own line after the `assertThat()` clause:

  ```java
  assertThat(actual)
      .isEqualTo(expected);
  ```

• Every test asserts something (S2699); only one call inside an `assertThatThrownBy` lambda
  (S5778); no `Thread.sleep` — use Awaitility (S2925).
• Protected endpoints: test the allowed role and at least 401 (anonymous) and 403 (wrong role).
• Keep Quarkus tests deterministic and use the project's builder and tenant/security setup.

## Implementation loop

For bugs, write a red regression test before production changes when practical. For features,
add tests proportional to risk, then make the smallest change that turns them green.

When a test fails:

1. Reproduce it with the narrowest relevant test and identify whether the cause is setup,
   production, environment, test data from other tests, or an existing assumption.
2. Fix the cause without weakening, deleting, skipping, or disabling meaningful assertions.
3. Rerun the focused test, then the applicable broader set.

A test that passes alone but fails in the full suite almost always depends on shared data
or ordering — fix the test's data scoping, not the order.

## Quality and verification

• Prefer IntelliJ MCP with the configured test run or test method/class. Use CLI only when MCP
  is unavailable or an explicit command is required.
• For CLI fallback, run from `../../../api` with the pinned JDK; if needed, set
  `JAVA_HOME="$HOME/.version-fox/sdks/java"` and verify `javac -version`.
  Example: `./mvnw test -Dtest='ImpfungServiceTest' -Dsurefire.failIfNoSpecifiedTests=false`.
• Consider at least 80% Jacoco coverage for new production paths, including applicable success,
  validation/authorization, boundary, and external-service failure cases.

## Done

Work is complete when the behavior is covered at the right boundary, project conventions are
met, focused and applicable broader tests pass, coverage and SonarQube are considered, and any
unverified limitation or assumption is reported.
