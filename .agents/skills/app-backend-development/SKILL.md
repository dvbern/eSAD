---
name: backend-development
description: Conventions for Java/Quarkus work in `api/` — resources, mappers, services, repos, entities and their code style, covering config access through `ConfigService`, checkerframework nullability and line wrapping. Use it whenever the task touches `api/` — "add this endpoint", "fix this service", "read this config property", "refactor this mapper" — even when style isn't mentioned. For backend tests use `backend-testing`.
argument-hint: "<backend scope or file>"
---

# Backend development

Use this skill for Java and Quarkus work in [`../../../api`](../../../api).

## Configuration

* Read configuration through
  [`ConfigService`](../../../api/src/main/java/ch/dvbern/oss/hemed/esc/api/shared/config/service/ConfigService.java)
  instead of injecting `@ConfigProperty` where the value is used. A new property becomes a
  `@ConfigProperty` field on `ConfigService` and is consumed via its Lombok getter.

## Nullability

* Use and respect `@Nullable` and `@NonNull` from `org.checkerframework.checker.nullness.qual`.

## Style

* Keep lines short — indentation and line length come from
  [`../../../.editorconfig`](../../../.editorconfig) — and wrap as follows:
  * one chained method call per line, with the dot on the new line;
  * one argument per line, with a newline after the opening parenthesis and before the closing
    parenthesis;
  * one builder continuation per line;
  * a newline before each `&&`, `||`, `?` and `:`.
* Keep `stream()` on the line of its source and start each following stream step on its own line:

  ```java
  List<String> namen = kinder.stream()
      .map(Kind::getName)
      .sorted()
      .toList();
  ```

## Done

Work is complete when configuration is reached through `ConfigService`, nullability is annotated
and respected, the wrapping rules hold for every line you touched, and the affected tests pass.
