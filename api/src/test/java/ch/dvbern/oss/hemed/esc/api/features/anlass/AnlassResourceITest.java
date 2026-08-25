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
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.shared.clock.AppClock;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfigService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassArt;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassStatus;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.features.schule.lehrperson.Lehrperson;
import ch.dvbern.oss.hemed.esc.api.shared.aktivitaet.AktivitaetHistoryService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.types.DateTimeRange;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.assertj.AppValidationErrorJsonSpecAssert;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.AnlassBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBE;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsMpaBS;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSchularztBE;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSchularztBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.AnlassApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AnlassJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AnlassListJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AnlassSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AnlassStatusSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AnlassTypSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AnlassWithDateTimeJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AnlassWithDateTimeListJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AnlassWithOpenSlotsListJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AnlassWithOpenSlotsOptionJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AppValidationErrorJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.BenutzerOptionJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.SchulInformationenListJsonSpec;
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
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
@QuarkusTest
@RequiredArgsConstructor
class AnlassResourceITest extends AbstractITest {

    private final AnlassApiSpec api = AnlassApiSpec.anlass(RequestSpecUtil.quarkusSpec());

    private final AnlassService anlassService;
    private final AnlassMapper anlassMapper;
    private final AktivitaetHistoryService aktivitaetHistoryService;
    private final AnlassConfigService anlassConfigService;
    private final FixtureService fixtureService;
    private final AppClock appClock;
    private final TenantProducer tenantProducer;

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
        Benutzer benutzer = fixtureService.createBenutzerFuerZentraleOrganisation();

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
    @TestAsAdminBE
    void createAnlass_without_titel_featureDisabled_succeeds() {
        // ARRANGE
        tenantProducer.setCurrentTenant(TenantEnum.BE);
        Benutzer arzt = fixtureService.readBenutzerWithBerechtigungen(TestAsSchularztBE.BENUTZER_ID_ID);
        AnlassConfig config =
            anlassConfigService.getByAnlassTypAndStichtag(AnlassTyp.KLASSE_8, appClock.getToday().plusMonths(1));
        AnlassSaveJsonSpec saveAnlassJson = AnlassBuilder.createJaxSpec(builder -> builder
            .withAnlassTyp(AnlassTyp.KLASSE_8)
            .withAnlassConfig(config)
            .withArzt(arzt)
            .withTitle(null)).getData();

        // ACT + ASSERT
        api
            .anlassResourceCreateAnlass()
            .body(saveAnlassJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.CREATED.getStatusCode());
    }

    @Test
    @TestAsAdminBS
    void createAnlass_without_titel_featureEnabled_fails() {
        // ARRANGE
        AnlassConfig config =
            anlassConfigService.getByAnlassTypAndStichtag(AnlassTyp.KLASSE_8, appClock.getToday().plusMonths(1));
        AnlassSaveJsonSpec saveAnlassJson = AnlassBuilder.createJaxSpec(builder -> builder
            .withAnlassTyp(AnlassTyp.KLASSE_8)
            .withAnlassConfig(config)
            .withTitle(null)).getData();

        // ACT
        AppValidationErrorJsonSpec validationError = api
            .anlassResourceCreateAnlass()
            .body(saveAnlassJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract().as(AppValidationErrorJsonSpec.class);

        // ASSERT
        AppValidationErrorJsonSpecAssert.assertThatAppValidationErrorJsonSpec(validationError)
            .extractingExactlyOneViolation()
            .hasRawMessageContaining("Der Titel des Anlasses muss gesetzt sein");
    }

    @Test
    @TestAsAdminBE
    void updateAnlass_without_titel_featureDisabled_succeeds() {
        // ARRANGE
        tenantProducer.setCurrentTenant(TenantEnum.BE);
        Benutzer arzt = fixtureService.readBenutzerWithBerechtigungen(TestAsSchularztBE.BENUTZER_ID_ID);
        AnlassConfig config = anlassConfigService.getByAnlassTypAndStichtag(
            AnlassTyp.KLASSE_8,
            appClock.offsetDateTime().now().toLocalDate());
        final Anlass existingAnlass = anlassService.persist(
            AnlassBuilder.create(builder -> builder
                .withTitle("Mein Anlass")
                .withAnlassConfig(config)
                .withArzt(arzt))
        );
        AnlassJsonSpec updatedAnlassJson = AnlassBuilder.createJaxSpec(builder -> builder
            .forAnlass(existingAnlass)
            .withTitle(null));

        // ACT + ASSERT
        api
            .anlassResourceUpdateAnlass()
            .anlassIdPath(existingAnlass.getId().getId())
            .body(updatedAnlassJson.getData())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());
    }

