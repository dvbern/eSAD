package ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.LehrpersonBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.LehrpersonApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.LehrpersonJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.LehrpersonListJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.LehrpersonOptionListJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.LehrpersonSaveJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@RequiredArgsConstructor
class LehrpersonResourceITest extends AbstractITest {

    private final LehrpersonApiSpec api = LehrpersonApiSpec.lehrperson(RequestSpecUtil.quarkusSpec());

    private final LehrpersonService lehrpersonService;
    private final FixtureService fixtureService;

    @Test
    @TestAsAdminBS
    void findAllAktivByKlasse() {
        // ARRANGE
        Klasse klasse1 = fixtureService.createKlasse();
        Lehrperson lehrer1Klasse1 = fixtureService.createLehrperson(klasse1);
        Lehrperson lehrer2Klasse1 = fixtureService.createLehrperson(klasse1);

        // Inaktive Lehrperson
        Lehrperson lpInaktiv = LehrpersonBuilder.create(
            (LehrpersonBuilder builder) -> {
                builder.forKlasse(klasse1);
                builder.withExternalImportInfo(
                    UUID.randomUUID().toString(),
                    false,
                    getAppClock().offsetDateTime().now());
            });
        lehrpersonService.persist(lpInaktiv);

        Klasse klasse2 = fixtureService.createKlasse();
        Lehrperson lehrerKlasse2 = fixtureService.createLehrperson(klasse2);

        // ACT
        LehrpersonListJsonSpec result = api
            .lehrpersonResourceFindAllAktivByKlasse()
            .klasseIDPath(klasse1.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(LehrpersonListJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getLehrpersonList()).hasSize(2);
        assertThat(result.getLehrpersonList())
            .anyMatch(kontaktPerson -> lehrer1Klasse1.getId().getId().equals(kontaktPerson.getId())
                && lehrer1Klasse1.getName().equals(kontaktPerson.getData().getName())
                && lehrer1Klasse1.getVorname().equals(kontaktPerson.getData().getVorname()))
            .anyMatch(kontaktPerson -> lehrer2Klasse1.getId().getId().equals(kontaktPerson.getId())
                && lehrer2Klasse1.getName().equals(kontaktPerson.getData().getName())
                && lehrer2Klasse1.getVorname().equals(kontaktPerson.getData().getVorname()))
            .noneMatch(kontaktPersonAndereSchule -> lehrerKlasse2.getId()
                .getId()
                .equals(kontaktPersonAndereSchule.getId())
                && lehrerKlasse2.getName().equals(kontaktPersonAndereSchule.getData().getName())
                && lehrerKlasse2.getVorname().equals(kontaktPersonAndereSchule.getData().getVorname()))
            .noneMatch(kontaktPersonInaktiv -> lpInaktiv.getId().getId().equals(kontaktPersonInaktiv.getId()));
    }

    @Test
    @TestAsAdminBS
    void findAllOptionsActive_withAndWithoutExclusion() {
        // ARRANGE
        Klasse klasse1 = fixtureService.createKlasse();
        fixtureService.createLehrperson(klasse1);

        Klasse klasse2 = fixtureService.createKlasse();
        fixtureService.createLehrperson(klasse2);

        int currentLehrpersonenSize = lehrpersonService.findAllOptionsActive(null).size();

        // ACT
        LehrpersonOptionListJsonSpec allOptions = api
            .lehrpersonResourceFindAllOptionsActive()
            .klasseIDPath(klasse1.getId().getId())
            .excludeKlasseQuery(false)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(LehrpersonOptionListJsonSpec.class);

        LehrpersonOptionListJsonSpec optionsExcluded = api
            .lehrpersonResourceFindAllOptionsActive()
            .klasseIDPath(klasse1.getId().getId())
            .excludeKlasseQuery(true)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(LehrpersonOptionListJsonSpec.class);

        // ASSERT
        assertThat(allOptions).isNotNull();
        assertThat(allOptions.getLehrpersonList()).hasSize(currentLehrpersonenSize);
        assertThat(optionsExcluded).isNotNull();
        assertThat(optionsExcluded.getLehrpersonList()).hasSize(currentLehrpersonenSize - 1);
    }

    @Test
    @TestAsAdminBS
    void getLehrperson() {
        // ARRANGE
        Klasse klasse = fixtureService.createKlasse();
        Lehrperson lehrperson = fixtureService.createLehrperson(klasse);

        // ACT
        LehrpersonJsonSpec result = api
            .lehrpersonResourceGetLehrperson()
            .klasseIDPath(klasse.getId().getId())
            .lehrpersonIDPath(lehrperson.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(LehrpersonJsonSpec.class);

        // ASSERT
        assertThat(result)
            .isNotNull()
            .extracting(LehrpersonJsonSpec::getId).isEqualTo(lehrperson.getId().getId());
    }

    @Test
    @TestAsAdminBS
    void createLehrperson() {
        // ARRANGE
        Klasse klasse = fixtureService.createKlasse();
        LehrpersonSaveJsonSpec payload = LehrpersonBuilder.createSaveJsonSpec(
            builder -> builder
                .withID(ID.random(Lehrperson.class))
                .forKlasse(klasse));

        // ACT
        LehrpersonJsonSpec result = api
            .lehrpersonResourceCreateLehrperson()
            .klasseIDPath(klasse.getId().getId())
            .body(payload)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().as(LehrpersonJsonSpec.class);

        // ASSERT
        Optional<Lehrperson> lehrpersonOptional =
            lehrpersonService.findById(ID.of(UUID.fromString(result.getId()), Lehrperson.class));
        assertThat(lehrpersonOptional).isPresent();
        Lehrperson lehrperson = lehrpersonOptional.get();
        assertThat(lehrperson.getId()).isNotNull();
        assertThat(lehrperson.getExternalImportInfo()).isNull();
        assertThat(lehrperson.getName()).isEqualTo(payload.getName());
        assertThat(lehrperson.getVorname()).isEqualTo(payload.getVorname());
        Set<Klasse> klassen = fixtureService.findKlassenOfLehrperson(lehrperson.getId());
        assertThat(klassen)
            .isNotNull()
            .hasSize(1)
            .anyMatch(element -> element.getBezeichnung().equals(klasse.getBezeichnung()));
    }

    @Test
    @TestAsAdminBS
    void updateLehrperson() {
        // ARRANGE
        Klasse savedKlasse = fixtureService.createKlasse();
        Lehrperson savedLehrperson = fixtureService.createLehrperson(savedKlasse);
        assertThat(savedLehrperson).isNotNull();
        assertThat(savedLehrperson.getId()).isNotNull();
        assertThat(savedLehrperson.getExternalSystemIdOrNull()).isNull();
        assertThat(savedLehrperson.isManualOrAktivInExternalSystem()).isTrue();
        LehrpersonSaveJsonSpec payload = LehrpersonBuilder.createSaveJsonSpec(
            builder -> builder
                .forLehrperson(savedLehrperson)
                .withName("Neuer Name", "Neuer Vorname"));

        // ACT
        LehrpersonJsonSpec result = api
            .lehrpersonResourceUpdateLehrperson()
            .klasseIDPath(savedKlasse.getId().getId())
            .lehrpersonIDPath(savedLehrperson.getId().getId())
            .body(payload)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(LehrpersonJsonSpec.class);

        // ASSERT
        Optional<Lehrperson> lehrpersonOptional =
            lehrpersonService.findById(ID.of(UUID.fromString(result.getId()), Lehrperson.class));
        assertThat(lehrpersonOptional).isPresent();
        Lehrperson lehrperson = lehrpersonOptional.get();
        assertThat(lehrperson.getId()).isNotNull();
        assertThat(lehrperson.getName()).isEqualTo(payload.getName());
        assertThat(lehrperson.getVorname()).isEqualTo(payload.getVorname());
    }

    @Test
    @TestAsAdminBS
    void deleteLehrperson() {
        // ARRANGE
        Klasse savedKlasse = fixtureService.createKlasse();
        Lehrperson savedLehrperson = fixtureService.createLehrperson(savedKlasse);
        Optional<Lehrperson> kpsOptional = lehrpersonService.findById(savedLehrperson.getId());
        assertThat(kpsOptional).isPresent();

        // ACT
        api.lehrpersonResourceDeleteLehrperson()
            .klasseIDPath(savedKlasse.getId().getId())
            .lehrpersonIDPath(savedLehrperson.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());

        // ASSERT
        Optional<Lehrperson> lehrpersonRemoved = lehrpersonService.findById(savedLehrperson.getId());
        assertThat(lehrpersonRemoved).isNotPresent();
    }

    @Test
    @TestAsAdminBS
    void addLehrpersonToKlasse() {
        // ARRANGE
        Klasse klasse1 = fixtureService.createKlasse();
        Lehrperson lehrperson = fixtureService.createLehrperson(klasse1);
        Set<Klasse> klassenVorher = fixtureService.findKlassenOfLehrperson(lehrperson.getId());
        assertThat(klassenVorher).hasSize(1).containsExactly(klasse1);

        // ACT
        Klasse klasse2 = fixtureService.createKlasse();
        api.lehrpersonResourceAddLehrpersonToKlasse()
            .klasseIDPath(klasse2.getId().getId())
            .lehrpersonIDPath(lehrperson.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());

        // ASSERT
        Set<Klasse> klassenNachher = fixtureService.findKlassenOfLehrperson(lehrperson.getId());
        assertThat(klassenNachher).hasSize(2).containsExactlyInAnyOrder(klasse1, klasse2);
    }

    @Test
    @TestAsAdminBS
    void removeLehrpersonFromKlasse() {
        // ARRANGE
        Klasse klasse1 = fixtureService.createKlasse();
        Klasse klasse2 = fixtureService.createKlasse();
        Lehrperson lehrperson = fixtureService.createLehrperson(klasse1, klasse2);
        Set<Klasse> schulenVorher = fixtureService.findKlassenOfLehrperson(lehrperson.getId());
        assertThat(schulenVorher).hasSize(2).containsExactlyInAnyOrder(klasse1, klasse2);

        // ACT
        api.lehrpersonResourceRemoveLehrpersonFromKlasse()
            .klasseIDPath(klasse2.getId().getId())
            .lehrpersonIDPath(lehrperson.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());

        // ASSERT
        Set<Klasse> klassenNachRemove1 = fixtureService.findKlassenOfLehrperson(lehrperson.getId());
        assertThat(klassenNachRemove1).hasSize(1).containsExactly(klasse1);
    }

    @Test
    @TestAsAdminBS
    void removeLehrpersonFromKlasse_deleteLehrperson_if_klasse_is_empty() {
        // ARRANGE
        Klasse klasse1 = fixtureService.createKlasse();
        Klasse klasse2 = fixtureService.createKlasse();
        Lehrperson lehrperson = fixtureService.createLehrperson(klasse1, klasse2);
        Set<Klasse> schulenVorher = fixtureService.findKlassenOfLehrperson(lehrperson.getId());
        assertThat(schulenVorher).hasSize(2).containsExactlyInAnyOrder(klasse1, klasse2);

        // ACT
        api.lehrpersonResourceRemoveLehrpersonFromKlasse()
            .klasseIDPath(klasse2.getId().getId())
            .lehrpersonIDPath(lehrperson.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());

        // ASSERT
        Set<Klasse> klassenNachRemove1 = fixtureService.findKlassenOfLehrperson(lehrperson.getId());
        assertThat(klassenNachRemove1).hasSize(1).containsExactly(klasse1);

        @NonNull Optional<Lehrperson> optionalLehrperson = lehrpersonService.findById(lehrperson.getId());
        assertThat(optionalLehrperson).isPresent();

        // ACT
        api.lehrpersonResourceRemoveLehrpersonFromKlasse()
            .klasseIDPath(klasse1.getId().getId())
            .lehrpersonIDPath(lehrperson.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());

        optionalLehrperson = lehrpersonService.findById(lehrperson.getId());
        assertThat(optionalLehrperson).isNotPresent();
    }
}
