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
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.SchuleService;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.KlasseService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.BenutzerBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.DossierBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.KindBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.KlasseBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.NachkontrolleBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.SchuleBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.DossierApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.KinderApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.SchulenApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AnlassFlagsJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AppValidationErrorJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.BenutzerOptionJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.DossierJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.DossierSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.EntityAktivStatusSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.EntityAktivStatusSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KindJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KindSaveModeDuplikatSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.KindSaveResultJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.SchuleJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.SchuleSaveJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
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
    void updateDossier() {
        // ARRANGE
        Kind kind = kindService.persistWithoutDuplicateCheck(KindBuilder.defaultKind());
        Dossier existingDossier = dossierService.persist(
            DossierBuilder.create(builder -> builder.forKind(kind)));
        Assertions.assertThat(existingDossier.isAndereZusatzinformation()).isFalse();
        Assertions.assertThat(existingDossier.getAndereZusatzInformationText()).isNull();
        String zusatzinformation = "MeineTestInfo";

        Benutzer createDto = BenutzerBuilder.create(
            builder -> builder
                .withRandomEmail("nina.neu@example.com")
                .withRandomVorname("Nina")
                .withRandomNachname("Neu")
                .withFachRolle(List.of(FachRolle.SCHULAERZTIN, FachRolle.MPA))
                .withKeycloakId(UUID.randomUUID().toString())
        );
        benutzerRepo.persist(createDto);
        Benutzer zustaendigArztMPA = benutzerService.getById(createDto.getId());

        DossierJsonSpec updatedDossierJson = DossierBuilder.createJaxSpec(
            builder -> builder
                .forDossier(existingDossier)
                .withAndereZusatzinformation(true, zusatzinformation)
                .withZustaendigMPA(zustaendigArztMPA)
                .withZustaendigArzt(zustaendigArztMPA),
            appClock.offsetDateTime().now().toLocalDate());

        updatedDossierJson.getData().setNachkontrolle(NachkontrolleBuilder.createJaxSpec(builder ->
            builder.
                withNachkontrolleSehen(true)));

        // ACT
        DossierJsonSpec result = api
            .dossierResourceUpdateDossier()
            .dossierIdPath(existingDossier.getId().getId())
            .body(updatedDossierJson.getData())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(DossierJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isEqualTo(existingDossier.getId().getId());
        Assertions.assertThat(result.getData().getAndereZusatzinformation()).isTrue();
        Assertions.assertThat(result.getData().getAndereZusatzInformationText()).isEqualTo(zusatzinformation);
        Assertions.assertThat(result.getData().getZustaendigArzt())
            .isNotNull()
            .extracting(BenutzerOptionJsonSpec::getId).isEqualTo(zustaendigArztMPA.getId().getId());
        Assertions.assertThat(result.getData().getZustaendigMPA())
            .isNotNull()
            .extracting(BenutzerOptionJsonSpec::getId).isEqualTo(zustaendigArztMPA.getId().getId());
        AnlassFlagsJsonSpec anlassFlags = result.getData().getNachkontrolle().getData().getAnlassFlags();
        Assertions.assertThat(anlassFlags)
            .extracting(AnlassFlagsJsonSpec::getNachkontrolleSehen).isEqualTo(true);
    }

    @Test
    @TestAsAdminBS
    void updateDossier_throws_if_Kind_is_inactive() {
        // ARRANGE
        Kind kind = kindService.persistWithoutDuplicateCheck(KindBuilder.defaultKind());
        Dossier existingDossier = dossierService.persist(
            DossierBuilder.create(builder -> builder.forKind(kind)));
        Assertions.assertThat(existingDossier.isAndereZusatzinformation()).isFalse();
        Assertions.assertThat(existingDossier.getAndereZusatzInformationText()).isNull();
        String zusatzinformation = "MeineTestInfo";

        Benutzer createDto = BenutzerBuilder.create(
            builder -> builder
                .withRandomEmail("nina.neu@example.com")
                .withRandomVorname("Nina")
                .withRandomNachname("Neu")
                .withFachRolle(List.of(FachRolle.SCHULAERZTIN, FachRolle.MPA))
                .withKeycloakId(UUID.randomUUID().toString())
        );
        benutzerRepo.persist(createDto);

        EntityAktivStatusSaveJsonSpec saveJson = new EntityAktivStatusSaveJsonSpec(EntityAktivStatusSpec.INAKTIV);
        kindAPI.kindResourceUpdateKindAktivStatus()
            .kindIdPath(kind.getId().getId())
            .body(saveJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());

        Benutzer zustaendigArztMPA = benutzerService.getById(createDto.getId());

        DossierJsonSpec updatedDossierJson = DossierBuilder.createJaxSpec(
            builder -> builder
                .forDossier(existingDossier)
                .withAndereZusatzinformation(true, zusatzinformation)
                .withZustaendigMPA(zustaendigArztMPA)
                .withZustaendigArzt(zustaendigArztMPA),
            appClock.offsetDateTime().now().toLocalDate());

        updatedDossierJson.getData().setNachkontrolle(NachkontrolleBuilder.createJaxSpec(builder ->
            builder.
                withNachkontrolleSehen(true)));

        String expectedValidationErrorMessage = String.format(
            "Das Kind %s %s ist inaktiv. Es können keine Änderungen an Dossier %s vorgenommen werden.",
            kind.getNameVorname(),
            kind.getId(),
            existingDossier.getId());

        // ACT
        AppValidationErrorJsonSpec validationError = api
                .dossierResourceUpdateDossier()
                .dossierIdPath(existingDossier.getId().getId())
                .body(updatedDossierJson.getData())
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
    void updateDossier_throws_if_benutzer_does_not_have_fachrolle() {
        // ARRANGE
        Kind kind = kindService.persistWithoutDuplicateCheck(KindBuilder.defaultKind());
        Dossier existingDossier = dossierService.persist(
            DossierBuilder.create(builder -> builder.forKind(kind)));

        var createDto = BenutzerBuilder.createCreateDTO(
            builder -> builder
                .withRandomEmail("nina.neu@example.com")
                .withRandomVorname("Nina")
                .withRandomNachname("Neu")
                .withKeycloakId(UUID.randomUUID().toString())
        );
        Benutzer benutzer = benutzerService.getById(benutzerService.createBenutzer(createDto));

        DossierJsonSpec updatedDossierJson = DossierBuilder.createJaxSpec(
            builder -> builder
                .forDossier(existingDossier)
                .withZustaendigArzt(benutzer)
                .withZustaendigMPA(benutzer),
            appClock.offsetDateTime().now().toLocalDate());

        // ACT
        String dossierId = existingDossier.getId().getId();
        DossierSaveJsonSpec data = updatedDossierJson.getData();
        AppValidationErrorJsonSpec validationError = api
                .dossierResourceUpdateDossier()
                .dossierIdPath(dossierId)
                .body(data)
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.BAD_REQUEST.getStatusCode())
                .extract().as(AppValidationErrorJsonSpec.class);

        assertThat(validationError.getViolations()).hasSize(1);
        assertThat(validationError.getViolations().getFirst().getKey())
            .isEqualTo("NotNull");
        assertThat(validationError.getViolations().getFirst().getMessage())
            .isEqualTo("darf nicht null sein");
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
        assertThat(result.getData().getZustaendigArzt()).isNull();
        assertThat(result.getData().getZustaendigMPA()).isNull();

        assertThat(result.getKind().getId()).isEqualTo(kind.getId().getId());

    }

    @Test
    @TestAsAdminBS
    void openDossier_forKindWithKlasse_schuleHasMpaAndNoArzt_shouldSetOnDossier() {
        // ARRANGE
        Benutzer mpa = benutzerService.persist(BenutzerBuilder.create(
            builder -> builder.withFachRolle(List.of(FachRolle.MPA))));
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
        assertThat(result.getData().getZustaendigMPA()).isNotNull();
        assertThat(result.getData().getZustaendigArzt()).isNull();
        // ASSERT ID's
        assertThat(result.getKind().getId()).isEqualTo(kind.getId().getId());
        assertThat(result.getKind().getData().getSchule().getId()).isEqualTo(schule.getId().getId());
        assertThat(result.getKind().getData().getKlasse().getId()).isEqualTo(klasse.getId().getId());
        assertThat(result.getData().getZustaendigMPA().getId()).isEqualTo(mpa.getId().getId());
    }

    @Test
    @TestAsAdminBS
    void openDossier_forKindWithKlasse_schuleHasArztAndMpa_shouldSetOnDossier_changeSchule_shouldSetNull() {
        // ARRANGE
        Benutzer arztMpa = benutzerService.persist(BenutzerBuilder.create(
            builder -> builder.withFachRolle(List.of(FachRolle.MPA, FachRolle.SCHULAERZTIN))
                .withRandomNachname("ARZTMPA1___")));
        Schule schule = schuleService.persist(SchuleBuilder.create(builder -> builder.withZustaendigArzt(arztMpa)
            .withZustaendigMPA(arztMpa).withSchulhaus("Schulhaus 1")));
        Klasse klasse = klasseService.persist(KlasseBuilder.create(builder -> builder.forSchule(schule)
            .withBezeichnung("Klasse 1")));
        Kind kind = kindService.persistWithoutDuplicateCheck(KindBuilder.create(builder -> builder.withKlasse(klasse)));

        Benutzer newArztMpa = benutzerService.persist(BenutzerBuilder.create(
            builder -> builder.withFachRolle(List.of(FachRolle.MPA, FachRolle.SCHULAERZTIN))
                .withRandomNachname("ARZTMPA2___")));
        Schule newSchule = schuleService.persist(SchuleBuilder.create(builder -> builder.withZustaendigArzt(newArztMpa)
            .withZustaendigMPA(newArztMpa).withSchulhaus("Schulhaus 2")));
        Klasse newKlasse = klasseService.persist(KlasseBuilder.create(builder -> builder.forSchule(newSchule)
            .withBezeichnung("Klasse 2")));
        KindJsonSpec kindJsonSpec = KindBuilder.createJaxSpec(
            builder -> builder.withKlasse(newKlasse)
                .withID(kind.getId())
                .withName(kind.getName(), kind.getVorname()),
            appClock.offsetDateTime().now().toLocalDate());

        // ACT - get Dossier
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
        assertThat(result.getData().getZustaendigArzt()).isNotNull();
        assertThat(result.getData().getZustaendigMPA()).isNotNull();

        // Assert ID's
        assertThat(result.getKind().getId()).isEqualTo(kind.getId().getId());
        assertThat(result.getKind().getData().getSchule().getId()).isEqualTo(schule.getId().getId());
        assertThat(result.getKind().getData().getKlasse().getId()).isEqualTo(klasse.getId().getId());
        assertThat(result.getData().getZustaendigArzt().getId()).isEqualTo(arztMpa.getId().getId());
        assertThat(result.getData().getZustaendigMPA().getId()).isEqualTo(arztMpa.getId().getId());

        // ACT - change Klasse
        kindAPI
            .kindResourceUpdateKind()
            .kindIdPath(kind.getId().getId())
            .body(kindJsonSpec.getData())
            .kindSaveModeQuery(KindSaveModeDuplikatSpec.NO_CHECK)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(KindSaveResultJsonSpec.class);

        DossierJsonSpec updatedResult = api.dossierResourceGetOrCreateDossier()
            .kindIdPath(kind.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(DossierJsonSpec.class);

        // ASSERT not null
        assertThat(updatedResult).isNotNull();
        assertThat(updatedResult.getKind().getData().getSchule()).isNotNull();
        assertThat(updatedResult.getKind().getData().getKlasse()).isNotNull();
        assertThat(updatedResult.getData().getZustaendigArzt()).isNotNull();
        assertThat(updatedResult.getData().getZustaendigMPA()).isNotNull();

        // Assert ID's
        assertThat(updatedResult.getKind().getId()).isEqualTo(kind.getId().getId());
        assertThat(updatedResult.getKind().getData().getSchule().getId()).isEqualTo(newSchule.getId().getId());
        assertThat(updatedResult.getData().getZustaendigMPA().getId()).isEqualTo(newArztMpa.getId().getId());
        assertThat(updatedResult.getData().getZustaendigMPA().getId()).isEqualTo(newArztMpa.getId().getId());
    }

    @Test
    @TestAsAdminBS
    void updateArztAndMpaOnSchule_shouldUpdateEachDossierWithNew() {
        // ARRANGE
        Benutzer arztMpa = benutzerService.persist(BenutzerBuilder.create(
            builder -> builder.withFachRolle(List.of(
                FachRolle.SCHULAERZTIN, FachRolle.MPA))));
        Benutzer arztMpa2 = benutzerService.persist(BenutzerBuilder.create(
            builder -> builder.withFachRolle(List.of(
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
        assertThat(dossier1.getData().getZustaendigArzt()).isNotNull();
        assertThat(dossier1.getData().getZustaendigMPA()).isNotNull();

        assertThat(dossier2.getKind().getData().getSchule()).isNotNull();
        assertThat(dossier2.getKind().getData().getKlasse()).isNotNull();
        assertThat(dossier2.getData().getZustaendigArzt()).isNotNull();
        assertThat(dossier2.getData().getZustaendigMPA()).isNotNull();

        assertThat(dossier1.getKind().getId()).isEqualTo(kind1.getId().getId());
        assertThat(dossier1.getKind().getData().getSchule().getId()).isEqualTo(schule.getId().getId());
        assertThat(dossier1.getKind().getData().getKlasse().getId()).isEqualTo(klasse.getId().getId());
        assertThat(dossier1.getData().getZustaendigArzt().getId()).isEqualTo(arztMpa.getId().getId());
        assertThat(dossier1.getData().getZustaendigMPA().getId()).isEqualTo(arztMpa.getId().getId());

        assertThat(dossier2.getKind().getId()).isEqualTo(kind2.getId().getId());
        assertThat(dossier2.getKind().getData().getSchule().getId()).isEqualTo(schule.getId().getId());
        assertThat(dossier2.getKind().getData().getKlasse().getId()).isEqualTo(klasse.getId().getId());
        assertThat(dossier2.getData().getZustaendigArzt().getId()).isEqualTo(arztMpa.getId().getId());
        assertThat(dossier2.getData().getZustaendigMPA().getId()).isEqualTo(arztMpa.getId().getId());

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
        assertThat(updatedDossier1.getData().getZustaendigArzt()).isNotNull();
        assertThat(updatedDossier1.getData().getZustaendigMPA()).isNotNull();
        assertThat(updatedDossier2.getData().getZustaendigArzt()).isNotNull();
        assertThat(updatedDossier2.getData().getZustaendigMPA()).isNotNull();

        assertThat(schuleResult.getData().getZustaendigArzt().getId()).isEqualTo(arztMpa2.getId().getId());
        assertThat(schuleResult.getData().getZustaendigMPA().getId()).isEqualTo(arztMpa2.getId().getId());
        assertThat(updatedDossier1.getData().getZustaendigArzt().getId()).isEqualTo(arztMpa2.getId().getId());
        assertThat(updatedDossier1.getData().getZustaendigMPA().getId()).isEqualTo(arztMpa2.getId().getId());
        assertThat(updatedDossier2.getData().getZustaendigArzt().getId()).isEqualTo(arztMpa2.getId().getId());
        assertThat(updatedDossier2.getData().getZustaendigMPA().getId()).isEqualTo(arztMpa2.getId().getId());

    }
}
