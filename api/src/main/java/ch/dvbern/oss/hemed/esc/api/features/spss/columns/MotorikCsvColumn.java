package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Motorik;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MotorikCsvColumn extends AbstractCsvColumn {

    public MotorikCsvColumn(
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
                if (untersuchungArzt.getMotorik() == null) {
                    return "";
                }
                return switch (untersuchungArzt.getMotorik()) {
                    case Motorik.NORMAL -> "1";
                    case Motorik.AUFFAELLIG -> "2";
                    case Motorik.NICHT_GEPRUEFT -> "3";
                };
            }
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.motorik";
    }
}
