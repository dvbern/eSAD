package ch.dvbern.oss.hemed.esc.api.shared.config.service;

import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.deployment.DeploymentEnvironment;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class ConfigServiceTest {

    private final ConfigService configService = Mockito.mock(ConfigService.class, Mockito.CALLS_REAL_METHODS);

    @BeforeEach
    void setUp() {
        Mockito.reset(configService);
        configService.tenantOverride = Optional.empty();
    }

    @Test
    void validateNotProdAndDbResetFeatureEnabled_Success() {
        // Arrange
        when(configService.getEnvironment()).thenReturn(DeploymentEnvironment.DEV);
        when(configService.isAllowDbReset()).thenReturn(true);
        Mockito.doCallRealMethod().when(configService).validateNotProdAndDbResetFeatureEnabled();
        // Act & Assert
        assertDoesNotThrow(configService::validateNotProdAndDbResetFeatureEnabled);
    }

    @Test
    void validateNotProdAndDbResetFeatureEnabled_ThrowsException_WhenInProd() {
        // Arrange
        when(configService.getEnvironment()).thenReturn(DeploymentEnvironment.PROD);
        Mockito.doCallRealMethod().when(configService).validateNotProdAndDbResetFeatureEnabled();
        // Act & Assert
        assertThrows(
            AppFailureException.class,
            configService::validateNotProdAndDbResetFeatureEnabled);
    }

    @Test
    void validateNotProdAndDbResetFeatureEnabled_ThrowsException_WhenDbResetDisabled() {
        // Arrange
        when(configService.getEnvironment()).thenReturn(DeploymentEnvironment.DEV);
        when(configService.isAllowDbReset()).thenReturn(false);
        Mockito.doCallRealMethod().when(configService).validateNotProdAndDbResetFeatureEnabled();
        // Act & Assert
        assertThrows(
            AppFailureException.class,
            configService::validateNotProdAndDbResetFeatureEnabled);
    }

    @Test
    void validateNotProdAndTimetravelFeatureEnabled_Success() {
        // Arrange
        when(configService.getEnvironment()).thenReturn(DeploymentEnvironment.DEV);
        when(configService.isFeatureTimeTravelEndpointEnabled()).thenReturn(true);
        Mockito.doCallRealMethod().when(configService).validateNotProdAndTimeTravelFeatureEnabled();
        // Act & Assert
        assertDoesNotThrow(configService::validateNotProdAndTimeTravelFeatureEnabled);
    }

    @Test
    void validateNotProdAndTimetravelFeatureEnabled_ThrowsException_WhenInProd() {
        // Arrange
        when(configService.getEnvironment()).thenReturn(DeploymentEnvironment.PROD);
        Mockito.doCallRealMethod().when(configService).validateNotProdAndTimeTravelFeatureEnabled();
        // Act & Assert
        assertThrows(
            AppFailureException.class,
            configService::validateNotProdAndTimeTravelFeatureEnabled);
    }

    @Test
    void validateNotProdAndTimetravelFeatureEnabled_ThrowsException_WhenTimetravelDisabled() {
        // Arrange
        when(configService.getEnvironment()).thenReturn(DeploymentEnvironment.DEV);
        when(configService.isFeatureTimeTravelEndpointEnabled()).thenReturn(false);
        Mockito.doCallRealMethod().when(configService).validateNotProdAndTimeTravelFeatureEnabled();
        // Act & Assert
        assertThrows(
            AppFailureException.class,
            configService::validateNotProdAndTimeTravelFeatureEnabled);
    }

    @Test
    void getTenantOverride_returns_value_if_overridden() {
        // Arrange
        when(configService.getTenantOverride()).thenReturn(Optional.of(TenantEnum.BS));
        // Act & Assert
        Assertions.assertThat(configService.getTenantOverride()).isEqualTo(Optional.of(TenantEnum.BS));
    }

    @Test
    void getTenantOverride_returns_empty_if_not_overridden() {
        // Act & Assert
        Assertions.assertThat(configService.getTenantOverride()).isEmpty();
    }

    @Test
    void isStufeProd_returns_true_when_environment_is_prod() {
        // Arrange
        when(configService.getEnvironment()).thenReturn(DeploymentEnvironment.PROD);
        // Act & Assert
        Assertions.assertThat(configService.isStufeProd()).isTrue();
    }

    void isStufeProd_returns_false_when_environment_is_not_prod() {
        // Arrange
        when(configService.getEnvironment()).thenReturn(DeploymentEnvironment.DEV);
        // Act & Assert
        Assertions.assertThat(configService.isStufeProd()).isFalse();
    }
}
