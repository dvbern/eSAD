package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class NachkontrolleCsvColumn extends AbstractCsvColumn {

    public NachkontrolleCsvColumn(@NonNull CsvColumnConfig config, @Nullable String overrideHeader) {
        super(config, overrideHeader);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungDurchfuehrung(
            einladung,
            untersuchungDurchfuehrung -> untersuchungDurchfuehrung.isNachkontrolle() ? "1" : "");
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.nachkontrolle";
    }
}
