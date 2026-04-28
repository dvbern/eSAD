package ch.dvbern.dvbstarter.infrastructure.db.baserepo;

import ch.dvbern.dvbstarter.infrastructure.db.Db;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.EntityWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import jakarta.inject.Inject;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Optional;

public abstract class BaseRepo<T extends AbstractEntity<T>> {

    @Inject
    protected Db db;

    @NonNull
    public T getById(@NonNull ID<T> id) {
        return db.get(id);
    }

    @NonNull
    public Optional<T> findById(@NonNull ID<T> id) {
        return db.find(id);
    }

    @NonNull
    public T referenceById(@NonNull ID<T> id) {
        return db.getReference(id);
    }

    public void persist(@NonNull T entity) {
        db.persist(entity);
    }

    public void persistAndFlush(@NonNull T entity) {
        db.persist(entity);
        db.flush();
    }


    public void flush() {
        db.flush();
    }

    @NonNull
    public final <E extends AbstractEntity<T> & EntityWithOptimisticLocking> E persistWithOptimisticLocking(
        @NonNull E entity,
        @NonNull Long versionFromFrontend
    ) {
        // Setze die Version vom Frontend. Dies wird im AbstractEntityListener dann
        // mit der Version des Entity verglichen
        entity.setVersionFromFrontend(versionFromFrontend);

        db.persist(entity);
        db.flush(); // Damit wird die Version aktualisiert
        return entity;
    }

    @NonNull
    public final <E extends AbstractEntity<E> & EntityWithOptimisticLocking> E cascadeOptimisticLocking(
        @NonNull E entity,
        @NonNull Long versionFromFrontend
    ) {
        // Setze die Version vom Frontend. Dies wird im AbstractEntityListener dann
        // mit der Version des Entity verglichen
        entity.setVersionFromFrontend(versionFromFrontend);
        return entity;
    }

    public void remove(@NonNull ID<T> id) {
        db.remove(id);
    }
}
