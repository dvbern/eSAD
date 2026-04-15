package ch.dvbern.oss.hemed.esc.api.features.example.developer;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

import ch.dvbern.dvbstarter.shared.i18n.datetime.DateConst;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.DeveloperApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AppFailureErrorJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AppValidationErrorJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.ExampleMsgSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.FailureTypeSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.ViolationJsonSpec;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSupportBS;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class DeveloperResourceITest extends AbstractITest {

    public static final OffsetDateTime OFFSET_DATE_TIME = ZonedDateTime.of(
            LocalDateTime.of(2021, 4, 20, 12, 0),
            DateConst.ZONE_ID)
        .toOffsetDateTime();

    @BeforeEach
    void setup() {
        this.setTime(OFFSET_DATE_TIME);
    }

    DeveloperApiSpec developerApiSpec = DeveloperApiSpec.developer(RequestSpecUtil.quarkusSpec());

    @Test
    @TestAsSupportBS
    void getConfig() {

        // ACT
        var result = developerApiSpec
            .developerResourceGetConfig()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(200)
            .extract().asString();
        // ASSERT
        Assertions.assertThat(result).isNotNull()
            .contains("quarkus.application.name=esc-api", "app.allow-configprint=true");
    }

    @Test
    void getDate() {
        // ACT
        var result = developerApiSpec
            .developerResourceGetDate()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(200)
            .extract().as(ExampleDateTimeJson.class);
        // ASSERT
        Assertions.assertThat(result.getLocalDate()).isNotNull();
        Assertions.assertThat(result.getLocalDateTime()).isNotNull();
        Assertions.assertThat(result.getOffsetDateTime()).isNotNull();
        Assertions.assertThat(result.getZonedDateTime()).isNotNull();
    }

    @Test
    void triggerRuntimeException() {
        // ACT
        var failure = developerApiSpec
            .developerResourceTriggerFailureMessage()
            .body(new ExampleMsgSpec("Runtime Fehler"))
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(500)
            .extract()
            .as(AppFailureErrorJsonSpec.class);
        // ASSERT
        Assertions.assertThat(failure).isNotNull()
            .returns(FailureTypeSpec.FAILURE, AppFailureErrorJsonSpec::getType)
            .returns("Interner Server-Fehler: \"Runtime Fehler\".", AppFailureErrorJsonSpec::getSummary)
            .extracting(AppFailureErrorJsonSpec::getId)
            .isNotNull();
    }

    @Test
    void triggerFailureMessage() {
        // ACT
        var failure = developerApiSpec
            .developerResourceTriggerFailureMessage()
            .body(new ExampleMsgSpec("Behandelter Fehler"))
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(500)
            .extract()
            .as(AppFailureErrorJsonSpec.class);
        // ASSERT
        Assertions.assertThat(failure).isNotNull()
            .returns(FailureTypeSpec.FAILURE, AppFailureErrorJsonSpec::getType)
            .returns("Interner Server-Fehler: \"Behandelter Fehler\".", AppFailureErrorJsonSpec::getSummary);
    }

    @Test
    void triggerValidationMessage() {
        // ACT
        var failure = developerApiSpec
            .developerResourceTriggerValidationMessage()
            .body(new ExampleMsgSpec("invalidEmail"))
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(400)
            .extract()
            .as(AppValidationErrorJsonSpec.class);
        // ASSERT
        Assertions.assertThat(failure).isNotNull()
            .returns(FailureTypeSpec.VALIDATION, AppValidationErrorJsonSpec::getType)
            .extracting(AppValidationErrorJsonSpec::getViolations)
            .asInstanceOf(InstanceOfAssertFactories.list(ViolationJsonSpec.class))
            .hasSize(1)
            .first()
            .extracting(ViolationJsonSpec::getMessage)
            .matches(s -> s.contains("invalidEmail"));
    }
}
