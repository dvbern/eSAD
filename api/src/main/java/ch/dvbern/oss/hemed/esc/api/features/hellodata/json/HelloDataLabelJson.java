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

import java.util.UUID;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.labels.types.LabelTyp;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * One row of the HelloData export for Labels.
 * <p>
 * This is a <b>contract</b> towards HelloData, not a mirror of the entity: fields must not
 * be added or renamed casually here, because the statistic models at HelloData build on them.
 * <p>
 * The {@code id} is the production UUID (a deliberate decision, see the feature docs): it is
 * stable across load runs and serves as the join key.
 */
@Value
@AllArgsConstructor
@JaxDTO
public class HelloDataLabelJson {

    // Raw UUID instead of ID<Label> on purpose: this is the wire contract towards HelloData,
    // which must not be coupled to our internal ID wrapper. These values are never used to look
    // anything up on our side, so there is no risk of mixing up IDs of different entities.
    @IgnoreForIdClassTest
    @NotNull
    @NonNull
    @Schema(required = true)
    UUID id;

    // The type of the Label, either NOTIZEN or AUFGABEN.
    @NotNull
    @NonNull
    @Schema(required = true)
    LabelTyp labelTyp;

    @NotNull
    @NonNull
    @Schema(required = true)
    String nameDe;

    @NotNull
    @NonNull
    @Schema(required = true)
    String nameFr;

    // True when the Label is automatically assigned by the system.
    @NotNull
    @Schema(required = true)
    boolean autoLabel;

    // Whether the Label is generally still in use.
    @NotNull
    @Schema(required = true)
    boolean aktiv;
}
