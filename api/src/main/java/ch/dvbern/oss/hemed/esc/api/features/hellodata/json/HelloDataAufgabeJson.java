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

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.types.AufgabeStatus;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * One row of the HelloData export for Aufgaben.
 * <p>
 * This is a <b>contract</b> towards HelloData, not a mirror of the entity: fields must not
 * be added or renamed casually here, because the statistic models at HelloData build on them.
 * <p>
 * Deliberately <b>not</b> exported:
 * <ul>
 *     <li>{@code titel}, {@code bemerkung} - free text, may contain personal data</li>
 *     <li>{@code files} - the uploaded documents themselves, not an analytical dimension</li>
 *     <li>{@code link} - an internal deep link, not an analytical dimension</li>
 * </ul>
 * The {@code id} is the production UUID (a deliberate decision, see the feature docs): it is
 * stable across load runs and serves as the join key.
 */
@Value
@AllArgsConstructor
@JaxDTO
public class HelloDataAufgabeJson {

    // Raw UUID instead of ID<Aufgabe> on purpose: this is the wire contract towards HelloData,
    // which must not be coupled to our internal ID wrapper. These values are never used to look
    // anything up on our side, so there is no risk of mixing up IDs of different entities.
    @IgnoreForIdClassTest
    @NotNull
    @NonNull
    @Schema(required = true)
    UUID id;

    @NotNull
    @NonNull
    @Schema(required = true)
    AufgabeStatus aufgabeStatus;

    @NotNull
    @NonNull
    @Schema(required = true)
    LocalDate faelligBis;

    // The Benutzer who created this Aufgabe (Aufgabe.userErfasst).
    @NotNull
    @NonNull
    @Schema(required = true)
    UUID userErfasst;

    // The Benutzer responsible for this Aufgabe (Aufgabe.empfaenger).
    @NotNull
    @NonNull
    @Schema(required = true)
    UUID empfaenger;

    // Optional: an Aufgabe does not have to belong to a Kind.
    @IgnoreForIdClassTest
    @Nullable
    @Schema(required = false)
    UUID kindId;

    // Optional: an Aufgabe does not have to belong to an Anlass.
    @IgnoreForIdClassTest
    @Nullable
    @Schema(required = false)
    UUID anlassId;

    // The Labels assigned to this Aufgabe as a comma-separated list of Label UUIDs (e.g.
    // "350f2746-...,f8514824-..."), aggregated in the database and ordered by UUID so the
    // export stays reproducible. Empty string when the Aufgabe has no Label at all.
    @NotNull
    @NonNull
    @Schema(required = true)
    String labels;
}
