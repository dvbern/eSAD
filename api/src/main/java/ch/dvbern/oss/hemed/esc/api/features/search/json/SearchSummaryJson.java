/*
 *
 * Copyright (C) 2025 DV Bern AG, Switzerland
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

package ch.dvbern.oss.hemed.esc.api.features.search.json;

import java.time.LocalDate;
import java.util.UUID;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.dvbstarter.shared.types.sozialversicherungsnummer.SozialversicherungsNummer;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.json.SchuleOptionJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.json.KlasseOptionJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.types.Klassenstufe;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import com.querydsl.core.annotations.QueryProjection;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Data
@JaxDTO
public class SearchSummaryJson {

    @NotNull
    @NonNull
    @Schema(required = true)
    private final ID<Kind> id;

    @Nullable
    @Schema(required = false)
    @IgnoreForIdClassTest
    private final String esadId;

    @Nullable
    @Schema(required = false)
    private SozialversicherungsNummer sozialversicherungsNummer;

    @NotNull
    @NonNull
    @Schema(required = true)
    private final String name;

    @NotNull
    @NonNull
    @Schema(required = true)
    private final String vorname;

    @NotNull
    @NonNull
    @Schema(required = true)
    private Geschlecht geschlecht;

    @NotNull
    @NonNull
    @Schema(required = true)
    private LocalDate geburtsdatum;

    @NotNull
    @NonNull
    @Schema(required = true)
    private EntityAktivStatus kindAktivStatus;

    @Nullable
    @Schema(required = false)
    @Valid
    private final SchuleOptionJson schule;

    @Nullable
    @Schema(required = false)
    @Valid
    private final KlasseOptionJson klasse;

    @NotNull
    @NonNull
    @Schema(required = false)
    private Klassenstufe klassenStufe;

    @SuppressWarnings("java:S107" /*many params is the point here */)
    @QueryProjection
    public SearchSummaryJson(
        @NonNull UUID id,
        @Nullable String esadId,
        @Nullable SozialversicherungsNummer sozialversicherungsNummer,
        @NonNull String name,
        @NonNull String vorname,
        @NonNull Geschlecht geschlecht,
        @NonNull LocalDate geburtsdatum,
        @NonNull EntityAktivStatus kindAktivStatus,
        @Nullable UUID schuleId,
        @Nullable String schuleBezeichnung,
        @NonNull EntityAktivStatus schuleAktivStatus,
        @Nullable UUID klasseId,
        @Nullable String klasseBezeichnung,
        @NonNull EntityAktivStatus klasseAktivStatus,
        @NonNull Klassenstufe klassenStufe
    ) {
        this.id = ID.of(id, Kind.class);
        this.esadId = esadId;
        this.sozialversicherungsNummer = sozialversicherungsNummer;
        this.name = name;
        this.vorname = vorname;
        this.geschlecht = geschlecht;
        this.geburtsdatum = geburtsdatum;
        this.kindAktivStatus = kindAktivStatus;
        this.schule = mapSchuleOption(schuleId, schuleBezeichnung, schuleAktivStatus);
        this.klasse = mapKlasseOption(klasseId, klasseBezeichnung, klasseAktivStatus);
        this.klassenStufe = klassenStufe;
    }

    @Nullable
    private KlasseOptionJson mapKlasseOption(
        @Nullable @IgnoreForIdClassTest UUID klasseId,
        @Nullable String klasseBezeichnung,
        @NonNull EntityAktivStatus klasseAktivStatus
    ) {
        if (klasseId == null || klasseBezeichnung == null) {
            return null;
        }
        return new KlasseOptionJson(
            ID.of(klasseId, Klasse.class),
            klasseBezeichnung,
            klasseAktivStatus);
    }

    @Nullable
    private SchuleOptionJson mapSchuleOption(
        @Nullable @IgnoreForIdClassTest UUID schuleId,
        @Nullable String schuleBezeichnung,
        @NonNull EntityAktivStatus schuleAktivStatus
    ) {
        if (schuleId == null || schuleBezeichnung == null) {
            return null;
        }
        return new SchuleOptionJson(
            ID.of(schuleId, Schule.class),
            schuleBezeichnung,
            schuleAktivStatus);
    }
}
