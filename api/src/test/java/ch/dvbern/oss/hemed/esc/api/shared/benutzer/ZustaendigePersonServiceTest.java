package ch.dvbern.oss.hemed.esc.api.shared.benutzer;

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
import ch.dvbern.oss.hemed.esc.api.testutil.builder.SchuleBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
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
    private TenantSettingsService tenantSettingsService;
    private ConfigService configService;
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
        tenantSettingsService = mock(TenantSettingsService.class);
        configService = mock(ConfigService.class);
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

    @Nested
    class HandleSchulVerantwortlicheChangedForDossierZentral {

        @Test
        void arztChanged_shouldResetArztOnDossier_notMpa() {
            // Arrange: Fall 2, zentral: Arzt changes on Schule -> Dossier-Arzt should be set to null
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

            // Act
            service.schuleChangedOnDossier(dossier, args);

            // Assert: Arzt should be null (reset), MPA should remain unchanged
            assertThat(dossier.getZustaendigArzt())
                .as("Arzt should be reset to null for zentrale Organisation when arzt changed")
                .isNull();
            assertThat(dossier.getZustaendigMPA())
                .as("MPA should NOT be reset when only arzt changed")
                .isEqualTo(mpaOld);
        }

        @Test
        void mpaChanged_shouldResetMpaOnDossier_notArzt() {
            // Arrange: Fall 2, zentral: MPA changes on Schule -> Dossier-MPA should be set to null
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

            // Act
            service.schuleChangedOnDossier(dossier, args);

            // Assert: MPA should be null (reset), Arzt should remain unchanged
            assertThat(dossier.getZustaendigMPA())
                .as("MPA should be reset to null for zentrale Organisation when MPA changed")
                .isNull();
            assertThat(dossier.getZustaendigArzt())
                .as("Arzt should NOT be reset when only MPA changed")
                .isEqualTo(arztOld);
        }
    }

    @Nested
    class HandleSchulVerantwortlicheChangedForDossierDezentral {

        @Test
        void mpaChanged_shouldSetNewMpa_notOldMpa() {
            // Arrange: Fall 2, dezentral: MPA changes on Schule and Dossier-MPA matches old Schul-MPA
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

            // Act
            service.schuleChangedOnDossier(dossier, args);

            // Assert: Dossier should have the NEW MPA, not the old one
            // eq() is required here: mixing raw values with argument matchers (any())
            // causes Mockito's InvalidUseOfMatchersException
            verify(benutzerService).getBenutzerIfOneOfRolesIsCorrect(eq(mpaNew.getId()), any());
            assertThat(dossier.getZustaendigMPA())
                .as("Dossier should have the new MPA from Schule, not the old one")
                .isEqualTo(mpaNew);
        }

        @Test
        void mpaRemovedFromSchule_shouldNotOverwriteDossierMpa() {
            // Arrange: dezentral: MPA removed from Schule (mpaNachher is null)
            // -> Dossier MPA should remain unchanged
            setupZentraleOrganisation(false);
            Benutzer mpaOld = BenutzerBuilder.create(b -> b.withNachname("MpaOld"));
            Dossier dossier = DossierBuilder.create(d -> d.withZustaendigMPA(mpaOld));

            VerantwortlicherSchuleChangedDTO args = VerantwortlicherSchuleChangedDTO.of(
                SchuleBuilder.create(s -> s.withZustaendigMPA(mpaOld)),
                SchuleBuilder.create(s -> {
                })
            );

            // Act
            service.schuleChangedOnDossier(dossier, args);

            // Assert: Dossier MPA should remain unchanged when new Schule MPA is null
            assertThat(dossier.getZustaendigMPA())
                .as("MPA should remain unchanged when new Schule MPA is null")
                .isEqualTo(mpaOld);
            verify(benutzerService, never()).getBenutzerIfOneOfRolesIsCorrect(any(ID.class), any());
        }

        @Test
        void arztChanged_shouldSetNewArztOnDossier() {
            // Arrange: dezentral: Arzt changes on Schule, Dossier has same old Arzt
            // -> new Arzt should be propagated to Dossier
            setupZentraleOrganisation(false);
            Benutzer arztOld = BenutzerBuilder.create(b -> b.withNachname("ArztOld"));
            Benutzer arztNew = BenutzerBuilder.create(b -> b.withNachname("ArztNew"));
            Dossier dossier = DossierBuilder.create(d -> d.withZustaendigArzt(arztOld));

            VerantwortlicherSchuleChangedDTO args = VerantwortlicherSchuleChangedDTO.of(
                SchuleBuilder.create(s -> s.withZustaendigArzt(arztOld)),
                SchuleBuilder.create(s -> s.withZustaendigArzt(arztNew))
            );

            when(benutzerService.getBenutzerIfCorrectRole(arztNew.getId(), FachRolle.SCHULAERZTIN)).thenReturn(arztNew);

            // Act
            service.schuleChangedOnDossier(dossier, args);

            // Assert: new Arzt should be set on dossier
            assertThat(dossier.getZustaendigArzt())
                .as("Dossier should have the new Arzt")
                .isEqualTo(arztNew);
        }

        @Test
        void arztChanged_oldArztNotOnSchule_shouldNotArchive() {
            // Arrange: dezentral: Arzt changes on Schule, the archiving check in
            // handleSchulVerantwortlicheChangedForDossier runs BEFORE the dossier's Arzt is updated,
            // so the old Arzt is still in getBerechtigteAerzteIdsFromDossierOrSchule() -> no archiving
            setupZentraleOrganisation(false);
            Benutzer arztOld = BenutzerBuilder.create(b -> b.withNachname("ArztOld"));
            Benutzer arztNew = BenutzerBuilder.create(b -> b.withNachname("ArztNew"));
            Dossier dossier = DossierBuilder.create(d -> d.withZustaendigArzt(arztOld));

            VerantwortlicherSchuleChangedDTO args = VerantwortlicherSchuleChangedDTO.of(
                SchuleBuilder.create(s -> s.withZustaendigArzt(arztOld)),
                SchuleBuilder.create(s -> s.withZustaendigArzt(arztNew))
            );

            when(benutzerService.getBenutzerIfCorrectRole(arztNew.getId(), FachRolle.SCHULAERZTIN)).thenReturn(arztNew);

            // Act
            service.schuleChangedOnDossier(dossier, args);

            // Assert: should NOT archive because old Arzt is still in berechtigte (check runs before change)
            verify(archivierungService, never()).createGesamtArchivdokument(any(), any(), any());
        }

        @Test
        void arztChanged_oldArztStillHasAccessViaSchule_shouldNotArchive() {
            // Arrange: dezentral: Arzt changes on Schule, but old Arzt still has access via
            // the Kind's Schule -> should NOT archive.
            // We set the Kind's Schule to have the old Arzt, so getBerechtigteAerzteIdsFromDossierOrSchule
            // still contains the old Arzt's ID.
            setupZentraleOrganisation(false);
            Benutzer arztOld = BenutzerBuilder.create(b -> b.withNachname("ArztOld"));
            Benutzer arztNew = BenutzerBuilder.create(b -> b.withNachname("ArztNew"));
            Schule schuleWithOldArzt = SchuleBuilder.create(s -> s.withZustaendigArzt(arztOld));
            Dossier dossier = DossierBuilder.create(d -> {
                d.forKind(KindBuilder.create(k -> k.withSchule(schuleWithOldArzt)));
                d.withZustaendigArzt(arztOld);
            });

            VerantwortlicherSchuleChangedDTO args = VerantwortlicherSchuleChangedDTO.of(
                SchuleBuilder.create(s -> s.withZustaendigArzt(arztOld)),
                SchuleBuilder.create(s -> s.withZustaendigArzt(arztNew))
            );

            when(benutzerService.getBenutzerIfCorrectRole(arztNew.getId(), FachRolle.SCHULAERZTIN)).thenReturn(arztNew);

            // Act
            service.schuleChangedOnDossier(dossier, args);

            // Assert: old Arzt should NOT be archived because still accessible via Kind's Schule
            verify(archivierungService, never()).createGesamtArchivdokument(any(), any(), any());
            assertThat(dossier.getZustaendigArzt())
                .as("Dossier should still have the new Arzt set")
                .isEqualTo(arztNew);
        }
    }

    @Nested
    class VerantwortlicherChangedOnSchuleAsync {

        @Test
        void shouldCreateQueueItems_whenArztChanged() {
            // Arrange
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

            // Act
            service.verantwortlicherChangedOnSchuleAsync(schule, args);

            // Assert: should create a queue item for each dossier
            verify(dossierQueueService, times(2)).createDossierQueueItem(
                any(), eq(DossierQueueTyp.VERANTW_CHANGED_ON_SCHULE), any());
        }

        @Test
        void shouldNotCreateQueueItems_whenNothingChanged() {
            // Arrange: arzt and mpa are the same before and after
            Benutzer arzt = BenutzerBuilder.create(b -> b.withNachname("Arzt"));
            Schule schule = SchuleBuilder.create(s -> s.withZustaendigArzt(arzt));

            VerantwortlicherSchuleChangedDTO args = VerantwortlicherSchuleChangedDTO.of(
                SchuleBuilder.create(s -> s.withZustaendigArzt(arzt)),
                SchuleBuilder.create(s -> s.withZustaendigArzt(arzt))
            );

            // Act
            service.verantwortlicherChangedOnSchuleAsync(schule, args);

            // Assert: nothing changed, no queue items created
            verify(dossierQueueService, never()).createDossierQueueItem(any(), any(), any());
            verify(dossierService, never()).findBySchule(any());
        }

        @Test
        void shouldCreateQueueItems_whenMpaChanged() {
            // Arrange
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

            // Act
            service.verantwortlicherChangedOnSchuleAsync(schule, args);

            // Assert
            verify(dossierQueueService).createDossierQueueItem(
                eq(dossier.getId()), eq(DossierQueueTyp.VERANTW_CHANGED_ON_SCHULE), any());
        }
    }

    @Nested
    class VerantwortlicherChangedOnSchule {

        @Test
        void shouldDeserializeAndProcess_zentral() throws Exception {
            // Arrange
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

            // Act
            service.verantwortlicherChangedOnSchule(dossier.getId(), jsonString);

            // Assert: zentral -> arzt should be set to null
            assertThat(dossier.getZustaendigArzt()).isNull();
            verify(dossierService).persist(dossier);
        }

        @Test
        void shouldThrow_whenJsonIsInvalid() {
            // Arrange
            Dossier dossier = DossierBuilder.create(d -> {
            });
            ID<Dossier> dossierID = dossier.getId();
            when(dossierService.getById(dossierID)).thenReturn(dossier);

            // Act & Assert
            assertThatThrownBy(() -> service.verantwortlicherChangedOnSchule(dossierID, "invalid-json"))
                .isInstanceOf(AppFailureException.class);
        }
    }

    @Nested
    class VerantwortlicherChangedOnDossierZentral {

        @Test
        void shouldSetArztToNull_whenSameAsSchuleArzt() {
            // Arrange: zentral, arzt on dossier same as schule -> should be set to null
            setupZentraleOrganisation(true);
            Benutzer arzt = BenutzerBuilder.create(b -> b.withNachname("Arzt"));
            Schule schule = SchuleBuilder.create(s -> s.withZustaendigArzt(arzt));
            Dossier dossier = DossierBuilder.create(d -> {
                d.forKind(KindBuilder.create(k -> k.withSchule(schule)));
                d.withZustaendigArzt(arzt);
            });

            // Act
            service.verantwortlicherChangedOnDossier(dossier, arzt.getId(), null);

            // Assert: arzt should be null because same as schule
            assertThat(dossier.getZustaendigArzt()).isNull();
            verify(dossierService).persist(dossier);
        }

        @Test
        void shouldSetArzt_whenDifferentFromSchuleArzt() {
            // Arrange: zentral, arzt different from schule arzt -> should be set to new arzt
            setupZentraleOrganisation(true);
            Benutzer schuleArzt = BenutzerBuilder.create(b -> b.withNachname("SchuleArzt"));
            Benutzer newArzt = BenutzerBuilder.create(b -> b.withNachname("NewArzt"));
            Schule schule = SchuleBuilder.create(s -> s.withZustaendigArzt(schuleArzt));
            Dossier dossier = DossierBuilder.create(d -> d
                .forKind(KindBuilder.create(k -> k
                    .withSchule(schule))));

            when(benutzerService.getBenutzerIfCorrectRole(newArzt.getId(), FachRolle.SCHULAERZTIN)).thenReturn(newArzt);

            // Act
            service.verantwortlicherChangedOnDossier(dossier, newArzt.getId(), null);

            // Assert: arzt should be set to new arzt (different from schule)
            assertThat(dossier.getZustaendigArzt()).isEqualTo(newArzt);
            verify(dossierService).persist(dossier);
        }

        @Test
        void shouldSetMpaToNull_whenSameAsSchuleMpa() {
            // Arrange: zentral, mpa on dossier same as schule -> should be set to null
            setupZentraleOrganisation(true);
            Benutzer mpa = BenutzerBuilder.create(b -> b.withNachname("Mpa"));
            Schule schule = SchuleBuilder.create(s -> s.withZustaendigMPA(mpa));
            Dossier dossier = DossierBuilder.create(d -> {
                d.forKind(KindBuilder.create(k -> k.withSchule(schule)));
                d.withZustaendigMPA(mpa);
            });

            // Act
            service.verantwortlicherChangedOnDossier(dossier, null, mpa.getId());

            // Assert: mpa should be null because same as schule
            assertThat(dossier.getZustaendigMPA()).isNull();
            verify(dossierService).persist(dossier);
        }

        @Test
        void shouldSetMpa_whenDifferentFromSchuleMpa() {
            // Arrange: zentral, mpa different from schule mpa -> should be set to new mpa
            setupZentraleOrganisation(true);
            Benutzer schuleMpa = BenutzerBuilder.create(b -> b.withNachname("SchuleMpa"));
            Benutzer newMpa = BenutzerBuilder.create(b -> b.withNachname("NewMpa"));
            Schule schule = SchuleBuilder.create(s -> s.withZustaendigMPA(schuleMpa));
            Dossier dossier = DossierBuilder.create(d -> d
                .forKind(KindBuilder.create(k -> k
                    .withSchule(schule))));

            when(benutzerService.getBenutzerIfOneOfRolesIsCorrect(eq(newMpa.getId()), any())).thenReturn(newMpa);

            // Act
            service.verantwortlicherChangedOnDossier(dossier, null, newMpa.getId());

            // Assert: mpa should be set to new mpa (different from schule)
            assertThat(dossier.getZustaendigMPA()).isEqualTo(newMpa);
            verify(dossierService).persist(dossier);
        }
    }

    @Nested
    class VerantwortlicherChangedOnDossierDezentral {

        @Test
        void shouldSetNewArzt_andArchive_whenOldArztLosesAccess() {
            // Arrange: dezentral, arzt changes and old arzt loses access -> archive
            setupZentraleOrganisation(false);
            Benutzer arztOld = BenutzerBuilder.create(b -> b.withNachname("ArztOld"));
            Benutzer arztNew = BenutzerBuilder.create(b -> b.withNachname("ArztNew"));
            Dossier dossier = DossierBuilder.create(d -> d.withZustaendigArzt(arztOld));

            when(benutzerService.getBenutzerIfCorrectRole(arztNew.getId(), FachRolle.SCHULAERZTIN)).thenReturn(arztNew);
            when(benutzerService.getById(arztOld.getId())).thenReturn(arztOld);

            // Act
            service.verantwortlicherChangedOnDossier(dossier, arztNew.getId(), null);

            // Assert: arzt changed and old arzt lost access -> archive
            assertThat(dossier.getZustaendigArzt()).isEqualTo(arztNew);
            verify(archivierungService).createGesamtArchivdokument(
                dossier,
                arztOld,
                ArchivierungTyp.ZUSTAENDIGER_ARZT_CHANGED);
            verify(dossierService).persist(dossier);
        }

        @Test
        void shouldSetNewArzt_andNotArchive_whenOldArztStillHasAccessViaSchule() {
            // Arrange: dezentral, arzt changes but old arzt still on schule -> no archive
            setupZentraleOrganisation(false);
            Benutzer arztOld = BenutzerBuilder.create(b -> b.withNachname("ArztOld"));
            Benutzer arztNew = BenutzerBuilder.create(b -> b.withNachname("ArztNew"));
            Schule schule = SchuleBuilder.create(s -> s.withZustaendigArzt(arztOld));
            Dossier dossier = DossierBuilder.create(d -> {
                d.forKind(KindBuilder.create(k -> k.withSchule(schule)));
                d.withZustaendigArzt(arztOld);
            });

            when(benutzerService.getBenutzerIfCorrectRole(arztNew.getId(), FachRolle.SCHULAERZTIN)).thenReturn(arztNew);

            // Act
            service.verantwortlicherChangedOnDossier(dossier, arztNew.getId(), null);

            // Assert: old arzt still on schule -> no archive
            assertThat(dossier.getZustaendigArzt()).isEqualTo(arztNew);
            verify(archivierungService, never()).createGesamtArchivdokument(any(), any(), any());
        }

        @Test
        void shouldNotArchive_whenArztDidNotChange() {
            // Arrange: dezentral, same arzt -> no change, no archive
            setupZentraleOrganisation(false);
            Benutzer arzt = BenutzerBuilder.create(b -> b.withNachname("Arzt"));
            Dossier dossier = DossierBuilder.create(d -> d.withZustaendigArzt(arzt));

            // Act
            service.verantwortlicherChangedOnDossier(dossier, arzt.getId(), null);

            // Assert: same arzt -> no archive
            verify(archivierungService, never()).createGesamtArchivdokument(any(), any(), any());
            verify(dossierService).persist(dossier);
        }

        @Test
        void shouldSetNewMpa_whenMpaChanged() {
            // Arrange: dezentral, mpa changes
            setupZentraleOrganisation(false);
            Benutzer mpaOld = BenutzerBuilder.create(b -> b.withNachname("MpaOld"));
            Benutzer mpaNew = BenutzerBuilder.create(b -> b.withNachname("MpaNew"));
            Dossier dossier = DossierBuilder.create(d -> d.withZustaendigMPA(mpaOld));

            when(benutzerService.getBenutzerIfOneOfRolesIsCorrect(eq(mpaNew.getId()), any())).thenReturn(mpaNew);

            // Act
            service.verantwortlicherChangedOnDossier(dossier, null, mpaNew.getId());

            // Assert: mpa changed
            assertThat(dossier.getZustaendigMPA()).isEqualTo(mpaNew);
            verify(dossierService).persist(dossier);
        }

        @Test
        void shouldNotChangeMpa_whenSameMpa() {
            // Arrange: dezentral, same mpa -> no change
            setupZentraleOrganisation(false);
            Benutzer mpa = BenutzerBuilder.create(b -> b.withNachname("Mpa"));
            Dossier dossier = DossierBuilder.create(d -> d.withZustaendigMPA(mpa));

            // Act
            service.verantwortlicherChangedOnDossier(dossier, null, mpa.getId());

            // Assert: same mpa -> not fetched again
            verify(benutzerService, never()).getBenutzerIfOneOfRolesIsCorrect(any(ID.class), any());
            verify(dossierService).persist(dossier);
        }
    }

    @Nested
    class VerantwortlicherChangedOnAnlass {

        @Test
        void shouldUpdateAnlassArztAndMpa() {
            // Arrange
            setupZentraleOrganisation(true);
            Benutzer arztNew = BenutzerBuilder.create(b -> b.withNachname("ArztNew"));
            Benutzer mpaNew = BenutzerBuilder.create(b -> b.withNachname("MpaNew"));
            Anlass anlass = mock(Anlass.class);

            when(einladungService.findByAnlass(any())).thenReturn(List.of());
            when(benutzerService.getBenutzerIfCorrectRole(arztNew.getId(), FachRolle.SCHULAERZTIN)).thenReturn(arztNew);
            when(benutzerService.getBenutzerIfOneOfRolesIsCorrect(eq(mpaNew.getId()), any())).thenReturn(mpaNew);

            // Act
            service.verantwortlicherChangedOnAnlass(anlass, arztNew.getId(), mpaNew.getId());

            // Assert: arzt and mpa set on anlass, anlass persisted
            verify(anlass).setArzt(arztNew);
            verify(anlass).setMpa(mpaNew);
            verify(anlass).disableOptimisticLockingCheck();
            verify(anlassService).persist(anlass);
        }

        @Test
        void shouldArchive_whenDezentralAndOldArztLosesAccess() {
            // Arrange: dezentral, anlass arzt changes and old arzt not in dossier berechtigte
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

            // Act
            service.verantwortlicherChangedOnAnlass(anlass, arztNew.getId(), null);

            // Assert: old arzt not in dossier berechtigte -> teil archive
            verify(archivierungService).createTeilArchivdokument(
                dossier,
                ArchivierungTyp.ZUSTAENDIGER_ARZT_CHANGED,
                anlass);
        }

        @Test
        void shouldNotArchive_whenZentral() {
            // Arrange: zentral, arzt changes on anlass -> should NOT archive
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

            // Act
            service.verantwortlicherChangedOnAnlass(anlass, arztNew.getId(), null);

            // Assert: zentral -> no archive
            verify(archivierungService, never()).createTeilArchivdokument(any(), any(), any());
        }

        @Test
        void shouldNotArchive_whenDezentralAndOldArztStillHasAccess() {
            // Arrange: dezentral, old arzt still in dossier berechtigte (via schule)
            setupZentraleOrganisation(false);
            Benutzer arztOld = BenutzerBuilder.create(b -> b.withNachname("ArztOld"));
            Benutzer arztNew = BenutzerBuilder.create(b -> b.withNachname("ArztNew"));
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

            // Act
            service.verantwortlicherChangedOnAnlass(anlass, arztNew.getId(), null);

            // Assert: old arzt still accessible via schule -> no archive
            verify(archivierungService, never()).createTeilArchivdokument(any(), any(), any());
        }
    }

    @Nested
    class VerantwortlicherChangedByDuplikatMerge {

        @Test
        void shouldArchive_whenOtherDossierArztIsNOTInBasisDossierBerechtigte() {
            // Arrange: Fall 11 - The other dossier's Arzt is different from basis dossier's berechtigte
            // -> should archive.
            // The Kind has no Schule set, so berechtigte list only contains the dossier's own Arzt.
            setupZentraleOrganisation(false);
            Benutzer arztBasis = BenutzerBuilder.create(b -> b.withNachname("ArztBasis"));
            Benutzer arztOther = BenutzerBuilder.create(b -> b.withNachname("ArztOther"));

            Dossier basisDossier = DossierBuilder.create(d -> d.withZustaendigArzt(arztBasis));
            Dossier otherDossier = DossierBuilder.create(d -> d.withZustaendigArzt(arztOther));

            // Act
            service.verantwortlicherChangedByDuplikatMerge(basisDossier, otherDossier);

            // Assert: should archive because otherDossier's Arzt is NOT in basisDossier's berechtigte
            verify(archivierungService).createGesamtArchivdokument(
                otherDossier, arztOther, ArchivierungTyp.DUPLIKAT_MERGE);
        }

        @Test
        void shouldNotArchive_whenOtherDossierArztISInBasisDossierBerechtigte() {
            // Arrange: Fall 11 - The other dossier's Arzt is the SAME as basis dossier's Arzt
            // -> should NOT archive because the Arzt still has access via the basis dossier
            setupZentraleOrganisation(false);
            Benutzer sameArzt = BenutzerBuilder.create(b -> b.withNachname("SameArzt"));

            Dossier basisDossier = DossierBuilder.create(d -> d.withZustaendigArzt(sameArzt));
            Dossier otherDossier = DossierBuilder.create(d -> d.withZustaendigArzt(sameArzt));

            // Act
            service.verantwortlicherChangedByDuplikatMerge(basisDossier, otherDossier);

            // Assert: should NOT archive because otherDossier's Arzt IS in basisDossier's berechtigte
            verify(archivierungService, never()).createGesamtArchivdokument(any(), any(), any());
        }

        @Test
        void shouldNotArchive_whenOtherDossierArztIsOnBasisDossierSchule() {
            // Arrange: Fall 11 - The other dossier's Arzt is accessible via basisDossier's Kind's Schule
            // -> should NOT archive because the Arzt still has access via the Schule
            setupZentraleOrganisation(false);
            Benutzer arztOther = BenutzerBuilder.create(b -> b.withNachname("ArztOther"));
            Schule schuleWithArzt = SchuleBuilder.create(s -> s.withZustaendigArzt(arztOther));

            Dossier basisDossier = DossierBuilder.create(d ->
                d.forKind(KindBuilder.create(k -> k.withSchule(schuleWithArzt))));
            Dossier otherDossier = DossierBuilder.create(d -> d.withZustaendigArzt(arztOther));

            // Act
            service.verantwortlicherChangedByDuplikatMerge(basisDossier, otherDossier);

            // Assert: should NOT archive because otherDossier's Arzt is on basisDossier's Schule
            verify(archivierungService, never()).createGesamtArchivdokument(any(), any(), any());
        }

        @Test
        void shouldNotArchive_forZentraleOrganisation() {
            // Arrange: Fall 11 - zentrale Organisation: never archive for responsibility changes
            setupZentraleOrganisation(true);
            Benutzer arztBasis = BenutzerBuilder.create(b -> b.withNachname("ArztBasis"));
            Benutzer arztOther = BenutzerBuilder.create(b -> b.withNachname("ArztOther"));

            Dossier basisDossier = DossierBuilder.create(d -> d.withZustaendigArzt(arztBasis));
            Dossier otherDossier = DossierBuilder.create(d -> d.withZustaendigArzt(arztOther));

            // Act
            service.verantwortlicherChangedByDuplikatMerge(basisDossier, otherDossier);

            // Assert: should NOT archive for zentrale Organisation
            verify(archivierungService, never()).createGesamtArchivdokument(any(), any(), any());
        }
    }
}
