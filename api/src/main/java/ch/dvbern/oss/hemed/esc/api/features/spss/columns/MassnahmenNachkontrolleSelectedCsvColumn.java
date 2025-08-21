package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MassnahmenNachkontrolleSelectedCsvColumn extends AbstractCsvColumn {

    public MassnahmenNachkontrolleSelectedCsvColumn(
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
                untersuchungMassnahmen.getNachkontrolle().isNachkontrolleSehen()
                    || untersuchungMassnahmen.getNachkontrolle().isNachkontrolleHoeren()
                    || untersuchungMassnahmen.getNachkontrolle().isNachkontrolleGroesseGewicht()
                    || untersuchungMassnahmen.getNachkontrolle().isNachkontrolleSprache()
                    || untersuchungMassnahmen.getNachkontrolle().isNachkontrolleMotorik()
                    || untersuchungMassnahmen.getNachkontrolle().isNachkontrolleAnderes() ? "1" : ""
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.massnahmenNachkontrolleSelected";
    }
}
