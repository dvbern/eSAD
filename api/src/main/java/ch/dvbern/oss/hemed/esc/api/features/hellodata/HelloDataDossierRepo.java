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
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataDossierJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.QDossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.QKind;
import ch.dvbern.oss.hemed.esc.api.features.schule.QSchule;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.QKlasse;
import com.querydsl.core.types.Projections;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.hibernate.jpa.HibernateHints;

/**
 * Read-only export repo: it only projects rows into {@link HelloDataDossierJson} and streams
 * them, so it extends {@link ReadOnlyRepo} (just the {@link ch.dvbern.dvbstarter.infrastructure.db.Db}
 * handle) rather than {@link BaseRepo}. It needs none of {@link BaseRepo}'s entity-CRUD - the
 * same read-only shape the other HelloData export repos have.
 */
@Repository
@RequiredArgsConstructor
public class HelloDataDossierRepo extends ReadOnlyRepo {

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
     * {@code Schule} and {@code Klasse} are optional on the Kind and are therefore joined with
     * an explicit {@code leftJoin}: a Kind that only has a Schule (but no Klasse), or neither,
     * must still keep its Dossier in the export.
     */
    @NonNull
    public Stream<HelloDataDossierJson> streamAll() {
        QDossier dossier = QDossier.dossier;
        QKind kind = QKind.kind;
        QSchule schule = QSchule.schule;
        QKlasse klasse = QKlasse.klasse;

        return db.select(Projections.constructor(
                HelloDataDossierJson.class,
                dossier.id,
                kind.id,
                kind.geschlecht,
                kind.geburtsdatum.year(),
                kind.nationalitaet,
                kind.erstsprache,
                kind.wohntyp,
                schule.id,
                klasse.id,
                kind.kindAktivStatus,
                dossier.kinderschutzfall,
                dossier.keineAuskunftAnDrittpersonen,
                dossier.sansPapier,
                dossier.immunitaetMasern,
                dossier.immunitaetVarizellen,
                dossier.papierakteStatus,
                kind.hashValue))
            .from(dossier)
            .join(dossier.kind, kind)
            // leftJoin: a Kind without a Schule or without a Klasse must not drop out of the
            // export - it simply carries null in that column.
            .leftJoin(kind.schule, schule)
            .leftJoin(kind.klasse, klasse)
            // Stable ordering: makes the export reproducible and allows adding keyset
            // pagination (?after=<id>) later without a contract change.
            .orderBy(dossier.id.asc())
            .setHint(HibernateHints.HINT_FETCH_SIZE, FETCH_SIZE)
            .stream();
    }
}
