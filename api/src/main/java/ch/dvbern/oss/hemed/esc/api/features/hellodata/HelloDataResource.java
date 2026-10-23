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

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataAnlaesseEnvelopeJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataAnlassJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataAufgabeJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataAufgabenEnvelopeJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataBenutzerJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataBenutzereEnvelopeJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataDossierJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataDossiersEnvelopeJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataImpfstatusEnvelopeJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataImpfstatusKrankheitJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataImpfstoffJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataImpfstoffeEnvelopeJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataImpfungInternJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataImpfungenInternEnvelopeJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataLabelJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataLabelsEnvelopeJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataNotizJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataNotizenEnvelopeJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataKlasseJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataKlassenEnvelopeJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataEinladungJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataEinladungenEnvelopeJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataSchuleJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataSchulenEnvelopeJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataSentinelJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen.HelloDataUntersuchungenEnvelopeJson;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.untersuchungen.HelloDataUntersuchungenJson;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;

import io.micrometer.core.annotation.Timed;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.faulttolerance.api.RateLimit;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Machine-to-machine interface for HelloData (Kanton Bern). HelloData fetches the data once per
 * night as a full load, calling the endpoints one after another.
 * <p>
 * <b>Tenant BE only.</b> The tenant is resolved from the request hostname, as everywhere in
 * ESC. It is <b>not</b> implied by the Berechtigung: a service account is granted
 * {@code HELLODATA_EXPORT_LESEN} by the machine-client branch in {@code RolesAugmentor}
 * regardless of tenant, exactly like ESCADA_PUSH and PUPIL_PUSH, because service accounts have
 * no Benutzer record and therefore no FachRolle. The restriction to BE is enforced in
 * {@link HelloDataExportService}, which refuses to export for any other tenant.
 * <p>
 * <b>Two response formats, same data.</b> Every dataset is offered twice because it is not
 * settled whether HelloData's ingestion can read NDJSON:
 * <ul>
 *     <li>{@code /hellodata/v1/ndjson/<dataset>} - NDJSON, one JSON object per line. The
 *     recommended format: the consumer can parse it line by line and never has to hold the
 *     export in memory.</li>
 *     <li>{@code /hellodata/v1/json/<dataset>} - one ordinary JSON document with the rows in a
 *     {@code data} array. Needs nothing but a standard JSON parser, at the price that a
 *     consumer using {@code json.loads(...)} buffers the whole export.</li>
 * </ul>
 * Both are streamed by the server and both end with the same {@code _meta} sentinel. If the
 * sentinel is missing the response was truncated and the load must be treated as failed (once
 * HTTP 200 has been sent, no error can be reported via the status code any more).
 * <p>
 * <b>Why two paths and not one path with content negotiation:</b> an {@code Accept} header is
 * too easy to get wrong from the outside. A client sending {@code Accept: * / *} - which curl
 * and most generated clients do by default - would get whichever variant JAX-RS happens to
 * prefer, and the failure mode is a silently different response body rather than an error.
 * Separate paths make the choice explicit and keep the existing NDJSON endpoint provably
 * unchanged.
 * <p>
 * <b>Why {@code StreamingOutput} and not {@code Multi}:</b> items of a {@code Multi} are
 * emitted on the IO/event-loop thread, where blocking JDBC is forbidden
 * ({@code BlockingOperationNotAllowedException}). {@code StreamingOutput.write(...)} instead
 * runs on a worker thread with an active CDI request context (and therefore a correctly
 * resolved tenant). See {@link HelloDataExportService} for details.
 */
@Path("/hellodata/v1")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_HELLODATA)
@RolesAllowed({ BerechtigungName.HELLODATA_EXPORT_LESEN })
@RateLimit(RateLimitConst.HELLODATA_EXPORT_LIMIT)
@Timed("HelloDataResourceMS")
public class HelloDataResource {

    private static final String NDJSON_SCHULEN_RESPONSE_DESCRIPTION = """
        NDJSON (newline delimited JSON): one JSON object per line, separated by \\n, \
        with no enclosing array and no commas. The schema describes a single line.

        Every data line matches HelloDataSchuleJson. The final line is a \
        HelloDataSentinelJson: if it is missing, the response was truncated and the load \
        must be treated as failed - after HTTP 200 no error can be signalled by status code.""";

