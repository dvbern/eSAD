package ch.dvbern.oss.hemed.esc.api.features.document360;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.UriBuilder;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.features.document360.json.Document360CodeRequestJson;
import ch.dvbern.oss.hemed.esc.api.features.document360.json.Document360CodeResponseJson;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;

import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * Implements Document360 JWT SSO for the currently authenticated ESC user.
 *
 * <p>ESC never issues the Document360 session itself. It maps Tenants
 * Document360 reader groups, asks Document360 Identity for a one-time {@code code}, and
 * returns a redirect URI {@code {kb-login-url}?code=}.</p>
 *
 * @see <a href="https://docs.document360.com/docs/configuring-the-jwt-sso">Document360 JWT SSO</a>
 */
@Service
@ApplicationScoped
@Slf4j
public class Document360Service {

    /**
     * Document360 reader groups, one per Mandant. A reader group restricts which project versions,
     * languages, categories and articles the reader may see.
     *
     * <p>Copy the IDs from Document360 &rarr; Settings &rarr; Users &amp; Security &rarr; Reader
     * groups. An unknown ID does <em>not</em> fail the request: Document360 issues the token anyway
     * and the reader silently ends up without the intended restriction.</p>
     */
    private static final String READER_GROUP_BE = "d1b4aeec-6346-4392-98f4-08aefd06c548";
    private static final String READER_GROUP_BS = "5c037035-1783-4e06-a496-f7d1fb7edaee";
    private static final String READER_GROUP_WIN = "9983387a-4783-4414-887b-42d14609251b";

    /**
     * Lifetime of the Document360 SSO session requested via {@code tokenValidity}.
     */
    private static final int TOKEN_VALIDITY_MINUTES = 60;

    private final ConfigService configService;
    private final BenutzerService benutzerService;
    private final Document360Client document360Client;
    private final TenantContext tenantContext;

    public Document360Service(
        ConfigService configService,
        BenutzerService benutzerService,
        TenantContext tenantContext,
        @RestClient Document360Client document360Client
    ) {
        this.configService = configService;
        this.benutzerService = benutzerService;
        this.tenantContext = tenantContext;
        this.document360Client = document360Client;
    }

    public @NonNull URI authenticateAndGetRedirectUri() {
        Benutzer currentBenutzer = benutzerService.getCurrentBenutzerOrThrowException();
        TenantEnum tenant = tenantContext.get();

        String code = requestCode(currentBenutzer, tenant);

        return UriBuilder.fromUri(configService.getDocument360KbLoginUrl().toString())
            .queryParam("code", code)
            .build();
    }

    private @NonNull String requestCode(@NonNull Benutzer benutzer, @NonNull TenantEnum tenant) {
        Document360CodeRequestJson payload = buildPayload(benutzer, tenant);

        Document360CodeResponseJson response;
        try {
            response = document360Client.generateCode(basicAuthHeader(), payload);
        } catch (Exception e) {
            LOG.error("Document360 JWT SSO: generateCode failed for tenant {}", tenant, e);
            throw new AppFailureException(
                AppFailureMessage.internalError("Document360 JWT SSO: generateCode failed"));
        }

        if (response == null || response.getCode() == null || response.getCode().isBlank()) {
            LOG.error("Document360 JWT SSO: no code returned for tenant {}", tenant);
            throw new AppFailureException(
                AppFailureMessage.internalError("Document360 JWT SSO: no code returned"));
        }

        return response.getCode();
    }

    private @NonNull String basicAuthHeader() {
        String credentials =
            configService.getDocument360ClientId() + ':' + configService.getDocument360ClientSecret();

        return "Basic " + Base64.getEncoder()
            .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
    }

    private static @NonNull Document360CodeRequestJson buildPayload(
        @NonNull Benutzer benutzer,
        @NonNull TenantEnum tenant
    ) {
        String email = benutzer.getEmail().getAddress();

        return new Document360CodeRequestJson(
            email,
            benutzer.getVorname(),
            benutzer.getNachname(),
            email,
            readerGroupIdsFor(tenant),
            TOKEN_VALIDITY_MINUTES
        );
    }

    private static @NonNull List<String> readerGroupIdsFor(@NonNull TenantEnum tenant) {
        return switch (tenant) {
            case DEFAULT -> List.of();
            case BE -> List.of(READER_GROUP_BE);
            case BS -> List.of(READER_GROUP_BS);
            case WIN -> List.of(READER_GROUP_WIN);
        };
    }
}
