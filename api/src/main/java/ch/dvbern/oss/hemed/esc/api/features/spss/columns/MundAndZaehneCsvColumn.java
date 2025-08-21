package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.MundAndZaehne;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MundAndZaehneCsvColumn extends AbstractCsvColumn {

    private final MundAndZaehne mundAndZaehne;

    public MundAndZaehneCsvColumn(
        @NonNull CsvColumnConfig config,
        @Nullable String overrideHeader,
        @NonNull MundAndZaehne mundAndZaehne) {
        super(config, overrideHeader);
        this.mundAndZaehne = mundAndZaehne;
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungArzt(
            einladung,
            untersuchungArzt ->
                untersuchungArzt.getMundAndZaehne().contains(mundAndZaehne) ? "1" : ""
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.mundAndZaehne";
    }
}
