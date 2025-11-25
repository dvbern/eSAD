package ch.dvbern.oss.hemed.esc.api.features.arzt;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.dvbstarter.shared.types.phonenumber.NormalizedPhoneNumber;
import ch.dvbern.oss.hemed.esc.api.features.arzt.types.Fachrichtung;
import ch.dvbern.oss.hemed.esc.api.features.arzt.types.HpvTeilnahme;
import ch.dvbern.oss.hemed.esc.api.features.arzt.types.Organisation;
import ch.dvbern.oss.hemed.esc.api.shared.person.Adresse;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.AdresseBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.ArztBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.AerzteApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.ArztIdListJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.ArztJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.ArztListJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.ArztSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.ExportModeSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import io.restassured.response.ResponseOptions;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

@QuarkusTest
@RequiredArgsConstructor
class ArztResourceITest extends AbstractITest {

    private final AerzteApiSpec api = AerzteApiSpec.aerzte(RequestSpecUtil.quarkusSpec());

    private final ArztService arztService;


    @Test
    @TestAsAdminBS
    void createArzt() {
        // ARRANGE
        final ArztSaveJsonSpec expectedArztSaveJson =
            ArztBuilder.createSaveJaxSpec(builder -> builder.withAnrede("Anrede")
                .withBemerkungen("Bemerkung")
                .withEmail("test@mailbucket.dvbern.ch")
                .withFachrichtung(Fachrichtung.ANDERES)
                .withOrganisation(Organisation.JURISTISCHE_PERSON)
                .withHpvTeilnahme(HpvTeilnahme.JA)
                .withAdresse(AdresseBuilder.defaultAdresse())
                .withName(TestUtil.getRandomString("Name")));
        Assertions.assertThat(expectedArztSaveJson.getAdresse()).isNotNull();

        // ACT
        final ArztJsonSpec result = api.arztResourceCreateArzt()
            .body(expectedArztSaveJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.CREATED.getStatusCode())
            .extract()
            .as(ArztJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        final Optional<Arzt> actualdOptional = arztService.findById(ID.of(UUID.fromString(result.getId()), Arzt.class));
        Assertions.assertThat(actualdOptional).isPresent();
        final Arzt actual = actualdOptional.get();
        Assertions.assertThat(actual.getAnrede()).isEqualTo(expectedArztSaveJson.getAnrede());
        Assertions.assertThat(actual.getBemerkungen()).isEqualTo(expectedArztSaveJson.getBemerkungen());
        Assertions.assertThat(actual.getEmail()).isNotNull().hasToString(expectedArztSaveJson.getEmail());
        Assertions.assertThat(actual.getFachrichtung()).hasToString(expectedArztSaveJson.getFachrichtung().toString());
        Assertions.assertThat(actual.getOrganisation()).hasToString(expectedArztSaveJson.getOrganisation().toString());
        Assertions.assertThat(actual.getHpvTeilnahme()).hasToString(expectedArztSaveJson.getHpvTeilnahme().toString());
        Assertions.assertThat(actual.getName()).isEqualTo(expectedArztSaveJson.getName());
        Assertions.assertThat(actual.getVorname()).isEqualTo(expectedArztSaveJson.getVorname());
        Assertions.assertThat(actual.getTelefon()).hasToString(expectedArztSaveJson.getTelefon());

        Assertions.assertThat(actual.getAdresse()).isNotNull();
        Assertions.assertThat(actual.getAdresse().getStrasseUndNr())
            .isEqualTo(expectedArztSaveJson.getAdresse().getStrasseUndNr());
        Assertions.assertThat(actual.getAdresse().getAdresseZusatz())
            .isEqualTo(expectedArztSaveJson.getAdresse().getAdresseZusatz());
        Assertions.assertThat(actual.getAdresse().getPlz()).isEqualTo(expectedArztSaveJson.getAdresse().getPlz());
        Assertions.assertThat(actual.getAdresse().getOrt()).isEqualTo(expectedArztSaveJson.getAdresse().getOrt());
    }

    @Test
    @TestAsAdminBS
    void createMinimalArzt() {
        // ARRANG
        final ArztSaveJsonSpec expectedArztSaveJson = ArztBuilder.createSaveJaxSpec(builder -> builder.withAnrede(null)
            .withVorame(null)
            .withAdresse(null)
            .withTelefon(null)
            .withEmail(null)
            .withBemerkungen(null));

        // ACT
        final ArztJsonSpec result = api.arztResourceCreateArzt()
            .body(expectedArztSaveJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.CREATED.getStatusCode())
            .extract()
            .as(ArztJsonSpec.class);

        // ASSERT
        final Optional<Arzt> actualdOptional = arztService.findById(ID.of(UUID.fromString(result.getId()), Arzt.class));
        Assertions.assertThat(actualdOptional).isPresent();
        final Arzt actual = actualdOptional.get();

        Assertions.assertThat(actual.getAnrede()).isNull();
        Assertions.assertThat(actual.getVorname()).isNull();
        Assertions.assertThat(actual.getAdresse()).isNull();
        Assertions.assertThat(actual.getTelefon()).isNull();
        Assertions.assertThat(actual.getEmail()).isNull();
        Assertions.assertThat(actual.getBemerkungen()).isNull();

        Assertions.assertThat(actual.getName()).isNotNull().isEqualTo(expectedArztSaveJson.getName());
        Assertions.assertThat(actual.getFachrichtung())
            .isNotNull()
            .hasToString(expectedArztSaveJson.getFachrichtung().toString());
        Assertions.assertThat(actual.getOrganisation())
            .isNotNull()
            .hasToString(expectedArztSaveJson.getOrganisation().toString());
        Assertions.assertThat(actual.getHpvTeilnahme())
            .isNotNull()
            .hasToString(expectedArztSaveJson.getHpvTeilnahme().toString());
    }

    @Test
    @TestAsAdminBS
    void findArzt() {
        // ARRANGE
        final String expectedTelefon = "+41 79 111 22 33";
        final Adresse expectedAdress = AdresseBuilder.create(builder -> builder.withStrasse("Musterstrasse 2")
            .withPlz("4051")
            .withOrt("Basel")
            .withAdresseZusatz(null));

        final ArztSaveJsonSpec expectedArztSaveJson =
            ArztBuilder.createSaveJaxSpec(builder -> builder.withAnrede("Frau")
                .withBemerkungen("bemerkung")
                .withEmail("arzt2@mailbucket.dvbern.ch")
                .withTelefon(NormalizedPhoneNumber.parse(expectedTelefon))
                .withFachrichtung(Fachrichtung.KINDERARZT)
                .withOrganisation(Organisation.NATUERLICHE_PERSON)
                .withHpvTeilnahme(HpvTeilnahme.NEIN)
                .withName("Doktor")
                .withVorame("Maxine")
                .withAdresse(expectedAdress));

        // ACT
        final ArztJsonSpec result = api.arztResourceGetArzt()
            .arztIdPath("11111111-1111-1111-1111-111111111002")
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(ArztJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        final ArztSaveJsonSpec resultData = result.getData();
        Assertions.assertThat(resultData.getAnrede()).isEqualTo(expectedArztSaveJson.getAnrede());
        Assertions.assertThat(resultData.getBemerkungen()).isEqualTo(expectedArztSaveJson.getBemerkungen());
        Assertions.assertThat(resultData.getEmail()).isEqualTo(expectedArztSaveJson.getEmail());
        Assertions.assertThat(resultData.getFachrichtung()).isEqualTo(expectedArztSaveJson.getFachrichtung());
        Assertions.assertThat(resultData.getOrganisation()).isEqualTo(expectedArztSaveJson.getOrganisation());
        Assertions.assertThat(resultData.getHpvTeilnahme()).isEqualTo(expectedArztSaveJson.getHpvTeilnahme());
        Assertions.assertThat(resultData.getName()).isEqualTo(expectedArztSaveJson.getName());
        Assertions.assertThat(resultData.getVorname()).isEqualTo(expectedArztSaveJson.getVorname());
        Assertions.assertThat(resultData.getTelefon()).hasToString(expectedTelefon);

        Assertions.assertThat(resultData.getAdresse()).isNotNull();
        Assertions.assertThat(resultData.getAdresse().getStrasseUndNr()).isEqualTo(expectedAdress.getStrasseUndNr());
        Assertions.assertThat(resultData.getAdresse().getAdresseZusatz()).isNull();
        Assertions.assertThat(resultData.getAdresse().getPlz()).isEqualTo(expectedAdress.getPlz());
        Assertions.assertThat(resultData.getAdresse().getOrt()).isEqualTo(expectedAdress.getOrt());
    }

    @Test
    @TestAsAdminBS
    void findAllAerzte() {
        // ARRANGE
        final List<String> expectedIds =
            List.of("11111111-1111-1111-1111-111111111001", "11111111-1111-1111-1111-111111111002");

        // ACT
        ArztListJsonSpec result = api.arztResourceFindAllArzte()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(ArztListJsonSpec.class);

        // ASSERT
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getArztList()).extracting(ArztJsonSpec::getId).containsAll(expectedIds);
    }

    @Test
    @TestAsAdminBS
    void removeArzt() {
        // ARRANGE
        final Arzt arztPersisted = arztService.persist(ArztBuilder.defaultArzt());

        final Optional<Arzt> persistedOptional = arztService.findById(arztPersisted.getId());
        Assertions.assertThat(persistedOptional).isPresent();

        // ACT
        api.arztResourceRemoveArzt()
            .arztIdPath(arztPersisted.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());

        // ASSERT
        final Optional<Arzt> actualdOptional = arztService.findById(arztPersisted.getId());
        Assertions.assertThat(actualdOptional).isNotPresent();
    }

    @Test
    @TestAsAdminBS
    void updateArzt() {
        // ARRANGE
        final Arzt arztArrange = ArztBuilder.create(builder -> builder.withName("ChangeName")
            .withEmail("change@mailbucket.dvbern.ch")
            .withFachrichtung(Fachrichtung.OPHTHALMOLOGIE_OHNE_ORTHOPTIK)
            .withOrganisation(Organisation.JURISTISCHE_PERSON));

        final Arzt arztPersisted = arztService.persist(arztArrange);

        final Optional<Arzt> persistedOptional = arztService.findById(arztPersisted.getId());
        Assertions.assertThat(persistedOptional).isPresent();

        final String expectedName = "newName";
        final String expectedEmail = "new@mailbucket.dvbern.ch";
        ArztSaveJsonSpec saveJson =
            ArztBuilder.createSaveJaxSpec(builder -> builder.withName(expectedName).withEmail(expectedEmail));

        // ACT
        api.arztResourceUpdateArzt()
            .arztIdPath(arztPersisted.getId().getId())
            .body(saveJson)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());

        // ASSERT
        Optional<Arzt> actualdOptional =
            arztService.findById(ID.of(UUID.fromString(arztPersisted.getId().getId()), Arzt.class));
        Assertions.assertThat(actualdOptional).isPresent();
        Arzt actual = actualdOptional.get();
        Assertions.assertThat(actual.getId()).isNotNull();
        Assertions.assertThat(actual.getName()).isEqualTo(expectedName);
        Assertions.assertThat(actual.getEmail()).isNotNull().hasToString(expectedEmail);
    }

