package ch.dvbern.oss.hemed.esc.api.features.importoptions.pupil;

import java.io.File;
import java.util.Map;
import java.util.Objects;

import ch.dvbern.oss.hemed.esc.api.features.example.developer.dbinfo.DatabaseService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.assertj.AppValidationErrorJsonSpecAssert;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminWIN;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsPupil;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.DeveloperApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.PupilApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AppValidationErrorJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.EscadaImportResultJsonKindImportResultJsonsInnerSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.EscadaImportResultJsonKlasseImportResultJsonsInnerSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.EscadaImportResultJsonSchuleImportResultJsonsInnerSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.ImportStatusSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.PupilImportResultJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.ViolationJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.response.ResponseBody;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.PUPIL_PUSH;

@QuarkusTest
@RequiredArgsConstructor
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
class PupilResourceITest extends AbstractITest {

    private static final String SO_TIMEOUT = "http.socket.timeout";
    private final DatabaseService databaseService;
    private final PupilApiSpec api = PupilApiSpec.pupil(RequestSpecUtil.quarkusSpec());
    private final DeveloperApiSpec developerApiSpec = DeveloperApiSpec.developer(RequestSpecUtil.quarkusSpec());

    @Nullable
    private Integer socketTimeoutBefore;

    @BeforeAll
    @ActivateRequestContext
    void beforeAll() {
        Map<String, ?> clientConfigParams = RestAssuredConfig.config().getHttpClientConfig().params();
        socketTimeoutBefore =
            clientConfigParams.get(SO_TIMEOUT) != null ? (Integer) clientConfigParams.get(SO_TIMEOUT) : null;
        // import takes longer that the default socket timeout
        resetDatabase();
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        configureRestAssuredSocketTimeout(360000); // set to 360s
    }

    @AfterAll
    @ActivateRequestContext
    void afterAll() {
        resetDatabase();
        configureRestAssuredSocketTimeout(socketTimeoutBefore);
    }

    private void resetDatabase() {
        databaseService.resetAndReinitDatabasePreservingHibernateSequenceValue(TenantEnum.BS);
    }

    private static void configureRestAssuredSocketTimeout(@Nullable Integer timeout) {
        RestAssured.config = RestAssuredConfig.config()
            .httpClient(HttpClientConfig.httpClientConfig()
                .setParam(SO_TIMEOUT, timeout));
    }

    @TestAsPupil
    @Test
    @Order(10)
    void pupil_csv_upload_throws_if_file_empty() {
        File emptyFile = TestUtil.loadEmptyFile();
        // ACT
        var result = api.pupilResourceSyncWithPupil()
            ._fileMultiPart(emptyFile)
            .execute(ResponseBody::peek)
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract()
            .as(AppValidationErrorJsonSpec.class);
        // ASSERT

        AppValidationErrorJsonSpecAssert.assertThatAppValidationErrorJsonSpec(result)
            .extractingExactlyOneViolation()
            .hasClientKey("InvalidCSVFormat");

        Assertions.assertThat(result.getViolations())
            .isNotEmpty()
            .asInstanceOf(InstanceOfAssertFactories.list(ViolationJsonSpec.class))
            .hasSize(1)
            .extracting(ViolationJsonSpec::getKey)
            .allMatch(k -> k.equals("InvalidCSVFormat"));
    }

    @TestAsPupil
    @Test
    @Order(40)
    void pupil_csv_upload_pseudo_data() {
        // ARRANGE
        File firstFile = loadTestData();

        // ACT
        var result = api.pupilResourceSyncWithPupil()
            ._fileMultiPart(firstFile)
            .execute(v -> v)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(PupilImportResultJsonSpec.class);
        // ASSERT

        Assertions.assertThat(result.getSchuleImportResultJsons())
            .hasSize(31)
            .isNotEmpty()
            .asInstanceOf(InstanceOfAssertFactories.list(EscadaImportResultJsonSchuleImportResultJsonsInnerSpec.class))
            .anyMatch(r -> Objects.equals(r.getExternalId(), "222221"))
            .allMatch(r -> r.getStatus() == ImportStatusSpec.OK);

        Assertions.assertThat(result.getKlasseImportResultJsons())
            .hasSize(46)
            .isNotEmpty()
            .asInstanceOf(InstanceOfAssertFactories.list(EscadaImportResultJsonKlasseImportResultJsonsInnerSpec.class))
            .anyMatch(k -> Objects.equals(k.getExternalId(), "111111"))
            .allMatch(k -> k.getStatus() == ImportStatusSpec.OK);

        Assertions.assertThat(result.getKindImportResultJsons())
            .isNotEmpty()
            .hasSize(49)
            .asInstanceOf(InstanceOfAssertFactories.list(EscadaImportResultJsonKindImportResultJsonsInnerSpec.class))
            .anyMatch(k -> Objects.equals(k.getExternalId(), "S84475"));

    }