    private static final String NDJSON_UNTERSUCHUNGEN_RESPONSE_DESCRIPTION = """
        NDJSON (newline delimited JSON): one JSON object per line, separated by \\n, \
        with no enclosing array and no commas. The schema describes a single line.

        Every data line matches HelloDataUntersuchungenJson. The final line is a \
        HelloDataSentinelJson: if it is missing, the response was truncated and the load \
        must be treated as failed - after HTTP 200 no error can be signalled by status code.""";

    private static final String JSON_RESPONSE_DESCRIPTION = """
        A single JSON document: the rows in a `data` array, closed by a `_meta` object. \
        Same rows and same guarantees as the NDJSON variant, only framed differently.

        The response is streamed, so `_meta` is the last member of the document - `rowCount` \
        is not known before the last row has been read. JSON members are unordered, so read it \
        by key, not by position. If `_meta` is missing the response was truncated and the load \
        must be treated as failed - after HTTP 200 no error can be signalled by status code. \
        (A truncated response here is not valid JSON either, so a strict parser catches it too.)

        Note that consuming this with a whole-document parser buffers the entire export on the \
        client. Prefer the NDJSON variant if your tooling can read it.""";

    private static final String NDJSON_IMPFSTOFFE_RESPONSE_DESCRIPTION = """
        NDJSON (newline delimited JSON): one JSON object per line, separated by \\n, \
        with no enclosing array and no commas. The schema describes a single line.

        Every data line matches HelloDataImpfstoffJson. The final line is a \
        HelloDataSentinelJson: if it is missing, the response was truncated and the load \
        must be treated as failed - after HTTP 200 no error can be signalled by status code.""";

    private static final String NDJSON_LABELS_RESPONSE_DESCRIPTION = """
        NDJSON (newline delimited JSON): one JSON object per line, separated by \\n, \
        with no enclosing array and no commas. The schema describes a single line.

        Every data line matches HelloDataLabelJson. The final line is a \
        HelloDataSentinelJson: if it is missing, the response was truncated and the load \
        must be treated as failed - after HTTP 200 no error can be signalled by status code.""";

    private static final String NDJSON_BENUTZER_RESPONSE_DESCRIPTION = """
        NDJSON (newline delimited JSON): one JSON object per line, separated by \\n, \
        with no enclosing array and no commas. The schema describes a single line.

        Every data line matches HelloDataBenutzerJson. The final line is a \
        HelloDataSentinelJson: if it is missing, the response was truncated and the load \
        must be treated as failed - after HTTP 200 no error can be signalled by status code.""";

    private static final String NDJSON_NOTIZEN_RESPONSE_DESCRIPTION = """
        NDJSON (newline delimited JSON): one JSON object per line, separated by \\n, \
        with no enclosing array and no commas. The schema describes a single line.

        Every data line matches HelloDataNotizJson. The final line is a \
        HelloDataSentinelJson: if it is missing, the response was truncated and the load \
        must be treated as failed - after HTTP 200 no error can be signalled by status code.""";

    private static final String NDJSON_AUFGABEN_RESPONSE_DESCRIPTION = """
        NDJSON (newline delimited JSON): one JSON object per line, separated by \\n, \
        with no enclosing array and no commas. The schema describes a single line.

        Every data line matches HelloDataAufgabeJson. The final line is a \
        HelloDataSentinelJson: if it is missing, the response was truncated and the load \
        must be treated as failed - after HTTP 200 no error can be signalled by status code.""";

    private static final String NDJSON_KLASSEN_RESPONSE_DESCRIPTION = """
        NDJSON (newline delimited JSON): one JSON object per line, separated by \\n, \
        with no enclosing array and no commas. The schema describes a single line.

        Every data line matches HelloDataKlasseJson. The final line is a \
        HelloDataSentinelJson: if it is missing, the response was truncated and the load \
        must be treated as failed - after HTTP 200 no error can be signalled by status code.""";

    private static final String NDJSON_ANLAESSE_RESPONSE_DESCRIPTION = """
        NDJSON (newline delimited JSON): one JSON object per line, separated by \\n, \
        with no enclosing array and no commas. The schema describes a single line.

        Every data line matches HelloDataAnlassJson. The final line is a \
        HelloDataSentinelJson: if it is missing, the response was truncated and the load \
        must be treated as failed - after HTTP 200 no error can be signalled by status code.""";

    private static final String NDJSON_DOSSIERS_RESPONSE_DESCRIPTION = """
        NDJSON (newline delimited JSON): one JSON object per line, separated by \\n, \
        with no enclosing array and no commas. The schema describes a single line.

        Every data line matches HelloDataDossierJson. The final line is a \
        HelloDataSentinelJson: if it is missing, the response was truncated and the load \
        must be treated as failed - after HTTP 200 no error can be signalled by status code.""";

