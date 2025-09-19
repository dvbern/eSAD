package ch.dvbern.oss.hemed.esc.api.shared.config.service;

import ch.dvbern.dvbstarter.infrastructure.deployment.DeploymentEnvironment;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
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
    void isStufeProd_returns_true_when_environment_is_prod() {
        // Arrange
        when(configService.getEnvironment()).thenReturn(DeploymentEnvironment.PROD);
        // Act & Assert
        Assertions.assertThat(configService.isStufeProd()).isTrue();
    }

    @Test
    void isStufeProd_returns_false_when_environment_is_not_prod() {
        // Arrange
        when(configService.getEnvironment()).thenReturn(DeploymentEnvironment.DEV);
        // Act & Assert
        Assertions.assertThat(configService.isStufeProd()).isFalse();
    }
}
