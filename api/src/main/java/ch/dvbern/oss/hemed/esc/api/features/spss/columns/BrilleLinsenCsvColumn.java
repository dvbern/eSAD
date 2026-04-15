package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.types.BrilleLinsenResultat;
import ch.dvbern.oss.hemed.esc.api.shared.util.ValueEnhancedUtil;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Slf4j
public class BrilleLinsenCsvColumn extends AbstractCsvColumn {

    public BrilleLinsenCsvColumn(@NonNull CsvColumnConfig config, @Nullable String overrideHeader) {
        super(config, overrideHeader);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungSehen(
            einladung,
            sehen -> {
                BrilleLinsenResultat resultat =
                    ValueEnhancedUtil.getEnumValueOrNull(sehen.getBrilleLinsen(), BrilleLinsenResultat.class);
                if (resultat == null) {
                    return "";
                }
                if (!config.forSpssReport()) {
                    return config.tl().translateValue(resultat);
                }
                return switch (resultat) {
                    case OHNE_KEINE -> "1";
                    case MIT -> "2";
                    case OHNE_VERGESSEN -> "3";
                    case NICHT_GEPRUEFT -> "4";
                    default -> {
                        LOG.error(
                            "Unexpected value for BrilleLinsen, not allowed in spss report: {}",
                            sehen.getBrilleLinsen());
                        yield "";
                    }
                };
            });
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.brilleLinse";
    }
}
