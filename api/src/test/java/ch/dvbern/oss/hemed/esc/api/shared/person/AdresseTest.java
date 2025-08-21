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

import ch.dvbern.oss.hemed.esc.api.testutil.builder.AdresseBuilder;
import org.assertj.core.api.Assertions;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AdresseTest {


    @ParameterizedTest
    @CsvSource({
        "Musterstrasse 12, Musterstrasse\u00A012",
        "Dorfstrasse 122b, Dorfstrasse\u00A0122b",
        "von Dänikenweg 2, von Dänikenweg\u00A02",
        ",",
        "' ', ",  // Empty string is null after trim
        "'  Hauptstrasse 123  ', Hauptstrasse\u00A0123",  // Leading/trailing spaces
        "'Bahnhofstrasse  45', Bahnhofstrasse\u00A045",   // Trailing spaces before number
        "'  Am Markt 7', Am Markt\u00A07",                // Leading spaces
        "Einbahnstrasse, Einbahnstrasse",                  // No number part
        "'Teststrasse   ', Teststrasse",                   // Only trailing spaces, no number

    })
    void getStrasseUndNrNonBreakingSpaceSeparated(@Nullable String strasseNr, @Nullable String expectedResult) {
        var addr = AdresseBuilder.defaultAdresse();
        addr.setStrasseUndNr(strasseNr);
        Assertions.assertThat(addr.getStrasseUndNrNonBreakingSpaceSeparated()).isEqualTo(expectedResult);
    }
}
