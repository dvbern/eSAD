# HelloData Export

Machine-to-machine (M2M) interface that lets **HelloData** (the analytics platform of the
Kanton Bern) pull ESC master data once per night as a **full load**.

The exported datasets are listed below (Schulen, Impfstoffe, Labels, Benutzer, Notizen,
Aufgaben, Klassen, ImpfungenIntern, ImpfstatusKrankheiten, Anlaesse, Dossiers, Einladungen and
Untersuchungen); the feature is deliberately built so that further datasets can be added with
minimal ceremony.

- **Package:** `ch.dvbern.oss.hemed.esc.api.features.hellodata`
- **Base path:** `/api/hellodata/v1`
- **Tenant:** **Bern (BE) only**
- **Format:** NDJSON (newline-delimited JSON) **or** a JSON container object — both streamed
- **OpenAPI tag:** `HelloData` (schema: `api/openapi-schema/hellodata-openapi.yaml`)

---

## Endpoints

| Method | Path                                             | Produces               | Role                     |
|--------|--------------------------------------------------|------------------------|--------------------------|
| `GET`  | `/api/hellodata/v1/ndjson/schulen`               | `application/x-ndjson` | `HELLODATA_EXPORT_LESEN` |
| `GET`  | `/api/hellodata/v1/json/schulen`                 | `application/json`     | `HELLODATA_EXPORT_LESEN` |
| `GET`  | `/api/hellodata/v1/ndjson/impfstoffe`            | `application/x-ndjson` | `HELLODATA_EXPORT_LESEN` |
| `GET`  | `/api/hellodata/v1/json/impfstoffe`              | `application/json`     | `HELLODATA_EXPORT_LESEN` |
| `GET`  | `/api/hellodata/v1/ndjson/labels`                | `application/x-ndjson` | `HELLODATA_EXPORT_LESEN` |
| `GET`  | `/api/hellodata/v1/json/labels`                  | `application/json`     | `HELLODATA_EXPORT_LESEN` |
| `GET`  | `/api/hellodata/v1/ndjson/benutzer`              | `application/x-ndjson` | `HELLODATA_EXPORT_LESEN` |
| `GET`  | `/api/hellodata/v1/json/benutzer`                | `application/json`     | `HELLODATA_EXPORT_LESEN` |
| `GET`  | `/api/hellodata/v1/ndjson/notizen`               | `application/x-ndjson` | `HELLODATA_EXPORT_LESEN` |
| `GET`  | `/api/hellodata/v1/json/notizen`                 | `application/json`     | `HELLODATA_EXPORT_LESEN` |
| `GET`  | `/api/hellodata/v1/ndjson/aufgaben`              | `application/x-ndjson` | `HELLODATA_EXPORT_LESEN` |
| `GET`  | `/api/hellodata/v1/json/aufgaben`                | `application/json`     | `HELLODATA_EXPORT_LESEN` |
| `GET`  | `/api/hellodata/v1/ndjson/klassen`               | `application/x-ndjson` | `HELLODATA_EXPORT_LESEN` |
| `GET`  | `/api/hellodata/v1/json/klassen`                 | `application/json`     | `HELLODATA_EXPORT_LESEN` |
| `GET`  | `/api/hellodata/v1/ndjson/impfungenintern`       | `application/x-ndjson` | `HELLODATA_EXPORT_LESEN` |
| `GET`  | `/api/hellodata/v1/json/impfungenintern`         | `application/json`     | `HELLODATA_EXPORT_LESEN` |
| `GET`  | `/api/hellodata/v1/ndjson/impfstatuskrankheiten` | `application/x-ndjson` | `HELLODATA_EXPORT_LESEN` |
| `GET`  | `/api/hellodata/v1/json/impfstatuskrankheiten`   | `application/json`     | `HELLODATA_EXPORT_LESEN` |
| `GET`  | `/api/hellodata/v1/ndjson/anlaesse`              | `application/x-ndjson` | `HELLODATA_EXPORT_LESEN` |
| `GET`  | `/api/hellodata/v1/json/anlaesse`                | `application/json`     | `HELLODATA_EXPORT_LESEN` |
| `GET`  | `/api/hellodata/v1/ndjson/dossiers`              | `application/x-ndjson` | `HELLODATA_EXPORT_LESEN` |
| `GET`  | `/api/hellodata/v1/json/dossiers`                | `application/json`     | `HELLODATA_EXPORT_LESEN` |
| `GET`  | `/api/hellodata/v1/ndjson/anlaesse`              | `application/x-ndjson` | `HELLODATA_EXPORT_LESEN` |
| `GET`  | `/api/hellodata/v1/json/anlaesse`                | `application/json`     | `HELLODATA_EXPORT_LESEN` |
| `GET`  | `/api/hellodata/v1/ndjson/einladungen`           | `application/x-ndjson` | `HELLODATA_EXPORT_LESEN` |
| `GET`  | `/api/hellodata/v1/json/einladungen`             | `application/json`     | `HELLODATA_EXPORT_LESEN` |
| `GET`  | `/api/hellodata/v1/ndjson/untersuchungen`        | `application/x-ndjson` | `HELLODATA_EXPORT_LESEN` |
| `GET`  | `/api/hellodata/v1/json/untersuchungen`          | `application/json`     | `HELLODATA_EXPORT_LESEN` |

Responses: `200` (streamed body), `401` (not authenticated), `403` (wrong client/role).

### Two formats, same data

Every dataset is offered twice because it is not settled whether HelloData's ingestion can read
NDJSON. Both endpoints return the same rows from the same cursor, are streamed by the server,
and end with the same `_meta` sentinel. Pick one — they are not meant to be called in parallel.

**NDJSON (`/ndjson/schulen`) is the recommended format**: the consumer can parse it line by line and
never has to hold the export in memory.

**The JSON container (`/json/schulen`) needs nothing but a standard JSON parser.** The price is
paid on the client: `json.loads(response.text)` buffers the whole export before the first row is
visible. That is a property of the consumer, not of the endpoint — a consumer using an
incremental parser (`ijson`, Jackson streaming) keeps the streaming end to end.

### Why two paths instead of content negotiation

An `Accept` header is too easy to get wrong from the outside. A client sending `Accept: */*` —
which curl and most generated clients do by default — would get whichever variant JAX-RS happens
to prefer, and the failure mode is a *silently different response body* rather than an error.
Separate paths make the choice explicit and keep the existing NDJSON endpoint provably
unchanged.

---

## Response format (NDJSON)

The response is **newline-delimited JSON**: one JSON object per line, separated by `\n`, with
**no enclosing array and no commas**. This lets the consumer parse line by line
(`for line in response.iter_lines()`) instead of needing an incremental JSON parser.

Every data line matches `HelloDataSchuleJson`. The **final line** is always a
`HelloDataSentinelJson` nested under the key `_meta`:

```
{"id":"...","schulhaus":"Schulhaus A","plz":"3000","ort":"Bern","aktivStatus":"AKTIV","timestampMutiert":"2026-01-01T12:00:00+01:00"}
{"id":"...","schulhaus":"Schulhaus B", ... }
...
{"_meta":{"status":"complete","rowCount":1234,"exportedAt":"2026-01-01T02:15:00+01:00"}}
```

### Why the sentinel line matters

