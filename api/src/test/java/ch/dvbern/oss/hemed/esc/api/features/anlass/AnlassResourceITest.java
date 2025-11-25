/*
 *
 * Copyright (C) 2025 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.hemed.esc.api.features.anlass;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.UUID;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfigService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson.Lehrperson;
import ch.dvbern.oss.hemed.esc.api.shared.aktivitaet.AktivitaetHistoryService;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.types.DateTimeRange;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.assertj.AppValidationErrorJsonSpecAssert;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.AnlassBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.BenutzerBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.AnlassApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AnlassJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AnlassListJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AnlassSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AnlassStatusSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AnlassTypSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AppValidationErrorJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.BenutzerOptionJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.SchulInformationenListJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
@RequiredArgsConstructor
class AnlassResourceITest extends AbstractITest {

    private final AnlassApiSpec api = AnlassApiSpec.anlass(RequestSpecUtil.quarkusSpec());

    private final AnlassService anlassService;
    private final AnlassMapper anlassMapper;
    private final AktivitaetHistoryService aktivitaetHistoryService;
    private final AnlassConfigService anlassConfigService;
    private final BenutzerService benutzerService;
    private final FixtureService fixtureService;
    private final AppClock appClock;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    @TestAsAdminBS
    void getKlassenLehrpersonenForAnlass() {
        // ARRANGE
        String expectedSchuleBezeichung = "Leonhard (SEK) Theodor";
        String expectedKlasseBezeichnung = "Klasse 4c";
        String expectedName = "Schulkontakterin";
        String expectedVorname = "Klara";
        Anlass anlass = fixtureService.createAnlass();
        Klasse klasse = fixtureService.createKlasse();
        Schule schule = klasse.getSchule();
        Lehrperson lehrperson = fixtureService.createLehrperson(klasse);
        Kind kind = fixtureService.createKind(klasse);
        Dossier dossier = fixtureService.createDossier(kind);
        fixtureService.createEinladung(anlass, dossier);

        // ACT
        SchulInformationenListJsonSpec result = api.anlassResourceGetSchulInformationenForAnlass()
            .anlassIdPath(anlass.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(SchulInformationenListJsonSpec.class);

        // ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getSchulInformationenList()).isNotEmpty().hasSize(1);
        assertThat(result.getSchulInformationenList()
            .getFirst()
            .getKlasseLehrpersonenOptions()
        ).hasSize(1);
        assertThat(result.getSchulInformationenList()
            .getFirst()
            .getKlasseLehrpersonenOptions()
            .getFirst()
            .getLehrpersonOptions()).isNotEmpty().hasSize(1);
        assertThat(result.getSchulInformationenList()
            .getFirst()
            .getSchuleOptionJson()
            .getId()).isEqualTo(schule.getId().getId());
        assertThat(result.getSchulInformationenList()
            .getFirst()
            .getSchuleOptionJson()
            .getBezeichnung()).isEqualTo(expectedSchuleBezeichung);
        assertThat(result.getSchulInformationenList()
            .getFirst()
            .getKlasseLehrpersonenOptions()
            .getFirst()
            .getKlasseOptionJson()
            .getId()).isEqualTo(klasse.getId().getId());
        assertThat(result.getSchulInformationenList()
            .getFirst()
            .getKlasseLehrpersonenOptions()
            .getFirst()
            .getKlasseOptionJson()
            .getBezeichnung()).isEqualTo(
            expectedKlasseBezeichnung);
        assertThat(result.getSchulInformationenList()
            .getFirst()
            .getKlasseLehrpersonenOptions()
            .getFirst()
            .getLehrpersonOptions()
            .getFirst()
            .getId()).isEqualTo(lehrperson.getId().getId());
        assertThat(result.getSchulInformationenList()
            .getFirst()
            .getKlasseLehrpersonenOptions()
            .getFirst()
            .getLehrpersonOptions()
            .getFirst()
            .getName()).isEqualTo(expectedName);
        assertThat(result.getSchulInformationenList()
            .getFirst()
            .getKlasseLehrpersonenOptions()
            .getFirst()
            .getLehrpersonOptions()
            .getFirst()
            .getVorname()).isEqualTo(expectedVorname);
    }

    @Test
    @TestAsAdminBS
    void createAnlass() {
        // ARRANGE
        AnlassConfig config =
            anlassConfigService.getByAnlassTypAndStichtag(AnlassTyp.KLASSE_8, appClock.getToday().plusMonths(1));
        AnlassSaveJsonSpec saveAnlassJson = AnlassBuilder.createJaxSpec(builder -> builder
            .withAnlassTyp(AnlassTyp.KLASSE_8)
            .withAnlassConfig(config)
            .withTitle("Mein Anlass")).getData();

        // ACT
        AnlassJsonSpec result = api
            .anlassResourceCreateAnlass()
            .body(saveAnlassJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().as(AnlassJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isNotNull();
        Assertions.assertThat(result.getData().getTitel()).isEqualTo("Mein Anlass");
        Assertions.assertThat(result.getAnlassConfig().getData().getAnlassTyp()).isEqualTo(AnlassTypSpec.KLASSE_8);
    }

    @Test
    @TestAsAdminBS
    void createAnlass_with_benutzer() {
        // ARRANGE
        var createDto = BenutzerBuilder.createCreateDTO(
            builder -> builder
                .withRandomEmail("viktor.verantworltich@example.com")
                .withRandomVorname("Viktor")
                .withRandomNachname("Verantwortlich")
                .withKeycloakId(UUID.randomUUID().toString())
        );
        Benutzer benutzer = benutzerService.getById(benutzerService.createBenutzer(createDto));

        AnlassConfig config =
            anlassConfigService.getByAnlassTypAndStichtag(AnlassTyp.KLASSE_8, appClock.getToday().plusMonths(1));
        AnlassSaveJsonSpec saveAnlassJson = AnlassBuilder.createJaxSpec(builder -> builder
            .withAnlassTyp(AnlassTyp.KLASSE_8)
            .withAnlassConfig(config)
            .withArzt(benutzer)
            .withMPA(benutzer)
            .withWeiterePerson(benutzer)
            .withTitle("Mein Anlass")).getData();

        // ACT
        AnlassJsonSpec result = api
            .anlassResourceCreateAnlass()
            .body(saveAnlassJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().as(AnlassJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isNotNull();
        Assertions.assertThat(result.getData().getTitel()).isEqualTo("Mein Anlass");
        Assertions.assertThat(result.getAnlassConfig().getData().getAnlassTyp()).isEqualTo(AnlassTypSpec.KLASSE_8);
        Assertions.assertThat(result.getData().getArzt())
            .isNotNull()
            .extracting(BenutzerOptionJsonSpec::getId).isEqualTo(benutzer.getId().getId());
        Assertions.assertThat(result.getData().getMpa())
            .isNotNull()
            .extracting(BenutzerOptionJsonSpec::getId).isEqualTo(benutzer.getId().getId());
        Assertions.assertThat(result.getData().getWeiterePerson())
            .isNotNull()
            .extracting(BenutzerOptionJsonSpec::getId).isEqualTo(benutzer.getId().getId());
    }

    @Test
    @TestAsAdminBS
    void updateAnlass() {
        // ARRANGE
        AnlassConfig config = anlassConfigService.getByAnlassTypAndStichtag(
            AnlassTyp.KLASSE_8,
            appClock.offsetDateTime().now().toLocalDate());
        final Anlass existingAnlass = anlassService.persist(
            AnlassBuilder.create(builder -> builder
                .withTitle("Mein Anlass")
                .withAnlassConfig(config))
        );
        AnlassJsonSpec updatedAnlassJson = AnlassBuilder.createJaxSpec(builder -> builder
            .forAnlass(existingAnlass)
            .withTitle("Mein neuer Anlass"));

        // ACT
        AnlassJsonSpec result = api
            .anlassResourceUpdateAnlass()
            .anlassIdPath(existingAnlass.getId().getId())
            .body(updatedAnlassJson.getData())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(AnlassJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isEqualTo(existingAnlass.getId().getId());
        Assertions.assertThat(result.getData().getTitel()).isEqualTo("Mein neuer Anlass");
    }

    @Test
    @TestAsAdminBS
    void changeAnlassStatus() {
        // ARRANGE
        Anlass anlass = fixtureService.createAnlass();
        Assertions.assertThat(anlass.getAnlassStatus()).isEqualTo(AnlassStatus.IN_PLANUNG);

        // ACT
        AnlassJsonSpec result = api
            .anlassResourceChangeAnlassStatus()
            .anlassIdPath(anlass.getId().getId())
            .statusPath(AnlassStatusSpec.DURCHGEFUEHRT)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(AnlassJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isEqualTo(anlass.getId().getId());
        Assertions.assertThat(result.getData().getAnlassStatus()).isEqualTo(AnlassStatusSpec.DURCHGEFUEHRT);
    }

    @Test
    @TestAsAdminBS
    void updateAnlass_with_benutzer() {
        // ARRANGE
        AnlassConfig config = anlassConfigService.getByAnlassTypAndStichtag(
            AnlassTyp.KLASSE_8,
            appClock.offsetDateTime().now().toLocalDate());
        final Anlass existingAnlass = anlassService.persist(
            AnlassBuilder.create(builder -> builder
                .withTitle("Mein Anlass")
                .withAnlassConfig(config))
        );

        var createDto = BenutzerBuilder.createCreateDTO(
            builder -> builder
                .withRandomEmail("viktor.verantworltich@example.com")
                .withRandomVorname("Viktor")
                .withRandomNachname("Verantwortlich")
                .withKeycloakId(UUID.randomUUID().toString())
        );
        Benutzer benutzer = benutzerService.getById(benutzerService.createBenutzer(createDto));

        AnlassJsonSpec updatedAnlassJson = AnlassBuilder.createJaxSpec(builder -> builder
            .forAnlass(existingAnlass)
            .withMPA(benutzer)
            .withWeiterePerson(benutzer)
            .withArzt(benutzer)
            .withTitle("Mein neuer Anlass"));

        // ACT
        AnlassJsonSpec result = api
            .anlassResourceUpdateAnlass()
            .anlassIdPath(existingAnlass.getId().getId())
            .body(updatedAnlassJson.getData())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(AnlassJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isEqualTo(existingAnlass.getId().getId());
        Assertions.assertThat(result.getData().getTitel()).isEqualTo("Mein neuer Anlass");
        Assertions.assertThat(result.getData().getArzt())
            .isNotNull()
            .extracting(BenutzerOptionJsonSpec::getId).isEqualTo(benutzer.getId().getId());
        Assertions.assertThat(result.getData().getMpa())
            .isNotNull()
            .extracting(BenutzerOptionJsonSpec::getId).isEqualTo(benutzer.getId().getId());
        Assertions.assertThat(result.getData().getWeiterePerson())
            .isNotNull()
            .extracting(BenutzerOptionJsonSpec::getId).isEqualTo(benutzer.getId().getId());
    }

    @Test
    @TestAsAdminBS
    void updateAnlass_datum_ausserhalb_config_should_succeed() {
        // ARRANGE
        AnlassConfig config = anlassConfigService.getByAnlassTypAndStichtag(
            AnlassTyp.KLASSE_8,
            appClock.offsetDateTime().now().toLocalDate());
        final Anlass existingAnlass = anlassService.persist(
            AnlassBuilder.create(builder -> builder
                .withTitle("Mein Anlass")
                .withAnlassConfig(config))
        );
        Assertions.assertThat(config.getGueltigkeitsZeitraum().getDatumVon())
            .isEqualTo(LocalDate.of(2000, Month.JANUARY, 1));
        Assertions.assertThat(config.getGueltigkeitsZeitraum().getDatumBis())
            .isEqualTo(LocalDate.of(9999, Month.DECEMBER, 31));

        // ACT
        LocalDate datumNeu = LocalDate.of(1998, Month.DECEMBER, 31);
        AnlassJsonSpec updatedAnlassJson = AnlassBuilder.createJaxSpec(builder -> builder
            .forAnlass(existingAnlass)
            .withDatumSetSchuljahr(datumNeu));
        AnlassJsonSpec result = api
            .anlassResourceUpdateAnlass()
            .anlassIdPath(existingAnlass.getId().getId())
            .body(updatedAnlassJson.getData())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(AnlassJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isEqualTo(existingAnlass.getId().getId());
        Assertions.assertThat(result.getData().getDatum()).isEqualTo(datumNeu);
        Assertions.assertThat(result.getAnlassConfig().getId()).isEqualTo(config.getId().getId());
    }

    @Test
    @TestAsAdminBS
    void removeAnlass() {
        // ARRANGE
        AnlassConfig config = anlassConfigService.getByAnlassTypAndStichtag(
            AnlassTyp.KLASSE_8,
            appClock.offsetDateTime().now().toLocalDate());
        final Anlass anlassToDelete = anlassService.persist(
            AnlassBuilder.create(builder -> builder
                .withAnlassConfig(config))
        );

        var anlassWithHistEntry =
            anlassMapper.getById(anlassToDelete.getId()); // this creates an aktivitaet history entry
        var anlassHistory = aktivitaetHistoryService.findAllAnlassAktivitaetenForBenutzer(ID.of(
            UUID.fromString(TestAsAdminBS.BENUTZER_ID),
            Benutzer.class));
        Assertions.assertThat(anlassHistory)
            .anyMatch(hist -> hist.getAnlass().getId().equals(anlassWithHistEntry.getId()));

        // ACT
        ID<Anlass> anlassID = anlassToDelete.getId();
        api.anlassResourceRemoveAnlass()
            .anlassIdPath(anlassID.getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());

        // ASSERT
        // Verify the Anlass no longer exists by attempting to fetch it
        AppFailureException ex = assertThrows(
            AppFailureException.class,
            () -> anlassService.getById(anlassID)
        );
        assertThat(ex)
            .hasMessageContaining("ENTITY_NOT_FOUND");
    }

    @Test
    @TestAsAdminBS
    void removeAnlassWithEinladungAndUntersuchung_should_fail() {
        // ARRANGE
        var untersuchung = fixtureService.createUntersuchungWithDurchfuehrung();
        Anlass anlassToDelete = untersuchung.getEinladung().getAnlass();
        // ACT
        ID<Anlass> anlassID = anlassToDelete.getId();
        AppValidationErrorJsonSpec validationError = api.anlassResourceRemoveAnlass()
            .anlassIdPath(anlassID.getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract().as(AppValidationErrorJsonSpec.class);
        // Assert

        AppValidationErrorJsonSpecAssert.assertThatAppValidationErrorJsonSpec(validationError)
            .extractingExactlyOneViolation()
            .hasRawMessageContaining("solange noch Kinder eingeladen sind");

    }

    @Test
    @TestAsAdminBS
    void findAllAnlaesse() {
        // ARRANGE
        AnlassConfig config = anlassConfigService.getByAnlassTypAndStichtag(
            AnlassTyp.KLASSE_8,
            appClock.offsetDateTime().now().toLocalDate());
        final Anlass anlass = anlassService.persist(
            AnlassBuilder.create(builder -> builder
                .withTitle("Test findAllAnlasse")
                .withAnlassConfig(config))
        );
        // ACT
        AnlassListJsonSpec result = api
            .anlassResourceFindAllAnlaesse()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(AnlassListJsonSpec.class);
        // ASSERT
        Assertions.assertThat(result.getAnlassList())
            .isNotEmpty()
            .anyMatch(jsonSpec -> anlass.getId().getId().equals(jsonSpec.getId())
                && "Test findAllAnlasse".equals(jsonSpec.getData().getTitel()));
    }

    @Test
    @TestAsAdminBS
    void getAnlass() {
        // ARRANGE
        AnlassConfig config = anlassConfigService.getByAnlassTypAndStichtag(
            AnlassTyp.KLASSE_8,
            appClock.offsetDateTime().now().toLocalDate());
        final Anlass anlass = anlassService.persist(
            AnlassBuilder.create(builder -> builder
                .withTitle("Test getAnlass")
                .withAnlassConfig(config))
        );
        // ACT
        AnlassJsonSpec result = api
            .anlassResourceGetAnlass()
            .anlassIdPath(anlass.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(AnlassJsonSpec.class);
        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isEqualTo(anlass.getId().getId());
        Assertions.assertThat(result.getData().getTitel()).isEqualTo("Test getAnlass");
    }

    @Test
    @TestAsAdminBS
    void createAnlassWithDateTimeRange() {
        // ARRANGE
        LocalDate now = appClock.offsetDateTime().now().toLocalDate().plusMonths(1);
        LocalDateTime zeitVon = LocalDateTime.of(now, LocalTime.of(12, 30));
        LocalDateTime zeitBis = LocalDateTime.of(now, LocalTime.of(16, 0));

        AnlassConfig config =
            anlassConfigService.getByAnlassTypAndStichtag(AnlassTyp.KLASSE_8, zeitVon.toLocalDate());
        AnlassSaveJsonSpec saveAnlassJson = AnlassBuilder.createJaxSpec(builder -> builder
            .withAnlassTyp(AnlassTyp.KLASSE_8)
            .withAnlassConfig(config)
            .withTitle("Mein Anlass")
            .withZeitVonBis(new DateTimeRange(
                zeitVon,
                zeitBis
            ))).getData();

        // ACT
        AnlassJsonSpec result = api
            .anlassResourceCreateAnlass()
            .body(saveAnlassJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().as(AnlassJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isNotNull();
        Assertions.assertThat(result.getData().getTitel()).isEqualTo("Mein Anlass");
        Assertions.assertThat(result.getAnlassConfig().getData().getAnlassTyp()).isEqualTo(AnlassTypSpec.KLASSE_8);
        Assertions.assertThat(result.getData().getZeitVon()).isEqualTo(zeitVon);
        Assertions.assertThat(result.getData().getZeitBis()).isEqualTo(zeitBis);
    }

    @Test
    @TestAsAdminBS
    void createAnlassWithDateTimeRange_Date_is_updated_on_safe() {
        // ARRANGE
        LocalDate now = appClock.offsetDateTime().now().toLocalDate().plusMonths(1);
        LocalDateTime zeitVon = LocalDateTime.of(2002, 12, 1, 12, 30);
        LocalDateTime zeitBis = LocalDateTime.of(2002, 12, 1, 16, 0);

        AnlassConfig config =
            anlassConfigService.getByAnlassTypAndStichtag(AnlassTyp.KLASSE_8, zeitVon.toLocalDate());
        AnlassSaveJsonSpec saveAnlassJson = AnlassBuilder.createJaxSpec(builder -> builder
            .withAnlassTyp(AnlassTyp.KLASSE_8)
            .withAnlassConfig(config)
            .withTitle("Mein Anlass")
            .withZeitVonBis(new DateTimeRange(
                zeitVon,
                zeitBis
            ))).getData();

        // ACT
        AnlassJsonSpec result = api
            .anlassResourceCreateAnlass()
            .body(saveAnlassJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.CREATED.getStatusCode())
            .extract().as(AnlassJsonSpec.class);

        // ASSERT
        LocalDateTime expectedTimeVon = LocalDateTime.of(now, LocalTime.of(12, 30));
        LocalDateTime expectedTimeBis = LocalDateTime.of(now, LocalTime.of(16, 0));

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getId()).isNotNull();
        Assertions.assertThat(result.getData().getTitel()).isEqualTo("Mein Anlass");
        Assertions.assertThat(result.getAnlassConfig().getData().getAnlassTyp()).isEqualTo(AnlassTypSpec.KLASSE_8);
        Assertions.assertThat(result.getData().getZeitVon()).isEqualTo(expectedTimeVon);
        Assertions.assertThat(result.getData().getZeitBis()).isEqualTo(expectedTimeBis);
    }

    @Test
    @TestAsAdminBS
    void findAllAnlaesse_for_user() {
        // ARRANGE
        var createArztDto = BenutzerBuilder.createCreateDTO(
            builder -> builder
                .withRandomEmail("jhon.smith@example.com")
                .withRandomVorname("Jhon")
                .withRandomNachname("Smith")
                .withKeycloakId(UUID.randomUUID().toString())
        );
        Benutzer benutzer = benutzerService.getById(benutzerService.createBenutzer(createArztDto));

        LocalDate date = appClock.offsetDateTime().now().toLocalDate().plusMonths(1);

        AnlassConfig config = anlassConfigService.getByAnlassTypAndStichtag(
            AnlassTyp.KLASSE_8,
            appClock.offsetDateTime().now().toLocalDate());
        anlassService.persist(
            AnlassBuilder.create(builder -> builder
                .withTitle("Test findAllForUser with Arzt")
                .withArzt(benutzer)
                .withDatumSetSchuljahr(date)
                .withAnlassConfig(config))
        );

        anlassService.persist(
            AnlassBuilder.create(builder -> builder
                .withTitle("Test findAllForUser with MPA")
                .withMPA(benutzer)
                .withDatumSetSchuljahr(date)
                .withAnlassConfig(config))
        );

        anlassService.persist(
            AnlassBuilder.create(builder -> builder
                .withTitle("Test findAllForUser with WeiterePerson")
                .withWeiterePerson(benutzer)
                .withDatumSetSchuljahr(date)
                .withAnlassConfig(config))
        );

        anlassService.persist(
            AnlassBuilder.create(builder -> builder
                .withTitle("Test findAllForUser")
                .withDatumSetSchuljahr(date)
                .withAnlassConfig(config))
        );

        anlassService.persist(
            AnlassBuilder.create(builder -> builder
                .withTitle("Test findAllForUser with datum in the past")
                .withWeiterePerson(benutzer)
                .withDatumSetSchuljahr(date.minusMonths(2))
                .withAnlassConfig(config))
        );

        // ACT
        AnlassListJsonSpec result = api
            .anlassResourceFindAllForUser()
            .benutzerIDPath(benutzer.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(AnlassListJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result.getAnlassList())
            .isNotEmpty()
            .anyMatch(jsonSpec -> jsonSpec.getData().getArzt() != null && benutzer.getId()
                .getId()
                .equals(jsonSpec.getData().getArzt().getId()));
        Assertions.assertThat(result.getAnlassList())
            .anyMatch(jsonSpec -> jsonSpec.getData().getMpa() != null && benutzer.getId()
                .getId()
                .equals(jsonSpec.getData().getMpa().getId()));
        Assertions.assertThat(result.getAnlassList())
            .anyMatch(jsonSpec -> jsonSpec.getData().getWeiterePerson() != null && benutzer.getId()
                .getId()
                .equals(jsonSpec.getData().getWeiterePerson().getId()));
        Assertions.assertThat(result.getTotalRecords()).isEqualTo(3);
    }
}
