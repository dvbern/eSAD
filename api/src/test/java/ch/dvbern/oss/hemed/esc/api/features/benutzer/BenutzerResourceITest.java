package ch.dvbern.oss.hemed.esc.api.features.benutzer;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import jakarta.ws.rs.core.Response.Status;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumber;
import ch.dvbern.oss.hemed.esc.api.features.organisation.Organisation;
import ch.dvbern.oss.hemed.esc.api.features.organisation.OrganisationService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.AuthorizerService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSetting;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSettingsService;
import ch.dvbern.oss.hemed.esc.api.shared.settings.features.FeatureEnabledSetting;
import ch.dvbern.oss.hemed.esc.api.shared.types.EntityAktivStatus;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.BenutzerBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.TenantSettingBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSchularztBE;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSupportBE;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSupportBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.BenutzerApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AppFailureErrorJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.BenutzerJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.BenutzerListJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.BenutzerOptionJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.BenutzerOptionListJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.BenutzerSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.EntityAktivStatusSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.OrganisationJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.PrincipalJsonSpec;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;
import io.restassured.http.ContentType;
import io.restassured.response.ResponseBody;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@QuarkusTest
@TestAsAdminBS
@DisplayNameGeneration(ReplaceUnderscores.class)
@RequiredArgsConstructor
class BenutzerResourceITest {

    private static final String VORERFASST_KEYCLOAK_ID = "5d0c3f1e-8a2b-4c7d-9e6f-1a2b3c4d5e6f";
    private static final String VORERFASST_EMAIL = "anna.muster.vorerfasst@example.com";

    @TestHTTPEndpoint(BenutzerResource.class)
    @TestHTTPResource
    URL url = null;
    private final BenutzerApiSpec api = BenutzerApiSpec.benutzer(RequestSpecUtil.quarkusSpec());

    private final BenutzerService benutzerService;
    private final BenutzerRepo benutzerRepo;
    private final OrganisationService organisationService;
    private final TenantContext tenantContext;
    private final BenutzerResourceHelper benutzerResourceHelper;
    private final FixtureService fixtureService;
    private final AuthorizerService authorizerService;


    @Test
    void error_returns_a_response_of_type_failure() throws Exception {
        var response = given()
            .when()
            .contentType(ContentType.JSON)
            .get(url.toURI().resolve("error"))
            .then();

        response
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_BAD_REQUEST);

