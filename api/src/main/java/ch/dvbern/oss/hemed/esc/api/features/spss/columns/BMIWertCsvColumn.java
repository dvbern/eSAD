package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class BMIWertCsvColumn extends AbstractCsvColumn {

    public BMIWertCsvColumn(
        @NonNull CsvColumnConfig config,
        @Nullable String overrideHeader) {
        super(config, overrideHeader);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungGroesseGewicht(
            einladung,
            untersuchungGroesseGewicht ->
                untersuchungGroesseGewicht.getBmi() != null
                    ? String.valueOf(untersuchungGroesseGewicht.getBmi())
                    : ""
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.bmiWertCsv";
    }
}
