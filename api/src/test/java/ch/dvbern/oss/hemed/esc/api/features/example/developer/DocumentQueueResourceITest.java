package ch.dvbern.oss.hemed.esc.api.features.example.developer;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.fileio.FileNameUtil;
import ch.dvbern.dvbstarter.shared.i18n.datetime.DateConst;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.Briefvorlage;
import ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.BriefvorlageType;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.DocumentQueue;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.DocumentQueueMapper;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.DocumentQueueRepo;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.json.DocumentQueueDisplayJson;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.json.DocumentQueueListJson;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileTyp;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSupportBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.DocumentQueueApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.SuperAdminRunnerApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.CsvReportTypeSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.DocumentQueueCsvListSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.DocumentQueueCsvParamsSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.DocumentQueueCsvSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.DocumentQueueDisplayJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.DocumentQueueIDListJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.DocumentQueueListJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.DocumentQueueListSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.DocumentQueueMassenbriefSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.DocumentQueueParamsSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.DocumentQueueSpssSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.FileTypSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.SpssReportTypeSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import io.restassured.response.ResponseOptions;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static ch.dvbern.dvbstarter.shared.i18n.datetime.DateConst.ZONE_ID;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@RequiredArgsConstructor
class DocumentQueueResourceITest extends AbstractITest {

    private final DocumentQueueApiSpec api = DocumentQueueApiSpec.documentQueue(RequestSpecUtil.quarkusSpec());
    private final SuperAdminRunnerApiSpec systemApi =
        SuperAdminRunnerApiSpec.superAdminRunner(RequestSpecUtil.quarkusSpec());
    private final DocumentQueueMapper documentQueueMapper;
    private final DocumentQueueRepo documentQueueRepo;
    private final FixtureService fixtureService;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    @TestAsAdminBS
    void addPlaygroundDocumentQueue() {
        // ARRANGE
        DocumentQueueListJson initialDocumentQueue = documentQueueMapper.findAllByCurrentBenutzer();

        // & ACT
        api.documentQueueResourceCreate().body(new DocumentQueueSpssSaveJsonSpec(FileTypSpec.PLAYGROUND, null))
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.CREATED.getStatusCode());

        // ASSERT
        DocumentQueueListJson newDocumentQueue = documentQueueMapper.findAllByCurrentBenutzer();

