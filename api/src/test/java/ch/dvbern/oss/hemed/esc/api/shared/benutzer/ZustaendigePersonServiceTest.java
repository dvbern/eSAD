package ch.dvbern.oss.hemed.esc.api.shared.benutzer;

import java.util.EnumSet;
import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.deployment.DeploymentEnvironment;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungService;
import ch.dvbern.oss.hemed.esc.api.features.archivierung.ArchivierungTyp;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.DossierService;
import ch.dvbern.oss.hemed.esc.api.features.organisation.Organisation;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.shared.archivierung.ArchivierungService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.dossierqueue.DossierQueueService;
import ch.dvbern.oss.hemed.esc.api.shared.dossierqueue.DossierQueueTyp;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSetting;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSettingsService;
import ch.dvbern.oss.hemed.esc.api.shared.settings.features.FeatureEnabledSetting;
import ch.dvbern.oss.hemed.esc.api.shared.settings.features.TenantSettingFeatures;
import ch.dvbern.oss.hemed.esc.api.shared.util.VerantwortlicherSchuleChangedDTO;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.BenutzerBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.DossierBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.KindBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.OrganisationBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.OrganisationRollenBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.SchuleBuilder;
import java.util.UUID;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.oss.hemed.esc.api.shared.util.HasZustaendigkeit;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ZustaendigePersonServiceTest {

    private BenutzerService benutzerService;
    private DossierService dossierService;
    private ArchivierungService archivierungService;
    private ObjectMapper objectMapper;
    private DossierQueueService dossierQueueService;
    private EinladungService einladungService;
    private AnlassService anlassService;
    private ZustaendigePersonService service;
    private TenantSettingFeatures features;

    @BeforeEach
    void setUp() {
        benutzerService = mock(BenutzerService.class);
        dossierService = mock(DossierService.class);
        TenantSettingsService tenantSettingsService = mock(TenantSettingsService.class);
        ConfigService configService = mock(ConfigService.class);
        archivierungService = mock(ArchivierungService.class);
        objectMapper = new ObjectMapper().registerModule(new ParameterNamesModule());
        dossierQueueService = mock(DossierQueueService.class);
        einladungService = mock(EinladungService.class);
        anlassService = mock(AnlassService.class);

        service = new ZustaendigePersonService(
            benutzerService,
            dossierService,
            tenantSettingsService,
            configService,
            archivierungService,
            objectMapper,
            dossierQueueService,
            einladungService,
            anlassService
        );

        TenantSetting tenantSetting = mock(TenantSetting.class);
        features = mock(TenantSettingFeatures.class);
        when(tenantSettingsService.getTenantSetting()).thenReturn(tenantSetting);
        when(tenantSetting.getTenantSettingFeatures()).thenReturn(features);
        when(configService.getEnvironment()).thenReturn(DeploymentEnvironment.LOCAL);
    }

    private void setupZentraleOrganisation(boolean zentral) {
        FeatureEnabledSetting setting = zentral ? FeatureEnabledSetting.ENABLED : FeatureEnabledSetting.DISABLED;
        when(features.getZentraleOrganisationFeature()).thenReturn(setting);
    }

    /**
     * Erstellt einen Benutzer mit der angegebenen Organisation und der Rolle SCHULAERZTIN.
     * <p>
     * Da die neue Zuständigkeits-Logik organisations-basiert ist, brauchen die meisten Tests
     * Benutzer, die explizit einer Organisation zugeordnet sind, damit der Vergleich der
     * {@code singleOrganisation} überhaupt funktioniert.
     */
    @NonNull
    private static Benutzer buildArztInOrganisation(@NonNull String nachname, @NonNull Organisation organisation) {
        return BenutzerBuilder.create(b -> b
            .withNachname(nachname)
            .withOrganisationRollen(organisation, List.of(FachRolle.SCHULAERZTIN)));
    }

    @Nested
    class RequireZustaendigkeit {

        @Test
        void systemBerechtigung_shouldNotThrow() {
            // ARRANGE
            Benutzer systemUser = BenutzerBuilder.create(b -> b.withSystemRolle(EnumSet.of(FachRolle.ADMIN)));
            when(benutzerService.getCurrentBenutzerOrThrowException()).thenReturn(systemUser);

            Dossier dossier = DossierBuilder.create(d -> {});

            // ACT & ASSERT
            assertThatCode(() -> service.requireImmediateZustaendigkeit(dossier))
                .doesNotThrowAnyException();
        }

        @Test
        void noOrganisationOnEntity_shouldNotThrow() {
            // ARRANGE
            Benutzer normalUser = BenutzerBuilder.create(b -> {});
            when(benutzerService.getCurrentBenutzerOrThrowException()).thenReturn(normalUser);

            HasZustaendigkeit entity = mock(HasZustaendigkeit.class);
            when(entity.getSingleOrganisation()).thenReturn(null);

            // ACT & ASSERT
            assertThatThrownBy(() -> service.requireImmediateZustaendigkeit(entity))
                .isInstanceOf(AppValidationException.class);
        }

        @Test
        void userHasOrganisationBerechtigung_shouldNotThrow() {
            // ARRANGE
            Organisation org = OrganisationBuilder.create(o -> {});
            Benutzer user = BenutzerBuilder.create(b -> {});
            user.addOrganisation(OrganisationRollenBuilder.create(r -> r.withOrganisation(org).withBenutzer(user)));

            when(benutzerService.getCurrentBenutzerOrThrowException()).thenReturn(user);

            HasZustaendigkeit entity = mock(HasZustaendigkeit.class);
            when(entity.getSingleOrganisation()).thenReturn(org);

            // ACT & ASSERT
            assertThatCode(() -> service.requireImmediateZustaendigkeit(entity))
                .doesNotThrowAnyException();
        }

        @Test
        void userHasNOOrganisationBerechtigung_shouldThrow() {
            // ARRANGE
            Organisation orgEntity = OrganisationBuilder.create(o -> {});
            orgEntity.setId(ID.of(UUID.randomUUID(), Organisation.class));
            Organisation orgUser = OrganisationBuilder.create(o -> {});
            orgUser.setId(ID.of(UUID.randomUUID(), Organisation.class));

            Benutzer user = BenutzerBuilder.create(b -> {});
            user.addOrganisation(OrganisationRollenBuilder.create(r -> r.withOrganisation(orgUser).withBenutzer(user)));

            when(benutzerService.getCurrentBenutzerOrThrowException()).thenReturn(user);

            HasZustaendigkeit entity = mock(HasZustaendigkeit.class);
            when(entity.getSingleOrganisation()).thenReturn(orgEntity);

            // ACT & ASSERT
            assertThatThrownBy(() -> service.requireImmediateZustaendigkeit(entity))
                .isInstanceOf(AppValidationException.class);
        }
    }

    @Nested
    class IsBenutzerZustaendigForDossier {

        @Test
        void benutzerWithSupportRole_shouldAlwaysReturnTrue() {
            // ARRANGE: SUPPORT-Benutzer haben pauschalen Zugriff auf alle Dossiers,
            // unabhängig von Organisation oder Schule
            Benutzer supportUser = BenutzerBuilder.create(b -> b
                .withNachname("Support")
                .withSystemRolle(EnumSet.of(FachRolle.SUPPORT)));
            Dossier dossier = DossierBuilder.create(d -> {});

            // ACT
            boolean result = dossier.isBenutzerZustaendigForDossier(supportUser);

            // ASSERT
            assertThat(result)
                .as("SUPPORT-Benutzer ist immer für jedes Dossier zuständig")
                .isTrue();
        }

        @Test
        void benutzerWithoutOrganisation_shouldReturnFalse() {
            // ARRANGE: Benutzer ohne Organisation und ohne Support-Rolle
            Benutzer benutzer = BenutzerBuilder.create(b -> b.withNachname("OhneOrg"));
            Dossier dossier = DossierBuilder.create(d -> {});

            // ACT
            boolean result = dossier.isBenutzerZustaendigForDossier(benutzer);

            // ASSERT
            assertThat(result)
                .as("Benutzer ohne Organisation kann nicht zuständig sein")
                .isFalse();
        }

        @Test
        void benutzerSameOrganisationAsDossierArzt_shouldReturnTrue() {
            // ARRANGE: Benutzer und der zust. Arzt des Dossiers sind in derselben Organisation
            Organisation org = OrganisationBuilder.create(o -> {});
            org.setId(ID.of(UUID.randomUUID(), Organisation.class));

            Benutzer dossierArzt = BenutzerBuilder.create(b -> b.withNachname("DossierArzt"));
            dossierArzt.addOrganisation(
                OrganisationRollenBuilder.create(r -> r
                    .withOrganisation(org)
                    .withBenutzer(dossierArzt)
                    .withFachrollen(EnumSet.of(FachRolle.SCHULAERZTIN))));

            Benutzer benutzer = BenutzerBuilder.create(b -> b.withNachname("KollegeInGleicherOrg"));
            benutzer.addOrganisation(
                OrganisationRollenBuilder.create(r -> r
                    .withOrganisation(org)
                    .withBenutzer(benutzer)
                    .withFachrollen(EnumSet.of(FachRolle.SCHULAERZTIN))));

            Dossier dossier = DossierBuilder.create(d -> d.withZustaendigArzt(dossierArzt));

            // ACT
            boolean result = dossier.isBenutzerZustaendigForDossier(benutzer);

            // ASSERT
            assertThat(result)
                .as("Benutzer in gleicher Organisation wie der Dossier-Arzt ist zuständig")
                .isTrue();
        }

        @Test
        void benutzerSameOrganisationAsSchuleArzt_shouldReturnTrue() {
            // ARRANGE: Dossier-Arzt ist null (oder andere Org), aber Benutzer ist in derselben
            // Organisation wie der zuständige Arzt der Schule
            Organisation schulArztOrg = OrganisationBuilder.create(o -> {});
            schulArztOrg.setId(ID.of(UUID.randomUUID(), Organisation.class));

            Benutzer schulArzt = BenutzerBuilder.create(b -> b.withNachname("SchulArzt"));
            schulArzt.addOrganisation(
                OrganisationRollenBuilder.create(r -> r
                    .withOrganisation(schulArztOrg)
                    .withBenutzer(schulArzt)
                    .withFachrollen(EnumSet.of(FachRolle.SCHULAERZTIN))));

            Benutzer benutzer = BenutzerBuilder.create(b -> b.withNachname("KollegeSchule"));
            benutzer.addOrganisation(
                OrganisationRollenBuilder.create(r -> r
                    .withOrganisation(schulArztOrg)
                    .withBenutzer(benutzer)
                    .withFachrollen(EnumSet.of(FachRolle.SCHULAERZTIN))));

            Schule schule = SchuleBuilder.create(s -> s.withZustaendigArzt(schulArzt));
            Dossier dossier = DossierBuilder.create(d -> d
                .forKind(KindBuilder.create(k -> k.withSchule(schule))));

            // ACT
            boolean result = dossier.isBenutzerZustaendigForDossier(benutzer);

            // ASSERT
            assertThat(result)
                .as("Benutzer in gleicher Organisation wie der Schul-Arzt ist zuständig")
                .isTrue();
        }

        @Test
        void benutzerOrganisationDifferentFromDossierAndSchule_shouldReturnFalse() {
            // ARRANGE: Benutzer-Org unterscheidet sich von Dossier-Arzt-Org UND Schul-Arzt-Org
            Organisation dossierOrg = OrganisationBuilder.create(o -> {});
            dossierOrg.setId(ID.of(UUID.randomUUID(), Organisation.class));
            Organisation schulOrg = OrganisationBuilder.create(o -> {});
            schulOrg.setId(ID.of(UUID.randomUUID(), Organisation.class));
            Organisation otherOrg = OrganisationBuilder.create(o -> {});
            otherOrg.setId(ID.of(UUID.randomUUID(), Organisation.class));

            Benutzer dossierArzt = BenutzerBuilder.create(b -> b.withNachname("DossierArzt"));
            dossierArzt.addOrganisation(
                OrganisationRollenBuilder.create(r -> r
                    .withOrganisation(dossierOrg)
                    .withBenutzer(dossierArzt)
                    .withFachrollen(EnumSet.of(FachRolle.SCHULAERZTIN))));

            Benutzer schulArzt = BenutzerBuilder.create(b -> b.withNachname("SchulArzt"));
            schulArzt.addOrganisation(
                OrganisationRollenBuilder.create(r -> r
                    .withOrganisation(schulOrg)
                    .withBenutzer(schulArzt)
                    .withFachrollen(EnumSet.of(FachRolle.SCHULAERZTIN))));

            Benutzer benutzer = BenutzerBuilder.create(b -> b.withNachname("Fremder"));
            benutzer.addOrganisation(
                OrganisationRollenBuilder.create(r -> r
                    .withOrganisation(otherOrg)
                    .withBenutzer(benutzer)
                    .withFachrollen(EnumSet.of(FachRolle.SCHULAERZTIN))));

            Schule schule = SchuleBuilder.create(s -> s.withZustaendigArzt(schulArzt));
            Dossier dossier = DossierBuilder.create(d -> d
                .withZustaendigArzt(dossierArzt)
                .forKind(KindBuilder.create(k -> k.withSchule(schule))));

            // ACT
            boolean result = dossier.isBenutzerZustaendigForDossier(benutzer);

            // ASSERT
            assertThat(result)
                .as("Benutzer in fremder Organisation darf nicht zuständig sein")
                .isFalse();
        }

        @Test
        void schuleIsNull_shouldFallbackToDossierOrganisationOnly() {
            // ARRANGE: Das Kind hat keine Schule, also wird nur die Dossier-Arzt-Organisation geprüft.
            // Dies sichert ab, dass `dossier.getKind().getSchule()` ohne NPE behandelt wird.
            Organisation dossierOrg = OrganisationBuilder.create(o -> {});
            dossierOrg.setId(ID.of(UUID.randomUUID(), Organisation.class));

            Benutzer dossierArzt = BenutzerBuilder.create(b -> b.withNachname("DossierArzt"));
            dossierArzt.addOrganisation(
                OrganisationRollenBuilder.create(r -> r
                    .withOrganisation(dossierOrg)
                    .withBenutzer(dossierArzt)
                    .withFachrollen(EnumSet.of(FachRolle.SCHULAERZTIN))));

            Benutzer benutzer = BenutzerBuilder.create(b -> b.withNachname("KollegeOhneSchule"));
            benutzer.addOrganisation(
                OrganisationRollenBuilder.create(r -> r
                    .withOrganisation(dossierOrg)
                    .withBenutzer(benutzer)
                    .withFachrollen(EnumSet.of(FachRolle.SCHULAERZTIN))));

            // Default-Kind hat keine Schule
            Dossier dossier = DossierBuilder.create(d -> d.withZustaendigArzt(dossierArzt));
            assertThat(dossier.getKind().getSchule())
                .as("Vorbedingung: Default-Kind hat keine Schule gesetzt")
                .isNull();

            // ACT
            boolean result = dossier.isBenutzerZustaendigForDossier(benutzer);

            // ASSERT
            assertThat(result)
                .as("Auch ohne Schule am Kind sollte über die Dossier-Org Zuständigkeit erkannt werden")
                .isTrue();
        }

        @Test
        void noArztAnywhere_shouldReturnFalse() {
            // ARRANGE: Weder Dossier noch Schule haben einen zuständigen Arzt -> keine Organisation ableitbar
            Organisation org = OrganisationBuilder.create(o -> {});
            org.setId(ID.of(UUID.randomUUID(), Organisation.class));

            Benutzer benutzer = BenutzerBuilder.create(b -> b.withNachname("MitOrg"));
            benutzer.addOrganisation(
                OrganisationRollenBuilder.create(r -> r
                    .withOrganisation(org)
                    .withBenutzer(benutzer)
                    .withFachrollen(EnumSet.of(FachRolle.SCHULAERZTIN))));

            // Kein zust. Arzt am Dossier, kein Arzt an der Schule
            Schule schuleOhneArzt = SchuleBuilder.create(s -> {});
            Dossier dossier = DossierBuilder.create(d -> d
                .forKind(KindBuilder.create(k -> k.withSchule(schuleOhneArzt))));

            // ACT
            boolean result = dossier.isBenutzerZustaendigForDossier(benutzer);

            // ASSERT
            assertThat(result)
                .as("Wenn weder Dossier noch Schule einen Arzt haben, kann keine Zuständigkeit über die Organisation abgeleitet werden")
                .isFalse();
        }
    }

    @Nested
    class HasZustaendigkeitChanged {

        @Test
        void sameArzt_shouldReturnFalse() {
            // ARRANGE
            Benutzer arzt = BenutzerBuilder.create(b -> {});
            arzt.setId(ID.of(UUID.randomUUID(), Benutzer.class));
            Dossier dossier = DossierBuilder.create(d -> d.withZustaendigArzt(arzt));

            // ACT
            boolean result = service.hasZustaendigkeitChanged(dossier, arzt.getId());

            // ASSERT
            assertThat(result).isFalse();
        }

        @Test
        void diffArzt_sameOrganisation_shouldReturnFalse() {
            // ARRANGE
            Organisation org = OrganisationBuilder.create(o -> {});
            org.setId(ID.of(UUID.randomUUID(), Organisation.class));
            Benutzer arzt1 = BenutzerBuilder.create(b -> b.withID(ID.of(UUID.randomUUID(), Benutzer.class)));
            arzt1.addOrganisation(OrganisationRollenBuilder.create(r -> r.withOrganisation(org).withBenutzer(arzt1)));

            Benutzer arzt2 = BenutzerBuilder.create(b -> b.withID(ID.of(UUID.randomUUID(), Benutzer.class)));
            arzt2.addOrganisation(OrganisationRollenBuilder.create(r -> r.withOrganisation(org).withBenutzer(arzt2)));

            Dossier dossier = DossierBuilder.create(d -> d.withZustaendigArzt(arzt1));

            when(benutzerService.getById(arzt2.getId())).thenReturn(arzt2);

            // ACT
            boolean result = service.hasZustaendigkeitChanged(dossier, arzt2.getId());

            // ASSERT
            assertThat(result).isFalse();
        }

        @Test
        void diffArzt_diffOrganisation_shouldReturnTrue() {
            // ARRANGE
            Organisation org1 = OrganisationBuilder.create(o -> {});
            org1.setId(ID.of(UUID.randomUUID(), Organisation.class));
            Benutzer arzt1 = BenutzerBuilder.create(b -> b.withID(ID.of(UUID.randomUUID(), Benutzer.class)));
            arzt1.addOrganisation(OrganisationRollenBuilder.create(r -> r.withOrganisation(org1).withBenutzer(arzt1)));

            Organisation org2 = OrganisationBuilder.create(o -> {});
            org2.setId(ID.of(UUID.randomUUID(), Organisation.class));
            Benutzer arzt2 = BenutzerBuilder.create(b -> b.withID(ID.of(UUID.randomUUID(), Benutzer.class)));
            arzt2.addOrganisation(OrganisationRollenBuilder.create(r -> r.withOrganisation(org2).withBenutzer(arzt2)));

            Dossier dossier = DossierBuilder.create(d -> d.withZustaendigArzt(arzt1));

            when(benutzerService.getById(arzt2.getId())).thenReturn(arzt2);

            // ACT
            boolean result = service.hasZustaendigkeitChanged(dossier, arzt2.getId());

            // ASSERT
            assertThat(result).isTrue();
        }

        @Test
        void oldArztNull_shouldReturnFalse() {
            // ARRANGE
            Dossier dossier = DossierBuilder.create(d -> d.withZustaendigArzt(null));

            // ACT
            boolean result = service.hasZustaendigkeitChanged(dossier, ID.of(UUID.randomUUID(), Benutzer.class));

            // ASSERT
            assertThat(result).isFalse();
        }

        @Test
        void newArztNull_oldArztNotNull_shouldReturnTrueIfOldArztHasOrganisation() {
            // ARRANGE
            Organisation org1 = OrganisationBuilder.create(o -> {});
            org1.setId(ID.of(UUID.randomUUID(), Organisation.class));
            Benutzer arzt1 = BenutzerBuilder.create(b -> b.withID(ID.of(UUID.randomUUID(), Benutzer.class)));
            arzt1.addOrganisation(OrganisationRollenBuilder.create(r -> r.withOrganisation(org1).withBenutzer(arzt1)));

            Dossier dossier = DossierBuilder.create(d -> d.withZustaendigArzt(arzt1));

            // ACT
            boolean result = service.hasZustaendigkeitChanged(dossier, null);

            // ASSERT
            assertThat(result).isTrue();
        }
    }

    @Nested
    class HandleSchulVerantwortlicheChangedForDossierZentral {

        @Test
        void arztChanged_shouldResetArztOnDossier_alsoMpa() {
            // ARRANGE: Fall 2, zentral: Arzt changes on Schule -> Dossier-Arzt should be set to null; Dossier-MPA should also be set to null
            setupZentraleOrganisation(true);
            Benutzer arztOld = BenutzerBuilder.create(b -> b.withNachname("ArztOld"));
            Benutzer mpaOld = BenutzerBuilder.create(b -> b.withNachname("MpaOld"));
            Dossier dossier = DossierBuilder.create(d -> {
                d.withZustaendigArzt(arztOld);
                d.withZustaendigMPA(mpaOld);
            });

            VerantwortlicherSchuleChangedDTO args = VerantwortlicherSchuleChangedDTO.of(
                SchuleBuilder.create(s -> s.withZustaendigArzt(arztOld)),
                SchuleBuilder.create(s -> s.withZustaendigArzt(BenutzerBuilder.create(b -> b.withNachname("ArztNew"))))
            );

            // ACT
            service.schuleChangedOnDossier(dossier, args);

            // ASSERT: Arzt should be null (reset), MPA should remain unchanged
            assertThat(dossier.getZustaendigArzt())
                .as("Arzt should be reset to null for zentrale Organisation when arzt changed")
                .isNull();
            assertThat(dossier.getZustaendigMPA())
                .as("MPA should be reset to null for zentrale Organisation when arzt changed")
                .isNull();
        }

        @Test
        void mpaChanged_shouldResetMpaOnDossier_alsoArzt() {
            // ARRANGE: Fall 2, zentral: MPA changes on Schule -> Dossier-MPA should be set to null; Dossier-Arzt should also be set to null
            setupZentraleOrganisation(true);
            Benutzer arztOld = BenutzerBuilder.create(b -> b.withNachname("ArztOld"));
            Benutzer mpaOld = BenutzerBuilder.create(b -> b.withNachname("MpaOld"));
            Dossier dossier = DossierBuilder.create(d -> {
                d.withZustaendigArzt(arztOld);
                d.withZustaendigMPA(mpaOld);
            });

            VerantwortlicherSchuleChangedDTO args = VerantwortlicherSchuleChangedDTO.of(
                SchuleBuilder.create(s -> s.withZustaendigMPA(mpaOld)),
                SchuleBuilder.create(s -> s.withZustaendigMPA(BenutzerBuilder.create(b -> b.withNachname("MpaNew"))))
            );

            // ACT
            service.schuleChangedOnDossier(dossier, args);

            // ASSERT: MPA should be null (reset), Arzt should remain unchanged
            assertThat(dossier.getZustaendigMPA())
                .as("MPA should be reset to null for zentrale Organisation when MPA changed")
                .isNull();
            assertThat(dossier.getZustaendigArzt())
                .as("MPA should be reset to null for zentrale Organisation when MPA changed")
                .isNull();
        }

        @Test
        void shouldNeverArchive_whenZentral() {
            // ARRANGE: zentral: even when both Arzt and MPA change, no archiving should happen
            setupZentraleOrganisation(true);
            Benutzer arztOld = BenutzerBuilder.create(b -> b.withNachname("ArztOld"));
            Benutzer mpaOld = BenutzerBuilder.create(b -> b.withNachname("MpaOld"));
            Dossier dossier = DossierBuilder.create(d -> {
                d.withZustaendigArzt(arztOld);
                d.withZustaendigMPA(mpaOld);
            });

            VerantwortlicherSchuleChangedDTO args = VerantwortlicherSchuleChangedDTO.of(
                SchuleBuilder.create(s -> {
                    s.withZustaendigArzt(arztOld);
                    s.withZustaendigMPA(mpaOld);
                }),
                SchuleBuilder.create(s -> {
                    s.withZustaendigArzt(BenutzerBuilder.create(b -> b.withNachname("ArztNew")));
                    s.withZustaendigMPA(BenutzerBuilder.create(b -> b.withNachname("MpaNew")));
                })
            );

            // ACT
            service.schuleChangedOnDossier(dossier, args);

            // ASSERT: no archiving for zentrale Organisation
            verify(archivierungService, never()).createGesamtArchivdokument(any(), any(), any());
        }

        @Test
        void shouldNotPersist_whenArztAndMpaAlreadyNull() {
            // ARRANGE: zentral: Dossier has no Arzt and no MPA set (already null)
            setupZentraleOrganisation(true);
            Dossier dossier = DossierBuilder.create(d -> {});

            VerantwortlicherSchuleChangedDTO args = VerantwortlicherSchuleChangedDTO.of(
                SchuleBuilder.create(s -> s.withZustaendigArzt(BenutzerBuilder.create(b -> b.withNachname("ArztOld")))),
                SchuleBuilder.create(s -> s.withZustaendigArzt(BenutzerBuilder.create(b -> b.withNachname("ArztNew"))))
            );

            // ACT
            service.schuleChangedOnDossier(dossier, args);

            // ASSERT: Arzt and MPA remain null, persist is still called
            assertThat(dossier.getZustaendigArzt())
                .as("Arzt should remain null")
                .isNull();
            assertThat(dossier.getZustaendigMPA())
                .as("MPA should remain null")
                .isNull();
            verify(archivierungService, never()).createGesamtArchivdokument(any(), any(), any());
        }
    }

    @Nested
    class HandleSchulVerantwortlicheChangedForDossierDezentral {

        @Test
        void mpaChanged_shouldSetNewMpa_notOldMpa() {
            // ARRANGE: Fall 2, dezentral: MPA changes on Schule and Dossier-MPA matches old Schul-MPA
            // -> new MPA from Schule should be propagated to Dossier
            setupZentraleOrganisation(false);
            Benutzer mpaOld = BenutzerBuilder.create(b -> b.withNachname("MpaOld"));
            Benutzer mpaNew = BenutzerBuilder.create(b -> b.withNachname("MpaNew"));
            Dossier dossier = DossierBuilder.create(d -> d.withZustaendigMPA(mpaOld));

            VerantwortlicherSchuleChangedDTO args = VerantwortlicherSchuleChangedDTO.of(
                SchuleBuilder.create(s -> s.withZustaendigMPA(mpaOld)),
                SchuleBuilder.create(s -> s.withZustaendigMPA(mpaNew))
            );

            // eq() is required here: mixing raw values with argument matchers (any())
            // causes Mockito's InvalidUseOfMatchersException
            when(benutzerService.getBenutzerIfOneOfRolesIsCorrect(eq(mpaNew.getId()), any())).thenReturn(mpaNew);

            // ACT
            service.schuleChangedOnDossier(dossier, args);

            // ASSERT: Dossier should have the NEW MPA, not the old one
            // eq() is required here: mixing raw values with argument matchers (any())
            // causes Mockito's InvalidUseOfMatchersException
            verify(benutzerService).getBenutzerIfOneOfRolesIsCorrect(eq(mpaNew.getId()), any());
            assertThat(dossier.getZustaendigMPA())
                .as("Dossier should have the new MPA from Schule, not the old one")
                .isEqualTo(mpaNew);
        }

        @Test
        void mpaRemovedFromSchule_shouldNotOverwriteDossierMpa() {
            // ARRANGE: dezentral: MPA removed from Schule (mpaNachher is null)
            // -> Dossier MPA should remain unchanged
            setupZentraleOrganisation(false);
            Benutzer mpaOld = BenutzerBuilder.create(b -> b.withNachname("MpaOld"));
            Dossier dossier = DossierBuilder.create(d -> d.withZustaendigMPA(mpaOld));

            VerantwortlicherSchuleChangedDTO args = VerantwortlicherSchuleChangedDTO.of(
                SchuleBuilder.create(s -> s.withZustaendigMPA(mpaOld)),
                SchuleBuilder.create(s -> {
                })
            );

            // ACT
            service.schuleChangedOnDossier(dossier, args);

            // ASSERT: Dossier MPA should remain unchanged when new Schule MPA is null
            assertThat(dossier.getZustaendigMPA())
                .as("MPA should remain unchanged when new Schule MPA is null")
                .isEqualTo(mpaOld);
            verify(benutzerService, never()).getBenutzerIfOneOfRolesIsCorrect(any(ID.class), any());
        }

        @Test
        void arztChanged_shouldSetNewArztOnDossier() {
            // ARRANGE: dezentral: Arzt changes on Schule, Dossier has same old Arzt
            // -> new Arzt should be propagated to Dossier
            setupZentraleOrganisation(false);
            Benutzer arztOld = BenutzerBuilder.create(b -> b
                .withNachname("ArztOld")
                .withOrganisationRollen(new Organisation(), List.of(FachRolle.SCHULAERZTIN)));
            Benutzer arztNew = BenutzerBuilder.create(b -> b
                .withNachname("ArztNew")
                .withOrganisationRollen(new Organisation(), List.of(FachRolle.SCHULAERZTIN)));
            Dossier dossier = DossierBuilder.create(d -> d.withZustaendigArzt(arztOld));

            VerantwortlicherSchuleChangedDTO args = VerantwortlicherSchuleChangedDTO.of(
                SchuleBuilder.create(s -> s.withZustaendigArzt(arztOld)),
                SchuleBuilder.create(s -> s.withZustaendigArzt(arztNew))
            );

            when(benutzerService.getBenutzerIfCorrectRole(arztNew.getId(), FachRolle.SCHULAERZTIN)).thenReturn(arztNew);
            when(benutzerService.getById(arztOld.getId())).thenReturn(arztOld);
            when(benutzerService.getById(arztNew.getId())).thenReturn(arztNew);

            // ACT
            service.schuleChangedOnDossier(dossier, args);

            // ASSERT: new Arzt should be set on dossier
            assertThat(dossier.getZustaendigArzt())
                .as("Dossier should have the new Arzt")
                .isEqualTo(arztNew);
        }

        @Test
        void artzChanged_oldArztInSameOrganisationAsNewArzt_should_notArchive() {
            // ARRANGE
            setupZentraleOrganisation(false);
            Benutzer arztOld = BenutzerBuilder.create(b -> b.withNachname("ArztOld"));
            Benutzer arztNew = BenutzerBuilder.create(b -> b.withNachname("ArztNew"));
            Dossier dossier = DossierBuilder.create(d -> d.withZustaendigArzt(arztOld));

            Organisation organisation = OrganisationBuilder.create(o -> o.withVerantwortlicherArzt(arztOld));
            OrganisationRollenBuilder.create(o -> o.withOrganisation(organisation).withBenutzer(arztOld).withFachrollen(
                EnumSet.of(FachRolle.SCHULAERZTIN)));
            OrganisationRollenBuilder.create(o -> o.withOrganisation(organisation).withBenutzer(arztNew).withFachrollen(
                EnumSet.of(FachRolle.SCHULAERZTIN)));

            VerantwortlicherSchuleChangedDTO args = VerantwortlicherSchuleChangedDTO.of(
                SchuleBuilder.create(s -> s.withZustaendigArzt(arztOld)),
                SchuleBuilder.create(s -> s.withZustaendigArzt(arztNew))
            );

            when(benutzerService.getById(arztOld.getId())).thenReturn(arztOld);
            when(benutzerService.getById(arztNew.getId())).thenReturn(arztNew);

            // ACT
            service.schuleChangedOnDossier(dossier, args);

            // ASSERT: should NOT archive because old Arzt is still in berechtigte (check runs before change)
            verify(archivierungService, never()).createGesamtArchivdokument(any(), any(), any());
        }

        @Test
        void artzChanged_oldArztNotInSameOrganisationAsNewArzt_should_Archive() {
            // ARRANGE
            setupZentraleOrganisation(false);
            Benutzer arztOld = BenutzerBuilder.create(b -> b
                .withNachname("ArztOld")
                .withOrganisationRollen(new Organisation(), List.of(FachRolle.SCHULAERZTIN)));
            Benutzer arztNew = BenutzerBuilder.create(b -> b
                .withNachname("ArztNew")
                .withOrganisationRollen(new Organisation(), List.of(FachRolle.SCHULAERZTIN)));
            Dossier dossier = DossierBuilder.create(d -> d.withZustaendigArzt(arztOld));

            Organisation organisationOldArzt = OrganisationBuilder.create(o -> o.withVerantwortlicherArzt(arztOld));
            OrganisationRollenBuilder.create(o -> o.withOrganisation(organisationOldArzt)
                .withBenutzer(arztOld)
                .withFachrollen(
                    EnumSet.of(FachRolle.SCHULAERZTIN)));

            Organisation organisationNewArzt = OrganisationBuilder.create(o -> o.withVerantwortlicherArzt(arztNew));
            OrganisationRollenBuilder.create(o -> o.withOrganisation(organisationNewArzt)
                .withBenutzer(arztNew)
                .withFachrollen(
                    EnumSet.of(FachRolle.SCHULAERZTIN)));

            VerantwortlicherSchuleChangedDTO args = VerantwortlicherSchuleChangedDTO.of(
                SchuleBuilder.create(s -> s.withZustaendigArzt(arztOld)),
                SchuleBuilder.create(s -> s.withZustaendigArzt(arztNew))
            );

            when(benutzerService.getBenutzerIfCorrectRole(arztNew.getId(), FachRolle.SCHULAERZTIN)).thenReturn(arztNew);
            when(benutzerService.getById(arztOld.getId())).thenReturn(arztOld);
            when(benutzerService.getById(arztNew.getId())).thenReturn(arztNew);

            // ACT
            service.schuleChangedOnDossier(dossier, args);

            // ASSERT: should archive because old Arzt is in a different organisation than new Arzt
            verify(archivierungService, times(1)).createGesamtArchivdokument(any(), any(), any());
        }

        @Test
        void arztChanged_dossierArztDiffersFromOldSchulArzt_shouldNotTransferNorArchive() {
            // ARRANGE: dezentral, "Privatarzt" case: Dossier-Arzt != old Schul-Arzt
            // -> neither transfer new Arzt nor archive
            setupZentraleOrganisation(false);
            Benutzer privatArzt = BenutzerBuilder.create(b -> b.withNachname("PrivatArzt"));
            Benutzer schulArztOld = BenutzerBuilder.create(b -> b.withNachname("SchulArztOld"));
            Benutzer schulArztNew = BenutzerBuilder.create(b -> b.withNachname("SchulArztNew"));
            Dossier dossier = DossierBuilder.create(d -> d.withZustaendigArzt(privatArzt));

            VerantwortlicherSchuleChangedDTO args = VerantwortlicherSchuleChangedDTO.of(
                SchuleBuilder.create(s -> s.withZustaendigArzt(schulArztOld)),
                SchuleBuilder.create(s -> s.withZustaendigArzt(schulArztNew))
            );

            // ACT
            service.schuleChangedOnDossier(dossier, args);

            // ASSERT: Dossier-Arzt should remain unchanged (Privatarzt), no archiving
            assertThat(dossier.getZustaendigArzt())
                .as("Dossier-Arzt should remain the Privatarzt")
                .isEqualTo(privatArzt);
            verify(archivierungService, never()).createGesamtArchivdokument(any(), any(), any());
            verify(benutzerService, never()).getBenutzerIfCorrectRole(any(ID.class), any());
        }

        @Test
        void mpaChanged_dossierMpaDiffersFromOldSchulMpa_shouldNotTransfer() {
            // ARRANGE: dezentral: Dossier-MPA != old Schul-MPA
            // -> MPA should not be transferred
            setupZentraleOrganisation(false);
            Benutzer privatMpa = BenutzerBuilder.create(b -> b.withNachname("PrivatMpa"));
            Benutzer schulMpaOld = BenutzerBuilder.create(b -> b.withNachname("SchulMpaOld"));
            Benutzer schulMpaNew = BenutzerBuilder.create(b -> b.withNachname("SchulMpaNew"));
            Dossier dossier = DossierBuilder.create(d -> d.withZustaendigMPA(privatMpa));

            VerantwortlicherSchuleChangedDTO args = VerantwortlicherSchuleChangedDTO.of(
                SchuleBuilder.create(s -> s.withZustaendigMPA(schulMpaOld)),
                SchuleBuilder.create(s -> s.withZustaendigMPA(schulMpaNew))
            );

            // ACT
            service.schuleChangedOnDossier(dossier, args);

            // ASSERT: Dossier-MPA should remain unchanged
            assertThat(dossier.getZustaendigMPA())
                .as("Dossier-MPA should remain the private MPA")
                .isEqualTo(privatMpa);
            verify(benutzerService, never()).getBenutzerIfOneOfRolesIsCorrect(any(ID.class), any());
        }
    }

    @Nested
    class VerantwortlicherChangedOnSchuleAsync {

        @Test
        void shouldCreateQueueItems_whenArztChanged() {
            // ARRANGE
            Benutzer arztOld = BenutzerBuilder.create(b -> b.withNachname("ArztOld"));
            Benutzer arztNew = BenutzerBuilder.create(b -> b.withNachname("ArztNew"));
            Schule schule = SchuleBuilder.create(s -> s.withZustaendigArzt(arztOld));
            Dossier dossier1 = DossierBuilder.create(d -> {
            });
            Dossier dossier2 = DossierBuilder.create(d -> {
            });

            VerantwortlicherSchuleChangedDTO args = VerantwortlicherSchuleChangedDTO.of(
                SchuleBuilder.create(s -> s.withZustaendigArzt(arztOld)),
                SchuleBuilder.create(s -> s.withZustaendigArzt(arztNew))
            );

            when(dossierService.findBySchule(schule.getId())).thenReturn(List.of(dossier1, dossier2));

            // ACT
            service.verantwortlicherChangedOnSchuleAsync(schule, args);

            // ASSERT: should create a queue item for each dossier
            verify(dossierQueueService, times(2)).createDossierQueueItem(
                any(), eq(DossierQueueTyp.VERANTW_CHANGED_ON_SCHULE), any());
        }

        @Test
        void shouldNotCreateQueueItems_whenNothingChanged() {
            // ARRANGE: arzt and mpa are the same before and after
            Benutzer arzt = BenutzerBuilder.create(b -> b.withNachname("Arzt"));
            Schule schule = SchuleBuilder.create(s -> s.withZustaendigArzt(arzt));

            VerantwortlicherSchuleChangedDTO args = VerantwortlicherSchuleChangedDTO.of(
                SchuleBuilder.create(s -> s.withZustaendigArzt(arzt)),
                SchuleBuilder.create(s -> s.withZustaendigArzt(arzt))
            );

            // ACT
            service.verantwortlicherChangedOnSchuleAsync(schule, args);

            // ASSERT: nothing changed, no queue items created
            verify(dossierQueueService, never()).createDossierQueueItem(any(), any(), any());
            verify(dossierService, never()).findBySchule(any());
        }

        @Test
        void shouldCreateQueueItems_whenMpaChanged() {
            // ARRANGE
            Benutzer mpaOld = BenutzerBuilder.create(b -> b.withNachname("MpaOld"));
            Benutzer mpaNew = BenutzerBuilder.create(b -> b.withNachname("MpaNew"));
            Schule schule = SchuleBuilder.create(s -> s.withZustaendigMPA(mpaOld));
            Dossier dossier = DossierBuilder.create(d -> {
            });

            VerantwortlicherSchuleChangedDTO args = VerantwortlicherSchuleChangedDTO.of(
                SchuleBuilder.create(s -> s.withZustaendigMPA(mpaOld)),
                SchuleBuilder.create(s -> s.withZustaendigMPA(mpaNew))
            );

            when(dossierService.findBySchule(schule.getId())).thenReturn(List.of(dossier));

            // ACT
            service.verantwortlicherChangedOnSchuleAsync(schule, args);

            // ASSERT
            verify(dossierQueueService).createDossierQueueItem(
                eq(dossier.getId()), eq(DossierQueueTyp.VERANTW_CHANGED_ON_SCHULE), any());
        }
    }

    @Nested
    class VerantwortlicherChangedOnSchule {

        @Test
        void shouldDeserializeAndProcess_zentral() throws Exception {
            // ARRANGE
            setupZentraleOrganisation(true);
            Benutzer arztOld = BenutzerBuilder.create(b -> b.withNachname("ArztOld"));
            Benutzer arztNew = BenutzerBuilder.create(b -> b.withNachname("ArztNew"));
            Dossier dossier = DossierBuilder.create(d -> d.withZustaendigArzt(arztOld));

            VerantwortlicherSchuleChangedDTO args = VerantwortlicherSchuleChangedDTO.of(
                SchuleBuilder.create(s -> s.withZustaendigArzt(arztOld)),
                SchuleBuilder.create(s -> s.withZustaendigArzt(arztNew))
            );
            String jsonString = objectMapper.writeValueAsString(args);

            when(dossierService.getById(dossier.getId())).thenReturn(dossier);

            // ACT
            service.verantwortlicherChangedOnSchule(dossier.getId(), jsonString);

            // ASSERT: zentral -> arzt should be set to null
            assertThat(dossier.getZustaendigArzt()).isNull();
            verify(dossierService).persist(dossier);
        }

        @Test
        void shouldThrow_whenJsonIsInvalid() {
            // ARRANGE
            Dossier dossier = DossierBuilder.create(d -> {
            });
            ID<Dossier> dossierID = dossier.getId();
            when(dossierService.getById(dossierID)).thenReturn(dossier);

            // ACT & ASSERT
            assertThatThrownBy(() -> service.verantwortlicherChangedOnSchule(dossierID, "invalid-json"))
                .isInstanceOf(AppFailureException.class);
        }
    }

    @Nested
    class VerantwortlicherChangedOnDossierZentral {

        @Test
        void shouldSetArztToNull_whenSameAsSchuleArzt() {
            // ARRANGE: zentral, arzt on dossier same as schule -> should be set to null
            setupZentraleOrganisation(true);
            Benutzer arzt = BenutzerBuilder.create(b -> b.withNachname("Arzt"));
            Schule schule = SchuleBuilder.create(s -> s.withZustaendigArzt(arzt));
            Dossier dossier = DossierBuilder.create(d -> {
                d.forKind(KindBuilder.create(k -> k.withSchule(schule)));
                d.withZustaendigArzt(arzt);
            });

            // ACT
            service.verantwortlicherChangedOnDossier(dossier, arzt.getId(), null);

            // ASSERT: arzt should be null because same as schule
            assertThat(dossier.getZustaendigArzt()).isNull();
            verify(dossierService).persist(dossier);
        }

        @Test
        void shouldSetArzt_whenDifferentFromSchuleArzt() {
            // ARRANGE: zentral, arzt different from schule arzt -> should be set to new arzt
            setupZentraleOrganisation(true);
            Benutzer schuleArzt = BenutzerBuilder.create(b -> b.withNachname("SchuleArzt"));
            Benutzer newArzt = BenutzerBuilder.create(b -> b.withNachname("NewArzt"));
            Schule schule = SchuleBuilder.create(s -> s.withZustaendigArzt(schuleArzt));
            Dossier dossier = DossierBuilder.create(d -> d
                .forKind(KindBuilder.create(k -> k
                    .withSchule(schule))));

            when(benutzerService.getBenutzerIfCorrectRole(newArzt.getId(), FachRolle.SCHULAERZTIN)).thenReturn(newArzt);

            // ACT
            service.verantwortlicherChangedOnDossier(dossier, newArzt.getId(), null);

            // ASSERT: arzt should be set to new arzt (different from schule)
            assertThat(dossier.getZustaendigArzt()).isEqualTo(newArzt);
            verify(dossierService).persist(dossier);
        }

        @Test
        void shouldSetMpaToNull_whenSameAsSchuleMpa() {
            // ARRANGE: zentral, mpa on dossier same as schule -> should be set to null
            setupZentraleOrganisation(true);
            Benutzer mpa = BenutzerBuilder.create(b -> b.withNachname("Mpa"));
            Schule schule = SchuleBuilder.create(s -> s.withZustaendigMPA(mpa));
            Dossier dossier = DossierBuilder.create(d -> {
                d.forKind(KindBuilder.create(k -> k.withSchule(schule)));
                d.withZustaendigMPA(mpa);
            });

            // ACT
            service.verantwortlicherChangedOnDossier(dossier, null, mpa.getId());

            // ASSERT: mpa should be null because same as schule
            assertThat(dossier.getZustaendigMPA()).isNull();
            verify(dossierService).persist(dossier);
        }

        @Test
        void shouldSetMpa_whenDifferentFromSchuleMpa() {
            // ARRANGE: zentral, mpa different from schule mpa -> should be set to new mpa
            setupZentraleOrganisation(true);
            Benutzer schuleMpa = BenutzerBuilder.create(b -> b.withNachname("SchuleMpa"));
            Benutzer newMpa = BenutzerBuilder.create(b -> b.withNachname("NewMpa"));
            Schule schule = SchuleBuilder.create(s -> s.withZustaendigMPA(schuleMpa));
            Dossier dossier = DossierBuilder.create(d -> d
                .forKind(KindBuilder.create(k -> k
                    .withSchule(schule))));

            when(benutzerService.getBenutzerIfOneOfRolesIsCorrect(eq(newMpa.getId()), any())).thenReturn(newMpa);

            // ACT
            service.verantwortlicherChangedOnDossier(dossier, null, newMpa.getId());

            // ASSERT: mpa should be set to new mpa (different from schule)
            assertThat(dossier.getZustaendigMPA()).isEqualTo(newMpa);
            verify(dossierService).persist(dossier);
        }
    }

    @Nested
    class VerantwortlicherChangedOnDossierDezentral {

        @Test
        void shouldSetNewArzt_andArchive_whenOldArztLosesAccess() {
            // ARRANGE: dezentral, arzt changes and old arzt loses access -> archive
            setupZentraleOrganisation(false);
            Benutzer arztOld = BenutzerBuilder.create(b -> b.withNachname("ArztOld"));
            Benutzer arztNew = BenutzerBuilder.create(b -> b.withNachname("ArztNew"));
            Dossier dossier = DossierBuilder.create(d -> d.withZustaendigArzt(arztOld));

            when(benutzerService.getBenutzerIfCorrectRole(arztNew.getId(), FachRolle.SCHULAERZTIN)).thenReturn(arztNew);
            when(benutzerService.getById(arztOld.getId())).thenReturn(arztOld);

            // ACT
            service.verantwortlicherChangedOnDossier(dossier, arztNew.getId(), null);

            // ASSERT: arzt changed and old arzt lost access -> archive
            assertThat(dossier.getZustaendigArzt()).isEqualTo(arztNew);
            verify(archivierungService).createGesamtArchivdokument(
                dossier,
                arztOld,
                ArchivierungTyp.ZUSTAENDIGER_ARZT_CHANGED);
            verify(dossierService).persist(dossier);
        }

        @Test
        void shouldSetNewArzt_andNotArchive_whenOldArztStillHasAccessViaSchule() {
            // ARRANGE: dezentral, arzt changes but old arzt still on schule via same Organisation -> no archive
            // Die neue Zuständigkeit ist Organisations-basiert: der alte Arzt verliert den Zugriff nur,
            // wenn er weder über die Dossier-Org noch über die Schul-Org noch zuständig ist.
            setupZentraleOrganisation(false);
            Organisation gemeinsameOrg = OrganisationBuilder.create(o -> {});
            Benutzer arztOld = buildArztInOrganisation("ArztOld", gemeinsameOrg);
            Benutzer arztNew = buildArztInOrganisation("ArztNew", gemeinsameOrg);
            Schule schule = SchuleBuilder.create(s -> s.withZustaendigArzt(arztOld));
            Dossier dossier = DossierBuilder.create(d -> {
                d.forKind(KindBuilder.create(k -> k.withSchule(schule)));
                d.withZustaendigArzt(arztOld);
            });

            when(benutzerService.getBenutzerIfCorrectRole(arztNew.getId(), FachRolle.SCHULAERZTIN)).thenReturn(arztNew);

            // ACT
            service.verantwortlicherChangedOnDossier(dossier, arztNew.getId(), null);

            // ASSERT: old arzt still on schule (selbe Organisation) -> no archive
            assertThat(dossier.getZustaendigArzt()).isEqualTo(arztNew);
            verify(archivierungService, never()).createGesamtArchivdokument(any(), any(), any());
        }

        @Test
        void shouldNotArchive_whenArztDidNotChange() {
            // ARRANGE: dezentral, same arzt -> no change, no archive
            setupZentraleOrganisation(false);
            Benutzer arzt = BenutzerBuilder.create(b -> b.withNachname("Arzt"));
            Dossier dossier = DossierBuilder.create(d -> d.withZustaendigArzt(arzt));

            // ACT
            service.verantwortlicherChangedOnDossier(dossier, arzt.getId(), null);

            // ASSERT: same arzt -> no archive
            verify(archivierungService, never()).createGesamtArchivdokument(any(), any(), any());
            verify(dossierService).persist(dossier);
        }

        @Test
        void shouldSetNewMpa_whenMpaChanged() {
            // ARRANGE: dezentral, mpa changes
            setupZentraleOrganisation(false);
            Benutzer mpaOld = BenutzerBuilder.create(b -> b.withNachname("MpaOld"));
            Benutzer mpaNew = BenutzerBuilder.create(b -> b.withNachname("MpaNew"));
            Dossier dossier = DossierBuilder.create(d -> d.withZustaendigMPA(mpaOld));

            when(benutzerService.getBenutzerIfOneOfRolesIsCorrect(eq(mpaNew.getId()), any())).thenReturn(mpaNew);

            // ACT
            service.verantwortlicherChangedOnDossier(dossier, null, mpaNew.getId());

            // ASSERT: mpa changed
            assertThat(dossier.getZustaendigMPA()).isEqualTo(mpaNew);
            verify(dossierService).persist(dossier);
        }

        @Test
        void shouldNotChangeMpa_whenSameMpa() {
            // ARRANGE: dezentral, same mpa -> no change
            setupZentraleOrganisation(false);
            Benutzer mpa = BenutzerBuilder.create(b -> b.withNachname("Mpa"));
            Dossier dossier = DossierBuilder.create(d -> d.withZustaendigMPA(mpa));

            // ACT
            service.verantwortlicherChangedOnDossier(dossier, null, mpa.getId());

            // ASSERT: same mpa -> not fetched again
            verify(benutzerService, never()).getBenutzerIfOneOfRolesIsCorrect(any(ID.class), any());
            verify(dossierService).persist(dossier);
        }
    }

    @Nested
    class VerantwortlicherChangedOnAnlass {

        @Test
        void shouldUpdateAnlassArztAndMpa() {
            // ARRANGE
            setupZentraleOrganisation(true);
            Benutzer arztNew = BenutzerBuilder.create(b -> b.withNachname("ArztNew"));
            Benutzer mpaNew = BenutzerBuilder.create(b -> b.withNachname("MpaNew"));
            Anlass anlass = mock(Anlass.class);

            when(einladungService.findByAnlass(any())).thenReturn(List.of());
            when(benutzerService.getBenutzerIfCorrectRole(arztNew.getId(), FachRolle.SCHULAERZTIN)).thenReturn(arztNew);
            when(benutzerService.getBenutzerIfOneOfRolesIsCorrect(eq(mpaNew.getId()), any())).thenReturn(mpaNew);

            // ACT
            service.verantwortlicherChangedOnAnlass(anlass, arztNew.getId(), mpaNew.getId());

            // ASSERT: arzt and mpa set on anlass, anlass persisted
            verify(anlass).setArzt(arztNew);
            verify(anlass).setMpa(mpaNew);
            verify(anlass).disableOptimisticLockingCheck();
            verify(anlassService).persistAndFlush(anlass);
        }

        @Test
        void shouldArchive_whenDezentralAndOldArztLosesAccess() {
            // ARRANGE: dezentral, anlass arzt changes and old arzt not in dossier berechtigte
            setupZentraleOrganisation(false);
            Benutzer arztOld = BenutzerBuilder.create(b -> b.withNachname("ArztOld"));
            Benutzer arztNew = BenutzerBuilder.create(b -> b.withNachname("ArztNew"));
            Dossier dossier = DossierBuilder.create(d -> {
            });

            Anlass anlass = mock(Anlass.class);
            when(anlass.getArzt()).thenReturn(arztOld);

            Einladung einladung = mock(Einladung.class);
            when(einladung.getDossier()).thenReturn(dossier);

            when(einladungService.findByAnlass(any())).thenReturn(List.of(einladung));
            when(benutzerService.getBenutzerIfCorrectRole(arztNew.getId(), FachRolle.SCHULAERZTIN)).thenReturn(arztNew);

            // ACT
            service.verantwortlicherChangedOnAnlass(anlass, arztNew.getId(), null);

            // ASSERT: old arzt not in dossier berechtigte -> teil archive
            verify(archivierungService).createTeilArchivdokument(
                dossier,
                ArchivierungTyp.ZUSTAENDIGER_ARZT_CHANGED,
                anlass);
        }

        @Test
        void shouldNotArchive_whenZentral() {
            // ARRANGE: zentral, arzt changes on anlass -> should NOT archive
            setupZentraleOrganisation(true);
            Benutzer arztOld = BenutzerBuilder.create(b -> b.withNachname("ArztOld"));
            Benutzer arztNew = BenutzerBuilder.create(b -> b.withNachname("ArztNew"));
            Dossier dossier = DossierBuilder.create(d -> {
            });

            Anlass anlass = mock(Anlass.class);
            when(anlass.getArzt()).thenReturn(arztOld);

            Einladung einladung = mock(Einladung.class);
            when(einladung.getDossier()).thenReturn(dossier);

            when(einladungService.findByAnlass(any())).thenReturn(List.of(einladung));
            when(benutzerService.getBenutzerIfCorrectRole(arztNew.getId(), FachRolle.SCHULAERZTIN)).thenReturn(arztNew);

            // ACT
            service.verantwortlicherChangedOnAnlass(anlass, arztNew.getId(), null);

            // ASSERT: zentral -> no archive
            verify(archivierungService, never()).createTeilArchivdokument(any(), any(), any());
        }

        @Test
        void shouldNotArchive_whenDezentralAndOldArztStillHasAccess() {
            // ARRANGE: dezentral, old arzt teilt sich die Organisation mit dem zust. Schul-Arzt
            // -> bleibt zuständig, keine Archivierung.
            setupZentraleOrganisation(false);
            Organisation gemeinsameOrg = OrganisationBuilder.create(o -> {});
            Benutzer arztOld = buildArztInOrganisation("ArztOld", gemeinsameOrg);
            Benutzer arztNew = buildArztInOrganisation("ArztNew", gemeinsameOrg);
            Schule schule = SchuleBuilder.create(s -> s.withZustaendigArzt(arztOld));
            Dossier dossier = DossierBuilder.create(d -> {
                d.forKind(KindBuilder.create(k -> k.withSchule(schule)));
            });

            Anlass anlass = mock(Anlass.class);
            when(anlass.getArzt()).thenReturn(arztOld);

            Einladung einladung = mock(Einladung.class);
            when(einladung.getDossier()).thenReturn(dossier);

            when(einladungService.findByAnlass(any())).thenReturn(List.of(einladung));
            when(benutzerService.getBenutzerIfCorrectRole(arztNew.getId(), FachRolle.SCHULAERZTIN)).thenReturn(arztNew);

            // ACT
            service.verantwortlicherChangedOnAnlass(anlass, arztNew.getId(), null);

            // ASSERT: alter Arzt hängt via Schul-Org weiter dran -> no archive
            verify(archivierungService, never()).createTeilArchivdokument(any(), any(), any());
        }

        @Test
        void shouldNotIterateEinladungenNorArchive_whenArztDidNotChange() {
            // ARRANGE: dezentral, der Arzt am Anlass ändert nicht (gleiche ID wird übergeben).
            // Gemäss der lokalen Änderung sollen in diesem Fall die Einladungen gar nicht erst
            // durchlaufen werden (kein Aufruf an einladungService), damit für einen unveränderten
            // (Privat-)Arzt keine Archivierung erstellt wird.
            setupZentraleOrganisation(false);
            Benutzer arzt = BenutzerBuilder.create(b -> b.withNachname("Arzt"));

            Anlass anlass = mock(Anlass.class);
            when(anlass.getArzt()).thenReturn(arzt);

            when(benutzerService.getBenutzerIfCorrectRole(arzt.getId(), FachRolle.SCHULAERZTIN)).thenReturn(arzt);

            // ACT: gleiche Arzt-ID wie zuvor -> keine Änderung des Arztes
            service.verantwortlicherChangedOnAnlass(anlass, arzt.getId(), null);

            // ASSERT: Einladungen werden nicht geladen und keine Archivierung erstellt
            verify(einladungService, never()).findByAnlass(any());
            verify(archivierungService, never()).createTeilArchivdokument(any(), any(), any());
            // Der Anlass wird trotzdem aktualisiert und persistiert
            verify(anlass).setArzt(arzt);
            verify(anlassService).persistAndFlush(anlass);
        }

        @Test
        void shouldNotIterateEinladungenNorArchive_whenAnlassHadNoArzt() {
            // ARRANGE: dezentral, der Anlass hatte vorher keinen Arzt (null) und es wird ein neuer Arzt gesetzt.
            // Auch hier soll die Einladungs-Schleife (und damit die Archivierung) nicht ausgelöst werden,
            // da kein "alter" Arzt seinen Zugriff verlieren kann.
            setupZentraleOrganisation(false);
            Benutzer arztNew = BenutzerBuilder.create(b -> b.withNachname("ArztNew"));

            Anlass anlass = mock(Anlass.class);
            when(anlass.getArzt()).thenReturn(null);

            when(benutzerService.getBenutzerIfCorrectRole(arztNew.getId(), FachRolle.SCHULAERZTIN)).thenReturn(arztNew);

            // ACT
            service.verantwortlicherChangedOnAnlass(anlass, arztNew.getId(), null);

            // ASSERT: kein bisheriger Arzt -> Einladungen nicht laden, keine Archivierung
            verify(einladungService, never()).findByAnlass(any());
            verify(archivierungService, never()).createTeilArchivdokument(any(), any(), any());
            verify(anlass).setArzt(arztNew);
            verify(anlassService).persistAndFlush(anlass);
        }
    }

    @Nested
    class VerantwortlicherChangedByDuplikatMerge {

        @Test
        void shouldArchive_whenOtherDossierArztIsNOTInBasisDossierBerechtigte() {
            // ARRANGE: Fall 11 - The other dossier's Arzt is different from basis dossier's berechtigte
            // -> should archive.
            // The Kind has no Schule set, so berechtigte list only contains the dossier's own Arzt.
            setupZentraleOrganisation(false);
            Benutzer arztBasis = BenutzerBuilder.create(b -> b.withNachname("ArztBasis"));
            Benutzer arztOther = BenutzerBuilder.create(b -> b.withNachname("ArztOther"));

            Dossier basisDossier = DossierBuilder.create(d -> d.withZustaendigArzt(arztBasis));
            Dossier otherDossier = DossierBuilder.create(d -> d.withZustaendigArzt(arztOther));

            // ACT
            service.verantwortlicherChangedByDuplikatMerge(basisDossier, otherDossier);

            // ASSERT: should archive because otherDossier's Arzt is NOT in basisDossier's berechtigte
            verify(archivierungService).createGesamtArchivdokument(
                otherDossier, arztOther, ArchivierungTyp.DUPLIKAT_MERGE);
        }

        @Test
        void shouldNotArchive_whenOtherDossierArztISInBasisDossierBerechtigte() {
            // ARRANGE: Fall 11 - The other dossier's Arzt teilt die Organisation mit dem zust. Arzt
            // des basisDossiers -> should NOT archive because the Arzt still has access via the basis dossier.
            setupZentraleOrganisation(false);
            Organisation gemeinsameOrg = OrganisationBuilder.create(o -> {});
            Benutzer arztBasis = buildArztInOrganisation("ArztBasis", gemeinsameOrg);
            Benutzer arztOther = buildArztInOrganisation("ArztOther", gemeinsameOrg);

            Dossier basisDossier = DossierBuilder.create(d -> d.withZustaendigArzt(arztBasis));
            Dossier otherDossier = DossierBuilder.create(d -> d.withZustaendigArzt(arztOther));

            // ACT
            service.verantwortlicherChangedByDuplikatMerge(basisDossier, otherDossier);

            // ASSERT: should NOT archive because otherDossier's Arzt IS in basisDossier's berechtigte (gleiche Org)
            verify(archivierungService, never()).createGesamtArchivdokument(any(), any(), any());
        }

        @Test
        void shouldNotArchive_whenOtherDossierArztIsOnBasisDossierSchule() {
            // ARRANGE: Fall 11 - The other dossier's Arzt teilt die Organisation mit dem Schul-Arzt
            // des basisDossiers -> should NOT archive, da der Arzt über die Schul-Org weiterhin Zugriff hat.
            setupZentraleOrganisation(false);
            Organisation gemeinsameOrg = OrganisationBuilder.create(o -> {});
            Benutzer schulArzt = buildArztInOrganisation("SchulArzt", gemeinsameOrg);
            Benutzer arztOther = buildArztInOrganisation("ArztOther", gemeinsameOrg);
            Schule schuleWithArzt = SchuleBuilder.create(s -> s.withZustaendigArzt(schulArzt));

            Dossier basisDossier = DossierBuilder.create(d ->
                d.forKind(KindBuilder.create(k -> k.withSchule(schuleWithArzt))));
            Dossier otherDossier = DossierBuilder.create(d -> d.withZustaendigArzt(arztOther));

            // ACT
            service.verantwortlicherChangedByDuplikatMerge(basisDossier, otherDossier);

            // ASSERT: should NOT archive because otherDossier's Arzt hängt via Schul-Org weiter dran
            verify(archivierungService, never()).createGesamtArchivdokument(any(), any(), any());
        }

        @Test
        void shouldNotArchive_forZentraleOrganisation() {
            // ARRANGE: Fall 11 - zentrale Organisation: never archive for responsibility changes
            setupZentraleOrganisation(true);
            Benutzer arztBasis = BenutzerBuilder.create(b -> b.withNachname("ArztBasis"));
            Benutzer arztOther = BenutzerBuilder.create(b -> b.withNachname("ArztOther"));

            Dossier basisDossier = DossierBuilder.create(d -> d.withZustaendigArzt(arztBasis));
            Dossier otherDossier = DossierBuilder.create(d -> d.withZustaendigArzt(arztOther));

            // ACT
            service.verantwortlicherChangedByDuplikatMerge(basisDossier, otherDossier);

            // ASSERT: should NOT archive for zentrale Organisation
            verify(archivierungService, never()).createGesamtArchivdokument(any(), any(), any());
        }
    }
}
