# Frontend upgrade: Nx 23, Angular 22, Vitest

Upgrade of the frontend workspace (`frontend/`) from Nx 22.7 / Angular 21.2 / Jest to Nx 23.2 / Angular 22.1 /
Vitest. The work is split into steps that change one thing at a time. Each step is compared against the
[baseline](#baseline) before the next one starts.

## Status

| Step | Content                                         | Status      |
|------|-------------------------------------------------|-------------|
| 0    | Prep on current versions                        | done (CI Node version not yet seen in a pipeline) |
| 1    | Jest → Vitest (Nx 22.7.7, Angular 21.2)         | done        |
| 2    | Nx 23.0.2 (Angular 21.2, TypeScript 5.9)        | done        |
| 3    | Nx 23.2, Angular 22.1, TypeScript 6.0, libs     | done        |
| 4a   | Investigate the 30 kB bundle growth             | done: Angular 22 framework code, nothing to fix |
| 4b   | Strict templates for the libraries              | done        |
| 4c   | TypeScript 6 without compatibility settings     | done        |
| 4d   | Playwright e2e, switch HttpClient to fetch      | done        |
| 4e   | Remove `$safeNavigationMigration()` where possible | done: 100 removed, 5 replaced by `?? null` |
| 4f   | Install `prompts` for the Nx plugin              | done        |
| 4g   | Nx 23.2.1 and Angular 22.1.7/22.1.8 patches       | done        |
| 4h   | ng-select 24                                    | done        |
| 5    | Later follow-ups and optional migrations        | open        |

## Starting point and target

|                      | Now (2026-09-16)       | Target                  |
|----------------------|------------------------|-------------------------|
| Nx                   | 22.7.7                 | 23.2.1                  |
| Angular              | 21.2.18                | 22.1.7 (CLI/build 22.1.8) |
| TypeScript           | 5.9.2                  | 6.0.3                   |
| Unit test runner     | Jest 30 + `jest-preset-angular` 16 | Vitest 4 via `@nx/angular:unit-test` |
| Node                 | 24.15.0 (`.nvmrc`, `.tool-versions`, `engines`) | unchanged |

Constraints that shape the order (checked against npm package metadata on 2026-09-16):

- Angular 22 requires TypeScript `>=6.0 <6.1` and Node `^22.22.3 || ^24.15.0 || >=26`.
- `jest-preset-angular` 16 supports Angular `<22`. Staying on Jest would require `jest-preset-angular` 17.
- Nx 23 supports Angular 20–22. Nx's migrations bump Angular to 22.0 and TypeScript to 6.0 at Nx 23.1 without an
  opt-out prompt. Nx 23.0.2 still targets Angular 21.2.
- The installed Nx 22.7.7 already ships `@nx/angular:unit-test` (Angular's `@angular/build:unit-test` builder). All
  22 libs have `ng-packagr-lite` build targets, which that executor requires.
- No Nx or Angular tool rewrites Jest test code. Angular's `refactor-jasmine-vitest` schematic only covers
  Jasmine. The community codemod `npx codemod@legacy jest/vitest` converts `jest.*` calls.

## Baseline

Taken on `main` @ `ad9c17dbe`, Node 24.15.0, with `NX_DAEMON=false` and `--skip-nx-cache`.

| Check        | Command (in `frontend/`)                                         | Result                                   | Time |
|--------------|------------------------------------------------------------------|------------------------------------------|------|
| Unit tests   | `npx nx run-many -t test --runInBand`                            | 79 suites, 831 passed, 13 skipped, 0 failed | 19 s |
| Build        | `npx nx run-many -t build --configuration=production`            | green                                    | 34 s |
| Lint         | `npx nx run-many -t lint --all`                                  | 0 errors, 90 warnings                    | 15 s |
| Typecheck    | `npx tsc --noEmit -p apps/esc-app/tsconfig.app.json`             | clean                                    | 3 s  |

Tests per project: `angular` 246 (+7 skipped), `shared` 187 (+1), `anlass` 129, `types` 100, `feature` 64,
`components` 47, `viz` 18, `nx-plugin` 18, `stammdaten` 11 (+5), `kinder` 5, `i18n` 4, `anonymer-anlass` 1,
`publicinfo` 1. The other 12 projects have a test target but no specs. The 13 skipped tests are `.skip` calls in
`time-utils.spec.ts` (1), `dirty-guard.component.spec.ts`, `filter-functions.spec.ts`,
`filter-combinations.spec.ts` (7 together) and `stammdaten.component.spec.ts` (5). The `it.skip` in
`path-utils.spec.ts` only applies on Windows.

Existing warnings (not regressions when they show up later):

- Build: initial bundle budget exceeded (esc-app 1.29 MB, esc-info-app 622 kB, budget 500 kB);
  `apps/esc-app/src/app/app.component.scss` 149.61 kB against a 2 kB budget; about 435 Sass deprecation warnings,
  mostly the `if()` function in `libs/shared/ui/ui-components/src/styles/core/_unit.scss` plus `@import`;
  `qrcode` is not ESM.
- Lint: `no-unused-vars` 36, `no-explicit-any` 31, `no-non-null-assertion` 20, playwright rules 3.
- Tests: ts-jest warns 28 times that `isolatedModules` may prevent `emitDecoratorMetadata`.

Playwright e2e is not part of the baseline because it needs the full backend and Keycloak stack. It is run
before step 3 is merged.

## Step 0: Prep

- **Done:** removed `@nx/shared-fs-cache` from `devDependencies`. Nothing referenced it (no `nx.json` entry), and
  its latest release 5.0.7 declares `nx >= 18 < 23`, which would block `npm install` on Nx 23. The lockfile loses
  25 entries: the package, its nested copies, `@nx/key*`, `axios-retry`, `is-retry-allowed` and
  `@napi-rs/wasm-runtime`. Four `@emnapi`/`@tybys` entries become `optional`, and the root version is synced to
  `26.13.0-SNAPSHOT`.
- **Done:** removed the stale, untracked `frontend/migrations.json` (an Angular 20.3 SSR migration from September
  2025; the repo has no `main.server.ts`). `nx migrate` writes to the same file.
- **Done, not yet verified in a pipeline:** CI Node version. `.gitlab-ci.yml` passed `node-version: "20"` to the
  shared `components/build/nx` template. That input dated from 2024-11-22 and was not touched when the repo moved
  to Node 24 on 2026-05-13 (ESC-1233), although `engines` requires Node 24 and `check-engine` runs in the npm
  `pre*` hooks. It is now `"24"`. Angular 22 (step 3) needs Node ^22.22.3 or ^24.15.0. The first pipeline on this
  branch should confirm that the template accepts the value and that the job log prints Node 24.

Checks after step 0: 79 suites, 831 passed, 13 skipped; build green with the same 501 warning lines; lint 0 errors,
90 warnings; typecheck clean.

## Changing dependencies without Nexus access

`package-lock.json` pins tarballs to `https://nexus.dvbern.ch/nexus/content/groups/npm-all/`, which is only
reachable inside the DV Bern network. Nexus proxies `registry.npmjs.org` with the same paths, so tarballs and
integrity hashes are identical. Twenty committed entries already use `registry.npmjs.org` and must stay as they are.

What works (used in steps 0 to 3):

1. Start from the committed lockfile with Nexus URLs rewritten to `https://registry.npmjs.org/`.
2. Run the npm command with `--package-lock-only --registry=https://registry.npmjs.org/`, **without**
   `--legacy-peer-deps`.
3. For every lockfile entry that also exists in the committed lockfile with the same version, put the committed
   `resolved` URL back. New entries get the Nexus form of their URL.
4. Diff against `HEAD`: only the intended entries may change.

When a whole peer family changes major version (step 3), npm reports `ERESOLVE` against the old entries. Remove those
entries (including nested `node_modules` paths) from the public-URL copy first, then run step 2.

To install `node_modules`, write the public-URL copy of the lockfile in place, run
`npm ci --registry=https://registry.npmjs.org/`, and put the Nexus version back.

Pitfall: with `--legacy-peer-deps`, npm drops auto-installed peer dependencies from the lockfile (in step 0 that
removed `jsdom`, `@ngrx/store`, `tough-cookie` and others), which CI's regular install would not accept. Before
step 1 the flag was needed because `@types/node` 18 conflicted with the `@types/node` peer range of Vite. Since
step 1 raised `@types/node` to 24, a plain `npm ci` works.

## Step 1: Jest → Vitest

Done on the current Nx and Angular versions, so that a test failure in step 3 is not ambiguous between the runner
and the framework.

Result: 79 test files, 831 passed, 13 skipped, 0 failed, with the same counts per project as the baseline. Build
(501 warning lines), lint (0 errors, 90 warnings) and typecheck are unchanged. The test run takes 26 s instead of
19 s; both runs collect coverage.

### How tests run now

- **Angular projects** (12 with specs) use `@nx/angular:unit-test`, which wraps Angular's `@angular/build:unit-test`
  builder with Vitest 4 and jsdom. The target in `project.json` is just the executor; `nx.json` `targetDefaults`
  sets `watch: false`, `coverage: true`, `coverageReporters: ["lcov"]` and caches the output
  `{workspaceRoot}/coverage/{projectName}`. There is no per-project Vitest config and no setup file: the builder
  initialises `TestBed` itself.
- **`tools/nx-plugin`** uses `@nx/vitest:test` with `tools/nx-plugin/vitest.config.mts` (Node environment, coverage
  to `coverage/tools/nx-plugin`).
- **Projects without specs** (esc-app, esc-info-app, admin, app-mode-selection, aufgaben, config, dashboard,
  notizen, system, backend, playground, reports) no longer have a `test` target: the Angular builder fails with
  "No tests found" instead of passing like Jest's `passWithNoTests`. Their `tsconfig.spec.json` stays, switched to
  Vitest types, so adding a spec only needs the one-line `test` target.
- `tsconfig.spec.json`: `types` is `["vitest/globals", "node"]`; the Jest-specific `target: es2016`,
  `isolatedModules`, `emitDecoratorMetadata: false` and `module: commonjs` overrides are gone. `util-shared` and
  `data-access-types` now use `module: preserve` / `moduleResolution: bundler` like the other libs.
- `nx.json` generator defaults: `unitTestRunner: "vitest-angular"` for `@nx/angular:application` and
  `@nx/angular:library`. A dry run of the library generator works.
- The npm `test` script no longer passes Jest's `--runInBand`.

### Dependencies

- Removed: `jest`, `jest-environment-jsdom`, `jest-preset-angular`, `jest-util`, `ts-jest`, `@types/jest`,
  `@nx/jest`, `@lukemorales/jest-type-matchers`, `@testing-library/jest-dom` (only imported by esc-app's setup file,
  which had no specs). The root `jest.config.ts` and `jest.preset.js` (including the `^flat` mapper for transloco)
  are gone.
- Added: `vitest` ^4.1.11, `@vitest/coverage-v8` ^4.1.11, `jsdom` ^29.1.1 (already in the lockfile as a peer),
  `@nx/vitest` 22.7.7.
- `@types/node` 18.16.9 → ^24.13.3, matching the Node 24 runtime. Vitest declares a `@types/node` peer range of
  `^20 || ^22 || >=24`, so npm refused the install with 18. As a side effect, `npm ci` no longer needs
  `--legacy-peer-deps`.
- The lockfile loses 231 entries and gains 65. npm also moved `postcss` 8.5.21 → 8.5.28 and `nanoid` 3.3.16 → 3.3.18.

### Differences to Jest that required test changes

- **Specs are type-checked.** ts-jest compiled with `isolatedModules` and never reported type errors; the Angular
  builder type-checks the test program. Fixed errors that already existed:
  `default-values.util.spec.ts` (stores patched from tests need `{protectedState: false}`; `withDefaultValues` takes
  the entity type; `TestState` must be a `type`, because the util's `StoreSignals<T>` only maps types that extend
  `Record<string, unknown>`, which interfaces do not), `benutzer.store.spec.ts` and `page.component.spec.ts` (ID
  literals cast to `BackendEntityId`), and `util-shared`'s spec tsconfig (`moduleResolution: node` cannot resolve
  `@angular/common/http`).
- **Library tests run without zone.js.** The builder only loads zone.js when the build target's polyfills contain it,
  and `ng-packagr` targets have no polyfills. `stammdaten-dispatch.component.spec.ts` read `Router.url` right after a
  click and now awaits `fixture.whenStable()`.
- **Test files of a project share one environment** (the builder sets `isolate: false`). Global changes must be
  undone: `EntityId.spec.ts` spies on `crypto.randomUUID` and restores it instead of redefining `globalThis.crypto`;
  `AppValidators.spec.ts` switches back to real timers after its fake-timer tests.
- **`constructor.name` differs.** Jest saw Angular's `FormControl` as `FormControl2`; `FormWalker.spec.ts` now derives
  the control type with `instanceof`.
- `errorOnUnknownElements` and `errorOnUnknownProperties` are always on in the builder. No spec was affected.
- `vi.mock` is not supported for relative imports in Angular projects; mock through `TestBed` instead.
- Mechanical: `jest.fn/spyOn/useFakeTimers/clearAllMocks` → `vi.*`, `jest.Mock` → `Mock` from `vitest`,
  `@jest/globals` → `vitest`, `toHaveStrictType<T>()` → `expectTypeOf(x).toEqualTypeOf<T>()` (20 assertions).

### nx-plugin

`generator.spec.ts` mocks the `prompts` package, a CommonJS module whose export is the function itself.
`vi.mock` factories must return an object, and an ES module namespace is not callable, so `generator.ts` now takes
`default` when present and the module itself otherwise. That keeps working when Nx loads the plugin through
swc-node without ES module interop. `prompts` itself is not installed in the workspace (only `@types/prompts`), so
`npm run g:lib` failed already on `main`; installed in 4f.

CI is unchanged. Jest wrote `lcov` coverage to `coverage/<projectRoot>`; the Angular projects now write it to
`coverage/<projectName>` (`nx-plugin` keeps `coverage/tools/nx-plugin`). Neither runner produced JUnit reports.

## Step 2: Nx 23.0.2

`npx nx migrate 23.0.2`, then `npx nx migrate --run-migrations` (`migrations.json` removed afterwards). Angular and
TypeScript stay on 21.2 and 5.9.

Result: tests (79 files, 831 passed, 13 skipped), build (501 warning lines), lint (0 errors, 90 warnings) and
typecheck match the baseline.

- `package.json`: only the ten `nx`/`@nx/*` packages move from 22.7.7 to 23.0.2. The lockfile drops 75 entries that
  Nx 23 no longer pulls in (Module Federation and Rspack runtime packages) and picks up a few transitive patch
  releases.
- Of the 16 migrations, two changed files: `@nx/devkit/src/utils/string-utils` became `@nx/devkit/internal` in
  `tools/nx-plugin/src/generators/lib/generator.ts`, and `.gitignore` gained `.nx/migrate-runs`.
- Nx 23 publishes its packages with `exports` maps, so the plugin's `moduleResolution: node` (inherited from
  `tsconfig.base.json`) no longer found `@nx/angular/generators` or `@nx/angular/src/generators/library/schema`.
  `tools/nx-plugin/tsconfig.json` now uses `module`/`moduleResolution` `node16`, which still emits CommonJS because
  the plugin's `package.json` has `"type": "commonjs"`. The spec tsconfig no longer overrides `module`.
