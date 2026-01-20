package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import org.checkerframework.checker.nullness.qual.NonNull;

public class Zahlennachsprechen4CsvColumn extends AbstractCsvColumn {

    public Zahlennachsprechen4CsvColumn(@NonNull CsvColumnConfig config) {
        super(config, null);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungArztBeurteilung(
            einladung,
            untersuchungArzt -> {
                if (untersuchungArzt.getZahlenNachsprechen4() == null) {
                    return "";
                }
                return config.tl().translateValue(untersuchungArzt.getZahlenNachsprechen4());
            }
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.zahlennachsprechen4";
    }
}
