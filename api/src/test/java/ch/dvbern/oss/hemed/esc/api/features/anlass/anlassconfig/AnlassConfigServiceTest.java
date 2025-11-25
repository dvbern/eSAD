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

package ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig;

import java.time.LocalDate;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.AnlassConfigBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
@RequiredArgsConstructor
class AnlassConfigServiceTest {

    private final AnlassConfigService anlassConfigService;
    private final AnlassConfigRepo anlassConfigRepo;

    @Test
    @TestAsAdminBS
    void getByAnlassTypAndStichtag() {
        // ARRANGE some Configs that are valid at the stichtag but do not overlap with our pre existing configs that
        // starts at 2000.01.01 and
        LocalDate stichtag = LocalDate.of(1998, 2, 1);
        AnlassConfig configKlasse4 = AnlassConfigBuilder.create(builder -> builder
            .withID(ID.random(AnlassConfig.class))
            .withGueltigkeitsZeitraum(LocalDate.of(1998, 1, 1), LocalDate.of(1998, 12, 31))
            .withAnlassTyp(AnlassTyp.KLASSE_4));
        anlassConfigRepo.persist(configKlasse4);
        AnlassConfig configKlasse8 = AnlassConfigBuilder.create(builder -> builder
            .withID(ID.random(AnlassConfig.class))
            .withGueltigkeitsZeitraum(LocalDate.of(1998, 1, 1), LocalDate.of(1998, 12, 31))
            .withAnlassTyp(AnlassTyp.KLASSE_8));
        anlassConfigRepo.persist(configKlasse8);

        // ACT
        AnlassConfig resultKlasse4 = anlassConfigService.getByAnlassTypAndStichtag(AnlassTyp.KLASSE_4, stichtag);
        AnlassConfig resultKlasse8 = anlassConfigService.getByAnlassTypAndStichtag(AnlassTyp.KLASSE_8, stichtag);

        // ASSERT
        Assertions.assertThat(resultKlasse4).isNotNull();
        Assertions.assertThat(resultKlasse8).isNotNull();
        AppFailureException ex = assertThrows(
            AppFailureException.class,
            () -> anlassConfigService.getByAnlassTypAndStichtag(AnlassTyp.KLASSE_9, stichtag)
        );
        assertThat(ex)
            .hasMessageContaining("ANLASS_CONFIG_NOT_FOUND");
    }
}
