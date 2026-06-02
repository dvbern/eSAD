package ch.dvbern.oss.hemed.esc.api.features.benutzer;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Objects;

import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.oss.hemed.esc.api.features.organisation.Organisation;
import ch.dvbern.oss.hemed.esc.api.features.organisation.OrganisationService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.BenutzerBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBE;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAnotherSchularztBE;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSchularztBE;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSupportBE;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.BenutzerApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.BenutzerJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.BenutzerListJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.BenutzerOptionJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.BenutzerOptionListJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.BenutzerSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.PrincipalJsonSpec;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@DisplayNameGeneration(ReplaceUnderscores.class)
@RequiredArgsConstructor
class BenutzerResourceDezentralITest extends AbstractITest {

    @TestHTTPEndpoint(BenutzerResource.class)
    @TestHTTPResource
    URL url = null;
    private final BenutzerApiSpec api = BenutzerApiSpec.benutzer(RequestSpecUtil.quarkusSpec());

    private final BenutzerService benutzerService;
    private final BenutzerRepo benutzerRepo;
    private final OrganisationService organisationService;
    private final TenantProducer tenantProducer;
    private final BenutzerResourceHelper benutzerResourceHelper;
    private final FixtureService fixtureService;

    @Test
    @TestAsAdminBE
    void findAllBenutzerOptions() {
        // ACT
        var benList = api.benutzerResourceFindAllActiveBenutzerOptions()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_OK)
            .extract().as(BenutzerOptionListJsonSpec.class);

        // ASSERT
        Assertions.assertThat(benList.getBenutzerList()).isNotEmpty();
        // there must be at least the test admin present
        Assertions.assertThat(benList.getBenutzerList())
            .extracting(BenutzerOptionJsonSpec::getVorname)
            .contains("Adrian");
    }

    @Test
    @TestAsSchularztBE
    void findAllBenutzer_sameTenant_shouldSucceed() {
        // ARRANGE
        Benutzer schularzt = getSchularztBE();

        // ACT
        var benList = api.benutzerResourceFindAllBenutzer()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_OK)
            .extract().as(BenutzerListJsonSpec.class);

        // ASSERT
        Assertions.assertThat(benList.getBenutzerList())
            .isNotEmpty()
            .extracting(BenutzerJsonSpec::getData)
            .extracting(BenutzerSaveJsonSpec::getVorname)
            .contains(schularzt.getVorname());
    }

    @Test
    @TestAsSchularztBE
    void findAllBenutzerForOrganisation_sameOrg_shouldSucceed() {
        // ARRANGE
        Benutzer schularzt = getSchularztBE();
        Organisation organisation = Objects.requireNonNull(schularzt.getSingleOrganisation());

        Benutzer mpa = BenutzerBuilder.create(builder -> builder.withOrganisationRollen(organisation, List.of(FachRolle.MPA)));
        benutzerService.persist(mpa);

        // ACT
        var benList = api.benutzerResourceFindAllBenutzerForOrganisation()
            .organisationIDPath(organisation.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_OK)
            .extract().as(BenutzerListJsonSpec.class);

        // ASSERT
        Assertions.assertThat(benList.getBenutzerList())
            .extracting(BenutzerJsonSpec::getData)
            .extracting(BenutzerSaveJsonSpec::getVorname)
            .contains(schularzt.getVorname(), mpa.getVorname());
    }

    @Test
    @TestAsSchularztBE
    void findAllBenutzerForOrganisation_differentOrg_shouldFail() {
        // ARRANGE
        Benutzer otherSchularzt = benutzerResourceHelper.createSchularztAsOrganisationsChefDirectly(TenantEnum.BE);
        Organisation otherOrganisation = Objects.requireNonNull(otherSchularzt.getSingleOrganisation());

        // ACT & ASSERT
        api.benutzerResourceFindAllBenutzerForOrganisation()
            .organisationIDPath(otherOrganisation.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    @TestAsSchularztBE
    void getMyBenutzerData() {
        // ACT
        PrincipalJsonSpec principalJson = api.benutzerResourceGetMyBenutzerData()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_OK)
            .extract().as(PrincipalJsonSpec.class);

        // ASSERT
        Assertions.assertThat(principalJson).isNotNull();
        BenutzerJsonSpec benutzerJson = principalJson.getBenutzerJson();
        Assertions.assertThat(benutzerJson)
            .isNotNull()
            .extracting(BenutzerJsonSpec::getData)
            .extracting(BenutzerSaveJsonSpec::getEmail)
            .isNotNull()
            .isEqualTo(TestAsSchularztBE.USERNAME);
    }

    @Test
    @TestAsSupportBE
    void updateBenutzerAsSupport_only_allowed_fields_works() {
        // ARRANGE
        Benutzer benutzer = benutzerResourceHelper.createSchularztAsOrganisationsChefDirectly(TenantEnum.BE);
        Organisation organisation = Objects.requireNonNull(benutzer.getSingleOrganisation());

        String updatedVorname = "NewVorname";
        String updatedNachname = "NewNachname";
        BenutzerSaveJsonSpec saveJson = BenutzerBuilder.createJaxSpec(builder -> builder
            .forBenutzer(benutzer)
            .withVorname(updatedVorname)
            .withNachname(updatedNachname)
            .withOrganisationRollen(organisation, List.of(FachRolle.SCHULAERZTIN))
            .withBenutzerAktivStatus(EntityAktivStatus.AKTIV)).getData();

        // ACT
        BenutzerJsonSpec result = api.
            benutzerResourceUpdate()
            .benutzerIDPath(benutzer.getId().getId())
            .body(saveJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(BenutzerJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getData().getVorname()).isEqualTo(updatedVorname);
        assertThat(result.getData().getName()).isEqualTo(updatedNachname);
    }

    @Test
    @TestAsAnotherSchularztBE
    void updateBenutzer_differentOrg_shouldFail() {
        // ARRANGE
        Benutzer schularzt = getSchularztBE();

        BenutzerSaveJsonSpec saveJson = BenutzerBuilder.createJaxSpec(builder -> builder
            .forBenutzer(schularzt)
            .withVorname("Changed")).getData();

        // ACT & ASSERT
        api.benutzerResourceUpdate()
            .benutzerIDPath(schularzt.getId().getId())
            .body(saveJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    @TestAsSchularztBE
    void createBenutzer_differentOrg_shouldFail() {
        // ARRANGE
        Organisation otherOrganisation = Objects.requireNonNull(benutzerResourceHelper.createSchularztAsOrganisationsChefDirectly(TenantEnum.BE).getSingleOrganisation());

        BenutzerSaveJsonSpec saveJson = BenutzerBuilder.createJaxSpec(builder -> builder
            .withOrganisationRollen(otherOrganisation, List.of(FachRolle.MPA))
            .withNachname("New")
            .withVorname("User")
            .withEmail(EmailAddress.fromString("newuser@example.com"))
            .withBenutzerAktivStatus(EntityAktivStatus.AKTIV)).getData();

        // ACT & ASSERT
        api.benutzerResourceCreateBenutzer()
            .body(saveJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @NonNull
    private Benutzer getSchularztBE() {
        return fixtureService.readBenutzerWithBerechtigungen(TestAsSchularztBE.BENUTZER_ID_ID);
    }
}
