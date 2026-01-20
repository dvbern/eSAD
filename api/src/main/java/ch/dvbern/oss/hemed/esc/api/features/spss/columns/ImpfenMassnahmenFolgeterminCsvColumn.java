package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ImpfenMassnahmenFolgeterminCsvColumn extends AbstractCsvColumn {

    public ImpfenMassnahmenFolgeterminCsvColumn(@NonNull CsvColumnConfig config) {
        super(config, null);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungImpfungenMassnahmen(
            einladung,
            untersuchungImpfungenMassnahmen -> PrintUtil.trimToPlaceholder(
                untersuchungImpfungenMassnahmen.isVormerkenFuerGruppenFolgetermin(),
                config.tl())
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.impfenMassnahmenFolgetermin";
    }
}