Once the response has started with HTTP `200`, a failure mid-stream (network drop, proxy
timeout, exception) **can no longer be signalled via the status code**. A truncated stream is
indistinguishable from a short table.

**HelloData must therefore check that the last line carries a `_meta` with
`status = "complete"` and treat the load as failed otherwise.** `rowCount` must equal the
number of preceding data rows.

---

## Response format (JSON container object)

The alternative framing is one ordinary JSON document — the rows in a `data` array, closed by
the same `_meta` object:

```json
{"data":[
{"id":"...","schulhaus":"Schulhaus A","plz":"3000","ort":"Bern","aktivStatus":"AKTIV","timestampMutiert":"2026-01-01T12:00:00+01:00"},
{"id":"...","schulhaus":"Schulhaus B"}
],"_meta":{"status":"complete","rowCount":2,"exportedAt":"2026-01-01T02:15:00+01:00"}}
```

The document is written **incrementally** by `JsonEnvelopeWriter` — opening brace, then row after
row, then the sentinel — so the cursor, the flush cadence and the bounded heap usage on the
server are exactly the same as for NDJSON.

**`_meta` is deliberately the last member.** `rowCount` is only known once the cursor is
exhausted; putting the sentinel first would mean counting the rows upfront, which means
materialising them — the exact opposite of streaming. JSON members are unordered, so read it by
key, never by position.

A truncated response is still detectable here, and in fact harder to miss: it is not valid JSON
at all. The `_meta` check stays the reliable signal, though — a lenient parser may accept a
truncated document.

The rows are separated by newlines even though JSON does not require it: one byte per row, and
the response stays readable with `head` while debugging.

### The envelope DTO does not produce the response

`HelloDataSchulenEnvelopeJson` **describes** the wire format so it appears in the OpenAPI
contract; it is never serialised into a response, because building an instance would mean
holding every row in memory. That split is a drift risk — renaming a field on the DTO would
change the published contract without changing a byte the server sends — so
`HelloDataResourceITest.json_envelope_matches_the_declared_contract_type` compares a serialised
instance against a real response.

---

## The `HelloDataSchuleJson` contract

This DTO is a **contract towards HelloData**, not a mirror of the `Schule` entity. Fields must
not be added or renamed casually, because the statistic models at HelloData build on them.

| Field              | Type            | Required | Notes                                             |
|--------------------|-----------------|----------|---------------------------------------------------|
| `id`               | UUID            | yes      | Production UUID, stable across loads — the join key |
| `schulhaus`        | string          | yes      |                                                   |
| `schulstandort`    | string          | no       |                                                   |
| `schulkreis`       | string          | no       |                                                   |
| `schulhausnummer`  | string          | no       |                                                   |
| `quartierId`       | UUID            | no       | `leftJoin`, so Schulen without a Quartier survive |
| `plz`              | string          | no       |                                                   |
| `ort`              | string          | no       |                                                   |
| `aktivStatus`      | `AKTIV`/`INAKTIV` | yes    |                                                   |
| `timestampMutiert` | OffsetDateTime  | yes      |                                                   |

**Deliberately not exported** (data protection):

- `email`, `telefon` — contact details, not needed for analytics
- `bemerkungen` — free text, may contain personal data
- `zustaendigArzt`, `zustaendigMPA` — personal data of staff
- `adresse.strasseUndNr` — PLZ and Ort are granular enough for analytics

---

## The `HelloDataImpfstoffJson` contract

This DTO is a **contract towards HelloData**, not a mirror of the `Impfstoff` entity. Fields must
not be added or renamed casually, because the statistic models at HelloData build on them.

| Field         | Type   | Required | Notes                                                        |
|---------------|--------|----------|--------------------------------------------------------------|
| `id`          | UUID   | yes      | Production UUID, stable across loads — the join key          |
| `name`        | string | yes      |                                                              |
| `krankheiten` | string | yes      | Comma-separated `Krankheit` enum names, e.g. `MASERN,MUMPS` — the raw persisted column, handed on unchanged |

`Impfstoff` has no tenant column (the master data is global), so the export returns every
`Impfstoff`. The BE-only restriction still applies via
`HelloDataExportService.requireAllowedTenant(...)`.

---

## The `HelloDataLabelJson` contract

This DTO is a **contract towards HelloData**, not a mirror of the `Label` entity. Fields must
not be added or renamed casually, because the statistic models at HelloData build on them.

| Field       | Type              | Required | Notes                                             |
|-------------|-------------------|----------|---------------------------------------------------|
| `id`        | UUID              | yes      | Production UUID, stable across loads — the join key |
| `labelTyp`  | `NOTIZEN`/`AUFGABEN` | yes   | The type of the Label                             |
| `nameDe`    | string            | yes      | German name                                       |
| `nameFr`    | string            | yes      | French name                                       |
| `autoLabel` | boolean           | yes      | `true` when the Label is automatically assigned   |
| `aktiv`     | boolean           | yes      | Whether the Label is still in use                 |

`Label` has no tenant column (the master data is global), so the export returns every `Label`.
The BE-only restriction still applies via `HelloDataExportService.requireAllowedTenant(...)`.

---

## The `HelloDataBenutzerJson` contract

This DTO is a **contract towards HelloData**, not a mirror of the `Benutzer` entity. Fields must
not be added or renamed casually, because the statistic models at HelloData build on them.

| Field        | Type   | Required | Notes                                                        |
|--------------|--------|----------|--------------------------------------------------------------|
| `id`         | UUID   | yes      | Production UUID, stable across loads — the join key          |
| `fachrollen` | string | yes      | Comma-separated `FachRolle` enum names, e.g. `MPA,SUPPORT` — the union of the SystemRollen and all OrganisationRollen (`Benutzer.getFachRollenCombined()`); empty string when the Benutzer has none |

**Deliberately not exported** (data protection):

- `vorname`, `nachname`, `titel` — personal data, not needed for analytics
- `email`, `telefon` — contact details
- `keycloakId`, `loginInfos` — authentication internals

`Benutzer` has no tenant column; the BE-only restriction applies via
`HelloDataExportService.requireAllowedTenant(...)`. The export projects directly from
QueryDSL using database-side string aggregation (`listagg` within group) to combine system
and organization roles into `fachrollen` (see `HelloDataBenutzerRepo`).

---

## The `HelloDataNotizJson` contract

This DTO is a **contract towards HelloData**, not a mirror of the `Notiz` entity. Fields must
not be added or renamed casually, because the statistic models at HelloData build on them.

| Field                     | Type                     | Required | Notes                                                        |
|---------------------------|--------------------------|----------|--------------------------------------------------------------|
| `id`                      | UUID                     | yes      | Production UUID, stable across loads — the join key          |
| `dossierId`               | UUID                     | yes      | Foreign key to the Kinddossier                               |
| `verantwortlichePersonId` | UUID                     | yes      | Foreign key to the Benutzer (`Notiz.verantwortlicher`)       |
| `status`                  | `AKTIV`/`ABGESCHLOSSEN`  | yes      | `NotizStatus`                                                |
| `hatFileanhang`           | boolean                  | yes      | `true` as soon as at least one file has been uploaded        |
| `labels`                  | string                   | yes      | Comma-separated Label UUIDs, ordered by UUID, e.g. `350f2746-...,f8514824-...`; empty string when the Notiz has no Label |

**Deliberately not exported** (data protection):

