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

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * The last line of every export.
 * This is useful if the consumer of this api consumes the new line separated json response stream.
 * The problem with streaming is, that once we have sent 200 there is no way to change the statuscode, so if
 * something breaks on the way we can not send an error. To give a clear sign of success or failure this
 * HelloDatanSentinelJson serves as a clear indicator that the real END of data was reached.
 * <p>
 * A dedicated type rather than an ad-hoc map, so the line appears in the OpenAPI contract:
 * HelloData generates their ingestion models from it and needs to know that the final line has
 * a different shape than the data rows.
 * <p>
 * The payload is nested under {@value #META_KEY} so it can never collide with a data field.
 */
@Value
@AllArgsConstructor
@JaxDTO
public class HelloDataSentinelJson {

    /** Key under which the sentinel is nested. Part of the wire contract. */
    public static final String META_KEY = "_meta";

    @NotNull
    @NonNull
    @Valid
    @Schema(required = true)
    @JsonProperty(META_KEY)
    HelloDataMetaJson meta;
}
