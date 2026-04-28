package ch.dvbern.oss.hemed.esc.api.features.benutzer.ensure;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

import ch.dvbern.dvbstarter.infrastructure.deployment.DeploymentEnvironment;
import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumber;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerRepo;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSetting;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSettingsService;
import ch.dvbern.oss.hemed.esc.api.shared.settings.features.FeatureEnabledSetting;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.TenantSettingBuilder;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.when;

@QuarkusTest
@DisplayNameGeneration(ReplaceUnderscores.class)
@RequiredArgsConstructor
class EnsureBenutzerHelperServiceTest {

    private final EnsureBenutzerHelperService ensureBenutzerHelperService;
    private final BenutzerRepo benutzerRepo;
    private final TenantProducer tenantProducer;
    private final FixtureService fixtureService;

    private TenantSettingsService tenantSettingsServiceMock;

    @BeforeEach
    void setUp() throws MalformedURLException {
        tenantProducer.setCurrentTenant(TenantEnum.BS);
        tenantSettingsServiceMock = Mockito.mock(TenantSettingsService.class);
        ConfigService configServiceMock = Mockito.mock(ConfigService.class);
        QuarkusMock.installMockForType(tenantSettingsServiceMock, TenantSettingsService.class);
        QuarkusMock.installMockForType(configServiceMock, ConfigService.class);
        // default: ignoreFachrollenFromKeycloak is disabled (the old default behavior)
        TenantSetting tenantSetting = TenantSettingBuilder.create(builder ->
            builder.withBenutzerverwaltungIgnoreFachRolleFromKeycloakFeature(FeatureEnabledSetting.DISABLED));
        Mockito.when(tenantSettingsServiceMock.getTenantSetting()).thenReturn(tenantSetting);
        Mockito.when(configServiceMock.getEnvironment()).thenReturn(DeploymentEnvironment.UNIT_TEST);
        Mockito.when(configServiceMock.getPublicUrl()).thenReturn(URL.of(URI.create("https://example.com"), null));
    }

    @Test
    void ensureUser() {
        UUID subjectId = UUID.randomUUID();
        var testemail = "admin-testuser.basel." + subjectId + "@example.com";
        JsonWebTokenDataDTO jwtData = new JsonWebTokenDataDTO(
            null,
            subjectId,
            testemail,
            "testcreate",
            "testcreate",
            EmailAddress.fromString(testemail),
            "issuer",
            "+41 76 123 45 56",
            "+41 61 123 45 56", // might be the tel of the office phone
            "Dr. Med",
            TenantEnum.BS.name(),
            Set.of(FachRolle.SYSTEM_ADMIN)
        );
        ensureBenutzerHelperService.createNewUserFromToken(jwtData);
        var storeduser = benutzerRepo.findByEmail(EmailAddress.fromString(testemail));

        Assertions.assertThat(storeduser).isPresent();
        var userToUpdate = storeduser.get();
        JsonWebTokenDataDTO jwtDataChanged = new JsonWebTokenDataDTO(
            null,
            subjectId,
            testemail,
            "changed",
            "changed",
            EmailAddress.fromString(testemail),
            "issuer",
            "+41 76 123 45 56",
            "+41 61 123 45 90", // might be the tel of the office phone
            "Herr",
            TenantEnum.BS.name(),
            Set.of(FachRolle.SYSTEM_ADMIN)
        );
        ensureBenutzerHelperService.updateExistingUser(userToUpdate.getId(), jwtDataChanged);

        var updatedUser = benutzerRepo.findByEmail(EmailAddress.fromString(testemail));

        Assertions.assertThat(updatedUser).isPresent();
        Assertions.assertThat(updatedUser.get().getVorname()).isEqualTo("changed");
        Assertions.assertThat(updatedUser.get().getTelefon()).isEqualTo(NormalizedPhoneNumber.parse("+41 61 123 45 90"));
        Assertions.assertThat(updatedUser.get().getTitel()).isEqualTo("Herr");

    }

    @Test
    void updateExistingUser_ignoreFachrollenFromKeycloak_enabled_does_not_sync_fachRollen() {
        // ARRANGE: Create a user with SYSTEM_ADMIN FachRolle
        UUID subjectId = UUID.randomUUID();
        var testemail = "ignfr-" + subjectId.toString().substring(0, 8) + "@example.com";
        JsonWebTokenDataDTO jwtData = new JsonWebTokenDataDTO(
            null,
            subjectId,
            testemail,
            "testcreate",
            "testcreate",
            EmailAddress.fromString(testemail),
            "issuer",
            "+41 76 123 45 56",
            "+41 61 123 45 56",
            "Dr. Med",
            TenantEnum.BS.name(),
            Set.of(FachRolle.SYSTEM_ADMIN)
        );
        ensureBenutzerHelperService.createNewUserFromToken(jwtData);
        var storedUser = benutzerRepo.findByEmail(EmailAddress.fromString(testemail));
        Assertions.assertThat(storedUser).isPresent();
        var userToUpdate = fixtureService.readBenutzerWithBerechtigungen(storedUser.get().getId());
        EnumSet<FachRolle> originalFachRollen = userToUpdate.getFachRollenCombined();
        Assertions.assertThat(originalFachRollen).contains(FachRolle.SYSTEM_ADMIN);

        // Now enable ignoreFachrollenFromKeycloak
        when(tenantSettingsServiceMock.isBenutzerverwaltungIgnoreFachRolleFromKeycloakEnabled()).thenReturn(true);

        // ACT: Update with a different FachRolle (MPA), but ignoreFachrollenFromKeycloak=true
        JsonWebTokenDataDTO jwtDataChanged = new JsonWebTokenDataDTO(
            null,
            subjectId,
            testemail,
            "changed",
            "changed",
            EmailAddress.fromString(testemail),
            "issuer",
            "+41 76 123 45 56",
            "+41 61 123 45 90",
            "Herr",
            TenantEnum.BS.name(),
            Set.of(FachRolle.MPA)
        );
        ensureBenutzerHelperService.updateExistingUser(userToUpdate.getId(), jwtDataChanged);

        // ASSERT: User data was updated but FachRollen were NOT changed (still SYSTEM_ADMIN)
        var updatedUser = fixtureService.readBenutzerWithBerechtigungen(userToUpdate.getId());
        Assertions.assertThat(updatedUser.getVorname()).isEqualTo("changed");
        Assertions.assertThat(updatedUser.getTitel()).isEqualTo("Herr");
        // FachRollen should remain SYSTEM_ADMIN (not changed to MPA from token)
        Assertions.assertThat(updatedUser.getFachRollenCombined()).contains(FachRolle.SYSTEM_ADMIN);
        Assertions.assertThat(updatedUser.getFachRollenCombined()).doesNotContain(FachRolle.MPA);
    }

