package ch.dvbern.oss.hemed.esc.api.shared.util;

import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassConfig;
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

    @Nullable
    public static List<MundAndZaehne> getMundAndZaehne(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungArztBeurteilung arztBeurteilung,
        @NonNull MundAndZaehne defaultWert
    ) {
        if (config.getUntersuchungMundZaehne().isVisible()) {
            if (arztBeurteilung != null && !arztBeurteilung.getMundAndZaehne().isEmpty()) {
                return arztBeurteilung.getMundAndZaehne();
            }
            return List.of(defaultWert);
        }
        return null;
    }

    @Nullable
    public static List<Sprachfaehigkeit> getSprachfaehigkeit(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungArztBeurteilung arztBeurteilung,
        @NonNull Sprachfaehigkeit defaultWert
    ) {
        if (config.getUntersuchungSprache().isVisible()) {
            if (arztBeurteilung != null && !arztBeurteilung.getSprachfaehigkeit().isEmpty()) {
                return arztBeurteilung.getSprachfaehigkeit();
            }
            return List.of(defaultWert);
        }
        return null;
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

    @Nullable
    public static List<Bewegungsapparat> getBewegungsapparat(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungArztBeurteilung arztBeurteilung,
        @NonNull Bewegungsapparat defaultWert
    ) {
        if (config.getUntersuchungBewegungsapparat().isVisible()) {
            if (arztBeurteilung != null && !arztBeurteilung.getBewegungsapparat().isEmpty()) {
                return arztBeurteilung.getBewegungsapparat();
            }
            return List.of(defaultWert);
        }
        return null;
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

    @Nullable
    public static List<Fingeropposition> getFingeropposition(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungArztBeurteilung arztBeurteilung,
        @NonNull Fingeropposition defaultWert
    ) {
        if (config.getUntersuchungFingeropposition().isVisible()) {
            if (arztBeurteilung != null && !arztBeurteilung.getFingeropposition().isEmpty()) {
                return arztBeurteilung.getFingeropposition();
            }
            return List.of(defaultWert);
        }
        return null;
    }
}
