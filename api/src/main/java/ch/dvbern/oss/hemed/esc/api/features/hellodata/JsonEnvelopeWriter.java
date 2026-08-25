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
import java.nio.charset.StandardCharsets;

import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataSentinelJson;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Writes the export as one ordinary JSON document ({@value #MEDIA_TYPE}): a container object
 * with the rows in a {@value #DATA_KEY} array, closed by the same {@code _meta} sentinel the
 * NDJSON variant uses.
 *
 * <pre>
 * {"data":[
 * {"id":"...","schulhaus":"..."},
 * {"id":"...","schulhaus":"..."}
 * ],"_meta":{"status":"complete","rowCount":2,"exportedAt":"2026-01-01T02:15:00+01:00"}}
 * </pre>
 *
 * <b>Why this exists next to {@link NdjsonWriter}:</b> it is not settled whether HelloData's
 * ingestion can read NDJSON. This variant needs nothing but a standard JSON parser.
 * <p>
 * <b>Server side this still streams.</b> The document is written incrementally - opening brace,
 * then row after row, then the sentinel - so the cursor, the flush cadence and the bounded heap
 * usage are exactly the same as for NDJSON. A truncated response is also still detectable, and
 * in fact harder to miss: it is not valid JSON at all.
 * <p>
 * <b>Client side it usually does not.</b> A consumer calling {@code json.loads(response.text)}
 * has to hold the whole document in memory before it sees the first row, which is precisely
 * what NDJSON avoids. That is a property of the consumer, not of this writer - a consumer using
 * an incremental parser (ijson, Jackson streaming) keeps the streaming end to end. It is the
 * reason NDJSON stays the recommended format.
 * <p>
 * <b>Why {@code _meta} comes last:</b> {@code rowCount} is only known once the last row has been
 * read from the cursor. Putting the sentinel first would mean counting the rows upfront, which
 * means materialising them - the exact opposite of streaming. JSON object members are unordered,
 * so this costs a consumer nothing.
 * <p>
 * The rows are separated by newlines even though JSON does not require it: it costs one byte per
 * row and makes the response readable with {@code head} while debugging.
 */
@ApplicationScoped
@RequiredArgsConstructor
public class JsonEnvelopeWriter implements HelloDataExportWriter {

    public static final String MEDIA_TYPE = MediaType.APPLICATION_JSON;

    public static final String FORMAT_NAME = "json";

    /**
     * Key holding the row array. Part of the wire contract - it must stay in sync with the field
     * of the envelope DTO (for Schulen: {@code HelloDataSchulenEnvelopeJson.data}), which is what
     * HelloData generates their model from. {@code HelloDataResourceITest} compares the two.
     */
    public static final String DATA_KEY = "data";

    private final ObjectMapper objectMapper;

    @Override
    @NonNull
    public String formatName() {
        return FORMAT_NAME;
    }

    @Override
    public void writeStart(@NonNull OutputStream os) throws IOException {
        write(os, "{\"" + DATA_KEY + "\":[");
    }

    @Override
    public void writeRow(@NonNull OutputStream os, @NonNull Object row, long rowNumber) throws IOException {
        // The row count is unknown while streaming, so "a comma before every row except the
        // first" is the only separator rule that works without looking ahead.
        write(os, rowNumber == 1 ? "\n" : ",\n");
        // writeValueAsBytes rather than writeValue(os, ...): depending on its configuration
        // Jackson would otherwise close or flush the stream itself.
        os.write(objectMapper.writeValueAsBytes(row));
    }

    @Override
    public void writeEnd(@NonNull OutputStream os, @NonNull HelloDataSentinelJson sentinel) throws IOException {
        write(os, "\n],\"" + HelloDataSentinelJson.META_KEY + "\":");
        // the payload, not the sentinel itself: the sentinel serialises to {"_meta":{...}} and
        // the key is already written above.
        os.write(objectMapper.writeValueAsBytes(sentinel.getMeta()));
        write(os, "}");
    }

    private static void write(@NonNull OutputStream os, @NonNull String literal) throws IOException {
        os.write(literal.getBytes(StandardCharsets.UTF_8));
    }
}
