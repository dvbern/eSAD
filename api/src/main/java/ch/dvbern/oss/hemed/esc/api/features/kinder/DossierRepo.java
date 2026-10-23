package ch.dvbern.oss.hemed.esc.api.features.kinder;

import java.util.List;
import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.schule.QSchule;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import jakarta.persistence.FlushModeType;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Repository
@RequiredArgsConstructor
public class DossierRepo extends BaseRepo<Dossier> {

    /**
     * Reads the Dossier with a row lock, see {@code NachkontrolleService#getOrCreateByDossier}.
     * <p>
     * The query deliberately does not flush: flushing a Dossier that is still being built would validate
     * it too early - a new Kind's Dossier gets its Nachkontrolle before the responsible Arzt is set. So a
     * Dossier this transaction has created but not yet written is not found; nobody else can reach it
     * either, so it needs no lock.
     * <p>
     * The lock is taken with a query rather than {@code db.findLocked()}, see
     * {@code EinladungRepo#getByIdLockedQuery} for why that one does not lock.
     */
    @NonNull
    public Optional<Dossier> findByIdLockedQuery(@NonNull ID<Dossier> id) {
        return db.select(QDossier.dossier)
            .from(QDossier.dossier)
            .where(QDossier.dossier.id.eq(id.getUUID()))
            .setLockMode(LockModeType.PESSIMISTIC_WRITE)
            .setFlushMode(FlushModeType.COMMIT)
            .fetchOne();
    }

    @NonNull
    public Optional<Dossier> findByKind(@NonNull ID<Kind> kindID) {
        return db
            .selectFrom(QDossier.dossier)
            .innerJoin(QDossier.dossier.kind)
            .where(QKind.kind.id.eq(kindID.getUUID()))
            .fetchOne();
    }

    @NonNull
    public List<Dossier> findBySchule(@NonNull ID<Schule> schuleId) {
        return db.selectFrom(QDossier.dossier)
            .innerJoin(QDossier.dossier.kind, QKind.kind)
            .innerJoin(QKind.kind.schule, QSchule.schule) // trust schule on kind to be same as schule on klasse
            .where(QSchule.schule.id.eq(schuleId.getUUID())
                .and(QKind.kind.kindAktivStatus.ne(EntityAktivStatus.INAKTIV)))
            .orderBy(QKind.kind.name.asc(), QKind.kind.vorname.asc())
            .fetch();
    }

    @NonNull
    public Optional<Dossier> findByEsadId(@IgnoreForIdClassTest @NonNull String esadId) {
        return db.select(QDossier.dossier)
            .from(QDossier.dossier)
            .innerJoin(QDossier.dossier.kind, QKind.kind)
            .where(QKind.kind.esadId.eq(esadId))
            .fetchOne();
    }
}
