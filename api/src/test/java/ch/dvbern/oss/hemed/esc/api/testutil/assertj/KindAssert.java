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

package ch.dvbern.oss.hemed.esc.api.testutil.assertj;

import java.util.Objects;

import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ImportTyp;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.external.KindExternal;
import ch.dvbern.oss.hemed.esc.api.features.kinder.external.KindExternalSyncHelperService;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.annotation.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static ch.dvbern.oss.hemed.esc.api.features.kinder.external.KindExternalSyncHelperService.mapGeburtsdatum;
import static ch.dvbern.oss.hemed.esc.api.features.kinder.external.KindExternalSyncHelperService.mapGeschlechtForSchueler;
import static ch.dvbern.oss.hemed.esc.api.features.kinder.external.KindExternalSyncHelperService.mapNationalitaetNameToCode;
import static ch.dvbern.oss.hemed.esc.api.features.kinder.external.KindExternalSyncHelperService.mapValidPhoneNumberOrNull;
import static ch.dvbern.oss.hemed.esc.api.features.kinder.external.KindExternalSyncHelperService.mapValidSvnrOrNull;

@SuppressWarnings({"UnusedReturnValue", "unused"})
public class KindAssert extends AbstractAssert<KindAssert, Kind> {

    private KindAssert(Kind actual) {
        super(actual, KindAssert.class);
    }

    public static KindAssert assertThatKind(Kind actual) {
        return new KindAssert(actual);
    }

    @CanIgnoreReturnValue
    public KindAssert hasSameDataAs(KindExternal otherKind) {
        isNotNull();

        if (!Objects.equals(actual.getName(), otherKind.getSchuelerName())) {
            failIfDifferent(actual.getName(), otherKind.getSchuelerName(), "Name");
        }

        if (!Objects.equals(actual.getVorname(), otherKind.getSchuelerVorname())) {
            failIfDifferent(actual.getVorname(), otherKind.getSchuelerVorname(), "Vorname");
        }

        if (!Objects.equals(actual.getGeburtsdatum(), mapGeburtsdatum(otherKind.getSchuelerGeburtsdatum(), ImportTyp.ESCADA))) {
            failIfDifferent(actual.getGeburtsdatum(), otherKind.getSchuelerGeburtsdatum(), "Geburtsdatum");
        }

        if (actual.getNationalitaet() != mapNationalitaetNameToCode(otherKind.getSchuelerStaatcodeISO())) {

            failIfDifferent(actual.getNationalitaet(), otherKind.getSchuelerNationalitaet(), "Nationalitaet");
        }

        if (!Objects.equals(actual.getTelefon(), mapValidPhoneNumberOrNull(otherKind, ImportTyp.ESCADA))) {
            failIfDifferent(actual.getTelefon(), otherKind.getSchuelerMobile(), "Telefon");
        }

        if (!Objects.equals(actual.getEmail(), KindExternalSyncHelperService.mapValidEmailAdressOrNull(otherKind, ImportTyp.ESCADA))) {
            failIfDifferent(actual.getEmail(), otherKind.getSchuelerEMail(), "Email");
        }

        if (!Objects.equals(actual.getSozialversicherungsNummer(), mapValidSvnrOrNull(otherKind, ImportTyp.ESCADA))) {
            failIfDifferent(
                actual.getSozialversicherungsNummer(),
                otherKind.getSchuelerSozialversicherung(),
                "SozialversicherungsNummer");
        }

        if (!Objects.equals(actual.getErstsprache(), otherKind.getSchuelerErstsprache())) {
            failIfDifferent(actual.getErstsprache(), otherKind.getSchuelerErstsprache(), "Erstsprache");
        }

        if (actual.getGeschlecht() != mapGeschlechtForSchueler(otherKind.getSchuelerGeschlecht())) {

            failIfDifferent(actual.getGeschlecht(), otherKind.getSchuelerGeschlecht(), "Geschlecht");
        }

        if (!Objects.equals(actual.getExternalSystemIdOrNull(), otherKind.getExternalId())) {
            failIfDifferent(actual.getExternalSystemIdOrNull(), otherKind.getExternalId(), "ExternalSystemId");
        }

        if (!Objects.equals(actual.getAdresse(), KindExternalSyncHelperService.extractAdresse(otherKind))) {
            failIfDifferent(actual.getAdresse(), KindExternalSyncHelperService.extractAdresse(otherKind), "Adresse");
        }

        return this;
    }

    private <T> void failIfDifferent(@Nullable T actualValue, @Nullable T expectedValue, @NonNull String field) {
        failWithActualExpectedAndMessage(
            actualValue,
            expectedValue,
            "Expected Kind field '%s' to have same data but was >%s< instead of >%s<",
            field,
            actualValue,
            expectedValue);
    }

}
