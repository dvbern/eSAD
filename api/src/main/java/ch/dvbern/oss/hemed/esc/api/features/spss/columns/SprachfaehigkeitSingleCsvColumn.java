package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import org.checkerframework.checker.nullness.qual.NonNull;

public class SprachfaehigkeitSingleCsvColumn extends AbstractCsvColumn {

    public SprachfaehigkeitSingleCsvColumn(@NonNull CsvColumnConfig config) {
        super(config, null);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungArztBeurteilung(
            einladung,
            untersuchungArzt -> {
                if (untersuchungArzt.getSprachfaehigkeitSingleValue() == null) {
                    return "";
                }
                return config.tl().translateValue(untersuchungArzt.getSprachfaehigkeitSingleValue());
            }
        );
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.sprachfaehigkeitSingle";
    }
}
