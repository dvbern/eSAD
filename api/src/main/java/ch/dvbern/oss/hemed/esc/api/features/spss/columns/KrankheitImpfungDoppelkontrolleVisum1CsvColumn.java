package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class KrankheitImpfungDoppelkontrolleVisum1CsvColumn extends AbstractCsvColumn {

    public KrankheitImpfungDoppelkontrolleVisum1CsvColumn(
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
            impfungen -> impfungen.getVisum1() != null && impfungen.getVisum1().getPerson() != null ?
                impfungen.getVisum1().getPerson().getNameVorname() :
                "");
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.krankheitImpfungDoppelkontrolleVisum1";
    }

}
