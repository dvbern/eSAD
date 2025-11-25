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

import ch.dvbern.oss.hemed.esc.api.features.anlass.durchfuehrung.UntersuchungDurchfuehrung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import lombok.experimental.UtilityClass;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@UtilityClass
public class UntersuchungDurchfuehrungUtil {

    @Nullable
    public static Benutzer getArzt(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungDurchfuehrung durchfuehrung,
        @Nullable Benutzer defaultWert
    ) {
        if (config.getDatenUntersuchung().isVisible()) {
            if (durchfuehrung != null && durchfuehrung.getArzt() != null) {
                return durchfuehrung.getArzt();
            }
            return defaultWert;
        }
        return null;
    }

    @Nullable
    public static Benutzer getMpa(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungDurchfuehrung durchfuehrung,
        @Nullable Benutzer defaultWert
    ) {
        if (config.getDatenUntersuchung().isVisible()) {
            if (durchfuehrung != null && durchfuehrung.getMpa() != null) {
                return durchfuehrung.getMpa();
            }
            return defaultWert;
        }
        return null;
    }
}
