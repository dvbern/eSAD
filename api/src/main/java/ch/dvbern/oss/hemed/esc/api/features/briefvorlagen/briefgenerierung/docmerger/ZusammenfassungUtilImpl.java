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

package ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.briefgenerierung.docmerger;

import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.UntersuchungSehenBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.Audiogramm;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.AudiogrammBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.Dezibel;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenZusaetzlicheAngaben;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.OtoskopieResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenAudiogramm;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenOtoskopie;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.BrilleLinsenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.DistanzSehenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.FarbsehenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.InspektionKonvergenzResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.MotilitaetResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StereotestResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StrabismusResultat;
import ch.dvbern.oss.hemed.esc.api.shared.util.ValueEnhancedUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class ZusammenfassungUtilImpl {

    private final AnlassConfig anlassConfig;
    private final UntersuchungSehenBeurteilung untersuchungSehenBeurteilung;
    private final UntersuchungHoerenAudiogramm untersuchungHoerenAudiogramm;
    private final UntersuchungHoerenOtoskopie untersuchungHoerenOtoskopie;

    public ZusammenfassungUtilImpl(
        @NonNull AnlassConfig anlassConfig,
        @NonNull Untersuchung untersuchung
    ) {
        this.anlassConfig = anlassConfig;
        this.untersuchungHoerenAudiogramm = untersuchung.getUntersuchungHoerenAudiogramm() != null
            ? untersuchung.getUntersuchungHoerenAudiogramm()
            : new UntersuchungHoerenAudiogramm();
        this.untersuchungHoerenOtoskopie = untersuchung.getUntersuchungHoerenOtoskopie() != null ?
            untersuchung.getUntersuchungHoerenOtoskopie() : new UntersuchungHoerenOtoskopie();
        this.untersuchungSehenBeurteilung = untersuchung.getUntersuchungSehenBeurteilung() != null
            ? untersuchung.getUntersuchungSehenBeurteilung()
            : new UntersuchungSehenBeurteilung();
    }

    public boolean isStereotest(@NonNull StereotestResultat resultat) {
        return !anlassConfig.getSehenStereoTest().isVisible()
            || ValueEnhancedUtil.getEnumValueOrNull(
            untersuchungSehenBeurteilung.getStereotest(),
            StereotestResultat.class) == resultat;
    }

    public boolean isFarbsehen(@NonNull FarbsehenResultat resultat) {
        return !anlassConfig.getSehenFarbsehen().isVisible()
            || ValueEnhancedUtil.getEnumValueOrNull(
            untersuchungSehenBeurteilung.getFarbsehen(),
            FarbsehenResultat.class) == resultat;
    }

    public boolean isFernvisus(@NonNull DistanzSehenResultat resultat) {
        return !anlassConfig.getSehenFernvisus().isVisible() || ValueEnhancedUtil.getEnumSetOrEmpty(
            untersuchungSehenBeurteilung.getFernvisus(),
            DistanzSehenResultat.class).stream().allMatch(value -> value == resultat);

    }

    public boolean isNahvisus(@NonNull DistanzSehenResultat resultat) {
        return !anlassConfig.getSehenNahvisus().isVisible() || ValueEnhancedUtil.getEnumSetOrEmpty(
            untersuchungSehenBeurteilung.getNahvisus(),
            DistanzSehenResultat.class).stream().allMatch(value -> value == resultat);
    }

    public boolean isStrabismus(@NonNull StrabismusResultat resultat) {
        return !anlassConfig.getSehenStrabismus().isVisible() || ValueEnhancedUtil.getEnumSetOrEmpty(
            untersuchungSehenBeurteilung.getStrabismus(),
            StrabismusResultat.class).stream().allMatch(value -> value == resultat);
    }

    public boolean isInspektion(@NonNull InspektionKonvergenzResultat resultat) {
        return !anlassConfig.getSehenInspektion().isVisible()
            || ValueEnhancedUtil.getEnumValueOrNull(
            untersuchungSehenBeurteilung.getInspektion(),
            InspektionKonvergenzResultat.class) == resultat;
    }

    public boolean isKonvergenz(@NonNull InspektionKonvergenzResultat resultat) {
        return !anlassConfig.getSehenKonvergenz().isVisible()
            || ValueEnhancedUtil.getEnumValueOrNull(
            untersuchungSehenBeurteilung.getKonvergenz(),
            InspektionKonvergenzResultat.class) == resultat;
    }

    public boolean isAudiogramBeurteilung(@NonNull AudiogrammBeurteilung resultat) {
        return !anlassConfig.getHoerenAudiogramm().isVisible()
            || ValueEnhancedUtil.getEnumSetOrEmpty(
            untersuchungHoerenAudiogramm.getAudiogrammBeurteilung(),
            AudiogrammBeurteilung.class).contains(resultat);
    }

    public boolean isHoerenZusaetzlicheAngaben(@NonNull HoerenZusaetzlicheAngaben resultat) {
        return !anlassConfig.getHoerenAudiogrammZusaetzlicheAngaben().isVisible()
            || ValueEnhancedUtil.getEnumSetOrEmpty(
            untersuchungHoerenAudiogramm.getZusaetzlicheAngaben(),
            HoerenZusaetzlicheAngaben.class).contains(resultat);
    }

    public boolean isOtoskopieRechts(@NonNull OtoskopieResultat resultat) {
        return !anlassConfig.getHoerenOtoskopie().isVisible() ||
            ValueEnhancedUtil.getEnumSetOrEmpty(
                untersuchungHoerenOtoskopie.getOtoskopieRechts(),
                OtoskopieResultat.class).stream().allMatch(value -> value == resultat);
    }

    public boolean isOtoskopieLinks(@NonNull OtoskopieResultat resultat) {
        return !anlassConfig.getHoerenOtoskopie().isVisible() || ValueEnhancedUtil.getEnumSetOrEmpty(
            untersuchungHoerenOtoskopie.getOtoskopieLinks(),
            OtoskopieResultat.class).stream().allMatch(value -> value == resultat);
    }

    public boolean isNormalAudiogramm(@Nullable Audiogramm audiogramm) {
        if (audiogramm == null) {
            return true;
        }
        return (!anlassConfig.getHoerenAudiogramm250Hz().isVisible() || audiogramm.getDbHz250() == Dezibel.DB20)
            && audiogramm.getDbHz500() == Dezibel.DB20
            && audiogramm.getDbHz1000() == Dezibel.DB20
            && audiogramm.getDbHz2000() == Dezibel.DB20
            && audiogramm.getDbHz4000() == Dezibel.DB20
            && audiogramm.getDbHz6000() == Dezibel.DB20
            && (!anlassConfig.getHoerenAudiogramm8000Hz().isVisible() || audiogramm.getDbHz8000() == Dezibel.DB20);
    }

    public boolean isBrilleLinsen(@NonNull BrilleLinsenResultat resultat) {
        return !anlassConfig.getSehenBrilleLinsen().isVisible()
            || ValueEnhancedUtil.getEnumValueOrNull(
            untersuchungSehenBeurteilung.getBrilleLinsen(),
            BrilleLinsenResultat.class) == resultat;
    }

    public boolean isMotilitaet(@NonNull MotilitaetResultat resultat) {
        return !anlassConfig.getSehenMotilitaet().isVisible()
            || ValueEnhancedUtil.getEnumValueOrNull(
            untersuchungSehenBeurteilung.getMotilitaet(),
            MotilitaetResultat.class) == resultat;
    }
}
