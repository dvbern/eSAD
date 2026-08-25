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
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataSchuleJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.QSchule;
import ch.dvbern.oss.hemed.esc.api.features.schule.quartier.QQuartier;
import com.querydsl.core.types.Projections;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.hibernate.jpa.HibernateHints;

/**
 * Read-only export repo: it only projects rows into {@link HelloDataSchuleJson} and streams them,
 * so it extends {@link ReadOnlyRepo} (just the {@link ch.dvbern.dvbstarter.infrastructure.db.Db}
 * handle) rather than {@link BaseRepo}. It needs none of {@link BaseRepo}'s entity-CRUD, and is
 * not tied to a single entity type - the HelloData export will grow further datasets (Kind,
 * Anlass, ...) with the same read-only shape.
 */
@Repository
@RequiredArgsConstructor
public class HelloDataSchuleRepo extends ReadOnlyRepo {

    /**
     * Number of rows the JDBC driver fetches per roundtrip.
     * <p>
     * For PostgreSQL to actually use a server-side cursor (instead of pulling the whole result
     * set into the heap) three things must come together:
     * <ol>
     *     <li>{@code autoCommit = false}, i.e. the query runs inside a transaction</li>
     *     <li>{@code fetchSize > 0}</li>
     *     <li>a forward-only result set (the default)</li>
     * </ol>
     * The first one is the tricky one: the transaction must be open <b>during</b> streaming,
     * not just while issuing the query. See {@link HelloDataExportService}, which asserts it.
     * <p>
     * <b>Verified, not assumed:</b> a cursor really is used on this path. Measured by scaling
     * the table and timing the first row - with the ordering removed to take PostgreSQL's sort
     * out of the picture, time-to-first-row stayed flat at 0-2 ms while the table grew from
     * 25k to 400k rows, and the numbers matched Hibernate's explicit
     * {@code scroll(FORWARD_ONLY)} exactly. Buffering would have grown linearly.
     */
    private static final int FETCH_SIZE = 500;

    /**
     * The returned stream holds an open cursor and <b>must</b> be closed by the caller
     * (try-with-resources), within the calling transaction.
     */
    @NonNull
    public Stream<HelloDataSchuleJson> streamAll() {
        QSchule schule = QSchule.schule;
        QQuartier quartier = QQuartier.quartier;

        return db.select(Projections.constructor(
                HelloDataSchuleJson.class,
                schule.id,
                schule.schulhaus,
                schule.schulstandort,
                schule.schulkreis,
                schule.schulhausnummer,
                quartier.id,
                schule.adresse.plz,
                schule.adresse.ort,
                schule.schuleAktivStatus,
                schule.timestampMutiert))
            .from(schule)
            // leftJoin (not the implicit schule.quartier.id path): otherwise Schulen without a
            // Quartier would silently drop out of the export.
            .leftJoin(schule.quartier, quartier)
            // Stable ordering: makes the export reproducible and allows adding keyset
            // pagination (?after=<id>) later without a contract change.
            //
            // It does cost something: PostgreSQL has to sort the whole result before it can
            // emit the first row, so time-to-first-row grows with the table (measured ~24 ms
            // at 100k rows, ~86 ms at 400k) while the cursor itself stays flat. That is a
            // server-side sort, not client-side buffering - heap on our side stays bounded.
            .orderBy(schule.id.asc())
            .setHint(HibernateHints.HINT_FETCH_SIZE, FETCH_SIZE)
            .stream(); // stream the rows
    }
}
