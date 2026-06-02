package ch.dvbern.oss.hemed.esc.api.shared.dossierqueue;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.shared.archivierung.ArchivierungService;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.ZustaendigePersonService;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSetting;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DossierQueueServiceTest {

    private DossierQueueRepo dossierQueueRepo;
    private ConfigService configService;
    private AppClock appClock;
    private ArchivierungService archivierungService;
    private DossierQueueService service;
    private TenantSettingsService tenantSettingsService;

    @BeforeEach
    void setUp() {
        dossierQueueRepo = mock(DossierQueueRepo.class);
        configService = mock(ConfigService.class);
        appClock = mock(AppClock.class);
        archivierungService = mock(ArchivierungService.class);
        ZustaendigePersonService zustaendigePersonService = mock(ZustaendigePersonService.class);
        tenantSettingsService = mock(TenantSettingsService.class);
        service = new DossierQueueService(
            dossierQueueRepo,
            configService,
            appClock,
            archivierungService,
            zustaendigePersonService);
    }

    @Test
    void testFindAllUnfinished() {
        service.findAllUnfinished(10L);
        verify(dossierQueueRepo).findAllUnfinished(10L);
    }

    @Test
    void testUpdate() {
        // ARRANGE
        ID<DossierQueue> id = ID.of(UUID.randomUUID(), DossierQueue.class);
        DossierQueue existing = new DossierQueue();
        existing.setId(id);
        when(dossierQueueRepo.getById(id)).thenReturn(existing);

        DossierQueue updateData = new DossierQueue();
        updateData.setId(id);
        updateData.setStatus(DossierQueueStatus.SUCCESS);
        updateData.setLastError("No error");
        OffsetDateTime now = OffsetDateTime.now();
        updateData.setTimestampResult(now);
        updateData.setErrorCount(1);

        // ACT
        service.update(updateData);

        // ASSERT
        verify(dossierQueueRepo).persist(existing);
        assertThat(existing.getStatus()).isEqualTo(DossierQueueStatus.SUCCESS);
        assertThat(existing.getLastError()).isEqualTo("No error");
        assertThat(existing.getTimestampResult()).isEqualTo(now);
        assertThat(existing.getErrorCount()).isEqualTo(1);
    }

    @Test
    void testMarkAsInProgress() {
        // ARRANGE
        ID<DossierQueue> id = ID.of(UUID.randomUUID(), DossierQueue.class);
        DossierQueue existing = new DossierQueue();
        existing.setId(id);
        existing.setStatus(DossierQueueStatus.NEW);
        when(dossierQueueRepo.getById(id)).thenReturn(existing);

        // ACT
        service.markAsInProgress(id);

        // ASSERT
        verify(dossierQueueRepo).persist(existing);
        assertThat(existing.getStatus()).isEqualTo(DossierQueueStatus.IN_PROGRESS);
    }

    @Test
    void testCreateDossierQueueItem() {
        // ARRANGE
        ID<Dossier> dossierID = ID.of(UUID.randomUUID(), Dossier.class);

        // ACT
        service.createDossierQueueItem(dossierID, DossierQueueTyp.ARCHIVIERUNG, null);

        // ASSERT
        ArgumentCaptor<DossierQueue> captor = ArgumentCaptor.forClass(DossierQueue.class);
        verify(dossierQueueRepo).persist(captor.capture());
        DossierQueue created = captor.getValue();
        assertThat(created.getDossierQueueTyp()).isEqualTo(DossierQueueTyp.ARCHIVIERUNG);
        assertThat(created.getStatus()).isEqualTo(DossierQueueStatus.NEW);
        assertThat(created.getDossierID()).isEqualTo(dossierID.getUUID());
    }

    @Test
    void testGetById() {
        ID<DossierQueue> id = ID.of(UUID.randomUUID(), DossierQueue.class);
        service.getById(id);
        verify(dossierQueueRepo).getById(id);
    }

    @Test
    void testProcessDossierQueueItem_Archivierung() {
        // ARRANGE
        UUID dossierId = UUID.randomUUID();
        DossierQueue item = new DossierQueue();
        item.setDossierQueueTyp(DossierQueueTyp.ARCHIVIERUNG);
        item.setDossierID(dossierId);

        // ACT
        service.processDossierQueueItem(item);

        // ASSERT
        verify(archivierungService).archivieren(ID.of(dossierId, Dossier.class));
    }

    @Test
    void testProcessDocumentQueueItem_ArchivierungDelete() {
        // ARRANGE
        UUID dossierId = UUID.randomUUID();
        DossierQueue item = new DossierQueue();
        item.setDossierQueueTyp(DossierQueueTyp.ARCHIVIERUNG_DELETE);
        item.setDossierID(dossierId);

        // ACT
        service.processDossierQueueItem(item);

        // ASSERT
        verify(archivierungService).delete(ID.of(dossierId, Dossier.class));
    }

    @Test
    void testCleanUpOldDossierQueue() {
        // ARRANGE
        OffsetDateTime now = OffsetDateTime.parse("2026-03-18T10:00:00Z");
        AppClock.OffsetDateTimeProvider clockProvider = mock(AppClock.OffsetDateTimeProvider.class);
        when(appClock.offsetDateTime()).thenReturn(clockProvider);
        when(clockProvider.now()).thenReturn(now);

        when(configService.getDossierQueueMaxLifeTime()).thenReturn(Duration.ofDays(30));
        OffsetDateTime cutoff = now.minusDays(30);

        DossierQueue item1 = new DossierQueue();
        item1.setId(ID.of(UUID.randomUUID(), DossierQueue.class));
        item1.setStatus(DossierQueueStatus.SUCCESS);

        DossierQueue item2 = new DossierQueue();
        item2.setId(ID.of(UUID.randomUUID(), DossierQueue.class));
        item2.setStatus(DossierQueueStatus.FAILED);

        when(dossierQueueRepo.findAllFinishedAndOlderThan(cutoff)).thenReturn(List.of(item1, item2));

        TenantSetting tenantSetting = mock(TenantSetting.class);
        when(tenantSettingsService.getTenantSetting()).thenReturn(tenantSetting);
        when(tenantSetting.getSupportEmail()).thenReturn("support@example.com");

        // ACT
        long count = service.cleanUpOldDossierQueue();

        // ASSERT
        assertThat(count).isEqualTo(2);
        verify(dossierQueueRepo).remove(item1.getId());
        verify(dossierQueueRepo).remove(item2.getId());
    }
}
