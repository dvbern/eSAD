package ch.dvbern.oss.hemed.esc.api.shared.fileio.healthcheck;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileDataRepo;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSetting;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.core.async.SdkPublisher;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Publisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FileStoreHealthCheckServiceTest {

    private S3AsyncClient s3;
    private ConfigService configService;
    private TenantProducer tenantProducer;
    private FileDataRepo fileDataRepo;
    private TenantSettingsService tenantSettingsService;
    private FileStoreDiffWhitelistLoader whitelistLoader;
    private FileStoreHealthCheckMailService mailService;
    private FileStoreHealthCheckService sut;

    @BeforeEach
    void setUp() {
        s3 = mock(S3AsyncClient.class);
        configService = mock(ConfigService.class);
        tenantProducer = mock(TenantProducer.class);
        fileDataRepo = mock(FileDataRepo.class);
        tenantSettingsService = mock(TenantSettingsService.class);
        whitelistLoader = mock(FileStoreDiffWhitelistLoader.class);
        mailService = mock(FileStoreHealthCheckMailService.class);
        sut = new FileStoreHealthCheckService(s3, configService, tenantProducer, fileDataRepo, tenantSettingsService, whitelistLoader, mailService);

        when(configService.getS3BucketName()).thenReturn("test-bucket");
        when(configService.getFilestoreHealthcheckWhitelistFile()).thenReturn(Optional.empty());
        when(configService.getFilestoreHealthcheckReportMaxEntries()).thenReturn(500);
        when(whitelistLoader.load(null)).thenReturn(Set.of());
        when(tenantProducer.produceTenant()).thenReturn(TenantEnum.BS);

        TenantSetting tenantSetting = mock(TenantSetting.class);
        when(tenantSetting.getSupportEmail()).thenReturn("support@test.ch");
        when(tenantSettingsService.getTenantSetting()).thenReturn(tenantSetting);
    }

    // --- runForCurrentTenant ---

    @Test
    void runForCurrentTenant_noDrift_doesNotSendMail() {
        // ARRANGE
        givenS3Keys("id-a", "id-b");
        when(fileDataRepo.findAllFileIdsInBucket()).thenReturn(List.of("id-a", "id-b"));

        // ACT
        sut.runForCurrentTenant();

        // ASSERT
        verify(mailService, never()).sendReport(any(), any(), any());
    }

    @Test
    void runForCurrentTenant_withDrift_sendsReportWithCorrectCounts() {
        // ARRANGE
        givenS3Keys("id-a", "id-b", "s3-orphan");
        when(fileDataRepo.findAllFileIdsInBucket()).thenReturn(List.of("id-a", "id-b", "db-missing"));

        // ACT
        sut.runForCurrentTenant();

        // ASSERT
        ArgumentCaptor<FileStoreDiffReport> captor = ArgumentCaptor.forClass(FileStoreDiffReport.class);
        verify(mailService).sendReport(captor.capture(), eq("BS"), eq(List.of("support@test.ch")));

        FileStoreDiffReport report = captor.getValue();
        assertThat(report.dbOnlyTotal()).isEqualTo(1);
        assertThat(report.s3OnlyTotal()).isEqualTo(1);
        assertThat(report.dbOnly()).containsExactly("db-missing");
        assertThat(report.s3Only()).containsExactly("s3-orphan");
    }

    @Test
    void runForCurrentTenant_whitelistRemovesItems_noDriftAfterFiltering() {
        // ARRANGE
        givenS3Keys("id-a", "s3-orphan");
        when(fileDataRepo.findAllFileIdsInBucket()).thenReturn(List.of("id-a", "db-missing"));
        when(configService.getFilestoreHealthcheckWhitelistFile()).thenReturn(Optional.of(java.nio.file.Path.of("/w")));
        when(whitelistLoader.load(any())).thenReturn(Set.of("s3-orphan", "db-missing"));

        // ACT
        sut.runForCurrentTenant();

        // ASSERT
        verify(mailService, never()).sendReport(any(), any(), any());
    }

    @Test
    void runForCurrentTenant_driftExceedsMax_reportTruncatedButTotalsCorrect() {
        // ARRANGE
        when(configService.getFilestoreHealthcheckReportMaxEntries()).thenReturn(2);
        givenS3Keys("a", "b", "c");
        when(fileDataRepo.findAllFileIdsInBucket()).thenReturn(List.of("d", "e", "f"));

        // ACT
        sut.runForCurrentTenant();

        // ASSERT
        ArgumentCaptor<FileStoreDiffReport> captor = ArgumentCaptor.forClass(FileStoreDiffReport.class);
        verify(mailService).sendReport(captor.capture(), any(), any());

        FileStoreDiffReport report = captor.getValue();
        assertThat(report.dbOnlyTotal()).isEqualTo(3);
        assertThat(report.s3OnlyTotal()).isEqualTo(3);
        assertThat(report.dbOnly()).hasSize(2);
        assertThat(report.s3Only()).hasSize(2);
    }

    @Test
    void runForCurrentTenant_s3ListingFails_throwsIllegalStateException() {
        // ARRANGE
        when(s3.listObjectsV2Paginator(any(ListObjectsV2Request.class)))
            .thenThrow(new RuntimeException("S3 unreachable"));

        // ACT + ASSERT
        assertThatThrownBy(() -> sut.runForCurrentTenant())
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("test-bucket");
    }

    // --- helpers ---

    private void givenS3Keys(String... keys) {
        List<S3Object> objects = java.util.Arrays.stream(keys)
            .map(k -> S3Object.builder().key(k).build())
            .toList();
        SdkPublisher<S3Object> publisher = sdkPublisherOf(objects);
        ListObjectsV2Publisher listPublisher = mock(ListObjectsV2Publisher.class);
        when(s3.listObjectsV2Paginator(any(ListObjectsV2Request.class))).thenReturn(listPublisher);
        when(listPublisher.contents()).thenReturn(publisher);
    }

    /**
     * Mocks an S3 publisher that delivers the given objects to its subscribers.
     */
    @SuppressWarnings("unchecked")
    private static SdkPublisher<S3Object> sdkPublisherOf(List<S3Object> objects) {
        SdkPublisher<S3Object> publisher = mock(SdkPublisher.class);
        doAnswer(inv -> {
            Consumer<S3Object> consumer = inv.getArgument(0);
            objects.forEach(consumer);
            return CompletableFuture.completedFuture(null);
        }).when(publisher).subscribe(any(Consumer.class));
        return publisher;
    }
}