    private static final String NDJSON_EINLADUNGEN_RESPONSE_DESCRIPTION = """
        NDJSON (newline delimited JSON): one JSON object per line, separated by \\n, \
        with no enclosing array and no commas. The schema describes a single line.

        Every data line matches HelloDataEinladungJson. The final line is a \
        HelloDataSentinelJson: if it is missing, the response was truncated and the load \
        must be treated as failed - after HTTP 200 no error can be signalled by status code.""";

    private static final String NDJSON_IMPFSTATUSKRANKHEITEN_RESPONSE_DESCRIPTION = """
        NDJSON (newline delimited JSON): one JSON object per line, separated by \\n, \
        with no enclosing array and no commas. The schema describes a single line.

        Every data line matches HelloDataImpfstatusKrankheitJson. The final line is a \
        HelloDataSentinelJson: if it is missing, the response was truncated and the load \
        must be treated as failed - after HTTP 200 no error can be signalled by status code.""";

    private static final String NDJSON_IMPFUNGENINTERN_RESPONSE_DESCRIPTION = """
        NDJSON (newline delimited JSON): one JSON object per line, separated by \\n, \
        with no enclosing array and no commas. The schema describes a single line.

        Every data line matches HelloDataImpfungInternJson. The final line is a \
        HelloDataSentinelJson: if it is missing, the response was truncated and the load \
        must be treated as failed - after HTTP 200 no error can be signalled by status code.""";

    private final HelloDataExportService helloDataExportService;

    @GET
    @Path("/ndjson/schulen")
    @Produces(NdjsonWriter.MEDIA_TYPE)
    @Blocking
    @RateLimit(RateLimitConst.HELLODATA_EXPORT_LIMIT)
    @RolesAllowed({ BerechtigungName.HELLODATA_EXPORT_LESEN })
    @APIResponse(
        responseCode = "200",
        description = NDJSON_SCHULEN_RESPONSE_DESCRIPTION,
        content = @Content(
            mediaType = NdjsonWriter.MEDIA_TYPE,
            // Each line is either a data row or the closing sentinel. OpenAPI has no NDJSON
            // framing, so the schema describes a single line and the description the framing.
            schema = @Schema(
                oneOf = { HelloDataSchuleJson.class, HelloDataSentinelJson.class }))
    )
    public Response exportSchulen() {
        // helloDataExportService::writeSchulen has the right shape to act as a StreamingOutput
        // // Method reference on the CDI bean (not a lambda calling a private method):
        return ndjson(helloDataExportService::writeSchulen);
    }

    /**
     * The same export as {@link #exportSchulen()}, framed as one JSON document instead of
     * NDJSON. Kept as a separate path rather than an {@code Accept} variant - see the class
     * documentation for why.
     */
    @GET
    @Path("/json/schulen")
    @Produces(JsonEnvelopeWriter.MEDIA_TYPE)
    @Blocking
    @RateLimit(RateLimitConst.HELLODATA_EXPORT_LIMIT)
    @RolesAllowed({ BerechtigungName.HELLODATA_EXPORT_LESEN })
    @APIResponse(
        responseCode = "200",
        description = JSON_RESPONSE_DESCRIPTION,
        content = @Content(
            mediaType = JsonEnvelopeWriter.MEDIA_TYPE,
            schema = @Schema(implementation = HelloDataSchulenEnvelopeJson.class))
    )
    public Response exportSchulenAsJson() {
        return streamed(helloDataExportService::writeSchulenAsJson, JsonEnvelopeWriter.MEDIA_TYPE);
    }

    @GET
    @Path("/ndjson/einladungen")
    @Produces(NdjsonWriter.MEDIA_TYPE)
    @Blocking
    @RateLimit(RateLimitConst.HELLODATA_EXPORT_LIMIT)
    @RolesAllowed({ BerechtigungName.HELLODATA_EXPORT_LESEN })
    @APIResponse(
        responseCode = "200",
        description = NDJSON_EINLADUNGEN_RESPONSE_DESCRIPTION,
        content = @Content(
            mediaType = NdjsonWriter.MEDIA_TYPE,
            // Each line is either a data row or the closing sentinel. OpenAPI has no NDJSON
            // framing, so the schema describes a single line and the description the framing.
            schema = @Schema(
                oneOf = { HelloDataEinladungJson.class, HelloDataSentinelJson.class }))
    )
    public Response exportEinladungen() {
        return ndjson(helloDataExportService::writeEinladungen);
    }

