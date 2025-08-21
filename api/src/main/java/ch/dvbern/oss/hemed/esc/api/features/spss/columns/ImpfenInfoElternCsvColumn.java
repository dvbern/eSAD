package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.InformationEltern;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ImpfenInfoElternCsvColumn extends AbstractCsvColumn {

    public ImpfenInfoElternCsvColumn(@NonNull CsvColumnConfig config, @Nullable String overrideHeader) {
        super(config, overrideHeader);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungImpfungenMassnahmen(
            einladung,
            untersuchungImpfungenMassnahmen ->
                (untersuchungImpfungenMassnahmen.getInformationEltern() == null
                    || untersuchungImpfungenMassnahmen.getInformationEltern() == InformationEltern.KEINE) ? "" : "1"
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.impfenInfoEltern";
    }
}
