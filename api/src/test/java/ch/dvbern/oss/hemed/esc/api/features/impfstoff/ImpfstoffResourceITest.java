package ch.dvbern.oss.hemed.esc.api.features.impfstoff;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.impfstoff.types.Krankheit;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.ImpfstoffBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.ImpfstoffChargeBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.ImpfstoffChargenApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.ImpfstoffeApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.EntityAktivStatusSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.EntityAktivStatusSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.ImpfstoffChargeJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.ImpfstoffImpfstoffChargeDataJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.ImpfstoffJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.ImpfstoffSaveJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class ImpfstoffResourceITest extends AbstractITest {

    private final ImpfstoffeApiSpec api = ImpfstoffeApiSpec.impfstoffe(RequestSpecUtil.quarkusSpec());
    private final ImpfstoffChargenApiSpec apiCharge =
        ImpfstoffChargenApiSpec.impfstoffChargen(RequestSpecUtil.quarkusSpec());
    private final ImpfstoffService impfstoffService;
    private final ImpfstoffChargeService impfstoffChargeService;
    private final FixtureService fixtureService;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    @TestAsAdminBS
    void findAllImpfstoff() {
        // ARRANGE
        Impfstoff impfstoff = ImpfstoffBuilder.create(
            builder -> builder
                .withID(ID.random(Impfstoff.class))
                .withName(TestUtil.getRandomString("MMR")));
        impfstoffService.persist(impfstoff);

        // ACT
        ImpfstoffImpfstoffChargeDataJsonSpec data = api
            .impfstoffResourceFindAllImpfstoffeAndChargen()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(ImpfstoffImpfstoffChargeDataJsonSpec.class);

        List<ImpfstoffJsonSpec> result = data.getImpfstoffeList();

        // ASSERT
        Assertions.assertThat(result).isNotNull()
            .isNotEmpty()
            .anyMatch(item -> impfstoff.getId().getId().equals(item.getId())
                && impfstoff.getName().equals(item.getData().getName()));
    }

    @Test
    @TestAsAdminBS
    void findImpfstoffChargen() {
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

        // ACT
        ImpfstoffImpfstoffChargeDataJsonSpec data = api
            .impfstoffResourceFindAllImpfstoffeAndChargen()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(ImpfstoffImpfstoffChargeDataJsonSpec.class);

        List<ImpfstoffChargeJsonSpec> result = data.getChargenList();

        // ASSERT
        Assertions.assertThat(result)
            .isNotNull()
            .hasSizeGreaterThanOrEqualTo(3)
            .anyMatch(kp -> charge1Impfstoff1.getId().getId().equals(kp.getId())
                && charge1Impfstoff1.getName().equals(kp.getData().getName()))
            .anyMatch(kp -> charge2Impfstoff1.getId().getId().equals(kp.getId())
                && charge2Impfstoff1.getName().equals(kp.getData().getName()))
            .anyMatch(kp -> chargeAndererImpfstoff.getId().getId().equals(kp.getId())
                && chargeAndererImpfstoff.getName().equals(kp.getData().getName()));
    }

    @Test
    @TestAsAdminBS
    void removeImpfstoff() {
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
            .impfstoffResourceRemoveImpfstoff()
            .impfstoffeIdPath(impfstoff1.getId().getUUID())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());

        // ASSERT
        Optional<Impfstoff> impfstoffAfterDelete = impfstoffService.findById(impfstoff1.getId());
        Assertions.assertThat(impfstoffAfterDelete).isNotPresent();

        Optional<ImpfstoffCharge> impstoffChargeAfterDelete =
            impfstoffChargeService.findById(charge1Impfstoff1.getId());
        Assertions.assertThat(impstoffChargeAfterDelete).isNotPresent();

        Optional<ImpfstoffCharge> impstoffCharge2AfterDelete =
            impfstoffChargeService.findById(charge2Impfstoff1.getId());
        Assertions.assertThat(impstoffCharge2AfterDelete).isNotPresent();

        Optional<Impfstoff> impfstoffAndereAfterDelete = impfstoffService.findById(impfstoffAnderer.getId());
        Assertions.assertThat(impfstoffAndereAfterDelete).isPresent();

        Optional<ImpfstoffCharge> impstoffChargeAndererAfterDelete =
            impfstoffChargeService.findById(chargeAndererImpfstoff.getId());
        Assertions.assertThat(impstoffChargeAndererAfterDelete).isPresent();
    }

    @Test
    @TestAsAdminBS
    void createImpfstoff() {
        // ARRANGE
        String randomName = TestUtil.getRandomString("Myimpfstoff");
        Krankheit inKrankheit = Krankheit.MENINGOKOKKEN_B;

        ImpfstoffSaveJsonSpec saveJson = ImpfstoffBuilder.createJaxSpec(
                builder -> builder
                    .withName(randomName)
                    .forKrankheiten(inKrankheit))
            .getData();

        //ACT
        ImpfstoffJsonSpec result = api
            .impfstoffResourceCreateImpfstoff()
            .body(saveJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().as(ImpfstoffJsonSpec.class);

        ImpfstoffImpfstoffChargeDataJsonSpec data = api
            .impfstoffResourceFindAllImpfstoffeAndChargen()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(ImpfstoffImpfstoffChargeDataJsonSpec.class);

        List<ImpfstoffJsonSpec> result2 = data.getImpfstoffeList();
        Impfstoff impfstoff = impfstoffService.getById(ID.of(UUID.fromString(result.getId()), Impfstoff.class));

        //ASSERT
        Assertions.assertThat(impfstoff.getId()).isNotNull();
        Assertions.assertThat(impfstoff.getName()).isEqualTo(randomName);
        Assertions.assertThat(impfstoff.getKrankheiten()).contains(Krankheit.MENINGOKOKKEN_B);
        Assertions.assertThat(impfstoff.getImpfstoffAktivStatus()).isEqualTo(EntityAktivStatus.AKTIV);

        Assertions.assertThat(result2)
            .isNotNull().isNotEmpty()
            .anyMatch(item -> impfstoff.getId().getId().equals(item.getId())
                && impfstoff.getName().equals(item.getData().getName()))
            .anyMatch(item -> impfstoff.getId().getId().equals(item.getId())
                && impfstoff.getKrankheiten().contains(Krankheit.MENINGOKOKKEN_B));
    }

    @Test
    @TestAsAdminBS
    void updateImpfstoff() {
        // ARRANGE
        String nameBefore = TestUtil.getRandomString("ImpstoffVorher");
        String nameUpdated = TestUtil.getRandomString("ImpstoffNachher");
        Impfstoff impfstoff = ImpfstoffBuilder.create(
            builder -> builder
                .withID(ID.random(Impfstoff.class))
                .withName(nameBefore)
                .forKrankheiten(Krankheit.MASERN)
        );
        impfstoffService.persist(impfstoff);

        ImpfstoffSaveJsonSpec saveJson = ImpfstoffBuilder.createJaxSpec(
                builder -> builder
                    .withID(impfstoff.getId())
                    .withName(nameUpdated)
                    .forKrankheiten(Krankheit.HPV))
            .getData();

        //ACT
        ImpfstoffJsonSpec result = api
            .impfstoffResourceUpdateImpfstoff()
            .impfstoffeIdPath(impfstoff.getId().getId())
            .body(saveJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(ImpfstoffJsonSpec.class);

        ImpfstoffImpfstoffChargeDataJsonSpec data = api
            .impfstoffResourceFindAllImpfstoffeAndChargen()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(ImpfstoffImpfstoffChargeDataJsonSpec.class);

        List<ImpfstoffJsonSpec> result2 = data.getImpfstoffeList();

        // ASSERT
        Impfstoff impfstoff2 = impfstoffService.getById(ID.of(UUID.fromString(result.getId()), Impfstoff.class));
        Assertions.assertThat(impfstoff2.getId()).isNotNull();
        Assertions.assertThat(result.getData().getName()).isEqualTo(nameUpdated);
        Assertions.assertThat(impfstoff2.getKrankheiten()).isNotNull();
        Assertions.assertThat(impfstoff2.getKrankheiten()).isNotEmpty();
        Assertions.assertThat(impfstoff2.getKrankheiten()).contains(Krankheit.HPV);
        Assertions.assertThat(impfstoff2.getImpfstoffAktivStatus()).isEqualTo(EntityAktivStatus.AKTIV);

        Assertions.assertThat(result2)
            .isNotNull().isNotEmpty()
            .anyMatch(item -> impfstoff2.getId().getId().equals(item.getId())
                && impfstoff2.getName().equals(item.getData().getName()))
            .anyMatch(item -> impfstoff2.getId().getId().equals(item.getId())
                && impfstoff2.getKrankheiten().contains(Krankheit.HPV));
    }

    @Test
    @TestAsAdminBS
    void deactivateImpfstoff_shouldDeactiveAllChargenOfImpfstoff() {
        // ARRANGE
        Impfstoff impfstoff = fixtureService.createImpfstoff();
        ImpfstoffCharge charge1 = fixtureService.createImpfstoffCharge(impfstoff, "Charge 1");
        ImpfstoffCharge charge2 = fixtureService.createImpfstoffCharge(impfstoff, "Charge 2");
        EntityAktivStatusSaveJsonSpec saveJsonSpec = new EntityAktivStatusSaveJsonSpec(EntityAktivStatusSpec.INAKTIV);

        Assertions.assertThat(impfstoff.getImpfstoffAktivStatus()).isEqualTo(EntityAktivStatus.AKTIV);
        Assertions.assertThat(charge1.isAktiv()).isTrue();
        Assertions.assertThat(charge2.isAktiv()).isTrue();

        // ACT
        ImpfstoffJsonSpec resultImpfung = api.impfstoffResourceUpdateImpfstoffAktivStatus()
            .impfstoffIdPath(impfstoff.getId().getId())
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(ImpfstoffJsonSpec.class);

        List<ImpfstoffCharge> impfstoffChargen = impfstoffChargeService.findAllByImpfstoff(impfstoff.getId());

        // ASSERT
        Assertions.assertThat(resultImpfung).isNotNull();
        Assertions.assertThat(resultImpfung.getId()).isEqualTo(impfstoff.getId().getId());
        Assertions.assertThat(resultImpfung.getData().getImpfstoffAktivStatus())
            .isEqualTo(EntityAktivStatusSpec.INAKTIV);
        Assertions.assertThat(impfstoffChargen).hasSize(2);
        Assertions.assertThat(impfstoffChargen.get(0).getImpfstoff().getId()).isEqualTo(impfstoff.getId());
        Assertions.assertThat(impfstoffChargen.get(0).isAktiv()).isFalse();
        Assertions.assertThat(impfstoffChargen.get(1).getImpfstoff().getId()).isEqualTo(impfstoff.getId());
        Assertions.assertThat(impfstoffChargen.get(1).isAktiv()).isFalse();
    }

    @Test
    @TestAsAdminBS
    void deactivateImpfstoff_shouldDeactiveAllChargenOfImpfstoff_reactivateImpfstoffShouldNotReactivateChargen() {
        // ARRANGE
        Impfstoff impfstoff = fixtureService.createImpfstoff();
        ImpfstoffCharge charge1 = fixtureService.createImpfstoffCharge(impfstoff, "Charge 1");
        ImpfstoffCharge charge2 = fixtureService.createImpfstoffCharge(impfstoff, "Charge 2");
        EntityAktivStatusSaveJsonSpec saveJsonSpecDeactivate =
            new EntityAktivStatusSaveJsonSpec(EntityAktivStatusSpec.INAKTIV);
        EntityAktivStatusSaveJsonSpec saveJsonSpecActivate =
            new EntityAktivStatusSaveJsonSpec(EntityAktivStatusSpec.AKTIV);

        Assertions.assertThat(impfstoff.getImpfstoffAktivStatus()).isEqualTo(EntityAktivStatus.AKTIV);
        Assertions.assertThat(charge1.isAktiv()).isTrue();
        Assertions.assertThat(charge2.isAktiv()).isTrue();

        // ACT - deactivate
        ImpfstoffJsonSpec resultImpfung = api.impfstoffResourceUpdateImpfstoffAktivStatus()
            .impfstoffIdPath(impfstoff.getId().getId())
            .body(saveJsonSpecDeactivate)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(ImpfstoffJsonSpec.class);

        List<ImpfstoffCharge> impfstoffChargen = impfstoffChargeService.findAllByImpfstoff(impfstoff.getId());

        // ASSERT - deactivated
        Assertions.assertThat(resultImpfung).isNotNull();
        Assertions.assertThat(resultImpfung.getId()).isEqualTo(impfstoff.getId().getId());
        Assertions.assertThat(resultImpfung.getData().getImpfstoffAktivStatus())
            .isEqualTo(EntityAktivStatusSpec.INAKTIV);
        Assertions.assertThat(impfstoffChargen).hasSize(2);
        Assertions.assertThat(impfstoffChargen.get(0).getImpfstoff().getId()).isEqualTo(impfstoff.getId());
        Assertions.assertThat(impfstoffChargen.get(0).isAktiv()).isFalse();
        Assertions.assertThat(impfstoffChargen.get(1).getImpfstoff().getId()).isEqualTo(impfstoff.getId());
        Assertions.assertThat(impfstoffChargen.get(1).isAktiv()).isFalse();

        // ACT - activate
        ImpfstoffJsonSpec resultImpfung2 = api.impfstoffResourceUpdateImpfstoffAktivStatus()
            .impfstoffIdPath(impfstoff.getId().getId())
            .body(saveJsonSpecActivate)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(ImpfstoffJsonSpec.class);

        List<ImpfstoffCharge> impfstoffChargen2 = impfstoffChargeService.findAllByImpfstoff(impfstoff.getId());

        // ASSERT - activated
        Assertions.assertThat(resultImpfung2).isNotNull();
        Assertions.assertThat(resultImpfung2.getId()).isEqualTo(impfstoff.getId().getId());
        Assertions.assertThat(resultImpfung2.getData().getImpfstoffAktivStatus())
            .isEqualTo(EntityAktivStatusSpec.AKTIV);
        Assertions.assertThat(impfstoffChargen2).hasSize(2);
        Assertions.assertThat(impfstoffChargen2.get(0).getImpfstoff().getId()).isEqualTo(impfstoff.getId());
        Assertions.assertThat(impfstoffChargen2.get(0).isAktiv()).isFalse();
        Assertions.assertThat(impfstoffChargen2.get(1).getImpfstoff().getId()).isEqualTo(impfstoff.getId());
        Assertions.assertThat(impfstoffChargen2.get(1).isAktiv()).isFalse();
    }

}
