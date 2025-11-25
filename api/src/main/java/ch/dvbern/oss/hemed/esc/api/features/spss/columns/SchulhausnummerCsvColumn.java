package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class SchulhausnummerCsvColumn extends AbstractCsvColumn {

    public SchulhausnummerCsvColumn(@NonNull CsvColumnConfig config, @Nullable String overrideHeader) {
        super(config, overrideHeader);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungKindDaten(
            einladung,
            kindDaten -> kindDaten.getSchulhausnummer() == null ? "" : kindDaten.getSchulhausnummer());
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.schulhausnummer";
    }
}
