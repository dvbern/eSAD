package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import org.checkerframework.checker.nullness.qual.NonNull;

public class LesefitPunkteCsvColumn extends AbstractCsvColumn {

    public LesefitPunkteCsvColumn(@NonNull CsvColumnConfig config) {
        super(config, null);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungArztBeurteilung(
            einladung,
            untersuchungArzt ->
                untersuchungArzt.getLesefitPunkte() != null
                    ? String.valueOf(untersuchungArzt.getLesefitPunkte())
                    : ""
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.lesefitPunkte";
    }
}
