package ch.dvbern.oss.hemed.esc.api.features.anlass.anonymereintrag;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@Slf4j
@Repository
@RequiredArgsConstructor
public class AnonymerEintragRepo extends BaseRepo<AnonymerEintrag> {

    @Override
    @NonNull
    public AnonymerEintrag getById(@NonNull ID<AnonymerEintrag> id) {
        return db.select(QAnonymerEintrag.anonymerEintrag)
            .from(QAnonymerEintrag.anonymerEintrag)
            .where(QAnonymerEintrag.anonymerEintrag.id.eq(id.getUUID()))
            .fetchOne()
            .orElseThrow(() -> new AppFailureException(AppFailureMessage.entityNotFound(id)));
    }

    @NonNull
    public AnonymerEintrag getByIdLockedQuery(@NonNull ID<AnonymerEintrag> anonymerEintragID) {
        // we do this with a query instead of through db.findLocked() because findLocked does not work
        // it generates a warning  'HHH000444: Encountered request for locking however dialect reports that
        // database prefers locking be done in a separate select (follow-on locking); results will be locked after
        // initial query executes'
        // and it does not work, the generated query ends with select ... for update, when using the lockMode in an
        // actual query
        // the lock mode in sql ends up as select ... for no key update which seems to work

        return db.select(QAnonymerEintrag.anonymerEintrag)
            .from(QAnonymerEintrag.anonymerEintrag)
            .where(QAnonymerEintrag.anonymerEintrag.id.eq(anonymerEintragID.getUUID()))
            .setLockMode(LockModeType.PESSIMISTIC_WRITE)
            .fetchOne()
            .orElseThrow(() -> new AppFailureException(AppFailureMessage.entityNotFound(anonymerEintragID)));
    }
}
