package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfstatusKrankheitService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungInternService;
import org.checkerframework.checker.nullness.qual.NonNull;

public record CsvColumnConfig(
    @NonNull
    TL tl,

    @NonNull
    UntersuchungImpfstatusKrankheitService untersuchungImpfstatusKrankheitService,
    @NonNull
    UntersuchungImpfungInternService untersuchungImpfungInternService,

    boolean shouldTranslateHeader
) {
}
