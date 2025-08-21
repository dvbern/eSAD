package ch.dvbern.oss.hemed.esc.api.features.schule;

import java.util.Optional;
import java.util.UUID;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.KindService;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.KlasseService;
import ch.dvbern.oss.hemed.esc.api.features.schule.types.Klassenstufe;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.assertj.AppValidationErrorJsonSpecAssert;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.KindBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.KlasseBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.SchuleBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.KlassenApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AppValidationErrorJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.EntityAktivStatusSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.EntityAktivStatusSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KlasseJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KlasseListJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KlasseSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KlassenstufeSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.ViolationJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static ch.dvbern.oss.hemed.esc.api.testutil.builder.KlasseBuilder.KLASSENTYP_SEKE;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@RequiredArgsConstructor
class KlasseResourceITest extends AbstractITest {

    private final KlassenApiSpec api = KlassenApiSpec.klassen(RequestSpecUtil.quarkusSpec());

    private final KlasseService klasseService;
    private final SchuleService schuleService;
    private final KindService kindService;
    private final FixtureService fixtureService;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    @TestAsAdminBS
    void findAllKlassen() {
        // ARRANGE
        Schule schule = schuleService.persist(SchuleBuilder.defaultSchule());
        Klasse klasse = klasseService.persist(KlasseBuilder.create(
            builder -> builder
                .withID(ID.random(Klasse.class))
                .forSchule(schule)
                .withBezeichnung("Klasse 3a")));
        // ACT
        KlasseListJsonSpec result = api
            .klasseResourceFindAllKlassen()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(KlasseListJsonSpec.class);
        // ASSERT
        Assertions.assertThat(result.getKlassenList())
            .isNotEmpty()
            .anyMatch(jsonSpec -> klasse.getId().getId().equals(jsonSpec.getId())
                && "Klasse 3a".equals(jsonSpec.getData().getBezeichnung()));
    }

