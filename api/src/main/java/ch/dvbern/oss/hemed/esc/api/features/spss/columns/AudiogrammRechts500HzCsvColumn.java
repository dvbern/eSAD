package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class AudiogrammRechts500HzCsvColumn extends AbstractCsvColumn {

    public AudiogrammRechts500HzCsvColumn(@NonNull CsvColumnConfig config, @Nullable String overrideHeader) {
        super(config, overrideHeader);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungHoerenAudiogramm(
            einladung,
            untersuchungHoeren -> {
                if (untersuchungHoeren.getAudiogrammRechts() == null
                    || untersuchungHoeren.getAudiogrammRechts().getDbHz500() == null) {
                    return "";
                }
                return ExtractorForCsvUtil.dezibelStringValue(untersuchungHoeren.getAudiogrammRechts().getDbHz500());
            });
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.audiogrammRechts500Hz";
    }
}
