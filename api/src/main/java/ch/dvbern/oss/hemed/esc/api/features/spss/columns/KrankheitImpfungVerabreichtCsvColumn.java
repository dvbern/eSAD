package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungKrankheitImmunitaet;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.types.ImpfStatus;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class KrankheitImpfungVerabreichtCsvColumn extends AbstractCsvColumn {

    private final Krankheit krankheit;

    public KrankheitImpfungVerabreichtCsvColumn(
        @NonNull CsvColumnConfig config,
        @Nullable String overrideHeader,
        @NonNull Krankheit krankheit
    ) {
        super(config, overrideHeader);
        this.krankheit = krankheit;
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        List<UntersuchungImpfungKrankheitImmunitaet> krankheitImmunitaetList =
            ExtractorForCsvUtil.untersuchungImpfungKrankheitImmunitaetList(einladung, krankheit, config);

        if (krankheitImmunitaetList.isEmpty()) {
            return config.forSpssReport() ? "" : printBoolean(false);
        }

        // If any immunitaet comes from an Impfung which has been durchgefuehrt, we can return 1
        return krankheitImmunitaetList.stream()
            .map(UntersuchungImpfungKrankheitImmunitaet::getImpfung)
            .filter(impfung -> impfung.getImpfStatus() == ImpfStatus.DURCHGEFUEHRT)
            .filter(impfung -> impfung.getAnlassImpfungDurchgefuehrt() != null
                && impfung.getAnlassImpfungDurchgefuehrt()
                .getId()
                .getId()
                .equals(einladung.getAnlass().getId().getId()))
            .toList()
            .isEmpty() ? formatNegativResponse() : formatPositivResponse();
    }

    @NonNull
    private String formatPositivResponse() {
        return config.forSpssReport() ? "1" : PrintUtil.trimToPlaceholder(true, config.tl());
    }

    @NonNull
    private String formatNegativResponse() {
        return config.forSpssReport() ? "" : PrintUtil.trimToPlaceholder(false, config.tl());
    }

    String printBoolean(boolean input) {
        return PrintUtil.trimToPlaceholder(input, config.tl());
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.krankheitImpfungVerabreicht." + this.krankheit;
    }
}
