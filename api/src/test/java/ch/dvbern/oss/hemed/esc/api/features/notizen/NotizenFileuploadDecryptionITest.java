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

package ch.dvbern.oss.hemed.esc.api.features.notizen;

import java.io.File;
import java.util.Objects;

import ch.dvbern.dvbstarter.infrastructure.pdf.PDFContentExtractorUtil;
import ch.dvbern.dvbstarter.infrastructure.pdf.PDFContentExtractorUtil.PdfInfo;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.s3.S3EncryptedRequestBuilderImpl;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.s3.S3EncryptionKeyGenerator;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.s3.S3RequestBuilderImpl;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.s3.S3RequestBuilderProducer;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.FixtureService;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.restassured.RequestSpecUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import ch.dvbern.oss.hemed.esc.generated.restassured.api.NotizenApiSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.AppFailureErrorJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.FileDataJsonSpec;
import ch.dvbern.oss.hemed.esc.generated.restassured.dto.NotizJsonSpec;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.ResponseBody;
import io.restassured.response.ResponseOptions;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
@RequiredArgsConstructor
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NotizenFileuploadDecryptionITest extends AbstractITest {

    private final NotizenApiSpec api = NotizenApiSpec.notizen(RequestSpecUtil.quarkusSpec());
    private S3RequestBuilderProducer s3RequestBuilderProducerMock;
    private final FixtureService fixtureService;

    @BeforeAll
    void setup() {
        s3RequestBuilderProducerMock = Mockito.mock(S3RequestBuilderProducer.class);
        // install
        QuarkusMock.installMockForType(s3RequestBuilderProducerMock, S3RequestBuilderProducer.class);
    }

    @Test
    @TestAsAdminBS
    void upload_file_and_download_with_same_encryption_key_should_work() {
        // ARRANGE
        Notiz storedNotiz = fixtureService.createNotiz();
        String somreRandomKey = S3EncryptionKeyGenerator.generateKey();

        Mockito.when(s3RequestBuilderProducerMock.produceS3RequestBuilder())
            .thenReturn(new S3EncryptedRequestBuilderImpl(somreRandomKey));

        File testfile = TestUtil.loadTestPDF();
        String name = testfile.getName();

        NotizJsonSpec notizJsonSpec = api.notizResourceAddNotizFile()
            .notizIdPath(storedNotiz.getId().getId())
            ._fileMultiPart(testfile)
            .execute(ResponseBody::prettyPeek)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(NotizJsonSpec.class);

        Assertions.assertThat(notizJsonSpec.getData().getFiles())
            .isNotEmpty()
            .asInstanceOf(InstanceOfAssertFactories.list(FileDataJsonSpec.class))
            .hasSize(1);

        byte[] file = api.notizResourceDownloadNotizFileSync()
            .notizIdPath(storedNotiz.getId().getId())
            .fileIdPath(Objects.requireNonNull(notizJsonSpec.getData().getFiles()).getFirst().getId())
            .execute(ResponseOptions::andReturn)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .header("Content-Disposition", "attachment;filename=\"" + name+ '"')
            .extract()
            .asByteArray();

        // ASSERT
        assertThat(file).isNotNull();

        PdfInfo pdfInfo = PDFContentExtractorUtil.extractPdfInfo(file);
        String textContent = pdfInfo.textcontent();
        assertThat(textContent).contains("This is a small demonstration .pdf file");

    }

    @Test
    @TestAsAdminBS
    void when_upload_encrypted_and_download_without_key_should_fail() {
        // ARRANGE
        Notiz storedNotiz = fixtureService.createNotiz();
        String somreRandomKey = S3EncryptionKeyGenerator.generateKey();

        Mockito.when(s3RequestBuilderProducerMock.produceS3RequestBuilder())
            .thenReturn(new S3EncryptedRequestBuilderImpl(somreRandomKey));

        File testfile = TestUtil.loadTestPDF();

        NotizJsonSpec notizJsonSpec = api.notizResourceAddNotizFile()
            .notizIdPath(storedNotiz.getId().getId())
            ._fileMultiPart(testfile)
            .execute(ResponseBody::prettyPeek)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(NotizJsonSpec.class);

        Assertions.assertThat(notizJsonSpec.getData().getFiles())
            .isNotEmpty()
            .asInstanceOf(InstanceOfAssertFactories.list(FileDataJsonSpec.class))
            .hasSize(1);

        Mockito.when(s3RequestBuilderProducerMock.produceS3RequestBuilder()).thenReturn(new S3RequestBuilderImpl());

        var error = api.notizResourceDownloadNotizFileSync()
            .notizIdPath(storedNotiz.getId().getId())
            .fileIdPath(Objects.requireNonNull(notizJsonSpec.getData().getFiles()).getFirst().getId())
            .execute(ResponseOptions::andReturn)
            .then()
            .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
            .extract().as(AppFailureErrorJsonSpec.class);

        // ASSERT
        assertThat(error).isNotNull();
        assertThat(error.getSummary()).contains("Could not load file from S3");

    }

    @Test
    @TestAsAdminBS
    void when_upload_encrypted_and_download_with_wrong_key_should_fail() {
        // ARRANGe
        Notiz storedNotiz = fixtureService.createNotiz();
        // ARRANGE
        String somreRandomKey = S3EncryptionKeyGenerator.generateKey();
        String anotherRandomKey = S3EncryptionKeyGenerator.generateKey();

        Mockito.when(s3RequestBuilderProducerMock.produceS3RequestBuilder())
            .thenReturn(new S3EncryptedRequestBuilderImpl(somreRandomKey));

        File testfile = TestUtil.loadTestPDF();

        NotizJsonSpec notizJsonSpec = api.notizResourceAddNotizFile()
            .notizIdPath(storedNotiz.getId().getId())
            ._fileMultiPart(testfile)
            .execute(ResponseBody::prettyPeek)
            .then()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(NotizJsonSpec.class);

        Assertions.assertThat(notizJsonSpec.getData().getFiles())
            .isNotEmpty()
            .asInstanceOf(InstanceOfAssertFactories.list(FileDataJsonSpec.class))
            .hasSize(1);

        // switch to another key which is the wrong key for this file
        Mockito.when(s3RequestBuilderProducerMock.produceS3RequestBuilder())
            .thenReturn(new S3EncryptedRequestBuilderImpl(anotherRandomKey));

        var error = api.notizResourceDownloadNotizFileSync()
            .notizIdPath(storedNotiz.getId().getId())
            .fileIdPath(Objects.requireNonNull(notizJsonSpec.getData().getFiles()).getFirst().getId())
            .execute(ResponseOptions::andReturn)
            .then()
            .statusCode(Status.INTERNAL_SERVER_ERROR.getStatusCode())
            .extract().as(AppFailureErrorJsonSpec.class);

        // ASSERT
        assertThat(error).isNotNull();
        assertThat(error.getSummary()).contains("Could not load file from S3");
    }
}