    /**
     * The same export as {@link #exportEinladungen()}, framed as one JSON document instead of
     * NDJSON. Kept as a separate path rather than an {@code Accept} variant - see the class
     * documentation for why.
     */
    @GET
    @Path("/json/einladungen")
    @Produces(JsonEnvelopeWriter.MEDIA_TYPE)
    @Blocking
    @RateLimit(RateLimitConst.HELLODATA_EXPORT_LIMIT)
    @RolesAllowed({ BerechtigungName.HELLODATA_EXPORT_LESEN })
    @APIResponse(
        responseCode = "200",
        description = JSON_RESPONSE_DESCRIPTION,
        content = @Content(
            mediaType = JsonEnvelopeWriter.MEDIA_TYPE,
            schema = @Schema(implementation = HelloDataEinladungenEnvelopeJson.class))
    )
    public Response exportEinladungenAsJson() {
        return streamed(helloDataExportService::writeEinladungenAsJson, JsonEnvelopeWriter.MEDIA_TYPE);
    }

    @GET
    @Path("/ndjson/impfstoffe")
    @Produces(NdjsonWriter.MEDIA_TYPE)
    @Blocking
    @RateLimit(RateLimitConst.HELLODATA_EXPORT_LIMIT)
    @RolesAllowed({ BerechtigungName.HELLODATA_EXPORT_LESEN })
    @APIResponse(
        responseCode = "200",
        description = NDJSON_IMPFSTOFFE_RESPONSE_DESCRIPTION,
        content = @Content(
            mediaType = NdjsonWriter.MEDIA_TYPE,
            // Each line is either a data row or the closing sentinel. OpenAPI has no NDJSON
            // framing, so the schema describes a single line and the description the framing.
            schema = @Schema(
                oneOf = { HelloDataImpfstoffJson.class, HelloDataSentinelJson.class }))
    )
    public Response exportImpfstoffe() {
        return ndjson(helloDataExportService::writeImpfstoffe);
    }

    /**
     * The same export as {@link #exportImpfstoffe()}, framed as one JSON document instead of
     * NDJSON. Kept as a separate path rather than an {@code Accept} variant - see the class
     * documentation for why.
     */
    @GET
    @Path("/json/impfstoffe")
    @Produces(JsonEnvelopeWriter.MEDIA_TYPE)
    @Blocking
    @RateLimit(RateLimitConst.HELLODATA_EXPORT_LIMIT)
    @RolesAllowed({ BerechtigungName.HELLODATA_EXPORT_LESEN })
    @APIResponse(
        responseCode = "200",
        description = JSON_RESPONSE_DESCRIPTION,
        content = @Content(
            mediaType = JsonEnvelopeWriter.MEDIA_TYPE,
            schema = @Schema(implementation = HelloDataImpfstoffeEnvelopeJson.class))
    )
    public Response exportImpfstoffeAsJson() {
        return streamed(helloDataExportService::writeImpfstoffeAsJson, JsonEnvelopeWriter.MEDIA_TYPE);
    }

    @GET
    @Path("/ndjson/labels")
    @Produces(NdjsonWriter.MEDIA_TYPE)
    @Blocking
    @RateLimit(RateLimitConst.HELLODATA_EXPORT_LIMIT)
    @RolesAllowed({ BerechtigungName.HELLODATA_EXPORT_LESEN })
    @APIResponse(
        responseCode = "200",
        description = NDJSON_LABELS_RESPONSE_DESCRIPTION,
        content = @Content(
            mediaType = NdjsonWriter.MEDIA_TYPE,
            // Each line is either a data row or the closing sentinel. OpenAPI has no NDJSON
            // framing, so the schema describes a single line and the description the framing.
            schema = @Schema(
                oneOf = { HelloDataLabelJson.class, HelloDataSentinelJson.class }))
    )
    public Response exportLabels() {
        return ndjson(helloDataExportService::writeLabels);
    }

