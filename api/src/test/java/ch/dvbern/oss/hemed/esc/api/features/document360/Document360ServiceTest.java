package ch.dvbern.oss.hemed.esc.api.features.document360;

import java.net.URI;
import java.util.List;
import java.util.stream.Stream;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.features.document360.json.Document360CodeRequestJson;
import ch.dvbern.oss.hemed.esc.api.features.document360.json.Document360CodeResponseJson;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.testutil.builder.BenutzerBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class Document360ServiceTest {

    private ConfigService configService;
    private BenutzerService benutzerService;
    private Document360Client document360Client;
    private Document360Service document360Service;
    private TenantContext tenantContext;

    @BeforeEach
    void setUp() throws Exception {
        configService = Mockito.mock(ConfigService.class);
        benutzerService = Mockito.mock(BenutzerService.class);
        document360Client = Mockito.mock(Document360Client.class);
        tenantContext = Mockito.mock(TenantContext.class);
        document360Service = new Document360Service(configService, benutzerService, tenantContext, document360Client);

        when(configService.getDocument360KbLoginUrl()).thenReturn(URI.create("https://kb.document360.io/jwt/authorize")
            .toURL());
        when(configService.getDocument360ClientId()).thenReturn("test-client-id");
        when(configService.getDocument360ClientSecret()).thenReturn("test-client-secret");
        when(tenantContext.get()).thenReturn(TenantEnum.BS);
    }

    @Test
    void authenticateAndGetRedirectUri_withAdminUser_returnsRedirectUriWithCode() {
        Benutzer adminBenutzer = BenutzerBuilder.create(builder -> builder
            .withSystemRolle(List.of(FachRolle.ADMIN))
        );
        when(benutzerService.getCurrentBenutzerOrThrowException()).thenReturn(adminBenutzer);

        Document360CodeResponseJson responseJson = new Document360CodeResponseJson();
        responseJson.setCode("jwt-auth-code-123");
        when(document360Client.generateCode(eq("Basic dGVzdC1jbGllbnQtaWQ6dGVzdC1jbGllbnQtc2VjcmV0"), any()))
            .thenReturn(responseJson);

        URI result = document360Service.authenticateAndGetRedirectUri();

        assertThat(result).isEqualTo(URI.create("https://kb.document360.io/jwt/authorize?code=jwt-auth-code-123"));

        ArgumentCaptor<Document360CodeRequestJson> requestCaptor = ArgumentCaptor.forClass(Document360CodeRequestJson.class);
        verify(document360Client).generateCode(eq("Basic dGVzdC1jbGllbnQtaWQ6dGVzdC1jbGllbnQtc2VjcmV0"), requestCaptor.capture());

        Document360CodeRequestJson capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.getReaderGroupIds()).containsExactly("5c037035-1783-4e06-a496-f7d1fb7edaee");
        assertThat(capturedRequest.getEmailId()).isEqualTo(adminBenutzer.getEmail().getAddress());
        assertThat(capturedRequest.getFirstName()).isEqualTo(adminBenutzer.getVorname());
        assertThat(capturedRequest.getLastName()).isEqualTo(adminBenutzer.getNachname());
    }

    @Test
    void authenticateAndGetRedirectUri_withSupportUser_returnsRedirectUriWithSupportReaderGroup() {
        Benutzer supportBenutzer = BenutzerBuilder.create(builder -> builder
            .withSystemRolle(List.of(FachRolle.SUPPORT))
        );
        when(benutzerService.getCurrentBenutzerOrThrowException()).thenReturn(supportBenutzer);

        Document360CodeResponseJson responseJson = new Document360CodeResponseJson();
        responseJson.setCode("support-auth-code");
        when(document360Client.generateCode(any(), any())).thenReturn(responseJson);

        URI result = document360Service.authenticateAndGetRedirectUri();

        assertThat(result).isEqualTo(URI.create("https://kb.document360.io/jwt/authorize?code=support-auth-code"));

        ArgumentCaptor<Document360CodeRequestJson> requestCaptor = ArgumentCaptor.forClass(Document360CodeRequestJson.class);
        verify(document360Client).generateCode(any(), requestCaptor.capture());

        assertThat(requestCaptor.getValue().getReaderGroupIds())
            .containsExactly("5c037035-1783-4e06-a496-f7d1fb7edaee");
    }

    @ParameterizedTest
    @MethodSource("tenantReaderGroups")
    void authenticateAndGetRedirectUri_passesCorrectReaderGroupsForTenant(
        TenantEnum tenant,
        List<String> expectedReaderGroups
    ) {
        when(tenantContext.get()).thenReturn(tenant);
        Benutzer benutzer = BenutzerBuilder.defaultBenutzer();
        when(benutzerService.getCurrentBenutzerOrThrowException()).thenReturn(benutzer);

        Document360CodeResponseJson responseJson = new Document360CodeResponseJson();
        responseJson.setCode("jwt-auth-code");
        when(document360Client.generateCode(any(), any())).thenReturn(responseJson);

        URI result = document360Service.authenticateAndGetRedirectUri();

        assertThat(result).isEqualTo(URI.create("https://kb.document360.io/jwt/authorize?code=jwt-auth-code"));

        ArgumentCaptor<Document360CodeRequestJson> requestCaptor = ArgumentCaptor.forClass(Document360CodeRequestJson.class);
        verify(document360Client).generateCode(any(), requestCaptor.capture());

        assertThat(requestCaptor.getValue().getReaderGroupIds()).isEqualTo(expectedReaderGroups);
    }

    private static Stream<Arguments> tenantReaderGroups() {
        return Stream.of(
            Arguments.of(TenantEnum.DEFAULT, List.of()),
            Arguments.of(TenantEnum.BE, List.of("d1b4aeec-6346-4392-98f4-08aefd06c548")),
            Arguments.of(TenantEnum.BS, List.of("5c037035-1783-4e06-a496-f7d1fb7edaee")),
            Arguments.of(TenantEnum.WIN, List.of("9983387a-4783-4414-887b-42d14609251b"))
        );
    }

    @Test
    void authenticateAndGetRedirectUri_whenClientThrowsException_throwsAppFailureException() {
        Benutzer benutzer = BenutzerBuilder.defaultBenutzer();
        when(benutzerService.getCurrentBenutzerOrThrowException()).thenReturn(benutzer);
        when(document360Client.generateCode(any(), any())).thenThrow(new RuntimeException("API error"));

        assertThatThrownBy(() -> document360Service.authenticateAndGetRedirectUri())
            .isInstanceOf(AppFailureException.class)
            .hasMessageContaining("Document360 JWT SSO: generateCode failed");
    }

    @Test
    void authenticateAndGetRedirectUri_whenClientReturnsNullCode_throwsAppFailureException() {
        Benutzer benutzer = BenutzerBuilder.defaultBenutzer();
        when(benutzerService.getCurrentBenutzerOrThrowException()).thenReturn(benutzer);
        Document360CodeResponseJson responseJson = new Document360CodeResponseJson();
        when(document360Client.generateCode(any(), any())).thenReturn(responseJson);

        assertThatThrownBy(() -> document360Service.authenticateAndGetRedirectUri())
            .isInstanceOf(AppFailureException.class)
            .hasMessageContaining("Document360 JWT SSO: no code returned");
    }
}
