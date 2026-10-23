/*
 * Copyright (C) 2026 DV Bern AG, Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.hemed.esc.api.features.hellodata;

import java.util.UUID;
import java.util.stream.Stream;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.db.baserepo.ReadOnlyRepo;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anonymereintrag.QAnonymerEintrag;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.QEinladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.types.ArtAbmeldung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.types.EinladungStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.types.GrundAbmeldung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.QUntersuchung;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataEinladungJson;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizTyp;
import ch.dvbern.oss.hemed.esc.api.features.notizen.QNotiz;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.hibernate.jpa.HibernateHints;

/**
 * Read-only export repo: it only projects rows into {@link HelloDataEinladungJson} and streams
 * them, so it extends {@link ReadOnlyRepo} (just the {@link ch.dvbern.dvbstarter.infrastructure.db.Db}
 * handle) rather than {@link BaseRepo}. It needs none of {@link BaseRepo}'s entity-CRUD - the
 * same read-only shape the other HelloData export repos have.
 * <p>
 * Streams both regular {@link ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung}s
 * and {@link ch.dvbern.oss.hemed.esc.api.features.anlass.anonymereintrag.AnonymerEintrag}s:
 * HelloData has no separate endpoint for anonymous entries, so they are exported through
 * this dataset with {@code dossierId = null} and their pseudonym {@code hash} set.
 */
@Repository
@RequiredArgsConstructor
public class HelloDataEinladungRepo extends ReadOnlyRepo {

    /**
     * Number of rows the JDBC driver fetches per roundtrip. See {@link HelloDataSchuleRepo} for
     * the three conditions PostgreSQL needs before it actually uses a server-side cursor instead
     * of buffering the whole result set into the heap.
     */
    private static final int FETCH_SIZE = 500;

    /**
     * The returned stream holds open cursors and <b>must</b> be closed by the caller
     * (try-with-resources), within the calling transaction.
     */
    @NonNull
    public Stream<HelloDataEinladungJson> streamAll() {
        QEinladung einladung = QEinladung.einladung;
        QUntersuchung einladungUntersuchung = new QUntersuchung("einladungUntersuchung");

        Stream<HelloDataEinladungJson> einladungenStream = db.select(
                Projections.constructor(
                    HelloDataEinladungJson.class,
                    einladung.id,
                    einladung.anlass.id,
                    einladung.dossier.id,
                    einladungUntersuchung.id,
                    einladung.einladungStatus,
                    einladung.grundAbmeldung,
                    einladung.artAbmeldung,
                    hasNotizWithFile(
                        einladung,
                        NotizTyp.IMPFAUSWEIS,
                        "impfausweisNotiz"
                    ),
                    hasNotizWithFile(
                        einladung,
                        NotizTyp.FRAGEBOGEN,
                        "fragebogenNotiz"
                    ),
                    Expressions.nullExpression(String.class)
                )
            )
            .from(einladung)
            // leftJoin (not the implicit einladung.untersuchung.id path): otherwise Einladungen
            // without an Untersuchung would silently drop out of the export.
            .leftJoin(einladung.untersuchung, einladungUntersuchung)
            // Stable ordering: makes the export reproducible and allows adding keyset
            // pagination (?after=<id>) later without a contract change.
            .orderBy(einladung.id.asc())
            .setHint(HibernateHints.HINT_FETCH_SIZE, FETCH_SIZE)
            .stream();

        QAnonymerEintrag anonymerEintrag = QAnonymerEintrag.anonymerEintrag;
        QUntersuchung anonymerUntersuchung = new QUntersuchung("anonymerUntersuchung");

        Stream<HelloDataEinladungJson> anonymeEintraegeStream = db.select(
                Projections.constructor(
                    HelloDataEinladungJson.class,
                    anonymerEintrag.id,
                    anonymerEintrag.anlass.id,
                    Expressions.nullExpression(UUID.class),
                    anonymerUntersuchung.id,
                    Expressions.nullExpression(EinladungStatus.class),
                    Expressions.nullExpression(GrundAbmeldung.class),
                    Expressions.nullExpression(ArtAbmeldung.class),
                    Expressions.asBoolean(false),
                    Expressions.asBoolean(false),
                    anonymerEintrag.hash
                )
            )
            .from(anonymerEintrag)
            .leftJoin(anonymerEintrag.untersuchung, anonymerUntersuchung)
            .orderBy(anonymerEintrag.id.asc())
            .setHint(HibernateHints.HINT_FETCH_SIZE, FETCH_SIZE)
            .stream();

        return Stream.concat(einladungenStream, anonymeEintraegeStream);
    }

    /**
     * "Wurde ein Impfausweis / Fragebogen hochgeladen": a Notiz of that typ linked to the
     * Einladung <b>and</b> carrying at least one file. A Notiz without a file is not an upload.
     * <p>
     * An {@code exists} subquery rather than a join: joining the Notizen would multiply the rows
     * of the export, which a streaming full load must not do.
     *
     * @param alias each subquery needs its own alias, otherwise the two flags collide in the
     * generated query
     */
    @NonNull
    private static BooleanExpression hasNotizWithFile(
        @NonNull QEinladung einladung,
        @NonNull NotizTyp notizTyp,
        @NonNull String alias
    ) {
        QNotiz notiz = new QNotiz(alias);

        return JPAExpressions.selectOne()
            .from(notiz)
            .where(
                notiz.in(einladung.notizen),
                notiz.typ.eq(notizTyp),
                notiz.files.isNotEmpty())
            .exists();
    }
}
