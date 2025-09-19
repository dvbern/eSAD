package ch.dvbern.oss.hemed.esc.api.features.example.developer;

import ch.dvbern.oss.hemed.esc.api.features.notizen.Notiz;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileStoreService;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsSupport;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.NotizenApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.S3ResourceApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.NotizJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import io.restassured.response.ResponseOptions;
import jakarta.ws.rs.core.Response.Status;
import java.io.File;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@RequiredArgsConstructor
class S3ResourceITest {

    private final S3ResourceApiSpec api = S3ResourceApiSpec.s3Resource(RequestSpecUtil.quarkusSpec());
    private final NotizenApiSpec notizenApi = NotizenApiSpec.notizen(RequestSpecUtil.quarkusSpec());

    private final FileStoreService fileStoreService;
    private final FixtureService fixtureService;
    private final TenantProducer tenantProducer;

    @BeforeEach
    void setUp() {
        tenantProducer.setCurrentTenant(TenantEnum.BS);
    }

    @Test
    @TestAsSupport
    void downloadFile() {
        // ARRANGE
        Notiz storedNotiz = fixtureService.createNotiz();

        File testfile = TestUtil.loadTestImage();
        NotizJsonSpec notizJsonSpec = notizenApi.notizResourceAddNotizFile()
            .kindIdPath(storedNotiz.getDossier().getKind().getId().getId())
            .notizIdPath(storedNotiz.getId().getId())
            ._fileMultiPart(testfile)
            .execute(ResponseBody::prettyPeek)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(NotizJsonSpec.class);

        // ACT
        byte[] image = api.s3ResourceDownloadFile()
            .fileIdInbucketPath(notizJsonSpec.getData().getFiles().getFirst().getFileIdInBucket())
            .execute(ResponseOptions::andReturn)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .asByteArray();

        // ASSERT
        assertThat(image).isNotNull();
        assertThat(testfile)
            .hasSize(image.length)
            // check that loading the file synchronously works as well and returns the same content
            .hasSize(fileStoreService.getFileBytesSync(notizJsonSpec.getData()
                .getFiles()
                .getFirst()
                .getFileIdInBucket()).length);
    }

    @Test
    @TestAsSupport
    void downloadFileWithCommaInFilename() {
        // ARRANGE
        Notiz storedNotiz = fixtureService.createNotiz();

        File testfile = TestUtil.loadTestProblematicFilename();
        NotizJsonSpec notizJsonSpec = notizenApi.notizResourceAddNotizFile()
            .kindIdPath(storedNotiz.getDossier().getKind().getId().getId())
            .notizIdPath(storedNotiz.getId().getId())
            ._fileMultiPart(testfile)
            .execute(ResponseBody::prettyPeek)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(NotizJsonSpec.class);

        // ACT
        byte[] strangelyNamedFile = api.s3ResourceDownloadFile()
            .fileIdInbucketPath(notizJsonSpec.getData().getFiles().getFirst().getFileIdInBucket())
            .execute(ResponseOptions::andReturn)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .asByteArray();

        // ASSERT
        assertThat(strangelyNamedFile).isNotNull();
        assertThat(testfile)
            .hasSize(strangelyNamedFile.length)
            // check that loading the file synchronously works as well and returns the same content
            .hasSize(fileStoreService.getFileBytesSync(notizJsonSpec.getData()
                .getFiles()
                .getFirst()
                .getFileIdInBucket()).length);
    }
}
