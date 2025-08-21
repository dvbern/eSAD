package ch.dvbern.oss.hemed.esc.api.features.schule;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.schule.kontaktpersonschule.KontaktPersonSchule;
import ch.dvbern.oss.hemed.esc.api.features.schule.kontaktpersonschule.KontaktPersonSchuleService;
import ch.dvbern.oss.hemed.esc.api.features.schule.types.KontaktPersonSchuleFunktion;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.KontaktPersonSchuleBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.SchuleBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.KontaktpersonSchuleApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KontaktPersonSchuleJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KontaktPersonSchuleListJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KontaktPersonSchuleOptionListJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KontaktPersonSchuleSaveJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@RequiredArgsConstructor
class KontaktPersonSchuleResourceITest extends AbstractITest {

    private final KontaktpersonSchuleApiSpec api =
        KontaktpersonSchuleApiSpec.kontaktpersonSchule(RequestSpecUtil.quarkusSpec());

    private final KontaktPersonSchuleService kontaktPersonSchuleService;
    private final SchuleService schuleService;
    private final FixtureService fixtureService;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    @TestAsAdminBS
    void findAllActiveExcludeSchule() {
        // ARRANGE
        Schule schule1 = SchuleBuilder.create(
            builder -> builder
                .withID(ID.random(Schule.class))
                .withSchulhaus("SchulhausKontaktPersonen"));
        schuleService.persist(schule1);

        KontaktPersonSchule kps1 = KontaktPersonSchuleBuilder.create(
            builder -> builder
                .withID(ID.random(KontaktPersonSchule.class))
                .withName("Müller", "Kontaktperson Martin")
                .withFunktion(KontaktPersonSchuleFunktion.SCHULHAUSLEITUNG)
                .forSchule(schule1));
        kontaktPersonSchuleService.persist(kps1);

        KontaktPersonSchule kps2 = KontaktPersonSchuleBuilder.create(
            builder -> builder
                .withID(ID.random(KontaktPersonSchule.class))
                .withName("Meier", "Kontaktperson Kevin")
                .withFunktion(KontaktPersonSchuleFunktion.SEKRETARIAT)
                .forSchule(schule1));
        kontaktPersonSchuleService.persist(kps2);

        Schule schule2 = SchuleBuilder.create(
            builder -> builder
                .withID(ID.random(Schule.class))
                .withSchulhaus("SchulhausForTest"));
        schuleService.persist(schule2);

        KontaktPersonSchule kps3 = KontaktPersonSchuleBuilder.create(
            builder -> builder
                .withID(ID.random(KontaktPersonSchule.class))
                .withName("Mustermann", "Kontaktperson Max")
                .withFunktion(KontaktPersonSchuleFunktion.SCHULHAUSLEITUNG)
                .forSchule(schule2));
        kontaktPersonSchuleService.persist(kps3);

        // ACT
        KontaktPersonSchuleListJsonSpec result = api
            .kontaktPersonSchuleResourceFindAllAktivBySchule()
            .schuleIDPath(schule1.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(KontaktPersonSchuleListJsonSpec.class);

        KontaktPersonSchuleOptionListJsonSpec options = api.kontaktPersonSchuleResourceFindAllOptionsActive()
            .schuleIDPath(schule1.getId().getId())
            .excludeSchuleQuery(true)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(KontaktPersonSchuleOptionListJsonSpec.class);

        KontaktPersonSchuleOptionListJsonSpec optionsInklOwnKontaktPersonen = api.kontaktPersonSchuleResourceFindAllOptionsActive()
            .schuleIDPath(schule1.getId().getId())
            .excludeSchuleQuery(false)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(KontaktPersonSchuleOptionListJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getKontaktPersonenList()).hasSize(2);
        assertThat(result.getKontaktPersonenList())
            .anyMatch(kontaktPerson -> kps1.getId().getId().equals(kontaktPerson.getId())
                && kps1.getName().equals(kontaktPerson.getData().getName())
                && kps1.getVorname().equals(kontaktPerson.getData().getVorname()))
            .anyMatch(kontaktPerson -> kps2.getId().getId().equals(kontaktPerson.getId())
                && kps2.getName().equals(kontaktPerson.getData().getName())
                && kps2.getVorname().equals(kontaktPerson.getData().getVorname()))
            .noneMatch(kontaktPersonAndereSchule -> kps3.getId().getId().equals(kontaktPersonAndereSchule.getId())
                && kps3.getName().equals(kontaktPersonAndereSchule.getData().getName())
                && kps3.getVorname().equals(kontaktPersonAndereSchule.getData().getVorname()));

        assertThat(options).isNotNull();
        assertThat(options.getKontaktPersonList()).hasSize(1);

        assertThat(optionsInklOwnKontaktPersonen).isNotNull();
        assertThat(optionsInklOwnKontaktPersonen.getKontaktPersonList()).hasSize(3);
    }

    @Test
    @TestAsAdminBS
    void createKontaktPersonSchule() {
        // ARRANGE
        String vorname = "Kontaktperson Martin";
        String name = "Müller";

        Schule savedSchule = schuleService.persist(SchuleBuilder.defaultSchule());

        KontaktPersonSchuleSaveJsonSpec payload = KontaktPersonSchuleBuilder.createSaveJsonSpec(
            builder -> builder
                .withID(ID.random(KontaktPersonSchule.class))
                .withName(name, vorname)
                .withFunktion(KontaktPersonSchuleFunktion.SCHULHAUSLEITUNG)
                .forSchule(savedSchule));

        // ACT
        KontaktPersonSchuleJsonSpec result = api
            .kontaktPersonSchuleResourceCreateKontaktPersonSchule()
            .schuleIDPath(savedSchule.getId().getId())
            .body(payload)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().as(KontaktPersonSchuleJsonSpec.class);

        // ASSERT
        Optional<KontaktPersonSchule> kontaktPersonSchuleOptional =
            kontaktPersonSchuleService.findById(ID.of(UUID.fromString(result.getId()), KontaktPersonSchule.class));
        assertThat(kontaktPersonSchuleOptional).isPresent();
        KontaktPersonSchule kontaktPersonSchule = kontaktPersonSchuleOptional.get();
        assertThat(kontaktPersonSchule.getId()).isNotNull();
        assertThat(kontaktPersonSchule.getName()).isEqualTo(name);
        assertThat(kontaktPersonSchule.getVorname()).isEqualTo(vorname);
        assertThat(kontaktPersonSchule.getFunktion())
            .isEqualTo(KontaktPersonSchuleFunktion.SCHULHAUSLEITUNG);
        Set<Schule> schulenOfKP = fixtureService.findSchulenOfKontaktPerson(kontaktPersonSchule.getId());
        assertThat(schulenOfKP)
            .isNotNull()
            .isNotEmpty()
            .anyMatch(schule -> schule.getSchulhaus().equals(savedSchule.getSchulhaus()));
    }

    @Test
    @TestAsAdminBS
    void updateKontaktPersonSchule() {
        // ARRANGE
        ID<KontaktPersonSchule> kpsID = ID.random(KontaktPersonSchule.class);
        String oldVorname = "Kontaktperson Martin";
        String oldName = "Müller";
        String newVorname = "Kontaktperson Max";
        String newName = "Mustermann";
        KontaktPersonSchuleFunktion oldFunktion = KontaktPersonSchuleFunktion.SEKRETARIAT;
        KontaktPersonSchuleFunktion newFunktion = KontaktPersonSchuleFunktion.SCHULHAUSLEITUNG;
        Schule savedSchule = schuleService.persist(SchuleBuilder.defaultSchule());
        KontaktPersonSchule kps = KontaktPersonSchuleBuilder.create(
            builder -> builder
                .withID(kpsID)
                .withName(oldName, oldVorname)
                .withFunktion(oldFunktion)
                .forSchule(savedSchule));
        kontaktPersonSchuleService.persist(kps);

        KontaktPersonSchuleSaveJsonSpec payload = KontaktPersonSchuleBuilder.createSaveJsonSpec(
            builder -> builder
                .withID(kpsID)
                .withName(newName, newVorname)
                .withFunktion(newFunktion)
                .forSchule(SchuleBuilder.defaultSchule()));

        // ACT
        KontaktPersonSchuleJsonSpec result = api
            .kontaktPersonSchuleResourceUpdateKontaktPersonSchule()
            .schuleIDPath(kps.getSchulen().stream().findFirst().orElseThrow().getId().getId())
            .kontaktPersonSchuleIDPath(kps.getId().getId())
            .body(payload)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(KontaktPersonSchuleJsonSpec.class);

        // ASSERT
        Optional<KontaktPersonSchule> optionalKps =
            kontaktPersonSchuleService.findById(ID.of(UUID.fromString(result.getId()), KontaktPersonSchule.class));
        assertThat(optionalKps).isPresent();
        KontaktPersonSchule updatedKps = optionalKps.get();
        assertThat(updatedKps.getId()).isNotNull();
        assertThat(updatedKps.getId()).isEqualTo(kpsID);
        assertThat(updatedKps.getVorname()).isEqualTo(newVorname);
        assertThat(updatedKps.getName()).isEqualTo(newName);
        assertThat(updatedKps.getFunktion()).isEqualTo(newFunktion);
    }

    @Test
    @TestAsAdminBS
    void deleteKontaktPersonSchule() {
        // ARRANGE
        Schule savedSchule = schuleService.persist(SchuleBuilder.defaultSchule());
        KontaktPersonSchule kps = KontaktPersonSchuleBuilder.create(
            builder -> builder
                .withName("Müller", "Heinz Kontaktperson")
                .withFunktion(KontaktPersonSchuleFunktion.SEKRETARIAT)
                .forSchule(savedSchule));
        kontaktPersonSchuleService.persist(kps);
        Optional<KontaktPersonSchule> kpsOptional = kontaktPersonSchuleService.findById(kps.getId());
        assertThat(kpsOptional).isPresent();

        // ACT
        api.kontaktPersonSchuleResourceDeleteKontaktPersonSchule()
            .schuleIDPath(savedSchule.getId().getId())
            .kontaktPersonSchuleIDPath(kps.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());

        // ASSERT
        Optional<KontaktPersonSchule> kpsRemoved = kontaktPersonSchuleService.findById(kps.getId());
        assertThat(kpsRemoved).isNotPresent();
    }

    @Test
    @TestAsAdminBS
    void addKontaktPersonToSchule() {
        // ARRANGE
        Schule schule1 = fixtureService.createSchule();
        Schule schule2 = fixtureService.createSchule();
        KontaktPersonSchule kontaktPersonSchule = fixtureService.createKontaktPersonSchule(schule1);
        Set<Schule> schulenVorher = fixtureService.findSchulenOfKontaktPerson(kontaktPersonSchule.getId());
        assertThat(schulenVorher).hasSize(1).containsExactly(schule1);

        // ACT
        api.kontaktPersonSchuleResourceAddKontaktPersonSchuleToSchule()
            .schuleIDPath(schule2.getId().getId())
            .kontaktPersonSchuleIDPath(kontaktPersonSchule.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());

        // ASSERT
        Set<Schule> schulenNachher = fixtureService.findSchulenOfKontaktPerson(kontaktPersonSchule.getId());
        assertThat(schulenNachher).hasSize(2).containsExactlyInAnyOrder(schule1, schule2);
    }

    @Test
    @TestAsAdminBS
    void removeKontaktPersonFromSchule() {
        // ARRANGE
        Schule schule1 = fixtureService.createSchule();
        Schule schule2 = fixtureService.createSchule();
        KontaktPersonSchule kontaktPersonSchule = fixtureService.createKontaktPersonSchule(schule1, schule2);
        Set<Schule> schulenVorher = fixtureService.findSchulenOfKontaktPerson(kontaktPersonSchule.getId());
        assertThat(schulenVorher).hasSize(2).containsExactlyInAnyOrder(schule1, schule2);

        // ACT
        api.kontaktPersonSchuleResourceRemoveKontaktPersonSchuleFromSchule()
            .schuleIDPath(schule2.getId().getId())
            .kontaktPersonSchuleIDPath(kontaktPersonSchule.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());

        // ASSERT
        Set<Schule> schulenNachRemove1 = fixtureService.findSchulenOfKontaktPerson(kontaktPersonSchule.getId());
        assertThat(schulenNachRemove1).hasSize(1).containsExactly(schule1);
    }

    @Test
    @TestAsAdminBS
    void removeKontaktPersonFromSchule_deleteKontaktPerson_if_schule_is_empty() {
        // ARRANGE
        Schule schule1 = fixtureService.createSchule();
        Schule schule2 = fixtureService.createSchule();
        KontaktPersonSchule kontaktPersonSchule = fixtureService.createKontaktPersonSchule(schule1, schule2);
        Set<Schule> schulenVorher = fixtureService.findSchulenOfKontaktPerson(kontaktPersonSchule.getId());
        assertThat(schulenVorher).hasSize(2).containsExactlyInAnyOrder(schule1, schule2);

        // ACT
        api.kontaktPersonSchuleResourceRemoveKontaktPersonSchuleFromSchule()
            .schuleIDPath(schule2.getId().getId())
            .kontaktPersonSchuleIDPath(kontaktPersonSchule.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());

        // ASSERT
        Set<Schule> schulenNachRemove1 = fixtureService.findSchulenOfKontaktPerson(kontaktPersonSchule.getId());
        assertThat(schulenNachRemove1).hasSize(1).containsExactly(schule1);

        KontaktPersonSchule kontakt = kontaktPersonSchuleService.getById(kontaktPersonSchule.getId());
        assertThat(kontakt).isNotNull();

        // ACT
        api.kontaktPersonSchuleResourceRemoveKontaktPersonSchuleFromSchule()
            .schuleIDPath(schule1.getId().getId())
            .kontaktPersonSchuleIDPath(kontaktPersonSchule.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());

        Optional<KontaktPersonSchule> optionalKontaktPersonSchule = kontaktPersonSchuleService.findById(kontaktPersonSchule.getId());
        assertThat(optionalKontaktPersonSchule).isNotPresent();
    }
}
