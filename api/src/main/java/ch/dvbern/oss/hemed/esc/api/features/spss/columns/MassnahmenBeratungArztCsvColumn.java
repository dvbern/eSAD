package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil;
import org.checkerframework.checker.nullness.qual.NonNull;

public class MassnahmenBeratungArztCsvColumn extends AbstractCsvColumn {

    public MassnahmenBeratungArztCsvColumn(
        @NonNull CsvColumnConfig config) {
        super(config, null);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungMassnahmen(
            einladung,
            untersuchungMassnahmen ->
                PrintUtil.trimToPlaceholder(untersuchungMassnahmen.isBeratungSchulAerztin(), config.tl())

        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.massnahmenBeratungSA";
    }
}
