package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import org.checkerframework.checker.nullness.qual.NonNull;

public class VerhaltensfragebogenCsvColumn extends AbstractCsvColumn {

    public VerhaltensfragebogenCsvColumn(@NonNull CsvColumnConfig config) {
        super(config, null);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungArztBeurteilung(
            einladung,
            untersuchungArzt -> {
                if (untersuchungArzt.getVerhaltensfragebogen() == null) {
                    return "";
                }
                return config.tl().translateValue(untersuchungArzt.getVerhaltensfragebogen());
            }
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.verhaltensfragebogen";
    }
}
