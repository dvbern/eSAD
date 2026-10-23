/*
 *
 * Copyright (C) 2026 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.hemed.esc.api.features.hellodata;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.function.Supplier;
import java.util.stream.Stream;
import jakarta.transaction.Status;
import jakarta.transaction.SystemException;
import jakarta.transaction.TransactionManager;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataMetaJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataSentinelJson;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext;

import io.micrometer.core.annotation.Timed;
import io.quarkus.narayana.jta.runtime.TransactionConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.faulttolerance.Bulkhead;

/**
 * Writes the HelloData exports row by row into the response output stream.
 * <p>
 * <b>Important - why the transaction lives here and not on the resource:</b> a JAX-RS response
 * has two separate phases. First the resource method runs (the {@code @Transactional}
 * interceptor boundary sits exactly there and commits almost immediatley when the method returns), and only
 * afterwards does the MessageBodyWriter write the body. A cursor opened in phase 1 and read in
 * phase 2 therefore runs without a transaction - PostgreSQL then buffers the entire result set
 * in the heap and the streaming is silently gone.
 * <p>
 * That is why {@link HelloDataResource} calls these methods from inside
 * {@code StreamingOutput.write(...)}: there the CDI request context (and with it the tenant
 * resolution) is still active, a worker thread is running (blocking JDBC is allowed) and the
 * transaction opened here spans the whole cursor loop.
 * <p>
 * <b>Adding a dataset:</b> add a repo with a {@code streamAll()} returning a cursor-backed
 * stream, then a thin method here that delegates to {@link #write}. Do not inline the loop -
 * sentinel, flush cadence, auditing and abort handling have to stay in one place, otherwise one
 * endpoint eventually stops emitting {@code _meta} without anyone noticing. Carry over the
 * {@code @TransactionConfiguration} timeout too (see {@link #writeSchulen}), otherwise the new
 * export inherits Narayana's 60s default and gets rolled back mid-stream on a larger table.
 * <p>
 * <b>Formats:</b> every dataset is offered as NDJSON and as a JSON container object, because it
 * is not settled which one HelloData can ingest. The only difference is the framing, which is
 * what {@link HelloDataExportWriter} abstracts - the cursor, the transaction, the tenant check
 * and the audit trail are shared, so the two variants cannot drift apart in behaviour.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HelloDataExportService {

    private final HelloDataSchuleRepo helloDataSchuleRepo;
    private final HelloDataImpfstoffRepo helloDataImpfstoffRepo;
    private final HelloDataLabelRepo helloDataLabelRepo;
    private final HelloDataBenutzerRepo helloDataBenutzerRepo;
    private final HelloDataNotizRepo helloDataNotizRepo;
    private final HelloDataAufgabeRepo helloDataAufgabeRepo;
    private final HelloDataKlasseRepo helloDataKlasseRepo;
    private final HelloDataEinladungRepo helloDataEinladungRepo;
    private final HelloDataImpfstatusRepo helloDataImpfstatusRepo;
    private final HelloDataImpfungInternRepo helloDataImpfungInternRepo;
    private final HelloDataAnlassRepo helloDataAnlassRepo;
    private final HelloDataDossierRepo helloDataDossierRepo;
    private final HelloDataUntersuchungenRepo helloDataUntersuchungenRepo;
    private final NdjsonWriter ndjsonWriter;
    private final JsonEnvelopeWriter jsonEnvelopeWriter;
    private final TransactionManager transactionManager;
    private final TenantContext tenantContext;
    private final AppClock appClock;

    /**
     * HelloData exists for the Kanton Bern only. Adding another canton is a decision with a data
     * protection agreement behind it, not a configuration change - hence a constant.
     */
    private static final TenantEnum ONLY_ALLOWED_TENANT = TenantEnum.BE;

    /**
     * Config property that overrides the export transaction timeout (in seconds). If it is not
     * set, {@link #EXPORT_TRANSACTION_TIMEOUT_SECONDS} is used as the fallback.
     */
    static final String TRANSACTION_TIMEOUT_PROPERTY = "esc.hellodata-export.transaction-timeout-seconds";

    /**
     * Ten minutes. The whole cursor loop runs inside this single transaction (see the class
     * documentation), so the timeout has to cover the entire streamed response - not just the
     * initial query, but also how fast HelloData reads the stream and the network latency in
     * between. Without an explicit value, Narayana's 60s default reaper would roll the
     * transaction back mid-stream on any larger table or slow consumer, and the client would
     * receive a truncated stream with no {@code _meta} sentinel. 600s is deliberately generous
     * for a nightly full load; it is exposed as {@link #TRANSACTION_TIMEOUT_PROPERTY} so ops can
     * tune it without a rebuild.
     */
    static final int EXPORT_TRANSACTION_TIMEOUT_SECONDS = 600;

    /**
     * Flush after this many rows. The Quarkus REST output buffer defaults to ~8 KB; without an
     * explicit flush rows would sit in that buffer instead of reaching the client. Flushing per
     * row would mean one TCP write per database row, hence the compromise.
     * <p>
     * Lives here rather than in the writers so both formats are guaranteed to stream at the same
     * cadence - a format that forgot to flush would look like a working export in every test and
     * only show up as "the whole file arrives at once" in production.
     */
    private static final int FLUSH_EVERY_N_ROWS = 100;

    /**
     * The bulkhead is per method, so datasets do not block each other - a stuck Schulen export
     * still lets Klassen through. Within one dataset a second concurrent export is rejected,
     * which is all a client fetching once per night needs.
     * <p>
     * Bulkhead and timer belong here and not on the resource method: the resource only builds
     * the {@code StreamingOutput} and returns immediately, so a limit there would cap object
     * creation rather than the duration of the export.
     * <p>
     * <b>Known behaviour:</b> because this runs inside {@code StreamingOutput.write(...)}, a
     * rejected second export surfaces as HTTP 500 rather than 429. That is accepted: with a
     * single nightly client the case should not arise, both codes mean "retry" to a batch
     * client, and rate-limit rejections are 500 everywhere else in this application too.
     * Could be changed by adding an Exceptionmapper if needed.
     */
    @Bulkhead(1)
    @Timed("writeSchulenMS")
    @TransactionConfiguration(
        timeout = EXPORT_TRANSACTION_TIMEOUT_SECONDS,
        timeoutFromConfigProperty = TRANSACTION_TIMEOUT_PROPERTY)
    public void writeSchulen(@NonNull OutputStream os) {
        write("schulen", helloDataSchuleRepo::streamAll, ndjsonWriter, os);
    }

    /**
     * Same rows, same cursor, same guarantees as {@link #writeSchulen} - only the framing
     * differs (see {@link JsonEnvelopeWriter}).
     * <p>
     * It carries its own {@code @Bulkhead}, so one NDJSON and one JSON export of the same
     * dataset can run at the same time. That is two cursors rather than one, which is
     * acceptable: the two endpoints exist so HelloData can pick a format, not so they can be
     * called in parallel, and the nightly client uses exactly one of them.
     */
    @Bulkhead(1)
    @Timed("writeSchulenAsJsonMS")
    @TransactionConfiguration(
        timeout = EXPORT_TRANSACTION_TIMEOUT_SECONDS,
        timeoutFromConfigProperty = TRANSACTION_TIMEOUT_PROPERTY)
    public void writeSchulenAsJson(@NonNull OutputStream os) {
        write("schulen", helloDataSchuleRepo::streamAll, jsonEnvelopeWriter, os);
    }

    @Bulkhead(1)
    @Timed("writeUntersuchungenMS")
    @TransactionConfiguration(
        timeout = EXPORT_TRANSACTION_TIMEOUT_SECONDS,
        timeoutFromConfigProperty = TRANSACTION_TIMEOUT_PROPERTY)
    public void writeUntersuchungen(@NonNull OutputStream os) {
        write(
            "untersuchungen",
            helloDataUntersuchungenRepo::streamAll,
            ndjsonWriter,
            os
        );
    }

    @Bulkhead(1)
    @Timed("writeUntersuchungenAsJsonMS")
    @TransactionConfiguration(
        timeout = EXPORT_TRANSACTION_TIMEOUT_SECONDS,
        timeoutFromConfigProperty = TRANSACTION_TIMEOUT_PROPERTY)
    public void writeUntersuchungenAsJson(@NonNull OutputStream os) {
        write(
            "untersuchungen",
            helloDataUntersuchungenRepo::streamAll,
            jsonEnvelopeWriter,
            os
        );
    }

    /**
     * Same shape as {@link #writeSchulen}, only for the Impfstoffe dataset. See the class
     * documentation for why every dataset gets its own thin method delegating to {@link #write}.
     */
    @Bulkhead(1)
    @Timed("writeImpfstoffeMS")
    @TransactionConfiguration(
        timeout = EXPORT_TRANSACTION_TIMEOUT_SECONDS,
        timeoutFromConfigProperty = TRANSACTION_TIMEOUT_PROPERTY)
    public void writeImpfstoffe(@NonNull OutputStream os) {
        write("impfstoffe", helloDataImpfstoffRepo::streamAll, ndjsonWriter, os);
    }

    /**
     * Same rows, same cursor, same guarantees as {@link #writeImpfstoffe} - only the framing
     * differs (see {@link JsonEnvelopeWriter}).
     */
    @Bulkhead(1)
    @Timed("writeImpfstoffeAsJsonMS")
    @TransactionConfiguration(
        timeout = EXPORT_TRANSACTION_TIMEOUT_SECONDS,
        timeoutFromConfigProperty = TRANSACTION_TIMEOUT_PROPERTY)
    public void writeImpfstoffeAsJson(@NonNull OutputStream os) {
        write("impfstoffe", helloDataImpfstoffRepo::streamAll, jsonEnvelopeWriter, os);
    }

    /**
     * Same shape as {@link #writeSchulen}, only for the Labels dataset. See the class
     * documentation for why every dataset gets its own thin method delegating to {@link #write}.
     */
    @Bulkhead(1)
    @Timed("writeLabelsMS")
    @TransactionConfiguration(
        timeout = EXPORT_TRANSACTION_TIMEOUT_SECONDS,
        timeoutFromConfigProperty = TRANSACTION_TIMEOUT_PROPERTY)
    public void writeLabels(@NonNull OutputStream os) {
        write("labels", helloDataLabelRepo::streamAll, ndjsonWriter, os);
    }

    /**
     * Same rows, same cursor, same guarantees as {@link #writeLabels} - only the framing
     * differs (see {@link JsonEnvelopeWriter}).
     */
    @Bulkhead(1)
    @Timed("writeLabelsAsJsonMS")
    @TransactionConfiguration(
        timeout = EXPORT_TRANSACTION_TIMEOUT_SECONDS,
        timeoutFromConfigProperty = TRANSACTION_TIMEOUT_PROPERTY)
    public void writeLabelsAsJson(@NonNull OutputStream os) {
        write("labels", helloDataLabelRepo::streamAll, jsonEnvelopeWriter, os);
    }

    /**
     * Same shape as {@link #writeSchulen}, only for the Benutzer dataset. See the class
     * documentation for why every dataset gets its own thin method delegating to {@link #write}.
     */
    @Bulkhead(1)
    @Timed("writeBenutzerMS")
    @TransactionConfiguration(
        timeout = EXPORT_TRANSACTION_TIMEOUT_SECONDS,
        timeoutFromConfigProperty = TRANSACTION_TIMEOUT_PROPERTY)
    public void writeBenutzer(@NonNull OutputStream os) {
        write("benutzer", helloDataBenutzerRepo::streamAll, ndjsonWriter, os);
    }

    /**
     * Same rows, same cursor, same guarantees as {@link #writeBenutzer} - only the framing
     * differs (see {@link JsonEnvelopeWriter}).
     */
    @Bulkhead(1)
    @Timed("writeBenutzerAsJsonMS")
    @TransactionConfiguration(
        timeout = EXPORT_TRANSACTION_TIMEOUT_SECONDS,
        timeoutFromConfigProperty = TRANSACTION_TIMEOUT_PROPERTY)
    public void writeBenutzerAsJson(@NonNull OutputStream os) {
        write("benutzer", helloDataBenutzerRepo::streamAll, jsonEnvelopeWriter, os);
    }

    /**
     * Same shape as {@link #writeSchulen}, only for the Notizen dataset. See the class
     * documentation for why every dataset gets its own thin method delegating to {@link #write}.
     */
    @Bulkhead(1)
    @Timed("writeNotizenMS")
    @TransactionConfiguration(
        timeout = EXPORT_TRANSACTION_TIMEOUT_SECONDS,
        timeoutFromConfigProperty = TRANSACTION_TIMEOUT_PROPERTY)
    public void writeNotizen(@NonNull OutputStream os) {
        write("notizen", helloDataNotizRepo::streamAll, ndjsonWriter, os);
    }

    /**
     * Same rows, same cursor, same guarantees as {@link #writeNotizen} - only the framing
     * differs (see {@link JsonEnvelopeWriter}).
     */
    @Bulkhead(1)
    @Timed("writeNotizenAsJsonMS")
    @TransactionConfiguration(
        timeout = EXPORT_TRANSACTION_TIMEOUT_SECONDS,
        timeoutFromConfigProperty = TRANSACTION_TIMEOUT_PROPERTY)
    public void writeNotizenAsJson(@NonNull OutputStream os) {
        write("notizen", helloDataNotizRepo::streamAll, jsonEnvelopeWriter, os);
    }

    /**
     * Same shape as {@link #writeSchulen}, only for the Aufgaben dataset. See the class
     * documentation for why every dataset gets its own thin method delegating to {@link #write}.
     */
    @Bulkhead(1)
    @Timed("writeAufgabenMS")
    @TransactionConfiguration(
        timeout = EXPORT_TRANSACTION_TIMEOUT_SECONDS,
        timeoutFromConfigProperty = TRANSACTION_TIMEOUT_PROPERTY)
    public void writeAufgaben(@NonNull OutputStream os) {
        write("aufgaben", helloDataAufgabeRepo::streamAll, ndjsonWriter, os);
    }

    /**
     * Same rows, same cursor, same guarantees as {@link #writeAufgaben} - only the framing
     * differs (see {@link JsonEnvelopeWriter}).
     */
    @Bulkhead(1)
    @Timed("writeAufgabenAsJsonMS")
    @TransactionConfiguration(
        timeout = EXPORT_TRANSACTION_TIMEOUT_SECONDS,
        timeoutFromConfigProperty = TRANSACTION_TIMEOUT_PROPERTY)
    public void writeAufgabenAsJson(@NonNull OutputStream os) {
        write("aufgaben", helloDataAufgabeRepo::streamAll, jsonEnvelopeWriter, os);
    }

    /**
     * Same shape as {@link #writeSchulen}, only for the Klassen dataset. See the class
     * documentation for why every dataset gets its own thin method delegating to {@link #write}.
     */
    @Bulkhead(1)
    @Timed("writeKlassenMS")
    @TransactionConfiguration(
        timeout = EXPORT_TRANSACTION_TIMEOUT_SECONDS,
        timeoutFromConfigProperty = TRANSACTION_TIMEOUT_PROPERTY)
    public void writeKlassen(@NonNull OutputStream os) {
        write("klassen", helloDataKlasseRepo::streamAll, ndjsonWriter, os);
    }

    /**
     * Same rows, same cursor, same guarantees as {@link #writeKlassen} - only the framing
     * differs (see {@link JsonEnvelopeWriter}).
     */
    @Bulkhead(1)
    @Timed("writeKlassenAsJsonMS")
    @TransactionConfiguration(
        timeout = EXPORT_TRANSACTION_TIMEOUT_SECONDS,
        timeoutFromConfigProperty = TRANSACTION_TIMEOUT_PROPERTY)
    public void writeKlassenAsJson(@NonNull OutputStream os) {
        write("klassen", helloDataKlasseRepo::streamAll, jsonEnvelopeWriter, os);
    }

    /**
     * Same shape as {@link #writeSchulen}, only for the Einladungen dataset. See the class
     * documentation for why every dataset gets its own thin method delegating to {@link #write}.
     */
    @Bulkhead(1)
    @Timed("writeEinladungenMS")
    @TransactionConfiguration(
        timeout = EXPORT_TRANSACTION_TIMEOUT_SECONDS,
        timeoutFromConfigProperty = TRANSACTION_TIMEOUT_PROPERTY)
    public void writeEinladungen(@NonNull OutputStream os) {
        write("einladungen", helloDataEinladungRepo::streamAll, ndjsonWriter, os);
    }

    /**
     * Same rows, same cursor, same guarantees as {@link #writeEinladungen} - only the framing
     * differs (see {@link JsonEnvelopeWriter}).
     */
    @Bulkhead(1)
    @Timed("writeEinladungenAsJsonMS")
    @TransactionConfiguration(
        timeout = EXPORT_TRANSACTION_TIMEOUT_SECONDS,
        timeoutFromConfigProperty = TRANSACTION_TIMEOUT_PROPERTY)
    public void writeEinladungenAsJson(@NonNull OutputStream os) {
        write("einladungen", helloDataEinladungRepo::streamAll, jsonEnvelopeWriter, os);
    }

    @Bulkhead(1)
    @Timed("writeImpfstatusKrankheitenMS")
    @TransactionConfiguration(
        timeout = EXPORT_TRANSACTION_TIMEOUT_SECONDS,
        timeoutFromConfigProperty = TRANSACTION_TIMEOUT_PROPERTY)
    public void writeImpfstatusKrankheiten(@NonNull OutputStream os) {
        write("impfstatuskrankheiten", helloDataImpfstatusRepo::streamAll, ndjsonWriter, os);
    }

    @Bulkhead(1)
    @Timed("writeImpfstatusKrankheitenAsJsonMS")
    @TransactionConfiguration(
        timeout = EXPORT_TRANSACTION_TIMEOUT_SECONDS,
        timeoutFromConfigProperty = TRANSACTION_TIMEOUT_PROPERTY)
    public void writeImpfstatusKrankheitenAsJson(@NonNull OutputStream os) {
        write("impfstatuskrankheiten", helloDataImpfstatusRepo::streamAll, jsonEnvelopeWriter, os);
    }

    @Bulkhead(1)
    @Timed("writeImpfungenInternMS")
    @TransactionConfiguration(
        timeout = EXPORT_TRANSACTION_TIMEOUT_SECONDS,
        timeoutFromConfigProperty = TRANSACTION_TIMEOUT_PROPERTY)
    public void writeImpfungenIntern(@NonNull OutputStream os) {
        write("impfungenintern", helloDataImpfungInternRepo::streamAll, ndjsonWriter, os);
    }

    @Bulkhead(1)
    @Timed("writeImpfungenInternAsJsonMS")
    @TransactionConfiguration(
        timeout = EXPORT_TRANSACTION_TIMEOUT_SECONDS,
        timeoutFromConfigProperty = TRANSACTION_TIMEOUT_PROPERTY)
    public void writeImpfungenInternAsJson(@NonNull OutputStream os) {
        write("impfungenintern", helloDataImpfungInternRepo::streamAll, jsonEnvelopeWriter, os);
    }

    /**
     * Same shape as {@link #writeSchulen}, only for the Anlaesse dataset. See the class
     * documentation for why every dataset gets its own thin method delegating to {@link #write}.
     */
    @Bulkhead(1)
    @Timed("writeAnlaesseMS")
    @TransactionConfiguration(
        timeout = EXPORT_TRANSACTION_TIMEOUT_SECONDS,
        timeoutFromConfigProperty = TRANSACTION_TIMEOUT_PROPERTY)
    public void writeAnlaesse(@NonNull OutputStream os) {
        write("anlaesse", helloDataAnlassRepo::streamAll, ndjsonWriter, os);
    }

    /**
     * Same rows, same cursor, same guarantees as {@link #writeAnlaesse} - only the framing
     * differs (see {@link JsonEnvelopeWriter}).
     */
    @Bulkhead(1)
    @Timed("writeAnlaesseAsJsonMS")
    @TransactionConfiguration(
        timeout = EXPORT_TRANSACTION_TIMEOUT_SECONDS,
        timeoutFromConfigProperty = TRANSACTION_TIMEOUT_PROPERTY)
    public void writeAnlaesseAsJson(@NonNull OutputStream os) {
        write("anlaesse", helloDataAnlassRepo::streamAll, jsonEnvelopeWriter, os);
    }

    /**
     * Same shape as {@link #writeSchulen}, only for the Dossiers dataset. See the class
     * documentation for why every dataset gets its own thin method delegating to {@link #write}.
     */
    @Bulkhead(1)
    @Timed("writeDossiersMS")
    @TransactionConfiguration(
        timeout = EXPORT_TRANSACTION_TIMEOUT_SECONDS,
        timeoutFromConfigProperty = TRANSACTION_TIMEOUT_PROPERTY)
    public void writeDossiers(@NonNull OutputStream os) {
        write("dossiers", helloDataDossierRepo::streamAll, ndjsonWriter, os);
    }

    /**
     * Same rows, same cursor, same guarantees as {@link #writeDossiers} - only the framing
     * differs (see {@link JsonEnvelopeWriter}).
     */
    @Bulkhead(1)
    @Timed("writeDossiersAsJsonMS")
    @TransactionConfiguration(
        timeout = EXPORT_TRANSACTION_TIMEOUT_SECONDS,
        timeoutFromConfigProperty = TRANSACTION_TIMEOUT_PROPERTY)
    public void writeDossiersAsJson(@NonNull OutputStream os) {
        write("dossiers", helloDataDossierRepo::streamAll, jsonEnvelopeWriter, os);
    }

    /**
     * The single place that knows how a HelloData export is written: row by row, terminated by
     * the sentinel, audited, and with an abort that can no longer be reported as a status code.
     * <p>
     * Private and unannotated on purpose - it runs inside the transaction of the calling public
     * method, which is what keeps the database cursor alive to enable streaming.
     *
     * @param datasetName name used for auditing; matches the path segment of the endpoint
     * @param source opens the cursor - called inside the transaction, closed by this method
     * @param writer the framing; everything else is identical across formats
     */
    private void write(
        @NonNull String datasetName,
        @NonNull Supplier<Stream<?>> source,
        @NonNull HelloDataExportWriter writer,
        @NonNull OutputStream os
    ) {
        requireActiveTransaction(datasetName);
        requireAllowedTenant(datasetName);

        long rowCount = 0;
        long startNanos = System.nanoTime();

        try (Stream<?> rows = source.get()) {
            writer.writeStart(os);
            for (var iterator = rows.iterator(); iterator.hasNext(); ) {
                rowCount++;
                writer.writeRow(os, iterator.next(), rowCount);
                if (rowCount % FLUSH_EVERY_N_ROWS == 0) {
                    os.flush();
                }
            }
            writer.writeEnd(os, completionSentinel(rowCount));
            os.flush();

            LOG.info(
                "HelloData export '{}' ({}) completed: {} rows in {} ms",
                datasetName,
                writer.formatName(),
                rowCount,
                elapsedMs(startNanos));
        } catch (IOException e) {
            // The client disconnected or a proxy aborted. A status code can no longer be set
            // at this point - the missing sentinel is the signal to HelloData.
            String message = "HelloData export '%s' (%s) aborted after %d rows in %d ms"
                .formatted(datasetName, writer.formatName(), rowCount, elapsedMs(startNanos));
            LOG.warn(message);
            throw new UncheckedIOException(message, e);
        }
    }

    /**
     * Built here rather than in the writers so that both formats report the same thing - the
     * sentinel is what tells HelloData a load succeeded
     */
    @NonNull
    private HelloDataSentinelJson completionSentinel(long rowCount) {
        return new HelloDataSentinelJson(new HelloDataMetaJson(
            HelloDataMetaJson.STATUS_COMPLETE,
            rowCount,
            appClock.offsetDateTime().now()));
    }

    /**
     * This checks that the transaction is really active, otherwise the streaming from the
     * database would fail silently
     * <p>
     * PostgreSQL cursors are transaction-scoped, so pgjdbc only honours a fetch size while
     * {@code autoCommit} is false. Outside a transaction it
     * <a href="https://jdbc.postgresql.org/documentation/query/">"silently falls back to
     * fetching the whole ResultSet at once"</a> - no warning, no exception. The export would
     * still return correct data, in the correct order, with a correct sentinel, while holding
     * every row in the heap. No assertion on the response can tell the two apart.
     * <p>
     * So the invariant is checked rather than documented: move the query out of the streaming
     * callback and this fails immediately instead of quietly becoming a memory hog.
     * <p>
     * Note this is a necessary condition, not a sufficient one - it proves a transaction is
     * open, not that the driver actually opened a cursor.
     */
    private void requireActiveTransaction(@NonNull String dataset) {
        int status;
        try {
            status = transactionManager.getStatus();
        } catch (SystemException e) {
            throw new IllegalStateException(
                "Could not determine the transaction status for the HelloData export '%s'"
                    .formatted(dataset),
                e);
        }

        if (status != Status.STATUS_ACTIVE) {
            throw new IllegalStateException((
                "HelloData export '%s' must run inside an active transaction (status was %d). "
                    + "Without one PostgreSQL ignores the fetch size and buffers the whole "
                    + "result set, so the export would silently stop streaming.")
                .formatted(dataset, status));
        }
    }

    /**
     * The Berechtigung does not carry a tenant: {@code RolesAugmentor} grants the machine client
     * its fixed Berechtigung from (azp, preferred_username) alone, and the tenant comes from the
     * request hostname. Realm isolation makes a token from the BE realm useless against another
     * canton's hostname, but that is an operational control - if the client were ever
     * provisioned in another realm as well, nothing in the code would stop it exporting that
     * canton's data.
     * <p>
     * For an endpoint that deliberately bypasses the object-scoped authorization, "we did not
     * provision it that way" is too thin a guarantee, so the restriction is checked here.
     */
    private void requireAllowedTenant(@NonNull String dataset) {
        TenantEnum tenant = tenantContext.get();
        if (tenant != ONLY_ALLOWED_TENANT) {
            throw new IllegalStateException((
                "HelloData export '%s' is only available for tenant %s, but the request resolved "
                    + "to %s. Refusing to export another tenant's data.")
                .formatted(dataset, ONLY_ALLOWED_TENANT, tenant));
        }
    }

    private static long elapsedMs(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }
}
