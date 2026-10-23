# S3 files follow the database transaction

Files live in S3, the rows pointing to them live in Postgres. S3 cannot take part in the database transaction, so
until now the two could drift apart whenever a transaction rolled back after touching S3. With this change, every
upload and delete of a file that a row references is bound to the transaction that writes or removes the row:

| Operation | Transaction commits | Transaction rolls back |
|-----------|---------------------|------------------------|
| `FileStoreService.uploadToS3BoundToTransaction` | file stays | file is deleted again |
| `FileStoreService.deleteInS3AfterCommit` | file is deleted | file stays |

The mechanism is a CDI transactional observer (`FileStoreTransactionObserver`), the standard way in Quarkus to run code
after a transaction completed.

## The problem

`@JaxResource` makes the resource method the outermost transaction; services join it. Hibernate flushes and Narayana
commits in the `@Transactional` interceptor around the resource method, after all application code has returned. A
failure there (an optimistic locking conflict, for instance) rolls back the rows, but nothing undoes what already
happened in S3.

Two directions, both reproduced by tests before the fix:

- **Upload, then rollback: orphan in S3.** Two file ingress pushes for the same Einladung race: both upload their PDF,
  the second to commit fails on the `Einladung` version (`update Einladung ... where id=? and version=?`) and rolls
  back. Its PDF stayed in S3 with nothing pointing to it.
- **Delete, then rollback: row without file.** This is the worse one. `NotizService.removeUploadedFile`,
  `AufgabeService.removeUploadedFile`, `FileDataService.deleteFileData` and the Briefvorlage update deleted the S3 file
  inside the transaction. If anything failed afterwards, the row came back and its file was gone. Examples: updating
  a Briefvorlage with a stale version deleted the old template file although the Briefvorlage kept pointing to it;
  a Kind deletion that fails in a late step (Dossier, Kind) had already deleted all Notiz and Aufgabe files.

## How it works

```
 request thread                                      after completion (request thread, or reaper on timeout)
 ─────────────────────────────────────────────       ──────────────────────────────────────────────────────
 FileStoreService.uploadToS3BoundToTransaction
   require transaction ACTIVE
   capture client, bucket, tx id, MDC, origin
   fire UploadedInTransaction ─────────────────────▶ @Observes(during = AFTER_FAILURE)
   PUT to S3                                           delete the file, log with the captured context

 FileStoreService.deleteInS3AfterCommit
   require transaction ACTIVE
   capture ...
   fire DeletionAfterCommit ───────────────────────▶ @Observes(during = AFTER_SUCCESS)
                                                       delete the file, log with the captured context
```

ArC registers a JTA `Synchronization` when the event is fired inside a transaction and calls the observer from its
`afterCompletion`. Rules the code follows:

- **`@Transactional(MANDATORY)`** on both methods. `FileStoreService` is a `@Service`, so without it a caller that
  has no transaction would get one just for this call, which commits immediately and binds nothing.
- **Only an ACTIVE transaction.** ArC calls transactional observers right away when the transaction is not ACTIVE,
  for example when it is already marked rollback-only (`EventImpl.Notifier.notify` in ArC 3.33). A deferred delete
  would then delete a file whose row survives the rollback. `FileStoreService.requireActiveTransaction` rejects that
  case, and `deleteAfterCommit_inTransactionMarkedRollbackOnly_should_be_rejected_and_keep_file` pins it.
