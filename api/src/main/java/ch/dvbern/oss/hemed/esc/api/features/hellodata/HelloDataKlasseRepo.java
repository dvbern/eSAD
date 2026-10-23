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
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataKlasseJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.QKlasse;
import com.querydsl.core.types.Projections;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.hibernate.jpa.HibernateHints;

/**
 * Read-only export repo: it only projects rows into {@link HelloDataKlasseJson} and streams
 * them, so it extends {@link ReadOnlyRepo} (just the {@link ch.dvbern.dvbstarter.infrastructure.db.Db}
 * handle) rather than {@link BaseRepo}. It needs none of {@link BaseRepo}'s entity-CRUD - the
 * same read-only shape the other HelloData export repos have.
 */
@Repository
@RequiredArgsConstructor
public class HelloDataKlasseRepo extends ReadOnlyRepo {

    /**
     * Number of rows the JDBC driver fetches per roundtrip. See {@link HelloDataSchuleRepo} for
     * the three conditions PostgreSQL needs before it actually uses a server-side cursor instead
     * of buffering the whole result set into the heap.
     */
    private static final int FETCH_SIZE = 500;

    /**
     * The returned stream holds an open cursor and <b>must</b> be closed by the caller
     * (try-with-resources), within the calling transaction.
     */
    @NonNull
    public Stream<HelloDataKlasseJson> streamAll() {
        QKlasse klasse = QKlasse.klasse;

        return db.select(Projections.constructor(
                HelloDataKlasseJson.class,
                klasse.id,
                klasse.schule.id,
                klasse.bezeichnung,
                klasse.klassenstufe,
                klasse.klasseSchulart,
                klasse.klasseAktivStatus))
            .from(klasse)
            // Stable ordering: makes the export reproducible and allows adding keyset
            // pagination (?after=<id>) later without a contract change.
            .orderBy(klasse.id.asc())
            .setHint(HibernateHints.HINT_FETCH_SIZE, FETCH_SIZE)
            .stream();
    }
}
