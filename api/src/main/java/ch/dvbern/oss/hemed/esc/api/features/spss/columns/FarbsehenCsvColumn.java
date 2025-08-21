package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class FarbsehenCsvColumn extends AbstractCsvColumn {

    public FarbsehenCsvColumn(@NonNull CsvColumnConfig config, @Nullable String overrideHeader) {
        super(config, overrideHeader);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungSehen(
            einladung,
            sehen -> {
                if (sehen.getFarbsehen() == null) {
                    return "";
                }
                return switch (sehen.getFarbsehen()) {
                    case NORMAL -> "1";
                    case PATHOLOGISCH -> "2";
                    case UNSICHER -> "3";
                    case NICHT_GEPRUEFT -> "4";
                };
            });
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.farbsehen";
    }
}
