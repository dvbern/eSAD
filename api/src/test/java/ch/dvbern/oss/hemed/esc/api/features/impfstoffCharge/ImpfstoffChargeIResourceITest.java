package ch.dvbern.oss.hemed.esc.api.features.impfstoffCharge;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.Impfstoff;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.ImpfstoffCharge;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.ImpfstoffChargeService;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.ImpfstoffService;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.ImpfstoffBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.ImpfstoffChargeBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.ImpfstoffChargenApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.ImpfstoffChargeJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.ImpfstoffChargeListJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.ImpfstoffChargeSaveJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class ImpfstoffChargeIResourceITest extends AbstractITest {
    private final ImpfstoffChargenApiSpec api = ImpfstoffChargenApiSpec.impfstoffChargen(RequestSpecUtil.quarkusSpec());
    private final ImpfstoffService impfstoffService;
    private final ImpfstoffChargeService impfstoffChargeService;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    @TestAsAdminBS
    void removeImpfstoffCharge() {
        // ARRANGE
        Impfstoff impfstoff1 = impfstoffService.persist(ImpfstoffBuilder.defaultImpfstoff());

        ImpfstoffCharge charge1Impfstoff1 = ImpfstoffChargeBuilder.create(
            builder -> builder
                .forImpfstoff(impfstoff1)
                .withName("111"));
        impfstoffChargeService.persist(charge1Impfstoff1);

        ImpfstoffCharge charge2Impfstoff1 = ImpfstoffChargeBuilder.create(
            builder -> builder
                .forImpfstoff(impfstoff1)
                .withName("222"));
        impfstoffChargeService.persist(charge2Impfstoff1);

        Impfstoff impfstoffAnderer = impfstoffService.persist(ImpfstoffBuilder.defaultImpfstoff());
        ImpfstoffCharge chargeAndererImpfstoff = ImpfstoffChargeBuilder.create(
            builder -> builder
                .forImpfstoff(impfstoffAnderer)
                .withName("333"));
        impfstoffChargeService.persist(chargeAndererImpfstoff);

        //ACT
        api
            .impfstoffChargeResourceRemoveImpfstoffCharge()
            .impfstoffIdPath(impfstoff1.getId().getUUID())
            .impfstoffChargeIdPath(charge1Impfstoff1.getId().getUUID())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());

        api
            .impfstoffChargeResourceRemoveImpfstoffCharge()
            .impfstoffIdPath(impfstoff1.getId().getUUID())
            .impfstoffChargeIdPath(charge2Impfstoff1.getId().getUUID())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());

        //ASSERT
        Optional<ImpfstoffCharge> impstoffChargeAfterDelete =
            impfstoffChargeService.findById(charge1Impfstoff1.getId());
        Assertions.assertThat(impstoffChargeAfterDelete).isNotPresent();

        Optional<ImpfstoffCharge> impstoffCharge2AfterDelete =
            impfstoffChargeService.findById(charge2Impfstoff1.getId());
        Assertions.assertThat(impstoffCharge2AfterDelete).isNotPresent();

        Optional<ImpfstoffCharge> impstoffChargeAndererAfterDelete =
            impfstoffChargeService.findById(chargeAndererImpfstoff.getId());
        Assertions.assertThat(impstoffChargeAndererAfterDelete).isPresent();
    }

    @Test
    @TestAsAdminBS
    void impfstoffChargeResourceFindAllByImpfstoff() {
        var impfstoff = ImpfstoffBuilder.defaultImpfstoff();
        impfstoffService.persist(impfstoff);

        var impfstoffCharge1 = ImpfstoffChargeBuilder.create(
            builder -> builder
                .withID(ID.random(ImpfstoffCharge.class))
                .forImpfstoff(impfstoff)
                .withName("111"));
        impfstoffChargeService.persist(impfstoffCharge1);

        var impfstoffCharge2 = ImpfstoffChargeBuilder.create(
            builder -> builder
                .withID(ID.random(ImpfstoffCharge.class))
                .forImpfstoff(impfstoff)
                .withName("222"));
        impfstoffChargeService.persist(impfstoffCharge2);

        //ACT
        ImpfstoffChargeListJsonSpec result = api
            .impfstoffChargeResourceFindAllByImpfstoff()
            .impfstoffIdPath(impfstoff.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(ImpfstoffChargeListJsonSpec.class);

        List<ImpfstoffChargeJsonSpec> result2 = result.getImpfstoffChargeList();

        //ASSERT
        Assertions.assertThat(result2).isNotNull().isNotEmpty().hasSize(2)
            .anyMatch(jsonSpec -> impfstoffCharge1.getId().getId().equals(jsonSpec.getId()))
            .anyMatch(jsonSpec -> "111".equals(jsonSpec.getData().getName()))
            .anyMatch(jsonSpec -> "222".equals(jsonSpec.getData().getName()));

    }

    @Test
    @TestAsAdminBS
    void createImpfstoffCharge() {
        // ARRANGE
        var impfstoff = ImpfstoffBuilder.defaultImpfstoff();
        impfstoffService.persist(impfstoff);

        String randomName = TestUtil.getRandomString("impfstoff");
        ImpfstoffChargeSaveJsonSpec saveJson = ImpfstoffChargeBuilder.createJaxSpec(
                builder -> builder
                    .withName(randomName)
                    .forImpfstoff(impfstoff))
            .getData();

        //ACT
        ImpfstoffChargeJsonSpec result = api
            .impfstoffChargeResourceCreateImpfstoffCharge()
            .impfstoffIdPath(impfstoff.getId().getUUID())
            .body(saveJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().as(ImpfstoffChargeJsonSpec.class);

        ImpfstoffChargeListJsonSpec data = api
            .impfstoffChargeResourceFindAllByImpfstoff()
            .impfstoffIdPath(impfstoff.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(ImpfstoffChargeListJsonSpec.class);

        List<ImpfstoffChargeJsonSpec> result2 = data.getImpfstoffChargeList();
        ImpfstoffCharge updatedCharge =
            impfstoffChargeService.getById(ID.of(UUID.fromString(result.getId()), ImpfstoffCharge.class));

        //ASSERT
        Assertions.assertThat(updatedCharge).isNotNull();
        Assertions.assertThat(updatedCharge.getId()).isNotNull();
        Assertions.assertThat(updatedCharge.getName()).isEqualTo(randomName);
        Assertions.assertThat(updatedCharge.isAktiv()).isTrue();

        Assertions.assertThat(result2).isNotNull().isNotEmpty().hasSize(1)
            .anyMatch(jsonSpec -> randomName.equals(jsonSpec.getData().getName()));
    }

    @Test
    @TestAsAdminBS
    void updateImpfstoffCharge() {
        // ARRANGE
        Impfstoff impfstoff = ImpfstoffBuilder.defaultImpfstoff();
        impfstoffService.persist(impfstoff);

        String nameBefore = TestUtil.getRandomString("ImpstoffChargeVorher");
        String nameUpdated = TestUtil.getRandomString("ImpstoffChargeNachher");
        boolean activeAfter = false;

        ImpfstoffCharge impfstoffCharge = ImpfstoffChargeBuilder.create(
            builder -> builder
                .withID(ID.random(ImpfstoffCharge.class))
                .withName(nameBefore)
                .forImpfstoff(impfstoff)
        );
        impfstoffChargeService.persist(impfstoffCharge);

        ImpfstoffChargeSaveJsonSpec saveJson = ImpfstoffChargeBuilder.createJaxSpec(
                builder -> builder
                    .withName(nameUpdated)
                    .forImpfstoff(impfstoff))
            .getData();

        // ACT
        saveJson.setAktiv(activeAfter);

        ImpfstoffChargeJsonSpec result = api
            .impfstoffChargeResourceUpdateImpfstoffCharge()
            .impfstoffIdPath(impfstoff.getId().getId())
            .impfstoffChargeIdPath(impfstoffCharge.getId().getId())
            .body(saveJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(ImpfstoffChargeJsonSpec.class);

        ImpfstoffChargeListJsonSpec impfstoffChargeJsonSpec = api
            .impfstoffChargeResourceFindAllByImpfstoff()
            .impfstoffIdPath(result.getImpfstoffId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(ImpfstoffChargeListJsonSpec.class);

        List<ImpfstoffChargeJsonSpec> result2 = impfstoffChargeJsonSpec.getImpfstoffChargeList();

        // ASSERT
        Assertions.assertThat(result.getImpfstoffId()).isNotNull();
        Assertions.assertThat(result.getData().getName()).isEqualTo(nameUpdated);
        Assertions.assertThat(result.getData().getAktiv()).isEqualTo(activeAfter);

        Assertions.assertThat(result2).isNotNull()
            .isNotEmpty().hasSize(1)
            .anyMatch(jsonSpec -> nameUpdated.equals(jsonSpec.getData().getName()));
    }
}
