package ch.dvbern.oss.hemed.esc.api.features.example.developer;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.DocumentQueue;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.DocumentQueueMapper;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.DocumentQueueRepo;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.json.DocumentQueueListJson;
import ch.dvbern.oss.hemed.esc.api.shared.documentqueue.json.DocumentQueueSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileTyp;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSupport;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.DocumentQueueApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.SuperAdminRunnerApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.DocumentQueueDisplayJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.DocumentQueueIDListJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.DocumentQueueListJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.DocumentQueueListSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.DocumentQueueParamsSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.DocumentQueueSaveJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.FileTypSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.SpssReportTypeSpec;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import io.restassured.response.ResponseOptions;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@QuarkusTest
@RequiredArgsConstructor
class DocumentQueueResourceITest extends AbstractITest {

    private final DocumentQueueApiSpec api = DocumentQueueApiSpec.documentQueue(RequestSpecUtil.quarkusSpec());
    private final SuperAdminRunnerApiSpec systemApi =
        SuperAdminRunnerApiSpec.superAdminRunner(RequestSpecUtil.quarkusSpec());
    private final DocumentQueueMapper documentQueueMapper;
    private final DocumentQueueRepo documentQueueRepo;

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
    }

    @Test
    @TestAsAdminBS
    void addPlaygroundDocumentQueue() {
        // ARRANGE & ACT
        api.documentQueueResourceCreate().body(new DocumentQueueSaveJsonSpec(FileTypSpec.PLAYGROUND, null))
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.CREATED.getStatusCode());

        // ASSERT
        DocumentQueueListJson documentQueue = documentQueueMapper.findAllByCurrentBenutzer();

        assertThat(documentQueue).isNotNull();
        assertThat(documentQueue.getList()).hasSize(1);
        assertThat(documentQueue.getList().getFirst().getType()).isEqualTo(FileTyp.PLAYGROUND);
    }

    @Test
    @TestAsSupport
    @TestTransaction
    void addTooManylaygroundDocumentQueue() {
        // ARRANGE & ACT

        assertThatThrownBy(this::queueTooManyForSingleUser
        ).isInstanceOf(AppValidationException.class)
            .hasMessageContaining("AppValidation.TOO_MANY_QUEUED_DOCUMENTS, args: max=" + 10);

    }

    private void queueTooManyForSingleUser() {
        for (int i = 0; i < 11; i++) {
            DocumentQueueSaveJson queueJobTooMany = new DocumentQueueSaveJson(FileTyp.PLAYGROUND, null);
            this.documentQueueMapper.create(queueJobTooMany);
        }
    }

    @Test
    @TestAsSupport
    void findAllDocumentQueue() {
        // ARRANGE
        api.documentQueueResourceCreate().body(new DocumentQueueSaveJsonSpec(FileTypSpec.PLAYGROUND_ERROR, null))
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
    @TestAsSupport
    void downloadFile() {
        // ARRANGE
        // Clean out the queue to significantly reduce testing time.
        List<DocumentQueue> all = documentQueueRepo.findAll();
        for (DocumentQueue document : all) {
            documentQueueRepo.remove(document.getId());
        }

        api.documentQueueResourceCreate().body(new DocumentQueueSaveJsonSpec(FileTypSpec.PLAYGROUND, null))
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.CREATED.getStatusCode());

        systemApi.superAdminRunnerResourceTriggerDocumentQueueJob()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());

        ID<DocumentQueue> id = documentQueueMapper.findAllByCurrentBenutzer()
            .getList()
            .stream()
            .filter(item -> item.getType() == FileTyp.PLAYGROUND)
            .findAny()
            .orElseThrow()
            .getId();

        String formattedToday =
            getAppClock().localDateTime().now().toLocalDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        String fileName = formattedToday + "_Playground-File.pdf";

        // ACT
        byte[] image = api.documentQueueResourceDownloadFile()
            .documentQueueIdPath(id.getId())
            .execute(ResponseOptions::andReturn)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .header("Content-Disposition", "attachment;filename=\"" + fileName+ '"')
            .extract()
            .asByteArray();

        // ASSERT
        assertThat(image).isNotNull();
    }

    @Test
    @TestAsSupport
    void addMultiplePlaygroundDocumentQueue() {
        // ARRANGE & ACT
        List<DocumentQueueSaveJsonSpec> documentQueueSaveJsons = Arrays.asList(
            new DocumentQueueSaveJsonSpec(FileTypSpec.PLAYGROUND, null),
            new DocumentQueueSaveJsonSpec(
                FileTypSpec.SPSS_REPORT,
                new DocumentQueueParamsSpec(SpssReportTypeSpec.KINDERGARTEN)));

        DocumentQueueListSaveJsonSpec documentQueueListSaveJsonSpec =
            new DocumentQueueListSaveJsonSpec(documentQueueSaveJsons);

        DocumentQueueListJson documentQueue = documentQueueMapper.findAll();
        int currentSize = documentQueue.getList().size();

        api.documentQueueResourceCreateMultiple().body(documentQueueListSaveJsonSpec)
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.CREATED.getStatusCode());

        // ASSERT
        documentQueue = documentQueueMapper.findAll();

        assertThat(documentQueue).isNotNull();
        assertThat(documentQueue.getList()).hasSize(currentSize + 2);
    }

    @Test
    @TestAsSupport
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

        api.documentQueueResourceCreate().body(new DocumentQueueSaveJsonSpec(FileTypSpec.PLAYGROUND, null))
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.CREATED.getStatusCode());

        systemApi.superAdminRunnerResourceTriggerDocumentQueueJob()
            .execute(ResponseBody::prettyPeek)
            .then()
            .assertThat()
            .statusCode(Status.OK.getStatusCode());

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
        Assertions.assertThat(stillExisting).doesNotContainAnyElementsOf(idsToDelete);

    }
}