    @Test
    void updateExistingUser_ignoreFachrollenFromKeycloak_disabled_syncs_fachRollen() {
        // ARRANGE: Create a user with SYSTEM_ADMIN FachRolle
        UUID subjectId = UUID.randomUUID();
        var testemail = "syncfr-" + subjectId.toString().substring(0, 8) + "@example.com";
        JsonWebTokenDataDTO jwtData = new JsonWebTokenDataDTO(
            null,
            subjectId,
            testemail,
            "testcreate",
            "testcreate",
            EmailAddress.fromString(testemail),
            "issuer",
            "+41 76 123 45 56",
            "+41 61 123 45 56",
            "Dr. Med",
            TenantEnum.BS.name(),
            Set.of(FachRolle.SYSTEM_ADMIN)
        );
        ensureBenutzerHelperService.createNewUserFromToken(jwtData);
        var storedUser = benutzerRepo.findByEmail(EmailAddress.fromString(testemail));
        Assertions.assertThat(storedUser).isPresent();

        // Keep ignoreFachrollenFromKeycloak disabled (default in setUp)

        // ACT: Update with a different FachRolle (MPA)
        var userToUpdate = fixtureService.readBenutzerWithBerechtigungen(storedUser.get().getId());
        JsonWebTokenDataDTO jwtDataChanged = new JsonWebTokenDataDTO(
            null,
            subjectId,
            testemail,
            "changed",
            "changed",
            EmailAddress.fromString(testemail),
            "issuer",
            "+41 76 123 45 56",
            "+41 61 123 45 90",
            "Herr",
            TenantEnum.BS.name(),
            Set.of(FachRolle.MPA)
        );
        ensureBenutzerHelperService.updateExistingUser(userToUpdate.getId(), jwtDataChanged);

        // ASSERT: FachRollen WERE changed to MPA
        var updatedUser = fixtureService.readBenutzerWithBerechtigungen(userToUpdate.getId());
        Assertions.assertThat(updatedUser.getFachRollenCombined()).contains(FachRolle.MPA);
        Assertions.assertThat(updatedUser.getFachRollenCombined()).doesNotContain(FachRolle.SYSTEM_ADMIN);
    }

    /**
     * same as as ensureUser but changes the subject id (should not normally happen, might happen if a user is
     * deleted in kc and recreated with the same email)
     */
    @Test
    void ensureUser_keycloakIc_changed() {
        // ARRANGE
        UUID subjectId = UUID.randomUUID();

        var testemail = "admin-testuser.basel." + subjectId + "@example.com";
        JsonWebTokenDataDTO jwtData = new JsonWebTokenDataDTO(
            null,
            subjectId,
            testemail,
            "testcreate",
            "testcreate",
            EmailAddress.fromString(testemail),
            "issuer",
            "+41 76 123 45 56",
            "+41 61 123 45 56", // might be the tel of the office phone
            "Dr. Med",
            TenantEnum.BS.name(),
            Set.of(FachRolle.SYSTEM_ADMIN)
        );
        ensureBenutzerHelperService.createNewUserFromToken(jwtData);
        var storeduser = benutzerRepo.findByEmail(EmailAddress.fromString(testemail));
        Assertions.assertThat(storeduser).isPresent();
        Assertions.assertThat(storeduser.get().getKeycloakId()).isEqualTo(subjectId.toString());

        // ACT
        var userToUpdate = storeduser.get();
        UUID newSubjectId = UUID.randomUUID();
        JsonWebTokenDataDTO jwtDataChanged = new JsonWebTokenDataDTO(
            null,
            newSubjectId,
            testemail,
            "changed",
            "changed",
            EmailAddress.fromString(testemail),
            "issuer",
            "+41 76 123 45 56",
            "+41 61 123 45 56", // might be the tel of the office phone
            "Dr. Med",
            TenantEnum.BS.name(),
            Set.of(FachRolle.SYSTEM_ADMIN)
        );
        ensureBenutzerHelperService.updateExistingUser(userToUpdate.getId(), jwtDataChanged);

        // ASSERT
        var updatedUser = benutzerRepo.findByEmail(EmailAddress.fromString(testemail));
        Assertions.assertThat(updatedUser).isPresent();
        Assertions.assertThat(updatedUser.get().getVorname()).isEqualTo("changed");
        Assertions.assertThat(updatedUser.get().getKeycloakId()).isEqualTo(newSubjectId.toString());
    }
}
