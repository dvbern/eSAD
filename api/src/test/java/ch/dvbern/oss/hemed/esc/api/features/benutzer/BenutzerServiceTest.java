package ch.dvbern.oss.hemed.esc.api.features.benutzer;

import java.util.*;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppAuthException;
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
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminWIN;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.from;

@QuarkusTest
@DisplayNameGeneration(ReplaceUnderscores.class)
class BenutzerServiceTest {

    @Inject
    protected BenutzerService service;

    @Inject
    protected TenantProducer tenantProducer;

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
    @TestAsAdminWIN
    void check_system_admin_win_has_required_roles() {
        // ARRANGE
        Set<Berechtigung> excludedBerechtigungen = EnumSet.of(Berechtigung.PUPIL_PUSH);
        Set<Berechtigung> expectedBerechtigungen = EnumSet.allOf(Berechtigung.class);
        expectedBerechtigungen.removeAll(excludedBerechtigungen);

        // ACT
        tenantProducer.setCurrentTenant(TenantEnum.WIN); // Default ist BS
        Optional<Set<Berechtigung>> berechtigungenOfSysAdmin =
            service.findBerechtigungenByEmail(EmailAddress.fromString("win-esc@dvbern.ch"));

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
    void check_system_admin_be_has_all_roles() {
        // ARRANGE
        Set<Berechtigung> excludedBerechtigungen = EnumSet.of(Berechtigung.PUPIL_PUSH);
        Set<Berechtigung> expectedBerechtigungen = EnumSet.allOf(Berechtigung.class);
        expectedBerechtigungen.removeAll(excludedBerechtigungen);

        // ACT
        tenantProducer.setCurrentTenant(TenantEnum.BE); // Default ist BS
        Optional<Set<Berechtigung>> berechtigungenOfSysAdmin =
            service.findBerechtigungenByEmail(EmailAddress.fromString("be-esc@dvbern.ch"));
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
            .containsExactlyInAnyOrderElementsOf(FachRolle.collectAllBerechtigungen(TenantEnum.BS, FachRolle.SYSTEM_ADMIN));
        service.findBerechtigungenByEmail(createDto.getEmail())
            .ifPresentOrElse(
                berechtigungen -> assertThat(berechtigungen)
                    .containsExactlyInAnyOrderElementsOf(FachRolle.collectAllBerechtigungen(TenantEnum.BS, FachRolle.SYSTEM_ADMIN)),
                () -> Assertions.fail("No berechtigungen found")
            );
    }
}
