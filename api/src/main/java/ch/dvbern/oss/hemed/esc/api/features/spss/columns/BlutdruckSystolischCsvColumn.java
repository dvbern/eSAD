package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BlutdruckSystolischCsvColumn extends AbstractCsvColumn {

    public BlutdruckSystolischCsvColumn(
        @NonNull CsvColumnConfig config,
        @Nullable String overrideHeader) {
        super(config, overrideHeader);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungArzt(
            einladung,
            untersuchungArzt ->
                untersuchungArzt.getBlutdruckSystolisch() != null
                    ? String.valueOf(untersuchungArzt.getBlutdruckSystolisch())
                    : ""
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.blutdruckSystolisch";
    }
}
