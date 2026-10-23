/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.hemed.esc.api.features.hellodata.json;

import java.util.UUID;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.types.ArtAbmeldung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.types.EinladungStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.types.GrundAbmeldung;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * One row of the HelloData export for Einladungen.
 * <p>
 * This is a <b>contract</b> towards HelloData, not a mirror of the entity: fields must not
 * be added or renamed casually here, because the statistic models at HelloData build on them.
 * <p>
 * Deliberately <b>not</b> exported:
 * <ul>
 *     <li>{@code bemerkungAbmeldung} - free text, may contain personal data</li>
 *     <li>{@code notizen} - free text and personal data; only the two derived booleans
 *     {@link #impfausweisVorhanden} / {@link #fragebogenVorhanden} leave the system</li>
 *     <li>{@code spssExported}, {@code naechsteStation}, {@code naechsteStationSeit} - internal
 *     workflow state, not part of the agreed contract</li>
 * </ul>
 * The {@code id} is the production UUID (a deliberate decision, see the feature docs): it is
 * stable across load runs and serves as the join key.
 */
@Value
@AllArgsConstructor
@JaxDTO
public class HelloDataEinladungJson {

    // Raw UUID instead of ID<Einladung> on purpose: this is the wire contract towards
    // HelloData, which must not be coupled to our internal ID wrapper. These values are never
    // used to look anything up on our side, so there is no risk of mixing up IDs of different
    // entities.
    @IgnoreForIdClassTest
    @NotNull
    @NonNull
    @Schema(required = true)
    UUID id;

    @IgnoreForIdClassTest
    @NotNull
    @NonNull
    @Schema(required = true)
    UUID anlassId;

    /**
     * The Kinddossier this Einladung belongs to, or null for anonymous entries.
     */
    @IgnoreForIdClassTest
    @Nullable
    @Schema(required = false)
    UUID dossierId;

    /**
     * The Untersuchung that was actually carried out, if any. {@code leftJoin}, so Einladungen
     * without an Untersuchung survive the export.
     */
    @IgnoreForIdClassTest
    @Nullable
    @Schema(required = false)
    UUID untersuchungId;

    /**
     * Status of the Einladung, or null for anonymous entries.
     */
    @Nullable
    @Schema(required = false)
    EinladungStatus einladungStatus;

    @Nullable
    @Schema(required = false)
    GrundAbmeldung grundAbmeldung;

    @Nullable
    @Schema(required = false)
    ArtAbmeldung artAbmeldung;

    /**
     * Whether an Impfausweis was uploaded: true when a Notiz of typ {@code IMPFAUSWEIS} is
     * linked to this Einladung <b>and</b> carries at least one file. A Notiz without a file is
     * not an upload, so it does not count.
     * <p>
     * Only this boolean leaves the system - the Notiz itself is free text and stays inside ESC.
     */
    @Schema(required = true)
    boolean impfausweisVorhanden;

    /**
     * Whether a Fragebogen was uploaded. Same rule as {@link #impfausweisVorhanden}, for the
     * Notiz typ {@code FRAGEBOGEN}.
     */
    @Schema(required = true)
    boolean fragebogenVorhanden;

    /**
     * Optional for anonymous children, replaces the Kinderdossier. SHA256 hash calculated from
     * Nachname, Vorname, Geburtsdatum, Geschlecht. If identical, it can be inferred that it is
     * potentially the same child.
     */
    @Nullable
    @Schema(required = false)
    String hash;
}
