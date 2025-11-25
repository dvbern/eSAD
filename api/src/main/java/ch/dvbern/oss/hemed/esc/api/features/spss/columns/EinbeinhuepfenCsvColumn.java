package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.types.Einbeinhuepfen;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class EinbeinhuepfenCsvColumn extends AbstractCsvColumn {

    public EinbeinhuepfenCsvColumn(
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
                if (untersuchungArzt.getEinbeinhuepfen() == null) {
                    return "";
                }
                return switch (untersuchungArzt.getEinbeinhuepfen()) {
                    case Einbeinhuepfen.SICHER -> "1";
                    case Einbeinhuepfen.UNSICHER_UNGEUEBT -> "2";
                    case Einbeinhuepfen.NICHT_MOEGLICH -> "3";
                    case Einbeinhuepfen.ANDERES, Einbeinhuepfen.NICHT_GEPRUEFT -> "4";
                };
            }
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.einbeinhuepfen";
    }
}
