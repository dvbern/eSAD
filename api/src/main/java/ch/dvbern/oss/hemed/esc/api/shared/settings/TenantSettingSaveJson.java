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

package ch.dvbern.oss.hemed.esc.api.shared.settings;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.i18n.translations.AppLanguages;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.types.EinladungStatus;
import ch.dvbern.oss.hemed.esc.api.shared.settings.features.TenantSettingFeaturesJson;
import ch.dvbern.oss.hemed.esc.api.shared.types.AgbResponsability;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class TenantSettingSaveJson {

    @NotNull
    @NonNull
    @Schema(required = true)
    private final AppLanguages defaultLanguage;

    @NotNull
    @NonNull
    @Schema(required = true)
    private final List<AppLanguages> supportedLanguages;

    @NotNull
    @Valid
    @Schema(required = true)
    private int folgeTermineMaxSlots;

    @NotNull
    @Valid
    @Schema(required = true)
    private final String customerAdminEmails;

    @NotNull
    @NonNull
    @Schema(required = true)
    private final String supportEmail;

    @NotNull
    @NonNull
    @Schema(required = true)
    private final AgbResponsability agbResponsability;

    @NotNull
    @NonNull
    @Valid
    @Schema(required = true)
    private final TenantSettingFeaturesJson features;

    @NotNull
    @NonNull
    @Schema(required = true)
    private List<EinladungStatus> einladungStatusValues;
}
