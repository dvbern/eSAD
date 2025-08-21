package ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking;

import ch.dvbern.dvbstarter.infrastructure.db.Db;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
public class OptimisticLockingService {

    private final Db db;

    public final <T extends AbstractEntity<T> & EntityWithOptimisticLocking> T persistWithOptimisticLocking(
        @NonNull T entity,
        @NonNull SaveJsonWithOptimisticLocking saveJson
    ) {
        // Setze die Version vom Frontend. Dies wird im AbstractEntityListener dann
        // mit der Version des Entity verglichen
        entity.setVersionFromFrontend(saveJson.getVersion());

        db.persist(entity);
        db.flush(); // Damit wird die Version aktualisiert
        return entity;
    }
}
