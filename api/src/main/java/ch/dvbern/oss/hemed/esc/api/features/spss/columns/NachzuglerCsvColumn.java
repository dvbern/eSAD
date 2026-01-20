package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class NachzuglerCsvColumn extends AbstractCsvColumn {

    public NachzuglerCsvColumn(@NonNull CsvColumnConfig config, @Nullable String overrideHeader) {
        super(config, overrideHeader);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungDurchfuehrung(
            einladung,
            untersuchungDurchfuehrung -> {
                if (config.forSpssReport()) {
                    return untersuchungDurchfuehrung.isNachzuegler() ? "1" : "";
                }
                return PrintUtil.trimToPlaceholder(untersuchungDurchfuehrung.isNachzuegler(), config.tl());
            });
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.nachzugler";
    }
}
