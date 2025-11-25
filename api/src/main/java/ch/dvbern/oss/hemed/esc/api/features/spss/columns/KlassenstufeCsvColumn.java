package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class KlassenstufeCsvColumn extends AbstractCsvColumn {

    public KlassenstufeCsvColumn(@NonNull CsvColumnConfig config, @Nullable String overrideHeader) {
        super(config, overrideHeader);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungKindDaten(
            einladung,
            kindDaten -> kindDaten.getKlassenstufe() == null
                ? ""
                : config.tl().translateValue(kindDaten.getKlassenstufe()));
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.klassenstufe";
    }
}
