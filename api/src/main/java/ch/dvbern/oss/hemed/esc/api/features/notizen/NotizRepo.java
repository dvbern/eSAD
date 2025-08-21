package ch.dvbern.oss.hemed.esc.api.features.notizen;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.QDossier;
import ch.dvbern.oss.hemed.esc.api.features.labels.QLabel;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileData;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.QFileData;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Repository
@RequiredArgsConstructor
public class NotizRepo extends BaseRepo<Notiz> {

    @NonNull
    public List<Notiz> findAllForKind(@NonNull ID<Kind> kindID) {
        return db.selectFrom(QNotiz.notiz)
            .leftJoin(QNotiz.notiz.labels, QLabel.label).fetchJoin() // prefetch
            .leftJoin(QNotiz.notiz.files, QFileData.fileData) // Hier darf kein prefetch sein, siehe ESC-736
            .innerJoin(QNotiz.notiz.dossier, QDossier.dossier)
            .where(QDossier.dossier.kind.id.eq(kindID.getUUID()))
            .orderBy(QNotiz.notiz.timestampErstellt.desc())
            .fetch();
    }

    @NonNull
    public Notiz addUploadedFile(@NonNull ID<Notiz> notizID, FileData filedata) {
        Notiz lockedNotiz = db.findLocked(notizID, LockModeType.PESSIMISTIC_WRITE)
            .orElseThrow(() -> new AppFailureException(AppFailureMessage.entityNotFound(notizID)));

        lockedNotiz.getFiles().add(filedata);
        this.persist(lockedNotiz);
        return lockedNotiz;
    }
}