    /**
     * The same export as {@link #exportLabels()}, framed as one JSON document instead of
     * NDJSON. Kept as a separate path rather than an {@code Accept} variant - see the class
     * documentation for why.
     */
    @GET
    @Path("/json/labels")
    @Produces(JsonEnvelopeWriter.MEDIA_TYPE)
    @Blocking
    @RateLimit(RateLimitConst.HELLODATA_EXPORT_LIMIT)
    @RolesAllowed({ BerechtigungName.HELLODATA_EXPORT_LESEN })
    @APIResponse(
        responseCode = "200",
        description = JSON_RESPONSE_DESCRIPTION,
        content = @Content(
            mediaType = JsonEnvelopeWriter.MEDIA_TYPE,
            schema = @Schema(implementation = HelloDataLabelsEnvelopeJson.class))
    )
    public Response exportLabelsAsJson() {
        return streamed(helloDataExportService::writeLabelsAsJson, JsonEnvelopeWriter.MEDIA_TYPE);
    }

    @GET
    @Path("/ndjson/benutzer")
    @Produces(NdjsonWriter.MEDIA_TYPE)
    @Blocking
    @RateLimit(RateLimitConst.HELLODATA_EXPORT_LIMIT)
    @RolesAllowed({ BerechtigungName.HELLODATA_EXPORT_LESEN })
    @APIResponse(
        responseCode = "200",
        description = NDJSON_BENUTZER_RESPONSE_DESCRIPTION,
        content = @Content(
            mediaType = NdjsonWriter.MEDIA_TYPE,
            // Each line is either a data row or the closing sentinel. OpenAPI has no NDJSON
            // framing, so the schema describes a single line and the description the framing.
            schema = @Schema(
                oneOf = { HelloDataBenutzerJson.class, HelloDataSentinelJson.class }))
    )
    public Response exportBenutzer() {
        return ndjson(helloDataExportService::writeBenutzer);
    }

    /**
     * The same export as {@link #exportBenutzer()}, framed as one JSON document instead of
     * NDJSON. Kept as a separate path rather than an {@code Accept} variant - see the class
     * documentation for why.
     */
    @GET
    @Path("/json/benutzer")
    @Produces(JsonEnvelopeWriter.MEDIA_TYPE)
    @Blocking
    @RateLimit(RateLimitConst.HELLODATA_EXPORT_LIMIT)
    @RolesAllowed({ BerechtigungName.HELLODATA_EXPORT_LESEN })
    @APIResponse(
        responseCode = "200",
        description = JSON_RESPONSE_DESCRIPTION,
        content = @Content(
            mediaType = JsonEnvelopeWriter.MEDIA_TYPE,
            schema = @Schema(implementation = HelloDataBenutzereEnvelopeJson.class))
    )
    public Response exportBenutzerAsJson() {
        return streamed(helloDataExportService::writeBenutzerAsJson, JsonEnvelopeWriter.MEDIA_TYPE);
    }

    @GET
    @Path("/ndjson/notizen")
    @Produces(NdjsonWriter.MEDIA_TYPE)
    @Blocking
    @RateLimit(RateLimitConst.HELLODATA_EXPORT_LIMIT)
    @RolesAllowed({ BerechtigungName.HELLODATA_EXPORT_LESEN })
    @APIResponse(
        responseCode = "200",
        description = NDJSON_NOTIZEN_RESPONSE_DESCRIPTION,
        content = @Content(
            mediaType = NdjsonWriter.MEDIA_TYPE,
            // Each line is either a data row or the closing sentinel. OpenAPI has no NDJSON
            // framing, so the schema describes a single line and the description the framing.
            schema = @Schema(
                oneOf = { HelloDataNotizJson.class, HelloDataSentinelJson.class }))
    )
    public Response exportNotizen() {
        return ndjson(helloDataExportService::writeNotizen);
    }

    /**
     * The same export as {@link #exportNotizen()}, framed as one JSON document instead of
     * NDJSON. Kept as a separate path rather than an {@code Accept} variant - see the class
     * documentation for why.
     */
    @GET
    @Path("/json/notizen")
    @Produces(JsonEnvelopeWriter.MEDIA_TYPE)
    @Blocking
    @RateLimit(RateLimitConst.HELLODATA_EXPORT_LIMIT)
    @RolesAllowed({ BerechtigungName.HELLODATA_EXPORT_LESEN })
    @APIResponse(
        responseCode = "200",
        description = JSON_RESPONSE_DESCRIPTION,
        content = @Content(
            mediaType = JsonEnvelopeWriter.MEDIA_TYPE,
            schema = @Schema(implementation = HelloDataNotizenEnvelopeJson.class))
    )
    public Response exportNotizenAsJson() {
        return streamed(helloDataExportService::writeNotizenAsJson, JsonEnvelopeWriter.MEDIA_TYPE);
    }

