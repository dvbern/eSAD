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

package ch.dvbern.oss.hemed.esc.api.shared.aufgabe;

import lombok.Getter;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Important, synced to frontend enum Path!!!
 * If you change a *value* here, make sure to check the equivalent enum Path in the frontend!
 * Order alpabetically for cleaner code!
 */
@Getter
public enum Path {
    ADMIN("admin"),
    AERZTE("aerzte"),
    ALL("all"),
    ANLASS("anlass"),
    ANLASS_BEARBEITUNG("anlassbearbeitung"),
    ANLASS_CONFIG("anlass-config"),
    ANLASS_CONFIG_BASIS("basis-config"),
    ANLASS_CONFIG_DATEN("daten-config"),
    ANLASS_CONFIG_IMPFUNGEN("impfungen-config"),
    ANLASS_CONFIG_SEHEN("sehen-config"),
    ANLASS_CONFIG_HOEREN_AUDIOGRAMM("hoeren-audiogramm-config"),
    ANLASS_CONFIG_HOEREN_OTOSKOPIE("hoeren-otoskopie-config"),
    ANLASS_CONFIG_GROEOSSE_GEWICHT("groesse-gewich-configt"),
    ANLASS_CONFIG_TRIAGE("triage-config"),
    ANLASS_CONFIG_AERZTLICHE_UNTERSUCHUNG("aezrztliche-untersuchung-config"),
    ANLASS_CONFIG_MASSNAHMEN("massnahmen-config"),
    ANLASS_PLANUNG("anlassplanung"),
    ANLASS_UNTERSUCHUNG("untersuchung"),
    ANLASS_UNTERSUCHUNG_EINLADUNG("einladung"),
    ANLASS_UNTERSUCHUNG_EINLADUNG_ARZT("arzt"),
    ANLASS_UNTERSUCHUNG_EINLADUNG_DATEN("untersuchungdaten"),
    ANLASS_UNTERSUCHUNG_EINLADUNG_GROESSEGEWICHT("groessegewicht"),
    ANLASS_UNTERSUCHUNG_EINLADUNG_HOEREN("hoeren"),
    ANLASS_UNTERSUCHUNG_EINLADUNG_IDS_SEGMENT("einladungIds"),
    ANLASS_UNTERSUCHUNG_EINLADUNG_IMPFUNGEN("impfungen"),
    ANLASS_UNTERSUCHUNG_EINLADUNG_MASSNAHMEN("massnahmen"),
    ANLASS_UNTERSUCHUNG_EINLADUNG_SEHEN("sehen"),
    ANLASS_UNTERSUCHUNG_EINLADUNG_TRIAGE("triage"),
    AUFGABEN("aufgaben"),
    BENUTZER("benutzer"),
    BRIEFVORLAGEN("briefvorlagen"),
    CHARGE("charge"),
    CREATE("create"),
    COLUMNS("columns"),
    CONFIG("config"),
    DASHBOARD("dashboard"),
    DATEN("daten"),
    DIALOGS("dialogs"),
    DUPLIKATE("duplikate"),
    FORM("form"),
    FRAGEBOGEN("fragebogen"),
    IMPFAUSWEIS("impfausweis"),
    IMPFSTOFF("impfstoff"),
    INFOMATERIAL("infomaterial"),
    KINDER("kinder"),
    KLASSEN("klassen"),
    KONTAKTPERSONEN("kontaktpersonen"),
    LABEL("label"),
    LEHRPERSONEN("lehrpersonen"),
    NO_ROLES("no-roles"),
    NOTIZEN("notizen"),
    PERSONENDATEN("personendaten"),
    PLAYGROUND("playground"),
    PROFIL("profil"),
    RELATIVE_FOUR_UP("../../../.."),
    RELATIVE_HERE("."),
    RELATIVE_ONE_UP(".."),
    RELATIVE_THREE_UP("../../.."),
    RELATIVE_TWO_UP("../.."),
    REPORT("report"),
    SCHULE_KLASSE("schule"),
    SCHULEN("schulen"),
    STAMMDATEN("stammdaten"),
    SYSTEM("system"),
    SYSTEM_ADMIN("systemadmin"),
    SYSTEM_GENERAL("general"),
    SYSTEM_JOB("jobs"),
    TABLE("table"),
    TYPORGRAPHY("typography");

    @NonNull
    private final String value;

    Path(@NonNull String value) {
        this.value = value;
    }
}
