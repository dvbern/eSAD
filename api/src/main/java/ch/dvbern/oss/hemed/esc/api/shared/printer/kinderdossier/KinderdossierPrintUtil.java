package ch.dvbern.oss.hemed.esc.api.shared.printer.kinderdossier;

import java.util.function.Function;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungDurchfuehrung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungKindDaten;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungSehenBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztBemerkung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.UntersuchungGroesseGewicht;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.Audiogramm;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenAudiogramm;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenOtoskopie;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungImpfungenMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.shared.anlass.anlassflags.AnlassFlags;
import ch.dvbern.oss.hemed.esc.api.shared.printer.ExtractorUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil.PLACEHOLDER;

public final class KinderdossierPrintUtil {

    private KinderdossierPrintUtil() {
        // utility class
    }

    @NonNull
    public static String getImpfungInternChargeName(@NonNull UntersuchungImpfungIntern untersuchungImpfungIntern) {
        if (untersuchungImpfungIntern.getCharge() == null) {
            return PLACEHOLDER;
        }
        return untersuchungImpfungIntern.getCharge().getName();
    }

    @NonNull
    public static String extractKindKlasseDaten(
        @NonNull Kind kind,
        @NonNull Function<Klasse, String> stringTransformer
    ) {
        return ExtractorUtil.getKindKlasseDatenOptional(kind)
            .map(stringTransformer)
            .orElse(PLACEHOLDER);
    }

    @NonNull
    public static String extractKindSchuleDaten(
        @NonNull Kind kind,
        @NonNull Function<Schule, String> stringTransformer
    ) {
        return ExtractorUtil.getKindSchuleDatenOptional(kind)
            .map(stringTransformer)
            .orElse(PLACEHOLDER);
    }

    @NonNull
    public static String extractUntersuchungKindDaten(
        @NonNull Einladung einladung,
        @NonNull Function<UntersuchungKindDaten, String> stringTransformer
    ) {
        return ExtractorUtil.getUntersuchungKindDatenOptional(einladung)
            .map(stringTransformer)
            .orElse(PLACEHOLDER);
    }

    @Nullable
    public static Boolean extractUntersuchungDurchfuehrungBooleanFn(
        @NonNull Einladung einladung,
        @NonNull Function<UntersuchungDurchfuehrung, Boolean> booleanFunction
    ) {
        return ExtractorUtil.getUntersuchungDurchfuehrungOptional(einladung)
            .map(booleanFunction)
            .orElse(null);
    }

    @NonNull
    public static String extractUntersuchungDurchfuehrung(
        @NonNull Einladung einladung,
        @NonNull Function<UntersuchungDurchfuehrung, String> stringTransformer
    ) {
        return ExtractorUtil.getUntersuchungDurchfuehrungOptional(einladung)
            .map(stringTransformer)
            .orElse(PLACEHOLDER);
    }

    @NonNull
    public static String extractUntersuchungSehen(
        @NonNull Einladung einladung,
        @NonNull Function<UntersuchungSehenBeurteilung, String> stringTransformer
    ) {
        return ExtractorUtil.getUntersuchungSehenOptional(einladung)
            .map(stringTransformer)
            .orElse(PLACEHOLDER);
    }

    @NonNull
    public static String extractUntersuchungHoerenAudiogramm(
        @NonNull Einladung einladung,
        @NonNull Function<UntersuchungHoerenAudiogramm, String> stringTransformer
    ) {
        return ExtractorUtil.getUntersuchungHoerenAudiogrammOptional(einladung)
            .map(stringTransformer)
            .orElse(PLACEHOLDER);
    }

    @NonNull
    public static String extractUntersuchungHoerenOtoskopie(
        @NonNull Einladung einladung,
        @NonNull Function<UntersuchungHoerenOtoskopie, String> stringTransformer
    ) {
        return ExtractorUtil.getUntersuchungHoerenOtoskopieOptional(einladung)
            .map(stringTransformer)
            .orElse(PLACEHOLDER);
    }

