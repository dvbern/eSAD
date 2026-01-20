package ch.dvbern.oss.hemed.esc.api.features.schule;

import java.util.Optional;
import java.util.UUID;

import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.externalimportinfojson.ImportTyp;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.KlasseService;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.assertj.AppValidationErrorJsonSpecAssert;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.KlasseBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.SchuleBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.SchulenApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AppValidationErrorJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.EntityAktivStatusSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.EntityAktivStatusSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KlasseListJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KlasseOptionListJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.SchuleJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.SchuleListJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.SchuleOptionListJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.SchuleSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.ViolationJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@RequiredArgsConstructor
class SchuleResourceITest extends AbstractITest {

    private final SchulenApiSpec api = SchulenApiSpec.schulen(RequestSpecUtil.quarkusSpec());

    private final FixtureService fixtureService;
    private final SchuleService schuleService;
    private final KlasseService klasseService;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    @TestAsAdminBS
    void findAllSchulen() {
        // ARRANGE
        String quartierName = TestUtil.getRandomString("GBW");
        Schule schule = SchuleBuilder.create(
            builder -> builder
                .withID(ID.random(Schule.class))
                .withQuartier(fixtureService.createQuartier(quartierName))
                .withSchulhaus("TestSchulhaus"));
        schuleService.persist(schule);
        // ACT
        SchuleListJsonSpec result = api
            .schuleResourceFindAllSchulen()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(SchuleListJsonSpec.class);
        // ASSERT
        Assertions.assertThat(result.getSchulenList())
            .isNotEmpty()
            .anyMatch(jsonSpec -> schule.getId().getId().equals(jsonSpec.getId())
                && "TestSchulhaus".equals(jsonSpec.getData().getSchulhaus()));
    }

    @Test
    @TestAsAdminBS
    void findAllSchuleOptions() {

        // ARRANGE
        String quartierName = TestUtil.getRandomString("GBW");
        Schule schule = SchuleBuilder.create(
            builder -> builder
                .withID(ID.random(Schule.class))
                .withQuartier(fixtureService.createQuartier(quartierName))
                .withSchulhaus("TestSchulhaus"));
        schuleService.persist(schule);

        Schule schuleInaktiv = SchuleBuilder.create(
            builder -> builder
                .withID(ID.random(Schule.class))
                .withSchulhaus("DeaktivierteSchule")
                .withExternalImportInfo(
                    UUID.randomUUID().toString(),
                    false,
                    getAppClock().offsetDateTime().now(),
                    ImportTyp.ESCADA)
                .withEntityAktivStatus(EntityAktivStatus.INAKTIV));
        schuleService.persist(schuleInaktiv);

        // ACT
        SchuleOptionListJsonSpec result = api
            .schuleResourceFindAllOptions()
            .onlyActiveQuery(false)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(SchuleOptionListJsonSpec.class);
        // ASSERT
        Assertions.assertThat(result.getSchulenList())
            .isNotEmpty()
            .anyMatch(jsonSpec -> schule.getId().getId().equals(jsonSpec.getId())
                && "TestSchulhaus".equals(jsonSpec.getBezeichnung()))
            .anyMatch(jsonSpec -> schuleInaktiv.getId().getId().equals(jsonSpec.getId()));
    }

