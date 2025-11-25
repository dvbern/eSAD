package ch.dvbern.oss.hemed.esc.api.features.benutzer.ensure;

import java.util.Set;
import java.util.UUID;

import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumber;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerRepo;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;

@QuarkusTest
@DisplayNameGeneration(ReplaceUnderscores.class)
@RequiredArgsConstructor
class EnsureBenutzerHelperServiceTest {

    private final EnsureBenutzerHelperService ensureBenutzerHelperService;
    private final BenutzerRepo benutzerRepo;
    private final TenantProducer tenantProducer;

    @BeforeEach
    void setUp() {
        tenantProducer.setCurrentTenant(TenantEnum.BS);
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
