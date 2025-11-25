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

import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.UntersuchungSehenBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
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
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@UtilityClass
public class UntersuchungSehenUtil {

    @Nullable
    public static BrilleLinsenResultat getBrilleLinsen(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungSehenBeurteilung sehen,
        @Nullable BrilleLinsenResultat defaultWert
    ) {
        if (config.getSehenBrilleLinsen().isVisible()) {
            if (sehen != null && sehen.getBrilleLinsen() != null) {
                return sehen.getBrilleLinsen();
            }
            return defaultWert;
        }
        return null;
    }

    @Nullable
    public static StereotestResultat getStereoTest(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungSehenBeurteilung sehen,
        @NonNull StereotestResultat defaultWert
    ) {
        if (config.getSehenStereoTest().isVisible()) {
            if (sehen != null && sehen.getStereotest() != null) {
                return sehen.getStereotest();
            }
            return defaultWert;
        }
        return null;
    }

    @Nullable
    public static FarbsehenResultat getFarbsehen(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungSehenBeurteilung sehen,
        @NonNull FarbsehenResultat defaultWert
    ) {
        if (config.getSehenFarbsehen().isVisible()) {
            if (sehen != null && sehen.getFarbsehen() != null) {
                return sehen.getFarbsehen();
            }
            return defaultWert;
        }
        return null;
    }

    @Nullable
    public static VerwendetTafelResultat getVerwendeteTafel(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungSehenBeurteilung sehen,
        @NonNull VerwendetTafelResultat defaultWert
    ) {
        if (config.getSehenTafel().isVisible()) {
            if (sehen != null && sehen.getVerwendeteTafel() != null) {
                return sehen.getVerwendeteTafel();
            }
            return defaultWert;
        }
        return null;
    }

    @NonNull
    public static EnumSet<DistanzSehenResultat> getFernvisus(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungSehenBeurteilung sehen,
        @NonNull DistanzSehenResultat defaultWert
    ) {
        if (config.getSehenFernvisus().isVisible()) {
            if (sehen != null && CollectionUtils.isNotEmpty(sehen.getFernvisus())) {
                return sehen.getFernvisus();
            }
            return EnumSet.of(defaultWert);
        }
        return EnumSet.noneOf(DistanzSehenResultat.class);
    }

    @Nullable
    public static BigDecimal getFernvisusRechts(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungSehenBeurteilung sehen,
        @NonNull BigDecimal defaultWert
    ) {
        if (config.getSehenFernvisus().isVisible()) {
            if (sehen != null && CollectionUtils.isNotEmpty(sehen.getFernvisus())) {
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
        @NonNull BigDecimal defaultWert
    ) {
        if (config.getSehenFernvisus().isVisible()) {
            if (sehen != null && CollectionUtils.isNotEmpty(sehen.getFernvisus())) {
                return sehen.getFernvisusLinks();
            }
            return defaultWert;
        }
        return null;
    }

    @NonNull
    public static EnumSet<DistanzSehenResultat> getNahvisus(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungSehenBeurteilung sehen,
        @NonNull DistanzSehenResultat defaultWert) {
        if (config.getSehenNahvisus().isVisible()) {
            if (sehen != null && CollectionUtils.isNotEmpty(sehen.getNahvisus())) {
                return sehen.getNahvisus();
            }
            return EnumSet.of(defaultWert);
        }
        return EnumSet.noneOf(DistanzSehenResultat.class);
    }

    @Nullable
    public static MotilitaetResultat getMotilitaet(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungSehenBeurteilung sehen,
        @NonNull MotilitaetResultat defaultWert
    ) {
        if (config.getSehenMotilitaet().isVisible()) {
            if (sehen != null && sehen.getMotilitaet() != null) {
                return sehen.getMotilitaet();
            }
            return defaultWert;
        }
        return null;
    }

    @NonNull
    public static EnumSet<StrabismusResultat> getStrabismus(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungSehenBeurteilung sehen,
        @NonNull StrabismusResultat defaultWert) {
        if (config.getSehenStrabismus().isVisible()) {
            if (sehen != null && CollectionUtils.isNotEmpty(sehen.getStrabismus())) {
                return sehen.getStrabismus();
            }
            return EnumSet.of(defaultWert);
        }
        return EnumSet.noneOf(StrabismusResultat.class);
    }

    @Nullable
    public static StrabismusAuspraegungResultat getStrabismusAuspraegung(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungSehenBeurteilung sehen,
        @NonNull StrabismusAuspraegungResultat defaultWert
    ) {
        if (config.getSehenStrabismus().isVisible()) {
            if (sehen != null && sehen.getStrabismusAuspraegung() != null) {
                return sehen.getStrabismusAuspraegung();
            }
            return defaultWert;
        }
        return null;
    }

    @Nullable
    public static StrabismusAuftretenResultat getStrabismusAuftreten(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungSehenBeurteilung sehen,
        @NonNull StrabismusAuftretenResultat defaultWert
    ) {
        if (config.getSehenStrabismus().isVisible()) {
            if (sehen != null && sehen.getStrabismusAuftreten() != null) {
                return sehen.getStrabismusAuftreten();
            }
            return defaultWert;
        }
        return null;
    }

    @Nullable
    public static InspektionKonvergenzResultat getInspektion(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungSehenBeurteilung sehen,
        @NonNull InspektionKonvergenzResultat defaultWert
    ) {
        if (config.getSehenInspektion().isVisible()) {
            if (sehen != null && sehen.getInspektion() != null) {
                return sehen.getInspektion();
            }
            return defaultWert;
        }
        return null;
    }

    @Nullable
    public static InspektionKonvergenzResultat getKonvergenz(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungSehenBeurteilung sehen,
        @NonNull InspektionKonvergenzResultat defaultWert
    ) {
        if (config.getSehenKonvergenz().isVisible()) {
            if (sehen != null && sehen.getKonvergenz() != null) {
                return sehen.getKonvergenz();
            }
            return defaultWert;
        }
        return null;
    }
}
