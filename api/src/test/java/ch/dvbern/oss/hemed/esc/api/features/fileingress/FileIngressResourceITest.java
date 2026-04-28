package ch.dvbern.oss.hemed.esc.api.features.fileingress;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Optional;

import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfig;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.AnlassConfigService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.DossierService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.edokid_id.EdokidId;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizTyp;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.AnlassConfigBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsFileIngressBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.FileIngressApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.FileIngressResultJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.FileIngressStatusSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
@SuppressWarnings("TestMethodWithoutAssertion")
@QuarkusTest
@RequiredArgsConstructor
class FileIngressResourceITest extends AbstractITest {

    private final FileIngressApiSpec api = FileIngressApiSpec.fileIngress(RequestSpecUtil.quarkusSpec());
    private static final LocalDate DATE_EARLY = LocalDate.of(2025, 1, 1);

    private final FixtureService fixtureService;
    private final DossierService dossierService;
    private final AnlassConfigService anlassConfigService;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        setTime(OffsetDateTime.of(LocalDateTime.of(2025, Month.NOVEMBER, 26, 0, 0, 0), ZoneOffset.UTC));
    }

    @TestAsAdminBS
    @Test
    void file_ingress_wrong_user() {
        // ARRANGE
        File fragebogen = loadFile("fragebogen.pdf");
        Anlass anlass = fixtureService.createAnlass();
        // ACT / ASSERT
        api.fileIngressResourceUploadNotiz()
            ._fileMultiPart(fragebogen)
            .esadIdPath("1234567890")
            .anlassIdPath(anlass.getId().getId())
            .notizTypPath(NotizTyp.FRAGEBOGEN)
            .execute(ResponseBody::peek)
            .then()
            .statusCode(Status.FORBIDDEN.getStatusCode());
    }

    @TestAsFileIngressBS
    @Test
    void file_ingress_invalid_filename_not_parsable() {
        // ARRANGE
        File fragebogen = loadFile("fragebogen.pdf");
        Anlass anlass = fixtureService.createAnlass();
        // ACT
        FileIngressResultJsonSpec result = api.fileIngressResourceUploadNotiz()
            ._fileMultiPart(fragebogen)
            .esadIdPath("1234567890")
            .anlassIdPath(anlass.getId().getId())
            .notizTypPath(NotizTyp.FRAGEBOGEN)
            .execute(ResponseBody::peek)
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract()
            .as(FileIngressResultJsonSpec.class);
        // ASSERT
        assertFileIngressResultError(result, "InvalidFilenameForNotizIngress");
    }

    @TestAsFileIngressBS
    @Test
    void file_ingress_invalid_filename_geburtsdatum_invalid() {
        // ARRANGE
        File fragebogen = loadFile("01.01.2025__A00145625.pdf");
        Anlass anlass = fixtureService.createAnlass();
        // ACT
        FileIngressResultJsonSpec result = api.fileIngressResourceUploadNotiz()
            ._fileMultiPart(fragebogen)
            .esadIdPath("A00145625")
            .anlassIdPath(anlass.getId().getId())
            .notizTypPath(NotizTyp.FRAGEBOGEN)
            .execute(ResponseBody::peek)
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract()
            .as(FileIngressResultJsonSpec.class);
        // ASSERT
        assertFileIngressResultError(result, "InvalidFilenameForNotizIngress");
    }

    @TestAsFileIngressBS
    @Test
    void file_ingress_invalid_filename_esadId_invalid() {
        // ARRANGE
        File fragebogen = loadFile("2025-01-01__Gugus.pdf");
        Anlass anlass = fixtureService.createAnlass();
        // ACT
        FileIngressResultJsonSpec result = api.fileIngressResourceUploadNotiz()
            ._fileMultiPart(fragebogen)
            .esadIdPath("Gugus")
            .anlassIdPath(anlass.getId().getId())
            .notizTypPath(NotizTyp.FRAGEBOGEN)
            .execute(ResponseBody::peek)
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract()
            .as(FileIngressResultJsonSpec.class);
        // ASSERT
        assertFileIngressResultError(result, "InvalidFilenameForNotizIngress");
    }

    @TestAsFileIngressBS
    @Test
    void file_ingress_invalid_filename_esadId_not_same_as_pathParam() {
        // ARRANGE
        File fragebogen = loadFile("2025-01-01__A00145625.pdf");
        Anlass anlass = fixtureService.createAnlass();
        // ACT
        FileIngressResultJsonSpec result = api.fileIngressResourceUploadNotiz()
            ._fileMultiPart(fragebogen)
            .esadIdPath("A00145626") // intentionally wrong
            .anlassIdPath(anlass.getId().getId())
            .notizTypPath(NotizTyp.FRAGEBOGEN)
            .execute(ResponseBody::peek)
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract()
            .as(FileIngressResultJsonSpec.class);
        // ASSERT
        assertFileIngressResultError(result, "InvalidFilenameForNotizIngress");
    }

    @TestAsFileIngressBS
    @Test
    void file_ingress_no_dossier_found() {
        // ARRANGE
        Optional<Dossier> dossierOptional = findByEsadId("A00145625");
        assertThat(dossierOptional).isEmpty();
        File fragebogen = loadFile("2025-01-01__A00145625.pdf");
        Anlass anlass = fixtureService.createAnlass();
        // ACT
        FileIngressResultJsonSpec result = api.fileIngressResourceUploadNotiz()
            ._fileMultiPart(fragebogen)
            .esadIdPath("A00145625")
            .anlassIdPath(anlass.getId().getId())
            .notizTypPath(NotizTyp.FRAGEBOGEN)
            .execute(ResponseBody::peek)
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract()
            .as(FileIngressResultJsonSpec.class);
        // ASSERT
        assertFileIngressResultError(result, "NoDossierForEsadId");
    }

    @TestAsFileIngressBS
    @Test
    void file_ingress_geburtsdatum_mismatch() {
        // ARRANGE
        String esadId = "A00145625";
        fixtureService.createDossier(LocalDate.now(), esadId);
        Optional<Dossier> dossierOptional = findByEsadId(esadId);
        assertThat(dossierOptional).isPresent();
        File fragebogen = loadFile("2025-01-01__" + esadId + ".pdf");
        Anlass anlass = fixtureService.createAnlass();
        // ACT
        FileIngressResultJsonSpec result = api.fileIngressResourceUploadNotiz()
            ._fileMultiPart(fragebogen)
            .esadIdPath(esadId)
            .anlassIdPath(anlass.getId().getId())
            .notizTypPath(NotizTyp.FRAGEBOGEN)
            .execute(ResponseBody::peek)
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract()
            .as(FileIngressResultJsonSpec.class);
        // ASSERT
        assertFileIngressResultError(result, "FileIngressGeburtsdatumMissmatch");
    }

    @TestAsFileIngressBS
    @Test
    void file_ingress_invalid_notiz_typ() {
        // ARRANGE
        NotizTyp notizTyp = NotizTyp.ABMELDUNG;
        File fragebogen = loadFile("2025-01-01__A00145625.pdf");
        Anlass anlass = fixtureService.createAnlass();
        // ACT
        FileIngressResultJsonSpec result = api.fileIngressResourceUploadNotiz()
            ._fileMultiPart(fragebogen)
            .esadIdPath("A00145625")
            .anlassIdPath(anlass.getId().getId())
            .notizTypPath(notizTyp)
            .execute(ResponseBody::peek)
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract()
            .as(FileIngressResultJsonSpec.class);
        // ASSERT
        assertFileIngressResultError(result, "InvalidNotizTypIngress");
    }


    @TestAsFileIngressBS
    @Test
    void file_ingress_invalid_geburtsdatum() {
        // ARRANGE
        NotizTyp notizTyp = NotizTyp.ABMELDUNG;
        File fragebogen = loadFile("2025-02-31__A00145625.pdf");
        Anlass anlass = fixtureService.createAnlass();
        // ACT
        FileIngressResultJsonSpec result = api.fileIngressResourceUploadNotiz()
            ._fileMultiPart(fragebogen)
            .esadIdPath("A00145625")
            .anlassIdPath(anlass.getId().getId())
            .notizTypPath(notizTyp)
            .execute(ResponseBody::peek)
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract()
            .as(FileIngressResultJsonSpec.class);
        // ASSERT
        assertFileIngressResultError(result, "InvalidNotizTypIngress");
    }

    @TestAsFileIngressBS
    @Test
    void file_ingress_no_einladung() {
        // ARRANGE
        String esadId = "A00145626";
        LocalDate geburtsdatum = LocalDate.of(2025, 1, 1);
        fixtureService.createDossier(geburtsdatum, esadId);
        Optional<Dossier> dossierOptional = findByEsadId(esadId);
        assertThat(dossierOptional).isPresent();
        File fragebogen = loadFile(geburtsdatum.format(FileIngressService.DATE_TIME_FORMATTER) + "__" + esadId + ".pdf");
        Anlass anlass = fixtureService.createAnlass();
        // ACT
        FileIngressResultJsonSpec result = api.fileIngressResourceUploadNotiz()
            ._fileMultiPart(fragebogen)
            .esadIdPath(esadId)
            .anlassIdPath(anlass.getId().getId())
            .notizTypPath(NotizTyp.FRAGEBOGEN)
            .execute(ResponseBody::peek)
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract()
            .as(FileIngressResultJsonSpec.class);
        // ASSERT
        assertFileIngressResultError(result, "FileIngressNoSuitableAnlass");
    }

    @TestAsFileIngressBS
    @Test
    void file_ingress_success_only_one_anlass() {
        // ARRANGE
        String esadId = "A00145629";
        LocalDate geburtsdatum = LocalDate.of(2025, 1, 1);
        Dossier dossier = fixtureService.createDossier(geburtsdatum, esadId);
        fixtureService.createEinladung(dossier, AnlassTyp.IMPFTERMIN, DATE_EARLY, "theOtherAnlass");
        Einladung einladung = fixtureService.createEinladung(dossier, AnlassTyp.KLASSE_8, DATE_EARLY, "theOnlyAnlass");
        Optional<Dossier> dossierOptional = findByEsadId(esadId);
        assertThat(dossierOptional).isPresent();
        File fragebogen = loadFile(geburtsdatum.format(FileIngressService.DATE_TIME_FORMATTER) + "__" + esadId + ".pdf");
        // ACT
        FileIngressResultJsonSpec result = api.fileIngressResourceUploadNotiz()
            ._fileMultiPart(fragebogen)
            .esadIdPath(esadId)
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .notizTypPath(NotizTyp.FRAGEBOGEN)
            .execute(ResponseBody::peek)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(FileIngressResultJsonSpec.class);
        einladung = fixtureService.readEinladungWithFilesAndLabels(einladung.getId());
        // ASSERT
        assertFileIngressResultSuccess(
            result,
            "Die Datei 2025-01-01__A00145629.pdf wurde erfolgreich hochgeladen als 26-11-2025_Gerber-Emma_Fragebogen.pdf und angehängt an Dossier mit Esad-ID A0 01 456 29 im Anlass 2024/25 SU 2.SEK theOnlyAnlass.");
        assertThat(einladung.getFragebogenNotiz()).isPresent();
        assertThat(einladung.getFragebogenNotiz().get().getFiles()).hasSize(1);
        assertThat(einladung.getFragebogenNotiz().get().getBemerkungen()).isEqualTo("Datei 26-11-2025_Gerber-Emma_Fragebogen.pdf wurde am 26.11.2025 automatisch hochgeladen.");
    }

    @TestAsFileIngressBS
    @Test
    void file_ingress_success_anmeldung_bestaetigung_new_notiz() {
        // ARRANGE
        String esadId = "A00145630";
        LocalDate geburtsdatum = LocalDate.of(2025, 1, 1);
        Dossier dossier = fixtureService.createDossier(geburtsdatum, esadId);

        AnlassConfig anlassConfig = AnlassConfigBuilder.create(builder -> builder
            .withAnlassTyp(AnlassTyp.KLASSE_8)
            .withShowAnmeldungBestaetigung(true));
        anlassConfigService.createAnlassConfig(anlassConfig);
        Anlass anlass = fixtureService.createAnlass(anlassConfig);

        Einladung einladung = fixtureService.createEinladung(anlass, dossier);
        File file = loadFile(geburtsdatum.format(FileIngressService.DATE_TIME_FORMATTER) + "__" + esadId + ".pdf");

        // ACT
        FileIngressResultJsonSpec result = api.fileIngressResourceUploadNotiz()
            ._fileMultiPart(file)
            .esadIdPath(esadId)
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .notizTypPath(NotizTyp.ANMELDUNG_BESTAETIGUNG)
            .execute(ResponseBody::peek)
            .then()
            .log().all()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(FileIngressResultJsonSpec.class);

        einladung = fixtureService.readEinladungWithFilesAndLabels(einladung.getId());

        // ASSERT
        assertFileIngressResultSuccess(
            result,
            "Die Datei 2025-01-01__A00145630.pdf wurde erfolgreich hochgeladen als 26-11-2025_Gerber-Emma_Anmeldung-bestaetigung.pdf und angehängt an Dossier mit Esad-ID A0 01 456 30 im Anlass 2022/23 SU 2.SEK Test getAnlass.");
        assertThat(einladung.getAnmeldungBestaetigungNotiz()).isPresent();
        assertThat(einladung.getAnmeldungBestaetigungNotiz().get().getFiles()).hasSize(1);
    }

    @TestAsFileIngressBS
    @Test
    void file_ingress_success_impfanmeldung_existing_notiz() {
        // ARRANGE
        String esadId = "A00145631";
        LocalDate geburtsdatum = LocalDate.of(2025, 1, 1);
        Dossier dossier = fixtureService.createDossier(geburtsdatum, esadId);
        Einladung einladung = fixtureService.createEinladung(dossier, AnlassTyp.KLASSE_8, DATE_EARLY, "theOnlyAnlass");
        fixtureService.createImpfanmeldungNotiz(einladung.getId());
        File file = loadFile(geburtsdatum.format(FileIngressService.DATE_TIME_FORMATTER) + "__" + esadId + ".pdf");

        // ACT
        FileIngressResultJsonSpec result = api.fileIngressResourceUploadNotiz()
            ._fileMultiPart(file)
            .esadIdPath(esadId)
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .notizTypPath(NotizTyp.IMPFANMELDUNG)
            .execute(ResponseBody::peek)
            .then()
            .log().all()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(FileIngressResultJsonSpec.class);

        einladung = fixtureService.readEinladungWithFilesAndLabels(einladung.getId());

        // ASSERT
        assertFileIngressResultSuccess(
            result,
            "Die Datei 2025-01-01__A00145631.pdf wurde erfolgreich hochgeladen als 26-11-2025_Gerber-Emma_Impfanmeldung.pdf und angehängt an Dossier mit Esad-ID A0 01 456 31 im Anlass 2024/25 SU 2.SEK theOnlyAnlass.");
        assertThat(einladung.getImpfanmeldungsNotiz()).isPresent();
        assertThat(einladung.getImpfanmeldungsNotiz().get().getFiles()).hasSize(1);
    }

    @TestAsFileIngressBS
    @Test
    void file_ingress_success_impfausweis_new_notiz() {
        // ARRANGE
        String esadId = "A00145632";
        LocalDate geburtsdatum = LocalDate.of(2025, 1, 1);
        Dossier dossier = fixtureService.createDossier(geburtsdatum, esadId);
        Einladung einladung = fixtureService.createEinladung(dossier, AnlassTyp.KLASSE_8, DATE_EARLY, "theOnlyAnlass");
        File file = loadFile(geburtsdatum.format(FileIngressService.DATE_TIME_FORMATTER) + "__" + esadId + ".pdf");

        // ACT
        FileIngressResultJsonSpec result = api.fileIngressResourceUploadNotiz()
            ._fileMultiPart(file)
            .esadIdPath(esadId)
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .notizTypPath(NotizTyp.IMPFAUSWEIS)
            .execute(ResponseBody::peek)
            .then()
            .log().all()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(FileIngressResultJsonSpec.class);

        einladung = fixtureService.readEinladungWithFilesAndLabels(einladung.getId());

        // ASSERT
        assertFileIngressResultSuccess(
            result,
            "Die Datei 2025-01-01__A00145632.pdf wurde erfolgreich hochgeladen als 26-11-2025_Gerber-Emma_Impfausweis.pdf und angehängt an Dossier mit Esad-ID A0 01 456 32 im Anlass 2024/25 SU 2.SEK theOnlyAnlass.");
        assertThat(einladung.getImpfausweisNotiz()).isPresent();
        assertThat(einladung.getImpfausweisNotiz().get().getFiles()).hasSize(1);
    }

    @TestAsFileIngressBS
    @Test
    void file_ingress_success_fragebogen_new_notiz() {
        // ARRANGE
        String esadId = "A00145633";
        LocalDate geburtsdatum = LocalDate.of(2025, 1, 1);
        Dossier dossier = fixtureService.createDossier(geburtsdatum, esadId);
        Einladung einladung = fixtureService.createEinladung(dossier, AnlassTyp.KLASSE_8, DATE_EARLY, "theOnlyAnlass");
        File file = loadFile(geburtsdatum.format(FileIngressService.DATE_TIME_FORMATTER) + "__" + esadId + ".pdf");

        // ACT
        FileIngressResultJsonSpec result = api.fileIngressResourceUploadNotiz()
            ._fileMultiPart(file)
            .esadIdPath(esadId)
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .notizTypPath(NotizTyp.FRAGEBOGEN)
            .execute(ResponseBody::peek)
            .then()
            .log().all()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(FileIngressResultJsonSpec.class);

        einladung = fixtureService.readEinladungWithFilesAndLabels(einladung.getId());

        // ASSERT
        assertFileIngressResultSuccess(
            result,
            "Die Datei 2025-01-01__A00145633.pdf wurde erfolgreich hochgeladen als 26-11-2025_Gerber-Emma_Fragebogen.pdf und angehängt an Dossier mit Esad-ID A0 01 456 33 im Anlass 2024/25 SU 2.SEK theOnlyAnlass.");
        assertThat(einladung.getFragebogenNotiz()).isPresent();
        assertThat(einladung.getFragebogenNotiz().get().getFiles()).hasSize(1);
    }

    @TestAsFileIngressBS
    @Test
    void file_ingress_success_impfanmeldung_new_notiz() {
        // ARRANGE
        String esadId = "A00145639";
        LocalDate geburtsdatum = LocalDate.of(2025, 1, 1);
        Dossier dossier = fixtureService.createDossier(geburtsdatum, esadId);
        Einladung einladung = fixtureService.createEinladung(dossier, AnlassTyp.KLASSE_8, DATE_EARLY, "theOnlyAnlass");
        File file = loadFile(geburtsdatum.format(FileIngressService.DATE_TIME_FORMATTER) + "__" + esadId + ".pdf");

        // ACT
        FileIngressResultJsonSpec result = api.fileIngressResourceUploadNotiz()
            ._fileMultiPart(file)
            .esadIdPath(esadId)
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .notizTypPath(NotizTyp.IMPFANMELDUNG)
            .execute(ResponseBody::peek)
            .then()
            .log().all()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(FileIngressResultJsonSpec.class);

        einladung = fixtureService.readEinladungWithFilesAndLabels(einladung.getId());

        // ASSERT
        assertFileIngressResultSuccess(
            result,
            "Die Datei 2025-01-01__A00145639.pdf wurde erfolgreich hochgeladen als 26-11-2025_Gerber-Emma_Impfanmeldung.pdf und angehängt an Dossier mit Esad-ID A0 01 456 39 im Anlass 2024/25 SU 2.SEK theOnlyAnlass.");
        assertThat(einladung.getImpfanmeldungsNotiz()).isPresent();
        assertThat(einladung.getImpfanmeldungsNotiz().get().getFiles()).hasSize(1);
    }

    @TestAsFileIngressBS
    @Test
    void file_ingress_success_notiz_already_exists_with_bemerkungen() {
        // ARRANGE
        String esadId = "A00145634";
        LocalDate geburtsdatum = LocalDate.of(2025, 1, 1);
        Dossier dossier = fixtureService.createDossier(geburtsdatum, esadId);
        Einladung einladung = fixtureService.createEinladung(dossier, AnlassTyp.KLASSE_8, DATE_EARLY, "theOnlyAnlass");
        fixtureService.createFragebogenNotizWithBemerkung(einladung.getId(), "Bestehende Bemerkung");

        File file = loadFile(geburtsdatum.format(FileIngressService.DATE_TIME_FORMATTER) + "__" + esadId + ".pdf");

        // ACT
        FileIngressResultJsonSpec result = api.fileIngressResourceUploadNotiz()
            ._fileMultiPart(file)
            .esadIdPath(esadId)
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .notizTypPath(NotizTyp.FRAGEBOGEN)
            .execute(ResponseBody::peek)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(FileIngressResultJsonSpec.class);

        einladung = fixtureService.readEinladungWithFilesAndLabels(einladung.getId());

        // ASSERT
        assertFileIngressResultSuccess(
            result,
            "Die Datei 2025-01-01__A00145634.pdf wurde erfolgreich hochgeladen als 26-11-2025_Gerber-Emma_Fragebogen.pdf und angehängt an Dossier mit Esad-ID A0 01 456 34 im Anlass 2024/25 SU 2.SEK theOnlyAnlass.");
        assertThat(einladung.getFragebogenNotiz()).isPresent();
        assertThat(einladung.getFragebogenNotiz().get().getFiles()).hasSize(1);
        assertThat(einladung.getFragebogenNotiz().get().getBemerkungen()).startsWith("Bestehende Bemerkung\nDatei 26-11-2025_Gerber-Emma_Fragebogen.pdf wurde am 26.11.2025 automatisch hochgeladen.");
    }

    @TestAsFileIngressBS
    @Test
    void file_ingress_fails_fragebogen_if_anlass_config_is_not_enabled() {
        // ARRANGE
        String esadId = "A00145635";
        LocalDate geburtsdatum = LocalDate.of(2025, 1, 1);
        Dossier dossier = fixtureService.createDossier(geburtsdatum, esadId);

        AnlassConfig anlassConfig = AnlassConfigBuilder.create(builder -> builder.withAnlassTyp(AnlassTyp.IMPFAKTION).withShowFragebogen(false));
        anlassConfigService.createAnlassConfig(anlassConfig);
        Anlass anlass = fixtureService.createAnlass(anlassConfig);
        Einladung einladung = fixtureService.createEinladung(anlass, dossier);
        File file = loadFile(geburtsdatum.format(FileIngressService.DATE_TIME_FORMATTER) + "__" + esadId + ".pdf");

        // ACT
        FileIngressResultJsonSpec result = api.fileIngressResourceUploadNotiz()
            ._fileMultiPart(file)
            .esadIdPath(esadId)
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .notizTypPath(NotizTyp.FRAGEBOGEN)
            .execute(ResponseBody::peek)
            .then()
            .log().all()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract()
            .as(FileIngressResultJsonSpec.class);

        // ASSERT
        assertFileIngressResultError(result, "InvalidNotizTypForAnlassIngress");
    }

    @TestAsFileIngressBS
    @Test
    void file_ingress_fails_impfausweis_if_anlass_config_is_not_enabled() {
        // ARRANGE
        String esadId = "A00145636";
        LocalDate geburtsdatum = LocalDate.of(2025, 1, 1);
        Dossier dossier = fixtureService.createDossier(geburtsdatum, esadId);

        AnlassConfig anlassConfig = AnlassConfigBuilder.create(builder -> builder.withAnlassTyp(AnlassTyp.KLASSE_4).withShowImpfausweis(false));
        anlassConfigService.createAnlassConfig(anlassConfig);
        Anlass anlass = fixtureService.createAnlass(anlassConfig);
        Einladung einladung = fixtureService.createEinladung(anlass, dossier);
        File file = loadFile(geburtsdatum.format(FileIngressService.DATE_TIME_FORMATTER) + "__" + esadId + ".pdf");

        // ACT
        FileIngressResultJsonSpec result = api.fileIngressResourceUploadNotiz()
            ._fileMultiPart(file)
            .esadIdPath(esadId)
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .notizTypPath(NotizTyp.IMPFAUSWEIS)
            .execute(ResponseBody::peek)
            .then()
            .log().all()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract()
            .as(FileIngressResultJsonSpec.class);

        // ASSERT
        assertFileIngressResultError(result, "InvalidNotizTypForAnlassIngress");
    }

    @TestAsFileIngressBS
    @Test
    void file_ingress_fails_impfanmeldung_if_anlass_config_is_not_enabled() {
        // ARRANGE
        String esadId = "A00145637";
        LocalDate geburtsdatum = LocalDate.of(2025, 1, 1);
        Dossier dossier = fixtureService.createDossier(geburtsdatum, esadId);

        AnlassConfig anlassConfig = AnlassConfigBuilder.create(builder -> builder.withAnlassTyp(AnlassTyp.KLASSE_5).withShowImpfanmeldung(false));
        anlassConfigService.createAnlassConfig(anlassConfig);
        Anlass anlass = fixtureService.createAnlass(anlassConfig);
        Einladung einladung = fixtureService.createEinladung(anlass, dossier);
        File file = loadFile(geburtsdatum.format(FileIngressService.DATE_TIME_FORMATTER) + "__" + esadId + ".pdf");

        // ACT
        FileIngressResultJsonSpec result = api.fileIngressResourceUploadNotiz()
            ._fileMultiPart(file)
            .esadIdPath(esadId)
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .notizTypPath(NotizTyp.IMPFANMELDUNG)
            .execute(ResponseBody::peek)
            .then()
            .log().all()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract()
            .as(FileIngressResultJsonSpec.class);

        // ASSERT
        assertFileIngressResultError(result, "InvalidNotizTypForAnlassIngress");
    }

    @TestAsFileIngressBS
    @Test
    void file_ingress_fails_anmeldung_bestaetigung_if_anlass_config_is_not_enabled() {
        // ARRANGE
        String esadId = "A00145638";
        LocalDate geburtsdatum = LocalDate.of(2025, 1, 1);
        Dossier dossier = fixtureService.createDossier(geburtsdatum, esadId);

        AnlassConfig anlassConfig = AnlassConfigBuilder.create(builder -> builder.withAnlassTyp(AnlassTyp.FREMDSPRACHEN).withShowAnmeldungBestaetigung(false));
        anlassConfigService.createAnlassConfig(anlassConfig);
        Anlass anlass = fixtureService.createAnlass(anlassConfig);
        Einladung einladung = fixtureService.createEinladung(anlass, dossier);
        File file = loadFile(geburtsdatum.format(FileIngressService.DATE_TIME_FORMATTER) + "__" + esadId + ".pdf");

        // ACT
        FileIngressResultJsonSpec result = api.fileIngressResourceUploadNotiz()
            ._fileMultiPart(file)
            .esadIdPath(esadId)
            .anlassIdPath(einladung.getAnlass().getId().getId())
            .notizTypPath(NotizTyp.ANMELDUNG_BESTAETIGUNG)
            .execute(ResponseBody::peek)
            .then()
            .log().all()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract()
            .as(FileIngressResultJsonSpec.class);

        // ASSERT
        assertFileIngressResultError(result, "InvalidNotizTypForAnlassIngress");
    }

    @NonNull
    private File loadFile(@NonNull String desiredFilename) {
        File file = TestUtil.loadTestfileWithFilename("/FileUtilTest/pdfFileExample.pdf", desiredFilename);
        assertThat(file).exists();
        return file;
    }

    void assertFileIngressResultSuccess(@NonNull FileIngressResultJsonSpec result, @NonNull String expectedSuccessMessage) {
        assertNotNull(result);
        assertThat(result.getStatus()).isEqualTo(FileIngressStatusSpec.OK);
        assertThat(result.getSuccessMessage()).isNotBlank().isEqualTo(expectedSuccessMessage);
        assertThat(result.getErrors()).isEmpty();
    }

    void assertFileIngressResultError(@NonNull FileIngressResultJsonSpec result, @NonNull String expectedKey) {
        assertNotNull(result);
        assertThat(result.getStatus()).isEqualTo(FileIngressStatusSpec.ERROR);
        assertThat(result.getSuccessMessage()).isNull();
        assertThat(result.getErrors()).hasSize(1);
        assertThat(result.getErrors().getFirst().getViolations().getFirst().getKey())
            .isEqualTo(expectedKey);
    }

    @NonNull
    private Optional<Dossier> findByEsadId(String esadId) {
        return dossierService.findByEsadId(Objects.requireNonNull(EdokidId.parse(esadId)).toFormattedString());
    }
}
