package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class QuartierCsvColumn extends AbstractCsvColumn {

    public QuartierCsvColumn(@NonNull CsvColumnConfig config, @Nullable String overrideHeader) {
        super(config, overrideHeader);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        Schule schule = einladung.getDossier().getKind().extractSchule();

        if (schule == null || schule.getQuartier() == null) {
            return "";
        }

        if (!config.forSpssReport()) {
            return schule.getQuartier().getQuartierName();
        }

        return switch (schule.getQuartier().getQuartierName()) {
            case "GBW" -> "10";
            case "GBO" -> "11";
            case "KB" -> "12";
            case "LAND" -> "13";
            default -> "";
        };
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.quartier";
    }
}
