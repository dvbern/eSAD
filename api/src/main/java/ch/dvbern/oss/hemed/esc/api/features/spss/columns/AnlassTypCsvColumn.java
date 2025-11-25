package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class AnlassTypCsvColumn extends AbstractCsvColumn {

    public AnlassTypCsvColumn(@NonNull CsvColumnConfig config, @Nullable String overrideHeader) {
        super(config, overrideHeader);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return config.tl().translateValue(einladung.getAnlass().getAnlassConfig().getAnlassTyp());
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.anlassTyp";
    }
}
