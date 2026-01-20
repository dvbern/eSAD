/*
 * Copyright (C) 2025 DV Bern AG, Switzerland
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

package ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto;

import lombok.Getter;

@Getter
public enum MassenimportFields {
    COL_INDEX_SVNR(0),
    COL_INDEX_NACHNAME(1),
    COL_INDEX_VORNAME(2),
    COL_INDEX_GESCHLECHT(3),
    COL_INDEX_GEBURTSDATUM(4),
    COL_INDEX_NATIONALITAET(5),
    COL_INDEX_ERSTSPRACHE(6),
    COL_INDEX_EMAIL(7),
    COL_INDEX_TELEFON(8),
    COL_INDEX_WOHNTYP(9),
    COL_INDEX_ADRESSE_LAND(10),
    COL_INDEX_ADRESSE_STRASSE_NR(11),
    COL_INDEX_ADRESSE_ZUSATZ(12),
    COL_INDEX_ADRESSE_PLZ(13),
    COL_INDEX_ADRESSE_ORT(14),
    COL_INDEX_GESCHLECHT_KP(15),
    COL_INDEX_NACHNAME_KP(16),
    COL_INDEX_VORNAME_KP(17),
    COL_INDEX_BEZIEHUNG_KP(18),
    COL_INDEX_EMAIL_KP(19),
    COL_INDEX_TELEFON_KP(20),
    COL_INDEX_KORRESPONDENZ_KP(21),
    COL_INDEX_ADRESSE_LAND_KP(22),
    COL_INDEX_ADRESSE_STRASSE_NR_KP(23),
    COL_INDEX_ADRESSE_ZUSATZ_KP(24),
    COL_INDEX_ADRESSE_PLZ_KP(25),
    COL_INDEX_ADRESSE_ORT_KP(26),
    COL_INDEX_KLASSE_BEZEICHUNG(27);

    private final int colIndex;

    MassenimportFields(int colIndex) {
        this.colIndex = colIndex;
    }

}
