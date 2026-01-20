package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MassnahmenNachkontrolleMotorikCsvColumn extends AbstractCsvColumn {

    public MassnahmenNachkontrolleMotorikCsvColumn(
        @NonNull CsvColumnConfig config,
        @Nullable String overrideHeader) {
        super(config, overrideHeader);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungMassnahmen(
            einladung,
            untersuchungMassnahmen -> {
                if (config.forSpssReport()) {
                    return untersuchungMassnahmen.getNachkontrolle().isNachkontrolleMotorik() ? "1" : "";
                }
                return PrintUtil.trimToPlaceholder(
                    untersuchungMassnahmen.getNachkontrolle().isNachkontrolleMotorik(),
                    config.tl());
            }
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.massnahmenNachkontrolleMotorik";
    }
}
