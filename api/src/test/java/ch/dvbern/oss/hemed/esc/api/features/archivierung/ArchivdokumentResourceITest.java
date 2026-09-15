package ch.dvbern.oss.hemed.esc.api.features.archivierung;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import ch.dvbern.dvbstarter.shared.types.cleanfilename.CleanFileName;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileData;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileInfo;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileTyp;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.ArchivdokumentBuilder;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBE;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSchularztBE;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSupportBE;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.ArchivdokumentApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AppFailureErrorJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.ArchivdokumentListJsonSpec;

import io.quarkus.test.junit.QuarkusTest;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@RequiredArgsConstructor
@TestMethodOrder(OrderAnnotation.class)
class ArchivdokumentResourceITest extends AbstractITest {

    private final ArchivdokumentApiSpec api = ArchivdokumentApiSpec.archivdokument(RequestSpecUtil.quarkusSpec());
    private final FixtureService fixtureService;
    private final ArchivdokumentService archivdokumentService;
    private final BenutzerService benutzerService;
    private final TenantProducer tenantProducer;

    @Test
    @TestAsSupportBE
    @Order(10)
    void findAllOffeneArchivdokument_as_Support_find_all_with_correct_status() {
        //ARRANGE
        tenantProducer.setCurrentTenant(TenantEnum.BE);

        Archivdokument archivdokumentOffenI = ArchivdokumentBuilder.create(builder -> builder
            .withOwner(fixtureService.createBenutzerWithOrganisation())
            .withStatus(ArchivierungStatus.OFFEN)
            .withFiledata(fixtureService.createFileData()));
        archivdokumentService.persist(archivdokumentOffenI);

        Archivdokument archivdokumentOffenII = ArchivdokumentBuilder.create(builder -> builder
            .withOwner(fixtureService.createBenutzerWithOrganisation())
            .withStatus(ArchivierungStatus.OFFEN)
            .withFiledata(fixtureService.createFileData()));
        archivdokumentService.persist(archivdokumentOffenII);

        Archivdokument archivdokumentAbgeschlossen = ArchivdokumentBuilder.create(builder -> builder
            .withOwner(fixtureService.createBenutzerWithOrganisation())
            .withStatus(ArchivierungStatus.DOWNLOADED)
            .withFiledata(fixtureService.createFileData()));
        archivdokumentService.persist(archivdokumentAbgeschlossen);

        //ACT
        ArchivdokumentListJsonSpec result = api.archivdokumentResourceFindAllArchivdokumente()
            .onlyOpenQuery(true)
            .execute(Function.identity())
            .then()
            .assertThat()
            .statusCode(Response.Status.OK.getStatusCode())
            .extract()
            .as(ArchivdokumentListJsonSpec.class);

        //ASSERT
        assertThat(result.getArchivdokumentList())
            .isNotEmpty()
            .anyMatch(resultItem ->
                archivdokumentOffenII.getId().getId().equals(resultItem.getId()))
            .anyMatch(resultItem -> archivdokumentOffenI.getId().getId().equals(resultItem.getId()));
        assertThat(result.getArchivdokumentList()).hasSize(2);
    }

    @Test
    @TestAsAdminBE
    @Order(20)
    void findAllAbgeschlosseneArchivdokument_finds_own_with_correct_status() {
        //ARRANGE
        tenantProducer.setCurrentTenant(TenantEnum.BE);

        var adminId = ID.of(UUID.fromString(TestAsAdminBE.BENUTZER_ID), Benutzer.class);
        var currentBenutzer = benutzerService.getById(adminId);

        Archivdokument archivdokument = ArchivdokumentBuilder.create(builder -> builder
            .withOwner(currentBenutzer)
            .withStatus(ArchivierungStatus.DOWNLOADED)
            .withFiledata(fixtureService.createFileData()));
        archivdokumentService.persist(archivdokument);

        //ACT
        ArchivdokumentListJsonSpec result = api.archivdokumentResourceFindAllArchivdokumente()
            .execute(Function.identity())
            .then()
            .assertThat()
            .statusCode(Response.Status.OK.getStatusCode())
            .extract()
            .as(ArchivdokumentListJsonSpec.class);

        //ASSERT
        assertThat(result.getArchivdokumentList())
            .isNotEmpty()
            .anyMatch(resultItem ->
                archivdokument.getId().getId().equals(resultItem.getId()));
    }

