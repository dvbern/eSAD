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

import java.time.LocalDate;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassArt;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassStatus;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * One row of the HelloData export for Anlaesse.
 * <p>
 * This is a <b>contract</b> towards HelloData, not a mirror of the entity: fields must not
 * be added or renamed casually here, because the statistic models at HelloData build on them.
 * <p>
 * Deliberately <b>not</b> exported:
 * <ul>
 *     <li>{@code arztInfo}, {@code mpaInfo}, {@code weiterePersonInfo} - free text about staff,
 *     may contain personal data</li>
 *     <li>{@code weiterePerson} - staff personal data, not an analytical dimension</li>
 *     <li>{@code anlassConfig} - internal configuration of the Anlass, not analytics</li>
 * </ul>
 * The {@code id} is the production UUID (a deliberate decision, see the feature docs): it is
 * stable across load runs and serves as the join key.
 */
@Value
@AllArgsConstructor
@JaxDTO
public class HelloDataAnlassJson {

    // Raw UUID instead of ID<Anlass> on purpose: this is the wire contract towards HelloData,
    // which must not be coupled to our internal ID wrapper. These values are never used to look
    // anything up on our side, so there is no risk of mixing up IDs of different entities.
    @IgnoreForIdClassTest
    @NotNull
    @NonNull
    @Schema(required = true)
    UUID id;

    // Optional in the database: the Titel is only mandatory when the tenant has the feature
    // enabled, so it must stay nullable on the wire as well.
    @Nullable
    @Schema(required = false)
    String titel;

    // The Schuljahr the Anlass belongs to: Schuljahr 2025 runs from August 2025 to July 2026.
    @NotNull
    @NonNull
    @Schema(required = true)
    Integer schuljahr;

    @NotNull
    @NonNull
    @Schema(required = true)
    LocalDate datum;

    @NotNull
    @NonNull
    @Schema(required = true)
    AnlassStatus anlassStatus;

    @NotNull
    @NonNull
    @Schema(required = true)
    AnlassArt anlassArt;

    // The Benutzer carrying out the Anlass. Optional: zentrale Tenants plan Anlaesse without an
    // Arzt, and the Arzt is only assigned later.
    @IgnoreForIdClassTest
    @Nullable
    @Schema(required = false)
    UUID arztId;

    // The MPA responsible for the Anlass, likewise optional.
    @IgnoreForIdClassTest
    @Nullable
    @Schema(required = false)
    UUID mpaId;
}
