package ch.dvbern.dvbstarter.infrastructure.db.baserepo;

import ch.dvbern.dvbstarter.infrastructure.db.Db;
import jakarta.inject.Inject;

/**
 * Thin base for read-only repositories: it exposes only the injected {@link Db} query handle and
 * nothing else.
 * <p>
 * Unlike {@link BaseRepo} it carries no entity-CRUD API ({@code getById}, {@code persist},
 * {@code remove}, ...) and is not typed to a single entity. That fits repositories which only run
 * projection/streaming queries and never persist, load or remove entities - for example the
 * HelloData export repos, which return DTO projections and deliberately avoid entity semantics
 * (no audit, no lifecycle). Extending {@link BaseRepo} there would only pull in unused CRUD and a
 * misleading entity type parameter.
 */
public abstract class ReadOnlyRepo {

    @Inject
    protected Db db;
}
