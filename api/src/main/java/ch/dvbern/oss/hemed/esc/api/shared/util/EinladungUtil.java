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

package ch.dvbern.oss.hemed.esc.api.shared.util;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import org.checkerframework.checker.nullness.qual.NonNull;

@UtilityClass
public class EinladungUtil {

    @NonNull
    public static String extractSchulenCommaSeparated(@NonNull List<Einladung> einladungen) {

        return einladungen.stream()
            .map(einladung -> einladung.getDossier().getKind().getSchule())
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
}
