package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungIntern;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ImpfungVerabreicherCsvColumn extends AbstractCsvColumn {

    public ImpfungVerabreicherCsvColumn(@NonNull CsvColumnConfig config) {
        super(config, null);
    }

    @Override
    public @NonNull String extract(@NonNull Einladung einladung) {
        return "";
    }

    @Override
    @NonNull
    public String extract(@NonNull UntersuchungImpfungIntern impfung) {
        return PrintUtil.trimToPlaceholder(impfung.getUserDurchgefuehrt());
    }

    @Override
    protected @NonNull String getDefaultHeaderKey() {
        return "CsvColumn.ImpfungVerabreicher";
    }
}
