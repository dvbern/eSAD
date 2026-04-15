package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungIntern;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ImpfungChargeCsvColumn extends AbstractCsvColumn {

    public ImpfungChargeCsvColumn(@NonNull CsvColumnConfig config) {
        super(config, null);
    }

    @Override
    public @NonNull String extract(@NonNull Einladung einladung) {
        return "";
    }

    @Override
    @NonNull
    public String extract(@NonNull UntersuchungImpfungIntern impfung) {
        if (impfung.getCharge() == null) {
            return "";
        }
        return impfung.getCharge().getName();
    }

    @Override
    protected @NonNull String getDefaultHeaderKey() {
        return "CsvColumn.ImpfungCharge";
    }
}

