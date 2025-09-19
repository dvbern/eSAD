package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Einbeinstand;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class EinbeinstandCsvColumn extends AbstractCsvColumn {

    public EinbeinstandCsvColumn(
        @NonNull CsvColumnConfig config,
        @Nullable String overrideHeader) {
        super(config, overrideHeader);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungArztBeurteilung(
            einladung,
            untersuchungArzt -> {
                if (untersuchungArzt.getEinbeinstand() == null) {
                    return "";
                }
                return switch (untersuchungArzt.getEinbeinstand()) {
                    case Einbeinstand.SICHER -> "1";
                    case Einbeinstand.UNSICHER_UNGEUEBT -> "2";
                    case Einbeinstand.NICHT_MOEGLICH -> "3";
                    case Einbeinstand.ANDERES, Einbeinstand.NICHT_GEPRUEFT -> "4";
                };
            }
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.einbeinstand";
    }
}
