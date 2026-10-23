package ch.dvbern.oss.hemed.esc.api.shared.auth;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.ensure.JsonWebTokenClaims;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ClientAccountConfig.SvcAccountClientInfo;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSettingsService;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static ch.dvbern.oss.hemed.esc.api.shared.auth.DVBSupportRoleUtil.isDVBSupport;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
@RequiredArgsConstructor
class RolesAugmentorTest {

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_USERNAME = "test-user";
    private static final String TEST_SUBJECT = UUID.randomUUID().toString();
    private static final String TEST_ISSUER = "http://localhost/realms/test";

    private static final String ESCADA_CLIENT_USER = "escada-client@dvbern.ch";
    private static final String ESCADA_AZP = "escada-client";
    private static final String FILE_INGRESS_CLIENT_USER = "file-ingress-client@dvbern.ch";
    private static final String FILE_INGRESS_AZP = "file-ingress-client";
    private static final String PUPIL_CLIENT_USER = "pupil-client@dvbern.ch";
    private static final String PUPIL_AZP = "pupil-client";
    private static final String HELLODATA_CLIENT_USER = "hellodata-client@dvbern.ch";
    private static final String HELLODATA_AZP = "hellodata-client";

    private final RolesAugmentor rolesAugmentor;

    @Inject
    TenantContext tenantContext;

    private TenantSettingsService tenantSettingsServiceMock;
    private BenutzerService benutzerServiceMock;

    @BeforeEach
    void setUpMocks() {
        tenantSettingsServiceMock = mock(TenantSettingsService.class);
        benutzerServiceMock = mock(BenutzerService.class);
        ConfigService configServiceMock = mock(ConfigService.class);

        QuarkusMock.installMockForType(tenantSettingsServiceMock, TenantSettingsService.class);
        QuarkusMock.installMockForType(benutzerServiceMock, BenutzerService.class);
        QuarkusMock.installMockForType(configServiceMock, ConfigService.class);

        when(configServiceMock.getClientAccounts()).thenReturn(() -> List.of(
            new SvcAccountClientInfo(ESCADA_AZP, ESCADA_CLIENT_USER, Berechtigung.ESCADA_PUSH),
            new SvcAccountClientInfo(FILE_INGRESS_AZP, FILE_INGRESS_CLIENT_USER, Berechtigung.FILE_INGRESS_PUSH),
            new SvcAccountClientInfo(PUPIL_AZP, PUPIL_CLIENT_USER, Berechtigung.PUPIL_PUSH),
            new SvcAccountClientInfo(HELLODATA_AZP, HELLODATA_CLIENT_USER, Berechtigung.HELLODATA_EXPORT_LESEN)
        ));
    }

    @Test
    void augment_shouldReturnIdentityUnchanged_whenAnonymous() {
        // ARRANGE
        SecurityIdentity anonymousIdentity = QuarkusSecurityIdentity.builder()
            .setAnonymous(true)
            .build();

        // ACT
        SecurityIdentity result = rolesAugmentor.augment(anonymousIdentity, runBlocking())
            .await().indefinitely();

        // ASSERT
        assertThat(result.isAnonymous()).isTrue();
    }

    @Test
    void augmentWithRoles_shouldSetTenantAndDelegateToDatabase_whenIgnoreFachrollenEnabled() {
        // ARRANGE
        when(tenantSettingsServiceMock.isBenutzerverwaltungIgnoreFachRolleFromKeycloakEnabled()).thenReturn(true);
        when(benutzerServiceMock.getDesiredBerechtigungen(any(EmailAddress.class)))
            .thenReturn(List.of(Berechtigung.AUFGABEN_LESEN, Berechtigung.KINDER_LESEN_MEDIZINISCH));

        SecurityIdentity identity = buildIdentityWithToken(TEST_EMAIL, TEST_USERNAME, "some-azp", "BS");

        // ACT
        SecurityIdentity result = rolesAugmentor.augmentWithRoles(identity, TenantEnum.BS);

        // ASSERT
        verify(benutzerServiceMock).getDesiredBerechtigungen(EmailAddress.fromString(TEST_EMAIL));
        assertThat(result.getRoles()).containsExactlyInAnyOrder(
            Berechtigung.AUFGABEN_LESEN.getBerechtigungName(),
            Berechtigung.KINDER_LESEN_MEDIZINISCH.getBerechtigungName()
        );
    }

