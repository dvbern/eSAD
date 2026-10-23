package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfstatusKrankheitService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungInternService;

import static org.mockito.Mockito.mock;

public abstract class AbstractBaseColumnTest {
    final TL tlMock = mock(TL.class);
    final UntersuchungImpfstatusKrankheitService untersuchungImpfstatusKrankheitServiceMock =
        mock(UntersuchungImpfstatusKrankheitService.class);
    final UntersuchungImpfungInternService untersuchungImpfungInternServiceMock =
        mock(UntersuchungImpfungInternService.class);

    final CsvColumnConfig config =
        new CsvColumnConfig(
            tlMock,
            untersuchungImpfstatusKrankheitServiceMock,
            untersuchungImpfungInternServiceMock,
            false,
            true);

    final CsvColumnConfig configForCsv =
        new CsvColumnConfig(
            tlMock,
            untersuchungImpfstatusKrankheitServiceMock,
            untersuchungImpfungInternServiceMock,
            false,
            false);

}