- With `node16`, TypeScript applies ES module interop and rejects calling the `prompts` namespace import. The
  runtime fallback from step 1 stays (Nx transpiles plugins with the root `tsconfig.base.json`, which has no
  interop); its type now comes from a type-only default import. Checked: `tsc` emits
  `__importStar(require("prompts")).default`, swc emits plain `require("prompts")`, and both end up with the function.
- `tools/nx-plugin/package.json` pins `@nx/angular` and `@nx/devkit`; `@nx/dependency-checks` fails lint when they
  differ from the installed version, and `nx migrate` does not update this file. Both are 23.0.2 now, and
  `tools/nx-plugin/package-lock.json` (public registry URLs only, as before) was regenerated. That lockfile resolves
  `nx` 23.2.1 through `@nx/angular`'s range; it is not used by the workspace install.
- The remaining deep imports `nx/src/generators/testing-utils/create-tree-with-empty-workspace` (two specs) and
  `@nx/angular/src/generators/library/schema` still resolve under Nx 23.0.2.

## Step 3: Nx 23.2, Angular 22.1, TypeScript 6.0

Done. The app was tested manually on 2026-09-16 and works; Playwright e2e passed later (see 4d).

Result: tests (79 files, 831 passed, 13 skipped), lint (0 errors, 90 warnings) and typecheck match the baseline. The
build passes with the same warnings; the log has 54 fewer lines only because Nx 23 no longer triggers Node's
`NO_COLOR` notice. `dist/apps/esc-app/browser` and `dist/apps/esc-info-app/browser` still exist for the image build.

