package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.shared.person.LandCode;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class LandCodeCsvColumn extends AbstractCsvColumn {

    public LandCodeCsvColumn(@NonNull CsvColumnConfig config, @Nullable String overrideHeader) {
        super(config, overrideHeader);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungKindDaten(einladung, kindDaten -> {
            LandCode landCode = LandCode.getCode(kindDaten.getNationalitaet());
            return landCode != null ? String.valueOf(landCode.getCode()) : "";
        });
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.landCode";
    }
}
