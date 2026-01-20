package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class KrankheitImpfempfehlungCsvColumn extends AbstractCsvColumn {

    @NonNull
    private final Krankheit krankheit;

    public KrankheitImpfempfehlungCsvColumn(
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
            impfstatus -> {
                if (config.forSpssReport()) {
                    return impfstatus.isImpfempfehlung() ? "1" : "";
                }
                return PrintUtil.trimToPlaceholder(impfstatus.isImpfempfehlung(), config.tl());
            });
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.krankheitImpfempfehlung." + this.krankheit;
    }
}
