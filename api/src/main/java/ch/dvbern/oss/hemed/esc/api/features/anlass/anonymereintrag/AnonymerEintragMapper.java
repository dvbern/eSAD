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

package ch.dvbern.oss.hemed.esc.api.features.anlass.anonymereintrag;

import java.util.function.Consumer;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassMapper;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.shared.util.HashValueGeneratorUtil;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@JaxMapper
@RequiredArgsConstructor
public final class AnonymerEintragMapper {

    private final AnonymerEintragService anonymerEintragService;
    private final AnlassMapper anlassMapper;
    private final TenantProducer tenantProducer;

    @NonNull
    public AnonymerEintragJson getById(@NonNull ID<AnonymerEintrag> anonymerEintragID) {
        return toJson(anonymerEintragService.getById(anonymerEintragID));
    }

    @NonNull
    public AnonymerEintragJson createAnonymerEintrag(@NonNull AnonymerEintragCreateJson createJson) {
        AnonymerEintrag anonymerEintrag = toEntityForCreate(createJson);
        return toJson(anonymerEintragService.persist(anonymerEintrag));
    }

    @NonNull
    private AnonymerEintrag toEntityForCreate(AnonymerEintragCreateJson createJson) {
        String hashValue = HashValueGeneratorUtil.generateHashValueForKindValues(
            createJson.getKindName(),
            createJson.getKindVorname(),
            createJson.getKindGeschlecht(),
            createJson.getKindGeburtsdatum());
        Anlass anlass = anlassMapper.createForAnonymerEintrag(createJson);

        return new AnonymerEintrag(
            hashValue,
            anlass,
            null
        );
    }

    @NonNull
    private AnonymerEintragJson toJson(AnonymerEintrag anonymerEintrag) {
        return new AnonymerEintragJson(
            anonymerEintrag.getId(),
            anonymerEintrag.getHash(),
            AnlassMapper.toJson(anonymerEintrag.getAnlass(), tenantProducer.produceTenant())
        );
    }

    public void anonymerEintragAbschliessen(
        @NonNull ID<AnonymerEintrag> anonymerEintragID,
        @NonNull Consumer<ID<Anlass>> hasAuthorizationAnlass
    ) {
        AnonymerEintrag anonymerEintrag = anonymerEintragService.getById(anonymerEintragID);
        hasAuthorizationAnlass.accept(anonymerEintrag.getAnlass().getId());
        anonymerEintragService.anonymerEintragAbschliessen(anonymerEintragID);
    }
}