### Versions

`.npmrc` sets `min-release-age = 7`, which rejects `nx` 23.2.1 (published 2026-09-09), so this step uses
**`npx nx migrate 23.2.0`**. It contains the same Angular 22.1 and TypeScript 6 migrations.

| Package | Before | After |
|---|---|---|
| `nx`, `@nx/*` | 23.0.2 | 23.2.0 |
| `@angular/*` (framework) | 21.2.18 | 22.1.5 |
| `@angular/cli`, `@angular/build`, `@angular-devkit/*`, `@schematics/angular` | 21.2.19 | 22.1.7 |
| `typescript` | 5.9.2 | 6.0.3 |
| `zone.js` | 0.16.2 | 0.16.3 |
| `ng-packagr` | 21.2.3 | 22.1.1 |
| `angular-eslint` | 21.3.1 | 22.5.0 |
| `@ngrx/signals`, `effects`, `operators` | ^21.1.0 | ^22.0.0 |
| `@angular-architects/ngrx-toolkit` | ^21.0.1 | ^22.0.0 |
| `@ng-bootstrap/ng-bootstrap` | 20.0.0 | 21.0.0 |
| `@ng-select/ng-select` | ^21.8.2 | ^23.11.0 (lowest major for Angular 22; 24.x exists) |
| `angularx-qrcode` | ^21.0.5 | ^22.0.1 |

