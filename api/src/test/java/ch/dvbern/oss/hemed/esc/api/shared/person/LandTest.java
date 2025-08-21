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

package ch.dvbern.oss.hemed.esc.api.shared.person;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LandTest {

    @ParameterizedTest
    @CsvSource(
            {
                "DO",
                "FR",
                "SA",
                "SO",
                "AF",
                "AL",
                "AM",
                "AO",
                "AR",
                "AT",
                "AU",
                "AZ",
                "BA",
                "BD",
                "BE",
                "BG",
                "BI",
                "BJ",
                "BO",
                "BR",
                "BY",
                "CA",
                "CD",
                "CG",
                "CH",
                "CI",
                "CL",
                "CM",
                "CN",
                "CO",
                "CR",
                "CU",
                "CV",
                "CY",
                "CZ",
                "DE",
                "DK",
                "DZ",
                "EC",
                "EE",
                "EG",
                "ER",
                "ES",
                "ET",
                "FI",
                "GB",
                "GE",
                "GH",
                "GM",
                "GN",
                "GR",
                "GT",
                "HR",
                "HT",
                "HU",
                "ID",
                "IE",
                "IL",
                "IN",
                "IQ",
                "IR",
                "IS",
                "IT",
                "JM",
                "JO",
                "JP",
                "KE",
                "KG",
                "KO",
                "KR",
                "KZ",
                "LB",
                "LI",
                "LK",
                "LR",
                "LT",
                "LU",
                "LV",
                "LY",
                "MA",
                "MD",
                "ME",
                "MK",
                "MKD",
                "ML",
                "MM",
                "MN",
                "MV",
                "MX",
                "MY",
                "MZ",
                "NG",
                "NI",
                "NL",
                "NO",
                "NP",
                "NZ",
                "PA",
                "PE",
                "PH",
                "PK",
                "PL",
                "PT",
                "RO",
                "RS",
                "RU",
                "RW",
                "SD",
                "SE",
                "SG",
                "SI",
                "SK",
                "SL",
                "SN",
                "SV",
                "SY",
                "TG",
                "TH",
                "TI",
                "TJ",
                "TN",
                "TR",
                "TW",
                "TZ",
                "UA",
                "UG",
                "US",
                "UY",
                "UZ",
                "VA",
                "VE",
                "VN",
                "ZA",
                "ZM",
                "ZW",
            }
    )
    void testEscadaLaender(String escadaStaatCode){
        Assertions.assertThat(Land.getByEscadaIsoCode(escadaStaatCode)).isNotNull();
    }

    @ParameterizedTest
    @CsvSource(
        { "KO, KOSOVO", "MKD, NORDMAZEDONIEN" }
    )
    void testEscadaLaenderSpecialCases(
        String escadaStaatCode,
        Land expectedLand
    ) {
        Assertions.assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> Land.getByIsoCode(escadaStaatCode));
        Assertions.assertThat(Land.getByEscadaIsoCode(escadaStaatCode)).isEqualTo(expectedLand);
    }
}
