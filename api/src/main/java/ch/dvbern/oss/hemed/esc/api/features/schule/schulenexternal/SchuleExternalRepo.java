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

package ch.dvbern.oss.hemed.esc.api.features.schule.schulenexternal;

import java.util.List;
import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.db.baserepo.BaseRepo;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Repository;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.shared.externalsystem.ExternalSystemIdDTO;


import ch.dvbern.oss.hemed.esc.api.features.schule.QSchule;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.shared.externalsystem.QExternalSystemIdDTO;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Repository
@RequiredArgsConstructor
public class SchuleExternalRepo extends BaseRepo<SchuleExternal> {


    @NonNull
    public List<ExternalSystemIdDTO> findAllExternalIds() {
        return this.db.select(
            new QExternalSystemIdDTO(QSchuleExternal.schuleExternal.schulhausId, QSchule.schule.id))
            .from(QSchuleExternal.schuleExternal)
            .innerJoin(QSchule.schule).on(QSchuleExternal.schuleExternal.schule.eq(QSchule.schule))
            .fetch();
    }

    @NonNull
    public Optional<SchuleExternal> findExternalSchuleForSchule(@NonNull ID<Schule> id) {
        return this.db.select(QSchuleExternal.schuleExternal)
            .from(QSchuleExternal.schuleExternal)
            .where(QSchuleExternal.schuleExternal.schule.id.eq(id.getUUID()))
            .fetchOne();
    }

    @NonNull
    public SchuleExternal getExternalSchuleForSchule(@NonNull ID<Schule> id) {
        return this.findExternalSchuleForSchule(id)
            .orElseThrow(() -> new AppFailureException(AppFailureMessage.entityNotFound(id)));
    }
}
