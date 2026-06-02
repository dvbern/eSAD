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

package ch.dvbern.oss.hemed.esc.api.shared.printer;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PrintUtilImplTest {

    private final PrintUtilImpl printUtil = new PrintUtilImpl("-");

    @Test
    void truncate_returnsPlaceholder_whenInputIsNull() {
        assertThat(printUtil.truncate(null, 10)).isEqualTo("-");
    }

    @Test
    void truncate_returnsPlaceholder_whenInputIsBlank() {
        assertThat(printUtil.truncate("   ", 10)).isEqualTo("-");
    }

    @Test
    void truncate_returnsTrimmedInput_whenShorterThanMaxLength() {
        assertThat(printUtil.truncate("  Müller ", 10)).isEqualTo("Müller");
    }

    @Test
    void truncate_returnsInputUnchanged_whenExactlyMaxLength() {
        String input = "ABCDEFGHIJ"; // 10 chars
        assertThat(printUtil.truncate(input, 10)).isEqualTo(input);
    }

    @Test
    void truncate_truncatesAndAppendsEllipsis_whenLongerThanMaxLength() {
        String input = "Hubertus-Maximilianus-Schwarzenegger";
        String result = printUtil.truncate(input, 10);

        assertThat(result)
            .hasSize(10)
            .endsWith("…")
            .isEqualTo("Hubertus-…");
    }

    @Test
    void truncate_returnsOnlyEllipsis_whenMaxLengthIsOne() {
        assertThat(printUtil.truncate("Anything", 1)).isEqualTo("…");
    }

    @Test
    void truncate_returnsTrimmedInput_whenMaxLengthIsZeroOrNegative() {
        // maxLength <= 0 is treated as "no truncation"; we still return the (sanitized) trimmed input
        assertThat(printUtil.truncate("Müller", 0)).isEqualTo("Müller");
        assertThat(printUtil.truncate("Müller", -5)).isEqualTo("Müller");
    }
}
