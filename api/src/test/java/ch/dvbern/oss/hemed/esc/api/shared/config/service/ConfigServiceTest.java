package ch.dvbern.oss.hemed.esc.api.shared.config.service;

import java.time.Duration;

import ch.dvbern.dvbstarter.infrastructure.deployment.DeploymentEnvironment;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConfigServiceTest {

    private final TenantContext tenantContext = mock(TenantContext.class);
    private final ConfigService configService = Mockito.mock(ConfigService.class, Mockito.CALLS_REAL_METHODS);

    @BeforeEach
    void setUp() {
        Mockito.reset(configService, tenantContext);
        Mockito.doReturn(tenantContext).when(configService).getTenantContext();
    }

    @Test
    void validateNotProdAndDbResetFeatureEnabled_Success() {
        // ARRANGE
        when(configService.getEnvironment()).thenReturn(DeploymentEnvironment.DEV);
        when(configService.isAllowDbReset()).thenReturn(true);
        Mockito.doCallRealMethod().when(configService).validateNotProdAndDbResetFeatureEnabled();
        // ACT & ASSERT
        assertDoesNotThrow(configService::validateNotProdAndDbResetFeatureEnabled);
    }

    @Test
    void validateNotProdAndDbResetFeatureEnabled_ThrowsException_WhenInProd() {
        // ARRANGE
        when(configService.getEnvironment()).thenReturn(DeploymentEnvironment.PROD);
        Mockito.doCallRealMethod().when(configService).validateNotProdAndDbResetFeatureEnabled();
        // ACT & ASSERT
        assertThrows(
            AppFailureException.class,
            configService::validateNotProdAndDbResetFeatureEnabled);
    }

    @Test
    void validateNotProdAndDbResetFeatureEnabled_ThrowsException_WhenDbResetDisabled() {
        // ARRANGE
        when(configService.getEnvironment()).thenReturn(DeploymentEnvironment.DEV);
        when(configService.isAllowDbReset()).thenReturn(false);
        Mockito.doCallRealMethod().when(configService).validateNotProdAndDbResetFeatureEnabled();
        // ACT & ASSERT
        assertThrows(
            AppFailureException.class,
            configService::validateNotProdAndDbResetFeatureEnabled);
    }

    @Test
    void validateNotProdAndTimetravelFeatureEnabled_Success() {
        // ARRANGE
        when(configService.getEnvironment()).thenReturn(DeploymentEnvironment.DEV);
        when(configService.isFeatureTimeTravelEndpointEnabled()).thenReturn(true);
        Mockito.doCallRealMethod().when(configService).validateNotProdAndTimeTravelFeatureEnabled();
        // ACT & ASSERT
        assertDoesNotThrow(configService::validateNotProdAndTimeTravelFeatureEnabled);
    }

    @Test
    void validateNotProdAndTimetravelFeatureEnabled_ThrowsException_WhenInProd() {
        // ARRANGE
        when(configService.getEnvironment()).thenReturn(DeploymentEnvironment.PROD);
        Mockito.doCallRealMethod().when(configService).validateNotProdAndTimeTravelFeatureEnabled();
        // ACT & ASSERT
        assertThrows(
            AppFailureException.class,
            configService::validateNotProdAndTimeTravelFeatureEnabled);
    }

    @Test
    void validateNotProdAndTimetravelFeatureEnabled_ThrowsException_WhenTimetravelDisabled() {
        // ARRANGE
        when(configService.getEnvironment()).thenReturn(DeploymentEnvironment.DEV);
        when(configService.isFeatureTimeTravelEndpointEnabled()).thenReturn(false);
        Mockito.doCallRealMethod().when(configService).validateNotProdAndTimeTravelFeatureEnabled();
        // ACT & ASSERT
        assertThrows(
            AppFailureException.class,
            configService::validateNotProdAndTimeTravelFeatureEnabled);
    }

    @Test
    void isStufeProd_returns_true_when_environment_is_prod() {
        // ARRANGE
        when(configService.getEnvironment()).thenReturn(DeploymentEnvironment.PROD);
        // ACT & ASSERT
        Assertions.assertThat(configService.isStufeProd()).isTrue();
    }

    @Test
    void isStufeProd_returns_false_when_environment_is_not_prod() {
        // ARRANGE
        when(configService.getEnvironment()).thenReturn(DeploymentEnvironment.DEV);
        // ACT & ASSERT
        Assertions.assertThat(configService.isStufeProd()).isFalse();
    }

    @Test
    void getS3BucketName_returns_singleBucket_when_s3SingleBucket_is_true() {
        // ARRANGE
        Mockito.doReturn(true).when(configService).isS3SingleBucket();
        Mockito.doReturn("bucket-single").when(configService).getS3BucketNameSingle();
        Mockito.doReturn("bucket-bs").when(configService).getS3BucketNameBS();
        Mockito.doReturn("bucket-win").when(configService).getS3BucketNameWIN();
        Mockito.doReturn("bucket-be").when(configService).getS3BucketNameBE();

        // ACT & ASSERT
        Assertions.assertThat(configService.getS3BucketName()).isEqualTo("bucket-single");
    }

    @Test
    void getS3BucketName_returns_tenantBucket_when_s3SingleBucket_is_false() {
        // ARRANGE
        Mockito.doReturn(false).when(configService).isS3SingleBucket();
        Mockito.doReturn("bucket-single").when(configService).getS3BucketNameSingle();
        Mockito.doReturn("bucket-bs").when(configService).getS3BucketNameBS();
        Mockito.doReturn("bucket-win").when(configService).getS3BucketNameWIN();
        Mockito.doReturn("bucket-be").when(configService).getS3BucketNameBE();

        // ACT & ASSERT
        when(tenantContext.get()).thenReturn(TenantEnum.BS);
        Assertions.assertThat(configService.getS3BucketName()).isEqualTo("bucket-bs");

        when(tenantContext.get()).thenReturn(TenantEnum.WIN);
        Assertions.assertThat(configService.getS3BucketName()).isEqualTo("bucket-win");

        when(tenantContext.get()).thenReturn(TenantEnum.BE);
        Assertions.assertThat(configService.getS3BucketName()).isEqualTo("bucket-be");
    }

    @Test
    void getS3DeleteTimeout_returns_configured_timeout() {
        // ARRANGE
        Duration timeout = Duration.ofSeconds(5);
        configService.s3DeleteTimeout = timeout;

        // ACT & ASSERT
        Assertions.assertThat(configService.getS3DeleteTimeout())
            .isEqualTo(timeout);
    }
}
