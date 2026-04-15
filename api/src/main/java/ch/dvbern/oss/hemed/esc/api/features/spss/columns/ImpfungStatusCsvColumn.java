package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungIntern;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ImpfungStatusCsvColumn extends AbstractCsvColumn {

    public ImpfungStatusCsvColumn(@NonNull CsvColumnConfig config) {
        super(config, null);
    }

    @Override
    public @NonNull String extract(@NonNull Einladung einladung) {
        return "";
    }

    @Override
    @NonNull
    public String extract(@NonNull UntersuchungImpfungIntern impfung) {
        return config.tl().translateValue(impfung.getImpfStatus());
    }

    @Override
    protected @NonNull String getDefaultHeaderKey() {
        return "CsvColumn.ImpfungStatus";
    }
}

