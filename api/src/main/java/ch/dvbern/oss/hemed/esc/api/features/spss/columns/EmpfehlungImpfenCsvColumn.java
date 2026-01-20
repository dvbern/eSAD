package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.types.ImpfungenMassnahmenEmpfehlung;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil;
import org.checkerframework.checker.nullness.qual.NonNull;

public class EmpfehlungImpfenCsvColumn extends AbstractCsvColumn {

    private final ImpfungenMassnahmenEmpfehlung empfehlung;

    public EmpfehlungImpfenCsvColumn(
        @NonNull CsvColumnConfig config,
        @NonNull ImpfungenMassnahmenEmpfehlung empfehlung) {
        super(config, null);
        this.empfehlung = empfehlung;
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungImpfungenMassnahmen(
            einladung,
            massnahmen ->
                PrintUtil.trimToPlaceholder(massnahmen.getEmpfehlung().contains(this.empfehlung), config.tl())
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.empfehlungImpfen." + this.empfehlung;
    }

}
