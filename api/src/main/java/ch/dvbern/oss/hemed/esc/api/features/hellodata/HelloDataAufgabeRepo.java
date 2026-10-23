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
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataAufgabeJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.QKind;
import ch.dvbern.oss.hemed.esc.api.features.labels.QLabel;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.QAufgabe;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.hibernate.jpa.HibernateHints;

/**
 * Read-only export repo: it streams {@link HelloDataAufgabeJson} projections directly from the
 * database, so it extends {@link ReadOnlyRepo} (just the
 * {@link ch.dvbern.dvbstarter.infrastructure.db.Db} handle) rather than {@link BaseRepo}. It
 * needs none of {@link BaseRepo}'s entity-CRUD - the same read-only shape the other HelloData
 * export repos have.
 */
@Repository
@RequiredArgsConstructor
public class HelloDataAufgabeRepo extends ReadOnlyRepo {

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
     * an Aufgabe without Labels maps to an empty string, never {@code null}, because the wire
     * contract declares the field as required.
     * <p>
     * {@code kind} and {@code anlass} are optional on the entity and are therefore joined with
     * an explicit {@code leftJoin}: an Aufgabe without a Kind or without an Anlass must stay in
     * the export and simply carries {@code null} in that column.
     */
    @NonNull
    public Stream<HelloDataAufgabeJson> streamAll() {
        QAufgabe aufgabe = QAufgabe.aufgabe;
        QLabel label = QLabel.label;
        QKind kind = QKind.kind;
        QAnlass anlass = QAnlass.anlass;

        StringExpression labels = Expressions.stringTemplate(
            "coalesce(listagg(cast({0} as string), ',') within group (order by cast({0} as string)), '')",
            label.id);

        return db.select(Projections.constructor(
                HelloDataAufgabeJson.class,
                aufgabe.id,
                aufgabe.aufgabeStatus,
                aufgabe.faelligBis,
                aufgabe.userErfasst.id,
                aufgabe.empfaenger.id,
                kind.id,
                anlass.id,
                labels))
            .from(aufgabe)
            // leftJoin: an Aufgabe without any Label must not drop out of the export.
            .leftJoin(aufgabe.labels, label)
            .leftJoin(aufgabe.kind, kind)
            .leftJoin(aufgabe.anlass, anlass)
            .groupBy(
                aufgabe.id,
                aufgabe.aufgabeStatus,
                aufgabe.faelligBis,
                aufgabe.userErfasst.id,
                aufgabe.empfaenger.id,
                kind.id,
                anlass.id)
            // Stable ordering: makes the export reproducible and allows adding keyset
            // pagination (?after=<id>) later without a contract change.
            .orderBy(aufgabe.id.asc())
            .setHint(HibernateHints.HINT_FETCH_SIZE, FETCH_SIZE)
            .stream();
    }
}
