package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class KrankheitJahrMonatDerLetztenImpfungCsvColumn extends AbstractCsvColumn {

    public static final String NOT_AVAILABLE_VALUE = "55"; // will be set if not value is present
    @NonNull
    private final Krankheit krankheit;

    public KrankheitJahrMonatDerLetztenImpfungCsvColumn(
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
            impfstatus -> impfstatus.getDatumLetzteImpfung() != null ?
                impfstatus.getDatumLetzteImpfung().getMonth() + " " + impfstatus.getDatumLetzteImpfung().getYear() :
                NOT_AVAILABLE_VALUE);
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.krankheitJahrMonatDerLetztenImpfung." + this.krankheit;
    }
}