    @TestAsAdminWIN
    @Test
    @SuppressWarnings("java:S5976")
    @Order(50)
    void pupil_csv_upload_not_allowed_as_support() {
        // ARRANGE
        File firstFile = loadTestData();

        // ACT
        api.pupilResourceSyncWithPupil()
            ._fileMultiPart(firstFile)
            .execute(ResponseBody::prettyPeek)
            .then()
            .statusCode(Status.FORBIDDEN.getStatusCode());
    }

    @TestSecurity(
        user = TestAsPupil.USERNAME,
        // java Annotation require compile time Constants so we need to define the array each time :(
        roles = {
            PUPIL_PUSH
        }
    )
    @OidcSecurity(
        claims = {
            @Claim(key = "sub", value = TestAsPupil.BENUTZER_ID),
            @Claim(key = "tenant", value = "WIN"),
            @Claim(key = "family_name", value = "Serviceaccount"),
            @Claim(key = "given_name", value = "Pupil"),
            @Claim(key = "preferred_username", value = TestAsPupil.USERNAME),
            @Claim(key = "email", value = TestAsPupil.EMAIL),
            @Claim(key = "azp", value = "invalid_client"),
        }
    )
    @Test
    @Order(60)
    void pupil_csv_upload_client_not_allowed() {
        // ARRANGE
        File firstFile = loadTestData();

        // ACT
        api.pupilResourceSyncWithPupil()
            ._fileMultiPart(firstFile)
            .execute(ResponseBody::prettyPeek)
            .then()
            .statusCode(Status.FORBIDDEN.getStatusCode());
    }

    @TestSecurity(
        user = TestAsPupil.USERNAME,
        // java Annotation require compile time Constants so we need to define the array each time :(
        roles = {
            PUPIL_PUSH
        }
    )
    @OidcSecurity(
        claims = {
            @Claim(key = "sub", value = TestAsPupil.BENUTZER_ID),
            @Claim(key = "tenant", value = "WIN"),
            @Claim(key = "family_name", value = "Serviceaccount"),
            @Claim(key = "given_name", value = "Pupil"),
            @Claim(key = "preferred_username", value = "invalid_username"),
            @Claim(key = "email", value = TestAsPupil.EMAIL),
            @Claim(key = "azp", value = TestAsPupil.AZP),
        }
    )
    @Test
    @Order(70)
    void pupil_csv_upload_user_not_allowed() {
        // ARRANGE
        File firstFile = loadTestData();

        // ACT
        api.pupilResourceSyncWithPupil()
            ._fileMultiPart(firstFile)
            .execute(ResponseBody::prettyPeek)
            .then()
            .statusCode(Status.FORBIDDEN.getStatusCode());
    }

    @TestAsPupil
    @Test
    @Order(80)
    void pupil_client_blocked_from_other_apis() {
        // ACT
        developerApiSpec
            .developerResourceGetDate()
            .execute(ResponseBody::prettyPeek)
            .then()
            .statusCode(Status.FORBIDDEN.getStatusCode());
    }

    @TestAsPupil
    @Test
    @Order(110)
    void pupil_csv_upload_async_pseudo_data() {
        // ARRANGE
        File firstFile = loadTestData();

        // ACT
        api.pupilResourceSyncWithPupilAsync()
            ._fileMultiPart(firstFile)
            .execute(v -> v)
            .then()
            .statusCode(Status.OK.getStatusCode());
    }

    @NonNull
    private File loadTestData() {
        return TestUtil.loadComplexPupilFile();
    }
}
