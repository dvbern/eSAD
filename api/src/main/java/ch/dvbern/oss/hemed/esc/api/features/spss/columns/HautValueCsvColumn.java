package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Eine Spalte für den Haut-Wert in der CSV-Exportdatei.
 */
public class HautValueCsvColumn extends AbstractCsvColumn {

    public HautValueCsvColumn(@NonNull CsvColumnConfig config) {
        super(config, null);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungArztBeurteilung(
            einladung,
            untersuchungArzt -> {
                if (untersuchungArzt.getHaut() == null) {
                    return "";
                }
                return config.tl().translateValue(untersuchungArzt.getHaut());
            }
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.haut";
    }
}
