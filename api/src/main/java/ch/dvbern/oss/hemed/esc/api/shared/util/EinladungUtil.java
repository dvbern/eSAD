/*
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

package ch.dvbern.oss.hemed.esc.api.shared.util;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson.Lehrperson;
import ch.dvbern.oss.hemed.esc.api.shared.printer.BasePrinter;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil;
import lombok.experimental.UtilityClass;
import org.checkerframework.checker.nullness.qual.NonNull;

@UtilityClass
public class EinladungUtil {

    @NonNull
    public static String extractSchulenCommaSeparated(@NonNull List<Einladung> einladungen) {

        return einladungen.stream()
            .map(einladung -> einladung.getDossier().getKind().extractSchule())
            .filter(Objects::nonNull)
            .distinct()
            .map(Schule::getSchulhaus)
            .collect(Collectors.joining(", "));
    }

    @NonNull
    public static String extracKlassenCommaSeparated(@NonNull List<Einladung> einladungen) {
        return einladungen.stream()
            .map(einladung -> einladung.getDossier().getKind().getKlasse())
            .filter(Objects::nonNull)
            .distinct()
            .map(Klasse::getBezeichnung)
            .collect(Collectors.joining(", "));

    }

    @NonNull
    public static String extracLehrpersonenCommaSeparated(
        @NonNull List<Einladung> einladungen,
        @NonNull BasePrinter basePrinter
    ) {
        List<Klasse> klassen = einladungen.stream()
            .map(einladung -> einladung.getDossier().getKind().getKlasse())
            .filter(Objects::nonNull)
            .distinct()
            .toList();

        List<Lehrperson> lehrpersonen = klassen.stream().map(Klasse::getLehrpersonen)
            .flatMap(List::stream)
            .filter(Objects::nonNull)
            .distinct()
            .toList();

        return lehrpersonen.stream()
            .map(lehrperson -> PrintUtil.getNameWithAnrede(
                basePrinter,
                lehrperson.getGeschlecht(),
                lehrperson.getNameVorname()))
            .collect(Collectors.joining(", "));
    }
}