Not bumped, because their peer ranges already include Angular 22 (the plan's earlier table was wrong for the first
two): `@ng-icons/*` 29.10 (`>=18`), `@testing-library/angular` 17.3 (`>=17`), `@jsverse/transloco` 7.6,
`@sentry/angular` 10.65 (`<=22.x`), `ngx-file-drop` 16.

Lockfile: npm cannot swap the whole Angular peer set while the old entries are in the lockfile (`ERESOLVE`). The
entries of the upgraded package families (Angular, Nx, NgRx, the bumped libraries, TypeScript, zone.js, ng-packagr,
angular-eslint, 175 entries including nested ones) were removed from the public-URL copy before
`npm install --package-lock-only`, so everything else stays pinned. Result: 327 entries added, 152 changed,
418 removed, one copy each of `@angular/core` and `typescript`. Most of the churn is Angular 22's build tool chain
(Babel 8, esbuild 0.28.2, lmdb).

`tools/nx-plugin/package.json` pins `@nx/angular`/`@nx/devkit` 23.2.0. Its lockfile was regenerated from scratch with
`--min-release-age=7`: that directory has no `.npmrc`, so the step 2 version had resolved `nx` 23.2.1. It now
resolves `nx` 23.2.0 and has 459 entries (883 before).

### Migrations (23 ran, 7 changed files)

- `@angular/core:http-xhr-backend` (2 files): `provideCore.ts` and `providePublicCore.ts` now pass `withXhr()` to
  `provideHttpClient`, keeping the XHR backend the app used before Angular 22.
- `@angular/core:strict-templates-default` (25 files): Angular 22 enables `strictTemplates` by default. Library
  `tsconfig.lib*.json` files, which never set it, get `strictTemplates: false`. Both apps already had `true`.
- `@angular/core:strict-safe-navigation-narrow` (46 files) added suppressions for the new
  `nullishCoalescingNotNullable`/`optionalChainNotNullable` diagnostics; `@nx/angular:
  remove-conflicting-extended-diagnostics` (44 files) removed them again from the libraries, where
  `strictTemplates: false` does not allow `extendedDiagnostics`. They remain in the apps' `tsconfig.app.json`.
- `@angular/core:safe-optional-chaining` (39 templates, **105 expressions**): Angular 22 makes template `a?.b`
  evaluate to `undefined` like JavaScript; before it was `null`. The migration wraps expressions where that can
  matter in `$safeNavigationMigration(...)`, which the compiler evaluates with the old `null` semantics, so behaviour
  is unchanged. The other ~740 `?.` in templates are left alone.
- `@nx/js:add-ignore-deprecations-for-ts6` (28 files): `"ignoreDeprecations": "6.0"` in every `tsconfig.json` and
  in `tsconfig.base.json`, which also pins the TypeScript 5 defaults that TypeScript 6 changed:
  `types: ["*"]`, `esModuleInterop: false`, `noUncheckedSideEffectImports: false`.
- No changes from `change-detection-eager` (all components already declare `OnPush`), `can-match-snapshot-required`,
  `incremental-hydration`, `model-output`, and the remaining Nx migrations. The ESLint flat-config and `ban-types`
  migrations only wrote AI prompt files: the workspace was already on flat config, no config references
  `ban-types`, and lint passes. `migrations.json` and `tools/ai-migrations/` are not kept.

### Other changes

- Library `package.json` peer ranges mirror the root versions (as ESC-1327 did for 21.2.18). 111 specifiers in 19
  files follow the new versions; `@nx/dependency-checks` fails lint otherwise.
- `single-radio-wrapper.component.spec.ts` used `svuiRadio` on `type="checkbox"`. Angular 22 type-checks the static
  attribute against the directive's `input.required<'radio'>()`, so the test input is now `type="radio"`.
- esc-app's initial bundle grew from 1.29 MB to 1.32 MB (`main` 222 → 261 kB) and hit the 1.3 MB error budget. The
  error budget in `apps/esc-app/project.json` is now **1.35 MB**. esc-info-app grew from 622 kB to 646 kB (warning
  only).

