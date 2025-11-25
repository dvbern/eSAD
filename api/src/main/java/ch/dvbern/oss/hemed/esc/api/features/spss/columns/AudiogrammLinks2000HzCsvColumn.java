package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class AudiogrammLinks2000HzCsvColumn extends AbstractCsvColumn {

    public AudiogrammLinks2000HzCsvColumn(@NonNull CsvColumnConfig config, @Nullable String overrideHeader) {
        super(config, overrideHeader);
    }

    @Override
    @NonNull
    public String extract(@NonNull Einladung einladung) {
        return ExtractorForCsvUtil.extractUntersuchungHoerenAudiogramm(
            einladung,
            untersuchungHoeren -> {
                if (untersuchungHoeren.getAudiogrammLinks() == null
                    || untersuchungHoeren.getAudiogrammLinks().getDbHz2000() == null) {
                    return "";
                }
                return ExtractorForCsvUtil.dezibelStringValue(untersuchungHoeren.getAudiogrammLinks().getDbHz2000());
            });
    }

    @Override
    @NonNull
    protected String getDefaultHeaderKey() {
        return "CsvColumn.audiogrammLinks2000Hz";
    }
}
