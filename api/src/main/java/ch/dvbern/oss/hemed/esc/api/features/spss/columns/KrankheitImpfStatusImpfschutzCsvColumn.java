package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class KrankheitImpfStatusImpfschutzCsvColumn extends AbstractCsvColumn {

    @NonNull
    private final Krankheit krankheit;

    public KrankheitImpfStatusImpfschutzCsvColumn(
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
            impfstatus -> PrintUtil.trimToPlaceholder(impfstatus.isImpfschutzGegeben(), config.tl()));
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.krankheitImpfStatusImpfschutz." + this.krankheit;
    }
}
