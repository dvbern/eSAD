package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import org.checkerframework.checker.nullness.qual.NonNull;

public class AerztinAnlassCsvColumn extends AbstractCsvColumn {

    public AerztinAnlassCsvColumn(@NonNull CsvColumnConfig config) {
        super(config, null);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return einladung.getAnlass().getArzt() != null ? einladung.getAnlass().getArzt().getNameVorname() : "";
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.aerztinAnlass";
    }
}
