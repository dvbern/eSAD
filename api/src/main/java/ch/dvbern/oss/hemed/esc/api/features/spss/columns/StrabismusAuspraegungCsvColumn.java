package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.StrabismusAuspraegungResultat;
import ch.dvbern.oss.hemed.esc.api.shared.util.ValueEnhancedUtil;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class StrabismusAuspraegungCsvColumn extends AbstractCsvColumn {

    public StrabismusAuspraegungCsvColumn(@NonNull CsvColumnConfig config, @Nullable String overrideHeader) {
        super(config, overrideHeader);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungSehen(
            einladung,
            sehen -> {
                StrabismusAuspraegungResultat resultat =
                    ValueEnhancedUtil.getEnumValueOrNull(sehen.getStrabismusAuspraegung(), StrabismusAuspraegungResultat.class);
                if (resultat == null) {
                    return "";
                }
                if (!config.forSpssReport()) {
                    return config.tl().translateValue(resultat);
                }
                return switch (resultat) {
                    case KEIN_STRABISMUS -> "";
                    case MANIFEST -> "1";
                    case LATENT -> "2";
                    case INTERMITTIEREND -> "3";
                    case NICHT_GEPRUEFT -> "4";
                };
            }
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.strabismusAuspraegung";
    }
}