    @Test
    @TestAsAdminBS
    void exportAerzteApi() {
        setTime(OffsetDateTime.of(2025, 6, 25, 0, 0, 0, 0, ZoneOffset.UTC));
        ArztIdListJsonSpec idList = new ArztIdListJsonSpec(
            Stream.of(
                    "11111111-1111-1111-1111-111111111001",
                    "11111111-1111-1111-1111-111111111002")
                .toList());

        // ACT
        byte[] actualContent = api.arztResourceExportAerzte()
            .body(idList)
            .modePath(ExportModeSpec.TEXTDOCUMENT.getValue())
            .execute(ResponseOptions::andReturn)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .header("Content-Disposition", "attachment;filename=\"export_aerzte_25-06-2025.docx\"")
            .header("Content-Type", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
            .extract()
            .asByteArray();

        // ASSERT
        Assertions.assertThat(actualContent).isNotNull().hasSizeGreaterThan(0);

    }

    @Test
    @TestAsAdminBS
    void exportAerzteCsvApi() throws IOException {
        setTime(OffsetDateTime.of(2025, 6, 25, 0, 0, 0, 0, ZoneOffset.UTC));
        ArztIdListJsonSpec idList = new ArztIdListJsonSpec(
            Stream.of(
                    "11111111-1111-1111-1111-111111111001",
                    "11111111-1111-1111-1111-111111111002")
                .toList());

        // ACT
        byte[] actualContent = api.arztResourceExportAerzte()
            .body(idList)
            .modePath(ExportModeSpec.SPREADSHEET.getValue())
            .execute(ResponseOptions::andReturn)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .header("Content-Disposition", "attachment;filename=\"export_aerzte_25-06-2025.csv\"")
            .header("Content-Type", "application/csv")
            .extract()
            .asByteArray();

        // ASSERT
        Assertions.assertThat(actualContent).isNotNull().hasSizeGreaterThan(0);
    }
}
