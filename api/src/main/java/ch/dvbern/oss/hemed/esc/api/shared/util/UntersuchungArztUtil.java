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

import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Bewegungsapparat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Blutdruck;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Einbeinhuepfen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Einbeinstand;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Fingeropposition;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Haut;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Herzauskultation;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Motorik;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.MundAndZaehne;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Sprachfaehigkeit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Verhalten;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import lombok.experimental.UtilityClass;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@UtilityClass
public class UntersuchungArztUtil {

    @Nullable
    public static Verhalten getVerhalten(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungArztBeurteilung arztBeurteilung,
        @NonNull Verhalten defaultWert
    ) {
        if (config.getUntersuchungVerhalten().isVisible()) {
            if (arztBeurteilung != null && arztBeurteilung.getVerhalten() != null) {
                return arztBeurteilung.getVerhalten();
            }
            return defaultWert;
        }
        return null;
    }

    @Nullable
    public static Blutdruck getBlutdruck(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungArztBeurteilung arztBeurteilung,
        @NonNull Blutdruck defaultWert
    ) {
        if (config.getUntersuchungBlutdruck().isVisible()) {
            if (arztBeurteilung != null && arztBeurteilung.getBlutdruck() != null) {
                return arztBeurteilung.getBlutdruck();
            }
            return defaultWert;
        }
        return null;
    }

    @Nullable
    public static Herzauskultation getHerzauskultation(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungArztBeurteilung arztBeurteilung,
        @NonNull Herzauskultation defaultWert
    ) {
        if (config.getUntersuchungHerz().isVisible()) {
            if (arztBeurteilung != null && arztBeurteilung.getHerzauskultation() != null) {
                return arztBeurteilung.getHerzauskultation();
            }
            return defaultWert;
        }
        return null;
    }

    @Nullable
    public static Haut getHaut(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungArztBeurteilung arztBeurteilung,
        @NonNull Haut defaultWert
    ) {
        if (config.getUntersuchungHaut().isVisible()) {
            if (arztBeurteilung != null && arztBeurteilung.getHaut() != null) {
                return arztBeurteilung.getHaut();
            }
            return defaultWert;
        }
        return null;
    }

    @NonNull
    public static EnumSet<MundAndZaehne> getMundAndZaehne(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungArztBeurteilung arztBeurteilung,
        @NonNull MundAndZaehne defaultWert
    ) {
        if (config.getUntersuchungMundZaehne().isVisible()) {
            if (arztBeurteilung != null && !arztBeurteilung.getMundAndZaehne().isEmpty()) {
                return arztBeurteilung.getMundAndZaehne();
            }
            return EnumSet.of(defaultWert);
        }
        return EnumSet.noneOf(MundAndZaehne.class);
    }

    @NonNull
    public static EnumSet<Sprachfaehigkeit> getSprachfaehigkeit(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungArztBeurteilung arztBeurteilung,
        @NonNull Sprachfaehigkeit defaultWert
    ) {
        if (config.getUntersuchungSprachfaehigkeitMultiValue().isVisible()) {
            if (arztBeurteilung != null && !arztBeurteilung.getSprachfaehigkeitMultiValue().isEmpty()) {
                return arztBeurteilung.getSprachfaehigkeitMultiValue();
            }
            return EnumSet.of(defaultWert);
        }
        return EnumSet.noneOf(Sprachfaehigkeit.class);
    }

    @Nullable
    public static Motorik getMotorik(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungArztBeurteilung arztBeurteilung,
        @NonNull Motorik defaultWert
    ) {
        if (config.getUntersuchungMotorik().isVisible()) {
            if (arztBeurteilung != null && arztBeurteilung.getMotorik() != null) {
                return arztBeurteilung.getMotorik();
            }
            return defaultWert;
        }
        return null;
    }

    @NonNull
    public static EnumSet<Bewegungsapparat> getBewegungsapparat(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungArztBeurteilung arztBeurteilung,
        @NonNull Bewegungsapparat defaultWert
    ) {
        if (config.getUntersuchungBewegungsapparat().isVisible()) {
            if (arztBeurteilung != null && !arztBeurteilung.getBewegungsapparat().isEmpty()) {
                return arztBeurteilung.getBewegungsapparat();
            }
            return EnumSet.of(defaultWert);
        }
        return EnumSet.noneOf(Bewegungsapparat.class);
    }

    @Nullable
    public static Einbeinstand getEinbeinstand(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungArztBeurteilung arztBeurteilung,
        @NonNull Einbeinstand defaultWert
    ) {
        if (config.getUntersuchungEinbeinstand().isVisible()) {
            if (arztBeurteilung != null && arztBeurteilung.getEinbeinstand() != null) {
                return arztBeurteilung.getEinbeinstand();
            }
            return defaultWert;
        }
        return null;
    }

    @Nullable
    public static Einbeinhuepfen getEinbeinhuepfen(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungArztBeurteilung arztBeurteilung,
        @NonNull Einbeinhuepfen defaultWert
    ) {
        if (config.getUntersuchungEinbeinhuepfen().isVisible()) {
            if (arztBeurteilung != null && arztBeurteilung.getEinbeinhuepfen() != null) {
                return arztBeurteilung.getEinbeinhuepfen();
            }
            return defaultWert;
        }
        return null;
    }

    @NonNull
    public static EnumSet<Fingeropposition> getFingeropposition(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungArztBeurteilung arztBeurteilung,
        @NonNull Fingeropposition defaultWert
    ) {
        if (config.getUntersuchungFingeropposition().isVisible()) {
            if (arztBeurteilung != null && !arztBeurteilung.getFingeropposition().isEmpty()) {
                return arztBeurteilung.getFingeropposition();
            }
            return EnumSet.of(defaultWert);
        }
        return EnumSet.noneOf(Fingeropposition.class);
    }
}
