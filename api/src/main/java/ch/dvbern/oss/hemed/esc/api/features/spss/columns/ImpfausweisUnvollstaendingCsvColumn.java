package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ImpfausweisUnvollstaendingCsvColumn extends AbstractCsvColumn {

    public ImpfausweisUnvollstaendingCsvColumn(
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
            impfungen -> {
                if (config.forSpssReport()) {
                    return impfungen.isImpfausweisUnvollstaendig() ? "1" : "";
                }
                return PrintUtil.trimToPlaceholder(impfungen.isImpfausweisUnvollstaendig(), config.tl());
            });
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.impfausweisUnvollstaendig";
    }
}
