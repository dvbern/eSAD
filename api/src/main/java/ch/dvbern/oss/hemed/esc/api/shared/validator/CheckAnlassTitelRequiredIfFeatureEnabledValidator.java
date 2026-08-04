/*
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

package ch.dvbern.oss.hemed.esc.api.shared.validator;

import java.util.Objects;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSettingsService;
import jakarta.enterprise.context.Dependent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Dependent
@RequiredArgsConstructor
public class CheckAnlassTitelRequiredIfFeatureEnabledValidator
    extends AbstractValidator<CheckAnlassTitelRequiredIfFeatureEnabled, Anlass> {

    private final TenantSettingsService tenantSettingsService;

    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW) // Notwendig, weil sonst das flush das OptimisticLocking kaputt macht!
    public boolean isEntityValid(@NonNull Anlass anlass) {
        if (!tenantSettingsService.isAnlassTitleRequired()) {
            return true;
        }
        String titel = anlass.getTitel();
        return Objects.nonNull(titel) && !titel.isBlank();
    }
}
