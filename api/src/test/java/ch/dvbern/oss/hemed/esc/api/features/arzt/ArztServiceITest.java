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

package ch.dvbern.oss.hemed.esc.api.features.arzt;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import io.quarkus.test.junit.QuarkusTest;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class ArztServiceITest {


    private final ArztService arztService;
    @Test
    @TestAsAdminBS
    void findByIds() {
        // ARRANGE
        final List<ID<Arzt>> queryList =
            Stream.of("11111111-1111-1111-1111-111111111002", "11111111-1111-1111-1111-111111111003")
                .map(id -> ID.of(UUID.fromString(id), Arzt.class))
                .toList();

        final List<String> expectedIds = List.of("11111111-1111-1111-1111-111111111002");

        // ACT
        final List<Arzt> actualAerzte = arztService.findByIds(queryList);

        // ASSERT
        Assertions.assertThat(actualAerzte)
            .extracting(Arzt::getId)
            .extracting(ID::getUUID)
            .extracting(UUID::toString)
            .containsAll(expectedIds);
    }
}
