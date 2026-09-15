package ch.dvbern.oss.hemed.esc.api.shared.benutzer;

import java.util.EnumSet;
import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.deployment.DeploymentEnvironment;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.SystemRollen;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.SystemRollenService;
import ch.dvbern.oss.hemed.esc.api.features.organisation.Organisation;
import ch.dvbern.oss.hemed.esc.api.features.organisation.OrganisationRollen;
import ch.dvbern.oss.hemed.esc.api.features.organisation.OrganisationRollenService;
import ch.dvbern.oss.hemed.esc.api.features.organisation.OrganisationService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSetting;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSettingsService;
import ch.dvbern.oss.hemed.esc.api.shared.settings.features.FeatureEnabledSetting;
import ch.dvbern.oss.hemed.esc.api.shared.settings.features.TenantSettingFeatures;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.BenutzerBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.OrganisationBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.OrganisationRollenBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.SystemRollenBuilder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FachrollenServiceTest {

    private FachrollenService fachrollenService;

    private OrganisationService organisationService;
    private OrganisationRollenService organisationRollenService;
    private SystemRollenService systemRollenService;
    private TenantSettingFeatures features;
    private BenutzerService benutzerService;
    private TenantProducer tenantProducer;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        TenantSettingsService tenantSettingsService = mock(TenantSettingsService.class);
        ConfigService configService = mock(ConfigService.class);
        organisationService = mock(OrganisationService.class);
        organisationRollenService = mock(OrganisationRollenService.class);
        systemRollenService = mock(SystemRollenService.class);
        FachrollenMailService fachrollenMailService = mock(FachrollenMailService.class);
        benutzerService = mock(BenutzerService.class);
        tenantProducer = mock(TenantProducer.class);
        // Use BE tenant: SCHULAERZTIN grants BENUTZER_BERECHTIGUNGEN_SCHREIBEN there,
        // which lets us simulate a non-system caller with the required permission via an
        // organisation role.
        when(tenantProducer.produceTenant()).thenReturn(TenantEnum.BE);

        fachrollenService = new FachrollenService(
            organisationService,
            organisationRollenService,
            systemRollenService,
            configService,
            tenantSettingsService,
            fachrollenMailService,
            tenantProducer
        );

        TenantSetting tenantSetting = mock(TenantSetting.class);
        features = mock(TenantSettingFeatures.class);
        when(tenantSettingsService.getTenantSetting()).thenReturn(tenantSetting);
        when(tenantSetting.getTenantSettingFeatures()).thenReturn(features);
        when(configService.getEnvironment()).thenReturn(DeploymentEnvironment.LOCAL);
    }

    @Test
    void setting_organisation_and_system_rollen_should_fail() {
        setupZentraleOrganisation(false);

        Benutzer benutzer = BenutzerBuilder.defaultBenutzer();
        Organisation organisation = OrganisationBuilder.defaultOrganisation();
        EnumSet<FachRolle> fachrollen = EnumSet.of(FachRolle.MPA, FachRolle.SUPPORT);

        OrganisationRollen organisationRollen = OrganisationRollenBuilder.create(builder -> builder
            .withBenutzer(benutzer)
            .withOrganisation(organisation)
            .withFachrollen(EnumSet.of(FachRolle.MPA)));
        SystemRollen systemRollen = SystemRollenBuilder.create(builder -> builder
            .withBenutzer(benutzer)
            .withSystemrollen(EnumSet.of(FachRolle.SUPPORT)));

        when(organisationRollenService.createForOrganisation(
            organisation,
            benutzer,
            EnumSet.of(FachRolle.MPA))).thenReturn(organisationRollen);
        when(systemRollenService.createNewSystemRollen(
            benutzer,
            EnumSet.of(FachRolle.SUPPORT))).thenReturn(systemRollen);

        // ACT
        AppFailureException ex = assertThrows(
            AppFailureException.class,
            () -> fachrollenService.setFachrollenFromJWT(fachrollen, benutzer, organisation));

        // ASSERT
        assertThat(ex.getLocalizedMessage()).contains("NOT_POSSIBLE_ORGANISATION_ROLES_WITH_SYSTEM_ROLES");
    }

    @Test
    void setting_system_rollen_for_benutzer_without_system_rollen_should_work() {
        setupZentraleOrganisation(false);

        Benutzer benutzer = BenutzerBuilder.defaultBenutzer();
        EnumSet<FachRolle> fachrollen = EnumSet.of(FachRolle.SUPPORT);

        SystemRollen systemRollen = SystemRollenBuilder.create(builder -> builder
            .withBenutzer(benutzer)
            .withSystemrollen(EnumSet.of(FachRolle.SUPPORT)));

        when(systemRollenService.createNewSystemRollen(benutzer, fachrollen)).thenReturn(systemRollen);

        fachrollenService.setFachrollenFromJWT(fachrollen, benutzer, null);
        assertThat(benutzer.getSystemRollen()).isNotNull();
        assertThat(benutzer.getSystemRollen().getFachRollen()).contains(FachRolle.SUPPORT);
    }

    @Test
    void setting_system_rollen_for_benutzer_with_system_rollen_should_work() {
        setupZentraleOrganisation(false);

        Benutzer benutzer = BenutzerBuilder.create(builder -> builder.withSystemRolle(List.of(FachRolle.SYSTEM_ADMIN)));
        EnumSet<FachRolle> fachrollen = EnumSet.of(FachRolle.SUPPORT);

        SystemRollen systemRollen = SystemRollenBuilder.create(builder -> builder
            .withBenutzer(benutzer)
            .withSystemrollen(EnumSet.of(FachRolle.SUPPORT)));

        when(systemRollenService.createNewSystemRollen(benutzer, fachrollen)).thenReturn(systemRollen);

        fachrollenService.setFachrollenFromJWT(fachrollen, benutzer, null);
        assertThat(benutzer.getSystemRollen()).isNotNull();
        assertThat(benutzer.getSystemRollen().getFachRollen()).contains(FachRolle.SUPPORT);
    }

    @Test
    void setting_no_system_rollen_for_benutzer_with_system_rollen_should_delete_system_rollen() {
        setupZentraleOrganisation(false);

        Benutzer benutzer = BenutzerBuilder.create(builder -> builder.withSystemRolle(List.of(FachRolle.SYSTEM_ADMIN)));
        EnumSet<FachRolle> fachrollen = EnumSet.noneOf(FachRolle.class);

        SystemRollen systemRollen = SystemRollenBuilder.create(builder -> builder
            .withBenutzer(benutzer)
            .withSystemrollen(EnumSet.of(FachRolle.SUPPORT)));

        when(systemRollenService.createNewSystemRollen(benutzer, fachrollen)).thenReturn(systemRollen);

        fachrollenService.setFachrollenFromJWT(fachrollen, benutzer, null);
        assertThat(benutzer.getSystemRollen()).isNull();
    }

    @Test
    void setting_organisation_rollen_for_benutzer_with_existing_organisation_should_overwrite_them() {
        setupZentraleOrganisation(false);

        Organisation organisation = OrganisationBuilder.defaultOrganisation();
        Benutzer benutzer = BenutzerBuilder.create(builder -> builder
            .withOrganisationRollen(organisation, List.of(FachRolle.MPA))
        );

        fachrollenService.setFachrollenFromJWT(EnumSet.of(FachRolle.EMPFANG), benutzer, organisation);
        assertThat(benutzer.getOrganisationRollen()).isNotNull();
        assertThat(benutzer.getOrganisationRollen().stream().findFirst().get().getFachRollen()).containsExactly(
            FachRolle.EMPFANG);
    }

    @Test
    void setting_organisation_rollen_for_benutzer_without_rollen_should_work_when_you_enter_organisation() {
        setupZentraleOrganisation(false);

        Benutzer benutzer = BenutzerBuilder.defaultBenutzer();
        Organisation organisation = OrganisationBuilder.defaultOrganisation();
        EnumSet<FachRolle> fachrollen = EnumSet.of(FachRolle.MPA);

        OrganisationRollen organisationRollen = OrganisationRollenBuilder.create(builder -> builder
            .withBenutzer(benutzer)
            .withFachrollen(fachrollen));

        when(organisationRollenService.createForOrganisation(organisation, benutzer, fachrollen)).thenReturn(
            organisationRollen);

        fachrollenService.setFachrollenFromJWT(fachrollen, benutzer, organisation);
        assertThat(benutzer.getOrganisationRollen()).isNotNull();
        assertThat(benutzer.getOrganisationRollen().stream().findFirst().get().getFachRollen()).containsExactly(
            FachRolle.MPA);
    }

    @Test
    void setting_organisation_rollen_for_benutzer_without_rollen_should_work_with_zentrale_organisation() {
        setupZentraleOrganisation(true);

        Benutzer benutzer = BenutzerBuilder.defaultBenutzer();
        Organisation organisation = OrganisationBuilder.defaultOrganisation();
        EnumSet<FachRolle> fachrollen = EnumSet.of(FachRolle.MPA);

        OrganisationRollen organisationRollen = OrganisationRollenBuilder.create(builder -> builder
            .withBenutzer(benutzer)
            .withOrganisation(organisation)
            .withFachrollen(fachrollen));

        when(organisationRollenService.createForOrganisation(organisation, benutzer, fachrollen)).thenReturn(
            organisationRollen);
        when(organisationService.getZentraleOrganisation()).thenReturn(organisation);

        fachrollenService.setFachrollenFromJWT(fachrollen, benutzer, null);
        assertThat(benutzer.getOrganisationRollen()).isNotNull();
        assertThat(benutzer.getOrganisationRollen().stream().findFirst().get().getFachRollen()).containsExactly(
            FachRolle.MPA);
        assertThat(benutzer.getOrganisationRollen()
            .stream()
            .findFirst()
            .get()
            .getOrganisation()
            .getId()
            .getId()).isEqualTo(organisation.getId().getId());
    }

    @Test
    void setting_organisation_rollen_for_benutzer_without_rollen_should_work_without_organisation_for_schularzt() {
        setupZentraleOrganisation(false);

        Benutzer benutzer = BenutzerBuilder.defaultBenutzer();
        Organisation organisation = OrganisationBuilder.defaultOrganisation();
        EnumSet<FachRolle> fachrollen = EnumSet.of(FachRolle.SCHULAERZTIN);

        OrganisationRollen organisationRollen = OrganisationRollenBuilder.create(builder -> builder
            .withBenutzer(benutzer)
            .withOrganisation(organisation)
            .withFachrollen(fachrollen));

        when(organisationService.createOrganisationForBenutzer(benutzer)).thenReturn(organisation);
        when(organisationRollenService.createForOrganisation(organisation, benutzer, fachrollen)).thenReturn(
            organisationRollen);

        fachrollenService.setFachrollenFromJWT(fachrollen, benutzer, null);
        assertThat(benutzer.getOrganisationRollen()).isNotNull();
        assertThat(benutzer.getOrganisationRollen().stream().findFirst().get().getFachRollen()).containsExactly(
            FachRolle.SCHULAERZTIN);
        assertThat(benutzer.getOrganisationRollen()
            .stream()
            .findFirst()
            .get()
            .getOrganisation()
            .getId()
            .getId()).isEqualTo(organisation.getId().getId());
    }

    @Test
    void fail_setting_organisation_rollen_without_organisation_without_zentrale_organisation_without_schularzt() {
        setupZentraleOrganisation(false);

        Benutzer benutzer = BenutzerBuilder.defaultBenutzer();
        EnumSet<FachRolle> fachrollen = EnumSet.of(FachRolle.MPA);

        // ACT
        AppFailureException ex = assertThrows(
            AppFailureException.class,
            () -> fachrollenService.setFachrollenFromJWT(fachrollen, benutzer, null));

        // ASSERT
        assertThat(ex.getLocalizedMessage()).contains("ORGANISATION_ROLES_WITHOUT_ORGANISATION");
    }

    @Test
    void setting_system_rollen_for_benutzer_with_same_system_rollen_should_not_change_them() {
        setupZentraleOrganisation(false);

        // ARRANGE: benutzer already has SUPPORT system role
        Benutzer benutzer = BenutzerBuilder.create(builder -> builder.withSystemRolle(List.of(FachRolle.SUPPORT)));
        EnumSet<FachRolle> sameFachrollen = EnumSet.of(FachRolle.SUPPORT);

        // ACT
        fachrollenService.handleSystemRollen(sameFachrollen, benutzer);

        // ASSERT: FachRollen remain unchanged (same instance, setFachRollen never called)
        assertThat(benutzer.getSystemRollen()).isNotNull();
        assertThat(benutzer.getSystemRollen().getFachRollen()).containsExactly(FachRolle.SUPPORT);
    }

    private void setupZentraleOrganisation(boolean zentral) {
        FeatureEnabledSetting setting = zentral ? FeatureEnabledSetting.ENABLED : FeatureEnabledSetting.DISABLED;
        when(features.getZentraleOrganisationFeature()).thenReturn(setting);
    }

    // ---------------------------------------------------------------------------
    // validateUpdateRollen / handleOrganisationsRollen authorization rules
    // ---------------------------------------------------------------------------

    @Test
    void systemUserCanAddNonDelegableOrganisationRolleToOtherUser() {
        // System users may add a non-delegable role (e.g. SCHULAERZTIN).
        setupZentraleOrganisation(false);

        Organisation organisation = OrganisationBuilder.defaultOrganisation();
        Benutzer currentUser = BenutzerBuilder.create(b -> b
            .withID(ID.random(Benutzer.class))
            .withSystemRolle(List.of(FachRolle.SYSTEM_ADMIN)));
        Benutzer target = BenutzerBuilder.create(b -> b.withID(ID.random(Benutzer.class)));

        when(benutzerService.getCurrentBenutzerOrThrowException()).thenReturn(currentUser);
        OrganisationRollen newRollen = OrganisationRollenBuilder.create(b -> b
            .withBenutzer(target).withOrganisation(organisation)
            .withFachrollen(EnumSet.of(FachRolle.SCHULAERZTIN)));
        when(organisationRollenService.createForOrganisation(organisation, target, EnumSet.of(FachRolle.SCHULAERZTIN)))
            .thenReturn(newRollen);

        fachrollenService.handleOrganisationsRollenAndSendInviteMailIfNewUser(
            EnumSet.of(FachRolle.SCHULAERZTIN),
            target,
            organisation,
            currentUser);

        assertThat(target.getOrganisationRollen()).hasSize(1);
        assertThat(target.getOrganisationRollen().iterator().next().getFachRollen())
            .containsExactly(FachRolle.SCHULAERZTIN);
    }

    @Test
    void systemUserCanRemoveNonDelegableOrganisationRolleFromOtherUser() {
        setupZentraleOrganisation(false);

        Organisation organisation = OrganisationBuilder.defaultOrganisation();
        Benutzer currentUser = BenutzerBuilder.create(b -> b
            .withID(ID.random(Benutzer.class))
            .withSystemRolle(List.of(FachRolle.SYSTEM_ADMIN)));
        Benutzer target = BenutzerBuilder.create(b -> b
            .withID(ID.random(Benutzer.class))
            .withOrganisationRollen(organisation, List.of(FachRolle.SCHULAERZTIN)));

        when(benutzerService.getCurrentBenutzerOrThrowException()).thenReturn(currentUser);

        // remove all roles -> empty set
        fachrollenService.handleOrganisationsRollenAndSendInviteMailIfNewUser(
            EnumSet.noneOf(FachRolle.class),
            target,
            organisation,
            currentUser);

        assertThat(target.getOrganisationRollen().iterator().next().getFachRollen()).isEmpty();
    }

    @Test
    void nonSystemUserWithPermissionCanAddDelegableRoleInSameOrganisation() {
        setupZentraleOrganisation(false);

        Organisation organisation = OrganisationBuilder.defaultOrganisation();
        // Current user has SCHULAERZTIN in same organisation -> grants BENUTZER_BERECHTIGUNGEN_SCHREIBEN in BE
        Benutzer currentUser = BenutzerBuilder.create(b -> b
            .withID(ID.random(Benutzer.class))
            .withOrganisationRollen(organisation, List.of(FachRolle.SCHULAERZTIN)));
        Benutzer target = BenutzerBuilder.create(b -> b.withID(ID.random(Benutzer.class)));

        when(benutzerService.getCurrentBenutzerOrThrowException()).thenReturn(currentUser);
        OrganisationRollen newRollen = OrganisationRollenBuilder.create(b -> b
            .withBenutzer(target).withOrganisation(organisation)
            .withFachrollen(EnumSet.of(FachRolle.MPA)));
        when(organisationRollenService.createForOrganisation(organisation, target, EnumSet.of(FachRolle.MPA)))
            .thenReturn(newRollen);

        fachrollenService.handleOrganisationsRollenAndSendInviteMailIfNewUser(
            EnumSet.of(FachRolle.MPA),
            target,
            organisation,
            currentUser);

        assertThat(target.getOrganisationRollen()).hasSize(1);
        assertThat(target.getOrganisationRollen().iterator().next().getFachRollen())
            .containsExactly(FachRolle.MPA);
    }

    @Test
    void nonSystemUserWithPermissionCanRemoveDelegableRoleInSameOrganisation() {
        setupZentraleOrganisation(false);

        Organisation organisation = OrganisationBuilder.defaultOrganisation();
        Benutzer currentUser = BenutzerBuilder.create(b -> b
            .withID(ID.random(Benutzer.class))
            .withOrganisationRollen(organisation, List.of(FachRolle.SCHULAERZTIN)));
        Benutzer target = BenutzerBuilder.create(b -> b
            .withID(ID.random(Benutzer.class))
            .withOrganisationRollen(organisation, List.of(FachRolle.MPA)));

        when(benutzerService.getCurrentBenutzerOrThrowException()).thenReturn(currentUser);

        fachrollenService.handleOrganisationsRollenAndSendInviteMailIfNewUser(
            EnumSet.noneOf(FachRolle.class),
            target,
            organisation,
            currentUser);

        assertThat(target.getOrganisationRollen().iterator().next().getFachRollen()).isEmpty();
    }

    @Test
    void nonSystemUserWithPermissionCannotAddNonDelegableRole() {
        setupZentraleOrganisation(false);

        Organisation organisation = OrganisationBuilder.defaultOrganisation();
        Benutzer currentUser = BenutzerBuilder.create(b -> b
            .withID(ID.random(Benutzer.class))
            .withOrganisationRollen(organisation, List.of(FachRolle.SCHULAERZTIN)));
        Benutzer target = BenutzerBuilder.create(b -> b.withID(ID.random(Benutzer.class)));

        when(benutzerService.getCurrentBenutzerOrThrowException()).thenReturn(currentUser);
        EnumSet<FachRolle> requestedRollen = EnumSet.of(FachRolle.SCHULAERZTIN);

        AppFailureException ex = assertThrows(
            AppFailureException.class, () ->
                fachrollenService.handleOrganisationsRollenAndSendInviteMailIfNewUser(
                    requestedRollen,
                    target,
                    organisation,
                    currentUser));

        assertThat(ex.getLocalizedMessage()).contains("UPDATE_FACHROLLEN_NOT_ALLOWED");
    }

    @Test
    void nonSystemUserWithPermissionCannotRemoveNonDelegableRole() {
        setupZentraleOrganisation(false);

        Organisation organisation = OrganisationBuilder.defaultOrganisation();
        Benutzer currentUser = BenutzerBuilder.create(b -> b
            .withID(ID.random(Benutzer.class))
            .withOrganisationRollen(organisation, List.of(FachRolle.SCHULAERZTIN)));
        Benutzer target = BenutzerBuilder.create(b -> b
            .withID(ID.random(Benutzer.class))
            .withOrganisationRollen(organisation, List.of(FachRolle.SCHULAERZTIN)));

        when(benutzerService.getCurrentBenutzerOrThrowException()).thenReturn(currentUser);
        EnumSet<FachRolle> requestedRollen = EnumSet.noneOf(FachRolle.class);

        AppFailureException ex = assertThrows(
            AppFailureException.class, () ->
                fachrollenService.handleOrganisationsRollenAndSendInviteMailIfNewUser(
                    requestedRollen,
                    target,
                    organisation,
                    currentUser));

        assertThat(ex.getLocalizedMessage()).contains("UPDATE_FACHROLLEN_NOT_ALLOWED");
    }

    @Test
    void nonSystemUserWithRolesInDifferentOrganisationCannotChangeRoles() {
        setupZentraleOrganisation(false);

        Organisation orgTarget = OrganisationBuilder.defaultOrganisation();
        Organisation orgOther = OrganisationBuilder.defaultOrganisation();
        Benutzer currentUser = BenutzerBuilder.create(b -> b
            .withID(ID.random(Benutzer.class))
            .withOrganisationRollen(orgOther, List.of(FachRolle.SCHULAERZTIN)));
        Benutzer target = BenutzerBuilder.create(b -> b.withID(ID.random(Benutzer.class)));

        when(benutzerService.getCurrentBenutzerOrThrowException()).thenReturn(currentUser);
        EnumSet<FachRolle> requestedRollen = EnumSet.of(FachRolle.MPA);

        AppFailureException ex = assertThrows(
            AppFailureException.class, () ->
                fachrollenService.handleOrganisationsRollenAndSendInviteMailIfNewUser(
                    requestedRollen,
                    target,
                    orgTarget,
                    currentUser));

        assertThat(ex.getLocalizedMessage()).contains("UPDATE_FACHROLLEN_NOT_ALLOWED");
    }

    @Test
    void nonSystemUserWithoutRequiredBerechtigungCannotChangeRoles() {
        setupZentraleOrganisation(false);

        Organisation organisation = OrganisationBuilder.defaultOrganisation();
        // MPA does NOT grant BENUTZER_BERECHTIGUNGEN_SCHREIBEN
        Benutzer currentUser = BenutzerBuilder.create(b -> b
            .withID(ID.random(Benutzer.class))
            .withOrganisationRollen(organisation, List.of(FachRolle.MPA)));
        Benutzer target = BenutzerBuilder.create(b -> b.withID(ID.random(Benutzer.class)));

        when(benutzerService.getCurrentBenutzerOrThrowException()).thenReturn(currentUser);
        EnumSet<FachRolle> requestedRollen = EnumSet.of(FachRolle.EMPFANG);

        AppFailureException ex = assertThrows(
            AppFailureException.class, () ->
                fachrollenService.handleOrganisationsRollenAndSendInviteMailIfNewUser(
                    requestedRollen,
                    target,
                    organisation,
                    currentUser));

        assertThat(ex.getLocalizedMessage()).contains("UPDATE_FACHROLLEN_NOT_ALLOWED");
    }

    @Test
    void userCannotChangeOwnOrganisationsRollen() {
        setupZentraleOrganisation(false);

        Organisation organisation = OrganisationBuilder.defaultOrganisation();
        ID<Benutzer> sharedId = ID.random(Benutzer.class);
        Benutzer currentUser = BenutzerBuilder.create(b -> b
            .withID(sharedId)
            .withOrganisationRollen(organisation, List.of(FachRolle.SCHULAERZTIN)));
        // target is the same user (same id)
        Benutzer target = currentUser;

        when(benutzerService.getCurrentBenutzerOrThrowException()).thenReturn(currentUser);
        EnumSet<FachRolle> requestedRollen = EnumSet.of(FachRolle.MPA);

        AppFailureException ex = assertThrows(
            AppFailureException.class, () ->
                fachrollenService.handleOrganisationsRollenAndSendInviteMailIfNewUser(
                    requestedRollen,
                    target,
                    organisation,
                    currentUser));

        assertThat(ex.getLocalizedMessage()).contains("UPDATE_FACHROLLEN_NOT_ALLOWED");
    }

    @Test
    void creatingRollenForTargetWithoutExistingOrganisationsRollenTreatsAllAsAdded() {
        setupZentraleOrganisation(false);

        Organisation organisation = OrganisationBuilder.defaultOrganisation();
        Benutzer currentUser = BenutzerBuilder.create(b -> b
            .withID(ID.random(Benutzer.class))
            .withOrganisationRollen(organisation, List.of(FachRolle.SCHULAERZTIN)));
        Benutzer target = BenutzerBuilder.create(b -> b.withID(ID.random(Benutzer.class)));

        when(benutzerService.getCurrentBenutzerOrThrowException()).thenReturn(currentUser);
        EnumSet<FachRolle> requestedRollen = EnumSet.of(FachRolle.MPA, FachRolle.SCHULAERZTIN);

        // SCHULAERZTIN is not delegable -> all requested roles count as added -> reject
        AppFailureException ex = assertThrows(
            AppFailureException.class, () ->
                fachrollenService.handleOrganisationsRollenAndSendInviteMailIfNewUser(
                    requestedRollen, target, organisation, currentUser));

        assertThat(ex.getLocalizedMessage()).contains("UPDATE_FACHROLLEN_NOT_ALLOWED");
    }

    @Test
    void removingAllOrganisationsRollenTreatsAllExistingAsRemoved() {
        setupZentraleOrganisation(false);

        Organisation organisation = OrganisationBuilder.defaultOrganisation();
        Benutzer currentUser = BenutzerBuilder.create(b -> b
            .withID(ID.random(Benutzer.class))
            .withOrganisationRollen(organisation, List.of(FachRolle.MPA)));
        // target has delegable + non-delegable roles -> removing all must still be rejected
        Benutzer target = BenutzerBuilder.create(b -> b
            .withID(ID.random(Benutzer.class))
            .withOrganisationRollen(organisation, List.of(FachRolle.MPA)));

        when(benutzerService.getCurrentBenutzerOrThrowException()).thenReturn(currentUser);
        EnumSet<FachRolle> requestedRollen = EnumSet.noneOf(FachRolle.class);

        AppFailureException ex = assertThrows(
            AppFailureException.class, () ->
                fachrollenService.handleOrganisationsRollenAndSendInviteMailIfNewUser(
                    requestedRollen,
                    target,
                    organisation,
                    currentUser));

        assertThat(ex.getLocalizedMessage()).contains("UPDATE_FACHROLLEN_NOT_ALLOWED");
    }

    @Test
    void keepingExistingNonDelegableRoleUnchangedIsAllowed() {
        setupZentraleOrganisation(false);

        Organisation organisation = OrganisationBuilder.defaultOrganisation();
        Benutzer currentUser = BenutzerBuilder.create(b -> b
            .withID(ID.random(Benutzer.class))
            .withOrganisationRollen(organisation, List.of(FachRolle.SCHULAERZTIN)));
        // target has SCHULAERZTIN (non-delegable) and MPA. We keep SCHULAERZTIN, remove MPA.
        Benutzer target = BenutzerBuilder.create(b -> b
            .withID(ID.random(Benutzer.class))
            .withOrganisationRollen(organisation, List.of(FachRolle.SCHULAERZTIN, FachRolle.MPA)));

        when(benutzerService.getCurrentBenutzerOrThrowException()).thenReturn(currentUser);

        fachrollenService.handleOrganisationsRollenAndSendInviteMailIfNewUser(
            EnumSet.of(FachRolle.SCHULAERZTIN),
            target,
            organisation,
            currentUser);

        assertThat(target.getOrganisationRollen().iterator().next().getFachRollen())
            .containsExactly(FachRolle.SCHULAERZTIN);
    }

    @Test
    void noOpUpdateDoesNotFailEvenWhenExistingRollenContainNonDelegable() {
        setupZentraleOrganisation(false);

        Organisation organisation = OrganisationBuilder.defaultOrganisation();
        Benutzer currentUser = BenutzerBuilder.create(b -> b
            .withID(ID.random(Benutzer.class))
            .withOrganisationRollen(organisation, List.of(FachRolle.SCHULAERZTIN)));
        Benutzer target = BenutzerBuilder.create(b -> b
            .withID(ID.random(Benutzer.class))
            .withOrganisationRollen(organisation, List.of(FachRolle.SCHULAERZTIN, FachRolle.MPA)));

        when(benutzerService.getCurrentBenutzerOrThrowException()).thenReturn(currentUser);

        // No-op: requested set equals existing set
        fachrollenService.handleOrganisationsRollenAndSendInviteMailIfNewUser(
            EnumSet.of(FachRolle.SCHULAERZTIN, FachRolle.MPA), target, organisation, currentUser);

        assertThat(target.getOrganisationRollen().iterator().next().getFachRollen())
            .containsExactlyInAnyOrder(FachRolle.SCHULAERZTIN, FachRolle.MPA);
    }
}
