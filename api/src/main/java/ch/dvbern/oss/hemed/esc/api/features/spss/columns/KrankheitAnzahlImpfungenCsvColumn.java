package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class KrankheitAnzahlImpfungenCsvColumn extends AbstractCsvColumn {

    @NonNull
    private final Krankheit krankheit;

    public KrankheitAnzahlImpfungenCsvColumn(
        @NonNull CsvColumnConfig config,
        @Nullable String overrideHeader,
        @NonNull Krankheit krankheit
    ) {
        super(config, overrideHeader);
        this.krankheit = krankheit;
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungImpfstatusKrankheit(
            einladung,
            krankheit,
            config,
            impfstatus -> impfstatus.getAnzahlImpfungen() != null ?
                String.valueOf(impfstatus.getAnzahlImpfungen()) :
                "");
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.krankheitAnzahlImpfungen";
    }
}
