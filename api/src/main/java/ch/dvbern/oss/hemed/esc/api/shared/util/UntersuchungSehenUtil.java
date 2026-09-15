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

import java.math.BigDecimal;
import java.util.EnumSet;

import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.UntersuchungSehenBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.BrilleLinsenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.DistanzSehenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.FarbsehenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.InspektionKonvergenzResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.MotilitaetResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StereotestResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StrabismusAuftretenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StrabismusAuspraegungResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StrabismusResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.VerwendetTafelResultat;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.MultiValueEnhanced;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.SingleValueEnhanced;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@UtilityClass
public class UntersuchungSehenUtil {

    @Nullable
    public SingleValueEnhanced<BrilleLinsenResultat> getBrilleLinsenDefault(
        @NonNull AnlassConfig config,
        @Nullable BrilleLinsenResultat defaultWert
    ) {
        if (config.getSehenBrilleLinsen().isVisible() && defaultWert != null) {
            return SingleValueEnhanced.of(defaultWert, null);
        }
        return null;
    }

    @Nullable
    public static SingleValueEnhanced<StereotestResultat> getStereoTestDefault(
        @NonNull AnlassConfig config,
        @Nullable StereotestResultat defaultWert
    ) {
        if (config.getSehenStereoTest().isVisible()) {
            return SingleValueEnhanced.of(defaultWert, null);
        }
        return null;
    }

    @Nullable
    public static SingleValueEnhanced<FarbsehenResultat> getFarbsehenDefault(
        @NonNull AnlassConfig config,
        @Nullable FarbsehenResultat defaultWert
    ) {
        if (config.getSehenFarbsehen().isVisible()) {
            return SingleValueEnhanced.of(defaultWert, null);
        }
        return null;
    }

    @Nullable
    public static SingleValueEnhanced<VerwendetTafelResultat> getVerwendeteTafelDefault(
        @NonNull AnlassConfig config,
        @Nullable VerwendetTafelResultat defaultWert
    ) {
        if (config.getSehenTafel().isVisible()) {
            return SingleValueEnhanced.of(defaultWert, null);
        }
        return null;
    }

    @NonNull
    public static MultiValueEnhanced<DistanzSehenResultat> getFernvisusDefault(
        @NonNull AnlassConfig config,
        @NonNull DistanzSehenResultat defaultWert
    ) {
        if (config.getSehenFernvisus().isVisible()) {
            return MultiValueEnhanced.of(EnumSet.of(defaultWert), null);
        }
        return MultiValueEnhanced.of(EnumSet.noneOf(DistanzSehenResultat.class), null);
    }

    @Nullable
    public static SingleValueEnhanced<DistanzSehenResultat> getFernvisusAugeEinzelnDefault(
        @NonNull AnlassConfig config,
        @NonNull DistanzSehenResultat defaultWert
    ) {
        if (config.getSehenFernvisusAugeEinzeln().isVisible()) {
            return SingleValueEnhanced.of(defaultWert, null);
        }
        return null;
    }

    @Nullable
    public static BigDecimal getFernvisusRechts(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungSehenBeurteilung sehen,
        @Nullable BigDecimal defaultWert
    ) {
        if (config.getSehenFernvisus().isVisible()) {
            if (sehen != null && CollectionUtils.isNotEmpty(ValueEnhancedUtil.getEnumSetOrEmpty(
                sehen.getFernvisus(),
                DistanzSehenResultat.class))
            ) {
                return sehen.getFernvisusRechts();
            }
            return defaultWert;
        }
        return null;
    }

    @Nullable
    public static BigDecimal getFernvisusLinks(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungSehenBeurteilung sehen,
        @Nullable BigDecimal defaultWert
    ) {
        if (config.getSehenFernvisus().isVisible()) {
            if (sehen != null && CollectionUtils.isNotEmpty(ValueEnhancedUtil.getEnumSetOrEmpty(
                sehen.getFernvisus(),
                DistanzSehenResultat.class))
            ) {
                return sehen.getFernvisusLinks();
            }
            return defaultWert;
        }
        return null;
    }

    @NonNull
    public static MultiValueEnhanced<DistanzSehenResultat> getNahvisusDefault(
        @NonNull AnlassConfig config,
        @NonNull DistanzSehenResultat defaultWert) {
        if (config.getSehenNahvisus().isVisible()) {
            return MultiValueEnhanced.of(EnumSet.of(defaultWert), null);
        }
        return MultiValueEnhanced.of(EnumSet.noneOf(DistanzSehenResultat.class), null);
    }

    @Nullable
    public static SingleValueEnhanced<MotilitaetResultat> getMotilitaetDefault(
        @NonNull AnlassConfig config,
        @Nullable MotilitaetResultat defaultWert
    ) {
        if (config.getSehenMotilitaet().isVisible()) {
            return SingleValueEnhanced.of(defaultWert, null);
        }
        return null;
    }

    @NonNull
    public static MultiValueEnhanced<StrabismusResultat> getStrabismusDefault(
        @NonNull AnlassConfig config,
        @NonNull StrabismusResultat defaultWert) {
        if (config.getSehenStrabismus().isVisible()) {
            return MultiValueEnhanced.of(EnumSet.of(defaultWert), null);
        }
        return MultiValueEnhanced.of(EnumSet.noneOf(StrabismusResultat.class), null);
    }

    @Nullable
    public static SingleValueEnhanced<StrabismusAuspraegungResultat> getStrabismusAuspraegung(
        @NonNull AnlassConfig config,
        @Nullable StrabismusAuspraegungResultat defaultWert
    ) {
        if (config.getSehenStrabismusAuspraegung().isVisible()) {
            return SingleValueEnhanced.of(defaultWert, null);
        }
        return null;
    }

    @Nullable
    public static SingleValueEnhanced<StrabismusAuftretenResultat> getStrabismusAuftreten(
        @NonNull AnlassConfig config,
        @Nullable StrabismusAuftretenResultat defaultWert
    ) {
        if (config.getSehenStrabismusAuftreten().isVisible()) {
            return SingleValueEnhanced.of(defaultWert, null);
        }
        return null;
    }

    @Nullable
    public static SingleValueEnhanced<InspektionKonvergenzResultat> getInspektion(
        @NonNull AnlassConfig config,
        @Nullable InspektionKonvergenzResultat defaultWert
    ) {
        if (config.getSehenInspektion().isVisible()) {
            return SingleValueEnhanced.of(defaultWert, null);
        }
        return null;
    }

    @Nullable
    public static SingleValueEnhanced<InspektionKonvergenzResultat> getKonvergenz(
        @NonNull AnlassConfig config,
        @Nullable InspektionKonvergenzResultat defaultWert
    ) {
        if (config.getSehenKonvergenz().isVisible()) {
            return SingleValueEnhanced.of(defaultWert, null);
        }
        return null;
    }
}
