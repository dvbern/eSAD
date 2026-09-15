package ch.dvbern.oss.hemed.esc.api.features.archivierung;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import ch.dvbern.dvbstarter.infrastructure.db.SmartJPAQuery;
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

import com.querydsl.core.types.Expression;
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
    public List<Archivdokument> findAllForBenutzer(
        ID<Benutzer> benutzerID,
        List<ArchivierungStatus> archivierungStatusList) {
        return findAllForBenutzer(benutzerID, archivierungStatusList, null);
    }
    @NonNull
    public List<Archivdokument> findAllForBenutzer(
        ID<Benutzer> benutzerID,
        List<ArchivierungStatus> archivierungStatusList,
        @Nullable Integer limit) {
        return findAllForBenutzerQuery(QArchivdokument.archivdokument, benutzerID, archivierungStatusList, limit).fetch();
    }

    @NonNull
    public List<ID<Archivdokument>> findAllArchivdokIDsForBenutzer(
        ID<Benutzer> benutzerID,
        List<ArchivierungStatus> archivierungStatusList,
        @Nullable Integer limit) {
        List<UUID> ids = findAllForBenutzerQuery(QArchivdokument.archivdokument.id, benutzerID, archivierungStatusList, limit).fetch();
        return ids.stream()
            .map(uid -> ID.of(uid, Archivdokument.class))
            .toList();
    }

    private <T> SmartJPAQuery<T> findAllForBenutzerQuery(
        Expression<T> projection,
        ID<Benutzer> benutzerID,
        List<ArchivierungStatus> archivierungStatusList,
        @Nullable Integer limit) {
        var archivdokument = QArchivdokument.archivdokument;
        var query = db
            .select(projection)
            .from(archivdokument)
            .where(
                archivdokument.archivierungStatus.in(archivierungStatusList),
                archivdokument.owner.id.eq(benutzerID.getUUID()))
            .orderBy(archivdokument.timestampArchiviert.asc());
        if (limit != null) {
            query.limit(limit);
        }
        return query;
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
