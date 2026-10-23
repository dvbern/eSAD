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
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataNotizJson;
import ch.dvbern.oss.hemed.esc.api.features.labels.QLabel;
import ch.dvbern.oss.hemed.esc.api.features.notizen.QNotiz;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.hibernate.jpa.HibernateHints;

/**
 * Read-only export repo: it streams {@link HelloDataNotizJson} projections directly from the
 * database, so it extends {@link ReadOnlyRepo} (just the
 * {@link ch.dvbern.dvbstarter.infrastructure.db.Db} handle) rather than {@link BaseRepo}. It
 * needs none of {@link BaseRepo}'s entity-CRUD - the same read-only shape the other HelloData
 * export repos have.
 */
@Repository
@RequiredArgsConstructor
public class HelloDataNotizRepo extends ReadOnlyRepo {

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
     * {@code labels} is aggregated in the database into a comma-separated list of Label UUIDs;
     * a Notiz without Labels maps to an empty string, never {@code null}, because the wire
     * contract declares the field as required. {@code hatFileanhang} is likewise answered by
     * the database ({@code files is not empty} becomes an exists-subquery), so no FileData ever
     * has to be loaded into memory.
     */
    @NonNull
    public Stream<HelloDataNotizJson> streamAll() {
        QNotiz notiz = QNotiz.notiz;
        QLabel label = QLabel.label;

        StringExpression labels = Expressions.stringTemplate(
            "coalesce(listagg(cast({0} as string), ',') within group (order by cast({0} as string)), '')",
            label.id);

        return db.select(Projections.constructor(
                HelloDataNotizJson.class,
                notiz.id,
                notiz.dossier.id,
                notiz.verantwortlicher.id,
                notiz.status,
                // exists-subquery rather than a join: joining the files as well would multiply
                // the rows and duplicate the aggregated Labels.
                notiz.files.isNotEmpty(),
                labels))
            .from(notiz)
            // leftJoin: a Notiz without any Label must not drop out of the export.
            .leftJoin(notiz.labels, label)
            .groupBy(
                notiz.id,
                notiz.dossier.id,
                notiz.verantwortlicher.id,
                notiz.status)
            // Stable ordering: makes the export reproducible and allows adding keyset
            // pagination (?after=<id>) later without a contract change.
            .orderBy(notiz.id.asc())
            .setHint(HibernateHints.HINT_FETCH_SIZE, FETCH_SIZE)
            .stream();
    }
}
