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

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * The whole Impfstoffe export as a single JSON document, for consumers that cannot ingest NDJSON.
 * The name says <b>Envelope</b> and not just {@code HelloDataImpfstoffeJson} on purpose: one
 * letter apart from the row type {@link HelloDataImpfstoffJson} would be an easy thing to confuse
 * in a review.
 * <p>
 * <b>This type describes the wire format, it does not produce it.</b> The response is written
 * incrementally by {@code JsonEnvelopeWriter} so that it can stream - an instance of this class
 * is never serialised into a response, because building one would mean holding every row in
 * memory first. It exists so the shape appears in the OpenAPI contract HelloData generates their
 * ingestion model from.
 * <p>
 * That split is a drift risk: renaming a field here would change the published contract without
 * changing a single byte the server actually sends. {@code HelloDataResourceITest} therefore
 * compares a serialised instance of this class against a real response.
 */
@Value
@AllArgsConstructor
@JaxDTO
public class HelloDataImpfstoffeEnvelopeJson {

    /**
     * All exported Impfstoffe. Empty rather than absent when the tenant has none.
     */
    @NotNull
    @NonNull
    @Valid
    @Schema(required = true)
    List<HelloDataImpfstoffJson> data;

    /**
     * Always the <b>last</b> member of the document: {@code rowCount} is only known once the
     * cursor is exhausted (see {@code JsonEnvelopeWriter}). JSON members are unordered, so
     * consumers must not depend on the position - only on the key being present.
     * <p>
     * If it is missing, the response was truncated and the load must be treated as failed.
     */
    @NotNull
    @NonNull
    @Valid
    @Schema(required = true)
    @JsonProperty(HelloDataSentinelJson.META_KEY)
    HelloDataMetaJson meta;
}