        var type = response.extract()
            .body().<String>path("type");
        assertThat(type)
            .isEqualTo("FAILURE");
    }

    @Test
    @TestAsAdminBS
    void findAllBenutzerOptions() {
        // ARRANGE

        // ACT
        var benList = api.benutzerResourceFindAllActiveBenutzerOptions()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_OK)
            .extract().as(BenutzerOptionListJsonSpec.class);

        // ASSERT
        Assertions.assertThat(benList.getBenutzerList()).isNotEmpty();
        // there must be at least a systemadmin present
        Assertions.assertThat(benList.getBenutzerList())
            .extracting(BenutzerOptionJsonSpec::getVorname)
            .contains("ESC-BASEL-SYSADM");
    }

    @Test
    @TestAsAdminBS
    void findAllBenutzerOptions_returns_only_active_benutzer() {
        // ARRANGE
        Benutzer benutzerActive = BenutzerBuilder.create(
            builder -> builder
                .withBenutzerAktivStatus(EntityAktivStatus.AKTIV)
        );
        Benutzer benutzerInactive = BenutzerBuilder.create(
            builder -> builder
                .withBenutzerAktivStatus(EntityAktivStatus.INAKTIV)
        );
        benutzerRepo.persist(benutzerActive);
        benutzerRepo.persist(benutzerInactive);
        // ACT
        var benList = api.benutzerResourceFindAllActiveBenutzerOptions()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_OK)
            .extract().as(BenutzerOptionListJsonSpec.class);

        // ASSERT
        Assertions.assertThat(benList.getBenutzerList())
            .isNotEmpty()
            .anyMatch(ben -> ben.getId().equals(benutzerActive.getId().getId()))
            .noneMatch(ben -> ben.getId().equals(benutzerInactive.getId().getId()));
    }

    @Test
    @TestAsAdminBS
    void findAllBenutzer() {
        // ARRANGE
        Benutzer benutzer = fixtureService.createBenutzerFuerZentraleOrganisation();

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
            .contains(benutzer.getVorname());

    }

    @Test
    @TestAsSupportBE
    void findAllBenutzerForOrganisation() {
        // ARRANGE
        Benutzer schularzt1 = benutzerResourceHelper.createSchularztAsOrganisationsChef(TenantEnum.BE);
        Organisation organisation1 = organisationService.getById(schularzt1
            .getOrganisationRollen().stream().findFirst().orElseThrow()
            .getOrganisation().getId());
        Benutzer schularzt2 = benutzerResourceHelper.createSchularztAsOrganisationsChef(TenantEnum.BE);
        Organisation organisation2 = organisationService.getById(schularzt2
            .getOrganisationRollen().stream().findFirst().orElseThrow()
            .getOrganisation().getId());

        Benutzer mpa1 = BenutzerBuilder.create(builder -> builder.withOrganisationRollen(organisation1, List.of(FachRolle.MPA)));
        Benutzer mpa2 = BenutzerBuilder.create(builder -> builder.withOrganisationRollen(organisation2, List.of(FachRolle.MPA)));
        benutzerService.persist(mpa1);
        benutzerService.persist(mpa2);

        // ACT
        var benList = api.benutzerResourceFindAllBenutzerForOrganisation()
            .organisationIDPath(organisation1.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_OK)
            .extract().as(BenutzerListJsonSpec.class);

        // ASSERT
        Assertions.assertThat(benList.getBenutzerList())
            .isNotEmpty()
            .hasSize(2)
            .extracting(BenutzerJsonSpec::getData)
            .extracting(BenutzerSaveJsonSpec::getVorname)
            .contains(schularzt1.getVorname())
            .contains(mpa1.getVorname())
            .doesNotContain(schularzt2.getVorname())
            .doesNotContain(mpa2.getVorname());
    }

    @Test
    @TestAsAdminBS
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
            .isEqualTo(TestAsAdminBS.USERNAME);
    }

    @Test
    @TestSecurity(user = VORERFASST_EMAIL)
    @OidcSecurity(
        claims = {
            @Claim(key = "sub", value = VORERFASST_KEYCLOAK_ID),
            @Claim(key = "tenant", value = "BE"),
            @Claim(key = "family_name", value = "Muster"),
            @Claim(key = "given_name", value = "Anna"),
            @Claim(key = "preferred_username", value = VORERFASST_EMAIL),
            @Claim(key = "email", value = VORERFASST_EMAIL),
        }
    )
    void getMyBenutzerData_links_the_vorerfassten_Benutzer_on_the_first_login() {
        // ARRANGE: registered in the admin GUI before the first login, so without KeycloakId and with the email in
        // lowercase, like Keycloak delivers it
        Benutzer vorerfassterBenutzer = fixtureService.createVorerfasstenBenutzer(
            TenantEnum.BE,
            EmailAddress.fromString(VORERFASST_EMAIL)
        );

        // ACT
        PrincipalJsonSpec principalJson = api.benutzerResourceGetMyBenutzerData()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(HttpURLConnection.HTTP_OK)
            .extract().as(PrincipalJsonSpec.class);

        // ASSERT
        assertThat(principalJson.getBenutzerJson())
            .extracting(BenutzerJsonSpec::getId)
            .isEqualTo(vorerfassterBenutzer.getId().getId());
        assertThat(benutzerService.getById(vorerfassterBenutzer.getId()).getKeycloakId())
            .isEqualTo(VORERFASST_KEYCLOAK_ID);
    }

    @TestAsAdminBS
    @ParameterizedTest
    @CsvSource({
        "AKTIV, INAKTIV",
        "INAKTIV, AKTIV",
        "INAKTIV, INAKTIV",
        "AKTIV, AKTIV"
    })
    void updateActive(EntityAktivStatus oldAktivStatus, EntityAktivStatus newAktivStatus) {
        // ARRANGE
        Benutzer benutzer = fixtureService.createBenutzerFuerZentraleOrganisation();
        BenutzerSaveJsonSpec saveJson = BenutzerBuilder.createJaxSpec(
            builder -> builder
                .forBenutzer(benutzer)
                .withBenutzerAktivStatus(newAktivStatus)
        ).getData();

        // ACT
        BenutzerJsonSpec result = api
            .benutzerResourceUpdateBenutzerAktivStatus()
            .benutzerIDPath(benutzer.getId().getId())
            .body(saveJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(BenutzerJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        if (oldAktivStatus == newAktivStatus) {
            assertThat(benutzer.getBenutzerAktivStatus()).isEqualTo(getAktivStatus(result.getData()
                .getBenutzerAktivStatus())).isEqualTo(oldAktivStatus).isEqualTo(newAktivStatus);
        } else {
            assertThat(getAktivStatus(result.getData()
                .getBenutzerAktivStatus())).isEqualTo(benutzer.getBenutzerAktivStatus());
            assertThat(getAktivStatus(result.getData().getBenutzerAktivStatus())).isEqualTo(newAktivStatus);
            assertThat(getAktivStatus(result.getData().getBenutzerAktivStatus())).isNotEqualTo(oldAktivStatus);
        }

    }

    @Test
    @TestAsSupportBE
    void updateBenutzerAsSupport_only_allowed_fields_works() {
        // ARRANGE
        Benutzer benutzer = benutzerResourceHelper.createSchularztAsOrganisationsChef(TenantEnum.BE);
        EmailAddress originalEmail = benutzer.getEmail();
        Organisation organisation = benutzer.getOrganisationRollen().stream().findFirst().get().getOrganisation();

        String updatedVorname = "NewVorname";
        String updatedNachname = "NewNachname";
        List<FachRolle> udpatedFachRollen = List.of(FachRolle.SCHULAERZTIN);
        EmailAddress updatedEmail = EmailAddress.fromString("updated@mailbucket.dvbern.ch");
        NormalizedPhoneNumber updatedTelefon = NormalizedPhoneNumber.parse("+41790000000");
        EntityAktivStatus updatedActive = EntityAktivStatus.AKTIV;
        BenutzerSaveJsonSpec saveJson = BenutzerBuilder.createJaxSpec(builder -> builder
            .forBenutzer(benutzer)
            .withVorname(updatedVorname)
            .withNachname(updatedNachname)
            .withEmail(updatedEmail)
            .withTelefon(updatedTelefon)
            .withOrganisationRollen(organisation, udpatedFachRollen)
            .withBenutzerAktivStatus(updatedActive)).getData();

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
        assertThat(result.getData().getEmail()).isNotEqualTo(updatedEmail.toString())
            .isEqualTo(originalEmail.toString());
        assertThat(result.getData().getTelefon()).isEqualTo(updatedTelefon.getFormattedPhoneNumber());
        List<FachRolle> fachRollen = result.getData().getOrganisationRollen().getFirst().getFachRollen().stream()
            .map(spec -> FachRolle.valueOf(spec.name()))
            .toList();
        assertThat(fachRollen).containsExactlyElementsOf(udpatedFachRollen);
        assertThat(getAktivStatus(result.getData().getBenutzerAktivStatus())).isEqualTo(updatedActive);
    }

    @Test
    @TestAsSupportBE
    void updateBenutzerAsSupport_tryAssigningSystemAdminRole_doesnotUpdateRoles() {
        // ARRANGE
        Benutzer benutzer = benutzerResourceHelper.createSchularztAsOrganisationsChef(TenantEnum.BE);

        List<FachRolle> udpatedFachRollen = List.of(FachRolle.SYSTEM_ADMIN);
        BenutzerSaveJsonSpec saveJson = BenutzerBuilder.createJaxSpec(builder -> builder
            .forBenutzer(benutzer)
            .withSystemRolle(udpatedFachRollen)).getData();

        // ACT
        AppFailureErrorJsonSpec error = api.
            benutzerResourceUpdate()
            .benutzerIDPath(benutzer.getId().getId())
            .body(saveJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
            .extract().as(AppFailureErrorJsonSpec.class);

        // ASSERT
        Assertions.assertThat(error).isNotNull();
        Assertions.assertThat(error.getSummary())
            .contains(
                "Aktualisieren der Fachrollen ist nicht erlaubt, der Benutzer hat nicht die entsprechende "
                    + "Berechtigung.");
    }

    @Test
    @TestAsSchularztBE
    void updateBenutzer_movingToDifferentOrganisation_requiresAuthorizationForNewOrganisation() {
        // ARRANGE
        Benutzer schularzt = fixtureService.readBenutzerWithBerechtigungen(TestAsSchularztBE.BENUTZER_ID_ID);
        Organisation ownOrganisation = Objects.requireNonNull(schularzt.getSingleOrganisation());

        // a benutzer that currently belongs to the schularzt's own organisation
        Benutzer benutzer = BenutzerBuilder.create(builder -> builder
            .withOrganisationRollen(ownOrganisation, List.of(FachRolle.MPA)));
        benutzerService.persist(benutzer);

        // a foreign organisation the schularzt is not authorized to write to
        Organisation foreignOrganisation = Objects.requireNonNull(
            benutzerResourceHelper.createSchularztAsOrganisationsChefDirectly(TenantEnum.BE).getSingleOrganisation());

        BenutzerSaveJsonSpec saveJson = BenutzerBuilder.createJaxSpec(builder -> builder
            .forBenutzer(benutzer)
            .withOrganisationRollen(foreignOrganisation, List.of(FachRolle.MPA))).getData();

        // ACT
        AppFailureErrorJsonSpec error = api.
            benutzerResourceUpdate()
            .benutzerIDPath(benutzer.getId().getId())
            .body(saveJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
            .extract().as(AppFailureErrorJsonSpec.class);

        // ASSERT
        Assertions.assertThat(error)
            .isNotNull();
        Assertions.assertThat(error.getSummary())
            .contains("keine Berechtigung")
            .contains("Organisation");
    }

    @Test
    @TestAsSupportBE
    void updateBenutzer_settingOrganisationForFirstTime_requiresAuthorizationForNewOrganisation() {
        // ARRANGE
        // a benutzer that currently has no organisation at all
        Benutzer benutzer = BenutzerBuilder.create(builder -> { });
        benutzerService.persist(benutzer);

        // a foreign organisation - the support actor would actually be authorized to write to any organisation,
        // so we spy on AuthorizerService and force the org-write check to fail for this specific organisation,
        // in order to isolate and verify the new organisation-change-authorization logic in BenutzerMapper.update
        Organisation foreignOrganisation = Objects.requireNonNull(
            benutzerResourceHelper.createSchularztAsOrganisationsChefDirectly(TenantEnum.BE).getSingleOrganisation());

        AuthorizerService authorizerServiceSpy = spy(authorizerService);
        doThrow(new AppFailureException(AppFailureMessage.missingBerechtigung(
                Organisation.class,
                foreignOrganisation.getId(),
                "test")))
            .when(authorizerServiceSpy).requireWriteAuthorizationOrganisation(foreignOrganisation);
        QuarkusMock.installMockForType(authorizerServiceSpy, AuthorizerService.class);

        BenutzerSaveJsonSpec saveJson = BenutzerBuilder.createJaxSpec(builder -> builder
            .forBenutzer(benutzer)
            .withOrganisationRollen(foreignOrganisation, List.of(FachRolle.MPA))).getData();

        // ACT
        AppFailureErrorJsonSpec error = api.
            benutzerResourceUpdate()
            .benutzerIDPath(benutzer.getId().getId())
            .body(saveJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
            .extract().as(AppFailureErrorJsonSpec.class);

        // ASSERT
        Assertions.assertThat(error)
            .isNotNull();
        Assertions.assertThat(error.getSummary())
            .contains("keine Berechtigung")
            .contains("Organisation");
    }

    @Test
    @TestAsSupportBE
    void updateBenutzer_movingToDifferentOrganisation_worksIfAuthorizedForNewOrganisation() {
        // ARRANGE
        Organisation sourceOrganisation = Objects.requireNonNull(
            benutzerResourceHelper.createSchularztAsOrganisationsChefDirectly(TenantEnum.BE).getSingleOrganisation());
        Organisation targetOrganisation = Objects.requireNonNull(
            benutzerResourceHelper.createSchularztAsOrganisationsChefDirectly(TenantEnum.BE).getSingleOrganisation());

        // a benutzer that currently belongs to the source organisation
        Benutzer benutzer = BenutzerBuilder.create(builder -> builder
            .withOrganisationRollen(sourceOrganisation, List.of(FachRolle.MPA)));
        benutzerService.persist(benutzer);

        BenutzerSaveJsonSpec saveJson = BenutzerBuilder.createJaxSpec(builder -> builder
            .forBenutzer(benutzer)
            .withOrganisationRollen(targetOrganisation, List.of(FachRolle.MPA))).getData();

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
        assertThat(result)
            .isNotNull();
        assertThat(result.getData().getOrganisationRollen().getFirst().getOrganisation())
            .isNotNull()
            .extracting(OrganisationJsonSpec::getId)
            .isEqualTo(targetOrganisation.getId().getId());
    }

    @Test
    @TestAsSupportBS
    void createBenutzerAsSupport_works_if_tenantSetting_is_enabled() {
        // ARRANGE
        EmailAddress benutzerEmail = EmailAddress.fromString("new-benutzer@mailbucket.dvbern.ch");
        EntityAktivStatus aktivStatus = EntityAktivStatus.AKTIV;
        Organisation organisation = organisationService.getZentraleOrganisation();

        // Wir wollen eine ZentraleOrganisation mit Benutzerverwaltung simulieren
        TenantSettingsService tenantSettingsServiceMock = mock(TenantSettingsService.class);
        TenantSetting tenantSettingOverride = TenantSettingBuilder.create(tenantSettingBuilder -> tenantSettingBuilder
            .withBenutzerverwaltungInternFeature(FeatureEnabledSetting.ENABLED)
            .withBenutzerverwaltungIgnoreFachRolleFromKeycloakFeature(FeatureEnabledSetting.ENABLED)
        );
        when(tenantSettingsServiceMock.getTenantSetting()).thenReturn(tenantSettingOverride);
        QuarkusMock.installMockForType(tenantSettingsServiceMock, TenantSettingsService.class);


        BenutzerJsonSpec benutzerJson = BenutzerBuilder.createJaxSpec(
            builder -> builder
                .withOrganisationRollen(organisation, List.of(FachRolle.SCHOOL_NURSE))
                .withNachname("Smith")
                .withVorname("John")
                .withEmail(benutzerEmail)
                .withBenutzerAktivStatus(aktivStatus));

        // ACT
        BenutzerJsonSpec result = api.
            benutzerResourceCreateBenutzer()
            .body(benutzerJson.getData())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(BenutzerJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getData().getVorname()).isEqualTo("John");
        assertThat(result.getData().getName()).isEqualTo("Smith");
        assertThat(result.getData().getEmail()).isEqualTo(benutzerEmail.toString());
        List<FachRolle> fachRollen = result.getData().getOrganisationRollen().getFirst().getFachRollen().stream()
            .map(spec -> FachRolle.valueOf(spec.name()))
            .toList();
        assertThat(fachRollen).containsExactlyElementsOf(List.of(FachRolle.SCHOOL_NURSE));
        assertThat(getAktivStatus(result.getData().getBenutzerAktivStatus())).isEqualTo(aktivStatus);
    }

    @Test
    @TestAsSupportBS
    void createBenutzer_stores_the_email_in_lowercase() {
        // ARRANGE
        String uniquePrefix = UUID.randomUUID() + ".";
        Organisation organisation = organisationService.getZentraleOrganisation();

        // Wir wollen eine ZentraleOrganisation mit Benutzerverwaltung simulieren
        TenantSettingsService tenantSettingsServiceMock = mock(TenantSettingsService.class);
        TenantSetting tenantSettingOverride = TenantSettingBuilder.create(tenantSettingBuilder -> tenantSettingBuilder
            .withBenutzerverwaltungInternFeature(FeatureEnabledSetting.ENABLED)
            .withBenutzerverwaltungIgnoreFachRolleFromKeycloakFeature(FeatureEnabledSetting.ENABLED)
        );
        when(tenantSettingsServiceMock.getTenantSetting()).thenReturn(tenantSettingOverride);
        QuarkusMock.installMockForType(tenantSettingsServiceMock, TenantSettingsService.class);

        // as an admin may type it, whereas Keycloak delivers the email in lowercase at login
        BenutzerSaveJsonSpec saveJson = BenutzerBuilder.createJaxSpec(
            builder -> builder
                .withOrganisationRollen(organisation, List.of(FachRolle.SCHOOL_NURSE))
                .withEmail(EmailAddress.fromString(uniquePrefix + "Anna.Muster@MailBucket.DVBern.ch"))
        ).getData();

        // ACT
        BenutzerJsonSpec result = api.benutzerResourceCreateBenutzer()
            .body(saveJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(BenutzerJsonSpec.class);

        // ASSERT
        assertThat(benutzerService.getById(ID.parse(result.getId(), Benutzer.class)).getEmail())
            .isEqualTo(EmailAddress.fromString(uniquePrefix + "anna.muster@mailbucket.dvbern.ch"));
    }

    @Test
    @TestAsSupportBS
    void createBenutzerAsSupport_fails_if_tenantSetting_is_disabled() {
        // ARRANGE
        EmailAddress benutzerEmail = EmailAddress.fromString("create-fails@mailbucket.dvbern.ch");
        Organisation organisation = organisationService.getZentraleOrganisation();

        BenutzerJsonSpec benutzerJson = BenutzerBuilder.createJaxSpec(
            builder -> builder
                .withOrganisationRollen(organisation, List.of(FachRolle.SCHOOL_NURSE))
                .withNachname("Smith")
                .withVorname("John")
                .withEmail(benutzerEmail)
                .withBenutzerAktivStatus(EntityAktivStatus.AKTIV));

        // ACT
        AppFailureErrorJsonSpec error = api.
            benutzerResourceCreateBenutzer()
            .body(benutzerJson.getData())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
            .extract().as(AppFailureErrorJsonSpec.class);

        // ASSERT
        Assertions.assertThat(error).isNotNull();
        Assertions.assertThat(error.getSummary())
            .contains("Die Benutzerverwaltung ist für diesen Mandanten nicht erlaubt.");
    }

    @Test
    @TestAsSupportBS
    void updateBenutzerAsSupport_update_fachrolle_fails_if_tenantSetting_is_disabled() {
        // ARRANGE
        tenantContext.setCurrentTenant(TenantEnum.BS);
        Organisation organisation = organisationService.getZentraleOrganisation();

        List<FachRolle> originalFachRollen = List.of(FachRolle.SCHULAERZTIN);
        Benutzer benutzer = BenutzerBuilder.create(
            builder -> builder
                .withOrganisationRollen(organisation, originalFachRollen));
        benutzerRepo.persist(benutzer);

        List<FachRolle> udpatedFachRollen = List.of(FachRolle.EMPFANG);
        BenutzerSaveJsonSpec saveJson = BenutzerBuilder.createJaxSpec(builder -> builder
            .forBenutzer(benutzer)
            .withOrganisationRollen(organisation, udpatedFachRollen)).getData();

        // ACT
        AppFailureErrorJsonSpec error = api.
            benutzerResourceUpdate()
            .benutzerIDPath(benutzer.getId().getId())
            .body(saveJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
            .extract().as(AppFailureErrorJsonSpec.class);

        // ASSERT
        Assertions.assertThat(error).isNotNull();
        Assertions.assertThat(error.getSummary())
            .contains(
                "Die Benutzerverwaltung ist für diesen Mandanten nicht erlaubt.");
    }

    private static EntityAktivStatus getAktivStatus(EntityAktivStatusSpec statusSpec) {
        return EntityAktivStatus.valueOf(statusSpec.toString());
    }
}
