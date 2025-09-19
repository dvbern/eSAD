package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BlutdruckDiastolischCsvColumn extends AbstractCsvColumn {

    public BlutdruckDiastolischCsvColumn(
        @NonNull CsvColumnConfig config,
        @Nullable String overrideHeader) {
        super(config, overrideHeader);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungArztBeurteilung(
            einladung,
            untersuchungArzt ->
                untersuchungArzt.getBlutdruckDiastolisch() != null
                    ? String.valueOf(untersuchungArzt.getBlutdruckDiastolisch())
                    : ""
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.blutdruckDiastolisch";
    }
}
