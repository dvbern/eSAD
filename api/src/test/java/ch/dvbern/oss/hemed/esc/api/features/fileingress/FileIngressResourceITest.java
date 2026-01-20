package ch.dvbern.oss.hemed.esc.api.features.fileingress;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Optional;

import ch.dvbern.dvbstarter.shared.types.cleanfilename.CleanFileName;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.types.AnlassTyp;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Dossier;
import ch.dvbern.oss.hemed.esc.api.features.kinder.DossierService;
import ch.dvbern.oss.hemed.esc.api.features.kinder.edokid_id.EdokidId;
import ch.dvbern.oss.hemed.esc.api.features.labels.Label;
import ch.dvbern.oss.hemed.esc.api.features.notizen.Notiz;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileData;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileInfo;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileTyp;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsFileIngress;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.FileIngressApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.FileIngressResultJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.FileIngressStatusSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.apache.tika.mime.MediaType;
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
    private static final LocalDate DATE_LATE = LocalDate.of(2025, 12, 31);

    private final FixtureService fixtureService;
    private final DossierService dossierService;

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
        File fragebogen = loadFragebogen("fragebogen.pdf");
        // ACT / ASSERT
        api.fileIngressResourceUploadFragebogen()
            ._fileMultiPart(fragebogen)
            .esadIdPath("1234567890")
            .execute(ResponseBody::peek)
            .then()
            .statusCode(Status.FORBIDDEN.getStatusCode());
    }

    @TestAsFileIngress
    @Test
    void file_ingress_invalid_filename_not_parsable() {
        // ARRANGE
        File fragebogen = loadFragebogen("fragebogen.pdf");
        // ACT
        FileIngressResultJsonSpec result = api.fileIngressResourceUploadFragebogen()
            ._fileMultiPart(fragebogen)
            .esadIdPath("1234567890")
            .execute(ResponseBody::peek)
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract()
            .as(FileIngressResultJsonSpec.class);
        // ASSERT
        assertFileIngressResultError(result, "InvalidFilenameForFragebogenIngress");
    }

    @TestAsFileIngress
    @Test
    void file_ingress_invalid_filename_geburtsdatum_invalid() {
        // ARRANGE
        File fragebogen = loadFragebogen("01.01.2025__A00145625.pdf");
        // ACT
        FileIngressResultJsonSpec result = api.fileIngressResourceUploadFragebogen()
            ._fileMultiPart(fragebogen)
            .esadIdPath("A00145625")
            .execute(ResponseBody::peek)
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract()
            .as(FileIngressResultJsonSpec.class);
        // ASSERT
        assertFileIngressResultError(result, "InvalidFilenameForFragebogenIngress");
    }

    @TestAsFileIngress
    @Test
    void file_ingress_invalid_filename_esadId_invalid() {
        // ARRANGE
        File fragebogen = loadFragebogen("2025-01-01__Gugus.pdf");
        // ACT
        FileIngressResultJsonSpec result = api.fileIngressResourceUploadFragebogen()
            ._fileMultiPart(fragebogen)
            .esadIdPath("Gugus")
            .execute(ResponseBody::peek)
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract()
            .as(FileIngressResultJsonSpec.class);
        // ASSERT
        assertFileIngressResultError(result, "InvalidFilenameForFragebogenIngress");
    }

    @TestAsFileIngress
    @Test
    void file_ingress_invalid_filename_esadId_not_same_as_pathParam() {
        // ARRANGE
        File fragebogen = loadFragebogen("2025-01-01__A00145625.pdf");
        // ACT
        FileIngressResultJsonSpec result = api.fileIngressResourceUploadFragebogen()
            ._fileMultiPart(fragebogen)
            .esadIdPath("A00145626") // intentionally wrong
            .execute(ResponseBody::peek)
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract()
            .as(FileIngressResultJsonSpec.class);
        // ASSERT
        assertFileIngressResultError(result, "InvalidFilenameForFragebogenIngress");
    }

    @TestAsFileIngress
    @Test
    void file_ingress_no_dossier_found() {
        // ARRANGE
        Optional<Dossier> dossierOptional = findByEsadId("A00145625");
        assertThat(dossierOptional).isEmpty();
        File fragebogen = loadFragebogen("2025-01-01__A00145625.pdf");
        // ACT
        FileIngressResultJsonSpec result = api.fileIngressResourceUploadFragebogen()
            ._fileMultiPart(fragebogen)
            .esadIdPath("A00145625")
            .execute(ResponseBody::peek)
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract()
            .as(FileIngressResultJsonSpec.class);
        // ASSERT
        assertFileIngressResultError(result, "NoDossierForEsadId");
    }

    @TestAsFileIngress
    @Test
    void file_ingress_geburtsdatum_mismatch() {
        // ARRANGE
        String esadId = "A00145625";
        fixtureService.createDossier(LocalDate.now(), esadId);
        Optional<Dossier> dossierOptional = findByEsadId(esadId);
        assertThat(dossierOptional).isPresent();
        File fragebogen = loadFragebogen("2025-01-01__" + esadId + ".pdf");
        // ACT
        FileIngressResultJsonSpec result = api.fileIngressResourceUploadFragebogen()
            ._fileMultiPart(fragebogen)
            .esadIdPath(esadId)
            .execute(ResponseBody::peek)
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract()
            .as(FileIngressResultJsonSpec.class);
        // ASSERT
        assertFileIngressResultError(result, "FileIngressGeburtsdatumMissmatch");
    }

    @TestAsFileIngress
    @Test
    void file_ingress_no_einladung() {
        // ARRANGE
        String esadId = "A00145626";
        LocalDate geburtsdatum = LocalDate.of(2025, 1, 1);
        fixtureService.createDossier(geburtsdatum, esadId);
        Optional<Dossier> dossierOptional = findByEsadId(esadId);
        assertThat(dossierOptional).isPresent();
        File fragebogen = loadFragebogen(geburtsdatum.format(FileIngressService.DATE_TIME_FORMATTER) + "__" + esadId + ".pdf");
        // ACT
        FileIngressResultJsonSpec result = api.fileIngressResourceUploadFragebogen()
            ._fileMultiPart(fragebogen)
            .esadIdPath(esadId)
            .execute(ResponseBody::peek)
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract()
            .as(FileIngressResultJsonSpec.class);
        // ASSERT
        assertFileIngressResultError(result, "FileIngressNoSuitableAnlass");
    }

    @TestAsFileIngress
    @Test
    void file_ingress_only_impfanlass() {
        // ARRANGE
        String esadId = "A00145627";
        LocalDate geburtsdatum = LocalDate.of(2025, 1, 1);
        Dossier dossier = fixtureService.createDossier(geburtsdatum, esadId);
        fixtureService.createEinladung(dossier, AnlassTyp.IMPFTERMIN, DATE_EARLY, "theImpfterminAnlass");
        fixtureService.createEinladung(dossier, AnlassTyp.IMPFAKTION, DATE_EARLY, "theImpfaktionAnlass");
        fixtureService.createEinladung(dossier, AnlassTyp.IMPFSPRECHSTUNDE, DATE_EARLY, "theImpfsprechstundeAnlass");
        Optional<Dossier> dossierOptional = findByEsadId(esadId);
        assertThat(dossierOptional).isPresent();
        File fragebogen = loadFragebogen(geburtsdatum.format(FileIngressService.DATE_TIME_FORMATTER) + "__" + esadId + ".pdf");
        // ACT
        FileIngressResultJsonSpec result = api.fileIngressResourceUploadFragebogen()
            ._fileMultiPart(fragebogen)
            .esadIdPath(esadId)
            .execute(ResponseBody::peek)
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract()
            .as(FileIngressResultJsonSpec.class);
        // ASSERT
        assertFileIngressResultError(result, "FileIngressNoSuitableAnlass");
    }

    @TestAsFileIngress
    @Test
    void file_ingress_multiple_anlasstypen() {
        // ARRANGE
        String esadId = "A00145628";
        LocalDate geburtsdatum = LocalDate.of(2025, 1, 1);
        Dossier dossier = fixtureService.createDossier(geburtsdatum, esadId);
        fixtureService.createEinladung(dossier, AnlassTyp.IMPFTERMIN, DATE_EARLY, "theImpfterminAnlass");
        fixtureService.createEinladung(dossier, AnlassTyp.KLASSE_8, DATE_EARLY, "theKlasse8Anlass");
        fixtureService.createEinladung(dossier, AnlassTyp.KLASSE_9, DATE_EARLY, "theKlasse9Anlass");
        Optional<Dossier> dossierOptional = findByEsadId(esadId);
        assertThat(dossierOptional).isPresent();
        File fragebogen = loadFragebogen(geburtsdatum.format(FileIngressService.DATE_TIME_FORMATTER) + "__" + esadId + ".pdf");
        // ACT
        FileIngressResultJsonSpec result = api.fileIngressResourceUploadFragebogen()
            ._fileMultiPart(fragebogen)
            .esadIdPath(esadId)
            .execute(ResponseBody::peek)
            .then()
            .statusCode(Status.BAD_REQUEST.getStatusCode())
            .extract()
            .as(FileIngressResultJsonSpec.class);
        // ASSERT
        assertFileIngressResultError(result, "FileIngressNoSuitableAnlass");
    }

    @TestAsFileIngress
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
        File fragebogen = loadFragebogen(geburtsdatum.format(FileIngressService.DATE_TIME_FORMATTER) + "__" + esadId + ".pdf");
        // ACT
        FileIngressResultJsonSpec result = api.fileIngressResourceUploadFragebogen()
            ._fileMultiPart(fragebogen)
            .esadIdPath(esadId)
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

    @TestAsFileIngress
    @Test
    void file_ingress_success_only_multiple_anlass_same_anlasstyp() {
        // ARRANGE
        String esadId = "A00145630";
        LocalDate geburtsdatum = LocalDate.of(2025, 1, 1);
        Dossier dossier = fixtureService.createDossier(geburtsdatum, esadId);
        fixtureService.createEinladung(dossier, AnlassTyp.IMPFTERMIN, DATE_EARLY, "theImpfterminAnlass");
        fixtureService.createEinladung(dossier, AnlassTyp.KLASSE_8, DATE_EARLY, "theEarlyAnlass");
        Einladung einladung = fixtureService.createEinladung(dossier, AnlassTyp.KLASSE_8, DATE_LATE, "theLateAnlass");
        Optional<Dossier> dossierOptional = findByEsadId(esadId);
        assertThat(dossierOptional).isPresent();
        File fragebogen = loadFragebogen(geburtsdatum.format(FileIngressService.DATE_TIME_FORMATTER) + "__" + esadId + ".pdf");
        // ACT
        FileIngressResultJsonSpec result = api.fileIngressResourceUploadFragebogen()
            ._fileMultiPart(fragebogen)
            .esadIdPath(esadId)
            .execute(ResponseBody::peek)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(FileIngressResultJsonSpec.class);
        einladung = fixtureService.readEinladungWithFilesAndLabels(einladung.getId());
        // ASSERT
        assertFileIngressResultSuccess(
            result,
            "Die Datei 2025-01-01__A00145630.pdf wurde erfolgreich hochgeladen als 26-11-2025_Gerber-Emma_Fragebogen.pdf und angehängt an Dossier mit Esad-ID A0 01 456 30 im Anlass 2025/26 SU 2.SEK theLateAnlass.");
        assertThat(einladung.getFragebogenNotiz()).isPresent();
        assertThat(einladung.getFragebogenNotiz().get().getFiles()).hasSize(1);
        assertThat(einladung.getFragebogenNotiz().get().getBemerkungen()).isEqualTo("Datei 26-11-2025_Gerber-Emma_Fragebogen.pdf wurde am 26.11.2025 automatisch hochgeladen.");
    }

    @TestAsFileIngress
    @Test
    void file_ingress_success_notiz_already_exists_with_label_and_orphaned_metadata() {
        // ARRANGE
        String esadId = "A00145631";
        LocalDate geburtsdatum = LocalDate.of(2025, 1, 1);
        Dossier dossier = fixtureService.createDossier(geburtsdatum, esadId);
        Einladung einladung = fixtureService.createEinladung(dossier, AnlassTyp.KLASSE_8, DATE_EARLY, "theOnlyAnlass");
        Notiz existingNotiz = fixtureService.createFragebogenNotiz(einladung.getId());
        // Label Datenverlust anhaengen
        fixtureService.addLabelToNotiz(existingNotiz.getId(), Label.ID_LABEL_DATENVERLUST_NOTIZ);
        // Orphaned FileData anhaengen
        CleanFileName cleanFileName = new CleanFileName("MyOrphanedFileInS3.pdf");
        existingNotiz.getFiles().add(new FileData(
            FileTyp.FRAGEBOGEN,
            cleanFileName,
            FileInfo.of(cleanFileName, MediaType.APPLICATION_XML, 10L)));
        File fragebogen = loadFragebogen(geburtsdatum.format(FileIngressService.DATE_TIME_FORMATTER) + "__" + esadId + ".pdf");
        // ACT
        FileIngressResultJsonSpec result = api.fileIngressResourceUploadFragebogen()
            ._fileMultiPart(fragebogen)
            .esadIdPath(esadId)
            .execute(ResponseBody::peek)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(FileIngressResultJsonSpec.class);
        einladung = fixtureService.readEinladungWithFilesAndLabels(einladung.getId());
        // ASSERT
        assertFileIngressResultSuccess(
            result,
            "Die Datei 2025-01-01__A00145631.pdf wurde erfolgreich hochgeladen als 26-11-2025_Gerber-Emma_Fragebogen.pdf und angehängt an Dossier mit Esad-ID A0 01 456 31 im Anlass 2024/25 SU 2.SEK theOnlyAnlass.");
        assertThat(einladung.getFragebogenNotiz()).isPresent();
        assertThat(einladung.getFragebogenNotiz().get().getFiles()).hasSize(1);
        assertThat(einladung.getFragebogenNotiz().get().getBemerkungen()).isEqualTo("My Default Notiz Bemerkungen\n"
            + "Datei 26-11-2025_Gerber-Emma_Fragebogen.pdf wurde am 26.11.2025 automatisch hochgeladen.");
        // Label Datenverlust muss weg sein
        assertThat(einladung.getFragebogenNotiz().get().getLabels())
            .noneMatch(label -> label.getId().equals(Label.ID_LABEL_DATENVERLUST_NOTIZ));
        // Orphaned FileData muss weg sein
        assertThat(einladung.getFragebogenNotiz().get().getFiles())
            .noneMatch(fileData -> fileData.getFileInfoBucket().getFileName().getFileName().equals("MyOrphanedFileInS3.pdf"));
    }

    @NonNull
    private File loadFragebogen(@NonNull String desiredFilename) {
        File fragebogen = TestUtil.loadTestfileWithFilename("/FileUtilTest/pdfFileExample.pdf", desiredFilename);
        assertThat(fragebogen).exists();
        return fragebogen;
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