### To review before committing

- The 105 `$safeNavigationMigration()` wrappers. Alternative: the compiler option `legacyOptionalChaining: true`
  keeps `null` semantics globally without touching templates, but it is an escape hatch rather than the migration
  path. Wrappers can be removed one by one where `null` vs `undefined` makes no difference.
- The raised bundle budget, or an investigation of the 30 kB growth instead.
- `strictTemplates: false` on all libraries keeps today's behaviour; turning it on is a separate change.

## Step 4: Follow-up work

Planned on 2026-09-16, done one at a time, each checked against the baseline commands.

### 4a: Investigate the 30 kB bundle growth

Done. The growth comes from the new framework and library versions, not from workspace code. Nothing to fix; the
1.35 MB error budget stays.

Method: esc-app production builds with `--stats-json` on step 2 (`dcc68e1b0`, Angular 21, in a temporary worktree)
and on step 3, comparing the initial files (`main`, `polyfills`, `styles` and their static imports) per package.
The build changed too: Angular 22 enables chunk optimization in production builds by default, so the initial bundle
is now 7 files instead of 20, and the metafile's `bytesInOutput` values no longer add up to the file sizes (unchanged
packages such as `date-fns` appear 26 % larger). Each input was therefore scaled to its output file's real size;
with that, unchanged packages (`@sentry/*`, `date-fns`, `zone.js`, `immer`) stay within ±0.4 kB.

| Initial bundle | Step 2 (Angular 21) | Step 3 (Angular 22) |
|---|---|---|
| Raw size | 1.29 MB (1,287,957 bytes) | 1.32 MB (1,315,984 bytes) |
| Estimated transfer size | 277.85 kB | 281.85 kB |

| Package | Growth | Where |
|---|---|---|
| `@angular/core` | +8.3 kB | internal chunks reorganised (`_effect-chunk2` → `_pending_tasks-chunk`, +2.1 kB), `_debug_node` +4.3 kB, `primitives-event-dispatch` now initial (+1.3 kB) |
| `@angular/forms` | +7.1 kB | `forms.mjs` |
| `@angular/common` | +6.0 kB | HTTP `_module-chunk` +4.2 kB, `_common_module-chunk` +1.4 kB, `_xhr-chunk` +0.4 kB |
| `@ng-select/ng-select` 21.8 → 23.11 | +3.3 kB | |
| `@angular/router` | +2.2 kB | |
| `@ng-bootstrap/ng-bootstrap` 20 → 21 | +1.9 kB | |
| workspace libraries and app | about ±0 | |

