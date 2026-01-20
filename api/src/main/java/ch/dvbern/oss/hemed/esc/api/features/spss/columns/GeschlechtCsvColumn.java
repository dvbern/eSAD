package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class GeschlechtCsvColumn extends AbstractCsvColumn {

    public GeschlechtCsvColumn(@NonNull CsvColumnConfig config, @Nullable String overrideHeader) {
        super(config, overrideHeader);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungKindDaten(
            einladung,
            kindDaten -> {
                if (!config.forSpssReport()) {
                    return config.tl().translateValue(kindDaten.getGeschlecht());
                }
                return switch (kindDaten.getGeschlecht()) {
                    case MAENNLICH -> "1";
                    case WEIBLICH -> "2";
                    case DIVERS -> "4";
                    case UNBEKANNT -> "3";
                };
            });
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.geschlecht";
    }
}
