package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Sprachfaehigkeit;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class SprachfaehigkeitCsvColumn extends AbstractCsvColumn {

    private final Sprachfaehigkeit sprachfaehigkeit;

    public SprachfaehigkeitCsvColumn(
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
            untersuchungArzt ->
                untersuchungArzt.getSprachfaehigkeit().contains(sprachfaehigkeit) ? "1" : ""
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.sprachfaehigkeit";
    }
}
