package ch.dvbern.oss.hemed.esc.api.shared.util;

import java.math.BigDecimal;
import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungSehenBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.BrilleLinsenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.DistanzSehenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.FarbsehenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.InspektionKonvergenzResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.MotilitaetResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.StereotestResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.StrabismusAuftretenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.StrabismusAuspraegungResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.StrabismusResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.VerwendetTafelResultat;
import lombok.experimental.UtilityClass;
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

    @Nullable
    public static List<DistanzSehenResultat> getFernvisus(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungSehenBeurteilung sehen,
        @NonNull DistanzSehenResultat defaultWert
    ) {
        if (config.getSehenFernvisus().isVisible()) {
            if (sehen != null && !sehen.getFernvisus().isEmpty()) {
                return sehen.getFernvisus();
            }
            return List.of(defaultWert);
        }
        return null;
    }

    @Nullable
    public static BigDecimal getFernvisusRechts(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungSehenBeurteilung sehen,
        @NonNull BigDecimal defaultWert
    ) {
        if (config.getSehenFernvisus().isVisible()) {
            if (sehen != null && !sehen.getFernvisus().isEmpty()) {
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
            if (sehen != null && !sehen.getFernvisus().isEmpty()) {
                return sehen.getFernvisusLinks();
            }
            return defaultWert;
        }
        return null;
    }

    @Nullable
    public static List<DistanzSehenResultat> getNahvisus(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungSehenBeurteilung sehen,
        @NonNull DistanzSehenResultat defaultWert) {
        if (config.getSehenNahvisus().isVisible()) {
            if (sehen != null && !sehen.getNahvisus().isEmpty()) {
                return sehen.getNahvisus();
            }
            return List.of(defaultWert);
        }
        return null;
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

    @Nullable
    public static List<StrabismusResultat> getStrabismus(
        @NonNull AnlassConfig config,
        @Nullable UntersuchungSehenBeurteilung sehen,
        @NonNull StrabismusResultat defaultWert) {
        if (config.getSehenStrabismus().isVisible()) {
            if (sehen != null && !sehen.getStrabismus().isEmpty()) {
                return sehen.getStrabismus();
            }
            return List.of(defaultWert);
        }
        return null;
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
