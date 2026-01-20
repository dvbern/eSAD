package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Fingeropposition;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class FingeroppositionCsvColumn extends AbstractCsvColumn {

    private final Fingeropposition fingeropposition;

    public FingeroppositionCsvColumn(
        @NonNull CsvColumnConfig config,
        @Nullable String overrideHeader,
        @NonNull Fingeropposition fingeropposition) {
        super(config, overrideHeader);
        this.fingeropposition = fingeropposition;
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungArztBeurteilung(
            einladung,
            untersuchungArzt -> {
                if (config.forSpssReport()) {
                    return untersuchungArzt.getFingeropposition().contains(fingeropposition) ? "1" : "";
                }
                return PrintUtil.trimToPlaceholder(
                    untersuchungArzt.getFingeropposition().contains(fingeropposition),
                    config.tl());
            }
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.fingeropposition." + this.fingeropposition;
    }
}
