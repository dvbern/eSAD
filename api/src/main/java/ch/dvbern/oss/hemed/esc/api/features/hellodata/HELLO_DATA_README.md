# HelloData Export

Machine-to-machine (M2M) interface that lets **HelloData** (the analytics platform of the
Kanton Bern) pull ESC master data once per night as a **full load**.

The first exported dataset is **Schulen**; the feature is deliberately built so that further
datasets (Kind, Anlass, ...) can be added with minimal ceremony.

- **Package:** `ch.dvbern.oss.hemed.esc.api.features.hellodata`
- **Base path:** `/api/hellodata/v1`
- **Tenant:** **Bern (BE) only**
- **Format:** NDJSON (newline-delimited JSON) **or** a JSON container object — both streamed
- **OpenAPI tag:** `HelloData` (schema: `api/openapi-schema/hellodata-openapi.yaml`)

---

## Endpoints

| Method | Path                               | Produces               | Role                     |
|--------|------------------------------------|------------------------|--------------------------|
| `GET`  | `/api/hellodata/v1/ndjson/schulen` | `application/x-ndjson` | `HELLODATA_EXPORT_LESEN` |
| `GET`  | `/api/hellodata/v1/json/schulen`   | `application/json`     | `HELLODATA_EXPORT_LESEN` |

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

## Architecture

```
HelloDataResource        REST layer: builds a StreamingOutput and returns immediately
        │
        ▼
HelloDataExportService   Business layer: one place that knows how an export is written
        │                (transaction check, tenant check, sentinel, flush cadence, audit, abort)
        ├── HelloDataSchuleRepo   Data layer: cursor-backed streamAll()
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

   `02a` and `02b` share the `a`/`b` prefix because they are the same dataset in two formats —
   call one or the other, not both in sequence.

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

- `HelloDataResourceITest` (`@QuarkusTest`) — end-to-end coverage:
  - export matches exactly the Schulen of tenant BE (guards against tenant fallback to BS);
  - stream ends with a `_meta` line whose `rowCount` matches, and no other line is a sentinel;
  - each field carries the value of its own column (guards the by-position projection);
  - contact details and free text are never present in the output;
  - the response is streamed (chunked, no `Content-Length`) and not fully buffered;
  - two sequential exports both succeed (the bulkhead releases);
  - a non-BE tenant, a missing Berechtigung, and no authentication are all refused;
  - and for the JSON container variant: the body parses as one JSON document, carries the same
    rows as the NDJSON export, its `_meta.rowCount` matches the `data` array, it is streamed
    (chunked, no `Content-Length`), it matches the declared `HelloDataSchulenEnvelopeJson`
    field-for-field, and it enforces the same tenant and role restrictions.
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
