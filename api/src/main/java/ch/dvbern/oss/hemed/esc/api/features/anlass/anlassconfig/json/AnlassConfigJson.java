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

package ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.json;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.types.AnlassConfigStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class AnlassConfigJson {

    @NonNull
    @Schema(required = true)
    private final ID<AnlassConfig> id;

    @NonNull
    @NotNull
    @Valid
    @Schema(required = true)
    private AnlassConfigSaveJson data;

    @NonNull
    @NotNull
    @Schema(required = true)
    private AnlassConfigStatus anlassConfigStatus;

    @Schema(required = true)
    private boolean anlassConfigVisible;

    @Schema(required = true)
    private boolean latestAnlassConfigForAnlassTyp;

    // Daten

    @Schema(required = true)
    private boolean showTabDaten;

    // Daten

    @Schema(required = true)
    private boolean showTabVorabklaerungen;

    // Impfungen

    @Schema(required = true)
    private boolean showTabImpfungen;

    @Schema(required = true)
    private boolean showCardImpfungenHeader;

    @Schema(required = true)
    private boolean showCardImpfungenDataMinimal;

    @Schema(required = true)
    private boolean showCardImpfungenImpfstatus;

    @Schema(required = true)
    private boolean showCardImpfungenListeIntern;

    @Schema(required = true)
    private boolean showCardImpfungenChecklist;

    @Schema(required = true)
    private boolean showCardImpfungenDoppelkontrolle;

    @Schema(required = true)
    private boolean showCardImpfungenMassnahmen;

    // Sehen

    @Schema(required = true)
    private boolean showTabSehen;

    @Schema(required = true)
    private boolean showTabSehenBeurteilung;

    @Schema(required = true)
    private boolean showTabSehenBemerkungenFragenbogen;

    @Schema(required = true)
    private boolean showTabSehenMassnahmen;

    // Hoeren

    @Schema(required = true)
    private boolean showTabHoeren;

    @Schema(required = true)
    private boolean showTabHoerenAudiogramm;

    @Schema(required = true)
    private boolean showTabHoerenOtoskopie;

    // Groesse und Gewicht

    @Schema(required = true)
    private boolean showTabGroesseGewicht;

    // Triage

    @Schema(required = true)
    private boolean showTabTriage;

    // Läusekontrolle

    @Schema(required = true)
    private boolean showTabLaeusekontrolle;

    // Aerztliche Untersuchung

    @Schema(required = true)
    private boolean showTabUntersuchung;

    @Schema(required = true)
    private boolean showTabUntersuchungBeurteilung;

    @Schema(required = true)
    private boolean showTabUntersuchungBemerkung;

    // Sprache

    @Schema(required = true)
    private boolean showTabSprache;

    // Bewegungsapparat

    @Schema(required = true)
    private boolean showTabBewegungsapparat;

    // Schulrelevante Beeinträchtigung

    @Schema(required = true)
    private boolean showTabSchulrelevanteBeeintraechtigung;

    // Abschluss
    @Schema(required = true)
    private boolean showTabAbschluss;

    @Schema(required = true)
    private boolean showCardAbschlussAbschluss;

    @Schema(required = true)
    private boolean showCardAbschlussSignatur;

    // Massnahmen

    @Schema(required = true)
    private boolean showTabMassnahmen;
}