    @Test
    @TestAsAdminBS
    void findAllAktivSchuleOptions() {
        // ARRANGE
        Schule schuleInaktiv = SchuleBuilder.create(
            builder -> builder
                .withID(ID.random(Schule.class))
                .withSchulhaus("DeaktivierteSchule")
                .withExternalImportInfo(
                    UUID.randomUUID().toString(),
                    false,
                    getAppClock().offsetDateTime().now(),
                    ImportTyp.ESCADA)
                .withEntityAktivStatus(EntityAktivStatus.INAKTIV));
        schuleService.persist(schuleInaktiv);

        Schule schuleAktiv = SchuleBuilder.create(
            builder -> builder
                .withID(ID.random(Schule.class))
                .withSchulhaus("Aktive Schule")
                .withExternalImportInfo(
                    UUID.randomUUID().toString(),
                    true,
                    getAppClock().offsetDateTime().now(),
                    ImportTyp.ESCADA)
                .withEntityAktivStatus(EntityAktivStatus.AKTIV));
        schuleService.persist(schuleAktiv);

        Schule schuleAktivInternal = SchuleBuilder.create(
            builder -> builder
                .withID(ID.random(Schule.class))
                .withSchulhaus("Aktive Schule Internal")
                .withEntityAktivStatus(EntityAktivStatus.AKTIV));
        schuleService.persist(schuleAktivInternal);

        // ACT
        SchuleOptionListJsonSpec result = api
            .schuleResourceFindAllOptions()
            .onlyActiveQuery(true)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(SchuleOptionListJsonSpec.class);
        // ASSERT
        Assertions.assertThat(result.getSchulenList())
            .filteredOn(schuleOptionSpec -> schuleOptionSpec.getId().equals(schuleInaktiv.getId().getId()))
            .isEmpty();

        Assertions.assertThat(result.getSchulenList())
            .filteredOn(schuleOptionSpec -> schuleOptionSpec.getId().equals(schuleAktiv.getId().getId()))
            .isNotEmpty().hasSize(1);

        Assertions.assertThat(result.getSchulenList())
            .filteredOn(schuleOptionSpec -> schuleOptionSpec.getId().equals(schuleAktivInternal.getId().getId()))
            .isNotEmpty().hasSize(1);
    }

    @ParameterizedTest
    @CsvSource({
        "d6bff17c-a9e2-45ff-bff1-7ca9e265ffc5, Allgemeine Gewerbeschule Basel",
        "1727c8ac-5e00-4455-a7c8-ac5e00f4551f, Berufsfachschule Basel",
        "5ff2707c-303d-4618-b270-7c303da618b0, Fachmaturitätsschule",
        "6dde6527-e296-495c-9e65-27e296e95c4d, Gymnasium Bäumlihof",
        "3a688b01-7fd2-41ec-a88b-017fd281ec21, Gymnasium Kirschgarten / MfB",
        "78c2faf9-bc6d-4fcc-82fa-f9bc6dffccff, Gymnasium Kirschgarten / Passerelle",
        "d947ae8d-6ba2-427f-87ae-8d6ba2727f1f, Gymnasium Kirschgarten",
        "005750c6-c373-4774-9750-c6c373d774f1, Gymnasium Leonhard",
        "4f622ad8-b25f-4f6d-a22a-d8b25f5f6dc4, Gymnasium am Münsterplatz",
        "1e7d5454-0a4d-4ef1-bd54-540a4d3ef138, Wirtschaftsgymnasium und Wirtschaftsmittelschule",
        "b0a067bf-a9b0-4e33-a067-bfa9b0fe333b, Zentrum für Brückenangebote"
    })
    @TestAsAdminBS
    void findAllSchuleOptionsAndCheckThatDummySchulenArePresent(String schuleId, String schuleName) {
        // ARRANGE

        // ACT
        SchuleOptionListJsonSpec result = api
            .schuleResourceFindAllOptions()
            .onlyActiveQuery(true)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(SchuleOptionListJsonSpec.class);
        // ASSERT
        Assertions.assertThat(result.getSchulenList())
            .isNotEmpty()
            .anyMatch(jsonSpec -> schuleId.equals(jsonSpec.getId())
                && schuleName.equals(jsonSpec.getBezeichnung()));
    }

    @Test
    @TestAsAdminBS
    void findKlassenForSchule() {
        // ARRANGE
        Schule schule1 = schuleService.persist(SchuleBuilder.defaultSchule());
        Klasse klasse1Schule1 = KlasseBuilder.create(
            builder -> builder
                .forSchule(schule1)
                .withBezeichnung("4c"));
        Klasse klasse2Schule1 = KlasseBuilder.create(
            builder -> builder
                .forSchule(schule1)
                .withBezeichnung("5a"));
        klasseService.persist(klasse1Schule1);
        klasseService.persist(klasse2Schule1);
        Schule schule2 = schuleService.persist(SchuleBuilder.defaultSchule());
        Klasse andereKlasse = KlasseBuilder.create(
            builder -> builder
                .forSchule(schule2)
                .withBezeichnung("3c"));
        klasseService.persist(andereKlasse);
        // ACT
        KlasseListJsonSpec result = api
            .schuleResourceFindSchuleKlassen()
            .schuleIdPath(schule1.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(KlasseListJsonSpec.class);
        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getKlassenList())
            .hasSize(2)
            .anyMatch(kp -> klasse1Schule1.getId().getId().equals(kp.getId())
                && klasse1Schule1.getBezeichnung().equals(kp.getData().getBezeichnung()))
            .anyMatch(kp -> klasse2Schule1.getId().getId().equals(kp.getId())
                && klasse2Schule1.getBezeichnung().equals(kp.getData().getBezeichnung()))
            .noneMatch(kp -> andereKlasse.getId().getId().equals(kp.getId())
                && andereKlasse.getBezeichnung().equals(kp.getData().getBezeichnung()));
    }

