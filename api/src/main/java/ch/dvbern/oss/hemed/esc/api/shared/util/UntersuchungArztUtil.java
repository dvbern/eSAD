package ch.dvbern.oss.hemed.esc.api.shared.util;

import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArzt;
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
import lombok.experimental.UtilityClass;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@UtilityClass
public class UntersuchungArztUtil {

    @Nullable
    public static Verhalten getVerhalten(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungArzt arzt,
        @NonNull Verhalten defaultWert
    ) {
        if (config.getUntersuchungVerhalten().isVisible()) {
            if (arzt != null && arzt.getVerhalten() != null) {
                return arzt.getVerhalten();
            }
            return defaultWert;
        }
        return null;
    }

    @Nullable
    public static Blutdruck getBlutdruck(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungArzt arzt,
        @NonNull Blutdruck defaultWert
    ) {
        if (config.getUntersuchungBlutdruck().isVisible()) {
            if (arzt != null && arzt.getBlutdruck() != null) {
                return arzt.getBlutdruck();
            }
            return defaultWert;
        }
        return null;
    }

    @Nullable
    public static Herzauskultation getHerzauskultation(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungArzt arzt,
        @NonNull Herzauskultation defaultWert
    ) {
        if (config.getUntersuchungHerz().isVisible()) {
            if (arzt != null && arzt.getHerzauskultation() != null) {
                return arzt.getHerzauskultation();
            }
            return defaultWert;
        }
        return null;
    }

    @Nullable
    public static Haut getHaut(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungArzt arzt,
        @NonNull Haut defaultWert
    ) {
        if (config.getUntersuchungHaut().isVisible()) {
            if (arzt != null && arzt.getHaut() != null) {
                return arzt.getHaut();
            }
            return defaultWert;
        }
        return null;
    }

    @Nullable
    public static List<MundAndZaehne> getMundAndZaehne(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungArzt arzt,
        @NonNull MundAndZaehne defaultWert
    ) {
        if (config.getUntersuchungMundZaehne().isVisible()) {
            if (arzt != null && !arzt.getMundAndZaehne().isEmpty()) {
                return arzt.getMundAndZaehne();
            }
            return List.of(defaultWert);
        }
        return null;
    }

    @Nullable
    public static List<Sprachfaehigkeit> getSprachfaehigkeit(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungArzt arzt,
        @NonNull Sprachfaehigkeit defaultWert
    ) {
        if (config.getUntersuchungSprache().isVisible()) {
            if (arzt != null && !arzt.getSprachfaehigkeit().isEmpty()) {
                return arzt.getSprachfaehigkeit();
            }
            return List.of(defaultWert);
        }
        return null;
    }

    @Nullable
    public static Motorik getMotorik(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungArzt arzt,
        @NonNull Motorik defaultWert
    ) {
        if (config.getUntersuchungMotorik().isVisible()) {
            if (arzt != null && arzt.getMotorik() != null) {
                return arzt.getMotorik();
            }
            return defaultWert;
        }
        return null;
    }

    @Nullable
    public static List<Bewegungsapparat> getBewegungsapparat(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungArzt arzt,
        @NonNull Bewegungsapparat defaultWert
    ) {
        if (config.getUntersuchungBewegungsapparat().isVisible()) {
            if (arzt != null && !arzt.getBewegungsapparat().isEmpty()) {
                return arzt.getBewegungsapparat();
            }
            return List.of(defaultWert);
        }
        return null;
    }

    @Nullable
    public static Einbeinstand getEinbeinstand(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungArzt arzt,
        @NonNull Einbeinstand defaultWert
    ) {
        if (config.getUntersuchungEinbeinstand().isVisible()) {
            if (arzt != null && arzt.getEinbeinstand() != null) {
                return arzt.getEinbeinstand();
            }
            return defaultWert;
        }
        return null;
    }

    @Nullable
    public static Einbeinhuepfen getEinbeinhuepfen(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungArzt arzt,
        @NonNull Einbeinhuepfen defaultWert
    ) {
        if (config.getUntersuchungEinbeinhuepfen().isVisible()) {
            if (arzt != null && arzt.getEinbeinhuepfen() != null) {
                return arzt.getEinbeinhuepfen();
            }
            return defaultWert;
        }
        return null;
    }

    @Nullable
    public static List<Fingeropposition> getFingeropposition(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungArzt arzt,
        @NonNull Fingeropposition defaultWert
    ) {
        if (config.getUntersuchungFingeropposition().isVisible()) {
            if (arzt != null && !arzt.getFingeropposition().isEmpty()) {
                return arzt.getFingeropposition();
            }
            return List.of(defaultWert);
        }
        return null;
    }
}