    @GET
    @Path("/ndjson/aufgaben")
    @Produces(NdjsonWriter.MEDIA_TYPE)
    @Blocking
    @RateLimit(RateLimitConst.HELLODATA_EXPORT_LIMIT)
    @RolesAllowed({ BerechtigungName.HELLODATA_EXPORT_LESEN })
    @APIResponse(
        responseCode = "200",
        description = NDJSON_AUFGABEN_RESPONSE_DESCRIPTION,
        content = @Content(
            mediaType = NdjsonWriter.MEDIA_TYPE,
            // Each line is either a data row or the closing sentinel. OpenAPI has no NDJSON
            // framing, so the schema describes a single line and the description the framing.
            schema = @Schema(
                oneOf = { HelloDataAufgabeJson.class, HelloDataSentinelJson.class }))
    )
    public Response exportAufgaben() {
        return ndjson(helloDataExportService::writeAufgaben);
    }

    /**
     * The same export as {@link #exportAufgaben()}, framed as one JSON document instead of
     * NDJSON. Kept as a separate path rather than an {@code Accept} variant - see the class
     * documentation for why.
     */
    @GET
    @Path("/json/aufgaben")
    @Produces(JsonEnvelopeWriter.MEDIA_TYPE)
    @Blocking
    @RateLimit(RateLimitConst.HELLODATA_EXPORT_LIMIT)
    @RolesAllowed({ BerechtigungName.HELLODATA_EXPORT_LESEN })
    @APIResponse(
        responseCode = "200",
        description = JSON_RESPONSE_DESCRIPTION,
        content = @Content(
            mediaType = JsonEnvelopeWriter.MEDIA_TYPE,
            schema = @Schema(implementation = HelloDataAufgabenEnvelopeJson.class))
    )
    public Response exportAufgabenAsJson() {
        return streamed(helloDataExportService::writeAufgabenAsJson, JsonEnvelopeWriter.MEDIA_TYPE);
    }

    @GET
    @Path("/ndjson/klassen")
    @Produces(NdjsonWriter.MEDIA_TYPE)
    @Blocking
    @RateLimit(RateLimitConst.HELLODATA_EXPORT_LIMIT)
    @RolesAllowed({ BerechtigungName.HELLODATA_EXPORT_LESEN })
    @APIResponse(
        responseCode = "200",
        description = NDJSON_KLASSEN_RESPONSE_DESCRIPTION,
        content = @Content(
            mediaType = NdjsonWriter.MEDIA_TYPE,
            // Each line is either a data row or the closing sentinel. OpenAPI has no NDJSON
            // framing, so the schema describes a single line and the description the framing.
            schema = @Schema(
                oneOf = { HelloDataKlasseJson.class, HelloDataSentinelJson.class }))
    )
    public Response exportKlassen() {
        return ndjson(helloDataExportService::writeKlassen);
    }

    /**
     * The same export as {@link #exportKlassen()}, framed as one JSON document instead of
     * NDJSON. Kept as a separate path rather than an {@code Accept} variant - see the class
     * documentation for why.
     */
    @GET
    @Path("/json/klassen")
    @Produces(JsonEnvelopeWriter.MEDIA_TYPE)
    @Blocking
    @RateLimit(RateLimitConst.HELLODATA_EXPORT_LIMIT)
    @RolesAllowed({ BerechtigungName.HELLODATA_EXPORT_LESEN })
    @APIResponse(
        responseCode = "200",
        description = JSON_RESPONSE_DESCRIPTION,
        content = @Content(
            mediaType = JsonEnvelopeWriter.MEDIA_TYPE,
            schema = @Schema(implementation = HelloDataKlassenEnvelopeJson.class))
    )
    public Response exportKlassenAsJson() {
        return streamed(helloDataExportService::writeKlassenAsJson, JsonEnvelopeWriter.MEDIA_TYPE);
    }

    @GET
    @Path("/ndjson/impfstatuskrankheiten")
    @Produces(NdjsonWriter.MEDIA_TYPE)
    @Blocking
    @RateLimit(RateLimitConst.HELLODATA_EXPORT_LIMIT)
    @RolesAllowed({ BerechtigungName.HELLODATA_EXPORT_LESEN })
    @APIResponse(
        responseCode = "200",
        description = NDJSON_IMPFSTATUSKRANKHEITEN_RESPONSE_DESCRIPTION,
        content = @Content(
            mediaType = NdjsonWriter.MEDIA_TYPE,
            schema = @Schema(
                oneOf = { HelloDataImpfstatusKrankheitJson.class, HelloDataSentinelJson.class }))
    )
    public Response exportImpfstatusKrankheiten() {
        return ndjson(helloDataExportService::writeImpfstatusKrankheiten);
    }

