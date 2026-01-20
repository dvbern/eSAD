package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.OtoskopieResultat;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil;
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
            untersuchungHoeren -> {
                if (config.forSpssReport()) {
                    return untersuchungHoeren.getOtoskopieRechts().contains(otoskopieResultat) ? "1" : "";
                }
                return PrintUtil.trimToPlaceholder(untersuchungHoeren.getOtoskopieRechts().contains(otoskopieResultat), config.tl());
            }
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.hoereOtoskopieRechts." + this.otoskopieResultat;
    }
}
