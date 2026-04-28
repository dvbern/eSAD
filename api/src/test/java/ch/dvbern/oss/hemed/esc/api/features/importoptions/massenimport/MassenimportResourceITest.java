/*
 * Copyright (C) 2025 DV Bern AG, Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport;

import java.io.File;
import java.util.List;

import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.KindService;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.MassenimportApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AppValidationErrorJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.ViolationJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import io.restassured.response.ResponseOptions;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import static ch.dvbern.oss.hemed.esc.api.testutil.TestUtil.waitForConditionOrAssertFails;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
@RequiredArgsConstructor
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
class MassenimportResourceITest extends AbstractITest {

    private final MassenimportApiSpec api = MassenimportApiSpec.massenimport(RequestSpecUtil.quarkusSpec());
    private final FixtureService fixtureService;
    private final KindService kindService;
    // ARRANGE
    private static final String TOO_MANY_RETRIES_MESSAGE = "Async Operation took too long to complete";
    private static final String KEY_HINT = "hint";
    private static final String KEY_MESSAGE = "message";
    private static final String KEY_STATUS = "status";
    private static final String SUCCESS_VALUE_HINT = "An E-Mail will be sent with the results of the import";
    private static final String SUCCESS_VALUE_MESSAGE = "The file structure is valid, import started";
    private static final String SUCCESS_VALUE_STATUS = "in_progress";
    private static final String HEADER_CONTENT_DISPOSITION = "Content-Disposition";
    private static final String CONTENT_DISPOSITION = "attachment;filename=\"";
    private static final String CONTENT_TYPE_EXCEL =
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String CONTENT_TYPE_JSON = "application/json;charset=UTF-8";
    private static final String STANDARD_VORLAGE = "massenimport_standard_vorlage.xlsx";
    private static final String KLASSEN_VORLAGE = "massenimport_klassen_vorlage.xlsx";

    @Order(10)
    @Test
    @TestAsAdminBS
    void downloadStandardTemplate_returnsXlsxWithExpectedFilename() {
        // ACT
        Response response = api.massenimportResourceDownloadStandardTemplate()
            .execute(ResponseOptions::andReturn)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(CONTENT_TYPE_EXCEL)
            .extract()
            .response();

        // ASSERT
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getHeaders().get(HEADER_CONTENT_DISPOSITION).getValue())
            .isEqualTo(CONTENT_DISPOSITION + STANDARD_VORLAGE + '"');
    }

    @Order(20)
    @Test
    @TestAsAdminBS
    void downloadKlassenTemplate_returnsXlsxWithExpectedFilename() {
        // ACT
        Response response = api.massenimportResourceDownloadKlassenTemplate()
            .execute(ResponseOptions::andReturn)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(CONTENT_TYPE_EXCEL)
            .extract()
            .response();

        // ASSERT
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getHeaders().get(HEADER_CONTENT_DISPOSITION).getValue())
            .isEqualTo(CONTENT_DISPOSITION + KLASSEN_VORLAGE + '"');
    }

    @Order(30)
    @Test
    @TestAsAdminBS
    void massenimportKinderForSchuleAndKlasse_upload_wrong_template_fails() {
        // ARRANGE
        Schule schule = fixtureService.createSchule("Schulhaus");
        Klasse klasse = fixtureService.createKlasse("Klasse 1", schule);
        File file = TestUtil.loadTestfile("/Massenimport/massenimport_klassen.xlsx");

        // ACT & ASSERT
        var response = api.massenimportResourceMassenimportKinderForSchuleAndKlasse()
            ._fileMultiPart(file)
            .schuleIDPath(schule.getId().getId())
            .klasseIDPath(klasse.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract()
            .as(AppValidationErrorJsonSpec.class);
        assertThat(response.getViolations())
            .containsExactlyInAnyOrder(
                new ViolationJsonSpec(
                    "",
                    "InvalidSturctureInMassenuploadFile",
                    "Das Hochgeladene File hat nicht die erwartete Struktur. Bitte benutzen Sie das bereitgestellte "
                        + "Vorlagenfile.")
            );

    }

    @Order(40)
    @Test
    @TestAsAdminBS
    void massenimportKinderForSchuleExtractKlassen_upload_wrong_template_fails() {
        // ARRANGE
        Schule schule = fixtureService.createSchule("Schulhaus");
        File file = TestUtil.loadTestfile("/Massenimport/massenimport_standard.xlsx");

        // ACT & ASSERT
        var response = api.massenimportResourceMassenimportKinderForSchuleExtractKlassen()
            ._fileMultiPart(file)
            .schuleIDPath(schule.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract()
            .as(AppValidationErrorJsonSpec.class);
        assertThat(response.getViolations())
            .containsExactlyInAnyOrder(
                new ViolationJsonSpec(
                    "",
                    "InvalidSturctureInMassenuploadFile",
                    "Das Hochgeladene File hat nicht die erwartete Struktur. Bitte benutzen Sie das bereitgestellte "
                        + "Vorlagenfile.")
            );
    }

    @Order(50)
    @Test
    @TestAsAdminBS
    void massenimportKinderForSchuleAndKlasse() throws InterruptedException {
        // ARRANGE
        File file = TestUtil.loadTestfile("/Massenimport/massenimport_standard.xlsx");

        int numberOfKinderImported = 10;

        Schule schule = fixtureService.createSchule("Standard Massenimport-Schulhaus");
        Klasse klasse = fixtureService.createKlasse("Standard Massenimport-Klasse", schule);

        List<Kind> listSchule = kindService.findBySchule(schule.getId());
        List<Kind> listKlasse = kindService.findByKlasse(klasse.getId());

        // ACT
        api.massenimportResourceMassenimportKinderForSchuleAndKlasse()
            ._fileMultiPart(file)
            .schuleIDPath(schule.getId().getId())
            .klasseIDPath(klasse.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(CONTENT_TYPE_JSON)
            .body(KEY_HINT, equalTo(SUCCESS_VALUE_HINT))
            .body(KEY_MESSAGE, equalTo(SUCCESS_VALUE_MESSAGE))
            .body(KEY_STATUS, equalTo(SUCCESS_VALUE_STATUS))
            .extract()
            .response();

        // ASSERT
        // We retry up to 100 Times to ensure async operation finished
        waitForConditionOrAssertFails(
            () -> kindService.findBySchule(schule.getId()).size() == listSchule.size() + numberOfKinderImported
                && kindService.findByKlasse(klasse.getId()).size() == listKlasse.size() + numberOfKinderImported,
            20,
            100,
            TOO_MANY_RETRIES_MESSAGE
        );

        Assertions.assertThat(listSchule).isEmpty();
        Assertions.assertThat(listKlasse).isEmpty();

        List<Kind> updatedListSchule = kindService.findBySchule(schule.getId());
        List<Kind> updatedListKlasse = kindService.findByKlasse(klasse.getId());

        Assertions.assertThat(updatedListSchule)
            .isNotNull()
            .size()
            .isEqualTo(listSchule.size() + numberOfKinderImported);
        Assertions.assertThat(updatedListKlasse)
            .isNotNull()
            .size()
            .isEqualTo(listKlasse.size() + numberOfKinderImported);

        updatedListSchule.forEach(kind -> {
            Assertions.assertThat(kind.getSchule()).isNotNull();
            Assertions.assertThat(kind.getSchule().getSchulhaus()).isEqualTo(schule.getSchulhaus());
            Assertions.assertThat(kind.getKlasse()).isNotNull();
            Assertions.assertThat(kind.getKlasse().getSchule().getSchulhaus()).isEqualTo(schule.getSchulhaus());
            Assertions.assertThat(kind.getKlasse().getBezeichnung()).isEqualTo(klasse.getBezeichnung());
        });
    }

    @Order(60)
    @Test
    @TestAsAdminBS
    void massenimportKinderForSchuleExtractKlassen() throws InterruptedException {
        // ARRANGE
        File file = TestUtil.loadTestfile("/Massenimport/massenimport_klassen.xlsx");
        int numberOfKinderImported = 10;
        int numberOfKinderInKlasse1 = 3;
        int numberOfKinderInKlasse2 = 3;
        int numberOfKinderInKlasse3 = 3;
        int numberOfKinderInNoKlasse = 1;

        Schule schule = fixtureService.createSchule("Klassen Massenimport-Schulhaus");
        Klasse klasse1 = fixtureService.createKlasse("Klasse 1", schule);
        Klasse klasse2 = fixtureService.createKlasse("Klasse 2", schule);
        Klasse klasse3 = fixtureService.createKlasse("Klasse 3", schule);

        List<Kind> listSchule = kindService.findBySchule(schule.getId());
        List<Kind> listKlasse1 = kindService.findByKlasse(klasse1.getId());
        List<Kind> listKlasse2 = kindService.findByKlasse(klasse2.getId());
        List<Kind> listKlasse3 = kindService.findByKlasse(klasse3.getId());

        // ACT
        api.massenimportResourceMassenimportKinderForSchuleExtractKlassen()
            ._fileMultiPart(file)
            .schuleIDPath(schule.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(CONTENT_TYPE_JSON)
            .body(KEY_HINT, equalTo(SUCCESS_VALUE_HINT))
            .body(KEY_MESSAGE, equalTo(SUCCESS_VALUE_MESSAGE))
            .body(KEY_STATUS, equalTo(SUCCESS_VALUE_STATUS))
            .extract()
            .response();

        // ASSERT
        // We retry up to 100 Times to ensure async operation finished
        waitForConditionOrAssertFails(
            () -> kindService.findBySchule(schule.getId()).size() == listSchule.size() + numberOfKinderImported
                && kindService.findByKlasse(klasse1.getId()).size() == listKlasse1.size() + numberOfKinderInKlasse1
                && kindService.findByKlasse(klasse2.getId()).size() == listKlasse2.size() + numberOfKinderInKlasse2
                && kindService.findByKlasse(klasse3.getId()).size() == listKlasse3.size() + numberOfKinderInKlasse3,
            20,
            100,
            TOO_MANY_RETRIES_MESSAGE
        );

        Assertions.assertThat(listSchule).isEmpty();
        Assertions.assertThat(listKlasse1).isEmpty();
        Assertions.assertThat(listKlasse2).isEmpty();
        Assertions.assertThat(listKlasse3).isEmpty();

        List<Kind> updatedListSchule = kindService.findBySchule(schule.getId());
        List<Kind> updatedListKlasse1 = kindService.findByKlasse(klasse1.getId());
        List<Kind> updatedListKlasse2 = kindService.findByKlasse(klasse2.getId());
        List<Kind> updatedListKlasse3 = kindService.findByKlasse(klasse3.getId());

        Assertions.assertThat(updatedListSchule)
            .isNotNull()
            .size()
            .isEqualTo(listSchule.size() + numberOfKinderImported);
        Assertions.assertThat(updatedListKlasse1)
            .isNotNull()
            .size()
            .isEqualTo(listKlasse1.size() + numberOfKinderInKlasse1);
        Assertions.assertThat(updatedListKlasse2)
            .isNotNull()
            .size()
            .isEqualTo(listKlasse2.size() + numberOfKinderInKlasse2);
        Assertions.assertThat(updatedListKlasse3)
            .isNotNull()
            .size()
            .isEqualTo(listKlasse3.size() + numberOfKinderInKlasse3);
        Assertions.assertThat(updatedListSchule.size()
            - updatedListKlasse1.size()
            - updatedListKlasse2.size()
            - updatedListKlasse3.size()).isEqualTo(numberOfKinderInNoKlasse);
        updatedListSchule.forEach(kind -> {
            Assertions.assertThat(kind.getSchule()).isNotNull();
            Assertions.assertThat(kind.getSchule().getSchulhaus()).isEqualTo(schule.getSchulhaus());
        });
    }

    @Order(70)
    @Test
    @TestAsAdminBS
    void massenimportKinderForSchuleAndKlasse_noKinderPersistedIfDuplikat() throws InterruptedException {
        // ARRANGE
        File file = TestUtil.loadTestfile("/Massenimport/massenimport_standard_duplikat.xlsx");

        int numberOfKinderImportedFirstTry = 1;
        int numberOfKinderImportedSecondTry = 0;

        Schule schule = fixtureService.createSchule("Duplikat Standard Massenimport-Schulhaus");
        Klasse klasse = fixtureService.createKlasse("Duplikat Standard Massenimport-Klasse", schule);

        List<Kind> listSchule = kindService.findBySchule(schule.getId());
        List<Kind> listKlasse = kindService.findByKlasse(klasse.getId());

        // ACT - First Import
        api.massenimportResourceMassenimportKinderForSchuleAndKlasse()
            ._fileMultiPart(file)
            .schuleIDPath(schule.getId().getId())
            .klasseIDPath(klasse.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(CONTENT_TYPE_JSON)
            .body(KEY_HINT, equalTo(SUCCESS_VALUE_HINT))
            .body(KEY_MESSAGE, equalTo(SUCCESS_VALUE_MESSAGE))
            .body(KEY_STATUS, equalTo(SUCCESS_VALUE_STATUS))
            .extract()
            .response();

        // ASSERT - First Import completed successfully
        // We retry up to 100 Times to ensure async operation finished
        waitForConditionOrAssertFails(
            () -> kindService.findBySchule(schule.getId()).size() == listSchule.size() + numberOfKinderImportedFirstTry
                && kindService.findByKlasse(klasse.getId()).size()
                == listKlasse.size() + numberOfKinderImportedFirstTry,
            20,
            100,
            TOO_MANY_RETRIES_MESSAGE
        );

        Assertions.assertThat(listSchule).isEmpty();
        Assertions.assertThat(listKlasse).isEmpty();

        List<Kind> firstUpdatedListSchule = kindService.findBySchule(schule.getId());
        List<Kind> firstUpdatedListKlasse = kindService.findByKlasse(klasse.getId());

        Assertions.assertThat(firstUpdatedListSchule)
            .isNotNull()
            .size()
            .isEqualTo(listSchule.size() + numberOfKinderImportedFirstTry);
        Assertions.assertThat(firstUpdatedListKlasse)
            .isNotNull()
            .size()
            .isEqualTo(listKlasse.size() + numberOfKinderImportedFirstTry);

        firstUpdatedListSchule.forEach(kind -> {
            Assertions.assertThat(kind.getSchule()).isNotNull();
            Assertions.assertThat(kind.getSchule().getSchulhaus()).isEqualTo(schule.getSchulhaus());
            Assertions.assertThat(kind.getKlasse()).isNotNull();
            Assertions.assertThat(kind.getKlasse().getSchule().getSchulhaus()).isEqualTo(schule.getSchulhaus());
            Assertions.assertThat(kind.getKlasse().getBezeichnung()).isEqualTo(klasse.getBezeichnung());
        });

        // ACT 2 - Second Import should start but not persist
        api.massenimportResourceMassenimportKinderForSchuleAndKlasse()
            ._fileMultiPart(file)
            .schuleIDPath(schule.getId().getId())
            .klasseIDPath(klasse.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(CONTENT_TYPE_JSON)
            .body(KEY_HINT, equalTo(SUCCESS_VALUE_HINT))
            .body(KEY_MESSAGE, equalTo(SUCCESS_VALUE_MESSAGE))
            .body(KEY_STATUS, equalTo(SUCCESS_VALUE_STATUS))
            .extract()
            .response();

        // ASSERT - Second Import didn't persist any new Kinder because Kind already exists
        // We can't ensure the async operation finished, because in this specific test the before and after should
        // stay the same - difficult to assert, as we can't assert the log or mailbox
        waitForConditionOrAssertFails(
            () -> kindService.findBySchule(schule.getId()).size()
                == firstUpdatedListSchule.size() + numberOfKinderImportedSecondTry
                && kindService.findByKlasse(klasse.getId()).size()
                == firstUpdatedListKlasse.size() + numberOfKinderImportedSecondTry,
            20,
            100,
            TOO_MANY_RETRIES_MESSAGE
        );

        List<Kind> secondUpdatedListSchule = kindService.findBySchule(schule.getId());
        List<Kind> secondUpdatedListKlasse = kindService.findByKlasse(klasse.getId());

        Assertions.assertThat(secondUpdatedListSchule)
            .isNotNull()
            .size()
            .isEqualTo(listSchule.size() + numberOfKinderImportedFirstTry)
            .isEqualTo(firstUpdatedListSchule.size() + numberOfKinderImportedSecondTry);
        Assertions.assertThat(secondUpdatedListKlasse)
            .isNotNull()
            .size()
            .isEqualTo(listKlasse.size() + numberOfKinderImportedFirstTry)
            .isEqualTo(firstUpdatedListKlasse.size() + numberOfKinderImportedSecondTry);
    }

    @Order(80)
    @Test
    @TestAsAdminBS
    void massenimportKinderForSchuleExtractKlasse_noKinderPersistedIfDuplikat() throws InterruptedException {
        // ARRANGE
        File file = TestUtil.loadTestfile("/Massenimport/massenimport_klassen_duplikat.xlsx");
        int numberOfKinderImportedFirstTry = 1;
        int numberOfKinderImportedSecondTry = 0;

        Schule schule = fixtureService.createSchule("Duplikat Klassen Massenimport-Schulhaus");
        Klasse klasse = fixtureService.createKlasse("Klasse 1", schule);

        List<Kind> listSchule = kindService.findBySchule(schule.getId());
        List<Kind> listKlasse = kindService.findByKlasse(klasse.getId());

        // ACT 1
        api.massenimportResourceMassenimportKinderForSchuleExtractKlassen()
            ._fileMultiPart(file)
            .schuleIDPath(schule.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(CONTENT_TYPE_JSON)
            .body(KEY_HINT, equalTo(SUCCESS_VALUE_HINT))
            .body(KEY_MESSAGE, equalTo(SUCCESS_VALUE_MESSAGE))
            .body(KEY_STATUS, equalTo(SUCCESS_VALUE_STATUS))
            .extract()
            .response();

        // ASSERT
        // We retry up to 100 Times to ensure async operation finished
        waitForConditionOrAssertFails(
            () -> kindService.findBySchule(schule.getId()).size() == listSchule.size() + numberOfKinderImportedFirstTry
                && kindService.findByKlasse(klasse.getId()).size()
                == listKlasse.size() + numberOfKinderImportedFirstTry,
            20,
            100,
            TOO_MANY_RETRIES_MESSAGE
        );

        Assertions.assertThat(listSchule).isEmpty();
        Assertions.assertThat(listKlasse).isEmpty();

        List<Kind> firstUpdatedListSchule = kindService.findBySchule(schule.getId());
        List<Kind> firstUpdatedListKlasse = kindService.findByKlasse(klasse.getId());

        Assertions.assertThat(firstUpdatedListSchule)
            .isNotNull()
            .size()
            .isEqualTo(listSchule.size() + numberOfKinderImportedFirstTry);
        Assertions.assertThat(firstUpdatedListKlasse)
            .isNotNull()
            .size()
            .isEqualTo(listKlasse.size() + numberOfKinderImportedFirstTry);
        firstUpdatedListSchule.forEach(kind -> {
            Assertions.assertThat(kind.getSchule()).isNotNull();
            Assertions.assertThat(kind.getSchule().getSchulhaus()).isEqualTo(schule.getSchulhaus());
        });

        // ACT - Second Import should start but not persist
        api.massenimportResourceMassenimportKinderForSchuleExtractKlassen()
            ._fileMultiPart(file)
            .schuleIDPath(schule.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .contentType(CONTENT_TYPE_JSON)
            .body(KEY_HINT, equalTo(SUCCESS_VALUE_HINT))
            .body(KEY_MESSAGE, equalTo(SUCCESS_VALUE_MESSAGE))
            .body(KEY_STATUS, equalTo(SUCCESS_VALUE_STATUS))
            .extract()
            .response();

        // ASSERT - Second Import didn't persist any new Kinder because Kind already exists
        // We can't ensure the async operation finished, because in this specific test the before and after should
        // stay the same - difficult to assert, as we can't assert the log or mailbox
        waitForConditionOrAssertFails(
            () -> kindService.findBySchule(schule.getId()).size()
                == firstUpdatedListSchule.size() + numberOfKinderImportedSecondTry
                && kindService.findByKlasse(klasse.getId()).size()
                == firstUpdatedListKlasse.size() + numberOfKinderImportedSecondTry,
            20,
            100,
            TOO_MANY_RETRIES_MESSAGE
        );

        List<Kind> secondUpdatedListSchule = kindService.findBySchule(schule.getId());
        List<Kind> secondUpdatedListKlasse = kindService.findByKlasse(klasse.getId());

        Assertions.assertThat(secondUpdatedListSchule)
            .isNotNull()
            .size()
            .isEqualTo(listSchule.size() + numberOfKinderImportedFirstTry)
            .isEqualTo(firstUpdatedListSchule.size() + numberOfKinderImportedSecondTry);
        Assertions.assertThat(secondUpdatedListKlasse)
            .isNotNull()
            .size()
            .isEqualTo(listKlasse.size() + numberOfKinderImportedFirstTry)
            .isEqualTo(firstUpdatedListKlasse.size() + numberOfKinderImportedSecondTry);

    }
}