With `withXhr()` the bundle contains the fetch backend (now Angular's default) and the XHR backend. The XHR part
is about 0.7 kB, so switching to fetch will not win back much. esc-info-app (622 → 646 kB) was not analysed
separately.

### 4b: Strict templates for the libraries

Done. The 22 `tsconfig.lib.json` files no longer set `strictTemplates: false`, so the libraries use Angular 22's
default (`true`) like the apps. Instead they carry the apps' suppression of the `nullishCoalescingNotNullable` and
`optionalChainNotNullable` diagnostics (revisit together with the `$safeNavigationMigration()` wrappers). The three
`tsconfig.lib.prod.json` files that step 3 had changed are back to their previous content.

No template needed a fix: the apps already compiled all library sources with strict templates through the tsconfig
path mappings. To confirm that the setting is active in the libraries' own `ng-packagr` builds, a temporary
`[initialState]="4711"` on `svui-card-collapsible` (input type `CollapsibleState`) was built with both
configurations: the library build now fails with `TS2322: Type '4711' is not assignable to type
'CollapsibleState'`, with step 3's configuration it passed. The probe was reverted.

Checks: tests (79 files, 831 passed, 13 skipped), build (447 warning lines as in step 3), lint (0 errors,
90 warnings) and typecheck unchanged.

### 4c: TypeScript 6 without compatibility settings

Done. No tsconfig uses a deprecated option or `ignoreDeprecations` anymore, so the workspace is also ready for
TypeScript 7 in this respect.

- `tsconfig.base.json`: removed `baseUrl`, `ignoreDeprecations` and the TypeScript 5 defaults pinned in step 3
  (`types: ["*"]`, `esModuleInterop: false`, `noUncheckedSideEffectImports: false`); `moduleResolution` is
  `bundler` instead of `node`.
- Without `baseUrl`, TypeScript requires relative `paths` (TS5090), so all 38 entries now start with `./`. Nx 23's
  library generator writes new entries in that form (`addTsConfigPath` → `ensureRelativePath`).
- `ignoreDeprecations` removed from the 27 other tsconfigs.
- Both e2e projects set `types: ["node"]`. TypeScript 6 no longer includes `@types/*` automatically, and
  `esc-info-app-e2e/playwright.config.ts` uses `process` and `__filename`; `esc-app-e2e` only compiled because
  an import pulled in Node types indirectly.
- `apps/esc-app/tsconfig.json` still sets `esModuleInterop: true`, which is TypeScript 6's default.

Verification:

- `tsc --noEmit` on all 53 tsconfigs with inputs (app, lib, spec, editor, e2e, nx-plugin): before the change one
  config had errors (`util-shared/tsconfig.lib.json`, 61 × "Cannot find module '@angular/common/http'" under
  `moduleResolution: node`; its `ng-packagr` build was not affected), afterwards none.
- Nx plugin at runtime: `nx g @dvb/nx-plugin:lib --dry-run` resolves the generator through the path mapping as before
  and still stopped at the missing `prompts` package (installed in 4f). swc-node still transpiles without ES module
  interop because `esModuleInterop` is unset (its default is `false`), so `require("prompts")` is unchanged.
- Tests (79 files, 831 passed, 13 skipped), build (447 warning lines), lint (0 errors, 90 warnings) and typecheck
  unchanged.

### 4d: Playwright e2e and the fetch backend

Done on 2026-09-16. The Playwright suite is being made reliable on `feature/improve-e2e-playwright-tests` (six commits
on top of `main`, including API and frontend fixes for races it found). To run it against this upgrade, a local
branch `tmp/angular-update-on-e2e` merged `feature/angular-update` into that branch; the merge had no conflicts.
On the merged code, unit tests (832 passed, the e2e branch adds one), build and typecheck passed. Lint reported three
`playwright/no-standalone-expect` errors in setup and fixture files that the e2e branch adds; that plugin was not
upgraded, so they belong to the e2e branch.

Run: BS tenant, API in Quarkus dev mode, esc-app dev server restarted on the merged code,
`E2E_TENANT=bs npx playwright test --config=apps/esc-app-e2e/playwright.config.ts` from `frontend/`.

| Run | Result |
|---|---|
| Angular 22 with `withXhr()` | 34 passed, 0 failed, 0 skipped (55 s) |
| Angular 22 with the fetch backend | 34 passed, 0 failed, 0 skipped (54 s) |

Same result as the e2e branch on Angular 21 (34 passed).

Fetch backend: `provideCore.ts` and `providePublicCore.ts` no longer pass `withXhr()`, so `HttpClient` uses
Angular 22's default `FetchBackend` (`withFetch()` is deprecated because fetch is the default). Fetch cannot report
upload progress; no hand-written code requests progress events, and the generated OpenAPI services only pass
`reportProgress` through. `authSessionExpiredInterceptorFn` reacts to HTTP 401 responses, which fetch reports the
same way. Loading the dashboard with the suite's saved admin login produced 11 `/api` requests, all with the browser
resource type `fetch`. Unit tests, build, lint and typecheck are unchanged on `feature/angular-update`; the initial
bundles are slightly smaller (esc-app 1.32 → 1.31 MB, esc-info-app 645.5 → 641.8 kB).

### 4e: Remove `$safeNavigationMigration()` where possible

Done on 2026-09-16. Angular 22 makes template `a?.b` evaluate to `undefined` when `a` is nullish; before it was
`null`. The step 3 migration wrapped 105 expressions in 39 templates in `$safeNavigationMigration()` to keep `null`.
The wrapper only changes anything when the receiver of `?.` is itself `null`/`undefined` (a missing property on an
existing object is `undefined` either way), so each instance was checked for (a) whether the receiver can be nullish
in that place and (b) whether the consumer treats `null` and `undefined` differently.

**Removed: 100.**

| Where the value goes | Instances | Why `null` vs `undefined` does not matter |
|---|---|---|
| `svui-form-control [viewValue]` → `svui-view-value` | 45 | rendered as `viewValue() ? viewValue() : '-'` |
| `svui-form-control-bottom [validateWhen]` / `[errors]` | 4 | `booleanAttribute` transform, `@if (errors())` |
| Getter/function arguments: `getStatus`, `formatDateStr`, `formatTimeWithDateIfNotToday`, `fromISODateString`, `getLinkToDossier`, `benutzerFiltered`, `getOptionForBenutzer` | 12 | truthiness or `isNullish`/`isNotNullish` |
| Pipes: `date`, `booleanPipe` | 6 | both return the same for `null` and `undefined` |
| Component inputs typed `T \| undefined` (`anlass-summary-layout`, `tenant-footer`, `untersuchung-impfstatus`, `form-upload-one`, `notizen [kindId]`, table `[loadedRowCount]`) | 10 | `?? '-'`, `?? 0`, truthiness, or never read (`kindId`) |
| `ng-select [ngModel]` | 2 | ng-select ignores both (`isDefined` in `_isValidWriteValue`) |
| Transloco parameters passed directly (Ärzte and Mitglieder headings) | 3 | Transloco's `DefaultTranspiler` treats both as nil |
| Receiver never nullish: dialogs whose data is set right after `open()` (Duplikat merge incl. `ng-option [value]` and `EntityAktivStatus.` key, confirm remove), the Impfungen button's `routerLink`/`queryParams` inside `@if (lastImpfung()?.…)`, ternaries that test the same chain (`Klassenstufe.`, `NaechsteStation.`), `kind() ? … : ''` with a non-optional `data` (`Geschlecht.` title) | 13 | the `?.` never short-circuits there |
| Both results are broken | 5 | see below |

The five "both broken" cases:

- The Untersuchung tabs' dossier link `'/kinder/' + currentTab()?.kindID` (1). With `?einladungIds=` in the URL (normal
  navigation) it is `/kinder/<id>`. Without it (bookmark, pasted URL) the tab list is empty and the link was
  `/kinder/null` before. Pre-existing bug, fixed here: the link is only rendered when the current tab has a `kindID`
  (`@if (showLinkToDossier() && untersuchungStore.currentTab()?.kindID; as kindID)`).
- `vorname + ' ' + name` in the Notiz detail and Notizen upload headings (4): only while the Notiz is loading
  (`kind = notiz()?.kind`, resolver does not wait), "null null" before, "undefined undefined" now.

**Replaced by an explicit `?? null`: 5.** Here `null` matters, so the templates state it instead of relying on the
migration helper:

