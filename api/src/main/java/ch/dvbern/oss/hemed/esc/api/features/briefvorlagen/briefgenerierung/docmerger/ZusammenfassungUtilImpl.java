package ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.briefgenerierung.docmerger;

import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungSehen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.Audiogramm;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.AudiogrammBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.Dezibel;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.HoerenZusaetzlicheAngaben;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.OtoskopieResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenAudiogramm;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenOtoskopie;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.BrilleLinsenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.DistanzSehenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.FarbsehenResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.InspektionKonvergenzResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.MotilitaetResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.StereotestResultat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.StrabismusResultat;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class ZusammenfassungUtilImpl {

    private final AnlassConfig anlassConfig;
    private final UntersuchungSehen untersuchungSehen;
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
        this.untersuchungSehen = untersuchung.getUntersuchungSehen() != null
            ? untersuchung.getUntersuchungSehen()
            : new UntersuchungSehen();
    }

    public boolean isStereotest(@NonNull StereotestResultat resultat) {
        return !anlassConfig.getSehenStereoTest().isVisible() || untersuchungSehen.getStereotest() == resultat;
    }

    public boolean isFarbsehen(@NonNull FarbsehenResultat resultat) {
        return !anlassConfig.getSehenFarbsehen().isVisible() || untersuchungSehen.getFarbsehen() == resultat;
    }

    public boolean isFernvisus(@NonNull DistanzSehenResultat resultat) {
        return !anlassConfig.getSehenFernvisus().isVisible() || untersuchungSehen.getFernvisus()
            .stream()
            .allMatch(value -> value == resultat);
    }

    public boolean isNahvisus(@NonNull DistanzSehenResultat resultat) {
        return !anlassConfig.getSehenNahvisus().isVisible() || untersuchungSehen.getNahvisus()
            .stream()
            .allMatch(value -> value == resultat);
    }

    public boolean isStrabismus(@NonNull StrabismusResultat resultat) {
        return !anlassConfig.getSehenStrabismus().isVisible() || untersuchungSehen.getStrabismus()
            .stream()
            .allMatch(value -> value == resultat);
    }

    public boolean isInspektion(@NonNull InspektionKonvergenzResultat resultat) {
        return !anlassConfig.getSehenInspektion().isVisible() || untersuchungSehen.getInspektion() == resultat;
    }

    public boolean isKonvergenz(@NonNull InspektionKonvergenzResultat resultat) {
        return !anlassConfig.getSehenKonvergenz().isVisible() || untersuchungSehen.getKonvergenz() == resultat;
    }

    public boolean isAudiogramBeurteilung(@NonNull AudiogrammBeurteilung resultat) {
        return !anlassConfig.getHoerenAudiogramm().isVisible()
            || untersuchungHoerenAudiogramm.getAudiogrammBeurteilung()
            .stream()
            .allMatch(value -> value == resultat);
    }

    public boolean isHoerenZusaetzlicheAngaben(@NonNull HoerenZusaetzlicheAngaben resultat) {
        return !anlassConfig.getHoerenAudiogrammZusaetzlicheAngaben().isVisible()
            || untersuchungHoerenAudiogramm.getZusaetzlicheAngaben()
            .stream()
            .allMatch(value -> value == resultat);
    }

    public boolean isOtoskopieRechts(@NonNull OtoskopieResultat resultat) {
        return !anlassConfig.getHoerenOtoskopie().isVisible() || untersuchungHoerenOtoskopie.getOtoskopieRechts()
            .stream()
            .allMatch(value -> value == resultat);
    }

    public boolean isOtoskopieLinks(@NonNull OtoskopieResultat resultat) {
        return !anlassConfig.getHoerenOtoskopie().isVisible() || untersuchungHoerenOtoskopie.getOtoskopieLinks()
            .stream()
            .allMatch(value -> value == resultat);
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
        return !anlassConfig.getSehenBrilleLinsen().isVisible() || untersuchungSehen.getBrilleLinsen() == resultat;
    }

    public boolean isMotilitaet(@NonNull MotilitaetResultat resultat) {
        return !anlassConfig.getSehenMotilitaet().isVisible() || untersuchungSehen.getMotilitaet() == resultat;
    }
}
