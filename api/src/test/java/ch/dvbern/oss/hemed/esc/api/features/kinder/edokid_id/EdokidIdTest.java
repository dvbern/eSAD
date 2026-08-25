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

package ch.dvbern.oss.hemed.esc.api.features.kinder.edokid_id;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class EdokidIdTest {

    @ParameterizedTest
    @CsvSource(value = {
        "'',''",
        "2,2",
        "25,25",
        "251,25 1",
        "2515,25 15",
        "25150,25 15 0",
        "2515067,25 15 067",
        "251506725,25 15 067 25",
        "'25 15 067',25 15 067",
        "'  25   15 067  ',25 15 067",
        "A0014562,A0 01 456 2",
        "A00145624,A0 01 456 24",
        // longer than a complete esadId: the additional characters are simply appended
        "A001456245,A0 01 456 245",
    })
    void formatPartial(String input, String expected) {
        assertThat(EdokidId.formatPartial(input)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource(value = {
        "A,001,456,24,A0 01 456 24",
        "B,123,999,07,B1 23 999 07",
    })
    void toFormattedStringUsesTheSameFormatAsFormatPartial(
        char character,
        String laufnummer,
        String schulhausnummer,
        int eintrittsjahr,
        String expected
    ) {
        EdokidId edokidId = new EdokidId(character, laufnummer, schulhausnummer, eintrittsjahr);
        assertThat(edokidId.toFormattedString()).isEqualTo(expected);
    }
}
