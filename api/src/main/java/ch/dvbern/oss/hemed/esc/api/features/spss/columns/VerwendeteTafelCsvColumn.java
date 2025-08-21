package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class VerwendeteTafelCsvColumn extends AbstractCsvColumn {

    public VerwendeteTafelCsvColumn(@NonNull CsvColumnConfig config, @Nullable String overrideHeader) {
        super(config, overrideHeader);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungSehen(
            einladung,
            sehen -> {
                if (sehen.getVerwendeteTafel() == null) {
                    return "";
                }
                return switch (sehen.getVerwendeteTafel()) {
                    case E_TAFEL -> "1";
                    case C_TAFEL -> "2";
                    case PATTI_PICS -> "3";
                    case BUCHSTABEN -> "4";
                    case NICHT_GEPRUEFT -> "5";
                };
            });
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.verwendeteTafel";
    }
}
