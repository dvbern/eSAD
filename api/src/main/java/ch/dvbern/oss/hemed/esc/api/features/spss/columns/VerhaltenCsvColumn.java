package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Verhalten;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class VerhaltenCsvColumn extends AbstractCsvColumn {

    public VerhaltenCsvColumn(
        @NonNull CsvColumnConfig config,
        @Nullable String overrideHeader) {
        super(config, overrideHeader);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungArztBeurteilung(
            einladung,
            untersuchungArzt -> {
                if (untersuchungArzt.getVerhalten() == null) {
                    return "";
                }
                return switch (untersuchungArzt.getVerhalten()) {
                    case Verhalten.UNAUFFAELLIG -> "1";
                    case Verhalten.AUFFAELLIG -> "2";
                    case Verhalten.NICHT_GEPRUEFT -> "3";
                };
            }
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.verhalten";
    }
}
