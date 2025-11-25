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

package ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig;

import java.time.LocalDate;
import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.QAnlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Repository
@RequiredArgsConstructor
public class AnlassConfigRepo extends BaseRepo<AnlassConfig> {

    @NonNull
    public List<AnlassConfig> findAll() {
        return db.selectFrom(QAnlassConfig.anlassConfig)
            .orderBy(QAnlassConfig.anlassConfig.anlassTyp.asc())
            .orderBy(QAnlassConfig.anlassConfig.gueltigkeitsZeitraum.datumVon.desc())
            .fetch();
    }

    @NonNull
    public AnlassConfig getByAnlassTypAndStichtag(@NonNull AnlassTyp anlassTyp, @NonNull LocalDate stichtag) {
        return db.selectFrom(QAnlassConfig.anlassConfig)
            .where(QAnlassConfig.anlassConfig.anlassTyp.eq(anlassTyp)
                .and(QAnlassConfig.anlassConfig.gueltigkeitsZeitraum.datumVon.loe(stichtag))
                .and(QAnlassConfig.anlassConfig.gueltigkeitsZeitraum.datumBis.goe(stichtag))
            )
            .fetchOne()
            .orElseThrow(() -> new AppFailureException(AppFailureMessage.anlassConfigNotFound(anlassTyp, stichtag)));
    }

    // Only use for test purposes
    public void removeAllAnlassConfigsWithTyp(@NonNull AnlassTyp anlassTyp) {
        db.delete(QAnlassConfig.anlassConfig).where(QAnlassConfig.anlassConfig.anlassTyp.eq(anlassTyp)).execute();
    }

    public boolean isUsedInAtLeastOneAnlass(@NonNull ID<AnlassConfig> anlassConfigId) {
        return !db.select(QAnlass.anlass.id)
            .from(QAnlass.anlass)
            .leftJoin(QAnlass.anlass.anlassConfig)
            .where(QAnlassConfig.anlassConfig.id.eq(anlassConfigId.getUUID()))
            .fetch().isEmpty();
    }
}
