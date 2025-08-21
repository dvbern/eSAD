package ch.dvbern.oss.hemed.esc.api.features.anlass;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.EinladungStatus;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.QDossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.QKind;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Repository
@RequiredArgsConstructor
public class EinladungRepo extends BaseRepo<Einladung> {

    @NonNull
    public Einladung getByIdLockedQuery(@NonNull ID<Einladung> id) {
        // we do this with a query instead of through db.findLocked() because findLocked does not work
        // it generates a warning  'HHH000444: Encountered request for locking however dialect reports that
        // database prefers locking be done in a separate select (follow-on locking); results will be locked after initial query executes'
        // and it does not work, the generated query ends with select ... for update, when using the lockMode in an actual query
        // the lock mode in sql ends up as select ... for no key update which seems to work

        return db.select(QEinladung.einladung)
            .from(QEinladung.einladung)
            .where(QEinladung.einladung.id.eq(id.getUUID()))
            .setLockMode(LockModeType.PESSIMISTIC_WRITE)
            .fetchOne()
            .orElseThrow(() -> new AppFailureException(AppFailureMessage.entityNotFound(id)));
    }

    @NonNull
    public List<Einladung> findByAnlass(@NonNull ID<Anlass> anlassID) {
        return db.selectFrom(QEinladung.einladung)
            .innerJoin(QEinladung.einladung.anlass, QAnlass.anlass)
            .fetchJoin()
            .where(QAnlass.anlass.id.eq(anlassID.getUUID()))
            .orderBy(QEinladung.einladung.dossier.kind.name.asc())
            .fetch();
    }

    @NonNull
    public Optional<Einladung> findByAnlassUndKind(@NonNull ID<Anlass> anlassID, @NonNull ID<Kind> kindID) {
        return db.selectFrom(QEinladung.einladung)
            .innerJoin(QEinladung.einladung.anlass, QAnlass.anlass)
            .innerJoin(QEinladung.einladung.dossier, QDossier.dossier)
            .innerJoin(QDossier.dossier.kind, QKind.kind)
            .where(QAnlass.anlass.id.eq(anlassID.getUUID())
                .and(QKind.kind.id.eq(kindID.getUUID())))
            .fetchOne();
    }

    @NonNull
    public List<Einladung> getUntersuchungTabInfos(@NonNull List<ID<Einladung>> einladungIds) {
        return db.selectFrom(QEinladung.einladung)
            .where(QEinladung.einladung.id.in(einladungIds.stream().map(ID::getUUID).toList()))
            .fetch();
    }

    /**
     * Gibt die letzte Einladung für dieses Kind mit dem gleichen AnlassTyp zurück, die vor dem aktuellen Anlass liegt.
     * Also bei 5 Anlässen, wenn currentAnlass der 3. ist, wird der 2. zurückgegeben.
     * Die Einladung muss den Status NACHZUEGLER haben.
     */
    @NonNull
    public Optional<Einladung> findUrspruenglicheEinladungForNachzuegler(
        @NonNull Anlass currentAnlass,
        @NonNull ID<Kind> kindID) {
        return db.selectFrom(QEinladung.einladung)
            .innerJoin(QEinladung.einladung.anlass, QAnlass.anlass)
            .innerJoin(QAnlass.anlass.anlassConfig, QAnlassConfig.anlassConfig)
            .innerJoin(QEinladung.einladung.dossier, QDossier.dossier)
            .join(QDossier.dossier.kind, QKind.kind)
            .where(QAnlassConfig.anlassConfig.anlassTyp.eq(currentAnlass.getAnlassConfig().getAnlassTyp())
                .and(QKind.kind.id.eq(kindID.getUUID()))
                .and(QAnlass.anlass.id.ne(currentAnlass.getId().getUUID()))
                .and(QAnlass.anlass.datum.before(currentAnlass.getDatum())
                .and(QEinladung.einladung.einladungStatus.eq(EinladungStatus.NACHZUEGLER)))
            )
            .orderBy(QAnlass.anlass.datum.desc())
            .limit(1)
            .fetchOne();
    }

    /**
     * Gibt die letzte Einladung für dieses Kind zurück, die vor dem aktuellen Anlass liegt.
     * Also bei 5 Anlässen, wenn currentAnlass der 3. ist, wird der 2. zurückgegeben.
     */
    @NonNull
    public Optional<Einladung> findLastEinladungForKind(@NonNull Anlass currentAnlass, @NonNull ID<Kind> kindID) {
        return db.selectFrom(QEinladung.einladung)
            .innerJoin(QEinladung.einladung.anlass, QAnlass.anlass)
            .innerJoin(QAnlass.anlass.anlassConfig, QAnlassConfig.anlassConfig)
            .innerJoin(QEinladung.einladung.dossier, QDossier.dossier)
            .join(QDossier.dossier.kind, QKind.kind)
            .where(QKind.kind.id.eq(kindID.getUUID())
                .and(QAnlass.anlass.id.ne(currentAnlass.getId().getUUID()))
                .and(QAnlass.anlass.datum.before(currentAnlass.getDatum()))
            )
            .orderBy(QAnlass.anlass.datum.desc())
            .limit(1)
            .fetchOne();
    }

    @NonNull
    public List<Einladung> findAllByDossier(@NonNull ID<Dossier> dossierId) {
        return db.selectFrom(QEinladung.einladung)
            .innerJoin(QEinladung.einladung.dossier, QDossier.dossier)
            .where(QDossier.dossier.id.eq(dossierId.getUUID()))
            .fetch();
    }

    @NonNull
    public List<ID<Einladung>> findAllForSpssReport(@NonNull List<AnlassTyp> anlassTypList) {
        List<UUID> einladungIdList = db
            .select(QEinladung.einladung.id)
            .from(QEinladung.einladung)
            .innerJoin(QEinladung.einladung.anlass, QAnlass.anlass)
            .where(QAnlass.anlass.anlassConfig.anlassTyp.in(anlassTypList)
                .and(QAnlass.anlass.anlassStatus.eq(AnlassStatus.ABGESCHLOSSEN))
                .and(QEinladung.einladung.spssExported.isFalse())
                .and(QEinladung.einladung.einladungStatus.eq(EinladungStatus.TEILNAHME_BESTAETIGT))
            ).fetch();

        return einladungIdList.stream().map(uuid -> ID.of(uuid, Einladung.class)).toList();
    }

    public void markAsSpssExported(@NonNull List<ID<Einladung>> einladungIdList) {
        db.update(QEinladung.einladung)
            .set(QEinladung.einladung.spssExported, true)
            .where(QEinladung.einladung.id.in(einladungIdList.stream().map(ID::getUUID).toList()))
            .execute();
    }
}