    @GET
    @Path("/json/impfstatuskrankheiten")
    @Produces(JsonEnvelopeWriter.MEDIA_TYPE)
    @Blocking
    @RateLimit(RateLimitConst.HELLODATA_EXPORT_LIMIT)
    @RolesAllowed({ BerechtigungName.HELLODATA_EXPORT_LESEN })
    @APIResponse(
        responseCode = "200",
        description = JSON_RESPONSE_DESCRIPTION,
        content = @Content(
            mediaType = JsonEnvelopeWriter.MEDIA_TYPE,
            schema = @Schema(implementation = HelloDataImpfstatusEnvelopeJson.class))
    )
    public Response exportImpfstatusKrankheitenAsJson() {
        return streamed(helloDataExportService::writeImpfstatusKrankheitenAsJson, JsonEnvelopeWriter.MEDIA_TYPE);
    }

    @GET
    @Path("/ndjson/impfungenintern")
    @Produces(NdjsonWriter.MEDIA_TYPE)
    @Blocking
    @RateLimit(RateLimitConst.HELLODATA_EXPORT_LIMIT)
    @RolesAllowed({ BerechtigungName.HELLODATA_EXPORT_LESEN })
    @APIResponse(
        responseCode = "200",
        description = NDJSON_IMPFUNGENINTERN_RESPONSE_DESCRIPTION,
        content = @Content(
            mediaType = NdjsonWriter.MEDIA_TYPE,
            schema = @Schema(
                oneOf = { HelloDataImpfungInternJson.class, HelloDataSentinelJson.class }))
    )
    public Response exportImpfungenIntern() {
        return ndjson(helloDataExportService::writeImpfungenIntern);
    }

    @GET
    @Path("/json/impfungenintern")
    @Produces(JsonEnvelopeWriter.MEDIA_TYPE)
    @Blocking
    @RateLimit(RateLimitConst.HELLODATA_EXPORT_LIMIT)
    @RolesAllowed({ BerechtigungName.HELLODATA_EXPORT_LESEN })
    @APIResponse(
        responseCode = "200",
        description = JSON_RESPONSE_DESCRIPTION,
        content = @Content(
            mediaType = JsonEnvelopeWriter.MEDIA_TYPE,
            schema = @Schema(implementation = HelloDataImpfungenInternEnvelopeJson.class))
    )
    public Response exportImpfungenInternAsJson() {
        return streamed(helloDataExportService::writeImpfungenInternAsJson, JsonEnvelopeWriter.MEDIA_TYPE);
    }

    @GET
    @Path("/ndjson/anlaesse")
    @Produces(NdjsonWriter.MEDIA_TYPE)
    @Blocking
    @RateLimit(RateLimitConst.HELLODATA_EXPORT_LIMIT)
    @RolesAllowed({ BerechtigungName.HELLODATA_EXPORT_LESEN })
    @APIResponse(
        responseCode = "200",
        description = NDJSON_ANLAESSE_RESPONSE_DESCRIPTION,
        content = @Content(
            mediaType = NdjsonWriter.MEDIA_TYPE,
            // Each line is either a data row or the closing sentinel. OpenAPI has no NDJSON
            // framing, so the schema describes a single line and the description the framing.
            schema = @Schema(
                oneOf = { HelloDataAnlassJson.class, HelloDataSentinelJson.class }))
    )
    public Response exportAnlaesse() {
        return ndjson(helloDataExportService::writeAnlaesse);
    }

    /**
     * The same export as {@link #exportAnlaesse()}, framed as one JSON document instead of
     * NDJSON. Kept as a separate path rather than an {@code Accept} variant - see the class
     * documentation for why.
     */
    @GET
    @Path("/json/anlaesse")
    @Produces(JsonEnvelopeWriter.MEDIA_TYPE)
    @Blocking
    @RateLimit(RateLimitConst.HELLODATA_EXPORT_LIMIT)
    @RolesAllowed({ BerechtigungName.HELLODATA_EXPORT_LESEN })
    @APIResponse(
        responseCode = "200",
        description = JSON_RESPONSE_DESCRIPTION,
        content = @Content(
            mediaType = JsonEnvelopeWriter.MEDIA_TYPE,
            schema = @Schema(implementation = HelloDataAnlaesseEnvelopeJson.class))
    )
    public Response exportAnlaesseAsJson() {
        return streamed(helloDataExportService::writeAnlaesseAsJson, JsonEnvelopeWriter.MEDIA_TYPE);
    }

