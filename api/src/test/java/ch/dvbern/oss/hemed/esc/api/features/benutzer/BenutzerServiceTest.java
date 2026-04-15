package ch.dvbern.oss.hemed.esc.api.features.benutzer;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppAuthException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.ensure.JsonWebTokenDataDTO;
import ch.dvbern.oss.hemed.esc.api.shared.auth.Berechtigung;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.BenutzerBerechtigung;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.BenutzerBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBE;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.from;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
@DisplayNameGeneration(ReplaceUnderscores.class)
@RequiredArgsConstructor
class BenutzerServiceTest {

    private final BenutzerService service;
    private final TenantProducer tenantProducer;

    @Test
    void throws_on_missing_subject() {
        assertThatThrownBy(service::getCurrentBenutzerOrThrowException)
            .isInstanceOf(AppAuthException.class);
    }

    @Test
    @TestAsAdminBS
    void creates_a_new_benutzer_by_jwt() {

        var testemail = "admin-testuser.basel" + UUID.randomUUID() + "@example.com";
        JsonWebTokenDataDTO jwtData = new JsonWebTokenDataDTO(
            null,
            UUID.randomUUID(),
            testemail,
            "Baseleti",
            "Basil",
            EmailAddress.fromString(testemail),
            "issuer",
            "076 123 45 67",
            "061 123 45 67",
            "Herr",
            TenantEnum.BS.name(),
            Set.of(FachRolle.SYSTEM_ADMIN)
        );
        var benutzer = service.createBenutzerFromJWT(jwtData);

        assertThat(benutzer)
            .returns(testemail, from(Benutzer::getEmail).andThen(EmailAddress::getAddress))
            .returns("Basil", from(Benutzer::getVorname))
            .returns("Baseleti", from(Benutzer::getNachname))
            .returns(jwtData.getSubjectId().toString(), from(Benutzer::getKeycloakId));
    }

    @Test
    @TestAsAdminBS
    void check_system_admin_bs_has_required_roles() {
        // ARRANGE
        Set<Berechtigung> excludedBerechtigungen = EnumSet.of(Berechtigung.PUPIL_PUSH);
        Set<Berechtigung> expectedBerechtigungen = EnumSet.allOf(Berechtigung.class);
        expectedBerechtigungen.removeAll(excludedBerechtigungen);

        // ACT
        Optional<Set<Berechtigung>> berechtigungenOfSysAdmin =
            service.findBerechtigungenByEmail(EmailAddress.fromString("bs-esc@dvbern.ch"));

        // ASSERT
        Assertions.assertThat(berechtigungenOfSysAdmin)
            .isPresent()
            .get()
            .asInstanceOf(InstanceOfAssertFactories.set(Berechtigung.class))
            .containsExactlyInAnyOrderElementsOf(expectedBerechtigungen)
            .doesNotContainAnyElementsOf(excludedBerechtigungen);
    }

    @Test
    @TestAsAdminBE
    void check_system_admin_be_has_required_roles() {
        // ARRANGE
        Set<Berechtigung> excludedBerechtigungen = EnumSet.of(Berechtigung.PUPIL_PUSH);
        Set<Berechtigung> expectedBerechtigungen = EnumSet.allOf(Berechtigung.class);
        expectedBerechtigungen.removeAll(excludedBerechtigungen);

        // ACT
        tenantProducer.setCurrentTenant(TenantEnum.BE); // Default ist BS
        Optional<Set<Berechtigung>> berechtigungenOfSysAdmin =
            service.findBerechtigungenByEmail(EmailAddress.fromString("be-esc@dvbern.ch"));

        // ASSERT
        Assertions.assertThat(berechtigungenOfSysAdmin)
            .isPresent()
            .get()
            .asInstanceOf(InstanceOfAssertFactories.set(Berechtigung.class))
            .containsExactlyInAnyOrderElementsOf(expectedBerechtigungen)
            .doesNotContainAnyElementsOf(excludedBerechtigungen);
    }

