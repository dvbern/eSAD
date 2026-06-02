package ch.dvbern.dvbstarter.infrastructure.deployment;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppValidationException;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.UriInfo;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayNameGeneration(ReplaceUnderscores.class)
class ClientVersionVerificationFilterTest {

    ConfigService configServiceMock = mock(ConfigService.class);

    @ParameterizedTest
    @CsvSource({
        "1.0.0, /pub/client-config/             , PROD, 1.0.0",
        "1.0.0, /pub/client-config/             , PROD, 2.0.0",
        "1.0.0, /pub/client-config/something    , PROD, 1.0.0",
        "1.0.0, /pub/client-config/something    , PROD, 2.0.0",
        "1.0.0, /                               , PROD, 1.0.0",
        "1.0.0, /something                      , PROD, 1.0.0",
    })
    void handles_valid_request(
        String clientVersion,
        String path,
        DeploymentEnvironment environment,
        String serverVersion
    ) {
        // ARRANGE
        setupDeploymentConfig(environment, serverVersion, true);
        var filter = new ClientVersionVerificationFilter(configServiceMock);
        filter.httpRootPath = "rootpath";
        var context = setupContainerRequestContext(clientVersion, filter.httpRootPath + path);
        // ACT / ASSERT
        assertThatCode(() -> filter.filter(context))
            .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @CsvSource({
        "1.0.0, /            , PROD, 1.1.1",
        "1.0.0, /            , PROD, 1.1.0",
        "1.0.0, /something   , PROD, 2.0.0",
    })
    void rejects_invalid_request(
        String clientVersion,
        String path,
        DeploymentEnvironment environment,
        String serverVersion
    ) {
        // ARRANGE
        setupDeploymentConfig(environment, serverVersion, true);
        var filter = new ClientVersionVerificationFilter(configServiceMock);
        filter.httpRootPath = "rootpath";
        var context = setupContainerRequestContext(clientVersion, filter.httpRootPath + path);
        // ACT / ASSERT
        assertThrows(
            AppValidationException.class,
            () -> filter.filter(context)
        );
    }

    @NonNull
    private ContainerRequestContext setupContainerRequestContext(
        @NonNull String clientVersion,
        @NonNull String path
    ) {
        var uriInfo = mock(UriInfo.class);
        doReturn(path)
            .when(uriInfo)
            .getPath();
        var context = mock(ContainerRequestContext.class);
        doReturn(uriInfo)
            .when(context)
            .getUriInfo();
        doReturn(clientVersion)
            .when(context)
            .getHeaderString("X-App-Version");
        return context;
    }

    private void setupDeploymentConfig(
        @NonNull DeploymentEnvironment environment,
        @NonNull String version,
        boolean filterEnabled
    ) {
        when(configServiceMock.getVersion()).thenReturn(version);
        when(configServiceMock.getEnvironment()).thenReturn(environment);
        when(configServiceMock.isVersionFilterEnabled()).thenReturn(filterEnabled);
    }
}
