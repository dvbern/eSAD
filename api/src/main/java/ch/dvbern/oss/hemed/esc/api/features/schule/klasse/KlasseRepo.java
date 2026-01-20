package ch.dvbern.oss.hemed.esc.api.features.schule.klasse;

import java.util.List;
import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.klasseexternal.EscadaKlasseStufe;
import ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson.Lehrperson;
import ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson.QLehrperson;
import ch.dvbern.oss.hemed.esc.api.features.schule.types.Klassenstufe;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Repository
@RequiredArgsConstructor
public class KlasseRepo extends BaseRepo<Klasse> {

    @NonNull
    public List<Klasse> findAll() {
        return db.selectFrom(QKlasse.klasse)
            .orderBy(QKlasse.klasse.bezeichnung.asc())
            .fetch();
    }

    @NonNull
    public List<Klasse> findBySchule(@NonNull ID<Schule> schuleID) {
        return db.selectFrom(QKlasse.klasse)
            .where(QKlasse.klasse.schule.id.eq(schuleID.getUUID()))
            .orderBy(QKlasse.klasse.bezeichnung.asc())
            .fetch();
    }

    @NonNull
    public List<Klasse> findBySchuleAndBezeichnung(@NonNull ID<Schule> schuleID, @NonNull String bezeichnung) {
        return db.selectFrom(QKlasse.klasse)
            .where(QKlasse.klasse.schule.id.eq(schuleID.getUUID()).and(
                QKlasse.klasse.bezeichnung.eq(bezeichnung)
            ))
            .orderBy(QKlasse.klasse.timestampErstellt.asc())
            .fetch();
    }

    @NonNull
    public List<Klasse> findAktivBySchule(@NonNull ID<Schule> schuleID) {
        return db.selectFrom(QKlasse.klasse)
            .where(QKlasse.klasse.schule.id.eq(schuleID.getUUID())
                .and(QKlasse.klasse.klasseAktivStatus.eq(EntityAktivStatus.AKTIV)))
            .orderBy(QKlasse.klasse.bezeichnung.asc())
            .fetch();
    }

    @NonNull
    public List<EscadaKlasseStufe> extractEscadaKlassenStufen(@NonNull ID<Schule> schuleID) {
        List<Klassenstufe> klassenstufen = db
            .selectDistinct(QKlasse.klasse.klassenstufe)
            .from(QKlasse.klasse)
            .where(QKlasse.klasse.schule.id.eq(schuleID.getUUID()))
            .fetch();
        return klassenstufen.stream().map(Klassenstufe::getEscadaKlasseStufe).distinct().toList();
    }

    @NonNull
    public Klasse getByExternalId(@IgnoreForIdClassTest @NonNull String externalId) {
        return findByExternalId(externalId)
            .orElseThrow(() -> new AppFailureException(AppFailureMessage.entityNotFoundUsingExternalId(
                Klasse.class,
                externalId)));
    }

    @NonNull
    public Klasse getByExternalIdLockedQuery(@IgnoreForIdClassTest @NonNull String externalId) {
        return db.select(QKlasse.klasse)
            .from(QKlasse.klasse)
            .where(QKlasse.klasse.externalImportInfo.idInExternalSystem.eq(externalId))
            .setLockMode(LockModeType.PESSIMISTIC_WRITE)
            .fetchOne()
            .orElseThrow(() -> new AppFailureException(AppFailureMessage.entityNotFoundUsingExternalId(
                Klasse.class,
                externalId)));
    }

    @NonNull
    public Optional<Klasse> findByExternalId(@IgnoreForIdClassTest @NonNull String externalId) {
        return db.select(QKlasse.klasse)
            .from(QKlasse.klasse)
            .where(QKlasse.klasse.externalImportInfo.idInExternalSystem.eq(externalId))
            .fetchOne();

    }

    public List<Klasse> findByLehrperson(@NonNull ID<Lehrperson> id) {
        return db.selectDistinct(QKlasse.klasse)
            .from(QKlasse.klasse)
            .join(QKlasse.klasse.lehrpersonen, QLehrperson.lehrperson)
            .where(QLehrperson.lehrperson.id.eq(id.getUUID()))
            .fetch();
    }
}