- `titel`, `bemerkungen` — free text, may contain personal data
- `files` — the uploaded documents themselves; only `hatFileanhang` is of interest
- `anlassLink` — an internal deep link, not an analytical dimension

`Notiz` has no tenant column (the tenant comes with the database), so the export returns every
`Notiz`. The BE-only restriction applies via
`HelloDataExportService.requireAllowedTenant(...)`.

`labels` is aggregated database-side (`listagg` within group) over the `Notiz_Label` join table
and `hatFileanhang` is an exists-subquery (`files is not empty`), so neither Labels nor FileData
are ever loaded into the heap (see `HelloDataNotizRepo`). The Labels are joined with a
`leftJoin`, so a Notiz without Labels stays in the export.

---

## The `HelloDataAufgabeJson` contract

This DTO is a **contract towards HelloData**, not a mirror of the `Aufgabe` entity. Fields must
not be added or renamed casually, because the statistic models at HelloData build on them.

| Field           | Type                                     | Required | Notes                                                        |
|-----------------|------------------------------------------|----------|--------------------------------------------------------------|
| `id`            | UUID                                     | yes      | Production UUID, stable across loads — the join key          |
| `aufgabeStatus` | `OFFEN`/`IN_BEARBEITUNG`/`ABGESCHLOSSEN` | yes      | `AufgabeStatus`                                              |
| `faelligBis`    | LocalDate                                | yes      | Fälligkeitsdatum, e.g. `2026-01-31`                          |
| `userErfasst`   | UUID                                     | yes      | Foreign key to the Benutzer who created the Aufgabe          |
| `empfaenger`    | UUID                                     | yes      | Foreign key to the Benutzer responsible for the Aufgabe      |
| `kindId`        | UUID                                     | no       | `leftJoin`, so Aufgaben without a Kind survive               |
| `anlassId`      | UUID                                     | no       | `leftJoin`, so Aufgaben without an Anlass survive            |
| `labels`        | string                                   | yes      | Comma-separated Label UUIDs, ordered by UUID, e.g. `350f2746-...,f8514824-...`; empty string when the Aufgabe has no Label |

**Deliberately not exported** (data protection):

- `titel`, `bemerkung` — free text, may contain personal data
- `files` — the uploaded documents themselves, not an analytical dimension
- `link` — an internal deep link, not an analytical dimension

`Aufgabe` has no tenant column (the tenant comes with the database), so the export returns every
`Aufgabe`. The BE-only restriction applies via
`HelloDataExportService.requireAllowedTenant(...)`.

`labels` is aggregated database-side (`listagg` within group) over the `Aufgabe_Label` join
table, so no Label entity is ever loaded into the heap (see `HelloDataAufgabeRepo`). Labels,
Kind and Anlass are all joined with a `leftJoin`, so an Aufgabe without Labels, without a Kind
or without an Anlass stays in the export.

---

## The `HelloDataKlasseJson` contract

This DTO is a **contract towards HelloData**, not a mirror of the `Klasse` entity. Fields must
not be added or renamed casually, because the statistic models at HelloData build on them.

| Field               | Type              | Required | Notes                                                                                       |
|---------------------|-------------------|----------|---------------------------------------------------------------------------------------------|
| `id`                | UUID              | yes      | Production UUID, stable across loads — the join key                                         |
| `schuleId`          | UUID              | yes      | Reference to the Schule the Klasse belongs to                                               |
| `bezeichnung`       | string            | yes      | Name of the Klasse (e.g. 3A)                                                                |
| `klassenstufe`      | `Klassenstufe`    | yes      | Enum: `KINDERGARTEN_1`, `KINDERGARTEN_2`, `PRIMAR_KLASSE_1`..`6`, `SEK_I_KLASSE_1`..`3`, `SEK_II_KLASSE_1`..`4`, `ANDERE` |
| `klasseSchulart`    | string            | yes      | e.g. `Sek A`                                                                                |
| `klasseAktivStatus` | `AKTIV`/`INAKTIV` | yes      | EntityAktivStatus                                                                           |

**Deliberately not exported** (data protection):

- `lehrpersonen` — personal data of staff
- `externalImportInfo` — internal sync metadata

---

## The `HelloDataImpfungInternJson` contract

This DTO is a **contract towards HelloData**, not a mirror of internal entities. Fields must
not be added or renamed casually, because the statistic models at HelloData build on them.

| Field                          | Type                                                    | Required | Notes                                                                                 |
|--------------------------------|---------------------------------------------------------|----------|---------------------------------------------------------------------------------------|
| `id`                           | UUID                                                    | yes      | Production UUID (`UntersuchungImpfung`), stable across loads — the join key          |
| `untersuchungId`               | UUID                                                    | yes      | Production UUID of the parent `Untersuchung`                                          |
| `untersuchungImpfungenId`      | UUID                                                    | yes      | Production UUID of the parent `UntersuchungImpfungen`                                 |
| `impfstoffId`                  | UUID                                                    | yes      | Production UUID of the `Impfstoff`                                                    |
| `chargeId`                     | UUID                                                    | no       | Production UUID of the `ImpfstoffCharge` if recorded                                  |
| `datumImpfung`                 | LocalDate                                               | no       | Vaccination date if recorded                                                          |
| `impfStatus`                   | `ImpfStatus`                                            | yes      | Status of the vaccination, e.g. `GEIMPFT`                                             |
| `anlassImpfungErfasstId`       | UUID                                                    | yes      | Production UUID of the `Anlass` where the vaccination was recorded                    |
| `anlassImpfungDurchgefuehrtId` | UUID                                                    | no       | Production UUID of the `Anlass` where the vaccination was administered, if applicable |
| `krankheitenImmunitaeten`      | array of `HelloDataImpfungInternKrankheitImmunitaetJson`| yes      | Diseases and immunity statuses covered by this vaccination                            |

### Nested `HelloDataImpfungInternKrankheitImmunitaetJson`

| Field                 | Type        | Required | Notes                                    |
|-----------------------|-------------|----------|------------------------------------------|
| `krankheit`           | `Krankheit` | yes      | Enum name of the disease covered         |
| `immunitaetGenuegend` | boolean     | yes      | Whether sufficient immunity is confirmed |

---

## The `HelloDataImpfstatusKrankheitJson` contract

This DTO is a **contract towards HelloData**, not a mirror of internal entities. Fields must
not be added or renamed casually, because the statistic models at HelloData build on them.

| Field                    | Type                    | Required | Notes                                                                                  |
|--------------------------|-------------------------|----------|----------------------------------------------------------------------------------------|
| `id`                     | UUID                    | yes      | Production UUID (`UntersuchungImpfstatusKrankheit`), stable across loads — the join key |
| `untersuchungId`         | UUID                    | yes      | Production UUID of the parent `Untersuchung`                                           |
| `untersuchungImpfungenId`| UUID                    | yes      | Production UUID of the parent `UntersuchungImpfungen`                                  |
| `krankheit`              | `Krankheit`             | yes      | Enum name of the disease                                                               |
| `anzahlImpfungen`        | integer                 | no       | Number of vaccinations recorded for this disease                                       |
| `datumLetzteImpfung`     | LocalDate               | no       | Date of the last vaccination                                                           |
| `impfschutzGegeben`      | `ImpfschutzGegeben`     | yes      | Whether immunity/protection is sufficient (`JA`, `NEIN`, `KEINE_ANGABE`)                |
| `impfempfehlung`         | boolean                 | yes      | Whether vaccination is recommended                                                     |
| `impfentscheid`          | boolean                 | yes      | Whether vaccination was decided upon                                                   |

