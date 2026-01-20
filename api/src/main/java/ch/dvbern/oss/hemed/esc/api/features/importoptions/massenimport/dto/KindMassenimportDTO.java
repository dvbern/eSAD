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

import java.time.LocalDate;

import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumber;
import ch.dvbern.dvbstarter.shared.types.sozialversicherungsnummer.SozialversicherungsNummer;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import ch.dvbern.oss.hemed.esc.api.shared.person.Land;
import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.Nullable;

@Value
public class KindMassenimportDTO {

    @NotNull
    private String name;

    @NotNull
    private String vorname;

    @NotNull
    private Geschlecht geschlecht;

    @NotNull
    private LocalDate geburtsdatum;

    @Nullable
    private SozialversicherungsNummer sozialversicherungsNummer;

    @Nullable
    private Land nationalitaet;

    @Nullable
    private String erstsprache;

    @Nullable
    private EmailAddress email;

    @Nullable
    private NormalizedPhoneNumber telefon;

    @Nullable
    private String wohntyp;

    @Nullable
    private AdresseMassenimportDTO adresse;

    @Nullable
    private SchuleMassenimportDTO schuleInfo;

    @Nullable
    private KlasseMassenimportDTO klasseInfo;
}
