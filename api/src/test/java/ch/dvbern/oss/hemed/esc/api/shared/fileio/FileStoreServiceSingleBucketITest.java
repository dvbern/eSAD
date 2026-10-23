package ch.dvbern.oss.hemed.esc.api.shared.fileio;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext;
import ch.dvbern.oss.hemed.esc.api.testutil.AbstractITest;
import ch.dvbern.oss.hemed.esc.api.testutil.S3ObjectProbe;
import ch.dvbern.oss.hemed.esc.api.testutil.TestUtil;
import ch.dvbern.oss.hemed.esc.api.testutil.security.TestAsAdminBS;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * In single bucket mode every tenant writes to the same bucket; the cleanup has to go there as well.
 */
@QuarkusTest
@TestProfile(S3SingleBucketTestProfile.class)
@RequiredArgsConstructor
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class FileStoreServiceSingleBucketITest extends AbstractITest {

    private final FileStoreService fileStoreService;
    private final TenantContext tenantContext;
    private final S3ObjectProbe s3;

    @ParameterizedTest
    @EnumSource(value = TenantEnum.class, names = { "BS", "WIN", "BE" })
    @TestAsAdminBS
    void uploadBoundToTransaction_rolledBack_should_delete_file_in_single_bucket(TenantEnum tenant) {
        // ARRANGE
        tenantContext.setCurrentTenant(tenant);
        AtomicReference<String> file = new AtomicReference<>();
        AtomicReference<List<String>> bucketsBeforeRollback = new AtomicReference<>();

        // ACT
        catchThrowable(() -> QuarkusTransaction.requiringNew().run(() -> {
            file.set(upload());
            bucketsBeforeRollback.set(s3.bucketsContaining(file.get()));
            QuarkusTransaction.setRollbackOnly();
        }));

        // ASSERT
        assertThat(bucketsBeforeRollback.get())
            .containsExactly(s3.singleBucket());
        assertThat(s3.bucketsContaining(file.get()))
            .isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = TenantEnum.class, names = { "BS", "WIN", "BE" })
    @TestAsAdminBS
    void deleteAfterCommit_committed_should_delete_file_in_single_bucket(TenantEnum tenant) {
        // ARRANGE
        tenantContext.setCurrentTenant(tenant);
        String file = QuarkusTransaction.requiringNew().call(this::upload);

        // ACT
        QuarkusTransaction.requiringNew().run(() -> fileStoreService.deleteInS3AfterCommit(file));

        // ASSERT
        assertThat(s3.bucketsContaining(file))
            .isEmpty();
    }

    @NonNull
    private String upload() {
        FileUpload fileUpload = FileUploadWrapper.fromFile(TestUtil.loadTestPDF());
        FileInfo fileInfo = FileInfoS3Mapper.mapFileUploadToFileInfoWithMIMEDetect(fileUpload, FileTyp.NOTIZUPLOAD);
        fileStoreService.uploadToS3BoundToTransaction(fileUpload, fileInfo);
        return fileInfo.getFileIdInBucket();
    }
}