    @Test
    @TestAsAdminBS
    void berechtigungen_can_be_synced_and_read() {
        // ARRANGE
        var createDto = BenutzerBuilder.createCreateDTO(
            builder -> builder
                .withRandomEmail("norbertneu@example.com")
                .withRandomVorname("Norbert")
                .withRandomNachname("Neu")
                .withKeycloakId(UUID.randomUUID().toString())
        );

        ID<Benutzer> benutzerID = service.createBenutzer(createDto);

        // ACT
        List<BenutzerBerechtigung> storedBerechtigungen = service.syncBerechtigungenToBenutzer(
            benutzerID,
            FachRolle.collectAllBerechtigungen(TenantEnum.BS, FachRolle.SYSTEM_ADMIN)
        );
        // ASSERT
        assertThat(storedBerechtigungen)
            .extracting(BenutzerBerechtigung::getBerechtigung)
            .containsExactlyInAnyOrderElementsOf(FachRolle.collectAllBerechtigungen(
                TenantEnum.BS,
                FachRolle.SYSTEM_ADMIN));
        service.findBerechtigungenByEmail(createDto.getEmail())
            .ifPresentOrElse(
                berechtigungen -> assertThat(berechtigungen)
                    .containsExactlyInAnyOrderElementsOf(FachRolle.collectAllBerechtigungen(
                        TenantEnum.BS,
                        FachRolle.SYSTEM_ADMIN)),
                () -> Assertions.fail("No berechtigungen found")
            );
    }

    @Test
    @TestAsAdminBS
    void findByEmail_returns_existing_benutzer() {
        // ARRANGE
        var testemail = "fbe-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        var emailAddress = EmailAddress.fromString(testemail);
        var createDto = BenutzerBuilder.createCreateDTO(
            builder -> builder
                .withEmail(emailAddress)
                .withRandomVorname("FindMe")
                .withRandomNachname("ByEmail")
                .withKeycloakId(UUID.randomUUID().toString())
        );
        service.createBenutzer(createDto);

        // ACT
        Optional<Benutzer> result = service.findByEmail(emailAddress);

        // ASSERT
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(emailAddress);
    }

    @Test
    @TestAsAdminBS
    void findByEmail_returns_empty_for_nonexistent_email() {
        // ACT
        Optional<Benutzer> result = service.findByEmail(
            EmailAddress.fromString("nonexistent-" + UUID.randomUUID() + "@example.com"));

        // ASSERT
        assertThat(result).isEmpty();
    }

    @Test
    @TestAsAdminBS
    void getDesiredBerechtigungenToBenutzerFromFachrollen_syncs_berechtigungen_from_stored_fachrollen() {
        // ARRANGE: Create a user with SYSTEM_ADMIN FachRolle via JWT
        tenantProducer.setCurrentTenant(TenantEnum.BS);
        var testemail = "sync-fachrollen-test" + UUID.randomUUID() + "@example.com";
        JsonWebTokenDataDTO jwtData = new JsonWebTokenDataDTO(
            null,
            UUID.randomUUID(),
            testemail,
            "Syncer",
            "Fach",
            EmailAddress.fromString(testemail),
            "issuer",
            null,
            null,
            null,
            TenantEnum.BS.name(),
            Set.of(FachRolle.SYSTEM_ADMIN)
        );
        service.createBenutzerFromJWT(jwtData);

        // ACT
        List<Berechtigung> result = service.getDesiredBerechtigungen(
            EmailAddress.fromString(testemail));

        // ASSERT
        Set<Berechtigung> expectedBerechtigungen =
            FachRolle.collectAllBerechtigungen(TenantEnum.BS, FachRolle.SYSTEM_ADMIN);
        assertThat(result)
            .containsExactlyInAnyOrderElementsOf(expectedBerechtigungen);
    }

    @Test
    @TestAsAdminBS
    void getDesiredBerechtigungen_returns_empty_for_unknown_email() {
        // ACT
        List<Berechtigung> result = service.getDesiredBerechtigungen(
            EmailAddress.fromString("unknown-" + UUID.randomUUID() + "@example.com"));

        // ASSERT
        assertThat(result).isEmpty();
    }

    @Test
    @TestAsAdminBS
    void fails_creates_a_new_benutzer_by_jwt_for_organisation_and_system_rollen() {

        var testemail = "admin-testuser.basel" + UUID.randomUUID() + "@example.com";
        JsonWebTokenDataDTO jwtData = new JsonWebTokenDataDTO(
            null,
            UUID.randomUUID(),
            testemail,
            "Baseleti",
            "Basil",
            EmailAddress.fromString(testemail),
            "issuer",
            "076 123 45 67",
            "061 123 45 67",
            "Herr",
            TenantEnum.BS.name(),
            Set.of(FachRolle.SYSTEM_ADMIN, FachRolle.MPA)
        );
        AppFailureException ex = assertThrows(AppFailureException.class, () -> {
            service.createBenutzerFromJWT(jwtData);
        });

        // ASSERT
        assertThat(ex.getLocalizedMessage()).contains("NOT_POSSIBLE_ORGANISATION_ROLES_WITH_SYSTEM_ROLES");
    }
}