    @Test
    @TestAsAdminBS
    void updateAnlass_without_titel_featureEnabled_fails() {
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
            .withTitle(null));

        // ACT
        AppValidationErrorJsonSpec validationError = api
            .anlassResourceUpdateAnlass()
            .anlassIdPath(existingAnlass.getId().getId())
            .body(updatedAnlassJson.getData())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract().as(AppValidationErrorJsonSpec.class);

        // ASSERT
        AppValidationErrorJsonSpecAssert.assertThatAppValidationErrorJsonSpec(validationError)
            .extractingExactlyOneViolation()
            .hasRawMessageContaining("Der Titel des Anlasses muss gesetzt sein");
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

        Benutzer benutzer = fixtureService.createBenutzerFuerZentraleOrganisation();
        Benutzer arzt = fixtureService.readBenutzerWithBerechtigungen(TestAsSchularztBS.BENUTZER_ID_ID);
        Benutzer mpa = fixtureService.readBenutzerWithBerechtigungen(TestAsMpaBS.BENUTZER_ID_ID);

        AnlassJsonSpec updatedAnlassJson = AnlassBuilder.createJaxSpec(builder -> builder
            .forAnlass(existingAnlass)
            .withMPA(mpa)
            .withWeiterePerson(benutzer)
            .withArzt(arzt)
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
            .extracting(BenutzerOptionJsonSpec::getId).isEqualTo(arzt.getId().getId());
        Assertions.assertThat(result.getData().getMpa())
            .isNotNull()
            .extracting(BenutzerOptionJsonSpec::getId).isEqualTo(mpa.getId().getId());
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
        Anlass anlassToDelete = untersuchung.getAnlass();
        // ACT
        ID<Anlass> anlassID = anlassToDelete.getId();
        AppValidationErrorJsonSpec validationError = api.anlassResourceRemoveAnlass()
            .anlassIdPath(anlassID.getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract().as(AppValidationErrorJsonSpec.class);
        // ASSERT

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
            .anlassResourceFindAllAnlaesseBerechtigt()
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
        Benutzer benutzer = fixtureService.createBenutzerFuerZentraleOrganisation();

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
            .anlassResourceFindAnlaesseForDashboard()
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

    @Test
    @TestAsAdminBS
    void findAnlaesseForDashboard_sortedByDatumThenZeitVon() {
        // ARRANGE
        Benutzer benutzer = fixtureService.createBenutzerFuerZentraleOrganisation();

        LocalDate firstDate = appClock.offsetDateTime().now().toLocalDate().plusMonths(1);
        LocalDate secondDate = firstDate.plusDays(11);
        LocalDate thirdDate = firstDate.plusDays(19);

        AnlassConfig config = anlassConfigService.getByAnlassTypAndStichtag(
            AnlassTyp.KLASSE_8,
            appClock.offsetDateTime().now().toLocalDate());

        // persisted in an order which is neither sorted by Datum nor by Zeit
        persistAnlassForDashboard(benutzer, config, secondDate, LocalTime.of(11, 0), LocalTime.of(12, 0));
        persistAnlassForDashboard(benutzer, config, firstDate, LocalTime.of(14, 0), LocalTime.of(15, 0));
        persistAnlassForDashboard(benutzer, config, thirdDate, LocalTime.of(10, 0), LocalTime.of(10, 30));
        persistAnlassForDashboard(benutzer, config, firstDate, LocalTime.of(11, 30), LocalTime.of(12, 30));
        persistAnlassForDashboard(benutzer, config, firstDate, LocalTime.of(11, 0), LocalTime.of(12, 0));

        // ACT
        AnlassListJsonSpec result = api
            .anlassResourceFindAnlaesseForDashboard()
            .benutzerIDPath(benutzer.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().as(AnlassListJsonSpec.class);

        // ASSERT
        assertThat(result.getAnlassList())
            .extracting(anlass -> anlass.getData().getDatum(), anlass -> anlass.getData().getZeitVon())
            .containsExactly(
                tuple(firstDate, firstDate.atTime(11, 0)),
                tuple(firstDate, firstDate.atTime(11, 30)),
                tuple(firstDate, firstDate.atTime(14, 0)),
                tuple(secondDate, secondDate.atTime(11, 0)),
                tuple(thirdDate, thirdDate.atTime(10, 0))
            );
    }

    private void persistAnlassForDashboard(
        @NonNull Benutzer benutzer,
        @NonNull AnlassConfig config,
        @NonNull LocalDate datum,
        @NonNull LocalTime zeitVon,
        @NonNull LocalTime zeitBis
    ) {
        anlassService.persist(
            AnlassBuilder.create(builder -> builder
                .withTitle("Test Dashboard Sortierung " + datum + " " + zeitVon)
                .withArzt(benutzer)
                .withDatumSetSchuljahr(datum)
                .withZeitVonBis(DateTimeRange.of(datum.atTime(zeitVon), datum.atTime(zeitBis)))
                .withAnlassConfig(config))
        );
    }

    @Test
    @TestAsAdminBS
    @SuppressWarnings("java:S117" /* Leave for readability */)
    void findAnlaesseWithAvailableSlots() {
        // ARRANGE

        // Base Data To create Anlass
        AnlassConfig configImpftermin = anlassConfigService.getByAnlassTypAndStichtag(
            AnlassTyp.IMPFTERMIN,
            appClock.offsetDateTime().now().toLocalDate());
        AnlassConfig configImpfaktion = anlassConfigService.getByAnlassTypAndStichtag(
            AnlassTyp.IMPFAKTION,
            appClock.offsetDateTime().now().toLocalDate());
        AnlassConfig configKlasse8 = anlassConfigService.getByAnlassTypAndStichtag(
            AnlassTyp.KLASSE_8,
            appClock.offsetDateTime().now().toLocalDate());
        Klasse klasse = fixtureService.createKlasse();
        List<Dossier> kinderDossiers = IntStream.range(0, 20)
            .mapToObj(i -> fixtureService.createDossier(fixtureService.createKind(klasse)))
            .toList();

        Dossier searchForKinderWithDossier = fixtureService.createDossier(fixtureService.createKind(klasse));

        // Anlaesse should be returned
        // less than 16 kinder eingeladen
        Anlass anlass1 = anlassService.persist(AnlassBuilder.create(builder -> builder.withTitle("Impftermin 1")
            .withAnlassConfig(configImpftermin)));
        IntStream.range(0, 15).forEach(i -> fixtureService.createEinladung(anlass1, kinderDossiers.get(i)));

        Anlass anlass2 = anlassService.persist(AnlassBuilder.create(builder -> builder.withTitle("Impftermin 2")
            .withAnlassConfig(configImpftermin)));
        IntStream.range(0, 5).forEach(i -> fixtureService.createEinladung(anlass2, kinderDossiers.get(i)));

        // no kinder eingeladen
        Anlass anlass3 = anlassService.persist(AnlassBuilder.create(builder -> builder.withTitle("Impftermin 3")
            .withAnlassConfig(configImpftermin)));

        // Anlass scheduled later today
        LocalDateTime anlassLaterToday = appClock.offsetDateTime().now().toLocalDateTime().plusHours(1);
        Anlass anlass4 = anlassService.persist(AnlassBuilder.create(
            b -> b.withTitle("Impftermin 4").withAnlassConfig(configImpftermin)
                .withDatumSetSchuljahr(appClock.offsetDateTime().now().toLocalDate())
                .withZeitVonBis(
                    new DateTimeRange(anlassLaterToday, anlassLaterToday.plusHours(2))
                )
        ));

        // Anlass scheduled tomorrow
        Anlass anlass5 = anlassService.persist(AnlassBuilder.create(
            b -> b.withTitle("Impftermin 5")
                .withAnlassConfig(configImpftermin)
                .withDatumSetSchuljahr(appClock.offsetDateTime().now().toLocalDate().plusDays(1)))
        );

        // anlaesse with status pending
        Anlass anlass6 = anlassService.persist(AnlassBuilder.create(
            b -> b.withTitle("Impftermin 6")
                .withAnlassConfig(configImpftermin)
                .withAnlassStatus(AnlassStatus.IN_PLANUNG)
        ));
        Anlass anlass7 = anlassService.persist(AnlassBuilder.create(
            b -> b.withTitle("Impftermin 7")
                .withAnlassConfig(configImpftermin)
                .withAnlassStatus(AnlassStatus.AUFGEBOTEN)
        ));
        Anlass anlass8 = anlassService.persist(AnlassBuilder.create(
            b -> b.withTitle("Impftermin 8").withAnlassConfig(configImpftermin).withAnlassStatus(AnlassStatus.BEREIT)
        ));

        // different anlasstype included in request
        Anlass anlass9 = anlassService.persist(AnlassBuilder.create(
            b -> b.withTitle("Impfaktion 9").withAnlassConfig(configImpfaktion).withAnlassStatus(AnlassStatus.BEREIT)
        ));

        // Anlaesse should be filtered out
        // anlass with 16 or more kinder eingeladen
        Anlass anlass2_1 = anlassService.persist(AnlassBuilder.create(builder -> builder.withTitle("Impftermin 2.1")
            .withAnlassConfig(configImpftermin)));
        IntStream.range(0, 16).forEach(i -> fixtureService.createEinladung(anlass2_1, kinderDossiers.get(i)));

        // anlaesse with status finished or closed
        anlassService.persist(AnlassBuilder.create(
            b -> b.withTitle("Impftermin 2.2")
                .withAnlassConfig(configImpftermin)
                .withAnlassStatus(AnlassStatus.DURCHGEFUEHRT)
        ));
        anlassService.persist(AnlassBuilder.create(
            b -> b.withTitle("Impftermin 2.3")
                .withAnlassConfig(configImpftermin)
                .withAnlassStatus(AnlassStatus.ABGESAGT)
        ));
        anlassService.persist(AnlassBuilder.create(
            b -> b.withTitle("Impftermin 2.4")
                .withAnlassConfig(configImpftermin)
                .withAnlassStatus(AnlassStatus.ABGESCHLOSSEN)
        ));

        anlassService.persist(AnlassBuilder.create(
            b -> b.withTitle("Impftermin 2.5")
                .withAnlassConfig(configImpftermin)
                .withDatumSetSchuljahr(appClock.offsetDateTime().now().toLocalDate())
        ));

        anlassService.persist(AnlassBuilder.create(
            b -> b.withTitle("Impftermin 2.6")
                .withAnlassConfig(configImpftermin)
                .withDatumSetSchuljahr(appClock.offsetDateTime().now().toLocalDate().minusDays(1))
        ));

        // Anlass today without time von
        anlassService.persist(AnlassBuilder.create(
            b -> b.withTitle("Impftermin 2.7").withAnlassConfig(configImpftermin)
                .withDatumSetSchuljahr(appClock.offsetDateTime().now().toLocalDate())
        ));

        // Anlass today already started
        LocalDateTime anlassStartedTime = appClock.offsetDateTime().now().toLocalDateTime().plusMinutes(59);
        anlassService.persist(AnlassBuilder.create(
            b -> b.withTitle("Impftermin 2.8").withAnlassConfig(configImpftermin)
                .withDatumSetSchuljahr(appClock.offsetDateTime().now().toLocalDate())
                .withZeitVonBis(
                    new DateTimeRange(anlassStartedTime, anlassStartedTime.plusHours(2))
                )
        ));

        // Anlass was yesterday
        anlassService.persist(AnlassBuilder.create(
            b -> b.withTitle("Impftermin 2.9")
                .withAnlassConfig(configImpftermin)
                .withDatumSetSchuljahr(appClock.offsetDateTime().now().toLocalDate().minusDays(1))
        ));

        // Anlass with different type
        anlassService.persist(AnlassBuilder.create(
            b -> b.withTitle("Impftermin 2.10")
                .withAnlassConfig(configKlasse8)
                .withDatumSetSchuljahr(appClock.offsetDateTime().now().toLocalDate().minusDays(1))
        ));

        // kind already added to anlass
        Anlass anlass2_11 = anlassService.persist(AnlassBuilder.create(builder -> builder.withTitle("Impftermin 2.11")
            .withAnlassConfig(configImpftermin)));
        fixtureService.createEinladung(anlass2_11, searchForKinderWithDossier);

        // ACT
        AnlassWithOpenSlotsListJsonSpec result =
            api.anlassResourceSearchAnlassTermineWithAvailableSlots()
                .anlassTypQuery(AnlassTyp.IMPFTERMIN.toString(), AnlassTyp.IMPFAKTION.toString())
                .kindIdQuery(searchForKinderWithDossier.getKind().getId().getUUID())
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract().as(AnlassWithOpenSlotsListJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result.getMaximalAnzahlSlots()).isEqualTo(16);

        Assertions.assertThatList(result.getAnlaesseWithOpenEinladungenSlots())
            .hasSize(9)
            .extracting(
                AnlassWithOpenSlotsOptionJsonSpec::getTitel,
                AnlassWithOpenSlotsOptionJsonSpec::getOffeneSlots,
                AnlassWithOpenSlotsOptionJsonSpec::getAnlassTyp,
                AnlassWithOpenSlotsOptionJsonSpec::getAnlassId
            )
            .containsExactlyInAnyOrderElementsOf(
                List.of(
                    tuple("Impftermin 1", 1, AnlassTypSpec.IMPFTERMIN, anlass1.getId().getUUID().toString()),
                    tuple("Impftermin 2", 11, AnlassTypSpec.IMPFTERMIN, anlass2.getId().getUUID().toString()),
                    tuple("Impftermin 3", 16, AnlassTypSpec.IMPFTERMIN, anlass3.getId().getUUID().toString()),
                    tuple("Impftermin 4", 16, AnlassTypSpec.IMPFTERMIN, anlass4.getId().getUUID().toString()),
                    tuple("Impftermin 5", 16, AnlassTypSpec.IMPFTERMIN, anlass5.getId().getUUID().toString()),
                    tuple("Impftermin 6", 16, AnlassTypSpec.IMPFTERMIN, anlass6.getId().getUUID().toString()),
                    tuple("Impftermin 7", 16, AnlassTypSpec.IMPFTERMIN, anlass7.getId().getUUID().toString()),
                    tuple("Impftermin 8", 16, AnlassTypSpec.IMPFTERMIN, anlass8.getId().getUUID().toString()),
                    tuple("Impfaktion 9", 16, AnlassTypSpec.IMPFAKTION, anlass9.getId().getUUID().toString())
                )
            );
    }

    @Test
    @TestAsAdminBS
    @SuppressWarnings("java:S117" /* Leave for readability */)
    void findScheduledAnlaesseForKind() {
        // ARRANGE
        // Base Data To create Anlass
        AnlassConfig configImpftermin = anlassConfigService.getByAnlassTypAndStichtag(
            AnlassTyp.IMPFTERMIN,
            appClock.offsetDateTime().now().toLocalDate());
        AnlassConfig configKlasse8 = anlassConfigService.getByAnlassTypAndStichtag(
            AnlassTyp.KLASSE_8,
            appClock.offsetDateTime().now().toLocalDate());
        Klasse klasse = fixtureService.createKlasse();
        Dossier dossierForSearch = fixtureService.createDossier(fixtureService.createKind(klasse));
        Dossier otherDossier = fixtureService.createDossier(fixtureService.createKind(klasse));

        // Anlaesse should be found
        Anlass anlass1 = anlassService.persist(AnlassBuilder.create(builder -> builder.withTitle("Impftermin 1")
            .withAnlassConfig(configImpftermin)));
        fixtureService.createEinladung(anlass1, dossierForSearch);

        Anlass anlass2 = anlassService.persist(AnlassBuilder.create(builder -> builder.withTitle("Impftermin 2")
            .withAnlassConfig(configImpftermin)));
        fixtureService.createEinladung(anlass2, dossierForSearch);
        fixtureService.createEinladung(anlass2, otherDossier);

        LocalDateTime anlassLaterToday = appClock.offsetDateTime().now().toLocalDateTime().plusHours(1);
        Anlass anlass3 = anlassService.persist(AnlassBuilder.create(
            b -> b.withTitle("Impftermin 3").withAnlassConfig(configImpftermin)
                .withDatumSetSchuljahr(appClock.offsetDateTime().now().toLocalDate())
                .withZeitVonBis(
                    new DateTimeRange(anlassLaterToday, anlassLaterToday.plusHours(2))
                )
        ));
        fixtureService.createEinladung(anlass3, dossierForSearch);

        // Anlass scheduled tomorrow
        Anlass anlass4 = anlassService.persist(AnlassBuilder.create(
            b -> b.withTitle("Impftermin 4")
                .withAnlassConfig(configImpftermin)
                .withDatumSetSchuljahr(appClock.offsetDateTime().now().toLocalDate().plusDays(1)))
        );
        fixtureService.createEinladung(anlass4, dossierForSearch);

        // anlaesse with status pending
        Anlass anlass5 = anlassService.persist(AnlassBuilder.create(
            b -> b.withTitle("Impftermin 5")
                .withAnlassConfig(configImpftermin)
                .withAnlassStatus(AnlassStatus.IN_PLANUNG)
        ));
        fixtureService.createEinladung(anlass5, dossierForSearch);

        Anlass anlass6 = anlassService.persist(AnlassBuilder.create(
            b -> b.withTitle("Impftermin 6")
                .withAnlassConfig(configImpftermin)
                .withAnlassStatus(AnlassStatus.AUFGEBOTEN)
        ));
        fixtureService.createEinladung(anlass6, dossierForSearch);

        Anlass anlass7 = anlassService.persist(AnlassBuilder.create(
            b -> b.withTitle("Impftermin 7").withAnlassConfig(configImpftermin).withAnlassStatus(AnlassStatus.BEREIT)
        ));
        fixtureService.createEinladung(anlass7, dossierForSearch);

        // Anlass today without time von
        Anlass anlass8 = anlassService.persist(AnlassBuilder.create(
            b -> b.withTitle("Impftermin 8").withAnlassConfig(configImpftermin)
                .withDatumSetSchuljahr(appClock.offsetDateTime().now().toLocalDate())
        ));
        fixtureService.createEinladung(anlass8, dossierForSearch);

        // Anlass today already started
        LocalDateTime anlassStartedTime = appClock.offsetDateTime().now().toLocalDateTime().minusHours(1);
        Anlass anlass9 = anlassService.persist(AnlassBuilder.create(
            b -> b.withTitle("Impftermin 9").withAnlassConfig(configImpftermin)
                .withDatumSetSchuljahr(appClock.offsetDateTime().now().toLocalDate())
                .withZeitVonBis(
                    new DateTimeRange(anlassStartedTime, anlassStartedTime.plusHours(2))
                )
        ));
        fixtureService.createEinladung(anlass9, dossierForSearch);

        // Anlass later today
        LocalDateTime anlassTimeLater = appClock.offsetDateTime().now().toLocalDateTime().plusHours(59);
        Anlass anlass10 = anlassService.persist(AnlassBuilder.create(
            b -> b.withTitle("Impftermin 10").withAnlassConfig(configImpftermin)
                .withDatumSetSchuljahr(appClock.offsetDateTime().now().toLocalDate())
                .withZeitVonBis(
                    new DateTimeRange(anlassTimeLater, anlassTimeLater.plusHours(2))
                )
        ));
        fixtureService.createEinladung(anlass10, dossierForSearch);

        // Einzelanlass with where kind is already eingeladen
        Anlass anlass11 = anlassService.persist(AnlassBuilder.create(builder -> builder
            .withTitle("Impftermin 11")
            .withAnlassArt(AnlassArt.EINZEL_ANLASS)
            .withAnlassConfig(configImpftermin))
        );
        fixtureService.createEinladung(anlass11, dossierForSearch);

        // Anlaesse should not be found
        // kind not invited
        Anlass anlass2_1 = anlassService.persist(AnlassBuilder.create(builder -> builder.withTitle("Impftermin 2.1")
            .withAnlassConfig(configImpftermin)));
        fixtureService.createEinladung(anlass2_1, otherDossier);

        // empty anlass
        anlassService.persist(AnlassBuilder.create(builder -> builder.withTitle("Impftermin 2.2")
            .withAnlassConfig(configImpftermin)));

        // anlaesse with status finished or closed
        Anlass anlass2_2 = anlassService.persist(AnlassBuilder.create(
            b -> b.withTitle("Impftermin 2.2")
                .withAnlassConfig(configImpftermin)
                .withAnlassStatus(AnlassStatus.DURCHGEFUEHRT)
        ));
        fixtureService.createEinladung(anlass2_2, dossierForSearch);

        Anlass anlass2_3 = anlassService.persist(AnlassBuilder.create(
            b -> b.withTitle("Impftermin 2.3")
                .withAnlassConfig(configImpftermin)
                .withAnlassStatus(AnlassStatus.ABGESAGT)
        ));
        fixtureService.createEinladung(anlass2_3, dossierForSearch);

        Anlass anlass2_4 = anlassService.persist(AnlassBuilder.create(
            b -> b.withTitle("Impftermin 2.4")
                .withAnlassConfig(configImpftermin)
                .withAnlassStatus(AnlassStatus.ABGESCHLOSSEN)
        ));
        fixtureService.createEinladung(anlass2_4, dossierForSearch);

        Anlass anlass2_6 = anlassService.persist(AnlassBuilder.create(
            b -> b.withTitle("Impftermin 2.5")
                .withAnlassConfig(configImpftermin)
                .withDatumSetSchuljahr(appClock.offsetDateTime().now().toLocalDate().minusDays(1))
        ));
        fixtureService.createEinladung(anlass2_6, dossierForSearch);

        // Anlass was yesterday
        Anlass anlass2_7 = anlassService.persist(AnlassBuilder.create(
            b -> b.withTitle("Impftermin 2.6")
                .withAnlassConfig(configImpftermin)
                .withDatumSetSchuljahr(appClock.offsetDateTime().now().toLocalDate().minusDays(1))
        ));
        fixtureService.createEinladung(anlass2_7, dossierForSearch);

        // Anlass with different type
        Anlass anlass2_8 = anlassService.persist(AnlassBuilder.create(
            b -> b.withTitle("Impftermin 2.7")
                .withAnlassConfig(configKlasse8)
                .withDatumSetSchuljahr(appClock.offsetDateTime().now().toLocalDate().minusDays(1))
        ));
        fixtureService.createEinladung(anlass2_8, dossierForSearch);

        // Excluded anlass
        Anlass excludedAnlass = anlassService.persist(AnlassBuilder.create(
            b -> b.withTitle("Impftermin 2.8")
                .withAnlassConfig(configImpftermin)
                .withDatumSetSchuljahr(appClock.offsetDateTime().now().toLocalDate().minusDays(1))
        ));
        fixtureService.createEinladung(excludedAnlass, dossierForSearch);

        // ACT
        AnlassWithDateTimeListJsonSpec result =
            api.anlassResourceFindScheduledAnlaesseForKind()
                .anlassTypQuery(AnlassTyp.IMPFTERMIN.toString())
                .kindIdQuery(dossierForSearch.getKind().getId().getUUID())
                .excludeAnlassIdsQuery(excludedAnlass.getId().getUUID())
                .execute(ResponseBody::prettyPeek)
                .then()
                .assertThat()
                .statusCode(Status.OK.getStatusCode())
                .extract()
                .as(AnlassWithDateTimeListJsonSpec.class);

        // ASSERT
        Assertions.assertThatList(result.getAnlaesseWithDateTime())
            .hasSize(11)
            .extracting(
                AnlassWithDateTimeJsonSpec::getTitel,
                AnlassWithDateTimeJsonSpec::getAnlassId
            )
            .containsExactlyInAnyOrderElementsOf(
                List.of(
                    tuple("Impftermin 1", anlass1.getId().getUUID().toString()),
                    tuple("Impftermin 2", anlass2.getId().getUUID().toString()),
                    tuple("Impftermin 3", anlass3.getId().getUUID().toString()),
                    tuple("Impftermin 4", anlass4.getId().getUUID().toString()),
                    tuple("Impftermin 5", anlass5.getId().getUUID().toString()),
                    tuple("Impftermin 6", anlass6.getId().getUUID().toString()),
                    tuple("Impftermin 7", anlass7.getId().getUUID().toString()),
                    tuple("Impftermin 8", anlass8.getId().getUUID().toString()),
                    tuple("Impftermin 9", anlass9.getId().getUUID().toString()),
                    tuple("Impftermin 10", anlass10.getId().getUUID().toString()),
                    tuple("Impftermin 11", anlass11.getId().getUUID().toString())
                )
            );
    }
}
