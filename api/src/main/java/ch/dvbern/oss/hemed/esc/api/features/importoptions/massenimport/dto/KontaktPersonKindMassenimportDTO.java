/*
 * Copyright (C) 2025 DV Bern AG, Switzerland
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

package ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport.dto;

import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumber;
import ch.dvbern.oss.hemed.esc.api.features.kinder.types.KontaktPersonKindBeziehung;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.Nullable;

@Setter
@Getter
@AllArgsConstructor
public class KontaktPersonKindMassenimportDTO {

    @Nullable
    private Geschlecht geschlechtKp;

    @Nullable
    private String nachnameKp;

    @Nullable
    private String vornameKp;

    @Nullable
    private KontaktPersonKindBeziehung beziehungKp;

    @Nullable
    private EmailAddress emailKp;

    @Nullable
    private NormalizedPhoneNumber telefonKp;

    private boolean korrespondenzKp;

    @Nullable
    private AdresseMassenimportDTO adresseDTO;

}
