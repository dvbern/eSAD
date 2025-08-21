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

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.QKind;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Repository
@RequiredArgsConstructor
public class KindAktivitaetHistoryRepo extends BaseRepo<KindAktivitaetHistory> {

    private final ConfigService configService;

    @NonNull
    public List<KindAktivitaetHistory> findAllForBenutzer(@NonNull ID<Benutzer> benutzerId) {
        return db.selectFrom(QKindAktivitaetHistory.kindAktivitaetHistory)
            .where(QKindAktivitaetHistory.kindAktivitaetHistory.userErstellt.eq(benutzerId.getId()))
            .orderBy(QKindAktivitaetHistory.kindAktivitaetHistory.timestampErstellt.desc())
            .limit(configService.getDashboardAktivitaetHistoryLimit())
            .fetch();
    }

    @NonNull
    public List<KindAktivitaetHistory> findAllForKind(@NonNull ID<Kind> kindID) {
        return db.selectFrom(QKindAktivitaetHistory.kindAktivitaetHistory)
            .leftJoin(QKindAktivitaetHistory.kindAktivitaetHistory.kind, QKind.kind)
            .where(QKind.kind.id.eq(kindID.getUUID()))
            .fetch();
    }
}
