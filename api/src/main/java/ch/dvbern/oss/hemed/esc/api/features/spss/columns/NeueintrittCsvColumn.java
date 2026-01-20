package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class NeueintrittCsvColumn extends AbstractCsvColumn {

    public NeueintrittCsvColumn(@NonNull CsvColumnConfig config, @Nullable String overrideHeader) {
        super(config, overrideHeader);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungDurchfuehrung(
            einladung,
            untersuchungDurchfuehrung -> {
                if (config.forSpssReport()) {
                    return untersuchungDurchfuehrung.isNeueintritt() ? "1" : "";
                }
                return PrintUtil.trimToPlaceholder(untersuchungDurchfuehrung.isNeueintritt(), config.tl());
            });
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.neueintritt";
    }
}
