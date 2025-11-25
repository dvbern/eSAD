package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class AudiogrammLinks6000HzCsvColumn extends AbstractCsvColumn {

    public AudiogrammLinks6000HzCsvColumn(@NonNull CsvColumnConfig config, @Nullable String overrideHeader) {
        super(config, overrideHeader);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungHoerenAudiogramm(
            einladung,
            untersuchungHoeren -> {
                if (untersuchungHoeren.getAudiogrammLinks() == null
                    || untersuchungHoeren.getAudiogrammLinks().getDbHz6000() == null) {
                    return "";
                }
                return ExtractorForCsvUtil.dezibelStringValue(untersuchungHoeren.getAudiogrammLinks().getDbHz6000());
            });
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.audiogrammLinks6000Hz";
    }
}
