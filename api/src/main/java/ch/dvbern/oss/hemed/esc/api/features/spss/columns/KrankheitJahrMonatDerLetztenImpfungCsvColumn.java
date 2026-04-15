package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class KrankheitJahrMonatDerLetztenImpfungCsvColumn extends AbstractCsvColumn {

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
            impfstatus -> Optional.ofNullable(impfstatus.getDatumLetzteImpfung())
                .map(datum -> datum.format(DateTimeFormatter.ofPattern("MM.yyyy")))
                .orElse("")
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.krankheitJahrMonatDerLetztenImpfung." + this.krankheit;
    }
}
