package ch.dvbern.dvbstarter.infrastructure.exceptionmapper;

import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.sozialversicherungsnummer.SozialversicherungsNummer;
import ch.dvbern.dvbstarter.shared.types.sozialversicherungsnummer.SozialversicherungsNummerTestGenerator;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.Impfstoff;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.ImpfstoffBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.KindBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.ImpfstoffeApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.KinderApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AppValidationErrorJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.FailureTypeSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.ImpfstoffSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KindSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KindSaveModeDuplikatSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@RequiredArgsConstructor
class HibernateConstraintViolationExceptionMapperITest extends AbstractITest {

    private final KinderApiSpec kinderApi = KinderApiSpec.kinder(RequestSpecUtil.quarkusSpec());
    private final ImpfstoffeApiSpec impfstoffeApi = ImpfstoffeApiSpec.impfstoffe(RequestSpecUtil.quarkusSpec());

    private final AppClock appClock;
    private final FixtureService fixtureService;

    @Test
    @TestAsAdminBS
    void unique_key_constraint_violated_with_specific_message() {
        // ARRANGE
        SozialversicherungsNummer sozialversicherungsnummer =
            SozialversicherungsNummerTestGenerator.INSTANCE.getNextSozialversicherungsnummer();
        // Existing Kind with same VNR
        fixtureService.createKind(sozialversicherungsnummer);

        // ACT
        KindSaveJsonSpec saveKindJson = KindBuilder.createJaxSpec(
            builder -> builder
                .withSozialversicherungsNummer(sozialversicherungsnummer),
            appClock.offsetDateTime().now().toLocalDate()
        ).getData();
        AppValidationErrorJsonSpec errResult = kinderApi
            .kindResourceCreateKindAndDossier()
            .body(saveKindJson)
            .kindSaveModeQuery(KindSaveModeDuplikatSpec.CHECK)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract().as(AppValidationErrorJsonSpec.class);

        // ASSERT
        assertThat(errResult).isNotNull();
        assertThat(errResult.getType()).isEqualTo(FailureTypeSpec.VALIDATION);
        assertThat(errResult.getViolations()).hasSize(1);
        assertThat(errResult.getViolations().getFirst().getKey())
            .isEqualTo("GenericMessage");
        assertThat(errResult.getViolations().getFirst().getMessage())
            .isEqualTo("Die Sozialversicherungsnummer muss einzigartig sein");
    }

    @Test
    @TestAsAdminBS
    void unique_key_constraint_violated_no_specific_message() {
        // ARRANGE
        Impfstoff existingImpfstoff = fixtureService.createImpfstoff();

        //ACT
        ImpfstoffSaveJsonSpec saveJson = ImpfstoffBuilder.createJaxSpec(
                builder -> builder
                    .withName(existingImpfstoff.getName()))
            .getData();
        AppValidationErrorJsonSpec errResult = impfstoffeApi
            .impfstoffResourceCreateImpfstoff()
            .body(saveJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract().as(AppValidationErrorJsonSpec.class);

        // ASSERT
        assertThat(errResult).isNotNull();
        assertThat(errResult.getType()).isEqualTo(FailureTypeSpec.VALIDATION);
        assertThat(errResult.getViolations()).hasSize(1);
        assertThat(errResult.getViolations().getFirst().getKey())
            .isEqualTo("UniqueKeyViolation");
        assertThat(errResult.getViolations().getFirst().getMessage())
            .isEqualTo("Das Objekt konnte nicht gespeichert werden, da ein UniqueKeyConstraint verletzt wurde: uc_impfstoff_name. Bitte überprüfen Sie die Eingaben.");
    }
}
