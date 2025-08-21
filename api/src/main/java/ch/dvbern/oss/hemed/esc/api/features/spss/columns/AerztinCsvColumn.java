package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class AerztinCsvColumn extends AbstractCsvColumn {

    public AerztinCsvColumn(@NonNull CsvColumnConfig config, @Nullable String overrideHeader) {
        super(config, overrideHeader);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungDurchfuehrung(
            einladung,
            untersuchungDurchfuehrung ->
                untersuchungDurchfuehrung.getArzt() != null ? untersuchungDurchfuehrung.getArzt().getNameVorname() : ""
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.aerztin";
    }
}
