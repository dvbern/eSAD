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

package ch.dvbern.oss.hemed.esc.api.shared.util;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Objects;

import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.json.KindSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import com.google.common.hash.Hashing;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@UtilityClass
@Slf4j
public class HashValueGeneratorUtil {

    public static boolean needsNewHashValue(@NonNull Kind kind, @NonNull KindSaveJson saveJson) {
        return !Objects.equals(kind.getName(), saveJson.getName())
            || !Objects.equals(kind.getVorname(), saveJson.getVorname())
            || kind.getGeschlecht() != saveJson.getGeschlecht()
            || !Objects.equals(kind.getGeburtsdatum(), saveJson.getGeburtsdatum());
    }

    public static String generateHashValueForKindValues(
        @NonNull String nachname,
        @NonNull String vorname,
        @NonNull Geschlecht geschlecht,
        @NonNull LocalDate geburtsdatum
    ) {
        String normalized = StringNormalizerUtil.normalizeAndStripDiarcritics(nachname)
            + StringNormalizerUtil.normalizeAndStripDiarcritics(vorname)
            + geschlecht.name()
            + geburtsdatum.toString();

        return Hashing.sha256().hashString(
                normalized.toLowerCase(),
                StandardCharsets.UTF_8)
            .toString();
    }

}
