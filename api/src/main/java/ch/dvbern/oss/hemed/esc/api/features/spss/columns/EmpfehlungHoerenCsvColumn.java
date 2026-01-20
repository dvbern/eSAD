package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.HoerenEmpfehlung;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil;
import org.checkerframework.checker.nullness.qual.NonNull;

public class EmpfehlungHoerenCsvColumn extends AbstractCsvColumn {

    private final HoerenEmpfehlung empfehlung;

    public EmpfehlungHoerenCsvColumn(
        @NonNull CsvColumnConfig config,
        @NonNull HoerenEmpfehlung empfehlung
    ) {
        super(config, null);
        this.empfehlung = empfehlung;
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungHoerenAudiogramm(
            einladung,
            untersuchungHoeren ->
                PrintUtil.trimToPlaceholder(untersuchungHoeren.getEmpfehlung().contains(this.empfehlung), config.tl())
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.empfehlungHoeren." + this.empfehlung;
    }

}
