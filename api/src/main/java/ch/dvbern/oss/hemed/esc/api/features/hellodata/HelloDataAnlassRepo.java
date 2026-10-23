/*
 *
 * Copyright (C) 2026 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.hemed.esc.api.features.hellodata;

import java.util.stream.Stream;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.db.baserepo.ReadOnlyRepo;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.oss.hemed.esc.api.features.anlass.QAnlass;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataAnlassJson;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.QBenutzer;
import com.querydsl.core.types.Projections;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.hibernate.jpa.HibernateHints;

/**
 * Read-only export repo: it streams {@link HelloDataAnlassJson} projections directly from the
 * database, so it extends {@link ReadOnlyRepo} (just the
 * {@link ch.dvbern.dvbstarter.infrastructure.db.Db} handle) rather than {@link BaseRepo}. It
 * needs none of {@link BaseRepo}'s entity-CRUD - the same read-only shape the other HelloData
 * export repos have.
 */
@Repository
@RequiredArgsConstructor
public class HelloDataAnlassRepo extends ReadOnlyRepo {

    /**
     * Number of rows the JDBC driver fetches per roundtrip. See {@link HelloDataSchuleRepo} for
     * the three conditions PostgreSQL needs before it actually uses a server-side cursor instead
     * of buffering the whole result set into the heap.
     */
    private static final int FETCH_SIZE = 500;

    /**
     * The returned stream holds an open cursor and <b>must</b> be closed by the caller
     * (try-with-resources), within the calling transaction.
     * <p>
     * {@code zeitVon} and {@code zeitBis} come from the embedded {@code DateTimeRange}, which is
     * optional on the entity: an Anlass that has not been scheduled yet carries {@code null} in
     * both, and the wire contract declares them as not required accordingly.
     */
    @NonNull
    public Stream<HelloDataAnlassJson> streamAll() {
        QAnlass anlass = QAnlass.anlass;
        QBenutzer arzt = new QBenutzer("arzt");
        QBenutzer mpa = new QBenutzer("mpa");

        return db.select(Projections.constructor(
                HelloDataAnlassJson.class,
                anlass.id,
                anlass.titel,
                anlass.schuljahr,
                anlass.datum,
                anlass.anlassStatus,
                anlass.anlassArt,
                arzt.id,
                mpa.id))
            .from(anlass)
            // leftJoin (not the implicit anlass.arzt.id path): both associations are optional,
            // so an Anlass without Arzt or MPA must not silently drop out of the export.
            .leftJoin(anlass.arzt, arzt)
            .leftJoin(anlass.mpa, mpa)
            // Stable ordering: makes the export reproducible and allows adding keyset
            // pagination (?after=<id>) later without a contract change.
            .orderBy(anlass.id.asc())
            .setHint(HibernateHints.HINT_FETCH_SIZE, FETCH_SIZE)
            .stream();
    }
}
