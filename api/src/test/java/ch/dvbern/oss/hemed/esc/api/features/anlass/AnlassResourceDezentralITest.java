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
import java.util.UUID;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfigService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.AnlassBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBE;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAnotherSchularztBE;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSchoolNurseBE;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSchularztBE;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSupportBE;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.AnlassApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AnlassJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AnlassListJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AnlassSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AnlassStatusSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AnlassTypSpec;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@Slf4j
@QuarkusTest
@RequiredArgsConstructor
class AnlassResourceDezentralITest extends AbstractITest {

    private final AnlassApiSpec api = AnlassApiSpec.anlass(RequestSpecUtil.quarkusSpec());

    private final AnlassService anlassService;
    private final AnlassConfigService anlassConfigService;
    private final FixtureService fixtureService;
    private final AppClock appClock;
    private final ConfigService configService;
    private final TenantProducer tenantProducer;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();

        tenantProducer.setCurrentTenant(TenantEnum.BE);
        // Mock limit in config service to make sure we get all the results. Delegate all other calls to 'real'
        // config Service
        ConfigService configServiceMock = Mockito.spy(configService);
        Mockito.when(configServiceMock.getDashboardAnlassLimit()).thenReturn(Integer.MAX_VALUE);
        QuarkusMock.installMockForType(configServiceMock, ConfigService.class);
    }

    @Test
    @TestAsSchularztBE
    void getAnlass_sameOrg_shouldSucceed() {
        // ARRANGE
        Benutzer arzt = getSchularztBE();
        Anlass anlass = createAnlassWithArzt(arzt, null);

        // ACT
        AnlassJsonSpec result = api
            .anlassResourceGetAnlass()
            .anlassIdPath(anlass.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(AnlassJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isEqualTo(anlass.getId().getId());
    }

    @Test
    @TestAsAnotherSchularztBE
    void getAnlass_differentOrg_shouldFail() {
        // ARRANGE
        Benutzer arztInOtherOrg = getSchularztBE();
        Anlass anlass = createAnlassWithArzt(arztInOtherOrg, null);

        // ACT & ASSERT
        api.anlassResourceGetAnlass()
            .anlassIdPath(anlass.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    @TestAsSchularztBE
    void getAnlass_noArzt_shouldFail() {
        // ARRANGE
        UUID anlassID = UUID.randomUUID();
        createAnlassWithNoArzt(anlassID);
        // ACT & ASSERT
        api.anlassResourceGetAnlass()
            .anlassIdPath(anlassID.toString())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    @TestAsSupportBE
    void getAnlass_asSupport_shouldSucceed() {
        // ARRANGE
        Benutzer arzt = getSchularztBE();
        Anlass anlass = createAnlassWithArzt(arzt, null);

        // ACT
        AnlassJsonSpec result = api
            .anlassResourceGetAnlass()
            .anlassIdPath(anlass.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(AnlassJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isEqualTo(anlass.getId().getId());
    }

    @Test
    @TestAsSchularztBE
    void createAnlass_sameOrg_shouldSucceed() {
        // ARRANGE
        Benutzer arzt = getSchularztBE();
        AnlassConfig config =
            anlassConfigService.getByAnlassTypAndStichtag(AnlassTyp.KLASSE_8, appClock.getToday().plusMonths(1));
        AnlassSaveJsonSpec saveAnlassJson = AnlassBuilder.createJaxSpec(builder -> builder
            .withAnlassTyp(AnlassTyp.KLASSE_8)
            .withAnlassConfig(config)
            .withArzt(arzt)
            .withTitle("Dezentral Create Anlass")).getData();

        // ACT
        AnlassJsonSpec result = api
            .anlassResourceCreateAnlass()
            .body(saveAnlassJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().as(AnlassJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isNotNull();
        Assertions.assertThat(result.getData().getTitel()).isEqualTo("Dezentral Create Anlass");
        Assertions.assertThat(result.getAnlassConfig().getData().getAnlassTyp()).isEqualTo(AnlassTypSpec.KLASSE_8);
    }

    @Test
    @TestAsAnotherSchularztBE
    void createAnlass_differentOrg_shouldFail() {
        // ARRANGE
        Benutzer arztInOtherOrg = getSchularztBE();
        AnlassConfig config =
            anlassConfigService.getByAnlassTypAndStichtag(AnlassTyp.KLASSE_8, appClock.getToday().plusMonths(1));
        AnlassSaveJsonSpec saveAnlassJson = AnlassBuilder.createJaxSpec(builder -> builder
            .withAnlassTyp(AnlassTyp.KLASSE_8)
            .withAnlassConfig(config)
            .withArzt(arztInOtherOrg)
            .withTitle("Should Fail Create")).getData();

        // ACT & ASSERT
        api.anlassResourceCreateAnlass()
            .body(saveAnlassJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    @TestAsSchularztBE
    void updateAnlass_sameOrg_shouldSucceed() {
        // ARRANGE
        Benutzer arzt = getSchularztBE();
        Anlass existingAnlass = createAnlassWithArzt(arzt, null);

        AnlassJsonSpec updatedAnlassJson = AnlassBuilder.createJaxSpec(builder -> builder
            .forAnlass(existingAnlass)
            .withTitle("Updated Dezentral Anlass"));

        // ACT
        AnlassJsonSpec result = api
            .anlassResourceUpdateAnlass()
            .anlassIdPath(existingAnlass.getId().getId())
            .body(updatedAnlassJson.getData())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(AnlassJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isEqualTo(existingAnlass.getId().getId());
        Assertions.assertThat(result.getData().getTitel()).isEqualTo("Updated Dezentral Anlass");
    }

    @Test
    @TestAsAnotherSchularztBE
    void updateAnlass_differentOrg_shouldFail() {
        // ARRANGE
        Benutzer arztInOtherOrg = getSchularztBE();
        Anlass existingAnlass = createAnlassWithArzt(arztInOtherOrg, null);

        AnlassJsonSpec updatedAnlassJson = AnlassBuilder.createJaxSpec(builder -> builder
            .forAnlass(existingAnlass)
            .withTitle("Should Fail Update"));

        // ACT & ASSERT
        api.anlassResourceUpdateAnlass()
            .anlassIdPath(existingAnlass.getId().getId())
            .body(updatedAnlassJson.getData())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    @TestAsSchularztBE
    void changeAnlassStatus_sameOrg_shouldSucceed() {
        // ARRANGE
        Benutzer arzt = getSchularztBE();
        Anlass anlass = createAnlassWithArzt(arzt, arzt);
        Assertions.assertThat(anlass.getAnlassStatus()).isEqualTo(AnlassStatus.IN_PLANUNG);

        // ACT
        AnlassJsonSpec result = api
            .anlassResourceChangeAnlassStatus()
            .anlassIdPath(anlass.getId().getId())
            .statusPath(AnlassStatusSpec.DURCHGEFUEHRT)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(AnlassJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getData().getAnlassStatus()).isEqualTo(AnlassStatusSpec.DURCHGEFUEHRT);
    }

    @Test
    @TestAsAnotherSchularztBE
    void changeAnlassStatus_differentOrg_shouldFail() {
        // ARRANGE
        Benutzer arztInOtherOrg = getSchularztBE();
        Anlass anlass = fixtureService.createAnlass(AnlassStatus.IN_PLANUNG, arztInOtherOrg);

        // ACT & ASSERT
        api.anlassResourceChangeAnlassStatus()
            .anlassIdPath(anlass.getId().getId())
            .statusPath(AnlassStatusSpec.DURCHGEFUEHRT)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    @TestAsSchularztBE
    void removeAnlass_sameOrg_shouldSucceed() {
        // ARRANGE
        Benutzer arzt = getSchularztBE();
        Anlass anlass = createAnlassWithArzt(arzt, null);

        // ACT & ASSERT
        api.anlassResourceRemoveAnlass()
            .anlassIdPath(anlass.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());
    }

    @Test
    @TestAsAnotherSchularztBE
    void removeAnlass_differentOrg_shouldFail() {
        // ARRANGE
        Benutzer arztInOtherOrg = getSchularztBE();
        Anlass anlass = createAnlassWithArzt(arztInOtherOrg, null);

        // ACT & ASSERT
        api.anlassResourceRemoveAnlass()
            .anlassIdPath(anlass.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    @TestAsSchularztBE
    void writeAnlass_abgeschlossen_schularzt_shouldFail() {
        // ARRANGE
        Benutzer arzt = getSchularztBE();
        Anlass anlass = createAnlassWithArztAndStatus(arzt, null, AnlassStatus.ABGESCHLOSSEN);

        AnlassJsonSpec updatedAnlassJson = AnlassBuilder.createJaxSpec(builder -> builder
            .forAnlass(anlass)
            .withTitle("Should Fail - Abgeschlossen"));

        // ACT & ASSERT
        api.anlassResourceUpdateAnlass()
            .anlassIdPath(anlass.getId().getId())
            .body(updatedAnlassJson.getData())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    @TestAsSupportBE
    void writeAnlass_abgeschlossen_support_shouldSucceed() {
        // ARRANGE
        Benutzer arzt = getSchularztBE();
        Anlass anlass = createAnlassWithArztAndStatus(arzt, null, AnlassStatus.ABGESCHLOSSEN);

        AnlassJsonSpec updatedAnlassJson = AnlassBuilder.createJaxSpec(builder -> builder
            .forAnlass(anlass)
            .withTitle("Updated After Abgeschlossen"));

        // ACT
        AnlassJsonSpec result = api
            .anlassResourceUpdateAnlass()
            .anlassIdPath(anlass.getId().getId())
            .body(updatedAnlassJson.getData())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(AnlassJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getData().getTitel()).isEqualTo("Updated After Abgeschlossen");
    }

    @Test
    @TestAsSchularztBE
    void readLaeusekontrolle_sameOrg_shouldSucceed() {
        // ARRANGE
        Benutzer arzt = getSchularztBE();
        Anlass anlass = createLaeusekontrolleWithArzt(arzt);

        // ACT
        AnlassJsonSpec result = api
            .anlassResourceGetAnlass()
            .anlassIdPath(anlass.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(AnlassJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isEqualTo(anlass.getId().getId());
    }

    @Test
    @TestAsAnotherSchularztBE
    void readLaeusekontrolle_differentOrg_shouldFail() {
        // ARRANGE
        Benutzer arztInOtherOrg = getSchularztBE();
        Anlass anlass = createLaeusekontrolleWithArzt(arztInOtherOrg);

        // ACT & ASSERT
        api.anlassResourceGetAnlass()
            .anlassIdPath(anlass.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    @TestAsSchularztBE
    void writeLaeusekontrolle_sameOrg_shouldSucceed() {
        // ARRANGE
        Benutzer arzt = getSchularztBE();
        Anlass anlass = createLaeusekontrolleWithArzt(arzt);

        AnlassJsonSpec updatedAnlassJson = AnlassBuilder.createJaxSpec(builder -> builder
            .forAnlass(anlass)
            .withTitle("Updated Laeusekontrolle"));

        // ACT
        AnlassJsonSpec result = api
            .anlassResourceUpdateAnlass()
            .anlassIdPath(anlass.getId().getId())
            .body(updatedAnlassJson.getData())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(AnlassJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getData().getTitel()).isEqualTo("Updated Laeusekontrolle");
    }

    @Test
    @TestAsSchularztBE
    void findAllAnlaesse_shouldOnlyReturnAnlaesseFromSameOrg() {
        // ARRANGE
        Benutzer sameOrgArzt = getSchularztBE();
        Benutzer otherOrgArzt = getAnotherSchularztBE();

        Anlass sameOrgAnlass = createAnlassWithArzt(sameOrgArzt, null);
        Anlass otherOrgAnlass = createAnlassWithArzt(otherOrgArzt, null);

        // ACT
        AnlassListJsonSpec result = api
            .anlassResourceFindAllAnlaesseBerechtigt()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(AnlassListJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result.getAnlassList())
            .anyMatch(a -> sameOrgAnlass.getId().getId().equals(a.getId()));
        Assertions.assertThat(result.getAnlassList())
            .noneMatch(a -> otherOrgAnlass.getId().getId().equals(a.getId()));
    }

    @Test
    @TestAsAdminBE
    void findAllAnlaesse_asSystemAdmin_shouldReturnAll() {
        // ARRANGE
        Benutzer arzt1 = getSchularztBE();
        Benutzer arzt2 = getAnotherSchularztBE();

        Anlass anlass1 = createAnlassWithArzt(arzt1, null);
        Anlass anlass2 = createAnlassWithArzt(arzt2, null);

        // ACT
        AnlassListJsonSpec result = api
            .anlassResourceFindAllAnlaesseBerechtigt()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(AnlassListJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result.getAnlassList())
            .anyMatch(a -> anlass1.getId().getId().equals(a.getId()));
        Assertions.assertThat(result.getAnlassList())
            .anyMatch(a -> anlass2.getId().getId().equals(a.getId()));
    }

    @Test
    @TestAsSchularztBE
    void findAnlaesseForDashboard_shouldOnlyReturnAnlaesseFromSameOrg() {
        // ARRANGE
        Benutzer sameOrgArzt = getSchularztBE();
        Benutzer otherOrgArzt = getAnotherSchularztBE();

        LocalDate futureDate = appClock.offsetDateTime().now().toLocalDate().plusMonths(1);
        AnlassConfig config = getAnlassConfig(AnlassTyp.KLASSE_8);

        Anlass sameOrgAnlass = anlassService.persist(
            AnlassBuilder.create(builder -> builder
                .withTitle("Dashboard Same Org")
                .withArzt(sameOrgArzt)
                .withDatumSetSchuljahr(futureDate)
                .withAnlassConfig(config))
        );

        Anlass otherOrgAnlass = anlassService.persist(
            AnlassBuilder.create(builder -> builder
                .withTitle("Dashboard Other Org")
                .withArzt(otherOrgArzt)
                .withDatumSetSchuljahr(futureDate)
                .withAnlassConfig(config))
        );

        // ACT
        AnlassListJsonSpec result = api
            .anlassResourceFindAnlaesseForDashboard()
            .benutzerIDPath(sameOrgArzt.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(AnlassListJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result.getTotalRecords()).isLessThan(configService.getDashboardAnlassLimit());
        Assertions.assertThat(result.getAnlassList())
            .anyMatch(a -> sameOrgAnlass.getId().getId().equals(a.getId()));
        Assertions.assertThat(result.getAnlassList())
            .noneMatch(a -> otherOrgAnlass.getId().getId().equals(a.getId()));
    }

    @Test
    @TestAsSchoolNurseBE
    void getAnlass_sameOrg_asSchoolNurse_shouldSucceed() {
        // ARRANGE
        Benutzer arzt = getSchularztBE();
        Anlass anlass = createAnlassWithArzt(arzt, null);

        // ACT
        AnlassJsonSpec result = api
            .anlassResourceGetAnlass()
            .anlassIdPath(anlass.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(AnlassJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isEqualTo(anlass.getId().getId());
    }

    @Test
    @TestAsSchoolNurseBE
    void getAnlass_noArzt_asSchoolNurse_shouldFail() {
        // ARRANGE
        UUID anlassID = UUID.randomUUID();
        createAnlassWithNoArzt(anlassID);

        // ACT & ASSERT
        api.anlassResourceGetAnlass()
            .anlassIdPath(anlassID.toString())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    @TestAsSchoolNurseBE
    void createAnlass_sameOrg_asSchoolNurse_shouldSucceed() {
        // ARRANGE
        Benutzer arzt = getSchularztBE();
        AnlassConfig config =
            anlassConfigService.getByAnlassTypAndStichtag(AnlassTyp.KLASSE_8, appClock.getToday().plusMonths(1));
        AnlassSaveJsonSpec saveAnlassJson = AnlassBuilder.createJaxSpec(builder -> builder
            .withAnlassTyp(AnlassTyp.KLASSE_8)
            .withAnlassConfig(config)
            .withArzt(arzt)
            .withTitle("Dezentral Create Anlass SchoolNurse")).getData();

        // ACT
        AnlassJsonSpec result = api
            .anlassResourceCreateAnlass()
            .body(saveAnlassJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().as(AnlassJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isNotNull();
        Assertions.assertThat(result.getData().getTitel()).isEqualTo("Dezentral Create Anlass SchoolNurse");
        Assertions.assertThat(result.getAnlassConfig().getData().getAnlassTyp()).isEqualTo(AnlassTypSpec.KLASSE_8);
    }

    @Test
    @TestAsSchoolNurseBE
    void updateAnlass_sameOrg_asSchoolNurse_shouldSucceed() {
        // ARRANGE
        Benutzer arzt = getSchularztBE();
        Anlass existingAnlass = createAnlassWithArzt(arzt, null);

        AnlassJsonSpec updatedAnlassJson = AnlassBuilder.createJaxSpec(builder -> builder
            .forAnlass(existingAnlass)
            .withTitle("Updated Dezentral Anlass SchoolNurse"));

        // ACT
        AnlassJsonSpec result = api
            .anlassResourceUpdateAnlass()
            .anlassIdPath(existingAnlass.getId().getId())
            .body(updatedAnlassJson.getData())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(AnlassJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isEqualTo(existingAnlass.getId().getId());
        Assertions.assertThat(result.getData().getTitel()).isEqualTo("Updated Dezentral Anlass SchoolNurse");
    }

    @Test
    @TestAsSchoolNurseBE
    void changeAnlassStatus_sameOrg_asSchoolNurse_shouldSucceed() {
        // ARRANGE
        Benutzer arzt = getSchularztBE();
        Anlass anlass = createAnlassWithArzt(arzt, arzt);
        Assertions.assertThat(anlass.getAnlassStatus()).isEqualTo(AnlassStatus.IN_PLANUNG);

        // ACT
        AnlassJsonSpec result = api
            .anlassResourceChangeAnlassStatus()
            .anlassIdPath(anlass.getId().getId())
            .statusPath(AnlassStatusSpec.DURCHGEFUEHRT)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(AnlassJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getData().getAnlassStatus()).isEqualTo(AnlassStatusSpec.DURCHGEFUEHRT);
    }

    @Test
    @TestAsSchoolNurseBE
    void removeAnlass_sameOrg_asSchoolNurse_shouldSucceed() {
        // ARRANGE
        Benutzer arzt = getSchularztBE();
        Anlass anlass = createAnlassWithArzt(arzt, null);

        // ACT & ASSERT
        api.anlassResourceRemoveAnlass()
            .anlassIdPath(anlass.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());
    }

    @Test
    @TestAsSchoolNurseBE
    void writeAnlass_abgeschlossen_asSchoolNurse_shouldFail() {
        // ARRANGE
        Benutzer arzt = getSchularztBE();
        Anlass anlass = createAnlassWithArztAndStatus(arzt, null, AnlassStatus.ABGESCHLOSSEN);

        AnlassJsonSpec updatedAnlassJson = AnlassBuilder.createJaxSpec(builder -> builder
            .forAnlass(anlass)
            .withTitle("Should Fail - Abgeschlossen SchoolNurse"));

        // ACT & ASSERT
        api.anlassResourceUpdateAnlass()
            .anlassIdPath(anlass.getId().getId())
            .body(updatedAnlassJson.getData())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    @TestAsSchoolNurseBE
    void readLaeusekontrolle_sameOrg_asSchoolNurse_shouldSucceed() {
        // ARRANGE
        Benutzer arzt = getSchularztBE();
        Anlass anlass = createLaeusekontrolleWithArzt(arzt);

        // ACT
        AnlassJsonSpec result = api
            .anlassResourceGetAnlass()
            .anlassIdPath(anlass.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(AnlassJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isEqualTo(anlass.getId().getId());
    }

    @Test
    @TestAsSchoolNurseBE
    void writeLaeusekontrolle_sameOrg_asSchoolNurse_shouldSucceed() {
        // ARRANGE
        Benutzer arzt = getSchularztBE();
        Anlass anlass = createLaeusekontrolleWithArzt(arzt);

        AnlassJsonSpec updatedAnlassJson = AnlassBuilder.createJaxSpec(builder -> builder
            .forAnlass(anlass)
            .withTitle("Updated Laeusekontrolle SchoolNurse"));

        // ACT
        AnlassJsonSpec result = api
            .anlassResourceUpdateAnlass()
            .anlassIdPath(anlass.getId().getId())
            .body(updatedAnlassJson.getData())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(AnlassJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getData().getTitel()).isEqualTo("Updated Laeusekontrolle SchoolNurse");
    }

    @Test
    @TestAsSchoolNurseBE
    void findAllAnlaesse_asSchoolNurse_shouldOnlyReturnAnlaesseFromSameOrg() {
        // ARRANGE
        Benutzer sameOrgArzt = getSchularztBE();
        Benutzer otherOrgArzt = getAnotherSchularztBE();

        Anlass sameOrgAnlass = createAnlassWithArzt(sameOrgArzt, null);
        Anlass otherOrgAnlass = createAnlassWithArzt(otherOrgArzt, null);

        // ACT
        AnlassListJsonSpec result = api
            .anlassResourceFindAllAnlaesseBerechtigt()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(AnlassListJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result.getAnlassList())
            .anyMatch(a -> sameOrgAnlass.getId().getId().equals(a.getId()));
        Assertions.assertThat(result.getAnlassList())
            .noneMatch(a -> otherOrgAnlass.getId().getId().equals(a.getId()));
    }

    @Test
    @TestAsSchoolNurseBE
    void findAnlaesseForDashboard_asSchoolNurse_shouldOnlyReturnAnlaesseFromSameOrg() {
        // ARRANGE
        Benutzer sameOrgArzt = getSchularztBE();
        Benutzer otherOrgArzt = getAnotherSchularztBE();

        LocalDate futureDate = appClock.offsetDateTime().now().toLocalDate().plusMonths(1);
        AnlassConfig config = getAnlassConfig(AnlassTyp.KLASSE_8);

        Anlass sameOrgAnlass = anlassService.persist(
            AnlassBuilder.create(builder -> builder
                .withTitle("Dashboard Same Org SchoolNurse")
                .withArzt(sameOrgArzt)
                .withDatumSetSchuljahr(futureDate)
                .withAnlassConfig(config))
        );

        Anlass otherOrgAnlass = anlassService.persist(
            AnlassBuilder.create(builder -> builder
                .withTitle("Dashboard Other Org SchoolNurse")
                .withArzt(otherOrgArzt)
                .withDatumSetSchuljahr(futureDate)
                .withAnlassConfig(config))
        );

        // ACT
        AnlassListJsonSpec result = api
            .anlassResourceFindAnlaesseForDashboard()
            .benutzerIDPath(sameOrgArzt.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(AnlassListJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result.getTotalRecords()).isLessThan(configService.getDashboardAnlassLimit());
        Assertions.assertThat(result.getAnlassList())
            .anyMatch(a -> sameOrgAnlass.getId().getId().equals(a.getId()));
        Assertions.assertThat(result.getAnlassList())
            .noneMatch(a -> otherOrgAnlass.getId().getId().equals(a.getId()));
    }

    @NonNull
    private AnlassConfig getAnlassConfig(@NonNull AnlassTyp anlassTyp) {
        return anlassConfigService.getByAnlassTypAndStichtag(
            anlassTyp,
            appClock.offsetDateTime().now().toLocalDate());
    }

    private void createAnlassWithNoArzt(@NonNull UUID anlassID) {
        // Anlass ohne Arzt kann nur direkt über SQL eingefügt werden, da sonst der Validator zuschlägt
        AnlassConfig config =
            anlassConfigService.getByAnlassTypAndStichtag(AnlassTyp.KLASSE_8, appClock.getToday().plusMonths(1));
        String arztID = null;
        String mpaID = null;
        String sql = String.format(
            "INSERT INTO anlass (id, version, timestamperstellt, timestampmutiert, usererstellt, usermutiert, datum, "
                + "von, bis, anlassconfig_id, arzt_id, mpa_id, weitereperson_id, titel, anlassstatus, anlassart, "
                + "schuljahr) "
                + "VALUES ('%s', 0, '2023-01-01 00:00:00+00', '2023-01-01 00:00:00+00', 'test', 'test', '2023-01-01',"
                + " '2023-01-01 11:30:00.000000', '2023-01-01 12:00:00.000000', '%s', %s, %s, null, 'Dummy Anlass', "
                + "'IN_PLANUNG', 'EINZEL_ANLASS', 2023)",
            anlassID,
            config.getId().getId(),
            arztID,
            mpaID
        );
        fixtureService.executeSql(sql);
    }

    @NonNull
    private Anlass createAnlassWithArzt(@NonNull Benutzer arzt, @Nullable Benutzer mpa) {
        return anlassService.persist(
            AnlassBuilder.create(builder -> builder
                .withTitle("Dezentral Test Anlass")
                .withArzt(arzt)
                .withMPA(mpa)
                .withAnlassConfig(getAnlassConfig(AnlassTyp.KLASSE_8)))
        );
    }

    @NonNull
    private Anlass createAnlassWithArztAndStatus(
        @Nullable Benutzer arzt,
        @Nullable Benutzer mpa,
        @NonNull AnlassStatus status
    ) {
        return anlassService.persist(
            AnlassBuilder.create(builder -> builder
                .withTitle("Dezentral Test Anlass")
                .withArzt(arzt)
                .withMPA(mpa)
                .withAnlassStatus(status)
                .withAnlassConfig(getAnlassConfig(AnlassTyp.KLASSE_8)))
        );
    }

    @NonNull
    private Anlass createLaeusekontrolleWithArzt(@Nullable Benutzer arzt) {
        return anlassService.persist(
            AnlassBuilder.create(builder -> builder
                .withTitle("Laeusekontrolle Anlass")
                .withArzt(arzt)
                .withAnlassConfig(getAnlassConfig(AnlassTyp.LAEUSEKONTROLLE)))
        );
    }

    @NonNull
    private Benutzer getSchularztBE() {
        return fixtureService.readBenutzerWithBerechtigungen(TestAsSchularztBE.BENUTZER_ID_ID);
    }

    @NonNull
    private Benutzer getAnotherSchularztBE() {
        return fixtureService.readBenutzerWithBerechtigungen(TestAsAnotherSchularztBE.BENUTZER_ID_ID);
    }

    @NonNull
    private Benutzer getSchoolNurseBE() {
        return fixtureService.readBenutzerWithBerechtigungen(TestAsSchoolNurseBE.BENUTZER_ID_ID);
    }
}
