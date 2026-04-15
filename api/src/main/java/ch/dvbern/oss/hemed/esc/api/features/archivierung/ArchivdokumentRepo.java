package ch.dvbern.oss.hemed.esc.api.features.archivierung;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.QDossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.QKind;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.QBenutzer;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Repository
@RequiredArgsConstructor
public class ArchivdokumentRepo extends BaseRepo<Archivdokument> {

    private final AppClock appClock;

    @NonNull
    public List<Archivdokument> findAll() {
        return db
            .selectFrom(QArchivdokument.archivdokument)
            .orderBy(QArchivdokument.archivdokument.timestampArchiviert.asc())
            .fetch();
    }

    @NonNull
    public List<Archivdokument> findAllForDossier(@NonNull ID<Dossier> dossierID) {
        return db
            .selectFrom(QArchivdokument.archivdokument)
            .where(QArchivdokument.archivdokument.dossierID.eq(dossierID.getUUID()))
            .fetch();
    }

    @Nullable
    public Archivdokument getByDossierForDeletion(@NonNull ID<Dossier> dossierId) {
        return db.selectFrom(QArchivdokument.archivdokument)
            .where(QArchivdokument.archivdokument.dossierID.eq(dossierId.getUUID())
                .and(QArchivdokument.archivdokument.archivierungTyp.eq(ArchivierungTyp.KIND_DEAKTIVIERT)))
            .fetchOne()
            .orElse(null);
    }

    @NonNull
    public List<Archivdokument> findAll(List<ArchivierungStatus> archivierungStatusList) {
        return db
            .selectFrom(QArchivdokument.archivdokument)
            .where(QArchivdokument.archivdokument.archivierungStatus.in(archivierungStatusList))
            .orderBy(QArchivdokument.archivdokument.timestampArchiviert.asc())
            .fetch();
    }

    @NonNull
    public List<Archivdokument> findAllBenutzer(
        ID<Benutzer> benutzerID,
        List<ArchivierungStatus> archivierungStatusList) {
        return db
            .selectFrom(QArchivdokument.archivdokument)
            .where(QArchivdokument.archivdokument.archivierungStatus.in(archivierungStatusList)
                .and(QArchivdokument.archivdokument.owner.id.eq(benutzerID.getUUID())))
            .orderBy(QArchivdokument.archivdokument.timestampArchiviert.asc())
            .fetch();
    }

    @NonNull
    public List<Benutzer> findArchivOwnersHavingDocumentsInStatus(ArchivierungStatus archivierungStatus) {
        return db.selectDistinct(QBenutzer.benutzer)
            .from(QArchivdokument.archivdokument)
            .innerJoin(QArchivdokument.archivdokument.owner, QBenutzer.benutzer)
            .where(QArchivdokument.archivdokument.archivierungStatus.eq(archivierungStatus))
            .fetch();
    }

    @NonNull
    public List<ID<Dossier>> findKinderAelterAlsUndNichtArchiviert(int minAge, int batchsize) {
        LocalDate cutoffDate = appClock.getToday().minusYears(minAge);
        return db.select(QDossier.dossier.id)
            .from(QDossier.dossier)
            .innerJoin(QDossier.dossier.kind, QKind.kind)
            .where(QKind.kind.geburtsdatum.before(cutoffDate)
                .and(QDossier.dossier.timestampDefinitivArchiviert.isNull()))
            .limit(batchsize)
            .fetch()
            .stream()
            .map(uid -> ID.of(uid, Dossier.class))
            .toList();
    }

    @NonNull
    public List<ID<Dossier>> findKinderThatAreReadyToDelete(int monthsBeforeDeletion, int batchsize) {
        OffsetDateTime cutoffDate = appClock.offsetDateTime().now().minusMonths(monthsBeforeDeletion);

        return db.select(QDossier.dossier.id)
            .from(QDossier.dossier)
            .innerJoin(QDossier.dossier.kind, QKind.kind)
            .where(QKind.kind.kindAktivStatus.eq(EntityAktivStatus.INAKTIV)
                .and(db.selectOne()
                    .from(QArchivdokument.archivdokument)
                    .where(QArchivdokument.archivdokument.dossierID.eq(QDossier.dossier.id)
                        .and(QArchivdokument.archivdokument.archivierungTyp.eq(ArchivierungTyp.KIND_DEAKTIVIERT))
                        .and(QArchivdokument.archivdokument.archivierungStatus.eq(ArchivierungStatus.DOWNLOADED_READY_FOR_DELETION))
                        .and(QArchivdokument.archivdokument.timestampDownloaded.lt(cutoffDate)))
                    .exists())
            )
            .limit(batchsize)
            .fetch()
            .stream()
            .map(uid -> ID.of(uid, Dossier.class))
            .toList();
    }
}