---

## The `HelloDataAnlassJson` contract

This DTO is a **contract** towards HelloData, not a mirror of the `Anlass` entity. Fields must
not be added or renamed casually, because the statistic models at HelloData build on them.

| Field          | Type                                                                         | Required | Notes                                                        |
|----------------|------------------------------------------------------------------------------|----------|--------------------------------------------------------------|
| `id`           | UUID                                                                         | yes      | Production UUID, stable across loads — the join key          |
| `titel`        | string                                                                       | no       | Anlasstitel; only mandatory when the Tenant has the feature enabled |
| `schuljahr`    | integer                                                                      | yes      | Schuljahr 2025 runs from August 2025 to July 2026            |
| `zeitVon`      | LocalDateTime                                                                | no       | Start of the Anlass, from the embedded `DateTimeRange`       |
| `zeitBis`      | LocalDateTime                                                                | no       | End of the Anlass; absent together with `zeitVon`            |
| `anlassStatus` | `IN_PLANUNG`/`AUFGEBOTEN`/`BEREIT`/`DURCHGEFUEHRT`/`ABGESCHLOSSEN`/`ABGESAGT` | yes      | `AnlassStatus`                                               |
| `anlassArt`    | `EINZEL_ANLASS`/`GRUPPEN_ANLASS`                                             | yes      | `AnlassArt`                                                  |
| `arztId`       | UUID                                                                         | no       | Foreign key to the Benutzer carrying out the Anlass; `leftJoin`, so Anlaesse without Arzt survive |
| `mpaId`        | UUID                                                                         | no       | Foreign key to the responsible MPA Benutzer; `leftJoin` as well |

**Deliberately not exported** (data protection):

- `arztInfo`, `mpaInfo`, `weiterePersonInfo` — free text about staff, may contain personal data
- `weiterePerson` — staff personal data, not an analytical dimension
- `anlassConfig` — internal configuration of the Anlass, not an analytical dimension

`Anlass` has no tenant column (the tenant comes with the database), so the export returns every
`Anlass`. The BE-only restriction applies via
`HelloDataExportService.requireAllowedTenant(...)`.

---

## The `HelloDataDossierJson` contract

This DTO is a **contract towards HelloData**, not a mirror of the `Dossier` (or `Kind`) entity.
Fields must not be added or renamed casually, because the statistic models at HelloData build on
them.

| Field                          | Type                       | Required | Notes                                                        |
|--------------------------------|----------------------------|----------|---------------------------------------------------------------|
| `id`                           | UUID                       | yes      | Production UUID, stable across loads — the join key          |
| `kindId`                       | UUID                       | yes      | Foreign key to the Kind (`Dossier.kind`)                      |
| `geschlecht`                   | `Geschlecht`                | yes      | `MAENNLICH`/`WEIBLICH`/`DIVERS`/`UNBEKANNT`                   |
| `geburtsjahr`                  | number                     | yes      | Year extracted from `Kind.geburtsdatum` (database-side)      |
| `nationalitaet`                | `Land`                      | no       | ISO country code enum; `null` when unknown                    |
| `erstsprache`                  | string                     | no       |                                                                |
| `wohntyp`                      | string                     | no       | e.g. `Eltern`, `Heim`, `Grosseltern`                          |
| `schuleId`                     | UUID                       | no       | `leftJoin`; the Schule is usually implied by the Klasse, but some Kinder only have a Schule |
| `klasseId`                     | UUID                       | no       | `leftJoin`, so a Kind without a Klasse survives               |
| `kindAktivStatus`              | `EntityAktivStatus`         | yes      | `AKTIV`/`INAKTIV`                                             |
| `kinderschutzfall`             | boolean                    | yes      |                                                                |
| `keineAuskunftAnDrittpersonen` | boolean                    | yes      |                                                                |
| `sansPapier`                   | boolean                    | yes      |                                                                |
| `immunitaetMasern`             | boolean                    | yes      |                                                                |
| `immunitaetVarizellen`         | boolean                    | yes      |                                                                |
| `papierakteStatus`             | `PapierakteStatus`          | yes      | `VORHANDEN`/`NICHT_VORHANDEN`/`UNKLAR`                        |
| `hashValue`                    | string                     | yes      | SHA-256 over Name, Vorname, Geschlecht, Geburtsdatum (`Kind.hashValue`, same computation as `AnonymerEintrag.hash`) — hints at duplicate Kinder |

**Deliberately not exported** (data protection):

- `name`, `vorname`, `adresse`, `email`, `telefon` of the Kind — personal data, not needed for
  analytics
- `andereZusatzinformation`, `andereZusatzInformationText` — free text, may contain personal data
- `zustaendigArzt`, `zustaendigMPA` — personal data of staff

`Dossier` and `Kind` have no tenant column (the tenant comes with the database), so the export
returns every Dossier. The BE-only restriction applies via
`HelloDataExportService.requireAllowedTenant(...)`.

`geburtsjahr` is derived from `Kind.geburtsdatum` directly in the QueryDSL projection
(`geburtsdatum.year()`), so no `LocalDate` is loaded just to extract its year. `schuleId` and
`klasseId` are joined with `leftJoin` (see `HelloDataDossierRepo`): a Kind that only has a Schule
(but no Klasse), or neither, must still keep its Dossier in the export.

---

## The `HelloDataEinladungJson` contract

This DTO is a **contract** towards HelloData, not a mirror of internal entities. Fields must
not be added or renamed casually, because the statistic models at HelloData build on them.

Exports both regular invitations (`Einladung`) and anonymous participations (`AnonymerEintrag`).

| Field                  | Type              | Required | Notes                                                                                                                              |
|------------------------|-------------------|----------|------------------------------------------------------------------------------------------------------------------------------------|
| `id`                   | UUID              | yes      | Production UUID (`Einladung` or `AnonymerEintrag`), stable across loads — the join key                                             |
| `anlassId`             | UUID              | yes      | Production UUID of the `Anlass`                                                                                                    |
| `dossierId`            | UUID              | no       | Production UUID of the `Kinddossier`; absent/null for anonymous entries                                                            |
| `untersuchungId`       | UUID              | no       | Production UUID of the `Untersuchung` if carried out                                                                               |
| `einladungStatus`      | `EinladungStatus` | no       | Status of the Einladung (`TEILNAHME_BESTAETIGT`, `ABGEMELDET`, `NICHT_ERSCHIENEN`, etc.); absent/null for anonymous entries         |
| `grundAbmeldung`       | `GrundAbmeldung`  | no       | Reason for cancellation if cancelled                                                                                               |
| `artAbmeldung`         | `ArtAbmeldung`    | no       | Method/channel of cancellation if cancelled                                                                                        |
| `impfausweisVorhanden` | boolean           | yes      | Whether an Impfausweis file was uploaded (`Notiz` with typ `IMPFAUSWEIS` and at least one file)                                   |
| `fragebogenVorhanden`  | boolean           | yes      | Whether a Fragebogen file was uploaded (`Notiz` with typ `FRAGEBOGEN` and at least one file)                                       |
| `hash`                 | string            | no       | Optional for anonymous children, replaces the Kinderdossier. SHA256 hash calculated from Nachname, Vorname, Geburtsdatum, Geschlecht |