- `'Land.' + (kind()?.data?.nationalitaet ?? null)` in `kind-dossier-main` and
  `'LandCode.' + (selectedKind()?.landCode ?? null)` in `kind-personendaten`. The translations define `Land.null`
  (blank) and `LandCode.null` ("Kein Land ausgewählt"), and `kind-dossier-main` renders while the Kind loads, so
  `null` selects a designed translation where `undefined` would show the raw key. `?? null` also maps a missing
  property to `null`, which the wrapper did not; the API has no Jackson null-exclusion configured and sends empty
  values as `null`, so the rendered text is the same.
- The three `json` pipes in `playground-long-list`. Here the difference shows: for the table variant without
  pagination, `itemTable.pagination()` is `undefined`, so the debug output reads `paginationConfig: null` instead of
  an empty value.

No `$safeNavigationMigration()` call is left in the workspace.

Verification:

- A read-only Playwright crawl with the e2e storage state of `ESC_SUPPORT` captured visible text, `title` attributes,
  link targets, input and ng-select values of 56 pages (7 Kind dossiers and profiles, Untersuchung Daten/Impfungen/
  Massnahmen for two Einladungen, Notiz detail and upload, Ärzte, Schulen, Anlass-Konfiguration, 5 Anlassplanungen,
  Aufgaben, Duplikate, all playground pages) before and after. Two identical baseline runs differed only in the
  dashboard's recently opened Anlässe and the playground's random list data; after the change the only other
  difference was the dossier link above. Not reachable in the crawl: the Organisation pages (no BS e2e user has the
  permission) and the Duplikat merge dialog (no duplicates in the seed data); both are covered by the analysis above.
- Unit tests (831 passed, 13 skipped), build, lint and typecheck unchanged.
- Playwright e2e on `tmp/angular-update-on-e2e` with the removal merged in: 34 passed, 0 failed (56 s).
- Follow-up (`?? null` and the dossier link), compared on the same data without an e2e run in between (the suite
  resets and changes the database): crawls with and without the four changed templates differed only in the hidden
  "Zum Dossier" link on Untersuchung pages opened without `?einladungIds=` and the playground's
  `paginationConfig: null`. With `?einladungIds=` the link still points to `/kinder/<id>`. Unit tests, build, lint
  and typecheck unchanged.
- Playwright e2e on `tmp/angular-update-on-e2e` with the follow-up merged in: 34 passed, 0 failed (58 s).

### 4f: Install `prompts` for the Nx plugin

Done on 2026-09-17. `tools/nx-plugin`'s lib generator imports `prompts`, which was never installed (only
`@types/prompts`, and that under `dependencies`).

- Root `package.json`: `prompts` ^2.4.2 added to `devDependencies`, `@types/prompts` moved there from
  `dependencies`. The lockfile gains one entry.
- `tools/nx-plugin/package.json`: `prompts` instead of `@types/prompts` in `dependencies`, as
  `@nx/dependency-checks` requires once the package resolves; lockfile regenerated with `--min-release-age=7`.
- `nx g @dvb/nx-plugin:lib --name=probe --type=util --scope=shared --dry-run` now runs through. Note: the generator
  writes the repository's `projects.json` (commit scopes) with `fs` even in a dry run; it was restored afterwards.
- `nx-plugin` lint, tests (18) and build green.

### 4g: Nx 23.2.1 and Angular 22.1.7/22.1.8 patches

Done on 2026-09-17. These patches were younger than `.npmrc`'s `min-release-age = 7`; the upgrade takes them
deliberately (Nx and Angular patch releases are trusted), and nothing else is exempt from the rule.

- `npm_config_min_release_age=0 npx nx migrate 23.2.1`: `nx` and `@nx/*` 23.2.1, no migrations.
- Angular framework packages 22.1.5 → 22.1.7; `@angular/cli`, `@angular/build`, `@angular-devkit/*`,
  `@schematics/angular` 22.1.7 → 22.1.8. Library peer ranges and `tools/nx-plugin` follow.
- Lockfile in two passes: the Angular/Nx families resolved without the age rule; then every other entry younger
  than 7 days (`@babel/*` 8.0.5, `memfs`/`@jsonjoy.com/fs-*` 4.78.0, `glob-to-regex.js` 1.3.1,
  `@napi-rs/wasm-runtime` 1.2.4) was removed and re-resolved under the rule. A lock-only run keeps stale nested
  entries, so `npm ci` must confirm the result (it rejected a leftover `@tybys/wasm-util` 0.9.0 once).
- Tests, build, lint and typecheck unchanged.

### 4h: ng-select 24

Done on 2026-09-17: `@ng-select/ng-select` ^23.11.0 → ^24.1.1 (24.1.2 was younger than the age window) plus its new
peer `@angular/cdk` 22.1.7.

What 24 changes (release notes): the dropdown renders in a CDK Overlay instead of next to the select;
CSS-variable theming; `appendTo` only controls the DOM location; `popover` is a no-op; the panel receives the
select's host classes; typeahead no longer emits on select/close/clear; Escape is only consumed when it closes an
open dropdown.

Changes in the workspace:

- `.ng-dropdown-panel.form-control` reset in `_ng-select-component-overrides.scss`. `svuiNgSelect` adds
  `form-control` to every select, ng-select 24 copies it onto the panel, and Bootstrap then gave the panel 8px
  padding (options inset, panel 16px taller) and, for invalid selects, the red validation icon. With the reset the
  forwarded classes no longer change the panel's computed style.
- `apps/esc-app/project.json`: initial bundle error budget 1.35 MB → 1.4 MB. The header's Kind search puts
  ng-select into the initial bundle, and with it the CDK overlay: +48.5 kB raw (`@angular/cdk` +37.5 kB, of which the
  overlay module 29.8 kB; esc-app styles +8.5 kB; ng-select +5.6 kB), 1.31 → 1.36 MB, transfer 281 → 292 kB.
