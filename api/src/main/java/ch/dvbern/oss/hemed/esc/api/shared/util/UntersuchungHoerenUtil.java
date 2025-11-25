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

import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.AudiogrammBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenZusaetzlicheAngaben;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.OtoskopieResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenAudiogramm;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenOtoskopie;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.types.RequirementSetting;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@UtilityClass
public class UntersuchungHoerenUtil {

    @NonNull
    public static EnumSet<AudiogrammBeurteilung> getAudiogrammBeurteilung(
        @NonNull RequirementSetting config,
        @Nullable UntersuchungHoerenAudiogramm hoeren,
        @NonNull AudiogrammBeurteilung defaultWert
    ) {
        if (config.isVisible()) {
            if (hoeren != null && CollectionUtils.isNotEmpty(hoeren.getAudiogrammBeurteilung())) {
                return hoeren.getAudiogrammBeurteilung();
            }
            return EnumSet.of(defaultWert);
        }
        return EnumSet.noneOf(AudiogrammBeurteilung.class);
    }

    @NonNull
    public static EnumSet<HoerenZusaetzlicheAngaben> getZusaetzlicheAngaben(
        @NonNull RequirementSetting config,
        @Nullable UntersuchungHoerenAudiogramm hoeren,
        @NonNull HoerenZusaetzlicheAngaben defaultWert
    ) {
        if (config.isVisible()) {
            if (hoeren != null && CollectionUtils.isNotEmpty(hoeren.getZusaetzlicheAngaben())) {
                return hoeren.getZusaetzlicheAngaben();
            }
            return EnumSet.of(defaultWert);
        }
        return EnumSet.noneOf(HoerenZusaetzlicheAngaben.class);
    }

    @NonNull
    public static EnumSet<OtoskopieResultat> getOtoskopieRechts(
        @NonNull RequirementSetting config,
        @Nullable UntersuchungHoerenOtoskopie hoeren,
        @Nullable OtoskopieResultat defaultWert
    ) {
        if (config.isVisible()) {
            if (hoeren != null && CollectionUtils.isNotEmpty(hoeren.getOtoskopieRechts())) {
                return hoeren.getOtoskopieRechts();
            }
            if (defaultWert != null) {
                return EnumSet.of(defaultWert);
            }
        }
        return EnumSet.noneOf(OtoskopieResultat.class);
    }

    @NonNull
    public static EnumSet<OtoskopieResultat> getOtoskopieLinks(
        @NonNull RequirementSetting config,
        @Nullable UntersuchungHoerenOtoskopie hoeren,
        @Nullable OtoskopieResultat defaultWert
    ) {
        if (config.isVisible()) {
            if (hoeren != null && CollectionUtils.isNotEmpty(hoeren.getOtoskopieLinks())) {
                return hoeren.getOtoskopieLinks();
            }
            if (defaultWert != null) {
                return EnumSet.of(defaultWert);
            }
        }
        return EnumSet.noneOf(OtoskopieResultat.class);
    }
}
