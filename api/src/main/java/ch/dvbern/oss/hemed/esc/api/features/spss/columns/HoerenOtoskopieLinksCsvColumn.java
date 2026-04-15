package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.types.OtoskopieResultat;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintUtil;
import ch.dvbern.oss.hemed.esc.api.shared.util.ValueEnhancedUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class HoerenOtoskopieLinksCsvColumn extends AbstractCsvColumn {

    private final OtoskopieResultat otoskopieResultat;

    public HoerenOtoskopieLinksCsvColumn(
        @NonNull CsvColumnConfig config,
        @Nullable String overrideHeader,
        @NonNull OtoskopieResultat otoskopieResultat
    ) {
        super(config, overrideHeader);
        this.otoskopieResultat = otoskopieResultat;
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungHoerenOtoskopie(
            einladung,
            untersuchungHoeren -> {
                boolean result =
                    ValueEnhancedUtil.getEnumSetOrEmpty(untersuchungHoeren.getOtoskopieLinks(), OtoskopieResultat.class)
                        .contains(otoskopieResultat);
                if (config.forSpssReport()) {
                    return result ? "1" : "";
                }
                return PrintUtil.trimToPlaceholder(result, config.tl());
            }
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.hoereOtoskopieLinks." + this.otoskopieResultat;
    }
}
