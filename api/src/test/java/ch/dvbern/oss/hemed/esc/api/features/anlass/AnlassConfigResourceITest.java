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

package ch.dvbern.oss.hemed.esc.api.features.anlass;

import java.time.LocalDate;

import ch.dvbern.dvbstarter.shared.i18n.datetime.DateConst;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfigService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.types.RequirementSetting;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.AnlassConfigBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSupport;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.AnlassConfigApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AnlassConfigJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AnlassConfigListJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AnlassConfigSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AnlassTypSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@QuarkusTest
@RequiredArgsConstructor
class AnlassConfigResourceITest extends AbstractITest {

    private final AnlassConfigApiSpec api = AnlassConfigApiSpec.anlassConfig(RequestSpecUtil.quarkusSpec());

    private final FixtureService fixtureService;
    private final AnlassConfigService anlassConfigService;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    @TestAsSupport
    void testFindAll() {
        // ARRANGE
        AnlassConfig config = fixtureService.createAnlassConfigAllOptional(AnlassTyp.KINDERGARTEN);

        // ACT
        AnlassConfigListJsonSpec result = api
            .anlassConfigResourceFindAllAnlassConfigs()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(AnlassConfigListJsonSpec.class);

        // ASSERT
        assertThat(result.getAnlassConfigList())
            .isNotEmpty()
            .anyMatch(configJson -> configJson.getId().equals(config.getId().getId())
                && configJson.getData().getAnlassTyp() == AnlassTypSpec.KINDERGARTEN);
    }

    @Test
    @TestAsSupport
    void testCreateAnlassConfigDisableOldIfExists() {
        // ARRANGE
        AnlassConfig oldAnlassConfig =
            anlassConfigService.getByAnlassTypAndStichtag(AnlassTyp.KINDERGARTEN, DateConst.END_OF_TIME);
        AnlassConfigSaveJsonSpec saveJsonSpec = AnlassConfigBuilder.createJaxSpec(
            builder -> builder
                .withAnlassTyp(AnlassTyp.KINDERGARTEN)
                .withGueltigkeitsZeitraum(
                    LocalDate.of(2025, 8, 1),
                    LocalDate.of(9999, 12, 31))
        ).getData();

        // ACT
        AnlassConfigListJsonSpec list = api
            .anlassConfigResourceCreateAnlassConfig()
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().as(AnlassConfigListJsonSpec.class);

        AnlassConfig oldAnlassConfigReread = anlassConfigService.getById(oldAnlassConfig.getId());

        // ASSERT
        assertThat(list).isNotNull();
        AnlassConfig result =
            anlassConfigService.getByAnlassTypAndStichtag(AnlassTyp.KINDERGARTEN, LocalDate.of(2025, 8, 1));
        assertThat(result).isNotNull();
        assertThat(oldAnlassConfig).isNotNull();
        assertEquals(oldAnlassConfigReread.getId(), oldAnlassConfig.getId());
        assertEquals(oldAnlassConfigReread.getAnlassTyp(), result.getAnlassTyp());
        assertThat(
            oldAnlassConfigReread.getGueltigkeitsZeitraum().getDatumBis())
            .isEqualTo(result.getGueltigkeitsZeitraum().getDatumVon().minusDays(1));
    }

    @Test
    @TestAsSupport
    void testUpdateAnlassConfig() {
        // ARRANGE
        AnlassConfig config = fixtureService.createAnlassConfigAllOptional(2025, AnlassTyp.KINDERGARTEN);
        AnlassTyp newAnlassTyp = AnlassTyp.KLASSE_4;
        RequirementSetting newSehenTafelSetting = RequirementSetting.REQUIRED;

        config.setAnlassTyp(newAnlassTyp);
        config.setSehenTafel(newSehenTafelSetting);

        AnlassConfigSaveJsonSpec saveJsonSpec = AnlassConfigBuilder.createJaxSpec(builder ->
            builder.forAnlassConfig(config)).getData();
        // ACT
        AnlassConfigListJsonSpec list = api
            .anlassConfigResourceUpdateAnlassConfig()
            .anlassConfigIdPath(config.getId().getId())
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(AnlassConfigListJsonSpec.class);

        // ASSERT
        assertThat(list).isNotNull();
        AnlassConfigJsonSpec result = list.getAnlassConfigList().stream().filter(anlassConfigJsonSpec ->  anlassConfigJsonSpec.getId().equals(config.getId().getId())).findFirst().orElse(null);
        assertThat(result).isNotNull();
        assertEquals(config.getId().getId(), result.getId());
        assertNotEquals(
            newAnlassTyp.toString(),
            result.getData().getAnlassTyp().toString()); // AnlassTyp is the only field which cant be updated
        assertEquals(
            AnlassTypSpec.KINDERGARTEN,
            result.getData().getAnlassTyp()); // Should keep the previous anlasstyp
        assertEquals(newSehenTafelSetting.toString(), result.getData().getSehenTafel().toString());
    }
}
