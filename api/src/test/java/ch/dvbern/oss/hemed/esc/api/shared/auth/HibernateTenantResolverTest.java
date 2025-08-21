package ch.dvbern.oss.hemed.esc.api.shared.auth;

import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import org.assertj.core.api.Assertions;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class HibernateTenantResolverTest {

    private JsonWebToken mockToken;
    private ConfigService configServiceMock;
    private HibernateTenantResolver sut;


    @BeforeEach
    void beforeEach() {
        mockToken = Mockito.mock(JsonWebToken.class);
        configServiceMock = Mockito.mock(ConfigService.class);
        Mockito
            .when(configServiceMock.getTenantOverride())
            .thenReturn(Optional.empty());
        sut = new HibernateTenantResolver(configServiceMock, mockToken);
    }

    @Test
    void getDefaultTenantId() {
        Assertions.assertThat(sut.getDefaultTenantId())
            .isEqualTo("DEFAULT");
    }

    @Test
    void resolveTenantId_when_token_is_empty_returns_default() {
        Mockito.when(mockToken.getRawToken())
            .thenReturn(null);
        Assertions.assertThat(sut.resolveTenantId())
            .isEqualTo("DEFAULT");
    }

    @Test
    void resolveTenantId_when_token_has_valid_claim_works() {
        Mockito.when(mockToken.getRawToken())
            .thenReturn("{irrelevant:token}");
        Mockito.when(mockToken.claim("tenant"))
            .thenReturn(Optional.of("bs"));

        Assertions.assertThat(sut.resolveTenantId())
            .isEqualTo("BS");
    }

    @Test
    void resolveTenantId_when_token_has_valid_be_claim_works() {
        Mockito.when(mockToken.getRawToken())
            .thenReturn("{irrelevant:token}");
        Mockito.when(mockToken.claim("tenant"))
            .thenReturn(Optional.of("BE"));

        Assertions.assertThat(sut.resolveTenantId())
            .isEqualTo("BE");
    }

    @Test
    void resolveTenantId_when_token_has_invlaid_claim_throws() {
        Mockito.when(mockToken.getRawToken())
            .thenReturn("{irrelevant:token}");
        Mockito.when(mockToken.claim("tenant"))
            .thenReturn(Optional.of("wrong"));

        Assertions.assertThatThrownBy(() -> sut.resolveTenantId())
            .isInstanceOf(AppFailureException.class)
            .hasMessageContaining("AppFailure.INVALID_TENANT_CLAIM");
    }

    @Test
    void resolveTenantId_when_overriden_works() {
        // ARRANGE
        Mockito.when(configServiceMock.getTenantOverride())
            .thenReturn(Optional.of(TenantEnum.BE));
        // ACT & ASSERT
        Assertions.assertThat(sut.resolveTenantId())
            .isEqualTo("BE");
    }
}
