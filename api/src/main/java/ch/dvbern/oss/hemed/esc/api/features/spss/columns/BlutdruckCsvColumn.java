package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Blutdruck;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BlutdruckCsvColumn extends AbstractCsvColumn {

    public BlutdruckCsvColumn(
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
                if (untersuchungArzt.getBlutdruck() == null) {
                    return "";
                }
                return switch (untersuchungArzt.getBlutdruck()) {
                    case Blutdruck.NORMAL -> "1";
                    case Blutdruck.AUFFAELLIG -> "2";
                    case Blutdruck.NICHT_GEPRUEFT -> "3";
                };
            }
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.blutdruck";
    }
}