    @NonNull
    public static String extractUntersuchungGroesseGewicht(
        @NonNull Einladung einladung,
        @NonNull Function<UntersuchungGroesseGewicht, String> stringTransformer
    ) {
        return ExtractorUtil.getUntersuchungGroesseGewichtOptional(einladung)
            .map(stringTransformer)
            .orElse(PLACEHOLDER);
    }

    @Nullable
    public static Boolean extractUntersuchungImpfungenBooleanFn(
        @NonNull Einladung einladung,
        @NonNull Function<UntersuchungImpfungen, Boolean> booleanFunction
    ) {
        return ExtractorUtil.getUntersuchungImpfungenOptional(einladung)
            .map(booleanFunction)
            .orElse(null);
    }

    @NonNull
    public static String extractUntersuchungImpfungen(
        @NonNull Einladung einladung,
        @NonNull Function<UntersuchungImpfungen, String> stringFunction
    ) {
        return ExtractorUtil.getUntersuchungImpfungenOptional(einladung)
            .map(stringFunction)
            .orElse(PLACEHOLDER);
    }

    @NonNull
    public static String extractUntersuchungArzt(
        @NonNull Einladung einladung,
        @NonNull Function<UntersuchungArztBeurteilung, String> stringFunction
    ) {
        return ExtractorUtil.getUntersuchungArztOptional(einladung)
            .map(stringFunction)
            .orElse(PLACEHOLDER);
    }

    @NonNull
    public static String extractUntersuchungArztBemerkung(
        @NonNull Einladung einladung,
        @NonNull Function<UntersuchungArztBemerkung, String> stringFunction
    ) {
        return ExtractorUtil.getUntersuchungArztBemerkungOptional(einladung)
            .map(stringFunction)
            .orElse(PLACEHOLDER);
    }

    @NonNull
    public static String extractUntersuchungMassnahmen(
        @NonNull Einladung einladung,
        @NonNull Function<UntersuchungMassnahmen, String> stringFunction
    ) {
        return ExtractorUtil.getUntersuchungMassnahmenOptional(einladung)
            .map(stringFunction)
            .orElse(PLACEHOLDER);
    }

    @NonNull
    public static String extractUntersuchungImpfungenMassnahmen(
        @NonNull Einladung einladung,
        @NonNull Function<UntersuchungImpfungenMassnahmen, String> stringFunction
    ) {
        return ExtractorUtil.getUntersuchungImpfungenMassnahmenOptional(einladung)
            .map(stringFunction)
            .orElse(PLACEHOLDER);
    }

    @Nullable
    public static Boolean extractUntersuchungMassnahmenBooleanFn(
        @NonNull Einladung einladung,
        @NonNull Function<UntersuchungMassnahmen, Boolean> booleanFunction
    ) {
        return ExtractorUtil.getUntersuchungMassnahmenOptional(einladung)
            .map(booleanFunction)
            .orElse(null);
    }

    @Nullable
    public static Boolean extractUntersuchungImpfungenMassnahmenBooleanFn(
        @NonNull Einladung einladung,
        @NonNull Function<UntersuchungImpfungenMassnahmen, Boolean> booleanFunction
    ) {
        return ExtractorUtil.getUntersuchungImpfungenMassnahmenOptional(einladung)
            .map(booleanFunction)
            .orElse(null);
    }

    @Nullable
    public static Audiogramm extractAudiogramm(
        @NonNull Einladung einladung,
        @NonNull Function<UntersuchungHoerenAudiogramm, Audiogramm> audiogrammFunction
    ) {
        return ExtractorUtil.getUntersuchungHoerenAudiogrammOptional(einladung)
            .map(audiogrammFunction)
            .orElse(null);
    }

    @Nullable
    public static AnlassFlags extractAnlassFlags(
        @NonNull Einladung einladung,
        @NonNull Function<UntersuchungMassnahmen, AnlassFlags> anlassFlagsFunction
    ) {
        return ExtractorUtil.getUntersuchungMassnahmenOptional(einladung)
            .map(anlassFlagsFunction)
            .orElse(null);
    }

    public static boolean isAnyVisible(@NonNull RequirementSetting... settings) {
        for (RequirementSetting setting : settings) {
            if (setting.isVisible()) {
                return true;
            }
        }
        return false;
    }
}