**Deliberately not exported** (data protection):

- `bemerkungAbmeldung` — free text, may contain personal data
- `notizen` — free text and personal data; only the two derived booleans `impfausweisVorhanden` / `fragebogenVorhanden` leave the system
- `spssExported`, `naechsteStation`, `naechsteStationSeit` — internal workflow state, not an analytical dimension

---

## The `HelloDataUntersuchungenJson` contract

This DTO is a **contract towards HelloData**, not a mirror of the `Untersuchung` entity (and its
~15 related sub-entities). Fields must not be added or renamed casually, because the statistic
models at HelloData build on them.

`Untersuchung` itself has no tenant column (the tenant comes with the database), so the export
returns every `Untersuchung`. The BE-only restriction applies via
`HelloDataExportService.requireAllowedTenant(...)`. Every sub-object below is `leftJoin`ed and
therefore `null` as a whole when that part of the Untersuchung was never filled in; this is why
only `id` on the root is required. Given the size of the contract (~16 sub-objects, ~90 leaf
fields), this single table groups the leaf fields under their sub-object instead of one table
per sub-entity — the "Sub-object" column plus the dotted field name (e.g. `sehen.brilleLinsen`)
tells you exactly where a field lives in `HelloDataUntersuchungenJson`.

Fields ending in `NichtGeprueftGrund` are the reason a checkbox-style field was left empty
(`NichtGeprueftGrund`: e.g. `NICHT_GEPRUEFT`, `NICHT_ANWENDBAR`) and are folded into the same row
as their companion field below to keep the table readable.

