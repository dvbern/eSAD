package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Fingeropposition;
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
        return ExtractorForCsvUtil.extractUntersuchungArzt(
            einladung,
            untersuchungArzt ->
                untersuchungArzt.getFingeropposition().contains(fingeropposition) ? "1" : ""
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.fingeropposition";
    }
}
