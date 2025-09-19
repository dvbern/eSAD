package ch.dvbern.oss.hemed.esc.api.features.benutzer;

import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumber;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.BenutzerBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.BenutzerApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.*;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@TestAsAdminBS
@DisplayNameGeneration(ReplaceUnderscores.class)
@RequiredArgsConstructor
class BenutzerResourceITest {

    @TestHTTPEndpoint(BenutzerResource.class)
    @TestHTTPResource
    URL url = null;
    private final BenutzerApiSpec api = BenutzerApiSpec.benutzer(RequestSpecUtil.quarkusSpec());

    private final BenutzerService benutzerService;
    private final BenutzerRepo benutzerRepo;

    @Test
    void error_returns_a_response_of_type_failure() throws Exception {
        var response = given()
            .when()
            .contentType(ContentType.JSON)
            .get(url.toURI().resolve("error"))
            .then();

        response
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_BAD_REQUEST);

        var type = response.extract()
            .body().<String>path("type");
        assertThat(type)
            .isEqualTo("FAILURE");
    }

    @Test
    @TestAsAdminBS
    void findAllBenutzerOptions() {
        // ARRANGE

        // ACT
        var benList = api.benutzerResourceFindAllActiveBenutzerOptions()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_OK)
            .extract().as(BenutzerOptionListJsonSpec.class);

        // ASSERT
        Assertions.assertThat(benList.getBenutzerList()).isNotEmpty();
        // there must be at least a systemadmin present
        Assertions.assertThat(benList.getBenutzerList())
            .extracting(BenutzerOptionJsonSpec::getVorname)
            .contains("ESC-BASEL-SYSADM");
    }

    @Test
    @TestAsAdminBS
    void findAllBenutzerOptions_returns_only_active_benutzer() {
        // ARRANGE
        Benutzer benutzerActive = BenutzerBuilder.create(
            builder -> builder
                .withActive(true)
        );
        Benutzer benutzerInactive = BenutzerBuilder.create(
            builder -> builder
                .withActive(false)
        );
        benutzerRepo.persist(benutzerActive);
        benutzerRepo.persist(benutzerInactive);
        // ACT
        var benList = api.benutzerResourceFindAllActiveBenutzerOptions()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_OK)
            .extract().as(BenutzerOptionListJsonSpec.class);

        // ASSERT
        Assertions.assertThat(benList.getBenutzerList())
            .isNotEmpty()
            .anyMatch(ben -> ben.getId().equals(benutzerActive.getId().getId()))
            .noneMatch(ben -> ben.getId().equals(benutzerInactive.getId().getId()));
    }

    @Test
    @TestAsAdminBS
    void findAllBenutzer() {
        // ARRANGE
        var createDto = BenutzerBuilder.createCreateDTO(
            builder -> builder
                .withRandomEmail("nina.neu@example.com")
                .withRandomVorname("Nina")
                .withRandomNachname("Neu")
                .withKeycloakId(UUID.randomUUID().toString())
        );
        benutzerService.createBenutzer(createDto);

        // ACT
        var benList = api.benutzerResourceFindAllBenutzer()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_OK)
            .extract().as(BenutzerListJsonSpec.class);

        // ASSERT
        Assertions.assertThat(benList.getBenutzerList())
            .isNotEmpty()
            .extracting(BenutzerJsonSpec::getData)
            .extracting(BenutzerSaveJsonSpec::getVorname)
            .contains(createDto.getVorname());

    }

    @Test
    @TestAsAdminBS
    void getMyBenutzerData() {
        // ACT
        PrincipalJsonSpec principalJson = api.benutzerResourceGetMyBenutzerData()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_OK)
            .extract().as(PrincipalJsonSpec.class);

        // ASSERT
        Assertions.assertThat(principalJson).isNotNull();
        BenutzerJsonSpec benutzerJson = principalJson.getBenutzerJson();
        Assertions.assertThat(benutzerJson)
            .isNotNull()
            .extracting(BenutzerJsonSpec::getData)
            .extracting(BenutzerSaveJsonSpec::getEmail)
            .isNotNull()
            .isEqualTo(TestAsAdminBS.USERNAME);
    }

    @TestAsAdminBS
    @ParameterizedTest
    @CsvSource({
        "true, false",
        "false, true",
        "false, false",
        "true, true"
    })
    void updateActive(boolean oldActive, boolean newActive) {
        // ARRANGE
        ID<Benutzer> id = ID.random(Benutzer.class);
        Benutzer benutzer = BenutzerBuilder.create(
            builder -> builder
                .withActive(oldActive)
                .withID(id)
        );
        benutzerRepo.persist(benutzer);
        BenutzerSaveJsonSpec saveJson = BenutzerBuilder.createJaxSpec(
            builder -> builder
                .forBenutzer(benutzer)
                .withActive(newActive)
        ).getData();

        // ACT
        BenutzerJsonSpec result = api
            .benutzerResourceUpdateBenutzerAktivStatus()
            .benutzerIDPath(id.getId())
            .body(saveJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(BenutzerJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        if (oldActive == newActive) {
            assertThat(benutzer.isAktiv()).isEqualTo(result.getData().getAktiv()).isEqualTo(oldActive).isEqualTo(newActive);
        } else {
            assertThat(result.getData().getAktiv()).isEqualTo(benutzer.isAktiv());
            assertThat(result.getData().getAktiv()).isEqualTo(newActive);
            assertThat(result.getData().getAktiv()).isNotEqualTo(oldActive);
        }

    }

    @Test
    @TestAsAdminBS
    void updateBenutzerAsAdmin_only_allowed_fields_works() {
        // ARRANGE
        EmailAddress originalEmail = EmailAddress.fromString("original@mailbucket.dvbern.ch");
        Benutzer benutzer = BenutzerBuilder.create(
            builder -> builder
                .withFachRolle(List.of(FachRolle.EMPFANG))
                .withEmail(originalEmail)
                .withActive(false));
        benutzerRepo.persist(benutzer);

        String updatedVorname = "NewVorname";
        String updatedNachname = "NewNachname";
        List<FachRolle> udpatedFachRollen = List.of(FachRolle.SCHULAERZTIN);
        EmailAddress updatedEmail = EmailAddress.fromString("updated@mailbucket.dvbern.ch");
        NormalizedPhoneNumber updatedTelefon = NormalizedPhoneNumber.parse("+41790000000");
        boolean updatedActive = true;
        BenutzerSaveJsonSpec saveJson = BenutzerBuilder.createJaxSpec(builder -> builder
            .forBenutzer(benutzer)
            .withVorname(updatedVorname)
            .withNachname(updatedNachname)
            .withEmail(updatedEmail)
            .withTelefon(updatedTelefon)
            .withFachRolle(udpatedFachRollen)
            .withActive(updatedActive)).getData();

        // ACT
        BenutzerJsonSpec result = api.
            benutzerResourceUpdate()
            .benutzerIDPath(benutzer.getId().getId())
            .body(saveJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(BenutzerJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getData().getVorname()).isEqualTo(updatedVorname);
        assertThat(result.getData().getName()).isEqualTo(updatedNachname);
        assertThat(result.getData().getEmail()).isNotEqualTo(updatedEmail.toString()).isEqualTo(originalEmail.toString());
        assertThat(result.getData().getTelefon()).isEqualTo(updatedTelefon.getFormattedPhoneNumber());
        List<FachRolle> fachRollen = result.getData().getFachRollen().stream()
            .map(spec -> FachRolle.valueOf(spec.name()))
            .toList();
        assertThat(fachRollen).containsExactlyElementsOf(udpatedFachRollen);
        assertThat(result.getData().getAktiv()).isEqualTo(updatedActive);
    }


    @Test
    @TestAsAdminBS
    void updateBenutzerAsAdmin_tryAssigningSystemAdminRole_doesnotUpdateRoles() {
        // ARRANGE
        List<FachRolle> originalFachRollen = List.of(FachRolle.SCHULAERZTIN);
        Benutzer benutzer = BenutzerBuilder.create(
            builder -> builder
                .withFachRolle(originalFachRollen));
        benutzerRepo.persist(benutzer);

        List<FachRolle> udpatedFachRollen = List.of(FachRolle.SYSTEM_ADMIN);
        BenutzerSaveJsonSpec saveJson = BenutzerBuilder.createJaxSpec(builder -> builder
            .forBenutzer(benutzer)
            .withFachRolle(udpatedFachRollen)).getData();

        // ACT
        AppFailureErrorJsonSpec error = api.
            benutzerResourceUpdate()
            .benutzerIDPath(benutzer.getId().getId())
            .body(saveJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
            .extract().as(AppFailureErrorJsonSpec.class);

        // ASSERT
        Assertions.assertThat(error).isNotNull();
        Assertions.assertThat(error.getSummary()).contains("Aktualisieren der Fachrollen ist nicht erlaubt, der Benutzer hat nicht die entsprechende Berechtigung.");
    }
}
