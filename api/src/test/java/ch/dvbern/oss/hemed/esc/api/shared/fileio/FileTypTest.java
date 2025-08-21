/*
 *
 * Copyright (C) 2024 DV Bern AG, Switzerland
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

package ch.dvbern.oss.hemed.esc.api.shared.fileio;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class FileTypTest {

    @Test
    void prefix_must_not_contain_doubled_underscores() {
        for (FileTyp fileTyp : FileTyp.values()) {
            Assertions.assertThat(fileTyp.getPrefix())
                .as(
                    "Prefix of %s must not contain double underscores because they separate the original filename",
                    fileTyp)
                .doesNotContain("__");
        }
    }
}
