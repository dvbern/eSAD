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

import java.util.EnumSet;

import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.laeusekontrolle.types.LaeusekontrolleBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.laeusekontrolle.UntersuchungLaeusekontrolle;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@UtilityClass
public class UntersuchungLaeusekontrolleUtil {

    @NonNull
    public static EnumSet<LaeusekontrolleBeurteilung> getLaeusekontrolleBeurteilung(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungLaeusekontrolle groesseGewicht,
        @NonNull LaeusekontrolleBeurteilung defaultWert
    ) {
        if (config.getLaeusekontrolleBeurteilung().isVisible()) {
            if (groesseGewicht != null && CollectionUtils.isNotEmpty(groesseGewicht.getBeurteilung())) {
                return groesseGewicht.getBeurteilung();
            }
            return EnumSet.of(defaultWert);
        }
        return EnumSet.noneOf(LaeusekontrolleBeurteilung.class);
    }
}
