---
name: quarkus-update
description: Upgrade a Quarkus project to a newer platform version. Use whenever the user asks to update, upgrade or bump Quarkus, to move to a new LTS or to the latest Quarkus, or to check what a Quarkus upgrade would break.
---

# Quarkus update

The update tooling moves version numbers and runs a few recipes. It does not read the
migration guides, it does not notice when its own edit is inert, and it does not test
anything. Those three are the actual work.

## 1. Ask for the target version first

Never pick the version yourself. Releases after the knowledge cutoff exist, so look up
what is actually out before asking:

```bash
curl -s https://repo1.maven.org/maven2/io/quarkus/platform/quarkus-bom/maven-metadata.xml
```

Note that `<latest>`/`<release>` can point at a Beta, and the metadata does not say which
versions are LTS - check https://quarkus.io/releases/ for that. Read the current version
from the project (`quarkus.platform.version` in the pom), then offer the user the current
LTS, the latest final release, and staying on the present stream's newest patch. Say
which is which, and which one you recommend.

## 2. Read every migration guide in the range

Guides live in the Quarkus wiki, one page per minor:

`https://github.com/quarkusio/quarkus/wiki/Migration-Guide-<MAJOR>.<MINOR>`

Fetch **every** minor between current and target, not only the target. On an LTS-to-LTS
jump the target's own page is usually empty ("direct continuation of the previous
release") while everything that matters sits in the intermediate pages. Ask each page for
a complete list of headings and actions rather than a summary, so minor items are not
dropped.

Then decide applicability **with evidence from this codebase**, not from reasoning about
what the project probably does: grep for the annotation, class, config key or extension
each item names and record the hit count. "No `@ElementCollection` anywhere" is a
finding; "we probably don't use that" is not. Check the extension list in the pom (or the
`Installed features:` line in the boot log) before concluding an item does not apply.
Where a guide says a default changed, confirm the old default in the dependency's
bytecode (`javap -p -c`) instead of trusting the guide's wording.

## 3. Run the update through the Maven goal

First confirm the local JDK meets the target's minimum Java version (`java -version`,
`$JAVA_HOME`) - a too-old JDK fails confusingly, and the minimum is on the release page
or in the target's migration guide.

Prefer the Maven goal over the `quarkus update` CLI: the CLI shells out to `./mvnw`, and
might not be installed

```bash
echo y | mvn -B io.quarkus.platform:quarkus-maven-plugin:<TARGET>:update -Dstream=<MAJOR>.<MINOR>
```

- `-Dstream` is required, or you get the newest stream rather than the one asked for.
- The goal prompts for confirmation before applying the OpenRewrite recipes and **skips
  them silently under `-B`** - there is no assume-yes parameter, hence `echo y |`.
- Commit (or at least diff) before running it so you can see exactly what it touched.

If the wrapper's `distributionUrl` is unreachable, repoint it at
`https://repo.maven.apache.org/maven2/...` for the run and **restore it afterwards**;
verify with `git diff` that it is byte-identical before committing.

## 4. Review what the tooling changed - and what it missed

Read the whole diff. Recurring problems:

- **Inert property edits.** The tool sets a property the parent pom overrides. Classic
  case: it bumps `maven.compiler.release`, but the parent fills the compiler plugin's
  `<release>` from its own property (e.g. `maven-compiler-plugin.javaversion`), and
  explicit plugin configuration beats a property. Prove the result instead of trusting
  it: `mvn clean compile` and check for `javac [debug release NN]`, or `javap -v` on an
  output class and read the major version (61 = 17, 65 = 21). **Run `clean` first** -
  stale incremental output will happily show the old value.
- **Hardcoded versions that duplicate a BOM-managed one.** Generator plugins and test
  clients often pin a version "to match the one Quarkus uses". The BOM moves, the pin
  does not. Grep for every literal `<version>` left in the pom and compare against
  `mvn dependency:list`.
- **Pins that the new BOM now manages.** If the BOM caught up, drop the explicit version
  rather than leaving a redundant pin.