    @Test
    @TestAsAdminBE
    @Order(30)
    void findAllOffeneArchivdokument_finds_own_with_correct_status() {
        //ARRANGE
        tenantProducer.setCurrentTenant(TenantEnum.BE);

        var adminId = ID.of(UUID.fromString(TestAsAdminBE.BENUTZER_ID), Benutzer.class);
        var currentBenutzer = benutzerService.getById(adminId);

        Archivdokument archivdokument_offen_I = ArchivdokumentBuilder.create(builder -> builder
            .withOwner(currentBenutzer)
            .withStatus(ArchivierungStatus.OFFEN)
            .withFiledata(fixtureService.createFileData()));
        archivdokumentService.persist(archivdokument_offen_I);

        Archivdokument archivdokument_offen_II = ArchivdokumentBuilder.create(builder -> builder
            .withOwner(fixtureService.createBenutzerWithOrganisation())
            .withStatus(ArchivierungStatus.OFFEN)
            .withFiledata(fixtureService.createFileData()));
        archivdokumentService.persist(archivdokument_offen_II);

        Archivdokument archivdokument_abgeschlossen = ArchivdokumentBuilder.create(builder -> builder
            .withOwner(fixtureService.createBenutzerWithOrganisation())
            .withStatus(ArchivierungStatus.DOWNLOADED)
            .withFiledata(fixtureService.createFileData()));
        archivdokumentService.persist(archivdokument_abgeschlossen);

        //ACT
        ArchivdokumentListJsonSpec result = api.archivdokumentResourceFindAllArchivdokumente()
            .onlyOpenQuery(true)
            .execute(Function.identity())
            .then()
            .assertThat()
            .statusCode(Response.Status.OK.getStatusCode())
            .extract()
            .as(ArchivdokumentListJsonSpec.class);

        //ASSERT
        assertThat(result.getArchivdokumentList())
            .isNotEmpty()
            .anyMatch(resultItem ->
                archivdokument_offen_I.getId().getId().equals(resultItem.getId()));
        assertThat(result.getArchivdokumentList()).hasSize(1);
    }

    @Test
    @TestAsSupportBE
    @Order(40)
    void downloadArchivdokument_works_and_sets_status_and_timestamtp() {
        tenantProducer.setCurrentTenant(TenantEnum.BE);

        Archivdokument archivdokument = ArchivdokumentBuilder.create(builder -> builder
            .withOwner(fixtureService.createBenutzerWithOrganisation())
            .withStatus(ArchivierungStatus.OFFEN)
            .withArchivierungTyp(ArchivierungTyp.ZUSTAENDIGER_ARZT_CHANGED)
            .withFiledata(fixtureService.createFileData()));
        archivdokumentService.persist(archivdokument);

        assertThat(archivdokument.getArchivierungStatus()).isEqualTo(ArchivierungStatus.OFFEN);
        assertThat(archivdokument.getTimestampDownloaded()).isNull();

        // ACT
        var success = api.archivdokumentResourceDownloadArchivFile()
            .archivdokumentIdPath(archivdokument.getId().getId())
            .execute(Function.identity())
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().asString();

        // ASSERT
        Assertions.assertThat(success).isNotNull();

        Archivdokument archivDokumentAfterDownload = archivdokumentService.getById(archivdokument.getId());

        assertThat(archivDokumentAfterDownload.getArchivierungStatus()).isEqualTo(ArchivierungStatus.DOWNLOADED);
        assertThat(archivDokumentAfterDownload.getTimestampDownloaded()).isNotNull();
    }

    @Test
    @TestAsSupportBE
    @Order(50)
    void downloadArchivdokument_works_and_sets_status_for_kind_deaktiviert() {
        tenantProducer.setCurrentTenant(TenantEnum.BE);

        Archivdokument archivdokument = ArchivdokumentBuilder.create(builder -> builder
            .withOwner(fixtureService.createBenutzerWithOrganisation())
            .withStatus(ArchivierungStatus.OFFEN)
            .withArchivierungTyp(ArchivierungTyp.KIND_DEAKTIVIERT)
            .withFiledata(fixtureService.createFileData()));
        archivdokumentService.persist(archivdokument);

        assertThat(archivdokument.getArchivierungStatus()).isEqualTo(ArchivierungStatus.OFFEN);
        assertThat(archivdokument.getTimestampDownloaded()).isNull();

        // ACT
        api.archivdokumentResourceDownloadArchivFile()
            .archivdokumentIdPath(archivdokument.getId().getId())
            .execute(Function.identity())
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().asString();

        // ASSERT
        Archivdokument archivDokumentAfterDownload = archivdokumentService.getById(archivdokument.getId());
        assertThat(archivDokumentAfterDownload.getArchivierungStatus()).isEqualTo(ArchivierungStatus.DOWNLOADED_READY_FOR_DELETION);
    }

