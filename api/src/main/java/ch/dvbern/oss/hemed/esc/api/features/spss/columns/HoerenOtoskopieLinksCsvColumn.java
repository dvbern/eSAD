package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.OtoskopieResultat;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class HoerenOtoskopieLinksCsvColumn extends AbstractCsvColumn {

    private final OtoskopieResultat otoskopieResultat;

    public HoerenOtoskopieLinksCsvColumn(
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
                untersuchungHoeren.getOtoskopieLinks().contains(otoskopieResultat) ? "1" : ""
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.hoereOtoskopieLinks";
    }
}
