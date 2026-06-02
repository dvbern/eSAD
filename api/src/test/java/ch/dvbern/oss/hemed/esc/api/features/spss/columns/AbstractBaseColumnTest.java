package ch.dvbern.oss.hemed.esc.api.features.spss.columns;

import ch.dvbern.dvbstarter.shared.i18n.translations.TL;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfstatusKrankheitService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungInternService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import org.mockito.Mockito;

public abstract class AbstractBaseColumnTest {
    final TL tlMock = Mockito.mock(TL.class);
    final UntersuchungImpfstatusKrankheitService untersuchungImpfstatusKrankheitServiceMock =
        Mockito.mock(UntersuchungImpfstatusKrankheitService.class);
    final UntersuchungImpfungInternService untersuchungImpfungInternServiceMock =
        Mockito.mock(UntersuchungImpfungInternService.class);
    final TenantProducer tenantProducerMock =
        Mockito.mock(TenantProducer.class);

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