- Library peer ranges (8 files).
- Typeahead: only `kind-search` uses it and feeds the Subject straight into the store, so the missing emissions do not
  matter. Our global `.ng-dropdown-panel` rules are not ancestor-scoped and still apply.

Verification:

- Screenshots and panel geometry before and after for five cases (dossier Schularzt select opening upwards,
  select in the "Brief generieren" dialog, Anlass list filter, header Kind search, playground multi-select): panels now
  live in `.cdk-overlay-pane`, stay on top (also over the dialog), keep option counts and selections; position
  differs by 1px, the list filter panel is 2px wider. In the dialog a second Escape (dropdown closed) now closes the
  dialog, as the release notes announce.
- Unit tests (831 passed, 13 skipped), build (447 warning lines), lint (0 errors, 90 warnings), typecheck green.
- Playwright e2e on `tmp/angular-update-on-e2e` with this change merged in: 34 passed (52 s).

## Step 5: Later follow-ups and optional migrations

Not required for the upgrade.

Recommended migrations (checked against the code on 2026-09-17):

- esc-app's `build`, `serve` and `extract-i18n` targets still use `@angular-devkit/build-angular:*`, which Angular 22
  deprecates together with the webpack tooling. esc-info-app already uses `@angular/build:*`; switching esc-app
  allows removing `@angular-devkit/build-angular` (and `@ngtools/webpack` with it).
- Generated OpenAPI services pass `reportProgress` (1,704 occurrences), deprecated in Angular 22 in favour of
  `reportUploadProgress`/`reportDownloadProgress`. Update the generator templates in `scripts/conf` and regenerate.
- Optional Angular schematics with remaining work: `inject-migration` (31 constructor injections left next to 1,604
  `inject()` calls), `signal-input-migration` (13 `@Input`), `output-migration` (5 `@Output`),
  `signal-queries-migration` (15 decorator queries), `router-testing-module-migration` (4 specs),
  `self-closing-tags-migration`, `cleanup-unused-imports`.
- `appendTo="body"` on about 320 ng-selects is no longer needed with the CDK overlay.
- ng-select shows "No items found" untranslated (`NgSelectConfig.notFoundText`); pre-existing.

Larger, optional:

- Zoneless change detection for esc-app (stable since Angular 22; esc-info-app already runs zoneless; a
  `feature/zoneless-migration` branch exists).
- Signal Forms are stable; the app has about 430 reactive-forms usages.
- Nx 23.2's `@nx/oxlint`/Oxfmt as faster lint/format options.

Keep in mind: Angular 22 changed the router's `paramsInheritanceStrategy` default to `'always'`. It has no effect today
(the only routed component input is `loading`, and no component reads route params directly), but inputs on routed
components now also receive parent route params, data and resolver results of the same name.

Other follow-ups:

- Sass deprecations (`if()`, `@import`).
- `tools/nx-plugin/package.json` carries security `overrides` (`form-data`, `shell-quote`, `websocket-driver`,
  `adm-zip`, `ws`) that ESC-1302 added because the Nx 23 update was pending. In the lockfile regenerated in step 3,
  `ws` and `adm-zip` are no longer part of the tree; check whether the overrides can go.
- Replace the `nx/src/generators/testing-utils/...` deep imports in the plugin specs with `@nx/devkit/testing`.

## CI components to check

None of this could be verified locally: `origin` is a mirror without access to the DV Bern GitLab pipelines. Check it
in the job logs of the first pipeline on this branch, or in the component sources.

`components/build/nx@1.13.0` (frontend build and test job):

- **Node version.** It accepts `node-version: "24"`, and the job log prints Node **24.15.0 or newer**. Angular 22
  requires `^24.15.0`; the repo's `engines` field only requires `>=24.0.0`, so an older 24.x image would pass
  `check-engine` and then fail in the Angular build.
- **Install command.** Whether it runs `npm ci` with `--legacy-peer-deps` or `--force`. Neither is needed anymore
  since step 1. With `--legacy-peer-deps`, npm skips peer dependencies, which the workspace no longer relies on, but
  the flag hides future peer conflicts.
- **Test command and flags.** Which command runs the unit tests (`npm run test`, `nx run-many -t test` or
  `nx affected -t test`), and whether it passes Jest-only flags such as `--runInBand`, `--ci` or `--coverage`, or
  `-c ci`. The removed `@nx/jest:jest` target default had a `ci` configuration; the new test targets have none.
- **Test timeout.** Jest ran with a 20 s per-test timeout when `CI` was set (`jest.preset.js`); Vitest's default is
  5 s. If slow runners produce timeouts, set `testTimeout` in a Vitest config passed through the `runnerConfig`
  option.
- **Reports and coverage.** Whether it collects JUnit XML (neither Jest nor Vitest produced any) or coverage files by
  path. `lcov.info` moved from `coverage/<projectRoot>/` to `coverage/<projectName>/` for the Angular projects
  (`nx-plugin` stays at `coverage/tools/nx-plugin/`). Neither runner printed a text coverage summary, so a
  coverage regex on the log had nothing to match before either.
- **Browsers.** Unit tests run in jsdom. If the component installs Chrome or Playwright for them, that is no longer
  needed.
- **Build artifacts.** The job must still export `dist/apps/esc-app/browser` and `dist/apps/esc-info-app/browser`,
  which `package:esc-app` (`components/package/buildah@2.1.0`, `frontend/Dockerfile`) copies into the image.

Not changed by this upgrade, but noticed: e2e is disabled (`e2e-on: 'never'`) and pins the Playwright image
`v1.41.1-jammy`, while the repo uses `@playwright/test` ^1.51. Align them before enabling e2e in CI.