    @Test
    void augmentWithRoles_shouldSetTenantAndDelegateToDatabase_whenIgnoreFachrollenDisabled() {
        // ARRANGE
        when(tenantSettingsServiceMock.isBenutzerverwaltungIgnoreFachRolleFromKeycloakEnabled()).thenReturn(false);
        when(benutzerServiceMock.getDesiredBerechtigungen(any(EmailAddress.class)))
            .thenReturn(List.of(Berechtigung.AUFGABEN_LESEN, Berechtigung.KINDER_LESEN_MEDIZINISCH));

        SecurityIdentity identity = buildIdentityWithToken(TEST_EMAIL, TEST_USERNAME, "some-azp", "BS");

        // ACT
        SecurityIdentity result = rolesAugmentor.augmentWithRoles(identity, TenantEnum.BS);

        // ASSERT
        verify(benutzerServiceMock, times(0)).getDesiredBerechtigungen(EmailAddress.fromString(TEST_EMAIL));
        assertThat(result.getRoles()).isEmpty();
    }

    @Test
    void augmentWithRoles_shouldReturnEscadaPushRole_whenEscadaClientUser() {
        // ARRANGE
        when(tenantSettingsServiceMock.isBenutzerverwaltungIgnoreFachRolleFromKeycloakEnabled()).thenReturn(false);
        SecurityIdentity identity = buildIdentityWithToken(TEST_EMAIL, ESCADA_CLIENT_USER, ESCADA_AZP, "BS");

        // ACT
        SecurityIdentity result = rolesAugmentor.augmentWithRoles(identity, TenantEnum.BS);

        // ASSERT
        assertThat(result.getRoles()).containsExactly(Berechtigung.ESCADA_PUSH.getBerechtigungName());
    }

    @Test
    void augmentWithRoles_shouldReturnFileIngressPushRole_whenFileIngressClientUser() {
        // ARRANGE
        when(tenantSettingsServiceMock.isBenutzerverwaltungIgnoreFachRolleFromKeycloakEnabled()).thenReturn(false);
        SecurityIdentity identity =
            buildIdentityWithToken(TEST_EMAIL, FILE_INGRESS_CLIENT_USER, FILE_INGRESS_AZP, "BS");

        // ACT
        SecurityIdentity result = rolesAugmentor.augmentWithRoles(identity, TenantEnum.BS);

        // ASSERT
        assertThat(result.getRoles()).containsExactly(Berechtigung.FILE_INGRESS_PUSH.getBerechtigungName());
    }

    @Test
    void augmentWithRoles_shouldReturnPupilPushRole_whenPupilClientUser() {
        // ARRANGE
        when(tenantSettingsServiceMock.isBenutzerverwaltungIgnoreFachRolleFromKeycloakEnabled()).thenReturn(false);
        SecurityIdentity identity = buildIdentityWithToken(TEST_EMAIL, PUPIL_CLIENT_USER, PUPIL_AZP, "BS");

        // ACT
        SecurityIdentity result = rolesAugmentor.augmentWithRoles(identity, TenantEnum.BS);

        // ASSERT
        assertThat(result.getRoles()).containsExactly(Berechtigung.PUPIL_PUSH.getBerechtigungName());
    }

    /**
     * The HelloData service account has no Benutzer record and therefore no FachRolle - it gets
     * its single Berechtigung from the machine-client branch, like the other clients above.
     */
    @Test
    void augmentWithRoles_shouldReturnHelloDataExportRole_whenHelloDataClientUser() {
        // ARRANGE
        when(tenantSettingsServiceMock.isBenutzerverwaltungIgnoreFachRolleFromKeycloakEnabled()).thenReturn(false);
        SecurityIdentity identity =
            buildIdentityWithToken("", HELLODATA_CLIENT_USER, HELLODATA_AZP, "BE");

        // ACT
        SecurityIdentity result = rolesAugmentor.augmentWithRoles(identity, TenantEnum.BE);

        // ASSERT
        assertThat(result.getRoles())
            .containsExactly(Berechtigung.HELLODATA_EXPORT_LESEN.getBerechtigungName());
    }

    /**
     * Regression test: a machine client (here HelloData, used by tenant BE) must get its fixed
     * role even when the tenant reads its FachRollen from the database. The client account check
     * runs before that database lookup, so the service account - which has no Benutzer record -
     * still receives its role and the database is never consulted for it.
     */
    @Test
    void augmentWithRoles_shouldReturnHelloDataExportRole_whenHelloDataClientUserAndIgnoreFachrollenEnabled() {
        // ARRANGE
        when(tenantSettingsServiceMock.isBenutzerverwaltungIgnoreFachRolleFromKeycloakEnabled()).thenReturn(true);
        SecurityIdentity identity =
            buildIdentityWithToken("", HELLODATA_CLIENT_USER, HELLODATA_AZP, "BE");

        // ACT
        SecurityIdentity result = rolesAugmentor.augmentWithRoles(identity, TenantEnum.BE);

        // ASSERT
        assertThat(result.getRoles())
            .containsExactly(Berechtigung.HELLODATA_EXPORT_LESEN.getBerechtigungName());
        verify(benutzerServiceMock, times(0)).getDesiredBerechtigungen(any(EmailAddress.class));
    }

