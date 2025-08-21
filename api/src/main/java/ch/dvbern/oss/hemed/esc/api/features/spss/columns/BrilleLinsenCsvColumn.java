package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BrilleLinsenCsvColumn extends AbstractCsvColumn {

    public BrilleLinsenCsvColumn(@NonNull CsvColumnConfig config, @Nullable String overrideHeader) {
        super(config, overrideHeader);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungSehen(
            einladung,
            sehen -> {
                if (sehen.getBrilleLinsen() == null) {
                    return "";
                }
                return switch (sehen.getBrilleLinsen()) {
                    case OHNE_KEINE -> "1";
                    case MIT -> "2";
                    case OHNE_VERGESSEN -> "3";
                    case NICHT_GEPRUEFT -> "4";
                };
            });
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.brilleLinse";
    }
}
