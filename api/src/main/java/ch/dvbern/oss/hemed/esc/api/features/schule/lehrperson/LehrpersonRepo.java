package ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.QKlasse;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@Repository
@RequiredArgsConstructor
public class LehrpersonRepo extends BaseRepo<Lehrperson> {

    @NonNull
    public List<Lehrperson> findAll() {
        return db.selectFrom(QLehrperson.lehrperson)
            .fetch();
    }

    @NonNull
    public List<Lehrperson> findAktivByKlasse(@NonNull ID<Klasse> klasseId) {
        return db
            .selectFrom(QLehrperson.lehrperson)
            .innerJoin(QLehrperson.lehrperson.klassen, QKlasse.klasse)
            .where(QKlasse.klasse.id.eq(klasseId.getUUID())
                .and(QLehrperson.lehrperson.externalImportInfo.aktivInExternalSystem.eq(true)
                    .or(QLehrperson.lehrperson.externalImportInfo.isNull()))) // check null because manually created
				// lehrpersonen don't have externalImportInfo
            .fetch();
    }

    @NonNull
    public List<Lehrperson> findAllOptionsActive(@Nullable ID<Klasse> klasseToExclude) {
        List<UUID> currentLehrpersonen = List.of();
        if (klasseToExclude != null) {
            currentLehrpersonen = db.select(QLehrperson.lehrperson.id)
                .from(QLehrperson.lehrperson)
                .innerJoin(QLehrperson.lehrperson.klassen, QKlasse.klasse)
                .where(QKlasse.klasse.id.eq(klasseToExclude.getUUID()))
                .fetch();
        }
        return db
            .selectFrom(QLehrperson.lehrperson)
            .leftJoin(QLehrperson.lehrperson.klassen, QKlasse.klasse)
            .where((QLehrperson.lehrperson.externalImportInfo.isNull()
                .or(QLehrperson.lehrperson.externalImportInfo.aktivInExternalSystem.isFalse()))
                .and(QLehrperson.lehrperson.id.notIn(currentLehrpersonen)))
            .fetch();
    }

    @NonNull
    public List<Lehrperson> findByKlasse(@NonNull ID<Klasse> klasseId) {
        return db
            .selectFrom(QLehrperson.lehrperson)
            .innerJoin(QLehrperson.lehrperson.klassen, QKlasse.klasse)
            .where(QKlasse.klasse.id.eq(klasseId.getUUID()))
            .fetch();
    }

    @NonNull
    public Lehrperson getByExternalId(@IgnoreForIdClassTest @NonNull String externalId) {
        return findByExternalId(externalId)
            .orElseThrow(() -> new AppFailureException(AppFailureMessage.entityNotFoundUsingExternalId(
                Lehrperson.class, externalId)));
    }

    public @NonNull Optional<Lehrperson> findByExternalId(@IgnoreForIdClassTest @NonNull String externalId) {
        return db.select(QLehrperson.lehrperson)
            .from(QLehrperson.lehrperson)
            .where(QLehrperson.lehrperson.externalImportInfo.idInExternalSystem.eq(externalId))
            .fetchOne();
    }
}
