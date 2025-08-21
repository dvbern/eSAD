package ch.dvbern.oss.hemed.esc.api.shared.documentqueue;

import java.time.OffsetDateTime;
import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Repository
@RequiredArgsConstructor
public class DocumentQueueRepo extends BaseRepo<DocumentQueue> {

    @NonNull
    public List<DocumentQueue> findAllUnfinished(long limit) {
        return db.selectFrom(QDocumentQueue.documentQueue)
            .where(QDocumentQueue.documentQueue.status.in(DocumentQueueStatus.processableStatus()))
            .limit(limit)
            .fetch();
    }

    @NonNull
    public List<DocumentQueue> findAll() {
        return db.selectFrom(QDocumentQueue.documentQueue)
            .orderBy(QDocumentQueue.documentQueue.timestampErstellt.desc())
            .fetch();
    }

    @NonNull
    public List<DocumentQueue> findAllByBenutzer(@NonNull ID<Benutzer> benutzerId) {
        return db.selectFrom(QDocumentQueue.documentQueue)
            .where(QDocumentQueue.documentQueue.benutzer.id.eq(benutzerId.getUUID()))
            .orderBy(QDocumentQueue.documentQueue.timestampErstellt.desc())
            .fetch();
    }

    @NonNull
    public List<DocumentQueue> findAllUnfinishedByBenutzer(@NonNull ID<Benutzer> benutzerId) {
        return db.selectFrom(QDocumentQueue.documentQueue)
            .where(QDocumentQueue.documentQueue.benutzer.id.eq(benutzerId.getUUID())
                .and(QDocumentQueue.documentQueue.status.in(DocumentQueueStatus.processableStatus())))
            .orderBy(QDocumentQueue.documentQueue.timestampErstellt.desc())
            .fetch();
    }

    @NonNull
    public DocumentQueue update(@NonNull DocumentQueue documentQueue) {
        db.persist(documentQueue);
        return documentQueue;
    }

    @NonNull
    public List<DocumentQueue> findAllFinishedAndOlderThan(@NonNull OffsetDateTime dateTime) {
        return db.selectFrom(QDocumentQueue.documentQueue)
            .where(QDocumentQueue.documentQueue.timestampResult.before(dateTime)
                .and(QDocumentQueue.documentQueue.status.in(DocumentQueueStatus.finishStatus())))
            .fetch();
    }
}