    @Test
    @TestAsAdminBS
    void createKlasse() {
        // ARRANGE
        Schule schule = schuleService.persist(SchuleBuilder.defaultSchule());
        String name = TestUtil.getRandomString("MyKlasse");
        KlasseSaveJsonSpec saveJsonSpec = KlasseBuilder.createSaveJaxSpec(
            klasseBuilder -> klasseBuilder
                .withBezeichnung(name)
                .withKlassenstufe(Klassenstufe.PRIMAR_KLASSE_1)
                .withKlasseSchulart(KlasseBuilder.KLASSENTYP_SEKE)
                .forSchule(schule));

        //ACT
        KlasseJsonSpec result = api
            .klasseResourceCreateKlasse()
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().as(KlasseJsonSpec.class);

        // ASSERT
        Klasse klasseReadOption = klasseService.getById(ID.of(UUID.fromString(result.getId()), Klasse.class));
        Assertions.assertThat(klasseReadOption.getId()).isNotNull();
        Assertions.assertThat(klasseReadOption.getExternalImportInfo()).isNull();
        Assertions.assertThat(klasseReadOption.getBezeichnung()).isEqualTo(name);
        Assertions.assertThat(klasseReadOption.getKlassenstufe()).isEqualTo(Klassenstufe.PRIMAR_KLASSE_1);
        Assertions.assertThat(klasseReadOption.getKlasseSchulart()).isEqualTo(KLASSENTYP_SEKE);
        Assertions.assertThat(klasseReadOption.getSchule()).isEqualTo(schule);

        KlasseJsonSpec klasseJsonSpec = api.klasseResourceFindKlasse()
            .klasseIdPath(result.getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(KlasseJsonSpec.class);
        Assertions.assertThat(klasseJsonSpec.getId()).isNotNull();
        Assertions.assertThat(klasseJsonSpec.getData().getBezeichnung()).isEqualTo(name);
        Assertions.assertThat(klasseJsonSpec.getData().getKlassenstufe()).isEqualTo(KlassenstufeSpec.PRIMAR_KLASSE_1);
        Assertions.assertThat(klasseJsonSpec.getData().getKlasseSchulart()).isEqualTo(KlasseBuilder.KLASSENTYP_SEKE);
        Assertions.assertThat(klasseJsonSpec.getData().getSchule().getId()).isEqualTo(schule.getId().getId());
        Assertions.assertThat(klasseJsonSpec.getData().getKlasseAktivStatus()).isEqualTo(EntityAktivStatusSpec.AKTIV);
    }

    @Test
    @TestAsAdminBS
    void updateKlasse() {
        // ARRANGE
        Klasse klasse = fixtureService.createKlasse();
        Assertions.assertThat(klasse).isNotNull();
        Assertions.assertThat(klasse.getExternalSystemIdOrNull()).isNull();
        Assertions.assertThat(klasse.isManualOrAktivInExternalSystem()).isTrue();
        Assertions.assertThat(klasse.getKlasseAktivStatus()).isEqualTo(EntityAktivStatus.AKTIV);

        // ACT
        String bezeichnungAfter = TestUtil.getRandomString("KlasseNachher");
        KlasseSaveJsonSpec saveJsonSpec = KlasseBuilder.createSaveJaxSpec(
            klasseBuilder -> klasseBuilder
                .forKlasse(klasse)
                .withBezeichnung(bezeichnungAfter)
                .withKlasseSchulart(KlasseBuilder.KLASSENTYP_SEKE)
                .withKlassenstufe(Klassenstufe.KINDERGARTEN_1)
        );
        KlasseJsonSpec result = api
            .klasseResourceUpdateKlasse()
            .klasseIdPath(klasse.getId().getId())
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(KlasseJsonSpec.class);

        // ASSERT
        KlasseJsonSpec klasseJsonSpec = api.klasseResourceFindKlasse()
            .klasseIdPath(result.getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(KlasseJsonSpec.class);

        Assertions.assertThat(klasseJsonSpec.getData().getBezeichnung()).isEqualTo(bezeichnungAfter);
        Assertions.assertThat(klasseJsonSpec.getData().getKlasseSchulart()).isEqualTo(KLASSENTYP_SEKE);
        Assertions.assertThat(klasseJsonSpec.getData().getKlassenstufe()).isEqualTo(KlassenstufeSpec.KINDERGARTEN_1);
        Assertions.assertThat(klasseJsonSpec.getData().getKlasseAktivStatus()).isEqualTo(EntityAktivStatusSpec.AKTIV);
    }

    @Test
    @TestAsAdminBS
    void changeSchuleAktivStatus_shouldWork() {
        // ARRANGE
        Klasse existingKlasse = fixtureService.createKlasse("TestKlasseToDeactivate");
        ID<Klasse> existingKlasseId = existingKlasse.getId();
        assertThat(existingKlasse.getKlasseAktivStatus()).isEqualTo(EntityAktivStatus.AKTIV);
        EntityAktivStatusSpec desiredStatus = EntityAktivStatusSpec.INAKTIV;
        EntityAktivStatusSaveJsonSpec saveJson = new EntityAktivStatusSaveJsonSpec(EntityAktivStatusSpec.INAKTIV);

        // ACT
        KlasseJsonSpec result = api.klasseResourceUpdateKlasseAktivStatus()
            .klasseIdPath(existingKlasseId.getId())
            .body(saveJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(KlasseJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(existingKlasseId.getId());
        assertThat(result.getExternalImportInfoJson()).isNull();
        assertThat(result.getData().getKlasseAktivStatus()).isEqualTo(desiredStatus);
    }

    @Test
    @TestAsAdminBS
    void changeSchuleAktivStatus_shouldWork_ifExternalAndInactive() {
        // ARRANGE
        Klasse existingKlasse = fixtureService.createKlasseExternal("TestKlasseToDeactivate", false);
        ID<Klasse> existingSchuleId = existingKlasse.getId();
        assertThat(existingKlasse.getKlasseAktivStatus()).isEqualTo(EntityAktivStatus.AKTIV);
        assertThat(existingKlasse.getExternalImportInfo()).isNotNull();
        assertThat(existingKlasse.getExternalImportInfo().isAktivInExternalSystem()).isFalse();
        EntityAktivStatusSpec desiredStatus = EntityAktivStatusSpec.INAKTIV;
        EntityAktivStatusSaveJsonSpec saveJson = new EntityAktivStatusSaveJsonSpec(EntityAktivStatusSpec.INAKTIV);

        // ACT
        KlasseJsonSpec result = api.klasseResourceUpdateKlasseAktivStatus()
            .klasseIdPath(existingSchuleId.getId())
            .body(saveJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(KlasseJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(existingSchuleId.getId());
        assertThat(result.getExternalImportInfoJson()).isNotNull();
        assertThat(result.getExternalImportInfoJson().getAktivInExternalSystem()).isFalse();
        assertThat(result.getData().getKlasseAktivStatus()).isEqualTo(desiredStatus);
    }

    @Test
    @TestAsAdminBS
    void changeKlasseAktivStatus_shouldNotWork_ifExternalAndActive() {
        Klasse existingKlasse = fixtureService.createKlasseExternal("TestKlasseToDeactivate", true);
        ID<Klasse> existingKlasseId = existingKlasse.getId();
        assertThat(existingKlasse.getKlasseAktivStatus()).isEqualTo(EntityAktivStatus.AKTIV);
        assertThat(existingKlasse.getExternalImportInfo()).isNotNull();
        assertThat(existingKlasse.getExternalImportInfo().isAktivInExternalSystem()).isTrue();
        EntityAktivStatusSaveJsonSpec saveJson = new EntityAktivStatusSaveJsonSpec(EntityAktivStatusSpec.INAKTIV);

        // ACT
        AppValidationErrorJsonSpec error = api.klasseResourceUpdateKlasseAktivStatus()
            .klasseIdPath(existingKlasseId.getId())
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
            "Importierte Daten können nicht deaktiviert werden (Objekt Klasse mit ID " + existingKlasseId + ").");
    }

    @Test
    @TestAsAdminBS
    void removeKlasse() {
        // ARRANGE
        Klasse persistedKlasse = fixtureService.createKlasse();
        Optional<Klasse> klasseBeforeDelete = klasseService.findById(persistedKlasse.getId());
        Assertions.assertThat(klasseBeforeDelete).isPresent();

        // ACT
        api.klasseResourceRemoveKlasse()
            .klasseIdPath(persistedKlasse.getId().getUUID())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());

        // ASSERT
        Optional<Klasse> klasseAfterDelete = klasseService.findById(persistedKlasse.getId());
        Assertions.assertThat(klasseAfterDelete).isNotPresent();
    }

    @Test
    @TestAsAdminBS
    void removeKlasse_withExternalSystemId_should_not_work() {
        // ARRANGE
        ID<Klasse> klasseID = ID.random(Klasse.class);
        String externalSystemId = ID.random(Klasse.class).getId();
        Schule schule = schuleService.persist(SchuleBuilder.defaultSchule());
        Klasse klasse = KlasseBuilder.create(
            builder -> builder
                .withID(klasseID)
                .forSchule(schule)
                .withBezeichnung("Klasse 4a")
                .withExternalImportInfo(externalSystemId, true, getAppClock().offsetDateTime().now())
        );
        Klasse persistedKlasse = klasseService.persist(klasse);

        // ACT
        AppValidationErrorJsonSpec error = api.klasseResourceRemoveKlasse()
            .klasseIdPath(persistedKlasse.getId().getUUID())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract().as(AppValidationErrorJsonSpec.class);

        // ASSERT
        Klasse klasseTriedToDelete = klasseService.getById(persistedKlasse.getId());
        Assertions.assertThat(klasseTriedToDelete).isNotNull();
        Assertions.assertThat(klasseTriedToDelete.getExternalSystemIdOrNull()).isEqualTo(externalSystemId);
        Assertions.assertThat(klasseTriedToDelete.getId()).isEqualTo(klasseID);

        AppValidationErrorJsonSpecAssert.assertThatAppValidationErrorJsonSpec(error)
            .extractingExactlyOneViolation()
            .hasRawMessageContaining("kann nicht gelöscht werden, da sie von Escada stammt.");
    }

    @Test
    @TestAsAdminBS
    void removeKlasse_withFK_References_should_not_work() {
        // ARRANGE
        ID<Kind> kindID = ID.random(Kind.class);
        Klasse persistedKlasse = fixtureService.createKlasse();
        Kind kind = KindBuilder.create(
            builder -> builder
                .withID(kindID)
                .withKlasse(persistedKlasse)
                .withName("Muster", "Max")
        );
        Kind persistedKind = kindService.persistWithoutDuplicateCheck(kind);

        // ACT
        AppValidationErrorJsonSpec error = api.klasseResourceRemoveKlasse()
            .klasseIdPath(persistedKlasse.getId().getUUID())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract().as(AppValidationErrorJsonSpec.class);

        // ASSERT
        Klasse klasseTriedToDelete = klasseService.getById(persistedKlasse.getId());
        Assertions.assertThat(klasseTriedToDelete).isNotNull();

        Assertions.assertThat(klasseTriedToDelete.getId()).isEqualTo(persistedKlasse.getId());
        Assertions.assertThat(persistedKind).isNotNull();
        Assertions.assertThat(persistedKind.getKlasse()).isNotNull();
        Assertions.assertThat(persistedKind.getKlasse().getId()).isEqualTo(persistedKlasse.getId());

        Assertions.assertThat(error.getViolations().getFirst().getMessage())
            .isEqualTo("Das Objekt konnte nicht gelöscht werden, da es von einem anderen Objekt referenziert wird.");
    }
}

