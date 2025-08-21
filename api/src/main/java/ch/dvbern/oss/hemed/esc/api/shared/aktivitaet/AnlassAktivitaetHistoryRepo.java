/*
 *
 * Copyright (C) 2025 DV Bern AG, Switzerland
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

package ch.dvbern.oss.hemed.esc.api.shared.aktivitaet;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.QAnlass;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Repository
@RequiredArgsConstructor
public class AnlassAktivitaetHistoryRepo extends BaseRepo<AnlassAktivitaetHistory> {


    private final ConfigService configService;

    @NonNull
    public List<AnlassAktivitaetHistory> findAllForBenutzer(
        @NonNull ID<Benutzer> benutzerId) {
        return db.selectFrom(QAnlassAktivitaetHistory.anlassAktivitaetHistory)
            .where(QAnlassAktivitaetHistory.anlassAktivitaetHistory.userErstellt.eq(benutzerId.getId()))
            .orderBy(QAnlassAktivitaetHistory.anlassAktivitaetHistory.timestampErstellt.desc())
            .limit(configService.getDashboardAktivitaetHistoryLimit())
            .fetch();
    }

    @NonNull
    public List<AnlassAktivitaetHistory> findAllForAnlass(@NonNull ID<Anlass> anlassID) {
        return db.selectFrom(QAnlassAktivitaetHistory.anlassAktivitaetHistory)
            .leftJoin(QAnlassAktivitaetHistory.anlassAktivitaetHistory.anlass, QAnlass.anlass)
            .where(QAnlass.anlass.id.eq(anlassID.getUUID()))
            .fetch();

    }
}
