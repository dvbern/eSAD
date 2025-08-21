package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht.PerzentileGroesse;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class GroesseBeurteilungCsvColumn extends AbstractCsvColumn {

    public GroesseBeurteilungCsvColumn(
        @NonNull CsvColumnConfig config,
        @Nullable String overrideHeader
    ) {
        super(config, overrideHeader);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungGroesseGewicht(
            einladung,
            untersuchungGroesseGewicht -> {
                if (untersuchungGroesseGewicht.getGroessePerzentile() == null) {
                    return "";
                }
                return switch (untersuchungGroesseGewicht.getGroessePerzentile()) {
                    case PerzentileGroesse.NORMAL -> "1";
                    case PerzentileGroesse.PERZENTILE_TIEF -> "5";
                    case PerzentileGroesse.PERZENTILE_HOCH -> "6";
                    case PerzentileGroesse.NICHT_GEPRUEFT -> "4";
                };
            }
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.groesseBeurteilung";
    }
}