| Sub-object          | Field                                                                     | Type                                                          | Required | Notes                                                                 |
|----------------------|---------------------------------------------------------------------------|----------------------------------------------------------------|----------|------------------------------------------------------------------------|
| —                    | `id`                                                                       | UUID                                                            | yes      | Production UUID (`Untersuchung`), stable across loads — the join key   |
| —                    | `kindDaten`, `durchfuehrung`, `vorabklaerungen`, `triage`, `impfungen`, `sehen`, `hoeren`, `groesseGewicht`, `arzt`, `sprache`, `bewegungsapparat`, `beeintraechtigung`, `laeusekontrolle`, `massnahmen`, `abschluss` | object | no | Each is `null` as a whole when that part of the Untersuchung was never filled in |
| `kindDaten`          | `id`                                                                       | UUID                                                            | yes      | `HelloDataKindDatenJson`'s own id                                       |
| `kindDaten`          | `klassenstufe`                                                             | `Klassenstufe`                                                  | no       |                                                                        |
| `kindDaten`          | `klassentyp`, `klasse`, `schulhaus`, `schulstandort`, `quartier`, `schulkreis`, `schulhausnummer` | string | no | Snapshot of the Schule/Klasse data at the time of the Untersuchung    |
| `kindDaten`          | `nationalitaet`                                                            | `Land`                                                          | no       | ISO country code enum                                                  |
| `kindDaten`          | `geschlecht`                                                               | `Geschlecht`                                                    | no       |                                                                        |
| `durchfuehrung`      | `id`                                                                       | UUID                                                            | yes      |                                                                        |
| `durchfuehrung`      | `nachkontrolle`, `nachzuegler`, `neueintritt`                              | boolean                                                         | yes      |                                                                        |
| `vorabklaerungen`    | `id`                                                                       | UUID                                                            | yes      |                                                                        |
| `vorabklaerungen`    | `fragebogenStatus` (+ `fragebogenStatusNichtGeprueftGrund`)                | `FragebogenStatus` (+ `NichtGeprueftGrund`)                     | no       |                                                                        |
| `vorabklaerungen`    | `einverstaendnisImpfungen` (+ `...NichtGeprueftGrund`)                     | `EinverstaendnisImpfungen` (+ `NichtGeprueftGrund`)             | no       |                                                                        |
| `vorabklaerungen`    | `chronischeKrankheiten` (+ `...NichtGeprueftGrund`)                        | `ChronischeKrankheiten` (+ `NichtGeprueftGrund`)                | no       |                                                                        |
| `triage`             | `id`                                                                       | UUID                                                            | yes      |                                                                        |
| `triage`             | `triageSu`, `triageMpa`, `befundSu`, `wunschSchueler`, `vorinformationLehrperson` | boolean                                                  | yes      |                                                                        |
| `impfungen`          | `id`                                                                       | UUID                                                            | yes      |                                                                        |
| `impfungen`          | `impfausweisUnvollstaendig`, `impfausweisNichtVorhanden`, `impfausweisZweiPlusEinsSchema`, `checkEinwilligungVorhanden`, `checkKontraindikationenAbgeklaert`, `checkDoppelkontrolleImpfentscheidDurchgefuehrt` | boolean | yes |                                                        |
| `impfungen`          | `massnahmen`                                                               | object (`HelloDataImpfungenMassnahmenJson`)                     | no       | See `impfungen.massnahmen.*` below                                     |
| `impfungen`          | `nurUnvollstaendige`                                                       | object (`HelloDataImpfungenNurUnvollstaendigeJson`)             | no       | See `impfungen.nurUnvollstaendige.*` below                             |
| `impfungen.massnahmen` | `id`                                                                     | UUID                                                            | yes      |                                                                        |
| `impfungen.massnahmen` | `keineMassnahmen`, `vormerkenFuerGruppenFolgetermin`, `impfberatungMpaDurchgefuehrt`, `infomaterialAbgegeben`, `informationHausarzt`, `rueckfrageHausarzt` | boolean | yes |                                                                |
| `impfungen.massnahmen` | `informationEltern`                                                      | `InformationEltern`                                             | no       |                                                                        |
| `impfungen.massnahmen` | `empfehlung`                                                             | set of `ImpfungenMassnahmenEmpfehlung`                          | no       |                                                                        |
| `impfungen.nurUnvollstaendige` | `id`                                                              | UUID                                                            | yes      |                                                                        |
| `impfungen.nurUnvollstaendige` | `impfausweisVollstaendig`                                         | `ImpfausweisVollstaendig`                                       | no       |                                                                        |
| `impfungen.nurUnvollstaendige` | `impfausweisGrundUnvollstaendig`                                  | `ImpfausweisGrundUnvollstaendig`                                | no       |                                                                        |
| `sehen`              | `id`                                                                       | UUID                                                            | yes      |                                                                        |
| `sehen`              | `brilleLinsen`, `stereotest`, `farbsehen`, `verwendeteTafel`, `motilitaet`, `strabismusAuspraegung`, `strabismusAuftreten`, `inspektion`, `konvergenz` (each + its own `...NichtGeprueftGrund`) | corresponding `*Resultat` enum (+ `NichtGeprueftGrund`) | no | Single-value results |
| `sehen`              | `fernvisus`, `nahvisus`, `strabismus`, `empfehlung` (each + its own `...NichtGeprueftGrund`) | set of `DistanzSehenResultat` / `DistanzSehenResultat` / `StrabismusResultat` / `SehenEmpfehlung` (+ `NichtGeprueftGrund`) | no | Multi-value results |
| `sehen`              | `fernvisusRechts`, `fernvisusLinks`, `nahvisusRechts`, `nahvisusLinks`     | BigDecimal                                                      | no       | Measured visus values                                                  |
| `sehen`              | `fernvisusAugeLinks`, `fernvisusAugeRechts` (each + its own `...NichtGeprueftGrund`) | `DistanzSehenAugeEinzelnResultat` (+ `NichtGeprueftGrund`) | no |                                                                        |
| `hoeren`             | `audiogrammId`, `otoskopieId`                                              | UUID                                                            | no       | `null` when that part (Audiogramm / Otoskopie) was not performed       |
| `hoeren`             | `audiogrammSimpel`, `hoerhilfe` (each + its own `...NichtGeprueftGrund`)   | `AudiogrammSimpel` / `HoerenHoerhilfe` (+ `NichtGeprueftGrund`) | no       |                                                                        |
| `hoeren`             | `audiogrammBeurteilung`, `zusaetzlicheAngaben`, `empfehlung`, `otoskopieRechts`, `otoskopieLinks` (each + its own `...NichtGeprueftGrund`) | set of `AudiogrammBeurteilung` / `HoerenZusaetzlicheAngaben` / `HoerenEmpfehlung` / `OtoskopieResultat` (+ `NichtGeprueftGrund`) | no | |
| `hoeren`             | `audiogrammRechts`, `audiogrammLinks`                                      | object (`HelloDataAudiogrammJson`)                              | no       | See `hoeren.audiogrammRechts/Links.*` below                            |
| `hoeren.audiogrammRechts` / `hoeren.audiogrammLinks` | `id`                                       | UUID                                                            | yes      |                                                                        |
| `hoeren.audiogrammRechts` / `hoeren.audiogrammLinks` | `dbHz250`, `dbHz500`, `dbHz1000`, `dbHz2000`, `dbHz4000`, `dbHz6000`, `dbHz8000` | `Dezibel`                            | no       | Measured decibels per frequency                                        |
| `groesseGewicht`     | `id`                                                                       | UUID                                                            | yes      |                                                                        |
| `groesseGewicht`     | `groesse`                                                                  | Integer                                                         | no       |                                                                        |
| `groesseGewicht`     | `gewicht`, `bmi`                                                           | BigDecimal                                                      | no       |                                                                        |
| `groesseGewicht`     | `groessePerzentileSingleValue`, `gewichtPerzentileSingleValue`, `bmiPerzentileSingleValue` (each + its own `...NichtGeprueftGrund`) | corresponding `Perzentile*` enum (+ `NichtGeprueftGrund`) | no | |
| `groesseGewicht`     | `groessePerzentileMultiValue`, `gewichtPerzentileMultiValue`, `bmiPerzentileMultiValue` (each + its own `...NichtGeprueftGrund`) | set of corresponding `Perzentile*` enum (+ `NichtGeprueftGrund`) | no | |
| `arzt`               | `id`                                                                       | UUID                                                            | yes      |                                                                        |
| `arzt`               | `einverstaendniserklaerung`, `mundAndZaehne`, `sprachfaehigkeitMultiValue`, `bewegungsapparat`, `fingeropposition` | set of the corresponding enum (`Einverstaendniserklaerung`, `MundAndZaehne`, `Sprachfaehigkeit`, `Bewegungsapparat`, `Fingeropposition`) | no | |
| `arzt`               | `verhaltensfragebogen`, `verhalten`, `blutdruck`, `herzauskultation`, `haut`, `sprachfaehigkeitSingleValue`, `motorik`, `kognition`, `einbeinstand`, `einbeinstandRechts`, `einbeinstandLinks`, `einbeinhuepfen`, `einbeinhuepfenRechts`, `einbeinhuepfenLinks`, `hampelmann`, `ballFangen`, `ballWerfen`, `nameSchreiben`, `figurNachmalen`, `stifthaltung`, `aussprache`, `lesefit`, `zahlenNachsprechen4`, `zahlenNachsprechen5`, `zeichnenKreis`, `zeichnenViereck`, `zeichnenDreieck`, `zeichnenMensch` | corresponding single-value enum, one type per field name | no | e.g. `blutdruck` is `Blutdruck`, `motorik` is `Motorik`, `zeichnenKreis` is `Zeichnen`, `zeichnenMensch` is `ZeichnenMensch`, etc. |
| `arzt`               | `blutdruckSystolisch`, `blutdruckDiastolisch`                              | Integer                                                         | no       |                                                                        |
| `arzt`               | `sprachfaehigkeitPunkte`, `motorikPunkte`, `kognitionPunkte`, `lesefitPunkte`, `zeichnenMenschPunkte` | BigDecimal                             | no       | Score/point values for the corresponding assessment                    |
| `sprache`            | `id`                                                                       | UUID                                                            | yes      |                                                                        |
| `sprache`            | `nachsprechen`, `wortschatz`, `sprachverstaendnis` (each + its own `...NichtGeprueftGrund`) | `Nachsprechen` / `Wortschatz` / `Sprachverstaendnis` (+ `NichtGeprueftGrund`) | no | |
| `bewegungsapparat`   | `id`                                                                       | UUID                                                            | yes      |                                                                        |
| `bewegungsapparat`   | `skoliose`, `haltungsschwaeche`, `verkuerzungHamstringmuskulatur` (each + its own `...NichtGeprueftGrund`) | `Skoliose` / `Haltungsschwaeche` / `VerkuerzungHamstringmuskulatur` (+ `NichtGeprueftGrund`) | no | |
| `beeintraechtigung`  | `id`                                                                       | UUID                                                            | yes      | `HelloDataSchulrelevanteBeeintraechtigungJson`'s own id                |
| `beeintraechtigung`  | `freiesGehen`, `ohneStuetzradVelofahren`, `grafomotorik`, `feinmotorik`, `grobmotorikEinbeinhuepfen`, `gleichgewichtEinbeinstand` (each + its own `...NichtGeprueftGrund`) | corresponding enum (+ `NichtGeprueftGrund`) | no | |
| `beeintraechtigung`  | `anzahlMonateFreiesGehen`                                                  | Integer                                                         | no       |                                                                        |
| `laeusekontrolle`    | `id`                                                                       | UUID                                                            | yes      |                                                                        |
| `laeusekontrolle`    | `beurteilung`                                                              | set of `LaeusekontrolleBeurteilung`                             | no       |                                                                        |
| `massnahmen`         | `id`                                                                       | UUID                                                            | yes      |                                                                        |
| `massnahmen`         | `keineMassnahmen`, `beratungMPA`, `beratungSchulAerztin`, `anderes`, `bereitsBehandeltSehen`, `bereitsBehandeltHoeren`, `bereitsBehandeltGroesseGewicht`, `bereitsBehandeltSprache`, `bereitsBehandeltMotorik`, `bereitsBehandeltAnderes` | boolean | yes | |
| `abschluss`          | `id`                                                                       | UUID                                                            | yes      |                                                                        |
| `abschluss`          | `ueberweisungErfolgt`, `aerztlicheValidierungErforderlich`, `beratungDurchgefuehrt` (each + its own `...NichtGeprueftGrund`) | `AbschlussResultat` (+ `NichtGeprueftGrund`) | no |                                                    |

**Deliberately not exported** (data protection):

