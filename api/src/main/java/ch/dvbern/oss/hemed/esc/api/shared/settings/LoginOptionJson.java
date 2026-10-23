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

package ch.dvbern.oss.hemed.esc.api.shared.settings;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.shared.types.LoginMethod;
import jakarta.validation.constraints.NotNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * A single login method as configured/derivable for the current tenant, read-only and derived by
 * {@link LoginMethodIdpHintResolver}. {@code idpHint} is the resolved hint string (never the enum name)
 * so that it stays correct across environments, and may be the empty string for {@link LoginMethod#PASSWORD}.
 */
@JaxDTO
public record LoginOptionJson(

    @NotNull
    @NonNull
    @Schema(required = true)
    LoginMethod method,

    @NotNull
    @NonNull
    @Schema(required = true)
    String idpHint,

    @Schema(required = true)
    int order
) {
}
