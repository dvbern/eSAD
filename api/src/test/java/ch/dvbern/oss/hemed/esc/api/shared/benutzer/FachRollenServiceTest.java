package ch.dvbern.oss.hemed.esc.api.shared.benutzer;

import java.util.EnumSet;
import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.deployment.DeploymentEnvironment;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.SystemRollen;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.SystemRollenService;
import ch.dvbern.oss.hemed.esc.api.features.organisation.Organisation;
import ch.dvbern.oss.hemed.esc.api.features.organisation.OrganisationRollen;
import ch.dvbern.oss.hemed.esc.api.features.organisation.OrganisationRollenService;
import ch.dvbern.oss.hemed.esc.api.features.organisation.OrganisationService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
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

class FachRollenServiceTest {

    private FachrollenService fachrollenService;

    private OrganisationService organisationService;
    private OrganisationRollenService organisationRollenService;
    private SystemRollenService systemRollenService;
    private TenantSettingFeatures features;

    @BeforeEach
    void setUp() {
        TenantSettingsService tenantSettingsService = mock(TenantSettingsService.class);
        ConfigService configService = mock(ConfigService.class);
        organisationService = mock(OrganisationService.class);
        organisationRollenService = mock(OrganisationRollenService.class);
        systemRollenService = mock(SystemRollenService.class);
        FachrollenMailService fachrollenMailService = mock(FachrollenMailService.class);

        fachrollenService = new FachrollenService(
            organisationService,
            organisationRollenService,
            systemRollenService,
            configService,
            tenantSettingsService,
            fachrollenMailService
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
        AppFailureException ex = assertThrows(AppFailureException.class, () -> {
            fachrollenService.setFachrollenFromJWT(fachrollen, benutzer, organisation);
        });

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
        assertThat(benutzer.getOrganisationRollen().stream().findFirst().get().getFachRollen()).containsExactly(FachRolle.EMPFANG);
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

        when(organisationRollenService.createForOrganisation(organisation, benutzer, fachrollen)).thenReturn(organisationRollen);

        fachrollenService.setFachrollenFromJWT(fachrollen, benutzer, organisation);
        assertThat(benutzer.getOrganisationRollen()).isNotNull();
        assertThat(benutzer.getOrganisationRollen().stream().findFirst().get().getFachRollen()).containsExactly(FachRolle.MPA);
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

        when(organisationRollenService.createForOrganisation(organisation, benutzer, fachrollen)).thenReturn(organisationRollen);
        when(organisationService.getZentraleOrganisation()).thenReturn(organisation);

        fachrollenService.setFachrollenFromJWT(fachrollen, benutzer, null);
        assertThat(benutzer.getOrganisationRollen()).isNotNull();
        assertThat(benutzer.getOrganisationRollen().stream().findFirst().get().getFachRollen()).containsExactly(FachRolle.MPA);
        assertThat(benutzer.getOrganisationRollen().stream().findFirst().get().getOrganisation().getId().getId()).isEqualTo(organisation.getId().getId());
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
        when(organisationRollenService.createForOrganisation(organisation, benutzer, fachrollen)).thenReturn(organisationRollen);

        fachrollenService.setFachrollenFromJWT(fachrollen, benutzer, null);
        assertThat(benutzer.getOrganisationRollen()).isNotNull();
        assertThat(benutzer.getOrganisationRollen().stream().findFirst().get().getFachRollen()).containsExactly(FachRolle.SCHULAERZTIN);
        assertThat(benutzer.getOrganisationRollen().stream().findFirst().get().getOrganisation().getId().getId()).isEqualTo(organisation.getId().getId());
    }

    @Test
    void fail_setting_organisation_rollen_without_organisation_without_zentrale_organisation_without_schularzt() {
        setupZentraleOrganisation(false);

        Benutzer benutzer = BenutzerBuilder.defaultBenutzer();
        EnumSet<FachRolle> fachrollen = EnumSet.of(FachRolle.MPA);

        // ACT
        AppFailureException ex = assertThrows(AppFailureException.class, () -> {
            fachrollenService.setFachrollenFromJWT(fachrollen, benutzer, null);
        });

        // ASSERT
        assertThat(ex.getLocalizedMessage()).contains("ORGANISATION_ROLES_WITHOUT_ORGANISATION");
    }

    private void setupZentraleOrganisation(boolean zentral) {
        FeatureEnabledSetting setting = zentral ? FeatureEnabledSetting.ENABLED : FeatureEnabledSetting.DISABLED;
        when(features.getZentraleOrganisationFeature()).thenReturn(setting);
    }
}
