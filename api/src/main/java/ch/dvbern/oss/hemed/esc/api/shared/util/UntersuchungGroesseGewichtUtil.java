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

import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileBmi;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileGewicht;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.types.PerzentileGroesse;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.UntersuchungGroesseGewicht;
import lombok.experimental.UtilityClass;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@UtilityClass
public class UntersuchungGroesseGewichtUtil {

    @Nullable
    public static PerzentileGroesse getGroessePerzentile(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungGroesseGewicht groesseGewicht,
        @NonNull PerzentileGroesse defaultWert
    ) {
        if (config.getGroesseGewichtGroessePerzentile().isVisible()) {
            if (groesseGewicht != null && groesseGewicht.getGroessePerzentile() != null) {
                return groesseGewicht.getGroessePerzentile();
            }
            return defaultWert;
        }
        return null;
    }

    @Nullable
    public static PerzentileGewicht getGewichtPerzentile(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungGroesseGewicht groesseGewicht,
        @NonNull PerzentileGewicht defaultWert
    ) {
        if (config.getGroesseGewichtGewichtPerzentile().isVisible()) {
            if (groesseGewicht != null && groesseGewicht.getGewichtPerzentile() != null) {
                return groesseGewicht.getGewichtPerzentile();
            }
            return defaultWert;
        }
        return null;
    }

    @Nullable
    public static PerzentileBmi getBmiPerzentile(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungGroesseGewicht groesseGewicht,
        @NonNull PerzentileBmi defaultWert
    ) {
        if (config.getGroesseGewichtBmiPerzentile().isVisible()) {
            if (groesseGewicht != null && groesseGewicht.getBmiPerzentile() != null) {
                return groesseGewicht.getBmiPerzentile();
            }
            return defaultWert;
        }
        return null;
    }
}