    @Test
    @TestAsAdminBE
    @Order(60)
    void downloadArchivdokument_throws_if_owner_is_not_current_benutzer() {
        //ARRANGE
        tenantProducer.setCurrentTenant(TenantEnum.BE);

        Archivdokument archivdokument = ArchivdokumentBuilder.create(builder -> builder
            .withOwner(fixtureService.createBenutzerWithOrganisation())
            .withStatus(ArchivierungStatus.OFFEN)
            .withFiledata(fixtureService.createFileData()));
        archivdokumentService.persist(archivdokument);

        //ACT
        AppFailureErrorJsonSpec failure = api.archivdokumentResourceDownloadArchivFile()
            .archivdokumentIdPath(archivdokument.getId().getId())
            .execute(Function.identity())
            .then()
            .assertThat()
            .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
            .extract().as(AppFailureErrorJsonSpec.class);

        // ASSERT
        assertThat(failure).isNotNull();
        assertThat(failure.getSummary()).contains("hat keine Berechtigung für Entity");
    }

    @Test
    @TestAsSchularztBE
    @Order(70)
    void bulkDownloadArchivFiles_returns_ok() throws IOException {
        //ARRANGE
        tenantProducer.setCurrentTenant(TenantEnum.BE);
        Benutzer currentSchularzt = benutzerService.getById(TestAsSchularztBE.BENUTZER_ID_ID);

        Archivdokument archivdokument1 = ArchivdokumentBuilder.create(builder -> builder
            .withOwner(currentSchularzt)
            .withStatus(ArchivierungStatus.OFFEN)
            .withFiledata(fixtureService.createFileData(TestUtil.loadTestPDF(), "bulkDownloadArchivFiles_returns_ok_1")));
        archivdokumentService.persist(archivdokument1);

        Archivdokument archivdokument2 = ArchivdokumentBuilder.create(builder -> builder
            .withOwner(currentSchularzt)
            .withStatus(ArchivierungStatus.OFFEN)
            .withFiledata(fixtureService.createFileData(TestUtil.loadTestPDF(), "bulkDownloadArchivFiles_returns_ok_2")));
        archivdokumentService.persist(archivdokument2);

        //ACT
        byte[] result = api.archivdokumentResourceBulkDownloadArchivFiles()
            .execute(Function.identity())
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .header("Content-Disposition", Matchers.containsString("attachment;filename=\"archivdokumente_"))
            .header("Content-Disposition", Matchers.endsWith(".zip\""))
            .header("Content-Type", "application/zip")
            // the zip is streamed from a temp file, so unlike other downloads its length is known up front and the
            // browser can show real progress - guard that it does not silently fall back to chunked
            .header("Content-Length", Matchers.notNullValue())
            .extract().asByteArray();

        //ASSERT
        assertThat(result).isNotNull();
        List<String> entryNames = extractZipEntryNames(result);
        assertThat(entryNames).hasSize(2)
            .allMatch(name -> name.startsWith("bulkDownloadArchivFiles_returns_ok"))
            .allMatch(name -> name.endsWith(".pdf"));

        Archivdokument doc1AfterDownload = archivdokumentService.getById(archivdokument1.getId());
        assertThat(doc1AfterDownload.getArchivierungStatus()).isEqualTo(ArchivierungStatus.DOWNLOADED_READY_FOR_DELETION);

        Archivdokument doc2AfterDownload  = archivdokumentService.getById(archivdokument2.getId());
        assertThat(doc2AfterDownload.getArchivierungStatus()).isEqualTo(ArchivierungStatus.DOWNLOADED_READY_FOR_DELETION);
    }

