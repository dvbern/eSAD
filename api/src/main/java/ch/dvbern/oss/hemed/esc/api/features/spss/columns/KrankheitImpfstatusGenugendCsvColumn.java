package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungKrankheitImmunitaet;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class KrankheitImpfstatusGenugendCsvColumn extends AbstractCsvColumn {

    private final Krankheit krankheit;

    public KrankheitImpfstatusGenugendCsvColumn(
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
            return config.forSpssReport() ? "" : PrintUtil.trimToPlaceholder(false, config.tl());
        }

        // If any Impfung has covered the immunitaet for this Krankheit, we can return 1.
        return krankheitImmunitaetList.stream()
            .filter(UntersuchungImpfungKrankheitImmunitaet::isImmunitaetGenuegend)
            .toList()
            .isEmpty() ? "" : formatPositivResponse();
    }

    @NonNull
    private String formatPositivResponse() {
        return config.forSpssReport() ? "1" : PrintUtil.trimToPlaceholder(true, config.tl());
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.krankheitImpfungGenuegend." + this.krankheit;
    }
}
