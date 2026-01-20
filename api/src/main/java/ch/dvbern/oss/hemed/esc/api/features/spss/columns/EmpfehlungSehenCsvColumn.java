package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.SehenEmpfehlung;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil;
import org.checkerframework.checker.nullness.qual.NonNull;

public class EmpfehlungSehenCsvColumn extends AbstractCsvColumn {

    private final SehenEmpfehlung empfehlung;

    public EmpfehlungSehenCsvColumn(
        @NonNull CsvColumnConfig config,
        @NonNull SehenEmpfehlung empfehlung) {
        super(config, null);
        this.empfehlung = empfehlung;
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungSehen(
            einladung,
            untersuchungSehen ->
                PrintUtil.trimToPlaceholder(untersuchungSehen.getEmpfehlung().contains(this.empfehlung), config.tl())
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.empfehlungSehen." + this.empfehlung;
    }

}