    @GET
    @Path("/ndjson/dossiers")
    @Produces(NdjsonWriter.MEDIA_TYPE)
    @Blocking
    @RateLimit(RateLimitConst.HELLODATA_EXPORT_LIMIT)
    @RolesAllowed({ BerechtigungName.HELLODATA_EXPORT_LESEN })
    @APIResponse(
        responseCode = "200",
        description = NDJSON_DOSSIERS_RESPONSE_DESCRIPTION,
        content = @Content(
            mediaType = NdjsonWriter.MEDIA_TYPE,
            // Each line is either a data row or the closing sentinel. OpenAPI has no NDJSON
            // framing, so the schema describes a single line and the description the framing.
            schema = @Schema(
                oneOf = { HelloDataDossierJson.class, HelloDataSentinelJson.class }))
    )
    public Response exportDossiers() {
        return ndjson(helloDataExportService::writeDossiers);
    }

    /**
     * The same export as {@link #exportDossiers()}, framed as one JSON document instead of
     * NDJSON. Kept as a separate path rather than an {@code Accept} variant - see the class
     * documentation for why.
     */
    @GET
    @Path("/json/dossiers")
    @Produces(JsonEnvelopeWriter.MEDIA_TYPE)
    @Blocking
    @RateLimit(RateLimitConst.HELLODATA_EXPORT_LIMIT)
    @RolesAllowed({ BerechtigungName.HELLODATA_EXPORT_LESEN })
    @APIResponse(
        responseCode = "200",
        description = JSON_RESPONSE_DESCRIPTION,
        content = @Content(
            mediaType = JsonEnvelopeWriter.MEDIA_TYPE,
            schema = @Schema(implementation = HelloDataDossiersEnvelopeJson.class))
    )
    public Response exportDossiersAsJson() {
        return streamed(helloDataExportService::writeDossiersAsJson, JsonEnvelopeWriter.MEDIA_TYPE);
    }

    @GET
    @Path("/ndjson/untersuchungen")
    @Produces(NdjsonWriter.MEDIA_TYPE)
    @Blocking
    @RateLimit(RateLimitConst.HELLODATA_EXPORT_LIMIT)
    @RolesAllowed({ BerechtigungName.HELLODATA_EXPORT_LESEN })
    @APIResponse(
        responseCode = "200",
        description = NDJSON_UNTERSUCHUNGEN_RESPONSE_DESCRIPTION,
        content = @Content(
            mediaType = NdjsonWriter.MEDIA_TYPE,
            // Each line is either a data row or the closing sentinel. OpenAPI has no NDJSON
            // framing, so the schema describes a single line and the description the framing.
            schema = @Schema(
                oneOf = { HelloDataUntersuchungenJson.class, HelloDataSentinelJson.class }))
    )
    public Response exportUntersuchungen() {
        return ndjson(helloDataExportService::writeUntersuchungen);
    }

    @GET
    @Path("/json/untersuchungen")
    @Produces(JsonEnvelopeWriter.MEDIA_TYPE)
    @Blocking
    @RateLimit(RateLimitConst.HELLODATA_EXPORT_LIMIT)
    @RolesAllowed({ BerechtigungName.HELLODATA_EXPORT_LESEN })
    @APIResponse(
        responseCode = "200",
        description = JSON_RESPONSE_DESCRIPTION,
        content = @Content(
            mediaType = JsonEnvelopeWriter.MEDIA_TYPE,
            schema = @Schema(implementation = HelloDataUntersuchungenEnvelopeJson.class))
    )
    public Response exportUntersuchungenAsJson() {
        return streamed(helloDataExportService::writeUntersuchungenAsJson, JsonEnvelopeWriter.MEDIA_TYPE);
    }

    /**
     * Every export endpoint answers the same way, so the media type is set in one place.
     */
    @NonNull
    private Response ndjson(@NonNull StreamingOutput streamingOutput) {
        return streamed(streamingOutput, NdjsonWriter.MEDIA_TYPE);
    }

    /**
     * Note that this returns immediately - it only builds the {@code StreamingOutput}. The
     * actual work (and with it the bulkhead and the timer) happens in the service, which is
     * called from {@code write(...)} once the body writer runs.
     */
    @NonNull
    private Response streamed(@NonNull StreamingOutput streamingOutput, @NonNull String mediaType) {
        return Response.ok(streamingOutput)
            .type(mediaType)
            .build();
    }
}
