package ch.dvbern.oss.hemed.esc.api.shared.preview;

import java.io.File;
import java.util.Objects;

import ch.dvbern.oss.hemed.esc.api.features.notizen.Notiz;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.Aufgabe;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.AufgabeApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.NotizenApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.PreviewApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AufgabeJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.FileDataJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.NotizJsonSpec;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import io.restassured.response.ResponseOptions;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


@QuarkusTest
@RequiredArgsConstructor
class PreviewResourceITest {

    public static final int EXPECTED_PREVIEW_IMAGE_LENGTH = 5695;
    private final PreviewApiSpec api = PreviewApiSpec.preview(RequestSpecUtil.quarkusSpec());
    private final NotizenApiSpec notizApi = NotizenApiSpec.notizen(RequestSpecUtil.quarkusSpec());
    private final AufgabeApiSpec aufgabeApi = AufgabeApiSpec.aufgabe(RequestSpecUtil.quarkusSpec());

    private final FixtureService fixtureService;


    @Test
    @TestAsAdminBS
    void downloadNotizFilePreview_should_return_image() {
        // ARRANGe
        Notiz storedNotiz = fixtureService.createNotiz();

        File testfile = TestUtil.loadTestImage();
        NotizJsonSpec notizJsonSpec = notizApi.notizResourceAddNotizFile()
            .notizIdPath(storedNotiz.getId().getId())
            ._fileMultiPart(testfile)

            .execute(ResponseBody::prettyPeek)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(NotizJsonSpec.class);

        assertThat(notizJsonSpec.getData().getFiles())
            .isNotEmpty()
            .asInstanceOf(InstanceOfAssertFactories.list(FileDataJsonSpec.class))
            .hasSize(1);
        // ACT

        byte[] image = api.previewResourceDownloadPreviewNotizFile()
            .notizIdPath(storedNotiz.getId().getId())
            .fileIdPath(Objects.requireNonNull(notizJsonSpec.getData().getFiles()).getFirst().getId())
            .execute(ResponseOptions::andReturn)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .header("Content-Disposition", "inline;filename=\"preview.png\"")
            .header("Content-Type", "image/png")
            .header("Cache-Control", "max-age=3600")
            .extract()
            .asByteArray();

        // ASSERT
        assertThat(image)
            .isNotNull()
            .hasSize(EXPECTED_PREVIEW_IMAGE_LENGTH);
    }
    @Test
    @TestAsAdminBS
    void downloadAufgabeFileSync() {
        // ARRANGE
        Aufgabe storedAufgabe = fixtureService.createAufgabe();

        AufgabeJsonSpec aufgabeJsonSpec = aufgabeApi
            .aufgabeResourceAddAufgabeFile()
            .aufgabeIdPath(storedAufgabe.getId().getId())
            ._fileMultiPart(TestUtil.loadTestImage())
            .execute(ResponseBody::prettyPeek)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(AufgabeJsonSpec.class);

        assertThat(aufgabeJsonSpec.getData().getFiles())
            .isNotEmpty()
            .asInstanceOf(InstanceOfAssertFactories.list(FileDataJsonSpec.class))
            .hasSize(1);
        // ACT

        byte[] image = api
            .previewResourceDownloadPreviewAufgabeFile()
            .aufgabeIdPath(storedAufgabe.getId().getId())
            .fileIdPath(Objects.requireNonNull(aufgabeJsonSpec.getData().getFiles()).getFirst().getId())
            .execute(ResponseOptions::andReturn)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .header("Content-Disposition", "inline;filename=\"preview.png\"")
            .header("Content-Type", "image/png")
            .header("Cache-Control", "max-age=3600")
            .extract()
            .asByteArray();

        // ASSERT
        assertThat(image)
            .isNotNull()
            .hasSize(EXPECTED_PREVIEW_IMAGE_LENGTH);
    }
}
