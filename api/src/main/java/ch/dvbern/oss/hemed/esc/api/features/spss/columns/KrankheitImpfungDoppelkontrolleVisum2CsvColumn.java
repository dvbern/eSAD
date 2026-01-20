package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class KrankheitImpfungDoppelkontrolleVisum2CsvColumn extends AbstractCsvColumn {

    public KrankheitImpfungDoppelkontrolleVisum2CsvColumn(
        @NonNull CsvColumnConfig config,
        @Nullable String overrideHeader
    ) {
        super(config, overrideHeader);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungImpfungen(
            einladung,
            impfungen -> impfungen.getVisum2() != null && impfungen.getVisum2().getPerson() != null ?
                impfungen.getVisum2().getPerson().getNameVorname() : ""
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.krankheitImpfungDoppelkontrolleVisum2";
    }

}
