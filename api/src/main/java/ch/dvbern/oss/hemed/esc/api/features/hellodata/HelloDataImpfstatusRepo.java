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

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.ReadOnlyRepo;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.QUntersuchungImpfstatusKrankheit;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.QUntersuchungImpfungen;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.QUntersuchung;
import ch.dvbern.oss.hemed.esc.api.features.hellodata.json.HelloDataImpfstatusKrankheitJson;
import com.querydsl.core.types.Projections;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.hibernate.jpa.HibernateHints;

@Repository
@RequiredArgsConstructor
public class HelloDataImpfstatusRepo extends ReadOnlyRepo {

    private static final int FETCH_SIZE = 500;

    @NonNull
    public Stream<HelloDataImpfstatusKrankheitJson> streamAll() {
        QUntersuchungImpfstatusKrankheit status = QUntersuchungImpfstatusKrankheit.untersuchungImpfstatusKrankheit;
        QUntersuchungImpfungen impfungen = QUntersuchungImpfungen.untersuchungImpfungen;
        QUntersuchung untersuchung = QUntersuchung.untersuchung;

        return db.from(status)
            .innerJoin(status.untersuchungImpfungen, impfungen)
            .innerJoin(impfungen.untersuchung, untersuchung)
            .orderBy(status.id.asc())
            .setHint(HibernateHints.HINT_FETCH_SIZE, FETCH_SIZE)
            .select(Projections.constructor(
                HelloDataImpfstatusKrankheitJson.class,
                status.id,
                untersuchung.id,
                impfungen.id,
                status.krankheit,
                status.anzahlImpfungen,
                status.datumLetzteImpfung,
                status.impfschutzGegeben,
                status.impfempfehlung,
                status.impfentscheid
            ))
            .stream();
    }
}