- **Extensions outside the platform.** A Quarkiverse extension is pinned by the platform
  BOM and can lag behind its own patch releases. If one is implicated in a failure, check
  its own releases and consider importing its BOM ahead of the platform BOM, with a
  comment saying why and when to drop it.

## 5. Enforcer plugin

`maven-enforcer-plugin` binds to `validate`, so it fails the build before a single test
runs. Rules usually in play: `requirePluginVersions`, `requireReleaseDeps`,
`requireMavenVersion`, `requireJavaVersion`, and in stricter setups
`dependencyConvergence` and `banDuplicateClasses`. Note that `requireJavaVersion` is
typically driven by the same parent property as the compiler plugin, so a Java bump has
to update both.

When the enforcer rejects the new graph, **propose a fix that makes the graph acceptable**
and explain the trade-off - do not reach for an exemption first:

1. Align the conflicting versions through `dependencyManagement`, preferably by letting
   the platform BOM win, or by importing the right BOM.
2. Add the missing explicit plugin version rather than exempting the plugin.
3. Only if neither works, add a narrow, commented exemption naming the coordinate and the
   reason.

Inspect with `mvn dependency:tree -Dverbose -Dincludes=<groupId>` and
`mvn enforcer:enforce -X`.

**Every upgrade is also the moment to clean up existing exemptions.** Go through
`unCheckedPlugins`, `additionalPlugins`, and any `<ignore>`/`<exclude>` entries under
`dependencyConvergence` or `banDuplicateClasses`: each names a coordinate that was once a
problem. Remove the ones whose coordinate no longer appears in `dependency:tree`, or whose
conflict the upgrade resolved. Confirm by deleting the entry and re-running
`mvn validate` - keep it only if the build actually fails without it. Report what you
removed.

## 6. Verify - the upgrade is not done until this passes

- **Baseline first.** Run the suite *before* touching anything and save the set of
  failing tests. A project's suite might not be green in a local environment, and without
  a baseline you cannot tell a pre-existing failure from one you caused. Compare sets,
  not counts. Run it while reading the migration guides - both are slow and neither
  depends on the other.
- **Build it the way CI does.** Read the CI config for the actual goal (often
  `clean install` with a coverage profile) and run that, not a convenient subset.
  Specifically, **do not disable code coverage**: the JaCoCo agent adds `$jacocoInit` to
  instrumented classes, which can break build-time bytecode transformers in extensions,
  and a `-Dskip-code-coverage=true` run hides it completely.
- **Know which tests actually ran.** Surefire (`test`) picks up `*Test.java`,
  `Test*.java`, `*Tests.java`, `*TestCase.java`; `*IT.java` runs only under failsafe, in
  `verify`/`install`. A class named `*ITest.java` matches surefire, a class named
  `*IT.java` does not - so "mvn test was green" can silently exclude the integration
  tests, which are usually the DB-heavy ones most likely to catch an ORM change. Count the
  executed classes and compare against the files on disk.
- **Regenerate and diff generated artifacts** - schema/DDL scripts, OpenAPI clients,
  model classes. Byte-identical output is strong evidence that a behavioural change does
  not touch this project.
- **Boot the application** (`quarkus:dev` and a packaged run) and exercise an endpoint.
  `quarkus:dev` blocks interactively - background it, or rely on the packaged run for the
  endpoint smoke test. `*QuarkusTest` classes do boot the app, but under the test profile
  - that does not replace a real boot of the packaged runner. Check first that no Quarkus
  instance is already running (a leftover `quarkus:dev` from earlier in the session): two
  instances fight over the port and the failure reads like an upgrade regression.
  Config-level changes such as HTTP host validation only show up at runtime.
- **Run the end-to-end suite against the upgraded backend** if there is one, and note that
  it may need its base URL pointed at the local stack explicitly.
- **Measure config changes rather than asserting them.** If a guide's item makes you add a
  setting, show the before/after behaviour (status codes, generated SQL, log lines).

Report what you verified and how, name anything you could not verify, and keep every
version bump you made by hand in the commit message with the reason.
