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

package ch.dvbern.oss.hemed.esc.api.features.arzt.json;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumber;
import ch.dvbern.oss.hemed.esc.api.features.arzt.types.Fachrichtung;
import ch.dvbern.oss.hemed.esc.api.features.arzt.types.HpvTeilnahme;
import ch.dvbern.oss.hemed.esc.api.features.arzt.types.Rechtsform;
import ch.dvbern.oss.hemed.esc.api.infrastructure.optimisticlocking.SaveJsonWithOptimisticLocking;
import ch.dvbern.oss.hemed.esc.api.shared.person.AdresseJson;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Value
@AllArgsConstructor
@JaxDTO
public class ArztSaveJson implements SaveJsonWithOptimisticLocking {

    @Nullable
    @Schema(required = false)
    String anrede;

    @NotNull
    @NonNull
    @Schema(required = true)
    Fachrichtung fachrichtung;

    @NotNull
    @NonNull
    @Schema(required = true)
    Rechtsform rechtsform;

    @Nullable
    @Schema(required = false)
    String vorname;

    @NotNull
    @NonNull
    @Schema(required = true)
    String name;

    @Nullable
    @Valid
    @Schema(required = false)
    AdresseJson adresse;

    @Nullable
    @Schema(required = false)
    NormalizedPhoneNumber telefon;

    @Nullable
    @Schema(required = false)
    EmailAddress email;

    @Nullable
    @Schema(required = false)
    String bemerkungen;

    @NotNull
    @NonNull
    @Schema(required = true)
    HpvTeilnahme hpvTeilnahme;

    @Schema(required = true)
    private long version;

}
