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

import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataSentinelJson;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Writes NDJSON (newline delimited JSON, {@value #MEDIA_TYPE}): one JSON object per line,
 * separated by {@code \n}, with no enclosing array and no commas.
 * <p>
 * Compared to a streamed JSON array this lets the consumer parse line by line
 * ({@code for line in response.iter_lines()}) instead of needing an incremental parser, and it
 * keeps memory usage bounded on the consumer side too. {@link JsonEnvelopeWriter} is the
 * alternative for consumers whose tooling can only ingest an ordinary JSON document.
 */
@ApplicationScoped
@RequiredArgsConstructor
public class NdjsonWriter implements HelloDataExportWriter {

    public static final String MEDIA_TYPE = "application/x-ndjson";

    public static final String FORMAT_NAME = "ndjson";

    private final ObjectMapper objectMapper;

    @Override
    @NonNull
    public String formatName() {
        return FORMAT_NAME;
    }

    /**
     * NDJSON has no preamble - the first row is simply the first line.
     */
    @Override
    public void writeStart(@NonNull OutputStream os) {
        // nothing to do
    }

    @Override
    public void writeRow(@NonNull OutputStream os, @NonNull Object row, long rowNumber) throws IOException {
        writeLine(os, row);
    }

    @Override
    public void writeEnd(@NonNull OutputStream os, @NonNull HelloDataSentinelJson sentinel) throws IOException {
        writeLine(os, sentinel);
    }

    private void writeLine(@NonNull OutputStream os, @NonNull Object value) throws IOException {
        // writeValueAsBytes rather than writeValue(os, ...): depending on its configuration
        // Jackson would otherwise close or flush the stream itself.
        os.write(objectMapper.writeValueAsBytes(value));
        os.write('\n');
    }
}
