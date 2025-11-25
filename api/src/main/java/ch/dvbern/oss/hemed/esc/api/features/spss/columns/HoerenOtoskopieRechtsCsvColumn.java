package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.OtoskopieResultat;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class HoerenOtoskopieRechtsCsvColumn extends AbstractCsvColumn {

    private final OtoskopieResultat otoskopieResultat;

    public HoerenOtoskopieRechtsCsvColumn(
        @NonNull CsvColumnConfig config,
        @Nullable String overrideHeader,
        @NonNull OtoskopieResultat otoskopieResultat) {
        super(config, overrideHeader);
        this.otoskopieResultat = otoskopieResultat;
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungHoerenOtoskopie(
            einladung,
            untersuchungHoeren ->
                untersuchungHoeren.getOtoskopieRechts().contains(otoskopieResultat) ? "1" : ""
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.hoereOtoskopieRechts";
    }
}
