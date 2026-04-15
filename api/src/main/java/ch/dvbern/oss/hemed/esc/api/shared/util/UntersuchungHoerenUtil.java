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
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.shared.valueenhanced.MultiValueEnhanced;
import lombok.experimental.UtilityClass;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@UtilityClass
public class UntersuchungHoerenUtil {

    @NonNull
    public static MultiValueEnhanced<AudiogrammBeurteilung> getAudiogrammBeurteilungDefault(
        @NonNull RequirementSetting config,
        @NonNull AudiogrammBeurteilung defaultWert
    ) {
        if (config.isVisible()) {
            return MultiValueEnhanced.of(EnumSet.of(defaultWert), null);
        }
        return MultiValueEnhanced.of(EnumSet.noneOf(AudiogrammBeurteilung.class), null);
    }

    @NonNull
    public static MultiValueEnhanced<HoerenZusaetzlicheAngaben> getZusaetzlicheAngabenDefault(
        @NonNull RequirementSetting config,
        @NonNull HoerenZusaetzlicheAngaben defaultWert
    ) {
        if (config.isVisible()) {
            return MultiValueEnhanced.of(EnumSet.of(defaultWert), null);
        }
        return MultiValueEnhanced.of(EnumSet.noneOf(HoerenZusaetzlicheAngaben.class), null);
    }

    @NonNull
    public static MultiValueEnhanced<OtoskopieResultat> getOtoskopieDefault(
        @NonNull RequirementSetting config,
        @Nullable OtoskopieResultat defaultWert
    ) {
        if (config.isVisible() && defaultWert != null) {
            return MultiValueEnhanced.of(EnumSet.of(defaultWert), null);
        }
        return MultiValueEnhanced.of(EnumSet.noneOf(OtoskopieResultat.class), null);
    }
}
