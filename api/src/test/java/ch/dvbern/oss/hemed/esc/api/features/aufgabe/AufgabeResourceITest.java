package ch.dvbern.oss.hemed.esc.api.features.aufgabe;

import java.io.File;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.KindService;
import ch.dvbern.oss.hemed.esc.api.features.labels.Label;
import ch.dvbern.oss.hemed.esc.api.features.labels.LabelService;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.Aufgabe;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.AufgabeService;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.json.NumOfOffeneFaelligeAufgabeJson;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.types.AufgabeStatus;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileStoreService;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.AufgabeBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.BenutzerBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.KindBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.LabelBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.AufgabeApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AppValidationErrorJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AufgabeJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AufgabeListJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AufgabeSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AufgabeStatusSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.FileDataJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.ViolationJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import io.restassured.response.ResponseOptions;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
@RequiredArgsConstructor
class AufgabeResourceITest extends AbstractITest {

    private final AufgabeApiSpec api = AufgabeApiSpec.aufgabe(RequestSpecUtil.quarkusSpec());

    private final AufgabeService aufgabeService;
    private final BenutzerService benutzerService;
    private final KindService kindService;
    private final LabelService labelService;
    private final FixtureService fixtureService;
    private final FileStoreService fileStoreService;
    private final AppClock appClock;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    @TestAsAdminBS
    void findAllAufgaben() {
        //ARRANGE
        Benutzer defaultEmpfaenger = BenutzerBuilder.defaultBenutzer();
        benutzerService.persist(defaultEmpfaenger);

        Aufgabe aufgabe = AufgabeBuilder.create(builder -> builder
            .withTitle(TestUtil.getRandomString("Test Aufgabe Titel"))
            .withEmpfaenger(defaultEmpfaenger)
            .withUserErfasst(defaultEmpfaenger)
            .withStatus(AufgabeStatus.ABGESCHLOSSEN)
        );

        aufgabeService.persist(aufgabe);

        //ACT
        AufgabeListJsonSpec result = api.aufgabeResourceFindAllAufgaben()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Response.Status.OK.getStatusCode())
            .extract()
            .as(AufgabeListJsonSpec.class);

