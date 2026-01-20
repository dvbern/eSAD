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
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumber;
import ch.dvbern.dvbstarter.shared.types.sozialversicherungsnummer.SozialversicherungsNummer;
import ch.dvbern.oss.hemed.esc.api.features.kinder.types.KontaktPersonKindBeziehung;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import ch.dvbern.oss.hemed.esc.api.shared.person.Land;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.checkerframework.checker.nullness.qual.NonNull;

@Value
@AllArgsConstructor
public class MassenimportRowDTO {

    @NotNull
    private int rowIndex;

    @NonNull
    private ValueErrorObject<SozialversicherungsNummer> sozialversicherungsNummer;

    @NonNull
    private ValueErrorObject<String> nachname;

    @NonNull
    private ValueErrorObject<String> vorname;

    @NonNull
    private ValueErrorObject<Geschlecht> geschlecht;

    @NonNull
    private ValueErrorObject<LocalDate> geburtsdatum;

    @NonNull
    private ValueErrorObject<Land> nationalitaet;

    @NonNull
    private ValueErrorObject<String> erstsprache;

    @NonNull
    private ValueErrorObject<EmailAddress> email;

    @NonNull
    private ValueErrorObject<NormalizedPhoneNumber> telefon;

    @NonNull
    private ValueErrorObject<String> wohntyp;

    @NonNull
    private ValueErrorObject<Land> land;

    @NonNull
    private ValueErrorObject<String> strasseUndNr;

    @NonNull
    private ValueErrorObject<String> adresszusatz;

    @NonNull
    private ValueErrorObject<String> plz;

    @NonNull
    private ValueErrorObject<String> ort;

    @NonNull
    private ValueErrorObject<Geschlecht> geschlechtKp;

    @NonNull
    private ValueErrorObject<String> nachnameKp;

    @NonNull
    private ValueErrorObject<String> vornameKp;

    @NonNull
    private ValueErrorObject<KontaktPersonKindBeziehung> beziehungKp;

    @NonNull
    private ValueErrorObject<EmailAddress> emailKp;

    @NonNull
    private ValueErrorObject<NormalizedPhoneNumber> telefonKp;

    @NonNull
    private ValueErrorObject<Boolean> korrespondenzKp;

    @NonNull
    private ValueErrorObject<Land> landKp;

    @NonNull
    private ValueErrorObject<String> strasseUndNrKp;

    @NonNull
    private ValueErrorObject<String> adresszusatzKp;

    @NonNull
    private ValueErrorObject<String> plzKp;

    @NonNull
    private ValueErrorObject<String> ortKp;

    @NonNull
    private ValueErrorObject<Klasse> klasse;

    public List<MassenimportErrorDTO> getAllErrors() {
        return Stream.of(
                sozialversicherungsNummer,
                nachname,
                vorname,
                geschlecht,
                geburtsdatum,
                nationalitaet,
                erstsprache,
                email,
                telefon,
                wohntyp,
                land,
                strasseUndNr,
                adresszusatz,
                plz,
                ort,
                geschlechtKp,
                nachnameKp,
                vornameKp,
                beziehungKp,
                emailKp,
                telefonKp,
                korrespondenzKp,
                landKp,
                strasseUndNrKp,
                adresszusatzKp,
                plzKp,
                ortKp,
                klasse
            )
            .filter(Objects::nonNull)
            .map(ValueErrorObject::getMassenimportErrorDTO)
            .filter(Objects::nonNull)
            .toList();
    }

    public boolean hasCriticalErrors() {
        return getAllErrors().stream()
            .anyMatch(error -> error.getErrorSeverity() == MassenimportProblemSeverity.ROLLBACK);
    }

    public boolean hasImportableKontaktpersonKindData() {
        return !hasCriticalErrors()
            && getNachnameKp().getValue() != null
            && getGeschlechtKp().getValue() != null;
    }

    public boolean hasImportableKindData() {
        return !hasCriticalErrors()
            && getNachname().getValue() != null
            && getVorname().getValue() != null
            && getGeschlecht().getValue() != null
            && getGeburtsdatum().getValue() != null;
    }
}