    @Test
    @TestAsAdminBS
    void findKlassenOptionsForSchule() {
        // ARRANGE
        Schule schule1 = schuleService.persist(SchuleBuilder.defaultSchule());
        Klasse klasse1Schule1 = KlasseBuilder.create(
            builder -> builder
                .forSchule(schule1)
                .withBezeichnung("4c"));
        Klasse klasse2Schule1 = KlasseBuilder.create(
            builder -> builder
                .forSchule(schule1)
                .withBezeichnung("5a"));
        klasseService.persist(klasse1Schule1);
        klasseService.persist(klasse2Schule1);
        Schule schule2 = schuleService.persist(SchuleBuilder.defaultSchule());
        Klasse andereKlasse = KlasseBuilder.create(
            builder -> builder
                .forSchule(schule2)
                .withBezeichnung("3c"));
        klasseService.persist(andereKlasse);
        // ACT
        KlasseOptionListJsonSpec result = api
            .schuleResourceFindSchuleKlassenOptions()
            .onlyActiveQuery(true)
            .schuleIdPath(schule1.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(KlasseOptionListJsonSpec.class);
        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getKlassenList())
            .hasSize(2)
            .anyMatch(kp -> klasse1Schule1.getId().getId().equals(kp.getId())
                && klasse1Schule1.getBezeichnung().equals(kp.getBezeichnung()))
            .anyMatch(kp -> klasse2Schule1.getId().getId().equals(kp.getId())
                && klasse2Schule1.getBezeichnung().equals(kp.getBezeichnung()))
            .noneMatch(kp -> andereKlasse.getId().getId().equals(kp.getId())
                && andereKlasse.getBezeichnung().equals(kp.getBezeichnung()));
    }

    @Test
    @TestAsAdminBS
    void findKlassenOptionsForSchuleIncludingDeactivated() {
        // ARRANGE
        Schule schule = schuleService.persist(SchuleBuilder.defaultSchule());
        Klasse aktiveKlasse = KlasseBuilder.create(
            builder -> builder
                .forSchule(schule)
                .withBezeichnung("4c")
                .withEntityAktivStatus(EntityAktivStatus.AKTIV));
        Klasse inaktiveKlasse = KlasseBuilder.create(
            builder -> builder
                .forSchule(schule)
                .withBezeichnung("5a")
                .withEntityAktivStatus(EntityAktivStatus.INAKTIV));
        klasseService.persist(aktiveKlasse);
        klasseService.persist(inaktiveKlasse);
        // ACT
        KlasseOptionListJsonSpec result = api
            .schuleResourceFindSchuleKlassenOptions()
            .onlyActiveQuery(false)
            .schuleIdPath(schule.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(KlasseOptionListJsonSpec.class);
        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getKlassenList())
            .hasSize(2)
            .anyMatch(kp -> aktiveKlasse.getId().getId().equals(kp.getId())
                && aktiveKlasse.getBezeichnung().equals(kp.getBezeichnung()))
            .anyMatch(kp -> inaktiveKlasse.getId().getId().equals(kp.getId()));
    }