    @Test
    @TestAsSchularztBE
    @Order(71)
    void bulkDownloadArchivFiles_respects_max_files_cap() throws IOException {
        //ARRANGE
        tenantProducer.setCurrentTenant(TenantEnum.BE);
        Benutzer currentSchularzt = benutzerService.getById(TestAsSchularztBE.BENUTZER_ID_ID);

        // create 3 open documents; the %test profile caps esc.archive.bulk-download.max-files at 2
        Archivdokument doc1 = ArchivdokumentBuilder.create(builder -> builder
            .withOwner(currentSchularzt)
            .withStatus(ArchivierungStatus.OFFEN)
            .withFiledata(fixtureService.createFileData(TestUtil.loadTestPDF(), "bulkDownloadArchivFiles_respects_max_files_cap1")));
        archivdokumentService.persist(doc1);

        Archivdokument doc2 = ArchivdokumentBuilder.create(builder -> builder
            .withOwner(currentSchularzt)
            .withStatus(ArchivierungStatus.OFFEN)
            .withFiledata(fixtureService.createFileData(TestUtil.loadTestPDF(), "bulkDownloadArchivFiles_respects_max_files_cap2")));
        archivdokumentService.persist(doc2);

        Archivdokument doc3 = ArchivdokumentBuilder.create(builder -> builder
            .withOwner(currentSchularzt)
            .withStatus(ArchivierungStatus.OFFEN)
            .withFiledata(fixtureService.createFileData(TestUtil.loadTestPDF(), "bulkDownloadArchivFiles_respects_max_files_cap3")));
        archivdokumentService.persist(doc3);

        //ACT fetch first batch of archived documents
        byte[] result = api.archivdokumentResourceBulkDownloadArchivFiles()
            .execute(Function.identity())
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().asByteArray();

        //ASSERT
        List<String> entryNames = extractZipEntryNames(result);
        assertThat(entryNames).hasSize(2);

        //ACT fetch remaining archived documents
        byte[] resultRemaining = api.archivdokumentResourceBulkDownloadArchivFiles()
            .execute(Function.identity())
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().asByteArray();

        //ASSERT
        List<String> entryNamesRemaining = extractZipEntryNames(resultRemaining);
        assertThat(entryNamesRemaining).hasSize(1)
            .allMatch(name -> name.startsWith("bulkDownloadArchivFiles_respects_max_files_cap"));


        Archivdokument doc1AfterDownload = archivdokumentService.getById(doc1.getId());
        assertThat(doc1AfterDownload.getArchivierungStatus()).isEqualTo(ArchivierungStatus.DOWNLOADED_READY_FOR_DELETION);

        Archivdokument doc2AfterDownload  = archivdokumentService.getById(doc2.getId());
        assertThat(doc2AfterDownload.getArchivierungStatus()).isEqualTo(ArchivierungStatus.DOWNLOADED_READY_FOR_DELETION);

        Archivdokument doc3AfterDownload  = archivdokumentService.getById(doc3.getId());
        assertThat(doc3AfterDownload.getArchivierungStatus()).isEqualTo(ArchivierungStatus.DOWNLOADED_READY_FOR_DELETION);
    }

    @Test
    @TestAsSchularztBE
    @Order(72)
    void bulkDownloadArchivFiles_skips_documents_with_broken_s3_reference() throws IOException {
        //ARRANGE
        tenantProducer.setCurrentTenant(TenantEnum.BE);
        Benutzer currentSchularzt = benutzerService.getById(TestAsSchularztBE.BENUTZER_ID_ID);

        Archivdokument validDoc = ArchivdokumentBuilder.create(builder -> builder
            .withOwner(currentSchularzt)
            .withStatus(ArchivierungStatus.OFFEN)
            .withFiledata(fixtureService.createFileData(TestUtil.loadTestPDF(), "bulkDownloadArchivFiles_skips_documents_with_broken_s3_reference")));
        archivdokumentService.persist(validDoc);

        // a document whose FileInfo points to a non-existent S3 key - the S3 fetch fails and
        // S3ZipWriter.addFromS3IfAvailable is expected to log and skip it
        FileData brokenFileData = new FileData(
            FileTyp.KINDERDOSSIER,
            new CleanFileName("broken.pdf"),
            FileInfo.of(
                CleanFileName.parse("broken.pdf"),
                org.apache.tika.mime.MediaType.parse("application/pdf"),
                42
            )
        );
        brokenFileData.getFileInfoBucket().setFileIdInBucket("nonexistent-s3-key-for-testing");

        Archivdokument brokenDoc = ArchivdokumentBuilder.create(builder -> builder
            .withOwner(currentSchularzt)
            .withStatus(ArchivierungStatus.OFFEN)
            .withFiledata(brokenFileData));
        archivdokumentService.persist(brokenDoc);

        //ACT
        byte[] result = api.archivdokumentResourceBulkDownloadArchivFiles()
            .execute(Function.identity())
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().asByteArray();

        //ASSERT
        List<String> entryNames = extractZipEntryNames(result);
        assertThat(entryNames).hasSize(1)
            .allMatch(name -> name.startsWith("bulkDownloadArchivFiles_skips_documents_with_broken_s3_reference"))
            .allMatch(name -> name.endsWith(".pdf"));

        Archivdokument validDocAfterDownload = archivdokumentService.getById(validDoc.getId());
        assertThat(validDocAfterDownload.getArchivierungStatus()).isEqualTo(ArchivierungStatus.DOWNLOADED_READY_FOR_DELETION);

        Archivdokument brokenDocAfterDownload = archivdokumentService.getById(brokenDoc.getId());
        assertThat(brokenDocAfterDownload.getArchivierungStatus()).isEqualTo(ArchivierungStatus.OFFEN);
    }

