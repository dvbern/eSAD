package ch.dvbern.oss.hemed.esc.api.shared.printer.kinderdossier;

import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztBemerkung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.features.anlass.bewegungsapparat.UntersuchungBewegungsapparat;
import ch.dvbern.oss.hemed.esc.api.features.anlass.durchfuehrung.UntersuchungDurchfuehrung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.UntersuchungGroesseGewicht;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.Audiogramm;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenAudiogramm;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenOtoskopie;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungenDataMinimal;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.AnlassWithDateTimeJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.kind.UntersuchungKindDaten;
import ch.dvbern.oss.hemed.esc.api.features.anlass.laeusekontrolle.UntersuchungLaeusekontrolle;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungImpfungenMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.UntersuchungSchulrelevanteBeeintraechtigung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.UntersuchungSehenBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.UntersuchungSprache;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.shared.anlass.anlassflags.AnlassFlags;
import ch.dvbern.oss.hemed.esc.api.shared.printer.ExtractorUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        @Nullable Untersuchung untersuchung,
        @NonNull Function<UntersuchungDurchfuehrung, Boolean> booleanFunction
    ) {
        return ExtractorUtil.getUntersuchungDurchfuehrungOptional(untersuchung)
            .map(booleanFunction)
            .orElse(null);
    }

    @NonNull
    public static String extractUntersuchungDurchfuehrung(
        @Nullable Untersuchung untersuchung,
        @NonNull Function<UntersuchungDurchfuehrung, String> stringTransformer
    ) {
        return ExtractorUtil.getUntersuchungDurchfuehrungOptional(untersuchung)
            .map(stringTransformer)
            .orElse(PLACEHOLDER);
    }

    @NonNull
    public static String extractUntersuchungSehen(
        @Nullable Untersuchung untersuchung,
        @NonNull Function<UntersuchungSehenBeurteilung, String> stringTransformer
    ) {
        return ExtractorUtil.getUntersuchungSehenOptional(untersuchung)
            .map(stringTransformer)
            .orElse(PLACEHOLDER);
    }

    @NonNull
    public static String extractUntersuchungHoerenAudiogramm(
        @Nullable Untersuchung untersuchung,
        @NonNull Function<UntersuchungHoerenAudiogramm, String> stringTransformer
    ) {
        return ExtractorUtil.getUntersuchungHoerenAudiogrammOptional(untersuchung)
            .map(stringTransformer)
            .orElse(PLACEHOLDER);
    }

    @NonNull
    public static String extractUntersuchungHoerenOtoskopie(
        @Nullable Untersuchung untersuchung,
        @NonNull Function<UntersuchungHoerenOtoskopie, String> stringTransformer
    ) {
        return ExtractorUtil.getUntersuchungHoerenOtoskopieOptional(untersuchung)
            .map(stringTransformer)
            .orElse(PLACEHOLDER);
    }

    @NonNull
    public static String extractUntersuchungGroesseGewicht(
        @Nullable Untersuchung untersuchung,
        @NonNull Function<UntersuchungGroesseGewicht, String> stringTransformer
    ) {
        return ExtractorUtil.getUntersuchungGroesseGewichtOptional(untersuchung)
            .map(stringTransformer)
            .orElse(PLACEHOLDER);
    }

    @Nullable
    public static Boolean extractUntersuchungImpfungenBooleanFn(
        @Nullable Untersuchung untersuchung,
        @NonNull Function<UntersuchungImpfungen, Boolean> booleanFunction
    ) {
        return ExtractorUtil.getUntersuchungImpfungenOptional(untersuchung)
            .map(booleanFunction)
            .orElse(null);
    }

    @NonNull
    public static String extractUntersuchungImpfungen(
        @Nullable Untersuchung untersuchung,
        @NonNull Function<UntersuchungImpfungen, String> stringFunction
    ) {
        return ExtractorUtil.getUntersuchungImpfungenOptional(untersuchung)
            .map(stringFunction)
            .orElse(PLACEHOLDER);
    }

    @NonNull
    public static String extractUntersuchungImpfungenDataMinimal(
        @Nullable Untersuchung untersuchung,
        @NonNull Function<UntersuchungImpfungenDataMinimal, String> stringFunction
    ) {
        return ExtractorUtil.getUntersuchungImpfungenOptional(untersuchung)
            .map(UntersuchungImpfungen::getUntersuchungImpfungenDataMinimal)
            .map(stringFunction)
            .orElse(PLACEHOLDER);
    }

    @NonNull
    public static String extractUntersuchungArzt(
        @Nullable Untersuchung untersuchung,
        @NonNull Function<UntersuchungArztBeurteilung, String> stringFunction
    ) {
        return ExtractorUtil.getUntersuchungArztOptional(untersuchung)
            .map(stringFunction)
            .orElse(PLACEHOLDER);
    }

    @NonNull
    public static String extractUntersuchungArztBemerkung(
        @Nullable Untersuchung untersuchung,
        @NonNull Function<UntersuchungArztBemerkung, String> stringFunction
    ) {
        return ExtractorUtil.getUntersuchungArztBemerkungOptional(untersuchung)
            .map(stringFunction)
            .orElse(PLACEHOLDER);
    }

    @NonNull
    public static String extractUntersuchungSprache(
        @Nullable Untersuchung untersuchung,
        @NonNull Function<UntersuchungSprache, String> stringFunction
    ) {
        return ExtractorUtil.getUntersuchungSpracheOptional(untersuchung)
            .map(stringFunction)
            .orElse(PLACEHOLDER);
    }

    @NonNull
    public static String extractUntersuchungBewegungsapparat(
        @Nullable Untersuchung untersuchung,
        @NonNull Function<UntersuchungBewegungsapparat, String> stringFunction
    ) {
        return ExtractorUtil.getUntersuchungBewegungsapparatOptional(untersuchung)
            .map(stringFunction)
            .orElse(PLACEHOLDER);
    }

    @NonNull
    public static String extractUntersuchungSchulrelevanteBeeintraechtigung(
        @Nullable Untersuchung untersuchung,
        @NonNull Function<UntersuchungSchulrelevanteBeeintraechtigung, String> stringFunction
    ) {
        return ExtractorUtil.getUntersuchungSchulrelevanteBeeintraechtigungOptional(untersuchung)
            .map(stringFunction)
            .orElse(PLACEHOLDER);
    }

    @NonNull
    public static String extractUntersuchungLaeusekontrolle(
        @Nullable Untersuchung untersuchung,
        @NonNull Function<UntersuchungLaeusekontrolle, String> stringTransformer
    ) {
        return ExtractorUtil.getUntersuchungLaeusekontrolleOptional(untersuchung)
            .map(stringTransformer)
            .orElse(PLACEHOLDER);
    }

    @NonNull
    public static String extractUntersuchungMassnahmen(
        @Nullable Untersuchung untersuchung,
        @NonNull Function<UntersuchungMassnahmen, String> stringFunction
    ) {
        return ExtractorUtil.getUntersuchungMassnahmenOptional(untersuchung)
            .map(stringFunction)
            .orElse(PLACEHOLDER);
    }

    @NonNull
    public static String extractUntersuchungImpfungenMassnahmen(
        @Nullable Untersuchung untersuchung,
        @NonNull Function<UntersuchungImpfungenMassnahmen, String> stringFunction
    ) {
        return ExtractorUtil.getUntersuchungImpfungenMassnahmenOptional(untersuchung)
            .map(stringFunction)
            .orElse(PLACEHOLDER);
    }

    @NonNull
    public static String extractImpfungenFolgetermin(
        @NonNull List<AnlassWithDateTimeJson> listeImpfungenFolgetermin,
        @NonNull Function<AnlassWithDateTimeJson, String> stringFunction
    ) {
        if (listeImpfungenFolgetermin.isEmpty()) {
            return PLACEHOLDER;
        }
        return listeImpfungenFolgetermin
            .stream().map(stringFunction).collect(Collectors.joining(", "));
    }

    @Nullable
    public static Boolean extractUntersuchungMassnahmenBooleanFn(
        @Nullable Untersuchung untersuchung,
        @NonNull Function<UntersuchungMassnahmen, Boolean> booleanFunction
    ) {
        return ExtractorUtil.getUntersuchungMassnahmenOptional(untersuchung)
            .map(booleanFunction)
            .orElse(null);
    }

    @Nullable
    public static Boolean extractUntersuchungImpfungenMassnahmenBooleanFn(
        @Nullable Untersuchung untersuchung,
        @NonNull Function<UntersuchungImpfungenMassnahmen, Boolean> booleanFunction
    ) {
        return ExtractorUtil.getUntersuchungImpfungenMassnahmenOptional(untersuchung)
            .map(booleanFunction)
            .orElse(null);
    }

    @Nullable
    public static Audiogramm extractAudiogramm(
        @Nullable Untersuchung untersuchung,
        @NonNull Function<UntersuchungHoerenAudiogramm, Audiogramm> audiogrammFunction
    ) {
        return ExtractorUtil.getUntersuchungHoerenAudiogrammOptional(untersuchung)
            .map(audiogrammFunction)
            .orElse(null);
    }

    @Nullable
    public static AnlassFlags extractAnlassFlags(
        @Nullable Untersuchung untersuchung,
        @NonNull Function<UntersuchungMassnahmen, AnlassFlags> anlassFlagsFunction
    ) {
        return ExtractorUtil.getUntersuchungMassnahmenOptional(untersuchung)
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
