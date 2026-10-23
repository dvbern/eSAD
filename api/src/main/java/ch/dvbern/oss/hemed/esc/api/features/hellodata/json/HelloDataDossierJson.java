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
import ch.dvbern.oss.hemed.esc.api.features.kinder.types.PapierakteStatus;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import ch.dvbern.oss.hemed.esc.api.shared.person.Land;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * One row of the HelloData export for Dossiers.
 * <p>
 * This is a <b>contract</b> towards HelloData, not a mirror of the entity: fields must not
 * be added or renamed casually here, because the statistic models at HelloData build on them.
 * <p>
 * Deliberately <b>not</b> exported (data protection):
 * <ul>
 *     <li>{@code name}, {@code vorname}, {@code adresse}, {@code email}, {@code telefon} of the
 *     Kind - personal data, not needed for analytics</li>
 *     <li>{@code andereZusatzinformation}, {@code andereZusatzInformationText} - free text, may
 *     contain personal data</li>
 *     <li>{@code zustaendigArzt}, {@code zustaendigMPA} - personal data of staff</li>
 * </ul>
 * The {@code id} is the production UUID (a deliberate decision, see the feature docs): it is
 * stable across load runs and serves as the join key.
 */
@Value
@AllArgsConstructor
@JaxDTO
public class HelloDataDossierJson {

    // Raw UUID instead of ID<Dossier> on purpose: this is the wire contract towards HelloData,
    // which must not be coupled to our internal ID wrapper. These values are never used to look
    // anything up on our side, so there is no risk of mixing up IDs of different entities.
    @IgnoreForIdClassTest
    @NotNull
    @NonNull
    @Schema(required = true)
    UUID id;

    // The Kind this Dossier belongs to (Dossier.kind).
    @IgnoreForIdClassTest
    @NotNull
    @NonNull
    @Schema(required = true)
    UUID kindId;

    @NotNull
    @NonNull
    @Schema(required = true)
    Geschlecht geschlecht;

    @NotNull
    @NonNull
    @Schema(required = true)
    Integer geburtsjahr;

    // Optional: not every Kind has a known nationality.
    @Nullable
    @Schema(required = false)
    Land nationalitaet;

    @Nullable
    @Schema(required = false)
    String erstsprache;

    @Nullable
    @Schema(required = false)
    String wohntyp;

    // In principle the Schule is given through the Klasse, but there are Kinder for which only
    // the Schule is known (Kind.schule). Optional for the same reason.
    @IgnoreForIdClassTest
    @Nullable
    @Schema(required = false)
    UUID schuleId;

    @IgnoreForIdClassTest
    @Nullable
    @Schema(required = false)
    UUID klasseId;

    @NotNull
    @NonNull
    @Schema(required = true)
    EntityAktivStatus kindAktivStatus;

    @NotNull
    @Schema(required = true)
    boolean kinderschutzfall;

    @NotNull
    @Schema(required = true)
    boolean keineAuskunftAnDrittpersonen;

    @NotNull
    @Schema(required = true)
    boolean sansPapier;

    @NotNull
    @Schema(required = true)
    boolean immunitaetMasern;

    @NotNull
    @Schema(required = true)
    boolean immunitaetVarizellen;

    @NotNull
    @NonNull
    @Schema(required = true)
    PapierakteStatus papierakteStatus;

    // Same value and computation as AnonymerEintrag.hash: a SHA-256 hash over Name, Vorname,
    // Geschlecht and Geburtsdatum (see HashValueGeneratorUtil). Identical hashes across Kinder
    // hint that they may be the same person.
    @NotNull
    @NonNull
    @Schema(required = true)
    String hashValue;
}
