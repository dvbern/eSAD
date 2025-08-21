package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MassnahmenBereitsBehandeltSelectedCsvColumn extends AbstractCsvColumn {

    public MassnahmenBereitsBehandeltSelectedCsvColumn(
        @NonNull CsvColumnConfig config,
        @Nullable String overrideHeader) {
        super(config, overrideHeader);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungMassnahmen(
            einladung,
            untersuchungMassnahmen ->
                untersuchungMassnahmen.isBereitsBehandeltSehen()
                    || untersuchungMassnahmen.isBereitsBehandeltHoeren()
                    || untersuchungMassnahmen.isBereitsBehandeltGroesseGewicht()
                    || untersuchungMassnahmen.isBereitsBehandeltSprache()
                    || untersuchungMassnahmen.isBereitsBehandeltMotorik()
                    || untersuchungMassnahmen.isBereitsBehandeltAnderes() ? "1" : ""
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.massnahmenBereitsBehandeltSelected";
    }
}