- Names, addresses, and other identifying Kind/Kontaktperson data — only the anonymized
  `kindDaten` snapshot (Klasse/Schule/Nationalität/Geschlecht) leaves the system
- Free-text `bemerkungen` fields across the sub-entities — may contain personal data

---

## Architecture

```
HelloDataResource        REST layer: builds a StreamingOutput and returns immediately
        │
        ▼
HelloDataExportService   Business layer: one place that knows how an export is written
        │                (transaction check, tenant check, sentinel, flush cadence, audit, abort)
        ├── HelloDataSchuleRepo        Data layer: cursor-backed streamAll()
        ├── HelloDataImpfstoffRepo     Data layer: cursor-backed streamAll()
        ├── HelloDataLabelRepo         Data layer: cursor-backed streamAll()
        ├── HelloDataBenutzerRepo      Data layer: cursor-backed streamAll()
        ├── HelloDataNotizRepo         Data layer: cursor-backed streamAll()
        ├── HelloDataAufgabeRepo       Data layer: cursor-backed streamAll()
        ├── HelloDataKlasseRepo        Data layer: cursor-backed streamAll()
        ├── HelloDataImpfungInternRepo Data layer: cursor-backed streamAll()
        ├── HelloDataImpfstatusRepo    Data layer: cursor-backed streamAll()
        ├── HelloDataAnlassRepo        Data layer: cursor-backed streamAll()
        ├── HelloDataDossierRepo       Data layer: cursor-backed streamAll()
        ├── HelloDataEinladungRepo     Data layer: cursor-backed streamAll()
        ├── HelloDataUntersuchungenRepo Data layer: cursor-backed streamAll(), maps via HelloDataUntersuchungenMapper
        └── HelloDataExportWriter Serialization: the framing, and nothing else
              ├── NdjsonWriter        one JSON object per line + sentinel line
              └── JsonEnvelopeWriter  {"data":[ ... ],"_meta":{ ... }}
```

Only the **framing** differs between the two formats. The cursor, the transaction, the tenant
check, the flush cadence, the sentinel content and the audit trail are shared, so the variants
cannot drift apart in behaviour. The sentinel in particular is built in the service and handed
to the writer, rather than built by each writer.

### Why `StreamingOutput` and not reactive `Multi`

Items of a `Multi` are emitted on the IO/event-loop thread, where blocking JDBC is forbidden
(`BlockingOperationNotAllowedException`). `StreamingOutput.write(...)` instead runs on a worker
thread (`@Blocking`) with an active CDI request context — so the tenant is still resolved and
blocking JDBC is allowed.

### Why the transaction lives in the service, not the resource

A JAX-RS response has two phases: first the resource method runs (the `@Transactional`
interceptor commits when it returns), and only **afterwards** does the `MessageBodyWriter`
write the body. A cursor opened in phase 1 and read in phase 2 would run **without** a
transaction — and PostgreSQL then buffers the whole result set in the heap, silently killing
the streaming.

That is why the service methods are called from inside `StreamingOutput.write(...)`: there the
transaction spans the entire cursor loop. `HelloDataExportService.requireActiveTransaction(...)`
asserts this invariant and turns a silent degradation into a loud failure.

### Transaction timeout

Because the transaction spans the **whole** streamed response, its timeout has to cover not just
the query but also how fast HelloData reads the stream. Narayana's default is **60s**, which a
larger table or a slow consumer would blow through — the reaper would then roll the transaction
back mid-stream and the client would get a truncated response with no `_meta` sentinel.

`writeSchulen` therefore carries an explicit `@TransactionConfiguration` with a **10-minute
(600s)** timeout, overridable at runtime via `esc.hellodata-export.transaction-timeout-seconds`
(a plain integer, in seconds). This is deliberately scoped to the export method rather than
raising the global `quarkus.transaction-manager.default-transaction-timeout`, so the safety net
stays tight for every other transaction in the app.

### Server-side cursor (streaming, not buffering)

`HelloDataSchuleRepo.streamAll()` sets `HINT_FETCH_SIZE = 500`. For PostgreSQL to actually use
a server-side cursor, three conditions must hold together:

1. `autoCommit = false` (i.e. inside a transaction — see above)
2. `fetchSize > 0`
3. a forward-only result set (the default)

The row loop in `HelloDataExportService` flushes every **100 rows** (the Quarkus REST output
buffer is ~8 KB), so rows reach the client while the export is still running instead of one TCP
write per row. The cadence lives in the loop rather than in the writers, so both formats are
guaranteed to stream identically — a writer that forgot to flush would look correct in every
test and only show up as "the whole file arrives at once" in production.

Results are ordered by `schule.id ASC`: this makes the export reproducible and leaves room to
add keyset pagination (`?after=<id>`) later without a contract change. The ordering costs a
server-side sort (time-to-first-row grows with the table), but heap usage stays bounded.

---

## Security

Access is controlled by **three independent gates**:

1. **`ClientFilter`** — the `hellodata` service client may only reach `/hellodata` paths, and
   `/hellodata` paths may only be reached by the `hellodata` client (matched on the token's
   `azp` and `preferred_username`).
2. **`@RolesAllowed(HELLODATA_EXPORT_LESEN)`** — `RolesAugmentor` grants this fixed Berechtigung
   to the machine client from `(azp, preferred_username)` alone. Service accounts have no
   `Benutzer` record and therefore no `FachRolle`, exactly like `ESCADA_PUSH` and `PUPIL_PUSH`.
3. **Tenant restriction to BE** — the Berechtigung does **not** carry a tenant (the tenant is
   resolved from the request hostname). Because this endpoint deliberately bypasses the
   object-scoped authorization, `HelloDataExportService.requireAllowedTenant(...)` explicitly
   refuses any tenant other than `BE`.

As a compensating control for bypassing object-scoped auth, every completed export is **audited**
(`LOG.info("HelloData export '{}' completed: {} rows in {} ms", ...)`).

### Rate limiting / concurrency

- `@RateLimit(HELLODATA_EXPORT_LIMIT)` — kept in line with `DEFAULT_LIMIT`; it only guards
  against a client hammering the endpoint, not against long-running exports.
- `@Bulkhead(1)` on `writeSchulen` — the meaningful limit: exactly **one export at a time** per
  dataset. A rejected second concurrent export surfaces as HTTP `500` (not `429`); this is
  accepted, since a single nightly client should never hit it and both codes mean "retry".
  The bulkhead is **per method**, so `writeSchulen` and `writeSchulenAsJson` each get one slot
  and could run concurrently. That is two cursors instead of one — acceptable, because the two
  endpoints exist so HelloData can *pick* a format, not so they can be called in parallel.

---

## Configuration

The service account is identified via these ESC config properties:

| Property                            | Default                            |
|-------------------------------------|-------------------------------------|
| `esc.hellodata-export.client.user`  | `service-account-hello-data-client` |
| `esc.hellodata-export.client.azp`   | `hello-data-client`                |

