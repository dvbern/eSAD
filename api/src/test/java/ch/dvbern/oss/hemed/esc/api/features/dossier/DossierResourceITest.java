package ch.dvbern.oss.hemed.esc.api.features.dossier;

import java.util.List;
import java.util.UUID;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerRepo;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.DossierService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.kinder.KindService;
import ch.dvbern.oss.hemed.esc.api.features.organisation.Organisation;
import ch.dvbern.oss.hemed.esc.api.features.organisation.OrganisationService;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.SchuleService;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.KlasseService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.BenutzerBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.DossierBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.KindBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.KlasseBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.NachkontrolleBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.SchuleBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSchularztBE;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.DossierApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.KinderApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.SchulenApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AnlassFlagsJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AppFailureErrorJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AppValidationErrorJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.BenutzerOptionJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.DossierJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.EntityAktivStatusSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.EntityAktivStatusSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.FailureTypeSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KindJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.PapierakteStatusSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.SchuleJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.SchuleSaveJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
@QuarkusTest
@RequiredArgsConstructor
class DossierResourceITest extends AbstractITest {

    private final DossierApiSpec api = DossierApiSpec.dossier(RequestSpecUtil.quarkusSpec());
    private final KinderApiSpec kindAPI = KinderApiSpec.kinder(RequestSpecUtil.quarkusSpec());
    private final SchulenApiSpec schuleAPI = SchulenApiSpec.schulen(RequestSpecUtil.quarkusSpec());

