package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Sprachfaehigkeit;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class SprachfaehigkeitMultiCsvColumn extends AbstractCsvColumn {

    private final Sprachfaehigkeit sprachfaehigkeit;

    public SprachfaehigkeitMultiCsvColumn(
        @NonNull CsvColumnConfig config,
        @Nullable String overrideHeader,
        @NonNull Sprachfaehigkeit sprachfaehigkeit) {
        super(config, overrideHeader);
        this.sprachfaehigkeit = sprachfaehigkeit;
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungArztBeurteilung(
            einladung,
            untersuchungArzt -> {
                if (config.forSpssReport()) {
                    return untersuchungArzt.getSprachfaehigkeitMultiValue().contains(sprachfaehigkeit) ? "1" : "";
                }
                return PrintUtil.trimToPlaceholder(
                    untersuchungArzt.getSprachfaehigkeitMultiValue()
                        .contains(sprachfaehigkeit), config.tl());
            }
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.sprachfaehigkeit." + this.sprachfaehigkeit;
    }
}
