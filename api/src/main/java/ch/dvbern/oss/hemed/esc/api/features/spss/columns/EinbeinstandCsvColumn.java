package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
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
                Einbeinstand einbeinstand = untersuchungArzt.getEinbeinstand();
                if (einbeinstand == null) {
                    return "";
                }

                if (!config.forSpssReport()) {
                    return config.tl().translateValue(einbeinstand);
                }

                return switch (einbeinstand) {
                    case Einbeinstand.SICHER -> "1";
                    case Einbeinstand.UNSICHER_UNGEUEBT -> "2";
                    case Einbeinstand.NICHT_MOEGLICH -> "3";
                    case Einbeinstand.ANDERES, Einbeinstand.NICHT_GEPRUEFT -> "4";
                    default -> throw new IllegalStateException("Unexpected value for Einbeinstand: " + einbeinstand);
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
