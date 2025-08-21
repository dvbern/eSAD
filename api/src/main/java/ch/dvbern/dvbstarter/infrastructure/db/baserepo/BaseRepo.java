package ch.dvbern.dvbstarter.infrastructure.db.baserepo;

import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.db.Db;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.shared.abstractentity.AbstractEntity;
import jakarta.inject.Inject;
import org.checkerframework.checker.nullness.qual.NonNull;

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

    public void remove(@NonNull ID<T> id) {
        db.remove(id);
    }
}