        //ASSERT
        assertThat(result.getAufgaben()).isNotEmpty()
            .anyMatch(aufgabeJsonSpec ->
                aufgabe.getId().getId().equals(aufgabeJsonSpec.getId())
                    && aufgabe.getTitel().equals(aufgabeJsonSpec.getData().getTitel()));
        assertThat(result.getNumOfOffeneFaelligeAufgaben()).isNotNull();
    }

    @Test
    @TestAsAdminBS
    void findNumOfFaelligeAufgaben() {
        //ARRANGE
        Benutzer defaultEmpfaenger = BenutzerBuilder.defaultBenutzer();
        benutzerService.persist(defaultEmpfaenger);
        int numOfFaelligeAufgabenBefore = aufgabeService.getNumOfOffeneFaelligeAufgaben();

        Aufgabe aufgabe1 = AufgabeBuilder.create(builder -> builder
            .withTitle(TestUtil.getRandomString("Abgeschlossene Aufgabe Titel"))
            .withEmpfaenger(defaultEmpfaenger)
            .withUserErfasst(defaultEmpfaenger)
            .withFaelligDatum(getAppClock().localDateTime().now().toLocalDate().minusDays(1))
            .withStatus(AufgabeStatus.ABGESCHLOSSEN)
        );
        aufgabeService.persist(aufgabe1);

        Aufgabe aufgabe2 = AufgabeBuilder.create(
            builder -> builder
                .withTitle(TestUtil.getRandomString("In Bearbeitung today Aufgabe Titel"))
                .withEmpfaenger(defaultEmpfaenger)
                .withUserErfasst(defaultEmpfaenger)
                .withFaelligDatum(getAppClock().localDateTime().now().toLocalDate())
                .withStatus(AufgabeStatus.IN_BEARBEITUNG)
        );
        aufgabeService.persist(aufgabe2);

        Aufgabe aufgabe3 = AufgabeBuilder.create(builder -> builder
            .withTitle(TestUtil.getRandomString("Not yet faellige Aufgabe Titel"))
            .withEmpfaenger(defaultEmpfaenger)
            .withUserErfasst(defaultEmpfaenger)
            .withFaelligDatum(getAppClock().localDateTime().now().toLocalDate().plusDays(1))
            .withStatus(AufgabeStatus.OFFEN)
        );
        aufgabeService.persist(aufgabe3);

        //ACT
        NumOfOffeneFaelligeAufgabeJson result = api.aufgabeResourceGetNumOfFaelligeAufgaben()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Response.Status.OK.getStatusCode())
            .extract()
            .as(NumOfOffeneFaelligeAufgabeJson.class);

        //ASSERT
        assertThat(result.getNumOfOffeneFaelligeAufgaben()).isEqualTo(numOfFaelligeAufgabenBefore + 1);
    }

    @Test
    @TestAsAdminBS
    void getAufgabeForKind() {
        // ARRANGE
        Kind kind = fixtureService.createKind();
        Benutzer benutzer = fixtureService.createBenutzer();
        LocalDate faelligBis = appClock.offsetDateTime().now().toLocalDate().minusDays(1);

        Aufgabe aufgabe = AufgabeBuilder.create(builder -> builder
            .withStatus(AufgabeStatus.ABGESCHLOSSEN)
            .withEmpfaenger(benutzer)
            .withUserErfasst(benutzer)
            .withFaelligDatum(faelligBis)
            .withKind(kind)
            .withTitle("Meine Aufgabe für Kinder"));
        aufgabeService.persist(aufgabe);

        // ACT
        AufgabeListJsonSpec result = api
            .aufgabeResourceFindAllForKind()
            .kindIdPath(kind.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(AufgabeListJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result.getAufgaben())
            .isNotNull()
            .anyMatch(aufgabeJsonSpec ->
                aufgabe.getId().getId().equals(aufgabeJsonSpec.getId())
                    && aufgabe.getKind() != null
                    && aufgabe.getKind().getId().getId().equals(kind.getId().getId())
                    && aufgabe.getTitel().equals(aufgabeJsonSpec.getData().getTitel()));
    }

    @Test
    @TestAsAdminBS
    void getAufgabe() {
        // ARRANGE
        Aufgabe aufgabe = fixtureService.createAufgabe();

        // ACT
        AufgabeJsonSpec result = api
            .aufgabeResourceGetAufgabe()
            .aufgabeIdPath(aufgabe.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(AufgabeJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isEqualTo(aufgabe.getId().getId());
        Assertions.assertThat(result.getData().getTitel()).isEqualTo(aufgabe.getTitel());
    }

    @Test
    @TestAsAdminBS
    void findNumOfFaelligeAufgabenForCurrentUser() {
        // ARRANGE
        var adminId = ID.of(UUID.fromString(TestAsAdminBS.BENUTZER_ID), Benutzer.class);
        var currentBenutzer = benutzerService.getById(adminId);
        Benutzer someOtherUser = BenutzerBuilder.defaultBenutzer();
        benutzerService.persist(someOtherUser);
        var numOfOffeneFaelligeAufgabenByCurrentUser = aufgabeService.getNumOfOffeneFaelligeAufgabenByCurrentUser();
        var numOfFaelligeTotal = aufgabeService.getNumOfOffeneFaelligeAufgaben();

        Aufgabe aufgabe1 = AufgabeBuilder.create(builder -> builder
            .withTitle(TestUtil.getRandomString("My faellige Aufgabe Titel"))
            .withEmpfaenger(currentBenutzer)
            .withUserErfasst(someOtherUser)
            .withFaelligDatum(getAppClock().localDateTime().now().toLocalDate().minusDays(1))
            .withStatus(AufgabeStatus.OFFEN)
        );
        aufgabeService.persist(aufgabe1);

        Aufgabe aufgabe2 = AufgabeBuilder.create(
            builder -> builder
                .withTitle(TestUtil.getRandomString("Sommeone else's faellige Aufgabe Titel"))
                .withEmpfaenger(someOtherUser)
                .withUserErfasst(currentBenutzer)
                .withFaelligDatum(getAppClock().localDateTime().now().toLocalDate().minusDays(1))
                .withStatus(AufgabeStatus.OFFEN)
        );
        aufgabeService.persist(aufgabe2);

        // ACT
        var resForMe = api.aufgabeResourceGetNumOfFaelligeAufgabenForCurrentUser()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Response.Status.OK.getStatusCode())
            .extract()
            .as(NumOfOffeneFaelligeAufgabeJson.class);

        var restTotal = api.aufgabeResourceGetNumOfFaelligeAufgaben()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Response.Status.OK.getStatusCode())
            .extract()
            .as(NumOfOffeneFaelligeAufgabeJson.class);

        // ASSERT
        assertThat(resForMe.getNumOfOffeneFaelligeAufgaben()).isEqualTo(numOfOffeneFaelligeAufgabenByCurrentUser + 1);
        assertThat(restTotal.getNumOfOffeneFaelligeAufgaben()).isEqualTo(numOfFaelligeTotal + 2);
        assertThat(resForMe.getNumOfOffeneFaelligeAufgaben()).isLessThan(aufgabeService.getNumOfOffeneFaelligeAufgaben());
    }

    @Test
    @TestAsAdminBS
    void createAufgabe() {
        // ARRANGE
        Benutzer defaultEmpfaenger = BenutzerBuilder.defaultBenutzer();
        benutzerService.persist(defaultEmpfaenger);
        Benutzer userErfasst = benutzerService.getCurrentBenutzerOrThrowException();

        LocalDate faelligBis = LocalDate.of(2025, 5, 30);

        AufgabeSaveJsonSpec saveJson = AufgabeBuilder.createJaxSpec(builder -> builder
            .withStatus(AufgabeStatus.OFFEN)
            .withEmpfaenger(defaultEmpfaenger)
            .withUserErfasst(userErfasst)
            .withFaelligDatum(faelligBis)
            .withTitle("Meine Aufgabe")).getData();

        // ACT
        AufgabeJsonSpec result = api
            .aufgabeResourceCreateAufgabe()
            .body(saveJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().as(AufgabeJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isNotNull();
        Assertions.assertThat(result.getData().getTitel()).isEqualTo("Meine Aufgabe");
        Assertions.assertThat(result.getData().getEmpfaenger().getId()).isEqualTo(defaultEmpfaenger.getId().getId());
        Assertions.assertThat(result.getData().getUserErfasst().getId()).isEqualTo(userErfasst.getId().getId());
        Assertions.assertThat(result.getData().getFaelligBis()).isEqualTo(faelligBis);
    }

    @Test
    @TestAsAdminBS
    void createAufgabe_withWrongUserErfasst_UserErfasstWillBeCurrentUser() {
        // ARRANGE
        Benutzer defaultEmpfaenger = BenutzerBuilder.defaultBenutzer();
        benutzerService.persist(defaultEmpfaenger);
        Benutzer defaultUserErfasst = BenutzerBuilder.defaultBenutzer();
        benutzerService.persist(defaultUserErfasst);
        Benutzer actualUserErfasst = benutzerService.getCurrentBenutzerOrThrowException();

        LocalDate faelligBis = LocalDate.of(2025, 5, 30);

        AufgabeSaveJsonSpec saveJson = AufgabeBuilder.createJaxSpec(builder -> builder
            .withStatus(AufgabeStatus.OFFEN)
            .withEmpfaenger(defaultEmpfaenger)
            .withUserErfasst(defaultUserErfasst)
            .withFaelligDatum(faelligBis)
            .withTitle("Meine Aufgabe")).getData();

        // ACT
        AufgabeJsonSpec result = api
            .aufgabeResourceCreateAufgabe()
            .body(saveJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().as(AufgabeJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isNotNull();
        Assertions.assertThat(result.getData().getTitel()).isEqualTo("Meine Aufgabe");
        Assertions.assertThat(result.getData().getEmpfaenger().getId()).isEqualTo(defaultEmpfaenger.getId().getId());
        Assertions.assertThat(result.getData().getUserErfasst().getId())
            .isNotEqualTo(defaultUserErfasst.getId().getId());
        Assertions.assertThat(result.getData().getUserErfasst().getId()).isEqualTo(actualUserErfasst.getId().getId());
        Assertions.assertThat(result.getData().getFaelligBis()).isEqualTo(faelligBis);
    }

    @Test
    @TestAsAdminBS
    void createAufgabe_withInactiveKind_shouldThrow() {
        // ARRANGE
        Benutzer defaultEmpfaenger = BenutzerBuilder.defaultBenutzer();
        benutzerService.persist(defaultEmpfaenger);
        Benutzer defaultUserErfasst = BenutzerBuilder.defaultBenutzer();
        benutzerService.persist(defaultUserErfasst);
        Kind kind = fixtureService.createInactiveKind();

        LocalDate faelligBis = LocalDate.of(2025, 5, 30);

        AufgabeSaveJsonSpec saveJson = AufgabeBuilder.createJaxSpec(builder -> builder
            .withStatus(AufgabeStatus.OFFEN)
            .withKind(kind)
            .withEmpfaenger(defaultEmpfaenger)
            .withUserErfasst(defaultUserErfasst)
            .withFaelligDatum(faelligBis)
            .withTitle("Meine Aufgabe")).getData();

        String expectedValidationErrorMessage = String.format(
            "Das Kind %s %s ist inaktiv. Es kann keine Aufgabe für ein inaktives Kind erstellt werden.",
            kind.getNameVorname(),
            kind.getId());

        // ACT
        AppValidationErrorJsonSpec validationError = api.aufgabeResourceCreateAufgabe()
            .body(saveJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract()
            .as(AppValidationErrorJsonSpec.class);

        assertThat(validationError.getViolations()).hasSize(1);
        assertThat(validationError.getViolations().getFirst().getKey())
            .isEqualTo("CannotCreateEntityForInactiveKind");
        assertThat(validationError.getViolations().getFirst().getMessage())
            .isEqualTo(expectedValidationErrorMessage);
    }

    @Test
    @TestAsAdminBS
    void createAufgabe_with_optional_data() {
        // ARRANGE
        Benutzer defaultEmpfaenger = BenutzerBuilder.defaultBenutzer();
        benutzerService.persist(defaultEmpfaenger);
        Benutzer defaultUserErfasst = BenutzerBuilder.defaultBenutzer();
        benutzerService.persist(defaultUserErfasst);

        Kind kind = KindBuilder.defaultKind();
        kindService.persistWithoutDuplicateCheck(kind);

        Label label = LabelBuilder.defaultLabel();
        labelService.persist(label);

        LocalDate faelligBis = LocalDate.of(2025, 5, 30);

        AufgabeSaveJsonSpec saveJson = AufgabeBuilder.createJaxSpec(builder -> builder
            .withStatus(AufgabeStatus.OFFEN)
            .withEmpfaenger(defaultEmpfaenger)
            .withUserErfasst(defaultUserErfasst)
            .withFaelligDatum(faelligBis)
            .withTitle(TestUtil.getRandomString("Testaufgabe with Kind and Label"))
            .withKind(kind)
            .withBemerkung("Meine Bemerkung")
            .withLabel(Collections.singleton(label))
        ).getData();

        // ACT
        AufgabeJsonSpec result = api
            .aufgabeResourceCreateAufgabe()
            .body(saveJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().as(AufgabeJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isNotNull();
        Assertions.assertThat(result.getData().getBemerkungen()).isEqualTo("Meine Bemerkung");
        Assertions.assertThat(result.getData().getTitel()).startsWith("Testaufgabe with Kind and Label");
        Assertions.assertThat(result.getData().getKind() != null ? result.getData().getKind().getId() : null)
            .isEqualTo(kind.getId().getId());
        Assertions.assertThat(result.getData().getLabels())
            .isNotEmpty()
            .hasSize(1);
    }

    @Test
    @TestAsAdminBS
    void updateAufgabe() {
        // ARRANGE
        Aufgabe existingAufgabe = fixtureService.createAufgabe();
        Benutzer defaultEmpfaenger = benutzerService.persist(BenutzerBuilder.defaultBenutzer());
        Benutzer userErfasst = benutzerService.getCurrentBenutzerOrThrowException();

        String newTitle = TestUtil.getRandomString("Neuer Titel");
        String newBem = TestUtil.getRandomString("Neue Bemerkung");

        AufgabeJsonSpec updatedAufgabeJson = AufgabeBuilder.createJaxSpec(builder -> builder
            .forAufgabe(existingAufgabe)
            .withUserErfasst(defaultEmpfaenger)
            .withEmpfaenger(defaultEmpfaenger)
            .withTitle(newTitle)
            .withBemerkung(newBem)
            .withStatus(AufgabeStatus.IN_BEARBEITUNG)
        );

        // ACT
        AufgabeJsonSpec result = api
            .aufgabeResourceUpdateAufgabe()
            .aufgabeIdPath(existingAufgabe.getId().getId())
            .body(updatedAufgabeJson.getData())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(AufgabeJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isEqualTo(existingAufgabe.getId().getId());
        Assertions.assertThat(result.getData().getTitel()).isEqualTo(newTitle);
        Assertions.assertThat(result.getData().getBemerkungen()).isEqualTo(newBem);
        Assertions.assertThat(result.getData().getStatus()).isEqualTo(AufgabeStatusSpec.IN_BEARBEITUNG);
        Assertions.assertThat(result.getData().getEmpfaenger().getId()).isEqualTo(defaultEmpfaenger.getId().getId());
        Assertions.assertThat(result.getData().getUserErfasst().getId()).isEqualTo(userErfasst.getId().getId());
    }

    @Test
    @TestAsAdminBS
    void updateAufgabe_forInactiveKind_shouldThrow() {
        // ARRANGE
        Kind kind = fixtureService.createInactiveKind();
        Aufgabe existingAufgabe = fixtureService.createAufgabe(kind);
        Benutzer defaultEmpfaenger = benutzerService.persist(BenutzerBuilder.defaultBenutzer());
        Benutzer defaultUserErfasst = benutzerService.persist(BenutzerBuilder.defaultBenutzer());

        String newTitle = TestUtil.getRandomString("Neuer Titel");
        String newBem = TestUtil.getRandomString("Neue Bemerkung");

        AufgabeJsonSpec updatedAufgabeJson = AufgabeBuilder.createJaxSpec(builder -> builder
            .forAufgabe(existingAufgabe)
            .withKind(kind)
            .withUserErfasst(defaultUserErfasst)
            .withEmpfaenger(defaultEmpfaenger)
            .withTitle(newTitle)
            .withBemerkung(newBem)
            .withStatus(AufgabeStatus.IN_BEARBEITUNG)
        );

        String expectedValidationErrorMessage = String.format(
            "Das Kind %s %s ist inaktiv. Es können keine Änderungen an Aufgabe %s vorgenommen werden.",
            kind.getNameVorname(),
            kind.getId(),
            existingAufgabe.getId());

        // ACT & ASSERT
        AppValidationErrorJsonSpec validationError = api
            .aufgabeResourceUpdateAufgabe()
            .aufgabeIdPath(existingAufgabe.getId().getId())
            .body(updatedAufgabeJson.getData())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract().as(AppValidationErrorJsonSpec.class);

        assertThat(validationError.getViolations()).hasSize(1);
        assertThat(validationError.getViolations().getFirst().getKey())
            .isEqualTo("CannotEditEntityForInactiveKind");
        assertThat(validationError.getViolations().getFirst().getMessage())
            .isEqualTo(expectedValidationErrorMessage);
    }

    @Test
    @TestAsAdminBS
    void removeAufgabe() {
        // ARRANGE
        Aufgabe aufgabe = fixtureService.createAufgabe();

        // ACT
        ID<Aufgabe> aufgabeId = aufgabe.getId();
        api.aufgabeResourceRemoveAufgabe()
            .aufgabeIdPath(aufgabeId.getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());

        // ASSERT
        AppFailureException ex = assertThrows(
            AppFailureException.class,
            () -> aufgabeService.getById(aufgabeId)
        );
        assertThat(ex).hasMessageContaining("ENTITY_NOT_FOUND");
    }

    @Test
    @TestAsAdminBS
    void removeAufgabe_ofInactiveKind_shouldFail() {
        // ARRANGE
        Kind kind = fixtureService.createInactiveKind();
        Aufgabe aufgabe = fixtureService.createAufgabe(kind);

        String expectedValidationErrorMessage = String.format(
            "Das Kind %s %s ist inaktiv. Aufgabe mit %s kann nicht vom Kind entfernt werden.",
            kind.getNameVorname(),
            kind.getId(),
            aufgabe.getId());

        // ACT
        ID<Aufgabe> aufgabeId = aufgabe.getId();
        AppValidationErrorJsonSpec validationError = api
            .aufgabeResourceRemoveAufgabe()
            .aufgabeIdPath(aufgabeId.getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract()
            .as(AppValidationErrorJsonSpec.class);

        assertThat(validationError.getViolations()).hasSize(1);
        assertThat(validationError.getViolations().getFirst().getKey())
            .isEqualTo("CannotRemoveEntityFromInactiveKind");
        assertThat(validationError.getViolations().getFirst().getMessage())
            .isEqualTo(expectedValidationErrorMessage);
    }

    @Nested
    class FileEndpoints {
        @Test
        @TestAsAdminBS
        void uploadFileToAufgabe() {
            // ARRANGE
            Aufgabe aufgabe = fixtureService.createAufgabe();
            File pdfFile = TestUtil.loadTestPDF();

            // ACT
            AufgabeJsonSpec aufgabeJsonSpec = api
                .aufgabeResourceAddAufgabeFile()
                .aufgabeIdPath(aufgabe.getId().getId())
                ._fileMultiPart(pdfFile)
                .execute(ResponseBody::prettyPeek)
                .then()
                .statusCode(Status.OK.getStatusCode())
                .extract().as(AufgabeJsonSpec.class);

            // ASSERT
            Assertions.assertThat(aufgabeJsonSpec.getData().getFiles())
                .isNotEmpty()
                .asInstanceOf(InstanceOfAssertFactories.list(FileDataJsonSpec.class))
                .hasSize(1);
        }

        @ParameterizedTest
        @CsvSource({
            "/FileUtilTest/jpgDisguisedAsPng.png"
        })
        @TestAsAdminBS
        void given_filetype_not_matching_extension__uploadFileToAufgabe__should_fail(String problematicFile) {
            // ARRANGE
            Aufgabe aufgabe = fixtureService.createAufgabe();
            File pdfFile = TestUtil.loadTestfile(problematicFile);

            //ACT
            AppValidationErrorJsonSpec error = api
                .aufgabeResourceAddAufgabeFile()
                .aufgabeIdPath(aufgabe.getId().getId())
                ._fileMultiPart(pdfFile)
                .execute(ResponseBody::prettyPeek)
                .then()
                .statusCode(Status.BAD_REQUEST.getStatusCode())
                .extract()
                .as(AppValidationErrorJsonSpec.class);
            // ASSERT
            ViolationJsonSpec violation = error.getViolations().getFirst();
            assertThat(violation.getMessage()).contains(
                "png' does not match content type: 'image/jpeg'. Expected due to file extension: 'image/png'");
        }

        @ParameterizedTest
        @CsvSource({
            "/FileUtilTest/hello.sh",
        })
        @TestAsAdminBS
        void given_disallowed_file_type__uploadFileToNotiz__should_fail(String problematicFile) {
            // ARRANGE
            Aufgabe aufgabe = fixtureService.createAufgabe();
            File pdfFile = TestUtil.loadTestfile(problematicFile);

            //ACT
            var violation = api
                .aufgabeResourceAddAufgabeFile()
                .aufgabeIdPath(aufgabe.getId().getId())
                ._fileMultiPart(pdfFile)
                ._fileMultiPart(pdfFile)
                .execute(ResponseBody::prettyPeek)
                .then()
                .statusCode(Status.BAD_REQUEST.getStatusCode())
                .extract()
                .as(AppValidationErrorJsonSpec.class);
            // ASSERT
            assertThat(violation.getViolations())
                .containsExactlyInAnyOrder(
                    new ViolationJsonSpec(
                        "",
                        "UploadInvalidFiletype",
                        "Dieser Datei-Typ kann nicht hochgeladen werden: application/octet-stream")
                );
        }

        @Test
        @TestAsAdminBS
        void downloadAufgabeFile() {
            // ARRANGE
            Aufgabe storedAufgabe = fixtureService.createAufgabe();

            File testfile = TestUtil.loadTestImage();
            AufgabeJsonSpec aufgabeJsonSpec = api
                .aufgabeResourceAddAufgabeFile()
                .aufgabeIdPath(storedAufgabe.getId().getId())
                ._fileMultiPart(testfile)
                .execute(ResponseBody::prettyPeek)
                .then()
                .statusCode(Status.OK.getStatusCode())
                .extract()
                .as(AufgabeJsonSpec.class);

            Assertions.assertThat(aufgabeJsonSpec.getData().getFiles())
                .isNotEmpty()
                .asInstanceOf(InstanceOfAssertFactories.list(FileDataJsonSpec.class))
                .hasSize(1);
            // ACT

            byte[] image = api
                .aufgabeResourceDownloadAufgabeFile()
                .aufgabeIdPath(storedAufgabe.getId().getId())
                .fileIdPath(Objects.requireNonNull(aufgabeJsonSpec.getData().getFiles()).getFirst().getId())
                .execute(ResponseOptions::andReturn)
                .then()
                .statusCode(Status.OK.getStatusCode())
                .extract()
                .asByteArray();

            // ASSERT
            assertThat(image).isNotNull();
            assertThat(testfile)
                .hasSize(image.length)
                // check that loading the file synchronously works as well and returns the same content
                .hasSize(fileStoreService.getFileBytesSync(aufgabeJsonSpec.getData()
                    .getFiles()
                    .getFirst()
                    .getFileIdInBucket()).length);
        }

        @Test
        @TestAsAdminBS
        void downloadAufgabeFileSync() {
            // ARRANGE
            Aufgabe storedAufgabe = fixtureService.createAufgabe();

            File testfile = TestUtil.loadTestImage();
            String name = testfile.getName();
            AufgabeJsonSpec aufgabeJsonSpec = api
                .aufgabeResourceAddAufgabeFile()
                .aufgabeIdPath(storedAufgabe.getId().getId())
                ._fileMultiPart(testfile)
                .execute(ResponseBody::prettyPeek)
                .then()
                .statusCode(Status.OK.getStatusCode())
                .extract()
                .as(AufgabeJsonSpec.class);

            Assertions.assertThat(aufgabeJsonSpec.getData().getFiles())
                .isNotEmpty()
                .asInstanceOf(InstanceOfAssertFactories.list(FileDataJsonSpec.class))
                .hasSize(1);
            // ACT

            byte[] image = api
                .aufgabeResourceDownloadAufgabeFileSync()
                .aufgabeIdPath(storedAufgabe.getId().getId())
                .fileIdPath(Objects.requireNonNull(aufgabeJsonSpec.getData().getFiles()).getFirst().getId())
                .execute(ResponseOptions::andReturn)
                .then()
                .statusCode(Status.OK.getStatusCode())
                .header("Content-Disposition", "attachment;filename=\"" + name + '"')
                .extract()
                .asByteArray();

            // ASSERT
            assertThat(image).isNotNull();
            assertThat(testfile)
                .hasSize(image.length)
                // check that loading the file synchronously works as well and returns the same content
                .hasSize(fileStoreService.getFileBytesSync(aufgabeJsonSpec.getData()
                    .getFiles()
                    .getFirst()
                    .getFileIdInBucket()).length);
        }

        @Test
        @TestAsAdminBS
        void removeFileFromAufgabe() {
            // ARRANGE
            Aufgabe storedAufgabe = fixtureService.createAufgabe();

            File pdfFile = TestUtil.loadTestPDF();

            AufgabeJsonSpec aufgabeJsonSpec = api
                .aufgabeResourceAddAufgabeFile()
                .aufgabeIdPath(storedAufgabe.getId().getId())
                ._fileMultiPart(pdfFile)
                .execute(ResponseBody::prettyPeek)
                .then()
                .statusCode(Status.OK.getStatusCode())
                .extract()
                .as(AufgabeJsonSpec.class);

            Assertions.assertThat(aufgabeJsonSpec.getData().getFiles())
                .isNotEmpty()
                .asInstanceOf(InstanceOfAssertFactories.list(FileDataJsonSpec.class))
                .hasSize(1);

            //ACT
            AufgabeJsonSpec aufgabeAfterRemoval = api
                .aufgabeResourceRemoveAufgabeFile()
                .aufgabeIdPath(storedAufgabe.getId().getId())
                .fileIdPath(Objects.requireNonNull(aufgabeJsonSpec.getData().getFiles()).getFirst().getId())
                .execute(ResponseBody::prettyPeek)
                .then()
                .statusCode(Status.OK.getStatusCode())
                .extract().as(AufgabeJsonSpec.class);

            // ASSERT
            Assertions.assertThat(aufgabeAfterRemoval.getData().getFiles())
                .isEmpty();
        }

        @Test
        @TestAsAdminBS
        void findAllNichtAbgeschlosseneForCurrentUser() {
            //ARRANGE
            var adminId = ID.of(UUID.fromString(TestAsAdminBS.BENUTZER_ID), Benutzer.class);
            var currentBenutzer = benutzerService.getById(adminId);
            Benutzer someOtherUser = BenutzerBuilder.defaultBenutzer();
            benutzerService.persist(someOtherUser);
            int numOfFaelligeAufgabenBefore = aufgabeService.getNumOfOffeneFaelligeAufgabenByCurrentUser();

            Benutzer defaultEmpfaenger = BenutzerBuilder.defaultBenutzer();
            benutzerService.persist(defaultEmpfaenger);

            Aufgabe aufgabe1 = AufgabeBuilder.create(builder -> builder
                .withTitle(TestUtil.getRandomString("Test Aufgabe 1 Titel"))
                .withEmpfaenger(currentBenutzer)
                .withUserErfasst(someOtherUser)
                .withStatus(AufgabeStatus.IN_BEARBEITUNG)
            );
            aufgabeService.persist(aufgabe1);

            Aufgabe aufgabe2 = AufgabeBuilder.create(builder -> builder
                .withTitle(TestUtil.getRandomString("Test Aufgabe 2 Titel"))
                .withEmpfaenger(currentBenutzer)
                .withUserErfasst(someOtherUser)
                .withStatus(AufgabeStatus.OFFEN)
            );
            aufgabeService.persist(aufgabe2);

            Aufgabe aufgabe3 = AufgabeBuilder.create(builder -> builder
                .withTitle(TestUtil.getRandomString("Test Aufgabe 3 Titel"))
                .withEmpfaenger(currentBenutzer)
                .withUserErfasst(someOtherUser)
                .withStatus(AufgabeStatus.ABGESCHLOSSEN)
            );

            aufgabeService.persist(aufgabe3);

            //ACT
            AufgabeListJsonSpec result = api.aufgabeResourceFindAllNichtAbgeschlosseneAufgabenForCurrent()
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Response.Status.OK.getStatusCode())
                .extract()
                .as(AufgabeListJsonSpec.class);

            //ASSERT
            assertThat(result.getNumOfOffeneFaelligeAufgaben()).isEqualTo(numOfFaelligeAufgabenBefore + 2);
            assertThat(result.getAufgaben()).isNotEmpty()
                .anyMatch(aufgabeJsonSpec ->
                    aufgabe1.getId().getId().equals(aufgabeJsonSpec.getId())
                        && aufgabe1.getAufgabeStatus().equals(AufgabeStatus.IN_BEARBEITUNG))
                .anyMatch(aufgabeJsonSpec -> aufgabe2.getId().getId().equals(aufgabeJsonSpec.getId())
                    && aufgabe2.getAufgabeStatus().equals(AufgabeStatus.OFFEN))
                .noneMatch(aufgabeJsonSpec -> aufgabe3.getId().getId().equals(aufgabeJsonSpec.getId())
                    && aufgabe3.getAufgabeStatus().equals(AufgabeStatus.ABGESCHLOSSEN));
        }
    }

}
