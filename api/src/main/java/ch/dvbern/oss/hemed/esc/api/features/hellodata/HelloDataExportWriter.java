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
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * How an export is framed on the wire. The row loop, the transaction, the tenant check, the
 * flush cadence and the auditing are identical for every format and live in
 * {@link HelloDataExportService}; only the framing differs, and that is what this interface is.
 * <p>
 * Two implementations exist because it is not settled which one HelloData can ingest:
 * <ul>
 *     <li>{@link NdjsonWriter} - one JSON object per line, no enclosing structure</li>
 *     <li>{@link JsonEnvelopeWriter} - a single JSON object with a {@code data} array</li>
 * </ul>
 * <b>Both stream.</b> The envelope is written incrementally (opening brace, then rows, then the
 * closing sentinel), so the server-side memory profile is the same for both - see
 * {@link JsonEnvelopeWriter} for what that does <i>not</i> guarantee about the client.
 * <p>
 * Implementations must not close or flush the stream themselves: flushing is the row loop's job
 * (it knows the cadence), and closing is the container's.
 */
public interface HelloDataExportWriter {

    /**
     * Short name of the format, used in the audit log so it stays visible which endpoint a
     * client actually called.
     */
    @NonNull
    String formatName();

    /**
     * Called once before the first row. NDJSON has no preamble, the envelope opens its object
     * and array here.
     */
    void writeStart(@NonNull OutputStream os) throws IOException;

    /**
     * @param rowNumber 1-based. Formats that need a separator between rows (the envelope needs a
     * comma) use it to recognise the first row - the row count is not known upfront, so
     * "separator before every row but the first" is the only workable rule while streaming.
     */
    void writeRow(@NonNull OutputStream os, @NonNull Object row, long rowNumber) throws IOException;

    /**
     * Writes the terminating {@code _meta} sentinel. Mandatory for every format: once HTTP 200
     * has been sent, a mid-stream failure can no longer be signalled by status code, so the
     * sentinel is the only way for HelloData to tell a complete export from a truncated one.
     * <p>
     * The sentinel is handed in rather than built here, so its content is defined once (in
     * {@link HelloDataExportService}) and cannot drift between the formats.
     */
    void writeEnd(@NonNull OutputStream os, @NonNull HelloDataSentinelJson sentinel) throws IOException;
}