        assertThat(newDocumentQueue).isNotNull();
        assertThat(newDocumentQueue.getList()).hasSize(initialDocumentQueue.getList().size() + 1);
        assertThat(newDocumentQueue.getList().stream()).anyMatch(document -> document.getType() == FileTyp.PLAYGROUND);
    }

    @Test
    @TestAsSupportBS
    void findAllDocumentQueue() {
        // ARRANGE
        // Clean out the queue to prevent differences between find and findAll as the lists get compared.
        List<DocumentQueue> all = documentQueueRepo.findAll();
        for (DocumentQueue document : all) {
            documentQueueRepo.remove(document.getId());
        }

        api.documentQueueResourceCreate().body(new DocumentQueueSpssSaveJsonSpec(FileTypSpec.PLAYGROUND_ERROR, null))
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.CREATED.getStatusCode());

        // ACT
        DocumentQueueListJsonSpec resultForUser = api.documentQueueResourceFindAllByCurrentBenutzer()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Response.Status.OK.getStatusCode())
            .extract()
            .as(DocumentQueueListJsonSpec.class);

        DocumentQueueListJsonSpec allResults = api.documentQueueResourceFindAll()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Response.Status.OK.getStatusCode())
            .extract()
            .as(DocumentQueueListJsonSpec.class);

        // ASSERT
        assertThat(resultForUser).isNotNull();
        assertThat(resultForUser.getList()).isNotEmpty();
        assertThat(resultForUser.getList())
            .anyMatch(document -> document.getType() == FileTypSpec.PLAYGROUND_ERROR);
        assertThat(allResults).isEqualTo(resultForUser);
    }

    @Test
    @TestAsSupportBS
    void downloadFile() {
        // ARRANGE
        // Clean out the queue to significantly reduce testing time.
        List<DocumentQueue> all = documentQueueRepo.findAll();
        for (DocumentQueue document : all) {
            documentQueueRepo.remove(document.getId());
        }

        api.documentQueueResourceCreate().body(new DocumentQueueSpssSaveJsonSpec(FileTypSpec.PLAYGROUND, null))
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.CREATED.getStatusCode());

        systemApi.superAdminRunnerResourceTriggerDocumentQueueJob()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());

        ID<DocumentQueue> id = documentQueueMapper.findAllByCurrentBenutzer()
            .getList()
            .stream()
            .filter(item -> item.getType() == FileTyp.PLAYGROUND)
            .findAny()
            .orElseThrow()
            .getId();

        String formattedToday =
            getAppClock().localDateTime().now().toLocalDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        String fileName = formattedToday + "_Playground.pdf";

        // ACT
        byte[] image = api.documentQueueResourceDownloadFile()
            .documentQueueIdPath(id.getId())
            .execute(ResponseOptions::andReturn)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .header("Content-Disposition", "attachment;filename=\"" + fileName + '"')
            .extract()
            .asByteArray();

        // ASSERT
        assertThat(image).isNotNull();
    }

    @Test
    @TestAsSupportBS
    void downloadMassenbriefZIPFile() {
        // ARRANGE
        // Clean out the queue to significantly reduce testing time.
        List<DocumentQueue> all = documentQueueRepo.findAll();
        for (DocumentQueue document : all) {
            documentQueueRepo.remove(document.getId());
        }

        // Prepare the data to create a Massenbrief
        Kind kind1 = fixtureService.createKind("Muster", "Max");
        Kind kind2 = fixtureService.createKind("Muster", "Moritz");
        Briefvorlage briefvorlage = fixtureService.createBriefvorlage(BriefvorlageType.KIND);
        DocumentQueueMassenbriefSaveJsonSpec saveJsonSpec = new DocumentQueueMassenbriefSaveJsonSpec(
            briefvorlage.getId().getId(),
            null,
            List.of(kind1.getId().getId(), kind2.getId().getId())
        );
        String formattedToday =
            getAppClock().localDateTime().now().toLocalDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        String fileName = formattedToday + "_Massenbrief-" + briefvorlage.getName() + ".zip";

        // Create entry for Queue
        api.documentQueueResourceCreateMassenbrief()
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.CREATED.getStatusCode());
        // manually trigger queue
        systemApi.superAdminRunnerResourceTriggerDocumentQueueJob()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());
        // Get id of newly generated ZIP
        ID<DocumentQueue> id = documentQueueMapper.findAllByCurrentBenutzer()
            .getList()
            .stream()
            .filter(item -> item.getType() == FileTyp.MASSENBRIEF_ZIP)
            .findAny()
            .orElseThrow()
            .getId();

        // ACT
        // download the ZIP-File
        byte[] image = api.documentQueueResourceDownloadFile()
            .documentQueueIdPath(id.getId())
            .execute(ResponseOptions::andReturn)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .header("Content-Disposition", "attachment;filename=\"" + fileName + '"')
            .extract()
            .asByteArray();

        // ASSERT
        assertThat(image).isNotNull();
    }

    @Test
    @TestAsSupportBS
    void downloadMassenbriefZIPFile_for500Kinder() {
        // ARRANGE
        // Clean out the queue to significantly reduce testing time.
        List<DocumentQueue> all = documentQueueRepo.findAll();
        for (DocumentQueue document : all) {
            documentQueueRepo.remove(document.getId());
        }

        // Prepare the data to create a Massenbrief
        List<String> kinderIdList = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            kinderIdList.add(fixtureService.createKind("Meier", "Max" + i).getId().getId());
        }
        Briefvorlage briefvorlage = fixtureService.createBriefvorlage(BriefvorlageType.KIND);
        DocumentQueueMassenbriefSaveJsonSpec saveJsonSpec = new DocumentQueueMassenbriefSaveJsonSpec(
            briefvorlage.getId().getId(),
            null,
            kinderIdList
        );
        String formattedToday =
            getAppClock().localDateTime().now().toLocalDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        String fileName = formattedToday + "_Massenbrief-" + briefvorlage.getName() + ".zip";

        // Create entry for Queue
        api.documentQueueResourceCreateMassenbrief()
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.CREATED.getStatusCode());
        // manually trigger queue
        systemApi.superAdminRunnerResourceTriggerDocumentQueueJob()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());
        // Get id of newly generated ZIP
        ID<DocumentQueue> id = documentQueueMapper.findAllByCurrentBenutzer()
            .getList()
            .stream()
            .filter(item -> item.getType() == FileTyp.MASSENBRIEF_ZIP)
            .findAny()
            .orElseThrow()
            .getId();

        // ACT
        // download the ZIP-File
        byte[] image = api.documentQueueResourceDownloadFile()
            .documentQueueIdPath(id.getId())
            .execute(ResponseOptions::andReturn)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .header("Content-Disposition", "attachment;filename=\"" + fileName + '"')
            .extract()
            .asByteArray();

        // ASSERT
        assertThat(image).isNotNull();
    }

    @Test
    @TestAsSupportBS
    void downloadMassenbriefZIPFile_fails_after_1_week() {
        // ARRANGE
        // Clean out the queue to significantly reduce testing time.
        List<DocumentQueue> all = documentQueueRepo.findAll();
        for (DocumentQueue document : all) {
            documentQueueRepo.remove(document.getId());
        }

        // Prepare the data to create a Massenbrief
        Kind kind1 = fixtureService.createKind("Muster", "Max");
        Kind kind2 = fixtureService.createKind("Muster", "Moritz");
        Briefvorlage briefvorlage = fixtureService.createBriefvorlage(BriefvorlageType.KIND);
        DocumentQueueMassenbriefSaveJsonSpec saveJsonSpec = new DocumentQueueMassenbriefSaveJsonSpec(
            briefvorlage.getId().getId(),
            null,
            List.of(kind1.getId().getId(), kind2.getId().getId())
        );

        // Create the Massenbrief
        api.documentQueueResourceCreateMassenbrief()
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.CREATED.getStatusCode());
        systemApi.superAdminRunnerResourceTriggerDocumentQueueJob()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());
        // Get id of Massenbrief
        ID<DocumentQueue> id = documentQueueMapper.findAllByCurrentBenutzer()
            .getList()
            .stream()
            .filter(item -> item.getType() == FileTyp.MASSENBRIEF_ZIP)
            .findAny()
            .orElseThrow()
            .getId();

        // Download Massenbrief
        String formattedToday =
            getAppClock().localDateTime().now().toLocalDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        String fileName = formattedToday + "_Massenbrief-" + briefvorlage.getName() + ".zip";
        byte[] image = api.documentQueueResourceDownloadFile()
            .documentQueueIdPath(id.getId())
            .execute(ResponseOptions::andReturn)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .header("Content-Disposition", "attachment;filename=\"" + fileName + '"')
            .extract()
            .asByteArray();
        assertThat(image).isNotNull();

        // Time Travel for DocumentQueueCleanUp
        LocalDate plus8days = LocalDate.now(ZONE_ID).plusDays(8);
        setTime(OffsetDateTime.ofInstant(
            plus8days.atStartOfDay().atZone(DateConst.ZONE_ID).toInstant(),
            DateConst.ZONE_ID));
        // Cleanup Documents older than 1 Week
        systemApi.superAdminRunnerResourceTriggerDocumentQueueCleanUpJob()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());
        // Act & Assert
        // Expecting 500 Interal Server Error, File shouldn't be found because automatic deletion worked successfully
        api.documentQueueResourceDownloadFile()
            .documentQueueIdPath(id.getId())
            .execute(ResponseOptions::andReturn)
            .then()
            .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    @TestAsSupportBS
    void addMultiplePlaygroundDocumentQueue() {
        // ARRANGE & ACT
        List<DocumentQueueSpssSaveJsonSpec> documentQueueSaveJsons = Arrays.asList(
            new DocumentQueueSpssSaveJsonSpec(FileTypSpec.PLAYGROUND, null),
            new DocumentQueueSpssSaveJsonSpec(
                FileTypSpec.SPSS_REPORT,
                new DocumentQueueParamsSpec(SpssReportTypeSpec.KINDERGARTEN)));

        DocumentQueueListSaveJsonSpec documentQueueListSaveJsonSpec =
            new DocumentQueueListSaveJsonSpec(documentQueueSaveJsons);

        DocumentQueueListJson documentQueue = documentQueueMapper.findAllByCurrentBenutzer();
        int currentSize = documentQueue.getList().size();

        api.documentQueueResourceCreateMultipleSpss().body(documentQueueListSaveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.CREATED.getStatusCode());

        // ASSERT
        documentQueue = documentQueueMapper.findAllByCurrentBenutzer();

        assertThat(documentQueue).isNotNull();
        assertThat(documentQueue.getList()).hasSize(currentSize + 2);
    }

    @Test
    @TestAsSupportBS
    void addAndDownloadCsvDocumentQueue() {
        // ARRANGE
        List<DocumentQueueCsvSaveJsonSpec> documentQueueCsvSaveJsons = Arrays.asList(
            new DocumentQueueCsvSaveJsonSpec(
                FileTypSpec.CSV_REPORT,
                LocalDate.of(2025, 11, 1),
                LocalDate.of(2025, 12, 31),
                new DocumentQueueCsvParamsSpec(CsvReportTypeSpec.SCHULUNTERSUCHUNG)));
        String expectedDisplayName = "Schuluntersuchung";
        String formattedDate = getAppClock().getNow().toLocalDate().format(FileNameUtil.DATE_FORMATTER_FOR_FILENAME);
        String expectedFileName = formattedDate + "_CSV-Report-Schuluntersuchung.csv";
        DocumentQueueCsvListSaveJsonSpec documentQueueCsvListSaveJsonSpec =
            new DocumentQueueCsvListSaveJsonSpec(documentQueueCsvSaveJsons);
        List<DocumentQueueDisplayJson> displayJsonList = documentQueueMapper.findAllByCurrentBenutzer().getList();
        int expectedSize = displayJsonList.size() + 1;

        // ACT - Create DocumentQueue Entry
        api.documentQueueResourceCreateMultipleCsv().body(documentQueueCsvListSaveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.CREATED.getStatusCode());

        // ASSERT - Created
        displayJsonList = new ArrayList<>(documentQueueMapper.findAllByCurrentBenutzer().getList());
        displayJsonList.sort(Comparator.comparing(
            displayJson ->
                displayJson.getEntityInfo().getTimestampErstellt(),
            Comparator.nullsLast(Comparator.reverseOrder())));
        assertThat(displayJsonList).isNotNull().hasSize(expectedSize);
        assertThat(displayJsonList.getFirst().getDisplayname()).isEqualTo(expectedDisplayName);

        // manually trigger queue
        systemApi.superAdminRunnerResourceTriggerDocumentQueueJob()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());
        // Get id of newly generated ZIP
        ID<DocumentQueue> id = documentQueueMapper.findAllByCurrentBenutzer()
            .getList()
            .stream()
            .filter(item -> item.getType() == FileTyp.CSV_REPORT)
            .findAny()
            .orElseThrow()
            .getId();

        // ACT
        // download the ZIP-File
        byte[] image = api.documentQueueResourceDownloadFile()
            .documentQueueIdPath(id.getId())
            .execute(ResponseOptions::andReturn)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .header("Content-Disposition", "attachment;filename=\"" + expectedFileName + '"')
            .extract()
            .asByteArray();

        // ASSERT
        assertThat(image).isNotNull();
    }

    @Test
    @TestAsAdminBS
    void addMassenbriefToDocumentQueue() {
        // Arrange
        Kind kind1 = fixtureService.createKind("Muster", "Max");
        Kind kind2 = fixtureService.createKind("Muster", "Moritz");
        Briefvorlage briefvorlage = fixtureService.createBriefvorlage(BriefvorlageType.KIND);
        String expectedDisplayName = briefvorlage.getName() + " Massenbrief";
        DocumentQueueMassenbriefSaveJsonSpec saveJsonSpec = new DocumentQueueMassenbriefSaveJsonSpec(
            briefvorlage.getId().getId(),
            null,
            List.of(kind1.getId().getId(), kind2.getId().getId())
        );
        List<DocumentQueueDisplayJson> displayJsonList = documentQueueMapper.findAllByCurrentBenutzer().getList();
        int expectedSize = displayJsonList.size() + 1;

        // Act
        api.documentQueueResourceCreateMassenbrief()
            .body(saveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.CREATED.getStatusCode());

        // ASSERT
        displayJsonList = new ArrayList<>(documentQueueMapper.findAllByCurrentBenutzer().getList());
        displayJsonList.sort(Comparator.comparing(
            displayJson ->
                displayJson.getEntityInfo().getTimestampErstellt(),
            Comparator.nullsLast(Comparator.reverseOrder())));
        assertThat(displayJsonList).isNotNull().hasSize(expectedSize);
        assertThat(displayJsonList.getFirst().getDisplayname()).isEqualTo(expectedDisplayName);
    }

    @Test
    @TestAsSupportBS
    @SuppressWarnings({
        "java:S5841" /* can not be sure if empty, just know docs of user are gone*/ }
    )
    void removeDocumentFromQueue() {
        // ARRANGE
        // Clean out the queue to significantly reduce testing time.
        List<DocumentQueue> all = documentQueueRepo.findAll();
        for (DocumentQueue document : all) {
            documentQueueRepo.remove(document.getId());
        }

        api.documentQueueResourceCreate().body(new DocumentQueueSpssSaveJsonSpec(FileTypSpec.PLAYGROUND, null))
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.CREATED.getStatusCode());

        systemApi.superAdminRunnerResourceTriggerDocumentQueueJob()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());

        DocumentQueueListJsonSpec allOfCurrent = api.documentQueueResourceFindAllByCurrentBenutzer()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(DocumentQueueListJsonSpec.class);

        List<String> idsToDelete = allOfCurrent.getList().stream().map(DocumentQueueDisplayJsonSpec::getId).toList();
        var jsonWithIDsToDelete = new DocumentQueueIDListJsonSpec(idsToDelete);

        // ACT
        api.documentQueueResourceRemoveDocumentQueueItems()
            .body(jsonWithIDsToDelete)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());

        // ASSERT
        List<DocumentQueue> allDocs = documentQueueRepo.findAll();
        List<String> stillExisting = allDocs.stream().map(documentQueue -> documentQueue.getId().getId()).toList();
        assertThat(stillExisting).doesNotContainAnyElementsOf(idsToDelete);

    }
}