    @Test
    @TestAsSchularztBE
    @Order(73)
    void bulkDownloadArchivFiles_no_archive_documents_to_download() throws IOException {
        //ARRANGE
        tenantProducer.setCurrentTenant(TenantEnum.BE);

        //ACT
        byte[] result = api.archivdokumentResourceBulkDownloadArchivFiles()
            .execute(Function.identity())
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract().asByteArray();

        //ASSERT
        List<String> entryNames = extractZipEntryNames(result);
        assertThat(entryNames).isEmpty();
    }

    @Test
    @TestAsSchularztBE
    @Order(74)
    void bulkDownloadArchivFiles_only_download_own_archives() throws IOException {
        //ARRANGE
        tenantProducer.setCurrentTenant(TenantEnum.BE);

        Archivdokument archivdokument1 = ArchivdokumentBuilder.create(builder -> builder
            .withOwner(fixtureService.createBenutzerWithOrganisation())
            .withStatus(ArchivierungStatus.OFFEN)
            .withFiledata(fixtureService.createFileData(TestUtil.loadTestPDF(), "bulkDownloadArchivFiles_only_download_own_archives")));
        archivdokumentService.persist(archivdokument1);

        //ACT
        byte[] result = api.archivdokumentResourceBulkDownloadArchivFiles()
            .execute(Function.identity())
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .header("Content-Disposition", Matchers.containsString("attachment;filename=\"archivdokumente_"))
            .header("Content-Disposition", Matchers.endsWith(".zip\""))
            .header("Content-Type", "application/zip")
            .extract().asByteArray();


        //ASSERT
        assertThat(result).isNotNull();
        List<String> entryNames = extractZipEntryNames(result);
        assertThat(entryNames).isEmpty();
    }

    @Test
    @TestAsSupportBE
    @Order(75)
    void bulkDownloadArchivFiles_bulk_download_archives_not_allowed_fo_support() throws IOException {
        //ARRANGE
        tenantProducer.setCurrentTenant(TenantEnum.BE);

        Archivdokument archivdokument1 = ArchivdokumentBuilder.create(builder -> builder
            .withOwner(fixtureService.createBenutzerWithOrganisation())
            .withStatus(ArchivierungStatus.OFFEN)
            .withFiledata(fixtureService.createFileData(TestUtil.loadTestPDF(), "bulkDownloadArchivFiles_bulk_download_archives_not_allowed_fo_support_1")));
        archivdokumentService.persist(archivdokument1);

        Archivdokument archivdokument2 = ArchivdokumentBuilder.create(builder -> builder
            .withOwner(fixtureService.createBenutzerWithOrganisation())
            .withStatus(ArchivierungStatus.OFFEN)
            .withFiledata(fixtureService.createFileData(TestUtil.loadTestPDF(), "bulkDownloadArchivFiles_bulk_download_archives_not_allowed_fo_support_2")));
        archivdokumentService.persist(archivdokument2);

        //ACT
        byte[] result = api.archivdokumentResourceBulkDownloadArchivFiles()
            .execute(Function.identity())
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .header("Content-Disposition", Matchers.containsString("attachment;filename=\"archivdokumente_"))
            .header("Content-Disposition", Matchers.endsWith(".zip\""))
            .header("Content-Type", "application/zip")
            .extract().asByteArray();

        //ASSERT
        assertThat(result).isNotNull();
        List<String> entryNames = extractZipEntryNames(result);
        assertThat(entryNames).isEmpty();
    }

    private static List<String> extractZipEntryNames(byte[] zipBytes) throws IOException {
        List<String> names = new ArrayList<>();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                names.add(entry.getName());
                zis.closeEntry();
            }
        }
        return names;
    }
}


