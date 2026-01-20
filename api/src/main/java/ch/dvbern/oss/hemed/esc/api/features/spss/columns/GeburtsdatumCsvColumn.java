package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import java.time.format.DateTimeFormatter;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import org.checkerframework.checker.nullness.qual.NonNull;

public class GeburtsdatumCsvColumn extends AbstractCsvColumn {

    public GeburtsdatumCsvColumn(@NonNull CsvColumnConfig config) {
        super(config, null);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungKindDaten(
            einladung,
            kindDaten -> kindDaten.getGeburtsdatum().format(
                DateTimeFormatter.ofPattern("dd.MM.yyyy")
            ));
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.geburtsdatum";
    }
}
