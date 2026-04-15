package ch.dvbern.oss.hemed.esc.api.shared.dossierqueue;

import java.time.OffsetDateTime;
import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.shared.archivierung.ArchivierungService;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.ZustaendigePersonService;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Service
@RequiredArgsConstructor
@Slf4j
public class DossierQueueService {

    private final DossierQueueRepo dossierQueueRepo;
    private final ConfigService configService;
    private final AppClock appClock;
    private final ArchivierungService archivierungService;
    private final ZustaendigePersonService zustaendigePersonService;

    @NonNull
    @Transactional(TxType.REQUIRED)
    public List<DossierQueue> findAllUnfinished(long limit) {
        return dossierQueueRepo.findAllUnfinished(limit);
    }

    @Transactional(TxType.REQUIRES_NEW)
    public void update(@NonNull DossierQueue dossierQueue) {
        DossierQueue managedEntity = getById(dossierQueue.getId());
        managedEntity.setStatus(dossierQueue.getStatus());
        managedEntity.setLastError(dossierQueue.getLastError());
        managedEntity.setTimestampResult(dossierQueue.getTimestampResult());
        managedEntity.setErrorCount(dossierQueue.getErrorCount());
        dossierQueueRepo.persist(managedEntity);
    }

    @Transactional(TxType.REQUIRES_NEW)
    public void markAsInProgress(@NonNull ID<DossierQueue> id) {
        DossierQueue managedEntity = getById(id);
        managedEntity.setStatus(DossierQueueStatus.IN_PROGRESS);
        dossierQueueRepo.persist(managedEntity);
    }

    @Transactional(TxType.REQUIRED)
    public void createDossierQueueItem(
        @NonNull ID<Dossier> dossierId,
        @NonNull DossierQueueTyp dossierQueueTyp,
        @Nullable String paramters) {
        DossierQueue queue = DossierQueue.of(dossierId.getUUID(), dossierQueueTyp, paramters);
        dossierQueueRepo.persist(queue);
    }

    @NonNull
    public DossierQueue getById(@NonNull ID<DossierQueue> id) {
        return dossierQueueRepo.getById(id);
    }

    @Transactional(TxType.NOT_SUPPORTED)
    public void processDossierQueueItem(@NonNull DossierQueue dossierQueue) {
        switch (dossierQueue.getDossierQueueTyp()) {
        case VERANTW_CHANGED_ON_SCHULE -> zustaendigePersonService.verantwortlicherChangedOnSchule(
            ID.of(dossierQueue.getDossierID(), Dossier.class),
            dossierQueue.getParameters());
        case ARCHIVIERUNG -> archivierungService.archivieren(
            ID.of(dossierQueue.getDossierID(), Dossier.class));
        case ARCHIVIERUNG_DELETE -> archivierungService.delete(ID.of(dossierQueue.getDossierID(), Dossier.class));
        default -> throw new AppFailureException(AppFailureMessage.internalError(
            "Unhandled Type in DossierQueue generator: " + dossierQueue.getDossierQueueTyp()));
        }
    }

    @Transactional(TxType.REQUIRED)
    public long cleanUpOldDossierQueue() {
        OffsetDateTime cutoffDateTime =
            appClock.offsetDateTime().now().minus(configService.getDossierQueueMaxLifeTime());
        List<DossierQueue> dossierQueuesToDelete = dossierQueueRepo.findAllFinishedAndOlderThan(cutoffDateTime);
        for (DossierQueue dossierQueue : dossierQueuesToDelete) {
            dossierQueueRepo.remove(dossierQueue.getId());
        }
        return dossierQueuesToDelete.size();
    }
}
