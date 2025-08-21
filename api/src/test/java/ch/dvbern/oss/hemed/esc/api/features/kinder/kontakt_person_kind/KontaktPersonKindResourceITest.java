package ch.dvbern.oss.hemed.esc.api.features.kinder.kontakt_person_kind;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.KontaktPersonKindBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.KontaktpersonKindApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KontaktPersonKindJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KontaktPersonKindListJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KontaktPersonKindOptionListJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KontaktPersonKindSaveJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
@RequiredArgsConstructor
class KontaktPersonKindResourceITest extends AbstractITest {

    private final KontaktpersonKindApiSpec api =
        KontaktpersonKindApiSpec.kontaktpersonKind(RequestSpecUtil.quarkusSpec());

    private final KontaktPersonKindService kontaktPersonKindService;
    private final FixtureService fixtureService;

    @Test
    @TestAsAdminBS
    void findAllAktivByKind() {
        // ARRANGE
        Kind kind1 = fixtureService.createKind();
        KontaktPersonKind vater1 = fixtureService.createKontaktPersonKind(kind1);
        KontaktPersonKind mutter1 = fixtureService.createKontaktPersonKind(kind1);
        KontaktPersonKind mutterInaktiv = KontaktPersonKindBuilder.create(
            builder -> builder
                .forKind(kind1)
                .withExternalImportInfo(UUID.randomUUID().toString(), false, getAppClock().offsetDateTime().now()));
        kontaktPersonKindService.persist(mutterInaktiv);
        Kind anotherKind = fixtureService.createKind();
        KontaktPersonKind anotherVater = fixtureService.createKontaktPersonKind(anotherKind);

        // ACT
        KontaktPersonKindListJsonSpec result = api
            .kontaktPersonKindResourceFindAktivByKind()
            .kindIDPath(kind1.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(KontaktPersonKindListJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getKontaktPersonenList()).hasSize(2);
        assertThat(result.getKontaktPersonenList())
            .anyMatch(kontaktPerson -> vater1.getId().getId().equals(kontaktPerson.getId())
                && vater1.getName().equals(kontaktPerson.getData().getName())
                && vater1.getVorname().equals(kontaktPerson.getData().getVorname()))
            .anyMatch(kontaktPerson -> mutter1.getId().getId().equals(kontaktPerson.getId())
                && mutter1.getName().equals(kontaktPerson.getData().getName())
                && mutter1.getVorname().equals(kontaktPerson.getData().getVorname()))
            .noneMatch(kontaktPersonAndereSchule -> anotherVater.getId()
                .getId()
                .equals(kontaktPersonAndereSchule.getId())
                && anotherVater.getName().equals(kontaktPersonAndereSchule.getData().getName())
                && anotherVater.getVorname().equals(kontaktPersonAndereSchule.getData().getVorname())
            )
            .noneMatch(kp -> mutterInaktiv.getId().getId().equals(kp.getId()));
    }

    @Test
    @TestAsAdminBS
    void getKontaktPersonKindById() {
        // ARRANGE
        ID<KontaktPersonKind> kontaktPersonKindID = ID.of(UUID.randomUUID(), KontaktPersonKind.class);
        Kind kind = fixtureService.createKind();
        KontaktPersonKind kontaktPersonKind = KontaktPersonKindBuilder.create(
            builder -> builder
                .forKind(kind)
                .withID(kontaktPersonKindID)
                .withName("Mustermann", "Max"));
        kontaktPersonKind = kontaktPersonKindService.persist(kontaktPersonKind);
        assertThat(kontaktPersonKind.getId()).isEqualTo(kontaktPersonKindID);
        assertThat(kontaktPersonKind.getName()).isEqualTo("Mustermann");
        assertThat(kontaktPersonKind.getVorname()).isEqualTo("Max");

        // ACT
        KontaktPersonKindJsonSpec result = api.kontaktPersonKindResourceGetKontaktPersonKind()
            .kindIDPath(kind.getId().getId())
            .kontaktPersonKindIDPath(kontaktPersonKindID.getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(KontaktPersonKindJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(kontaktPersonKindID.getId());
        assertThat(result.getData().getName()).isEqualTo(kontaktPersonKind.getName());
        assertThat(result.getData().getVorname()).isEqualTo(kontaktPersonKind.getVorname());
    }

    @Test
    @TestAsAdminBS
    void createKontaktPersonKind() {
        // ARRANGE
        Kind kind = fixtureService.createKind();
        KontaktPersonKindSaveJsonSpec payload = KontaktPersonKindBuilder.createJaxSpec(
            builder -> builder
                .forKind(kind)).getData();

        // ACT
        KontaktPersonKindJsonSpec result = api
            .kontaktPersonKindResourceCreateKontaktPersonKind()
            .kindIDPath(kind.getId().getId())
            .body(payload)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().as(KontaktPersonKindJsonSpec.class);

        // ASSERT
        KontaktPersonKind kontaktPersonKind =
            kontaktPersonKindService.getById(ID.of(UUID.fromString(result.getId()), KontaktPersonKind.class));
        assertThat(result.getId()).isNotNull();
        assertThat(result.getExternalImportInfoJson()).isNull();
        assertThat(result.getData().getName()).isEqualTo(payload.getName());
        assertThat(result.getData().getVorname()).isEqualTo(payload.getVorname());
        Set<Kind> kinder = fixtureService.findKinderOfElternperson(kontaktPersonKind.getId());
        assertThat(kinder)
            .isNotNull()
            .hasSize(1)
            .anyMatch(element -> element.getNameVorname().equals(kind.getNameVorname()));
    }

    @Test
    @TestAsAdminBS
    void updateKontaktPersonKind() {
        // ARRANGE
        Kind kind = fixtureService.createKind();
        KontaktPersonKind savedVater = fixtureService.createKontaktPersonKind(kind);
        assertThat(savedVater).isNotNull();
        assertThat(savedVater.getId()).isNotNull();
        assertThat(savedVater.getExternalSystemIdOrNull()).isNull();
        assertThat(savedVater.isManualOrAktivInExternalSystem()).isFalse();
        KontaktPersonKindSaveJsonSpec payload = KontaktPersonKindBuilder.createJaxSpec(
            builder -> builder
                .forKontaktPersonKind(savedVater)
                .withName("Neuer Name", "Neuer Vorname")).getData();

        // ACT
        KontaktPersonKindJsonSpec result = api
            .kontaktPersonKindResourceUpdateKontaktPersonKind()
            .kindIDPath(kind.getId().getId())
            .kontaktPersonKindIDPath(savedVater.getId().getId())
            .body(payload)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(KontaktPersonKindJsonSpec.class);

        // ASSERT
        KontaktPersonKind kontaktPersonKind =
            kontaktPersonKindService.getById(ID.of(UUID.fromString(result.getId()), KontaktPersonKind.class));
        assertThat(kontaktPersonKind.getId()).isNotNull();
        assertThat(kontaktPersonKind.getName()).isEqualTo(payload.getName());
        assertThat(kontaktPersonKind.getVorname()).isEqualTo(payload.getVorname());
    }

    @Test
    @TestAsAdminBS
    void deleteKontaktPersonKind() {
        // ARRANGE
        Kind savedKind = fixtureService.createKind();
        KontaktPersonKind savedVater = fixtureService.createKontaktPersonKind(savedKind);

        // ACT
        api.kontaktPersonKindResourceDeleteKontaktPersonKind()
            .kindIDPath(savedKind.getId().getId())
            .kontaktPersonKindIDPath(savedVater.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());

        // ASSERT
        ID<KontaktPersonKind> kpId = savedVater.getId();
        assertThrows(
            AppFailureException.class,
            () -> kontaktPersonKindService.getById(kpId)
        );
    }

    @Test
    @TestAsAdminBS
    void addKontaktPersonKindToKind() {
        // ARRANGE
        Kind kind1 = fixtureService.createKind();
        KontaktPersonKind kontaktPersonKind = fixtureService.createKontaktPersonKind(kind1);
        Set<Kind> kinderVorher = fixtureService.findKinderOfElternperson(kontaktPersonKind.getId());
        assertThat(kinderVorher).hasSize(1).containsExactly(kind1);

        // ACT
        Kind kind2 = fixtureService.createKind();
        api.kontaktPersonKindResourceAddKontaktPersonKindToKind()
            .kindIDPath(kind2.getId().getId())
            .kontaktPersonKindIDPath(kontaktPersonKind.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());

        // ASSERT
        Set<Kind> kinderNachher = fixtureService.findKinderOfElternperson(kontaktPersonKind.getId());
        assertThat(kinderNachher).hasSize(2).containsExactlyInAnyOrder(kind1, kind2);
    }

    @Test
    @TestAsAdminBS
    void removeKontaktPersonKindFromKind() {
        // ARRANGE
        Kind kind1 = fixtureService.createKind();
        Kind kind2 = fixtureService.createKind();
        KontaktPersonKind kontaktPersonKind = fixtureService.createKontaktPersonKind(kind1, kind2);
        Set<Kind> kinderVorher = fixtureService.findKinderOfElternperson(kontaktPersonKind.getId());
        assertThat(kinderVorher).hasSize(2).containsExactlyInAnyOrder(kind1, kind2);

        // ACT
        api.kontaktPersonKindResourceRemoveKontaktPersonKindFromKind()
            .kindIDPath(kind2.getId().getId())
            .kontaktPersonKindIDPath(kontaktPersonKind.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());

        // ASSERT
        Set<Kind> kinderNachher = fixtureService.findKinderOfElternperson(kontaktPersonKind.getId());
        assertThat(kinderNachher).hasSize(1).containsExactly(kind1);
    }

    @Test
    @TestAsAdminBS
    void removeKontaktPersonFromKind_deleteKontaktPerson_if_kind_is_empty() {
        // ARRANGE
        Kind kind1 = fixtureService.createKind();
        Kind kind2 = fixtureService.createKind();
        KontaktPersonKind kontaktPersonKind = fixtureService.createKontaktPersonKind(kind1, kind2);
        Set<Kind> kinderVorher = fixtureService.findKinderOfElternperson(kontaktPersonKind.getId());
        assertThat(kinderVorher).hasSize(2).containsExactlyInAnyOrder(kind1, kind2);

        // ACT
        api.kontaktPersonKindResourceRemoveKontaktPersonKindFromKind()
            .kindIDPath(kind2.getId().getId())
            .kontaktPersonKindIDPath(kontaktPersonKind.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());

        // ASSERT
        Set<Kind> kinderNachher = fixtureService.findKinderOfElternperson(kontaktPersonKind.getId());
        assertThat(kinderNachher).hasSize(1).containsExactly(kind1);

        KontaktPersonKind kontakt = kontaktPersonKindService.getById(kontaktPersonKind.getId());
        assertThat(kontakt).isNotNull();

        // ACT
        api.kontaktPersonKindResourceRemoveKontaktPersonKindFromKind()
            .kindIDPath(kind1.getId().getId())
            .kontaktPersonKindIDPath(kontaktPersonKind.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());

        // ASSERT
        Optional<KontaktPersonKind> optionalKontaktPersonKind =
            kontaktPersonKindService.findById(kontaktPersonKind.getId());
        assertThat(optionalKontaktPersonKind).isNotPresent();
    }

    @Test
    @TestAsAdminBS
    void findAllAktivOptions_withAndWithoutExclusion() {
        // ARRANGE
        Kind kind1 = fixtureService.createKind();
        fixtureService.createKontaktPersonKind(kind1);
        Kind kind2 = fixtureService.createKind();
        fixtureService.createKontaktPersonKind(kind2);

        int currentKontaktPersonen = kontaktPersonKindService.findAllAktivOptions(kind1.getId()).size();

        // ACT
        KontaktPersonKindOptionListJsonSpec resultWithExlusion =
            api.kontaktPersonKindResourceFindAllOptionsActive()
                .kindIDPath(kind1.getId().getId())
                .excludeKindQuery(true)
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract()
                .as(KontaktPersonKindOptionListJsonSpec.class);

        KontaktPersonKindOptionListJsonSpec resultWithoutExlusion =
            api.kontaktPersonKindResourceFindAllOptionsActive()
                .kindIDPath(kind1.getId().getId())
                .excludeKindQuery(false)
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract()
                .as(KontaktPersonKindOptionListJsonSpec.class);

        // ASSERT
        assertThat(resultWithExlusion).isNotNull();
        assertThat(resultWithExlusion.getKontaktPersonList()).hasSize(currentKontaktPersonen);

        assertThat(resultWithoutExlusion).isNotNull();
        assertThat(resultWithoutExlusion.getKontaktPersonList()).hasSize(currentKontaktPersonen + 1);
    }
}