- **Everything is captured up front.** When a transaction times out, Narayana's reaper rolls it back on its own thread
  and the observer runs there, without request context, tenant or MDC. The `FileStoreTxBinding` therefore carries the S3 client
  and the prepared `DeleteObjectRequest` (with the tenant's bucket), and the observer calls no other bean and no
  database.
- **Dedicated deletion timeout.** S3 deletes wait at most `esc.s3.delete-timeout-seconds` (default 5s), separate from
  the 30s upload timeout. For the deletes after the transaction, this keeps S3 latency spikes or disruptions from
  starving Narayana's single reaper worker thread or the request threads. The Massenbrief temporary files are deleted
  outside any transaction, with the same timeout.
- **The event is fired before the PUT**, so an upload that fails half-way is cleaned up by the rollback too. Deleting a
  key that never arrived is a no-op in S3.
- Narayana reports heuristic outcomes (hazard, mixed) as `STATUS_COMMITTED` (`StatusConverter`), so an unclear commit
  never deletes an upload. It can carry out a deferred delete although the row survived; the health check reports
  that as a missing file.

### Alternatives considered

| Option | Why not |
|--------|---------|
| Nest the services differently: upload outside, database work in `QuarkusTransaction.requiringNew()`, delete on failure | The upload happens deep inside shared services (`NotizService`, `FileDataService`) that other transactional code calls. Every caller would have to be restructured, and the next caller inside a transaction brings the bug back. Does nothing for deletes. |
| Explicit `Transaction.registerSynchronization` | Works (the first prototype did this) and is what ArC does internally. The observer is the standard, declarative form of the same thing. |
| `@TransactionScoped` lifecycle events | Not told whether the transaction committed. |
| Transactional outbox (intent row plus worker) | The only variant that survives a JVM crash between upload and commit. More moving parts than the problem needs today, since the failure it leaves (an orphan) is harmless and reported by the health check. |

## Tracing an operation in the logs

Every binding gets an operation id. The binding line (request thread) and the outcome line (possibly the reaper
thread) share it, together with the Narayana transaction id, which is the same id the `ARJUNA` messages print
(shortened excerpt from a test run):

```
INFO  [FileStoreService] (executor-thread-1) ESC-S3-TX op=42ebeb9a tx=0:ffffc0a801f0:fb46:6ab1b609:1694
      file='briefvorlage-461d...docx' bucket=esc-bs origin=BriefvorlageService.saveBriefvorlageAttachmentInFilestorage:
      upload bound to the transaction, deleted again if it does not commit
INFO  [FileStoreTransactionObserver] (executor-thread-1) ESC-S3-TX op=42ebeb9a tx=0:ffffc0a801f0:fb46:6ab1b609:1694
      file='briefvorlage-461d...docx' bucket=esc-bs origin=...: transaction rolled back, file deleted in S3

WARN  [com.arj.ats.arjuna] (Transaction Reaper) ARJUNA012117: TransactionReaper::check processing TX
      0:ffffc0a801f0:fb46:6ab1b609:1f2cc1 in state RUN
INFO  [FileStoreTransactionObserver] (Transaction Reaper Worker 0) ESC-S3-TX op=2d429470
      tx=0:ffffc0a801f0:fb46:6ab1b609:1f2cc1 file='notizupload-...pdf' bucket=esc-be origin=...: transaction rolled back,
      file deleted in S3
```

- `origin` is the application method that asked for the upload or delete (the first frame outside `FileStoreService`,
  `FileDataService` and ArC's generated classes).
- The observer logs with the MDC of the request that bound the file, so `Principal`, `RequestSource`,
  `RequestSourceArgs` and `TenantId` in the production log format are those of the original request, also on the
  reaper thread.
- A cleanup that fails is logged at ERROR (`...could not be deleted in S3...`) with the same ids; the file store
  health check reports the file afterwards.

Search: `grep 'op=42ebeb9a'` for one file, `grep 'tx=0:ffffc0a801f0:fb46:6ab1b609:1694'` for everything a transaction
did in S3.

## Call sites

| Where | Before | Now |
|-------|--------|-----|
| `NotizService` (user uploads, file ingress) | upload, delete inside the transaction | bound upload, delete after commit |
| `AufgabeService` | upload, delete inside the transaction | bound upload, delete after commit |
| `BriefvorlageService` (create, replace template) | upload, delete inside the transaction | bound upload, delete after commit |
| `FileDataService.createFileData` (Archivierung, Duplikat merge) | upload of generated bytes | bound upload |
| `FileDataService.deleteFileData` (removal of Notiz, Aufgabe, Briefvorlage, DocumentQueue results, Kind deletion) | delete inside the transaction | delete after commit |
| `DocumentQueueService.storeResult` | upload inside `REQUIRES_NEW` | bound upload |
| `EscadaSyncEventService`, `PupilSyncEventService` (new) | upload without transaction, event row in the repository's own transaction | upload and event row in one transaction |
| `MassenbriefGeneratorService` | temporary files, deleted in `finally` with the upload timeout | still unbound on purpose, since no row points to them; deleted with the delete timeout |

Escada and Pupil: `EscadaService` and `PupilService` are `NOT_SUPPORTED`. The save method moved into a small `@Service`
each, because `TransactionsTest` forbids `@Transactional` on non-public methods and calling a transactional method of
the same class.

Massenbrief: its temporary files are the only remaining use of the unbound `FileStoreService.uploadToS3Async` and
`deleteInS3Async`. The ArchUnit rule `unbound_s3_methods_should_only_be_called_by_massenbrief_generator_service` in
`LayeringTest` fails if any other class calls them, so a new call site for a file that a row points to cannot skip
the binding unnoticed. The unused `FileUpload` overload of `uploadToS3Async` and
`deleteFileWithIdAsyncAndReturnResponseIgnoringErrors` were removed.

## Tests

All are `@QuarkusTest`s against the real transaction manager, Postgres and LocalStack; no mocks. `QuarkusTransaction`
plays the part of the `@JaxResource` boundary where a test needs to look inside the transaction or decide its outcome.

| Test | Covers |
|------|--------|
| `FileStoreServiceTransactionFileStoreTxBindingITest` | commit, exception, rollback-only without exception, no transaction, transaction already rollback-only (the ArC guard), nested `REQUIRES_NEW`, reaper timeout (runs on the reaper thread, logs with the request's MDC), shared op and tx id in both log lines, every tenant, tenant switched inside a transaction, two tenants concurrently |
| `FileStoreServiceSingleBucketITest` | the legacy single bucket mode, via `S3SingleBucketTestProfile` |
| `FileIngressTransactionITest` | the real race: two pushes, real `OptimisticLockException` at commit, the loser's file is gone, the winner's stays; silent rollback after a caught exception |
| `BriefvorlageResourceITest` (2 new) | REST update with a stale version: old template stays, new upload is deleted; the cleanup log line carries op id, origin and the request path |
| `NotizFileTransactionITest`, `AufgabeFileTransactionITest` | add, remove and remove-all (the Kind deletion path) with commit and rollback |
| `FileDataServiceITest` | generated files (byte upload) |

Test support: `FixtureService.uploadFileToNotiz/Aufgabe` and `readFilesOfNotiz/Aufgabe`, `S3ObjectProbe` (checks
buckets directly, bypassing the tenant-dependent production code), and `InMemoryLogHandler`, which now keeps the MDC of
the logging thread and returns formatted messages.

The race test runs the second push in a nested `requiringNew()` on the same thread. It must not run a query in the
outer transaction after the first push: the auto-flush would lock the `Einladung` row and the nested push would wait
for it forever.

## Not covered, known limitations

- **JVM crash between PUT and commit, or S3 down during cleanup.** No callback runs, or it fails and logs an ERROR.
  The result is an orphan, which `FileStoreHealthCheckService` reports (it does not delete). An outbox would close
  this gap.
- **Reaper timeout during a slow PUT.** The cleanup can run before the PUT lands; the late object stays as an orphan.
- **Race inside ArC.** ArC checks for ACTIVE and registers its synchronization a moment later. If the reaper aborts the
  transaction exactly in between, ArC notifies the observers immediately. Not reachable outside a transaction timeout.
- **Scheduler and tenants (separate ticket).** `ScheduledTaskService.runForActiveTenants` runs every tenant in one
  request context. The request scoped `FileStoreService` keeps the S3 client (and `S3RequestBuilder` the SSE-C key) of
  the first tenant it saw; only the bucket name is resolved per call. This predates this change and affects uploads,
  deletes, the health check and therefore also the cleanup, which uses the same client as the upload.
- Unrelated to this change: two `KinderDeactivationServiceTest` tests can fail in a full local run, with or without
  it, depending on test order (`KindAnlaesseResourceITest` leaves Kinder that match the unordered deactivation query).

## Suggested reading order

1. `shared/fileio/FileStoreTransactionObserver.java`: the observer, the events, the captured `FileStoreTxBinding`.
2. `shared/fileio/FileStoreService.java`: the two public methods, `requireActiveTransaction`, `bindToTransaction`.
3. The call sites in the table above; most are two-line changes.
4. `FileIngressTransactionITest` and `BriefvorlageResourceITest` for the scenarios, then
   `FileStoreServiceTransactionFileStoreTxBindingITest` for the edge cases.
