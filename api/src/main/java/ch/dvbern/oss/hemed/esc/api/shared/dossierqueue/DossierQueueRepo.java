package ch.dvbern.oss.hemed.esc.api.shared.dossierqueue;

import java.time.OffsetDateTime;
import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Repository
@RequiredArgsConstructor
public class DossierQueueRepo extends BaseRepo<DossierQueue> {

    @NonNull
    public List<DossierQueue> findAllUnfinished(long limit) {
        return db.selectFrom(QDossierQueue.dossierQueue)
            .where(QDossierQueue.dossierQueue.status.in(DossierQueueStatus.processableStatus()))
            .orderBy(QDossierQueue.dossierQueue.timestampErstellt.asc()) // Die aeltesten zuerst
            .limit(limit)
            .fetch();
    }

    @NonNull
    public List<DossierQueue> findAllFinishedAndOlderThan(@NonNull OffsetDateTime dateTime) {
        return db.selectFrom(QDossierQueue.dossierQueue)
            .where(QDossierQueue.dossierQueue.timestampResult.before(dateTime)
                .and(QDossierQueue.dossierQueue.status.in(DossierQueueStatus.finishStatus())))
            .fetch();
    }
}