The export transaction timeout is tunable (see [Transaction timeout](#transaction-timeout)):

| Property                                          | Default (seconds) |
|---------------------------------------------------|-------------------|
| `esc.hellodata-export.transaction-timeout-seconds`| `600` (10 min)    |

The Keycloak client uses the `client_credentials` grant in realm `be` and only needs the role
`HELLODATA_EXPORT_LESEN` (FachRolle `HELLODATA_SERVICE`). No human `Benutzer` gets this role.

---

## Trying it locally

A Bruno collection lives under `api/bruno/hellodata/`. Load it with **Open Collection** and
point Bruno at the `api/bruno/hellodata/` folder — do **not** use **Import Collection** on
`bruno.json`. `bruno.json` is only the collection marker/config; import expects a single-file
export (with inline `items`), so importing `bruno.json` fails with *"unsupported collection
format"*.

1. `01 Get token (client_credentials)` — fetches a token for the `esc-hellodata` client
   (set `clientSecret` in the Bruno environment `local-be`; it is not checked in).
2. `02a Export Schulen (NDJSON)` — calls the endpoint and asserts the `_meta` sentinel line.
3. `02b Export Schulen (JSON container)` — the same data as one JSON document; asserts the
   `data` array and that `_meta.rowCount` matches its length.
4. `03b Export Untersuchungen (JSON container)` — export Untersuchungen as one JSON document;
   asserts the `data` array and that `_meta.rowCount` matches its length.

   `02a` and `02b` share the `a`/`b` prefix because they are the same dataset in two formats —
   call one or the other, not both in sequence.
4. `03a`/`03b Export Impfstoffe`, `04a`/`04b Export Labels`, `05a`/`05b Export Benutzer`,
   `05a`/`05b Export Klassen`, `05a`/`05b Export Impfungen intern`, `06a`/`06b Export Notizen`,
   `06a`/`06b Export Impfstatus`, `07a`/`07b Export Aufgaben`, `08a`/`08b Export Anlaesse`
   and `09a`/`09b Export Dossiers` — the same two formats for the other datasets.

Because Bruno buffers the response, verify **streaming** with `curl` instead:

```bash
curl -N -w '\nstart=%{time_starttransfer}s total=%{time_total}s\n' \
  -H "Authorization: Bearer $TOKEN" \
  -H "Accept: application/x-ndjson" \
  https://local-esc-be.localhost:6443/api/hellodata/v1/ndjson/schulen
```

If `time_starttransfer` is well below `time_total`, the cursor is live. If both values are
nearly equal, pgjdbc buffered the whole result set — then the transaction boundary is wrong.

---

## Tests

- `HelloDataResourceITest` (`@QuarkusTest`), `HelloDataBenutzerResourceITest`,
  `HelloDataImpfungenInternResourceITest`, `HelloDataImpfstatusResourceITest`, etc. — end-to-end
  per-dataset coverage:
  - export matches exactly the rows of tenant BE (guards against tenant fallback to BS);
  - stream ends with a `_meta` line whose `rowCount` matches, and no other line is a sentinel;
  - each field carries the value of its own column (guards the by-position projection);
  - contact details and free text are never present in the output;
  - the response is streamed (chunked, no `Content-Length`) and not fully buffered;
  - two sequential exports both succeed (the bulkhead releases);
  - a non-BE tenant, a missing Berechtigung, and no authentication are all refused;
  - and for the JSON container variant: the body parses as one JSON document, carries the same
    rows as the NDJSON export, its `_meta.rowCount` matches the `data` array, it is streamed
    (chunked, no `Content-Length`), it matches the declared `HelloData<Entities>EnvelopeJson`
    field-for-field, and it enforces the same tenant and role restrictions.
- `HelloDataImpfstoffeResourceITest`, `HelloDataBenutzerResourceITest`,
  `HelloDataNotizenResourceITest`, `HelloDataAufgabenResourceITest`,
  `HelloDataKlassenResourceITest` and `HelloDataAnlaesseResourceITest` (`@QuarkusTest`) — the
  same coverage per dataset. The Notizen test additionally pins the aggregated columns:
  `labels` carries the comma-separated Label UUIDs (and an empty string without Labels), the
  Label join does not multiply the rows, and `hatFileanhang` flips to `true` once a file is
  attached. The Aufgaben test pins the same `labels` aggregation plus the optional foreign keys:
  `kindId` and `anlassId` are `null` when they are not set and an Aufgabe without them still
  appears in the export. The Anlaesse test additionally pins the optional columns: an Anlass
  without Arzt, MPA and Zeitraum stays in the export and carries `null` in those fields.
- `HelloDataDossiersResourceITest` (`@QuarkusTest`) — the same coverage for the Dossiers
  dataset. It additionally pins `geburtsjahr` (derived from `Kind.geburtsdatum`), `hashValue`
  (matches `Kind.hashValue`), and the optional `schuleId`/`klasseId` foreign keys: both are
  `null` when a Kind has neither, `schuleId` alone is set when only the Schule is known, and a
  Dossier survives in the export in every case. It also pins a row to exactly the fields of
  `HelloDataDossierJson`: the seed fills in every field that is deliberately not exported, so
  neither their keys nor their values may show up.
- `HelloDataUntersuchungenResourceITest` (`@QuarkusTest`) — the same coverage for the
  Untersuchungen dataset. Because tenant BE is dezentral, the seed builds a full
  Kind → Dossier → Anlass → Einladung → Untersuchung chain with a zustaendiger Arzt/MPA on the
  Anlass and Dossier (`CheckArztRequiredForDezentraleTenants`), then attaches the Untersuchung to
  the Einladung (`CheckUntersuchungEinladungOrAnonymerEintrag`) before asserting the row, the
  `_meta` sentinel, the NDJSON/JSON parity and the declared `HelloDataUntersuchungenEnvelopeJson`
  field-for-field, all via `JsonNode` assertions rather than DTO getters.
- `HelloDataOpenApiContractTest` — verifies the generated OpenAPI contract.

---

## Adding a new dataset

1. Add a repo with a `streamAll()` returning a cursor-backed stream (see `HelloDataSchuleRepo`).
   Extend `ReadOnlyRepo` (not `BaseRepo`): these export repos only project and stream rows, so
   they want the `Db` handle without `BaseRepo`'s entity-CRUD and single-entity typing.
2. Add **two** thin methods to `HelloDataExportService` — `write<Dataset>(OutputStream)` passing
   `ndjsonWriter` and `write<Dataset>AsJson(OutputStream)` passing `jsonEnvelopeWriter` — both
   delegating to the shared private `write(...)`. **Do not inline the loop** — sentinel, flush
   cadence, auditing and abort handling must stay in one place, otherwise an endpoint eventually
   stops emitting `_meta` without anyone noticing. Carry over the `@TransactionConfiguration`
   timeout too, otherwise the new export inherits Narayana's 60s default and is rolled back
   mid-stream on a larger table.
3. Add two `@GET` methods on `HelloDataResource`: `/<dataset>` returning
   `ndjson(service::write<Dataset>)` and `/json/<dataset>` returning
   `streamed(service::write<Dataset>AsJson, JsonEnvelopeWriter.MEDIA_TYPE)`.
4. Add a `HelloData<Dataset>Json` DTO as the wire contract (extend the NDJSON `oneOf`) and a
   `HelloData<Dataset>EnvelopeJson` for the JSON variant. The envelope DTO never produces a
   response — add a test comparing it against a real one, or it will drift.
5. Regenerate the contract: `./mvnw compile quarkus:build` (note the `compile` — `quarkus:build`
   alone silently reuses stale classes), then run `HelloDataOpenApiContractTest` twice: it
   rewrites `hellodata-openapi.yaml` and fails on the first run by design.
