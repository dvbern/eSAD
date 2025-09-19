package ch.dvbern.dvbstarter.infrastructure.logging;

import java.io.IOException;
import java.net.URI;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import ch.dvbern.dvbstarter.infrastructure.deployment.DeploymentConfigFixtures;
import ch.dvbern.dvbstarter.infrastructure.deployment.DeploymentConfigFixtures.DeploymentConfigDTO;
import ch.dvbern.oss.commons.logging.mdc.CommonMDCField;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@DisplayNameGeneration(ReplaceUnderscores.class)
@ExtendWith(MockitoExtension.class)
class LoggingCommonMDCFieldsFilterTest {

    private final ConfigService configServiceMock = Mockito.mock(ConfigService.class);

    @Mock
    ContainerRequestContext requestCtx;

    @Mock
    SecurityContext securityCtx;

    @Mock
    UriInfo uriInfo;

    private Map<String, String> mdcBackup;

    /**
     * Store for our mock {@link ContainerRequestContext},
     * more specifically: the backing map for {@link ContainerRequestContext#getProperty(String)}
     * and {@link ContainerRequestContext#setProperty(String, Object)}
     */
    private final Map<String, Object> requestContextProperties = new HashMap<>();

    @BeforeEach
    void beforeEach() {
        setupDeploymentConfigMock(DeploymentConfigFixtures.defaults());

        mdcBackup = MDC.getCopyOfContextMap();
        MDC.clear();

        MDC.put("some-other-key", "some-other-value");
    }

    @AfterEach
    void afterEach() {
        MDC.setContextMap(mdcBackup);
    }

    @Nested
    class With_a_valid_principal {
        LoggingCommonMDCFieldsRequestFilter requestFilter;
        LoggingCommonMDCFieldsResponseFilter responseFilter;

        @BeforeEach
        void beforeEach() {
            givenRequestContextWith(
                "http://localhost:123/foo/bar?foo=bar",
                "POST",
                () -> "The Principal"
            );

            setupDeploymentConfigMock(DeploymentConfigFixtures.defaults()
                .withAppProject("The Project")
                .withAppModule("The Module")
                .withVersion("1.2.3-SNAPSHOT")
                .withInstance("The Instance")
            );

            requestFilter = new LoggingCommonMDCFieldsRequestFilter(configServiceMock) {
                @Override
                String getTenant() {
                    return "The Tenant";
                }
            };
            responseFilter = new LoggingCommonMDCFieldsResponseFilter();

            requestFilter.filter(requestCtx);
        }

        @Test
        void mdc_contains_all_standard_fields() {
            assertThat(MDC.get(CommonMDCField.Principal.name()))
                .isEqualTo("The Principal");
            assertThat(MDC.get(CommonMDCField.RequestSource.name()))
                .isEqualTo("/foo/bar");
            assertThat(MDC.get(CommonMDCField.RequestSourceArgs.name()))
                .isEqualTo("POST");
            assertThat(MDC.get(CommonMDCField.TenantId.name()))
                .isEqualTo("The Tenant");
            assertThat(MDC.get(CommonMDCField.AppProject.name()))
                .isEqualTo("The Project");
            assertThat(MDC.get(CommonMDCField.AppModule.name()))
                .isEqualTo("The Module");
            assertThat(MDC.get(CommonMDCField.AppVersion.name()))
                .isEqualTo("1.2.3-SNAPSHOT");
            assertThat(MDC.get(CommonMDCField.AppInstance.name()))
                .isEqualTo("The Instance");
        }

        @Test
        void RequestPath_contains_only_the_path_of_the_request_URL() {
            assertThat(MDC.get(CommonMDCField.RequestSource.name()))
                .isEqualTo("/foo/bar");
        }

        @Test
        void response_filter_retains_only_initial_MDC_fields() throws IOException {
            setupRequestContextPropertiesForResponse();

            responseFilter.filter(requestCtx, Mockito.mock(ContainerResponseContext.class));

            assertThat(MDC.getCopyOfContextMap())
                .containsExactly(Map.entry("some-other-key", "some-other-value"));
        }
    }

    @Nested
    class With_a_null_principal {
        LoggingCommonMDCFieldsRequestFilter sut;

        @BeforeEach
        void beforeEach() {
            givenRequestContextWith(
                "/ignored",
                "IGNORED",
                null
            );

            setupDeploymentConfigMock(DeploymentConfigFixtures.defaults()
                .withVersion("0.0.0"));
            sut = new LoggingCommonMDCFieldsRequestFilter(configServiceMock);

            sut.filter(requestCtx);
        }

        @Test
        void mdc_contains_none_for_the_principal_entry() {
            assertThat(MDC.get(CommonMDCField.Principal.name()))
                .isEmpty();
        }
    }

    @Nested
    class With_a_null_tenant {
        LoggingCommonMDCFieldsRequestFilter sut;

        @BeforeEach
        void beforeEach() {
            givenRequestContextWith(
                "/ignored",
                "IGNORED",
                () -> "ignored"
            );

            setupDeploymentConfigMock(DeploymentConfigFixtures.defaults());
            sut = new LoggingCommonMDCFieldsRequestFilter(configServiceMock);

            sut.filter(requestCtx);
        }

        @Test
        void mdc_contains_none_for_the_principal_entry() {
            assertThat(MDC.get(CommonMDCField.TenantId.name()))
                .isEmpty();
        }
    }

    void givenRequestContextWith(
        String requestURL,
        String requestMethod,
        @Nullable Principal principal
    ) {
        Mockito.when(uriInfo.getRequestUri())
            .thenReturn(URI.create(requestURL));
        Mockito.when(requestCtx.getMethod())
            .thenReturn(requestMethod);
        Mockito.when(requestCtx.getUriInfo())
            .thenReturn(uriInfo);

        Mockito.when(requestCtx.getSecurityContext())
            .thenReturn(securityCtx);
        Mockito.when(securityCtx.getUserPrincipal())
            .thenReturn(principal);

        setupRequestContextPropertiesForRequest();
    }

    private void setupRequestContextPropertiesForRequest() {
        Mockito.doAnswer(invocation -> requestContextProperties.put(
                invocation.getArgument(0),
                invocation.getArgument(1)
            ))
            .when(requestCtx).setProperty(ArgumentMatchers.anyString(), ArgumentMatchers.any());
    }

    void setupRequestContextPropertiesForResponse() {
        Mockito.doAnswer(invocation -> requestContextProperties.get(invocation.getArgument(0)))
            .when(requestCtx).getProperty(ArgumentMatchers.anyString());

    }

    private void setupDeploymentConfigMock(DeploymentConfigDTO config) {
        doReturn(config.appProject())
            .when(configServiceMock)
            .getAppProject();
        doReturn(config.appModule())
            .when(configServiceMock)
            .getAppModule();
        doReturn(config.environment())
            .when(configServiceMock)
            .getEnvironment();
        doReturn(config.version())
            .when(configServiceMock)
            .getVersion();
        doReturn(config.instance())
            .when(configServiceMock)
            .getInstance();
    }
}
