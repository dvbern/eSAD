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

package ch.dvbern.oss.hemed.esc.api.features.kinder.external;

import java.time.LocalDate;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.clock.FixedClockProvider;
import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumber;
import ch.dvbern.dvbstarter.shared.types.sozialversicherungsnummer.SozialversicherungsNummer;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ImportTyp;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.shared.person.Adresse;
import ch.dvbern.oss.hemed.esc.api.shared.person.Geschlecht;
import ch.dvbern.oss.hemed.esc.api.shared.person.Land;
import ch.dvbern.oss.hemed.esc.api.testutil.assertj.KindAssert;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.KindBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.KindExternalBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.KlasseBuilder;
import lombok.Getter;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class KindExternalSyncHelperServiceTest {

    private final FixedClockProvider clockProvider = FixedClockProvider.now();

    @Getter
    private final AppClock appClock = new AppClock(clockProvider);

    @ParameterizedTest
    @CsvSource({
        "invalidmail,",
        "validmail@example.com,validmail@example.com",
        ",",
    })
    void testIsValidEmail(String email, String expected) {
        var kind = new KindExternal();
        kind.setSchuelerEMail(email);
        kind.setSchuelerId("irrelevant");
        if (expected != null) {
            Assertions.assertThat(KindExternalSyncHelperService.mapValidEmailAdressOrNull(kind, ImportTyp.ESCADA))
                .isNotNull()
                .extracting(EmailAddress::getAddress)
                .isEqualTo(expected);
        } else {
            Assertions.assertThat(KindExternalSyncHelperService.mapValidEmailAdressOrNull(kind, ImportTyp.ESCADA)).isNull();
        }
    }

    @ParameterizedTest
    @CsvSource({
        "1234,",
        "asdf,",
        "034 402 12 34, +41 34 402 12 34",
        "076 123 45 67, +41 76 123 45 67",
        ",",
    })
    void testIsValidPhone(String phone, String expected) {
        var kind = new KindExternal();
        kind.setSchuelerMobile(phone);
        kind.setSchuelerId("irrelevantForParsing_only_for_logging");
        if (expected != null) {
            Assertions.assertThat(KindExternalSyncHelperService.mapValidPhoneNumberOrNull(kind, ImportTyp.ESCADA))
                .isNotNull()
                .extracting(NormalizedPhoneNumber::getFormattedPhoneNumber)
                .isEqualTo(expected);
        } else {
            Assertions.assertThat(KindExternalSyncHelperService.mapValidPhoneNumberOrNull(kind, ImportTyp.ESCADA)).isNull();
        }
    }

    @ParameterizedTest
    @CsvSource({
        "1234,",
        "asdf,",
        "7562298328400, 756.2298.3284.00",
        "756.2298.3284.00, 756.2298.3284.00",
        ",",
    })
    void testIsValidSvnr(String svnr, String expected) {
        var kind = new KindExternal();
        kind.setSchuelerSozialversicherung(svnr);
        kind.setSchuelerId("irrelevantForParsing_only_for_logging");
        if (expected != null) {
            Assertions.assertThat(KindExternalSyncHelperService.mapValidSvnrOrNull(kind, ImportTyp.ESCADA))
                .isNotNull()
                .extracting(SozialversicherungsNummer::toFormattedString)
                .isEqualTo(expected);
        } else {
            Assertions.assertThat(KindExternalSyncHelperService.mapValidSvnrOrNull(kind, ImportTyp.ESCADA)).isNull();
        }
    }

    @Nested
    @DisplayName("mapping in updateKindBasedOnKindExternal")
    class UpdateKindBasedOnKindExternal {

        private final KindExternal kindExternalUpdated = KindExternalBuilder.create(builder -> {
            builder.withEscadaKlasseId("123");
            builder.withSchulhausId("456");
            builder.withSchuelerErstsprache("Deutsch");
            builder.withSchuelerSprache("Engisch");
            builder.withSchuelerStaatcodeISO("PL");
            builder.withSchuelerNationalitaet("Polnisch");
            builder.withSchuelerEMail("changetest@mailbucket.dvbern.ch");
            builder.withSchuelerMobile("076 987 65 43");
            builder.withSchuelerOrt("Mytestort");
            builder.withSchuelerPLZ("7644");
            builder.withSchuelerAdresse2("schuelerAdresse2");
            builder.withSchuelerAdresse1("schuelerAdresse1");
            builder.withSchuelerAdresse("adresse");
            builder.withSchuelerSozialversicherung("756.4071.8253.59");
            builder.withSchuelerGeschlecht("2");
            builder.withSchuelerGeburtsdatum("2017-05-20T00:00:00");
            builder.withSchuelerVorname("Mychange_vorname");
            builder.withSchuelerName("Mychangename_name");
            builder.withWohntyp("Heim");
            builder.withSchuelerId("441122");
        });

        @Test
        void should_change_all_values_if_previous_db_valu_was_unchanged_vs_escada() {
            // Arrange
            KindExternal existingKindExternal = KindExternalBuilder.defaultKindExternal();
            Kind kindWithUnchangedEscadaData = KindExternalSyncHelperService.toEntityForCreate(
                existingKindExternal, KlasseBuilder.defaultKlasse(), appClock.offsetDateTime().now(), ImportTyp.ESCADA
            );

            // Act
            Kind kindAfterUpdate = KindExternalSyncHelperService.updateKindBasedOnKindExternal(
                kindWithUnchangedEscadaData,
                kindExternalUpdated,
                existingKindExternal,
                appClock.offsetDateTime().now(),
                ImportTyp.ESCADA);

            // Assert
            // Check if the data is updated correctly, all fields should have changed
            Assertions.assertThat(kindAfterUpdate).isNotNull()
                .returns("Mychangename_name", Kind::getName)
                .returns("Mychange_vorname", Kind::getVorname)
                .returns(LocalDate.of(2017, 5, 20), Kind::getGeburtsdatum)
                .returns(Land.POLEN, Kind::getNationalitaet)
                .returns(NormalizedPhoneNumber.parse("+41769876543"), Kind::getTelefon)
                .returns(EmailAddress.fromString("changetest@mailbucket.dvbern.ch"), Kind::getEmail)
                .returns(new SozialversicherungsNummer("756.4071.8253.59"), Kind::getSozialversicherungsNummer)
                .returns("Deutsch", Kind::getErstsprache)
                .returns(Geschlecht.WEIBLICH, Kind::getGeschlecht)
                .returns("441122", Kind::getExternalSystemIdOrNull)
                .extracting(Kind::getAdresse).isNotNull()
                .returns("adresse", Adresse::getStrasseUndNr)
                .returns("7644", Adresse::getPlz)
                .returns("Mytestort", Adresse::getOrt)
                .returns(Land.SCHWEIZ, Adresse::getLand);
        }

        @Test
        void should_not_change_values_if_previous_db_valud_was_changed_vs_escada() {
            // Arrange
            KindExternal previousKindExternal = KindExternalBuilder.defaultKindExternal();
            Kind defaultKind = KindBuilder.defaultKind();

            // ACT
            Kind updatedKind = KindExternalSyncHelperService.updateKindBasedOnKindExternal(
                defaultKind,
                previousKindExternal,
                previousKindExternal,
                appClock.offsetDateTime().now(),
                ImportTyp.ESCADA);

            // ASSERT
            // check that data did not change
            Assertions.assertThat(updatedKind).isNotNull()
                .returns(defaultKind.getName(), Kind::getName)
                .returns(defaultKind.getVorname(), Kind::getVorname)
                .returns(defaultKind.getGeburtsdatum(), Kind::getGeburtsdatum)
                .returns(defaultKind.getNationalitaet(), Kind::getNationalitaet)
                .returns(defaultKind.getTelefon(), Kind::getTelefon)
                .returns(defaultKind.getEmail(), Kind::getEmail)
                .returns(defaultKind.getSozialversicherungsNummer(), Kind::getSozialversicherungsNummer)
                .returns(defaultKind.getErstsprache(), Kind::getErstsprache)
                .returns(defaultKind.getGeschlecht(), Kind::getGeschlecht)
                .returns(defaultKind.getExternalSystemIdOrNull(), Kind::getExternalSystemIdOrNull)
                .extracting(Kind::getAdresse).isNotNull()
                .isEqualTo(defaultKind.getAdresse());
        }

        @Test
        void should_change_all_values_if_null_before() {
            // Arrange
            KindExternal previousKindExternal = KindExternalBuilder.defaultKindExternal();
            Kind defaultKind = new Kind();

            // ACT
            Kind updatedKind = KindExternalSyncHelperService.updateKindBasedOnKindExternal(
                defaultKind,
                previousKindExternal,
                previousKindExternal,
                appClock.offsetDateTime().now(),
                ImportTyp.ESCADA);
            // ASSERT
            KindAssert.assertThatKind(updatedKind).hasSameDataAs(previousKindExternal);

            // ACT
            // update should change values
            KindExternalSyncHelperService.updateKindBasedOnKindExternal(
                updatedKind,
                kindExternalUpdated,
                previousKindExternal,
                appClock.offsetDateTime().now(),
                ImportTyp.ESCADA);
            // ASSERT
            KindAssert.assertThatKind(updatedKind).hasSameDataAs(kindExternalUpdated);
        }
    }

}
