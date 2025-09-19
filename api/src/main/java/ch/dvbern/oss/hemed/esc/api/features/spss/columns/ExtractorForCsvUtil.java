package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungDurchfuehrung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungImpfstatusKrankheit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungImpfungKrankheitImmunitaet;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungKindDaten;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungSehenBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.UntersuchungTriage;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.UntersuchungArztBeurteilung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.UntersuchungGroesseGewicht;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.Dezibel;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenAudiogramm;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.UntersuchungHoerenOtoskopie;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungImpfungenMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungMassnahmen;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.shared.printer.ExtractorUtil;
import lombok.experimental.UtilityClass;
import org.checkerframework.checker.nullness.qual.NonNull;

@UtilityClass
public final class ExtractorForCsvUtil {

    @NonNull
    public static String extractUntersuchungKindDaten(
        @NonNull Einladung einladung,
        @NonNull Function<UntersuchungKindDaten, String> stringTransformer
    ) {
        return ExtractorUtil.getUntersuchungKindDatenOptional(einladung)
            .map(stringTransformer)
            .orElse("");
    }

    @NonNull
    public static String extractUntersuchungDurchfuehrung(
        @NonNull Einladung einladung,
        @NonNull Function<UntersuchungDurchfuehrung, String> stringTransformer
    ) {
        return ExtractorUtil.getUntersuchungDurchfuehrungOptional(einladung)
            .map(stringTransformer)
            .orElse("");
    }

    @NonNull
    public static String extractUntersuchungImpfungen(
        @NonNull Einladung einladung,
        @NonNull Function<UntersuchungImpfungen, String> stringTransformer
    ) {
        return ExtractorUtil.getUntersuchungImpfungenOptional(einladung)
            .map(stringTransformer)
            .orElse("");
    }

    @NonNull
    public static String extractUntersuchungImpfstatusKrankheit(
        @NonNull Einladung einladung,
        @NonNull Krankheit krankheit,
        @NonNull CsvColumnConfig config,
        @NonNull Function<UntersuchungImpfstatusKrankheit, String> stringTransformer
    ) {
        return getUntersuchungImpfstatusKrankheitOptional(einladung, krankheit, config)
            .map(stringTransformer)
            .orElse("");
    }

    @NonNull
    public static String extractUntersuchungSehen(
        @NonNull Einladung einladung,
        @NonNull Function<UntersuchungSehenBeurteilung, String> stringTransformer
    ) {
        return ExtractorUtil.getUntersuchungSehenOptional(einladung)
            .map(stringTransformer)
            .orElse("");
    }

    @NonNull
    public static String extractUntersuchungHoerenAudiogramm(
        @NonNull Einladung einladung,
        @NonNull Function<UntersuchungHoerenAudiogramm, String> stringTransformer
    ) {
        return ExtractorUtil.getUntersuchungHoerenAudiogrammOptional(einladung)
            .map(stringTransformer)
            .orElse("");
    }

    @NonNull
    public static String extractUntersuchungHoerenOtoskopie(
        @NonNull Einladung einladung,
        @NonNull Function<UntersuchungHoerenOtoskopie, String> stringTransformer
    ) {
        return ExtractorUtil.getUntersuchungHoerenOtoskopieOptional(einladung)
            .map(stringTransformer)
            .orElse("");
    }

    @NonNull
    public static String extractUntersuchungGroesseGewicht(
        @NonNull Einladung einladung,
        @NonNull Function<UntersuchungGroesseGewicht, String> stringTransformer
    ) {
        return ExtractorUtil.getUntersuchungGroesseGewichtOptional(einladung)
            .map(stringTransformer)
            .orElse("");
    }

    @NonNull
    public static String extractUntersuchungTriage(
        @NonNull Einladung einladung,
        @NonNull Function<UntersuchungTriage, String> stringTransformer
    ) {
        return ExtractorUtil.getUntersuchungTriageOptional(einladung)
            .map(stringTransformer)
            .orElse("");
    }

    @NonNull
    public static String extractUntersuchungMassnahmen(
        @NonNull Einladung einladung,
        @NonNull Function<UntersuchungMassnahmen, String> stringTransformer
    ) {
        return ExtractorUtil.getUntersuchungMassnahmenOptional(einladung)
            .map(stringTransformer)
            .orElse("");
    }

    @NonNull
    public static String extractUntersuchungArztBeurteilung(
        @NonNull Einladung einladung,
        @NonNull Function<UntersuchungArztBeurteilung, String> stringTransformer
    ) {
        return ExtractorUtil.getUntersuchungArztOptional(einladung)
            .map(stringTransformer)
            .orElse("");
    }

    @NonNull
    public static String extractUntersuchungImpfungenMassnahmen(
        @NonNull Einladung einladung,
        @NonNull Function<UntersuchungImpfungenMassnahmen, String> stringTransformer
    ) {
        return ExtractorUtil.getUntersuchungImpfungenMassnahmenOptional(einladung)
            .map(stringTransformer)
            .orElse("");
    }

    @NonNull
    private static Optional<UntersuchungImpfstatusKrankheit> getUntersuchungImpfstatusKrankheitOptional(
        @NonNull Einladung einladung,
        @NonNull Krankheit krankheit,
        @NonNull CsvColumnConfig config
    ) {

        if (einladung.getUntersuchung() == null) {
            return Optional.empty();
        }

        if (einladung.getUntersuchung().getUntersuchungImpfungen() == null) {
            return Optional.empty();
        }

        return config.untersuchungImpfstatusKrankheitService().findByUntersuchungAndKrankheit(
            einladung.getUntersuchung().getUntersuchungImpfungen().getId(),
            krankheit);
    }

    @NonNull
    public static List<UntersuchungImpfungKrankheitImmunitaet> untersuchungImpfungKrankheitImmunitaetList(
        @NonNull Einladung einladung,
        @NonNull Krankheit krankheit,
        @NonNull CsvColumnConfig config
    ) {
		return untersuchungImpfungtImmunitaetList(einladung, config).stream()
				.filter(krankheitImmunitaet -> krankheitImmunitaet.getKrankheit() == krankheit)
				.toList();
	}

    @NonNull
    public static List<UntersuchungImpfungKrankheitImmunitaet> untersuchungImpfungtImmunitaetList(
        @NonNull Einladung einladung,
        @NonNull CsvColumnConfig config) {
        if (einladung.getUntersuchung() == null) {
            return Collections.emptyList();
        }

        List<UntersuchungImpfungIntern> untersuchungImpfungInternList =
            config.untersuchungImpfungInternService().findByUntersuchung(einladung.getUntersuchung().getId());

        return untersuchungImpfungInternList.stream()
            .flatMap(untersuchungImpfungIntern -> untersuchungImpfungIntern.getKrankheitenImmunitaeten().stream())
            .toList();
    }

    @NonNull
    public String dezibelStringValue(@NonNull Dezibel dezibel) {
        return switch (dezibel) {
            case DB10 -> "10";
            case DB20 -> "20";
            case DB30 -> "30";
            case DB40 -> "40";
            case DB50 -> "50";
            case DB60 -> "60";
            case MORE_THAN_DB60 -> "70";
        };
    }
}
