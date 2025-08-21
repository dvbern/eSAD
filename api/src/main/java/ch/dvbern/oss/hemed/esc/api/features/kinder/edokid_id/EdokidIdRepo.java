package ch.dvbern.oss.hemed.esc.api.features.kinder.edokid_id;

import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Repository
@RequiredArgsConstructor
public class EdokidIdRepo extends BaseRepo<EdokidIdSequence> {

    @Transactional(TxType.REQUIRED)
    public EdokidIdSequence incrementLaufNummer(int eintrittsJahr) {
        return findSequenceByEintrittsjahrAndLock(eintrittsJahr)
            .map(existingSequence -> {
                existingSequence.increment();
                db.persist(existingSequence);
                return existingSequence;
            }).orElseGet(() -> createNewEdokidIdSequence(eintrittsJahr));
    }

    @NonNull
    public Optional<EdokidIdSequence> findSequenceByEintrittsjahrAndLock(int eintrittsJahr) {
        return db.select(QEdokidIdSequence.edokidIdSequence)
            .from(QEdokidIdSequence.edokidIdSequence)
            .where(QEdokidIdSequence.edokidIdSequence.eintrittsJahr.eq(eintrittsJahr))
            .setLockMode(LockModeType.PESSIMISTIC_WRITE)
            .fetchOne();

    }

    public EdokidIdSequence createNewEdokidIdSequence(int eintrittsJahr) {
        db.persist(new EdokidIdSequence(eintrittsJahr, 'A', 1));
        return findSequenceByEintrittsjahrAndLock(eintrittsJahr)
            .orElseThrow(() -> new AppFailureException(
                AppFailureMessage.couldNotCreateEntity(EdokidIdSequence.class.getSimpleName())
            ));
    }

}