    private final DossierService dossierService;
    private final KindService kindService;
    private final SchuleService schuleService;
    private final KlasseService klasseService;
    private final BenutzerService benutzerService;
    private final BenutzerRepo benutzerRepo;
    private final AppClock appClock;
    private final OrganisationService organisationService;
    private final FixtureService fixtureService;
    private final TenantContext tenantContext;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    @TestAsAdminBS
    void createDossier() {
        // ARRANGE
        Kind kind = KindBuilder.create(builder -> builder
            .withName("Kind", "Kira"));
        kind = kindService.persistWithoutDuplicateCheck(kind);
        // ACT
        DossierJsonSpec result = api
            .dossierResourceGetOrCreateDossier()
            .kindIdPath(kind.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(DossierJsonSpec.class);
        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isNotNull();
        Assertions.assertThat(result.getKind().getData().getName()).isEqualTo("Kind");
        Assertions.assertThat(result.getKind().getData().getVorname()).isEqualTo("Kira");

        // ACT: Dasselbe Dossier nochmals lesen:
        DossierJsonSpec result2 = api
            .dossierResourceGetOrCreateDossier()
            .kindIdPath(kind.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(DossierJsonSpec.class);
        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result2.getId()).isEqualTo(result.getId());
    }

    @Test
    @TestAsAdminBS
    void updateDossierMedizinisch() {
        // ARRANGE
        Kind kind = kindService.persistWithoutDuplicateCheck(KindBuilder.defaultKind());
        Dossier existingDossier = dossierService.persist(
            DossierBuilder.create(builder -> builder.forKind(kind)));
        Assertions.assertThat(existingDossier.isAndereZusatzinformation()).isFalse();
        Assertions.assertThat(existingDossier.getAndereZusatzInformationText()).isNull();
        String zusatzinformation = "MeineTestInfo";

        Organisation organisation = organisationService.getZentraleOrganisation();

        Benutzer createDto = BenutzerBuilder.create(
            builder -> builder
                .withRandomEmail("nina.neu@example.com")
                .withRandomVorname("Nina")
                .withRandomNachname("Neu")
                .withOrganisationRollen(organisation, List.of(FachRolle.SCHULAERZTIN, FachRolle.MPA))
                .withKeycloakId(UUID.randomUUID().toString())
        );
        benutzerRepo.persist(createDto);

        DossierJsonSpec updatedDossierJson = DossierBuilder.createJaxSpec(
            builder -> builder
                .forDossier(existingDossier)
                .withAndereZusatzinformation(true, zusatzinformation),
            appClock.offsetDateTime().now().toLocalDate());

        assertNotNull(updatedDossierJson.getDataMedizinisch());
        updatedDossierJson.getDataMedizinisch().setNachkontrolle(NachkontrolleBuilder.createJaxSpec(builder ->
            builder.
                withNachkontrolleSehen(true)));

        // ACT
        DossierJsonSpec result = api
            .dossierResourceUpdateDossierMedizinisch()
            .dossierIdPath(existingDossier.getId().getId())
            .body(updatedDossierJson.getDataMedizinisch())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(DossierJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isEqualTo(existingDossier.getId().getId());
        Assertions.assertThat(result.getDataMedizinisch()).isNotNull();
        Assertions.assertThat(result.getDataMedizinisch().getAndereZusatzinformation()).isTrue();
        Assertions.assertThat(result.getDataMedizinisch().getAndereZusatzInformationText()).isEqualTo(zusatzinformation);
        AnlassFlagsJsonSpec anlassFlags = result.getDataMedizinisch().getNachkontrolle().getData().getAnlassFlags();
        Assertions.assertThat(anlassFlags)
            .extracting(AnlassFlagsJsonSpec::getNachkontrolleSehen).isEqualTo(true);
    }

    @Test
    @TestAsAdminBS
    void updateDossier_Medizinisch_throws_if_Kind_is_inactive() {
        // ARRANGE
        Kind kind = kindService.persistWithoutDuplicateCheck(KindBuilder.defaultKind());
        Dossier existingDossier = dossierService.persist(
            DossierBuilder.create(builder -> builder.forKind(kind)));
        Assertions.assertThat(existingDossier.isAndereZusatzinformation()).isFalse();
        Assertions.assertThat(existingDossier.getAndereZusatzInformationText()).isNull();
        String zusatzinformation = "MeineTestInfo";

        Organisation organisation = organisationService.getZentraleOrganisation();

        Benutzer createDto = BenutzerBuilder.create(
            builder -> builder
                .withRandomEmail("nina.neu@example.com")
                .withRandomVorname("Nina")
                .withRandomNachname("Neu")
                .withOrganisationRollen(organisation, List.of(FachRolle.SCHULAERZTIN, FachRolle.MPA))
                .withKeycloakId(UUID.randomUUID().toString())
        );
        benutzerRepo.persist(createDto);

        EntityAktivStatusSaveJsonSpec saveJson = new EntityAktivStatusSaveJsonSpec(EntityAktivStatusSpec.INAKTIV);
        KindJsonSpec result = kindAPI.kindResourceUpdateKindAktivStatus()
            .kindIdPath(kind.getId().getId())
            .body(saveJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(KindJsonSpec.class);


        Assertions.assertThat(result.getData().getKindAktivStatus()).isEqualTo(EntityAktivStatusSpec.INAKTIV);

        DossierJsonSpec updatedDossierJson = DossierBuilder.createJaxSpec(
            builder -> builder
                .forDossier(existingDossier)
                .withAndereZusatzinformation(true, zusatzinformation),
            appClock.offsetDateTime().now().toLocalDate());

        assertThat(updatedDossierJson.getDataMedizinisch()).isNotNull();
        updatedDossierJson.getDataMedizinisch().setNachkontrolle(NachkontrolleBuilder.createJaxSpec(builder ->
            builder.
                withNachkontrolleSehen(true)));

        String expectedValidationErrorMessage = String.format(
            "Das Kind %s %s ist inaktiv. Es können keine Änderungen an Dossier %s vorgenommen werden.",
            kind.getNameVorname(),
            kind.getId(),
            existingDossier.getId());

        // ACT
        AppValidationErrorJsonSpec validationError = api
                .dossierResourceUpdateDossierMedizinisch()
                .dossierIdPath(existingDossier.getId().getId())
                .body(updatedDossierJson.getDataMedizinisch())
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.BAD_REQUEST.getStatusCode())
                .extract().as(AppValidationErrorJsonSpec.class);

        assertThat(validationError.getViolations()).hasSize(1);
        assertThat(validationError.getViolations().getFirst().getKey())
            .isEqualTo("CannotEditEntityForInactiveKind");
        assertThat(validationError.getViolations().getFirst().getMessage())
            .isEqualTo(expectedValidationErrorMessage);
    }

    @Test
    @TestAsAdminBS
    void updateDossier_Medizinisch_throws_if_benutzer_does_not_have_fachrolle() {
        // ARRANGE
        Kind kind = kindService.persistWithoutDuplicateCheck(KindBuilder.defaultKind());
        Dossier existingDossier = dossierService.persist(
            DossierBuilder.create(builder -> builder.forKind(kind)));

        Benutzer benutzer = fixtureService.createBenutzerNoRoles();
        BenutzerOptionJsonSpec benutzerOptionJson = BenutzerBuilder.createOptionJaxSpec(
            builder -> builder.forBenutzer(benutzer));

        // ACT
        AppFailureErrorJsonSpec errorArzt = api
            .dossierResourceUpdateDossierVerantwortlichArzt()
            .dossierIdPath(existingDossier.getId().getId())
            .body(benutzerOptionJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
            .extract().as(AppFailureErrorJsonSpec.class);
        assertThat(errorArzt).isNotNull();
        assertThat(errorArzt.getType()).isEqualTo(FailureTypeSpec.FAILURE);

        AppFailureErrorJsonSpec errorMpa = api
            .dossierResourceUpdateDossierVerantwortlichMpa()
            .dossierIdPath(existingDossier.getId().getId())
            .body(benutzerOptionJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
            .extract().as(AppFailureErrorJsonSpec.class);
        assertThat(errorMpa).isNotNull();
        assertThat(errorMpa.getType()).isEqualTo(FailureTypeSpec.FAILURE);
    }

    @Test
    @TestAsAdminBS
    void removeDossier() {
        // ARRANGE
        Kind kind = kindService.persistWithoutDuplicateCheck(KindBuilder.defaultKind());
        Dossier dossierToDelete = dossierService.persist(
            DossierBuilder.create(builder -> builder.forKind(kind)));

        // ACT
        ID<Dossier> dossierID = dossierToDelete.getId();
        api.dossierResourceRemoveDossier()
            .dossierIdPath(dossierID.getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());

        // ASSERT
        AppFailureException ex = assertThrows(
            AppFailureException.class,
            () -> dossierService.getById(dossierID)
        );
        assertThat(ex)
            .hasMessageContaining("ENTITY_NOT_FOUND");
    }

    @Test
    @TestAsAdminBS
    void openDossier_forKindWithoutKlasse_shouldNotSet() {
        // ARRANGE
        Kind kind = kindService.persistWithoutDuplicateCheck(KindBuilder.defaultKind());

        // ACT
        DossierJsonSpec result = api.dossierResourceGetOrCreateDossier()
            .kindIdPath(kind.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(DossierJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getKind().getData().getSchule()).isNull();
        assertThat(result.getKind().getData().getKlasse()).isNull();
        assertThat(result.getDataNichtMedizinisch().getZustaendigArzt()).isNull();
        assertThat(result.getDataNichtMedizinisch().getZustaendigMPA()).isNull();

        assertThat(result.getKind().getId()).isEqualTo(kind.getId().getId());

    }

    @Test
    @TestAsAdminBS
    void openDossier_forKindWithKlasse_schuleHasMpaAndNoArzt_shouldSetOnDossier() {
        // ARRANGE
        Organisation organisation = organisationService.getZentraleOrganisation();
        Benutzer mpa = benutzerService.persist(BenutzerBuilder.create(
            builder -> builder.withOrganisationRollen(organisation, List.of(FachRolle.MPA))));
        Schule schule = schuleService.persist(SchuleBuilder.create(builder -> builder.withZustaendigMPA(mpa)));
        Klasse klasse = klasseService.persist(KlasseBuilder.create(builder -> builder.forSchule(schule)));
        Kind kind = kindService.persistWithoutDuplicateCheck(KindBuilder.create(builder -> builder.withKlasse(klasse)));

        // ACT
        DossierJsonSpec result = api.dossierResourceGetOrCreateDossier()
            .kindIdPath(kind.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(DossierJsonSpec.class);

        // ASSERT not null
        assertThat(result).isNotNull();
        assertThat(result.getKind().getData().getSchule()).isNotNull();
        assertThat(result.getKind().getData().getKlasse()).isNotNull();
        assertThat(result.getDataNichtMedizinisch().getZustaendigMPA()).isNotNull();
        assertThat(result.getDataNichtMedizinisch().getZustaendigArzt()).isNull();
        // ASSERT ID's
        assertThat(result.getKind().getId()).isEqualTo(kind.getId().getId());
        assertThat(result.getKind().getData().getSchule().getId()).isEqualTo(schule.getId().getId());
        assertThat(result.getKind().getData().getKlasse().getId()).isEqualTo(klasse.getId().getId());
        assertThat(result.getDataNichtMedizinisch().getZustaendigMPA().getId()).isEqualTo(mpa.getId().getId());
    }

    @Test
    @TestAsAdminBS
    void updateArztAndMpaOnSchule_shouldUpdateEachDossierWithNew() {
        // ARRANGE
        Organisation organisation = organisationService.getZentraleOrganisation();

        Benutzer arztMpa = benutzerService.persist(BenutzerBuilder.create(
            builder -> builder.withOrganisationRollen(organisation, List.of(FachRolle.SCHULAERZTIN, FachRolle.MPA))));
        Benutzer arztMpa2 = benutzerService.persist(BenutzerBuilder.create(
            builder -> builder.withOrganisationRollen(organisation, List.of(
                FachRolle.SCHULAERZTIN, FachRolle.MPA))));
        Schule schule = schuleService.persist(SchuleBuilder.create(builder -> builder.withZustaendigArzt(arztMpa)
            .withZustaendigMPA(arztMpa)));
        Klasse klasse = klasseService.persist(KlasseBuilder.create(builder -> builder.forSchule(schule)));
        Kind kind1 =
            kindService.persistWithoutDuplicateCheck(KindBuilder.create(builder -> builder.withKlasse(klasse)));
        Kind kind2 =
            kindService.persistWithoutDuplicateCheck(KindBuilder.create(builder -> builder.withKlasse(klasse)));

        // ACT
        DossierJsonSpec dossier1 = api.dossierResourceGetOrCreateDossier()
            .kindIdPath(kind1.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(DossierJsonSpec.class);

        DossierJsonSpec dossier2 = api.dossierResourceGetOrCreateDossier()
            .kindIdPath(kind2.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(DossierJsonSpec.class);

        // ASSERT
        assertThat(dossier1.getKind().getData().getSchule()).isNotNull();
        assertThat(dossier1.getKind().getData().getKlasse()).isNotNull();
        assertThat(dossier1.getDataNichtMedizinisch().getZustaendigArzt()).isNotNull();
        assertThat(dossier1.getDataNichtMedizinisch().getZustaendigMPA()).isNotNull();

        assertThat(dossier2.getKind().getData().getSchule()).isNotNull();
        assertThat(dossier2.getKind().getData().getKlasse()).isNotNull();
        assertThat(dossier2.getDataNichtMedizinisch().getZustaendigArzt()).isNotNull();
        assertThat(dossier2.getDataNichtMedizinisch().getZustaendigMPA()).isNotNull();

        assertThat(dossier1.getKind().getId()).isEqualTo(kind1.getId().getId());
        assertThat(dossier1.getKind().getData().getSchule().getId()).isEqualTo(schule.getId().getId());
        assertThat(dossier1.getKind().getData().getKlasse().getId()).isEqualTo(klasse.getId().getId());
        assertThat(dossier1.getDataNichtMedizinisch().getZustaendigArzt().getId()).isEqualTo(arztMpa.getId().getId());
        assertThat(dossier1.getDataNichtMedizinisch().getZustaendigMPA().getId()).isEqualTo(arztMpa.getId().getId());

        assertThat(dossier2.getKind().getId()).isEqualTo(kind2.getId().getId());
        assertThat(dossier2.getKind().getData().getSchule().getId()).isEqualTo(schule.getId().getId());
        assertThat(dossier2.getKind().getData().getKlasse().getId()).isEqualTo(klasse.getId().getId());
        assertThat(dossier2.getDataNichtMedizinisch().getZustaendigArzt().getId()).isEqualTo(arztMpa.getId().getId());
        assertThat(dossier2.getDataNichtMedizinisch().getZustaendigMPA().getId()).isEqualTo(arztMpa.getId().getId());

        // ACT - Update Arzt and MPA on Schule
        schule.setZustaendigArzt(arztMpa2);
        schule.setZustaendigMPA(arztMpa2);

        SchuleSaveJsonSpec schuleSaveJsonSpec = SchuleBuilder.createSaveJaxForExistingSchule(schule);

        SchuleJsonSpec schuleResult = schuleAPI.schuleResourceUpdateSchule().schuleIdPath(schule.getId().getId())
            .body(schuleSaveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(SchuleJsonSpec.class);

        DossierJsonSpec updatedDossier1 = api.dossierResourceGetOrCreateDossier()
            .kindIdPath(kind1.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(DossierJsonSpec.class);

        DossierJsonSpec updatedDossier2 = api.dossierResourceGetOrCreateDossier()
            .kindIdPath(kind2.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(DossierJsonSpec.class);

        // ASSERT - Updated Correctly
        assertThat(schuleResult).isNotNull();
        assertThat(schuleResult.getData().getZustaendigArzt()).isNotNull();
        assertThat(schuleResult.getData().getZustaendigMPA()).isNotNull();
        assertThat(updatedDossier1.getDataNichtMedizinisch().getZustaendigArzt()).isNotNull();
        assertThat(updatedDossier1.getDataNichtMedizinisch().getZustaendigMPA()).isNotNull();
        assertThat(updatedDossier2.getDataNichtMedizinisch().getZustaendigArzt()).isNotNull();
        assertThat(updatedDossier2.getDataNichtMedizinisch().getZustaendigMPA()).isNotNull();

        assertThat(schuleResult.getData().getZustaendigArzt().getId()).isEqualTo(arztMpa2.getId().getId());
        assertThat(schuleResult.getData().getZustaendigMPA().getId()).isEqualTo(arztMpa2.getId().getId());
        assertThat(updatedDossier1.getDataNichtMedizinisch().getZustaendigArzt().getId()).isEqualTo(arztMpa2.getId().getId());
        assertThat(updatedDossier1.getDataNichtMedizinisch().getZustaendigMPA().getId()).isEqualTo(arztMpa2.getId().getId());
        assertThat(updatedDossier2.getDataNichtMedizinisch().getZustaendigArzt().getId()).isEqualTo(arztMpa2.getId().getId());
        assertThat(updatedDossier2.getDataNichtMedizinisch().getZustaendigMPA().getId()).isEqualTo(arztMpa2.getId().getId());

    }

    @Test
    @TestAsAdminBS
    void updateDossierVerantwortlichArzt() {
        // ARRANGE
        Organisation organisation = organisationService.getZentraleOrganisation();
        Kind kind = kindService.persistWithoutDuplicateCheck(KindBuilder.defaultKind());
        Dossier existingDossier = dossierService.persist(
            DossierBuilder.create(builder -> builder.forKind(kind)));

        Benutzer arzt = benutzerService.persist(BenutzerBuilder.create(
            builder -> builder.withOrganisationRollen(organisation, List.of(FachRolle.SCHULAERZTIN))));
        BenutzerOptionJsonSpec benutzerOptionJson = BenutzerBuilder.createOptionJaxSpec(
            builder -> builder.forBenutzer(arzt));

        // ACT
        DossierJsonSpec result = api
            .dossierResourceUpdateDossierVerantwortlichArzt()
            .dossierIdPath(existingDossier.getId().getId())
            .body(benutzerOptionJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(DossierJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(existingDossier.getId().getId());
        assertThat(result.getDataNichtMedizinisch().getZustaendigArzt()).isNotNull();
        assertThat(result.getDataNichtMedizinisch().getZustaendigArzt().getId()).isEqualTo(arzt.getId().getId());
    }

    @Test
    @TestAsAdminBS
    void updateDossierVerantwortlichMpa() {
        // ARRANGE
        Organisation organisation = organisationService.getZentraleOrganisation();
        Kind kind = kindService.persistWithoutDuplicateCheck(KindBuilder.defaultKind());
        Dossier existingDossier = dossierService.persist(
            DossierBuilder.create(builder -> builder.forKind(kind)));

        Benutzer mpa = benutzerService.persist(BenutzerBuilder.create(
            builder -> builder.withOrganisationRollen(organisation, List.of(FachRolle.MPA))));
        BenutzerOptionJsonSpec benutzerOptionJson = BenutzerBuilder.createOptionJaxSpec(
            builder -> builder.forBenutzer(mpa));

        // ACT
        DossierJsonSpec result = api
            .dossierResourceUpdateDossierVerantwortlichMpa()
            .dossierIdPath(existingDossier.getId().getId())
            .body(benutzerOptionJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(DossierJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(existingDossier.getId().getId());
        assertThat(result.getDataNichtMedizinisch().getZustaendigMPA()).isNotNull();
        assertThat(result.getDataNichtMedizinisch().getZustaendigMPA().getId()).isEqualTo(mpa.getId().getId());
    }

    @Test
    @TestAsAdminBS
    void updateDossierPapierakteStatus() {
        // ARRANGE
        Kind kind = kindService.persistWithoutDuplicateCheck(KindBuilder.defaultKind());
        Dossier existingDossier = dossierService.persist(
            DossierBuilder.create(builder -> builder.forKind(kind)));

        // ACT
        DossierJsonSpec result = api
            .dossierResourceUpdateDossierPapierakteStatus()
            .dossierIdPath(existingDossier.getId().getId())
            .statusPath(PapierakteStatusSpec.VORHANDEN)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(DossierJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(existingDossier.getId().getId());
        assertThat(result.getDataNichtMedizinisch().getPapierakteStatus()).isEqualTo(PapierakteStatusSpec.VORHANDEN);

        // ACT - update to a different status
        DossierJsonSpec result2 = api
            .dossierResourceUpdateDossierPapierakteStatus()
            .dossierIdPath(existingDossier.getId().getId())
            .statusPath(PapierakteStatusSpec.NICHT_VORHANDEN)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(DossierJsonSpec.class);

        // ASSERT
        assertThat(result2).isNotNull();
        assertThat(result2.getId()).isEqualTo(existingDossier.getId().getId());
        assertThat(result2.getDataNichtMedizinisch().getPapierakteStatus()).isEqualTo(PapierakteStatusSpec.NICHT_VORHANDEN);
    }

    @Test
    @TestAsSchularztBE
    void updateDossierPapierakteStatus_throws_if_user_not_zustaendig() {
        // ARRANGE
        tenantContext.setCurrentTenant(TenantEnum.BE);

        // Dossier gehoert einem Arzt einer anderen Organisation,
        // der aktuelle Benutzer (TestAsSchularztBE) ist nicht zustaendig
        Dossier existingDossier = createDossierOfOtherOrganisation();

        // ACT
        AppFailureErrorJsonSpec error = api
            .dossierResourceUpdateDossierPapierakteStatus()
            .dossierIdPath(existingDossier.getId().getId())
            .statusPath(PapierakteStatusSpec.VORHANDEN)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
            .extract().as(AppFailureErrorJsonSpec.class);

        // ASSERT
        assertThat(error).isNotNull();
        assertThat(error.getType()).isEqualTo(FailureTypeSpec.FAILURE);
    }

    @Test
    @TestAsSchularztBE
    void updateDossierVerantwortlichArzt_throws_if_user_not_zustaendig() {
        // ARRANGE
        tenantContext.setCurrentTenant(TenantEnum.BE);

        Dossier existingDossier = createDossierOfOtherOrganisation();
        Benutzer arzt = fixtureService.readBenutzerWithBerechtigungen(TestAsSchularztBE.BENUTZER_ID_ID);
        BenutzerOptionJsonSpec benutzerOptionJson = BenutzerBuilder.createOptionJaxSpec(
            builder -> builder.forBenutzer(arzt));

        // ACT
        AppFailureErrorJsonSpec error = api
            .dossierResourceUpdateDossierVerantwortlichArzt()
            .dossierIdPath(existingDossier.getId().getId())
            .body(benutzerOptionJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
            .extract().as(AppFailureErrorJsonSpec.class);

        // ASSERT
        assertThat(error).isNotNull();
        assertThat(error.getType()).isEqualTo(FailureTypeSpec.FAILURE);
    }

    @NonNull
    private Dossier createDossierOfOtherOrganisation() {
        Benutzer otherArzt = fixtureService.createBenutzerWithOrganisation();
        Kind kind = kindService.persistWithoutDuplicateCheck(KindBuilder.defaultKind());
        return dossierService.persist(DossierBuilder.create(builder -> builder
            .forKind(kind)
            .withZustaendigArzt(otherArzt)));
    }
}
