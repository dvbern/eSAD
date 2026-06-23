package ch.dvbern.oss.hemed.esc.api.features.fileio;

import ch.dvbern.oss.hemed.esc.api.features.notizen.Notiz;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.Aufgabe;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.AufgabeApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.NotizenApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AufgabeJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.FileDataJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.NotizJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class FileOrderingITest extends AbstractITest {

    private final AufgabeApiSpec api = AufgabeApiSpec.aufgabe(RequestSpecUtil.quarkusSpec());
    private final NotizenApiSpec notizenApi = NotizenApiSpec.notizen(RequestSpecUtil.quarkusSpec());

    @Inject
    FixtureService fixtureService;

    @Test
    @TestAsAdminBS
    void testFileOrdering() {
        // ARRANGE
        Aufgabe aufgabe = fixtureService.createAufgabe();
        File file1 = TestUtil.loadTestPDF();
        File file2 = TestUtil.loadTestImage();

        // ACT - Upload first file
        api.aufgabeResourceAddAufgabeFile()
            .aufgabeIdPath(aufgabe.getId().getId())
            ._fileMultiPart(file1)
            .execute(ResponseBody::prettyPeek)
            .then()
            .statusCode(200);

        // Wait a bit to ensure different timestamps
        setTime(java.time.OffsetDateTime.now().plusSeconds(1));

        // ACT - Upload second file
        api.aufgabeResourceAddAufgabeFile()
            .aufgabeIdPath(aufgabe.getId().getId())
            ._fileMultiPart(file2)
            .execute(ResponseBody::prettyPeek)
            .then()
            .statusCode(200);

        // Fetch again to ensure fresh load and OrderBy application
        AufgabeJsonSpec aufgabeJsonSpec = api.aufgabeResourceGetAufgabe()
            .aufgabeIdPath(aufgabe.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .statusCode(200)
            .extract()
            .as(AufgabeJsonSpec.class);

        // ASSERT
        List<FileDataJsonSpec> files = aufgabeJsonSpec.getData().getFiles();
        assertThat(files).hasSize(2);

        // The second file (most recently uploaded) should be first
        // file2 is logo.png -> renamed to something containing .png
        // file1 is pdfFileExample.pdf -> renamed to something containing .pdf
        assertThat(files.get(0).getCalculatedFileName()).contains(".png");
        assertThat(files.get(1).getCalculatedFileName()).contains(".pdf");
    }

    @Test
    @TestAsAdminBS
    void testNotizFileOrdering() {
        // ARRANGE
        Notiz notiz = fixtureService.createNotiz();
        File file1 = TestUtil.loadTestPDF();
        File file2 = TestUtil.loadTestImage();

        // ACT - Upload first file
        notizenApi.notizResourceAddNotizFile()
            .notizIdPath(notiz.getId().getId())
            ._fileMultiPart(file1)
            .execute(ResponseBody::prettyPeek)
            .then()
            .statusCode(200);

        // Wait a bit to ensure different timestamps
        setTime(java.time.OffsetDateTime.now().plusSeconds(1));

        // ACT - Upload second file
        notizenApi.notizResourceAddNotizFile()
            .notizIdPath(notiz.getId().getId())
            ._fileMultiPart(file2)
            .execute(ResponseBody::prettyPeek)
            .then()
            .statusCode(200);

        // Fetch again to ensure fresh load and OrderBy application
        NotizJsonSpec notizJsonSpec = notizenApi.notizResourceGetNotiz()
            .notizIdPath(notiz.getId().getId())
            .execute(ResponseBody::prettyPeek)
            .then()
            .statusCode(200)
            .extract()
            .as(NotizJsonSpec.class);

        // ASSERT
        List<FileDataJsonSpec> files = notizJsonSpec.getData().getFiles();
        assertThat(files).hasSize(2);

        // The second file (most recently uploaded) should be first
        assertThat(files.get(0).getCalculatedFileName()).contains(".png");
        assertThat(files.get(1).getCalculatedFileName()).contains(".pdf");
    }
}
