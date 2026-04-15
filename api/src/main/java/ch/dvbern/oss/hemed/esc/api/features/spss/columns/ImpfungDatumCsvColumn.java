package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import java.time.format.DateTimeFormatter;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungIntern;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ImpfungDatumCsvColumn extends AbstractCsvColumn {

    public ImpfungDatumCsvColumn(@NonNull CsvColumnConfig config) {
        super(config, null);
    }

    @Override
    public @NonNull String extract(@NonNull Einladung einladung) {
        return "";
    }

    @Override
    @NonNull
    public String extract(@NonNull UntersuchungImpfungIntern impfung) {
        if (impfung.getDatumImpfung() == null) {
            return "";
        }
        return impfung.getDatumImpfung().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }

    @Override
    protected @NonNull String getDefaultHeaderKey() {
        return "CsvColumn.ImpfungDatum";
    }
}
