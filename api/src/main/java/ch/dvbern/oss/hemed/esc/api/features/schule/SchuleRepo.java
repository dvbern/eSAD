package ch.dvbern.oss.hemed.esc.api.features.schule;

import java.util.List;
import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Repository
@RequiredArgsConstructor
public class SchuleRepo extends BaseRepo<Schule> {

    @NonNull
    public List<Schule> findAll() {
        return db.selectFrom(QSchule.schule)
            .orderBy(QSchule.schule.schulhaus.asc())
            .fetch();
    }

    @NonNull
    public List<Schule> findAllAktiv() {
        return db.selectFrom(QSchule.schule)
            .where(QSchule.schule.schuleAktivStatus.eq(EntityAktivStatus.AKTIV))
            .orderBy(QSchule.schule.schulhaus.asc())
            .fetch();
    }

    @NonNull
    public Optional<Schule> findByExternalId(@IgnoreForIdClassTest @NonNull String externalId) {
        return db.select(QSchule.schule)
            .from(QSchule.schule)
            .where(QSchule.schule.externalImportInfo.idInExternalSystem.eq(externalId))
            .fetchOne();
    }

    @NonNull
    public Schule getByExternalId(@IgnoreForIdClassTest @NonNull String externalId) {
        return findByExternalId(externalId)
            .orElseThrow(() -> new AppFailureException(AppFailureMessage.entityNotFoundUsingExternalId(
                Schule.class,
                externalId)));
    }
}