    @Test
    @TestAsAdminBS
    void getSchule() {
        // ARRANGE
        Schule schule = schuleService.persist(SchuleBuilder.defaultSchule());

        // ACT
        SchuleJsonSpec result = api
            .schuleResourceGetSchule()
            .schuleIdPath(schule.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(SchuleJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isEqualTo(schule.getId().getId());
        Assertions.assertThat(result.getData().getSchulhaus()).isEqualTo(schule.getSchulhaus());
        Assertions.assertThat(result.getExternalImportInfoJson()).isNull();
    }

    @Test
    @TestAsAdminBS
    void createSchule() {
        // ARRANGE
        ID<Schule> schuleId = ID.random(Schule.class);
        String name = "TestSchulhaus";
        String schulStandort = "TestSchulstandort";
        EmailAddress emailAddress = EmailAddress.fromString("test.schulhaus@mailbucket.dvbern.ch");
        String quartierName = TestUtil.getRandomString("GBW");
        SchuleSaveJsonSpec payload = SchuleBuilder.createSaveJaxSpec(
            schuleBuilder -> schuleBuilder
                .withID(schuleId)
                .withSchulhaus(name)
                .withSchulstandort(schulStandort)
                .withQuartier(fixtureService.createQuartier(quartierName))
                .withEmail(emailAddress));

        // ACT
        SchuleJsonSpec result = api
            .schuleResourceCreateSchule()
            .body(payload)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(SchuleJsonSpec.class);

        // ASSERT
        Optional<Schule> schuleOptional = schuleService.findById(ID.of(UUID.fromString(result.getId()), Schule.class));
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getExternalImportInfoJson()).isNull();
        Assertions.assertThat(schuleOptional).isPresent();
        Schule schule = schuleOptional.get();
        Assertions.assertThat(result.getData().getSchulhaus()).isNotNull();
        Assertions.assertThat(schule.getId()).isNotNull();
        Assertions.assertThat(schule.getSchulhaus()).isEqualTo(name);
        Assertions.assertThat(schule.getSchulstandort()).isEqualTo(schulStandort);
        Assertions.assertThat(schule.getEmail()).isEqualTo(emailAddress);
        Assertions.assertThat(schule.getSchuleAktivStatus()).isEqualTo(EntityAktivStatus.AKTIV);
    }

    @Test
    @TestAsAdminBS
    void updateSchule() {
        // ARRANGE
        ID<Schule> schuleId = ID.random(Schule.class);
        String nameBefore = "TestSchulhaus";
        String nameAfter = "TestSchulhausNewName";
        EmailAddress emailBefore = EmailAddress.fromString("test.schulhaus@mailbucket.dvbern.ch");
        EmailAddress emailAfter = EmailAddress.fromString("test.newSchulhaus@mailbucket.dvbern.ch");
        String schulStandort = "TestSchulstandort";
        String quartierName = TestUtil.getRandomString("GBW");
        Schule schule = SchuleBuilder.create(
            builder -> builder
                .withID(schuleId)
                .withSchulhaus(nameBefore)
                .withSchulstandort(schulStandort)
                .withQuartier(fixtureService.createQuartier(quartierName))
                .withEmail(emailBefore));
        Assertions.assertThat(schule.getSchulhaus()).isEqualTo(nameBefore);
        Assertions.assertThat(schule.getSchulstandort()).isEqualTo(schulStandort);
        Assertions.assertThat(schule.getEmail()).isEqualTo(emailBefore);
        Assertions.assertThat(schule.getExternalSystemIdOrNull()).isNull();
        Assertions.assertThat(schule.getSchuleAktivStatus()).isEqualTo(EntityAktivStatus.AKTIV);
        schuleService.persist(schule);

        SchuleSaveJsonSpec saveJsonSpec = SchuleBuilder.createSaveJaxSpec(
            schuleBuilder -> schuleBuilder
                .withID(schuleId)
                .withSchulhaus(nameAfter)
                .withSchulstandort(schulStandort)
                .withEmail(emailAfter));

        // ACT
        SchuleJsonSpec result = api
            .schuleResourceUpdateSchule()
            .schuleIdPath(schuleId.getId())
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(SchuleJsonSpec.class);

        // ASSERT
        Optional<Schule> schuleOptional = schuleService.findById(ID.of(UUID.fromString(result.getId()), Schule.class));
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(schuleOptional).isPresent();
        Schule schuleUpdated = schuleOptional.get();
        Assertions.assertThat(schuleUpdated.getId()).isEqualTo(schuleId);
        Assertions.assertThat(schuleUpdated.getSchulhaus()).isEqualTo(nameAfter);
        Assertions.assertThat(schuleUpdated.getSchulstandort()).isEqualTo(schulStandort);
        Assertions.assertThat(schuleUpdated.getEmail()).isEqualTo(emailAfter);
        Assertions.assertThat(schuleUpdated.getSchuleAktivStatus()).isEqualTo(EntityAktivStatus.AKTIV);
    }

    @Test
    @TestAsAdminBS
    void changeSchuleAktivStatus_shouldWork() {
        // ARRANGE
        Schule existingSchule = fixtureService.createSchule("TestSchuleToDeactivate");
        ID<Schule> existingSchuleId = existingSchule.getId();
        assertThat(existingSchule.getSchuleAktivStatus()).isEqualTo(EntityAktivStatus.AKTIV);
        EntityAktivStatusSpec desiredStatus = EntityAktivStatusSpec.INAKTIV;
        EntityAktivStatusSaveJsonSpec saveJson = new EntityAktivStatusSaveJsonSpec(EntityAktivStatusSpec.INAKTIV);

        // ACT
        SchuleJsonSpec result = api.schuleResourceUpdateSchuleAktivStatus()
            .schuleIdPath(existingSchuleId.getId())
            .body(saveJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(SchuleJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(existingSchuleId.getId());
        assertThat(result.getExternalImportInfoJson()).isNull();
        assertThat(result.getData().getSchuleAktivStatus()).isEqualTo(desiredStatus);
    }

    @Test
    @TestAsAdminBS
    void changeSchuleAktivStatus_shouldWork_ifExternalAndInactive() {
        // ARRANGE
        Schule existingSchule = fixtureService.createSchuleExternal("TestSchuleToDeactivate", false, ImportTyp.ESCADA);
        ID<Schule> existingSchuleId = existingSchule.getId();
        assertThat(existingSchule.getSchuleAktivStatus()).isEqualTo(EntityAktivStatus.AKTIV);
        assertThat(existingSchule.getExternalImportInfo()).isNotNull();
        assertThat(existingSchule.isAktivInExternalSystem()).isFalse();
        EntityAktivStatusSpec desiredStatus = EntityAktivStatusSpec.INAKTIV;
        EntityAktivStatusSaveJsonSpec saveJson = new EntityAktivStatusSaveJsonSpec(EntityAktivStatusSpec.INAKTIV);

        // ACT
        SchuleJsonSpec result = api.schuleResourceUpdateSchuleAktivStatus()
            .schuleIdPath(existingSchuleId.getId())
            .body(saveJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(SchuleJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(existingSchuleId.getId());
        assertThat(result.getExternalImportInfoJson()).isNotNull();
        assertThat(result.getExternalImportInfoJson().getAktivInExternalSystem()).isFalse();
        assertThat(result.getData().getSchuleAktivStatus()).isEqualTo(desiredStatus);
    }

    @Test
    @TestAsAdminBS
    void changeSchuleAktivStatus_shouldNotWork_ifExternalAndActive() {
        Schule existingSchule = fixtureService.createSchuleExternal("TestSchuleToDeactivate", true, ImportTyp.ESCADA);
        ID<Schule> existingSchuleId = existingSchule.getId();
        assertThat(existingSchule.getSchuleAktivStatus()).isEqualTo(EntityAktivStatus.AKTIV);
        assertThat(existingSchule.getExternalImportInfo()).isNotNull();
        assertThat(existingSchule.isAktivInExternalSystem()).isTrue();
        EntityAktivStatusSaveJsonSpec saveJson = new EntityAktivStatusSaveJsonSpec(EntityAktivStatusSpec.INAKTIV);

        // ACT
        AppValidationErrorJsonSpec error = api.schuleResourceUpdateSchuleAktivStatus()
            .schuleIdPath(existingSchuleId.getId())
            .body(saveJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract().as(AppValidationErrorJsonSpec.class);

        // ASSERT
        assertThat(error).isNotNull();
        ViolationJsonSpec violation = error.getViolations().getFirst();
        assertThat(violation).isNotNull();
        assertThat(violation.getMessage()).contains(
            "Importierte Daten können nicht deaktiviert werden (Objekt Schule mit ID " + existingSchuleId + ").");
    }

    @Test
    @TestAsAdminBS
    void removeSchule_should_work() {
        // ARRANGE
        ID<Schule> schuleId = ID.random(Schule.class);
        String quartierName = TestUtil.getRandomString("GBW");
        Schule schule = SchuleBuilder.create(
            builder -> builder
                .withID(schuleId)
                .withQuartier(fixtureService.createQuartier(quartierName))
                .withSchulhaus("TestSchulhausToRemove"));
        schuleService.persist(schule);
        Optional<Schule> schuleOptional = schuleService.findById(schuleId);
        Assertions.assertThat(schuleOptional).isPresent();

        // ACT
        api.schuleResourceRemoveSchule()
            .schuleIdPath(schuleId.getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());

        // ASSERT
        Optional<Schule> schuleRemoved = schuleService.findById(schuleId);
        Assertions.assertThat(schuleRemoved).isNotPresent();
    }

    @Test
    @TestAsAdminBS
    void removeSchule_withExternalSystemId_should_not_work() {
        // ARRANGE
        ID<Schule> schuleId = ID.random(Schule.class);
        String externalSystemId = ID.random(Schule.class).getId();
        Schule schule = SchuleBuilder.create(
            builder -> builder
                .withID(schuleId)
                .withQuartier(fixtureService.createQuartier(TestUtil.getRandomString("GBW")))
                .withSchulhaus("TestSchulhausToRemove")
                .withExternalImportInfo(
                    externalSystemId,
                    true,
                    getAppClock().offsetDateTime().now(),
                    ImportTyp.ESCADA));
        schuleService.persist(schule);
        Optional<Schule> schuleOptional = schuleService.findById(schuleId);
        Assertions.assertThat(schuleOptional).isPresent();

        // ACT
        AppValidationErrorJsonSpec error = api.schuleResourceRemoveSchule()
            .schuleIdPath(schuleId.getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract().as(AppValidationErrorJsonSpec.class);

        // ASSERT
        Schule schuleTriedToDelete = schuleService.getById(schuleId);
        Assertions.assertThat(schuleTriedToDelete).isNotNull();
        Assertions.assertThat(schuleTriedToDelete.getExternalSystemIdOrNull()).isEqualTo(externalSystemId);
        Assertions.assertThat(schuleTriedToDelete.getId()).isEqualTo(schuleId);

        AppValidationErrorJsonSpecAssert.assertThatAppValidationErrorJsonSpec(error)
            .extractingExactlyOneViolation()
            .hasRawMessageContaining("kann nicht gelöscht werden, da sie von Escada stammt.");

    }

    @Test
    @TestAsAdminBS
    void removeSchule_withFK_References_should_not_work() {
        // ARRANGE
        ID<Schule> schuleId = ID.random(Schule.class);
        ID<Klasse> klasseID = ID.random(Klasse.class);
        Schule schule = SchuleBuilder.create(
            builder -> builder
                .withID(schuleId)
                .withQuartier(fixtureService.createQuartier(TestUtil.getRandomString("GBW")))
                .withSchulhaus("TestSchulhausToRemove"));
        Schule persistedSchule = schuleService.persist(schule);
        Klasse klasse = KlasseBuilder.create(
            builder -> builder
                .forSchule(persistedSchule)
                .withID(klasseID)
                .withBezeichnung("Klasse 4a"));
        Klasse persistedKlasse = klasseService.persist(klasse);

        // ACT
        AppValidationErrorJsonSpec error = api.schuleResourceRemoveSchule()
            .schuleIdPath(schuleId.getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract().as(AppValidationErrorJsonSpec.class);

        // ASSERT
        Schule schuleTriedToDelete = schuleService.getById(schuleId);
        Assertions.assertThat(schuleTriedToDelete).isNotNull();
        Assertions.assertThat(schuleTriedToDelete.getId()).isEqualTo(schuleId);
        Assertions.assertThat(persistedKlasse).isNotNull();
        Assertions.assertThat(persistedKlasse.getSchule().getId()).isEqualTo(schuleId);

        AppValidationErrorJsonSpecAssert.assertThatAppValidationErrorJsonSpec(error)
            .extractingExactlyOneViolation()
            .hasRawMessageContaining(
                "Das Objekt konnte nicht gelöscht werden, da es von einem anderen Objekt referenziert wird.");
    }
}
