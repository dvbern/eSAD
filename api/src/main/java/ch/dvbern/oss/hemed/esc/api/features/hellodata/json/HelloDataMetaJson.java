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

package ch.dvbern.oss.hemed.esc.api.features.hellodata.json;

import java.time.OffsetDateTime;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Payload of the terminating sentinel line of an NDJSON export.
 * <p>
 * <b>Why this is needed:</b> once the response has started with HTTP 200, a failure mid-stream
 * (network, proxy timeout, exception) can no longer be signalled via the status code. A
 * truncated stream is indistinguishable from a short table. HelloData <b>must</b> therefore
 * check that the last line carries a {@code _meta} with {@code status = "complete"} and treat
 * the load as failed otherwise.
 */
@Value
@AllArgsConstructor
@JaxDTO
public class HelloDataMetaJson {

    public static final String STATUS_COMPLETE = "complete";

    @NotNull
    @NonNull
    @Schema(required = true)
    String status;

    @Schema(required = true)
    long rowCount;

    @NotNull
    @NonNull
    @Schema(required = true)
    OffsetDateTime exportedAt;
}
