package ch.dvbern.oss.hemed.esc.api.features.benutzer;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppAuthException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.ensure.JsonWebTokenDataDTO;
import ch.dvbern.oss.hemed.esc.api.features.organisation.Organisation;
import ch.dvbern.oss.hemed.esc.api.features.organisation.OrganisationRollen;
import ch.dvbern.oss.hemed.esc.api.features.organisation.OrganisationRollenService;
import ch.dvbern.oss.hemed.esc.api.features.organisation.OrganisationService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.Berechtigung;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.FachrollenService;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.BenutzerBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.OrganisationRollenBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBE;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
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
    private final FixtureService fixtureService;
    private final FachrollenService fachrollenService;
    private final OrganisationService organisationService;
    private final OrganisationRollenService organisationRollenService;
    private final MockMailbox mailbox;

    @BeforeEach
    void setUp() {
        mailbox.clear();
    }

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
    void berechtigungen_are_derived_from_fachrollen() {
        // ARRANGE
        tenantProducer.setCurrentTenant(TenantEnum.BS);
        Benutzer result = service.persist(BenutzerBuilder.create(
            builder -> builder
                .withRandomEmail("norbertneu@example.com")
                .withRandomVorname("Norbert")
                .withRandomNachname("Neu")
                .withKeycloakId(UUID.randomUUID().toString())
                .withSystemRolle(List.of(FachRolle.SYSTEM_ADMIN))
        ));

        // ACT
        result = fixtureService.readBenutzerWithBerechtigungen(result.getId());

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getOrganisationRollen()).isEmpty();
        assertThat(result.getSystemRollen()).isNotNull();
        assertThat(result.getSystemRollen().getFachRollen()).isNotEmpty();

        // Berechtigungen are now computed from FachRollen, not stored in DB
        Set<Berechtigung> computedBerechtigungen = result.getSystemRollen().getBerechtigungen(TenantEnum.BS);
        assertThat(computedBerechtigungen)
            .containsExactlyInAnyOrderElementsOf(FachRolle.collectAllBerechtigungen(
                TenantEnum.BS,
                FachRolle.SYSTEM_ADMIN));
    }

    @Test
    @TestAsAdminBS
    void findByEmail_returns_existing_benutzer() {
        // ARRANGE
        var testemail = "fbe-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        var emailAddress = EmailAddress.fromString(testemail);
        service.persist(BenutzerBuilder.create(
            builder -> builder
                .withEmail(emailAddress)
                .withRandomVorname("FindMe")
                .withRandomNachname("ByEmail")
                .withKeycloakId(UUID.randomUUID().toString())
        ));

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

    @Test
    @TestAsAdminBE
    void handleOrganisationsRollen_does_not_send_mail_when_organisationsRollen_empty_and_no_organisation() {
        // ARRANGE
        tenantProducer.setCurrentTenant(TenantEnum.BE);
        Benutzer benutzer = BenutzerBuilder.create(builder -> builder
            .withRandomEmail("no-org-rollen@example.com")
            .withRandomVorname("Ohne")
            .withRandomNachname("Organisation")
            .withKeycloakId(UUID.randomUUID().toString())
        );
        service.persist(benutzer);

        // ACT
        fachrollenService.handleOrganisationsRollen(EnumSet.noneOf(FachRolle.class), benutzer, null);

        // ASSERT
        assertThat(benutzer.getOrganisationRollen()).isEmpty();
        assertThat(mailbox.getTotalMessagesSent()).isZero();
    }

    @Test
    @TestAsAdminBE
    void handleOrganisationsRollen_does_not_send_mail_when_benutzer_is_verantwortlicher_arzt() {
        // ARRANGE
        tenantProducer.setCurrentTenant(TenantEnum.BE);
        Benutzer verantwortlicherArzt = BenutzerBuilder.create(builder -> builder
            .withRandomEmail("verantwortlich@example.com")
            .withRandomVorname("Vera")
            .withRandomNachname("Antwort")
            .withKeycloakId(UUID.randomUUID().toString())
        );
        service.persist(verantwortlicherArzt);

        Organisation organisation = new Organisation(verantwortlicherArzt, new HashSet<>());
        organisationService.persist(organisation);

        OrganisationRollen organisationRollen = OrganisationRollenBuilder.create(builder -> builder
            .withOrganisation(organisation)
            .withBenutzer(verantwortlicherArzt)
            .withFachrollen(EnumSet.of(FachRolle.SCHULAERZTIN))
        );
        organisationRollenService.persist(organisationRollen);
        verantwortlicherArzt.addOrganisation(organisationRollen);

        // ACT
        fachrollenService.handleOrganisationsRollen(
            EnumSet.of(FachRolle.SCHULAERZTIN), verantwortlicherArzt, organisation);

        // ASSERT
        assertThat(mailbox.getTotalMessagesSent()).isZero();
    }

    @Test
    @TestAsAdminBE
    void handleOrganisationsRollen_sends_mail_when_benutzer_is_not_verantwortlicher_arzt() {
        // ARRANGE
        tenantProducer.setCurrentTenant(TenantEnum.BE);
        Benutzer verantwortlicherArzt = BenutzerBuilder.create(builder -> builder
            .withRandomEmail("arzt@example.com")
            .withRandomVorname("Arzt")
            .withRandomNachname("Chef")
            .withKeycloakId(UUID.randomUUID().toString())
        );
        service.persist(verantwortlicherArzt);

        Organisation organisation = new Organisation(verantwortlicherArzt, new HashSet<>());
        organisationService.persist(organisation);

        OrganisationRollen arztOrgRollen = OrganisationRollenBuilder.create(builder -> builder
            .withOrganisation(organisation)
            .withBenutzer(verantwortlicherArzt)
            .withFachrollen(EnumSet.of(FachRolle.SCHULAERZTIN))
        );
        organisationRollenService.persist(arztOrgRollen);
        verantwortlicherArzt.addOrganisation(arztOrgRollen);

        Benutzer delegierterBenutzer = BenutzerBuilder.create(builder -> builder
            .withRandomEmail("delegiert@example.com")
            .withRandomVorname("Delegiert")
            .withRandomNachname("Benutzer")
            .withKeycloakId(UUID.randomUUID().toString())
        );
        service.persist(delegierterBenutzer);

        // ACT
        fachrollenService.handleOrganisationsRollen(
            EnumSet.of(FachRolle.MPA), delegierterBenutzer, organisation);

        // ASSERT: wait briefly for async mail sending to complete
        assertThat(mailbox.getTotalMessagesSent()).isPositive();
    }
}