    @Test
    void augmentWithRoles_shouldReturnFachRolleBerechtigungen_whenBenutzerverwaltungIgnoreFachRolleFromKeycloakDisabled() {
        // ARRANGE
        when(tenantSettingsServiceMock.isBenutzerverwaltungIgnoreFachRolleFromKeycloakEnabled()).thenReturn(false);

        SecurityIdentity identity = buildIdentityWithTokenAndFachRollen(
            TEST_EMAIL, TEST_USERNAME, "esc", "BS", Set.of(FachRolle.EMPFANG));

        Set<Berechtigung> expectedBerechtigungen = FachRolle.collectAllBerechtigungen(TenantEnum.BS, FachRolle.EMPFANG);

        // ACT
        SecurityIdentity result = rolesAugmentor.augmentWithRoles(identity, TenantEnum.BS);

        // ASSERT
        Set<String> expectedRoleNames = new HashSet<>();
        expectedBerechtigungen.forEach(b -> expectedRoleNames.add(b.getBerechtigungName()));
        assertThat(result.getRoles()).containsExactlyInAnyOrderElementsOf(expectedRoleNames);
    }

    @Test
    void augmentWithRoles_shouldReturnNoRoles_whenUserFromKeycloakWithoutTenantClaim() {
        // ARRANGE
        when(tenantSettingsServiceMock.isBenutzerverwaltungIgnoreFachRolleFromKeycloakEnabled()).thenReturn(false);
        JsonWebToken jwt = mock(JsonWebToken.class);
        when(jwt.getClaim(Claims.email)).thenReturn(TEST_EMAIL);
        when(jwt.getClaim(Claims.preferred_username)).thenReturn(TEST_USERNAME);
        when(jwt.getClaim(Claims.azp)).thenReturn("some-azp");
        when(jwt.getClaim(JsonWebTokenClaims.TENANT_CLAIM)).thenReturn(null);
        SecurityIdentity identity = QuarkusSecurityIdentity.builder()
            .setPrincipal(jwt)
            .build();

        // ACT
        SecurityIdentity result = rolesAugmentor.augmentWithRoles(identity, TenantEnum.BS);

        // ASSERT
        assertThat(result.getRoles()).isEmpty();
    }

    @Test
    void augmentWithRoles_shouldReturnNoRoles_whenUserFromKeycloakWithInvalidTenantClaim() {
        // ARRANGE
        when(tenantSettingsServiceMock.isBenutzerverwaltungIgnoreFachRolleFromKeycloakEnabled()).thenReturn(false);
        SecurityIdentity identity = buildIdentityWithToken(TEST_EMAIL, TEST_USERNAME, "some-azp", "INVALID_TENANT");

        // ACT
        SecurityIdentity result = rolesAugmentor.augmentWithRoles(identity, TenantEnum.BS);

        // ASSERT
        assertThat(result.getRoles()).isEmpty();
    }

    @Test
    void augmentWithRoles_shouldReturnSupportRolesForUrlTenant_whenDVBSupportMarkerInToken() {
        // ARRANGE
        when(tenantSettingsServiceMock.isBenutzerverwaltungIgnoreFachRolleFromKeycloakEnabled()).thenReturn(false);
        tenantContext.setCurrentTenant(TenantEnum.BS);

        SecurityIdentity identity = buildIdentityWithToken(
            TEST_EMAIL, TEST_USERNAME, "some-azp", DVBSupportRoleUtil.DVB_SUPPORT_TENANT_MARKER);

        Set<Berechtigung> expectedBerechtigungen = FachRolle.collectAllBerechtigungen(TenantEnum.BS, FachRolle.SUPPORT);

        // ACT
        SecurityIdentity result = rolesAugmentor.augmentWithRoles(identity, TenantEnum.BS);

        // ASSERT
        Set<String> expectedRoleNames = new HashSet<>();
        expectedBerechtigungen.forEach(b -> expectedRoleNames.add(b.getBerechtigungName()));
        assertThat(result.getRoles()).containsExactlyInAnyOrderElementsOf(expectedRoleNames);
    }

    @Test
    void augmentWithRoles_shouldReturnNoRoles_whenDVBSupportMarkerInTokenButUrlTenantIsNull() {
        // ARRANGE
        when(tenantSettingsServiceMock.isBenutzerverwaltungIgnoreFachRolleFromKeycloakEnabled()).thenReturn(false);

        SecurityIdentity identity = buildIdentityWithToken(
            TEST_EMAIL, TEST_USERNAME, "some-azp", DVBSupportRoleUtil.DVB_SUPPORT_TENANT_MARKER);

        // ACT
        SecurityIdentity result = rolesAugmentor.augmentWithRoles(identity, null);

        // ASSERT
        assertThat(result.getRoles()).isEmpty();
    }

