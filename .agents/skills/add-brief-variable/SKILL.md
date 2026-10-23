---
name: add-brief-variable
description: Procedure to add or update a Word template mail merge field (Briefvariable) in `api/`. Use when adding or modifying template variables, merge fields, or merge sources for letter templates (Briefvorlagen).
argument-hint: "<field-name or DTO>"
---

# Add a Word mail merge field (Briefvariable)

Use this skill to add or update a mail merge field (`MERGEFIELD`) for Microsoft Word templates (`.docx`).
Follow `backend-development` for code style and `backend-testing` for tests.

## Architecture

Main code: `api/src/main/java/ch/dvbern/oss/hemed/esc/api/features/briefvorlagen/briefgenerierung/docmerger/`.

Letter templates (`BriefvorlageType`) have two types:
- `KIND`: child letters. Prefixes: `allgemein`, `kind`, `externerArzt`.
- `ANLASS`: examination letters. Prefixes: the `KIND` prefixes plus `anlass`.

Key resolution:
- `RootMergeSource` routes top-level prefixes. For `anlass.*` it strips `anlass.` and delegates to
  `AnlassMergeSource`. In a `KIND` letter `anlassMergeSource` is `null`, so any `anlass.*` key throws
  `DOCMERGE_KEY_NOT_SUPPORTED_BY_BRIEFVORLAGE_TYPE`.
- `AnlassMergeSource` routes sub-prefixes (`meta`, `krankheit`, `impfung`, `impfstatusListe`, `sehen`,
  `hoeren.audiogramm`, `hoeren.otoskopie`, `groesseGewicht`, `untersuchungArzt`).
- Unresolved keys render as `[key]` in the letter. A `[...]` in the output means the routing is missing.

`MergeSourceCreatorService` builds the merge sources:
- Real letters: `fromKind(...)` and `fromAnlass(...)`.
- Test letters (Testbrief preview in the GUI, `BriefgenerierungService.generateTestBrief`):
  `testMergeSourceForKind(...)` and `testMergeSourceForAnlass(...)`. They take their data from
  `MergeSourceDummyDataCreator` (`api/src/main/java/.../shared/validator/`). This is **production code**.
- All four go through the static `toRootMergeSource(...)` (KIND) or `toRootMergeSourceWithAnlass(...)` (ANLASS).
  These call private static `to<X>DTO(...)` factories, one for each DTO.

### `BeanMergeSource`

`ch.dvbern.lib.doctemplate.common.BeanMergeSource` resolves `<prefix><property>` through the DTO getter:

```java
if (key.startsWith("impfstatusListe.")) {
    return new BeanMergeSource(impfstatusListSummaryDocMergerDTO, "impfstatusListe.").getData(ctx, key);
}
```

Write custom resolution only for dynamic keys (`krankheit.<KRANKHEIT>.*`) or matrix lookups
(`hoeren.audiogramm.<seite>.<hertz>.<dezibel>`).

## Procedure

### 1. Define the key

1. Select the template type (`KIND` or `ANLASS`) and the prefix from the Architecture list.
2. Name the canonical key after the field's location in the GUI (tab → section → input control).
   Example: Tab «Impfungen» → Section «Massnahmen» → Checkbox «Impfung beim Hausarzt» →
   `anlass.impfung.massnahmeImpfungBeimHausarzt`.

Criterion: Template type, target DTO, and the full canonical key are written down.

### 2. Update the DTO

1. Add a `String` property to the target `*DocMergerDTO`, named after the last key segment. For a new prefix,
   create a new DTO.
2. Annotate with `@Value`. Add `@AllArgsConstructor` only if the DTO also declares a convenience constructor
   (see `ImpfungDocMergerDTO`).
3. Update every `new <X>DocMergerDTO(` call site, in main and in test code.

Criterion: The project compiles and every constructor call passes the new value.

### 3. Route the key

Only needed for a new prefix or sub-prefix. Add a `BeanMergeSource` branch to `RootMergeSource` or
`AnlassMergeSource`, plus a constructor field for the new DTO.

Criterion: The full key reaches a getter of the DTO.

### 4. Populate the value in `MergeSourceCreatorService`

1. Set the value in the matching `to<X>DTO(...)` factory, from entity data.
2. Format dates with `PRINT_UTIL.formatDate(...)` and optional text with `PRINT_UTIL.trimToPlaceholder(...)`.
3. For absent data, return `""` or a localized fallback via `tl`.

Criterion: The four entry points (`fromKind`, `fromAnlass`, `testMergeSourceForKind`, `testMergeSourceForAnlass`)
produce a non-null value for the key.

### 5. Add translations

New fallback or display texts get a key in all four files in `api/src/main/resources/translations/`:
`translations-esc.properties` (de), `_en`, `_fr`, `_it`.

Criterion: The key exists in all four files.

### 6. Update the dummy data

Make sure `MergeSourceDummyDataCreator` fills the source entity field with realistic data, so the Testbrief
shows a value.

Criterion: A Testbrief would render a real value for the key, not `""`.

### 7. Update the template fixtures

The fixtures under `api/src/test/resources/DocMergerTest/` come in pairs:

| Type     | Template               | Expected text                         |
|----------|------------------------|---------------------------------------|
| `ANLASS` | `all-properties.docx`  | `expected/all-properties-textcontent.txt` |
| `KIND`   | `kind-properties.docx` | `expected/kind-properties-textcontent.txt` |

1. Add the `MERGEFIELD` to the matching `.docx`. A `KIND` field also goes into `all-properties.docx`.
   A `.docx` is a binary zip, so ask the user to insert the field in Word unless they asked you to edit
   `word/document.xml` yourself.
2. Update the expected text with the value that `MergeSourceDummyDataCreator` produces.
   `BriefvorlageResourceITest` compares the Testbrief output with this file exactly.

Criterion: Both `.docx` files and both expected texts contain the field.

### 8. Test

1. In `MergeSourceCreatorServiceTest`, cover formatting, absent data, and language fallbacks of the new value.
2. Run:
   ```bash
   api/mvnw -f api/pom.xml test -Dtest='MergeSourceCreatorServiceTest,MergeSourceCreatorServiceITest,DocMergerTest,BriefvorlageResourceITest,LayeringTest'
   ```

Criterion: All listed tests pass. For a field in a fixture, `BriefvorlageResourceITest` shows the new value.

## Rules

- **Single canonical key**: one key, one DTO property, one getter, one route per field. Rename by replacing
  the key, and update the fixtures in the same change.
- **GUI correlation**: the key mirrors tab / section / field in the GUI.
- **Bean routing**: bean properties resolve through `BeanMergeSource`. Custom string parsing is for dynamic or
  matrix keys only.
- **Type scope**: `KIND` templates use `allgemein`, `kind`, and `externerArzt` only.
- **Non-null values**: every value is a `String`: formatted data, `""`, or a localized fallback.
