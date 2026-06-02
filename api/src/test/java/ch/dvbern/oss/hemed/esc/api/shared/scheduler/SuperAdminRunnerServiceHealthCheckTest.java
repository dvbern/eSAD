package ch.dvbern.oss.hemed.esc.api.shared.scheduler;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.UntersuchungImpfungInternService;
import ch.dvbern.oss.hemed.esc.api.features.archivierung.ArchivdokumentService;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.features.kinddeactivation.KindDeactivationService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.KindService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.duplikate.DuplikatService;
import ch.dvbern.oss.hemed.esc.api.shared.archivierung.ArchivierungRuleService;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.DocumentQueueRunnerService;
import ch.dvbern.oss.hemed.esc.api.shared.dossierqueue.DossierQueueRunnerService;
import ch.dvbern.oss.hemed.esc.api.shared.dossierqueue.DossierQueueService;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.healthcheck.FileStoreHealthCheckService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SuperAdminRunnerServiceHealthCheckTest {

    private ConfigService configService;
    private FileStoreHealthCheckService fileStoreHealthCheckService;
    private SuperAdminRunnerService sut;

    @BeforeEach
    void setUp() {
        configService = mock(ConfigService.class);
        fileStoreHealthCheckService = mock(FileStoreHealthCheckService.class);
        sut = new SuperAdminRunnerService(
            mock(SchedulerConfigService.class),
            configService,
            mock(BenutzerService.class),
            mock(AppClock.class),
            mock(DocumentQueueRunnerService.class),
            mock(KindDeactivationService.class),
            mock(KindService.class),
            mock(DuplikatService.class),
            mock(UntersuchungImpfungInternService.class),
            mock(ArchivdokumentService.class),
            mock(DossierQueueRunnerService.class),
            mock(ArchivierungRuleService.class),
            mock(DossierQueueService.class),
            fileStoreHealthCheckService
        );
    }

    @Test
    void runFileStoreHealthCheckJob_enabledAndNotForced_runsCheck() {
        // ARRANGE
        when(configService.isFilestoreHealthcheckEnabled()).thenReturn(true);

        // ACT
        sut.runFileStoreHealthCheckJob(false);

        // ASSERT
        verify(fileStoreHealthCheckService).runForCurrentTenant();
    }

    @Test
    void runFileStoreHealthCheckJob_disabledAndNotForced_skipsCheck() {
        // ARRANGE
        when(configService.isFilestoreHealthcheckEnabled()).thenReturn(false);

        // ACT
        sut.runFileStoreHealthCheckJob(false);

        // ASSERT
        verify(fileStoreHealthCheckService, never()).runForCurrentTenant();
    }

    @Test
    void runFileStoreHealthCheckJob_disabledButForced_runsCheck() {
        // ARRANGE
        when(configService.isFilestoreHealthcheckEnabled()).thenReturn(false);

        // ACT
        sut.runFileStoreHealthCheckJob(true);

        // ASSERT
        verify(fileStoreHealthCheckService).runForCurrentTenant();
    }
}