    @Test
    void isDVBSupport_shouldReturnTrue_whenMarkerMatches() {
        // ACT / ASSERT
        assertThat(isDVBSupport(DVBSupportRoleUtil.DVB_SUPPORT_TENANT_MARKER)).isTrue();
    }

    @Test
    void isDVBSupport_shouldReturnFalse_whenMarkerDoesNotMatch() {
        // ACT / ASSERT
        assertThat(isDVBSupport("BS")).isFalse();
    }

    @NonNull
    private SecurityIdentity buildIdentityWithToken(
        @NonNull String email,
        @NonNull String preferredUsername,
        @NonNull String azp,
        @NonNull String tenantClaim
    ) {
        JsonWebToken jwt = mockJsonWebToken(email, preferredUsername, azp, tenantClaim);
        return QuarkusSecurityIdentity.builder()
            .setPrincipal(jwt)
            .build();
    }

    @NonNull
    private SecurityIdentity buildIdentityWithTokenAndFachRollen(
        @NonNull String email,
        @NonNull String preferredUsername,
        @NonNull String azp,
        @NonNull String tenantClaim,
        @NonNull Set<FachRolle> fachRollen
    ) {
        JsonWebToken jwt = mockJsonWebTokenWithFachRollen(email, preferredUsername, azp, tenantClaim, fachRollen);
        return QuarkusSecurityIdentity.builder()
            .setPrincipal(jwt)
            .build();
    }

    @NonNull
    private JsonWebToken mockJsonWebToken(
        @NonNull String email,
        @NonNull String preferredUsername,
        @NonNull String azp,
        @NonNull String tenantClaim
    ) {
        JsonWebToken jwt = mock(JsonWebToken.class);
        when(jwt.getClaim(Claims.email)).thenReturn(email);
        when(jwt.getClaim(Claims.preferred_username)).thenReturn(preferredUsername);
        when(jwt.getClaim(Claims.azp)).thenReturn(azp);
        when(jwt.getClaim(JsonWebTokenClaims.TENANT_CLAIM)).thenReturn(tenantClaim);
        when(jwt.getSubject()).thenReturn(TEST_SUBJECT);
        when(jwt.getIssuer()).thenReturn(TEST_ISSUER);
        when(jwt.getClaim(Claims.family_name)).thenReturn("TestFamily");
        when(jwt.getClaim(Claims.given_name)).thenReturn("TestGiven");
        when(jwt.getClaim(JsonWebTokenClaims.SESSION_ID_CLAIM)).thenReturn("test-session");
        when(jwt.claim(JsonWebTokenClaims.MOBILE_NUMMER)).thenReturn(Optional.empty());
        when(jwt.claim(JsonWebTokenClaims.TITLE_CLAIM)).thenReturn(Optional.empty());
        when(jwt.claim(JsonWebTokenClaims.PHONE_CLAIM)).thenReturn(Optional.empty());
        when(jwt.claim(JsonWebTokenClaims.CLIENT_ROLE_CLAIM)).thenReturn(Optional.empty());
        return jwt;
    }

    @NonNull
    @SuppressWarnings("java:S125") // This block of commented-out lines of code should be removed: Es ist kein Code
    private JsonWebToken mockJsonWebTokenWithFachRollen(
        @NonNull String email,
        @NonNull String preferredUsername,
        @NonNull String azp,
        @NonNull String tenantClaim,
        @NonNull Set<FachRolle> fachRollen
    ) {
        JsonWebToken jwt = mockJsonWebToken(email, preferredUsername, azp, tenantClaim);
        // Build resource_access JSON structure: { "esc": { "roles": ["EMPFANG", ...] } }
        JsonArrayBuilder rolesArrayBuilder = Json.createArrayBuilder();
        fachRollen.forEach(fr -> rolesArrayBuilder.add(fr.name()));

        JsonObject escClient = Json.createObjectBuilder()
            .add(JsonWebTokenClaims.CLIENT_ROLE_ROLES_CLAIM, rolesArrayBuilder.build())
            .build();

        JsonObject resourceAccess = Json.createObjectBuilder()
            .add(JsonWebTokenClaims.CLIENT_ROLE_CLIENT_NAME_CLAIM, escClient)
            .build();

        when(jwt.claim(JsonWebTokenClaims.CLIENT_ROLE_CLAIM)).thenReturn(Optional.of(resourceAccess));
        return jwt;
    }

    @NonNull
    private static AuthenticationRequestContext runBlocking() {
        return supplier -> Uni.createFrom().item(supplier.get());
    }
}
