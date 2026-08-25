/*
 * Copyright (C) 2023 DV Bern AG, Switzerland
 *
 * Das vorliegende Dokument, einschliesslich aller seiner Teile, ist urheberrechtlich
 * geschuezt. Jede Verwertung ist ohne Zustimmung der DV Bern AG unzulaessig. Dies gilt
 * insbesondere für Vervielfaeltigungen, die Einspeicherung und Verarbeitung in
 * elektronischer Form. Wird das Dokument einem Kunden im Rahmen der Projektarbeit zur
 * Ansicht uebergeben, ist jede weitere Verteilung durch den Kunden an Dritte untersagt.
 */

package ch.dvbern.oss.hemed.esc.api.shared.auth;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;

import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.ensure.JsonWebTokenDataDTO;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ClientAccountConfig.SvcAccountClientInfo;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSettingsService;

import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.SecurityIdentityAugmentor;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.quarkus.vertx.http.runtime.security.HttpSecurityUtils;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;

import static ch.dvbern.oss.hemed.esc.api.features.benutzer.ensure.JsonWebTokenClaims.TENANT_CLAIM;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.DVBSupportRoleUtil.assignDVBSupportBerechtigungenForTenant;

/**
 * This class is responsible for augmenting the SecurityIdentity with the roles of the user. To that end it checks all
 * the places in the database were such roles could be stored and adds them to the SecurityIdentity.
 */
@ApplicationScoped
@Slf4j
@RequiredArgsConstructor
public class RolesAugmentor implements SecurityIdentityAugmentor {

    public static final FachRolle[] EMPTY_FACHROLLEN = new FachRolle[0];

    private final TenantSettingsService tenantSettingsService;
    private final BenutzerService benutzerService;
    private final ConfigService configService;
    private final TenantProducer tenantProducer;

    @Override
    @NonNull
    public Uni<SecurityIdentity> augment(
        @NonNull SecurityIdentity identity,
        @NonNull AuthenticationRequestContext context,
        @NonNull Map<String, Object> attributes
    ) {
        if (identity.isAnonymous()) {
            return Uni.createFrom().item(identity);
        }
        // extracting tenant from routing context so we can figure out if we need to read the
        // rollen from the db or not
        RoutingContext routingContext = HttpSecurityUtils.getRoutingContextAttribute(attributes);
        // We resolve and remember the tenatn here (resolved from url). It must be published to the request-scoped
        // TenantProducer inside augmentWithRoles, which activates the request context.
        // Trying to do this here using the @RequestScoped producer would fail with ContextNotActiveException whenever
        // augmentation runs before the request context exists - which is exactly what happens with proactive authentication.
        TenantEnum tenantFromUrl = routingContext == null
            ? null
            : TenantExtractorFromUrlRequestFilter.getTenantFromUrl(URI.create(routingContext.request().absoluteURI()));

        return context.runBlocking(() -> augmentWithRoles(identity, tenantFromUrl));
    }

    @Override
    @NonNull
    public Uni<SecurityIdentity> augment(
        @NonNull SecurityIdentity identity,
        @NonNull AuthenticationRequestContext context
    ) {
        if (identity.isAnonymous()) {
            return Uni.createFrom().item(identity);
        }
        return context.runBlocking(() -> augmentWithRoles(identity, null));
    }

    @NonNull
    @ActivateRequestContext
    SecurityIdentity augmentWithRoles(@NonNull SecurityIdentity identity, @Nullable TenantEnum tenantFromUrl) {
        if (tenantFromUrl != null) {
            tenantProducer.setCurrentTenant(tenantFromUrl);
        }
        JsonWebToken jsonWebToken = (JsonWebToken) identity.getPrincipal();
        String preferedUserName = jsonWebToken.getClaim(Claims.preferred_username);
        String authorizedParty = jsonWebToken.getClaim(Claims.azp);

        // Machine-to-machine service accounts (escada, file ingress, pupil, hellodata) are special,
        // they get a single fixed Berechtigung. This has to run before the tenant-dependent augmentation
        Optional<Berechtigung> clientBerechtigung =
            findMatchingServiceClientAccountBerechtigung(authorizedParty, preferedUserName);
        if (clientBerechtigung.isPresent()) {
            LOG.info("The current user '{}' is a configured service client account", preferedUserName);
            return augmentWithFixedBerechtigung(identity, clientBerechtigung.get());
        }

        // Je nach TenantSetting: Die Rollen aus dem Token oder aus der DB lesen
        boolean ignoreFachrollenFromKeycloak =
            tenantSettingsService.isBenutzerverwaltungIgnoreFachRolleFromKeycloakEnabled();
        if (ignoreFachrollenFromKeycloak) {
            return augmentFromDatabase(identity);
        }
        return augmentFromJsonWebToken(identity);
    }

    /**
     * Finds the fixed {@link Berechtigung} for a configured machine-to-machine client account
     * matching the given authorized party and username, or an empty optional if the caller is
     * not one of the configured clients.
     */
    @NonNull
    private Optional<Berechtigung> findMatchingServiceClientAccountBerechtigung(
        @Nullable String authorizedParty,
        @Nullable String userName
    ) {
        return configService.getClientAccounts().entries().stream()
            .filter(configEntry -> configEntry.username().equals(userName) && configEntry.azp().equals(authorizedParty))
            .map(SvcAccountClientInfo::berechtigung)
            .findFirst();
    }

    @NonNull
    private SecurityIdentity augmentWithFixedBerechtigung(
        @NonNull SecurityIdentity identity,
        @NonNull Berechtigung berechtigung
    ) {
        QuarkusSecurityIdentity.Builder builder = QuarkusSecurityIdentity.builder(identity);
        builder.addRole(berechtigung.getBerechtigungName());
        return builder.build();
    }

    @NonNull
    private SecurityIdentity augmentFromDatabase(@NonNull SecurityIdentity identity) {
        QuarkusSecurityIdentity.Builder builder = QuarkusSecurityIdentity.builder(identity);
        JsonWebToken jsonWebToken = (JsonWebToken) identity.getPrincipal();
        String email = jsonWebToken.getClaim(Claims.email);
        benutzerService.getDesiredBerechtigungen(EmailAddress.fromString(email))
            .stream()
            .map(Berechtigung::getBerechtigungName)
            .forEach(builder::addRole);
        return builder.build();
    }

    @NonNull
    private SecurityIdentity augmentFromJsonWebToken(@NonNull SecurityIdentity identity) {
        return determineRoles(identity);
    }

    @NonNull
    private SecurityIdentity determineRoles(@NonNull SecurityIdentity identity) {
        QuarkusSecurityIdentity.Builder builder = QuarkusSecurityIdentity.builder(identity);
        // cast to json web token to get the keycloak id from the token (subject)
        JsonWebToken jsonWebToken = (JsonWebToken) identity.getPrincipal();

        String email = jsonWebToken.getClaim(Claims.email);

        Set<Berechtigung> berechtigungen;
        String tenantClaim = jsonWebToken.getClaim(TENANT_CLAIM);
        if (tenantClaim != null) {
            // handle supporter case
            if (DVBSupportRoleUtil.isDVBSupport(tenantClaim)) {
                TenantEnum tenantFromUrl = tenantProducer.produceTenant();
                berechtigungen = assignDVBSupportBerechtigungenForTenant(email, tenantFromUrl);
            }
            // handle standard case
            else if (TenantEnum.fromString(tenantClaim) != null) {
                TenantEnum tenantEnum = TenantEnum.fromString(tenantClaim);
                FachRolle[] fachRollen =
                    JsonWebTokenDataDTO.fromJsonWebToken(jsonWebToken).getFachRollen().toArray(EMPTY_FACHROLLEN);
                berechtigungen = FachRolle.collectAllBerechtigungen(Objects.requireNonNull(tenantEnum), fachRollen);
            } else {
                berechtigungen = Set.of(); // tenant or ESC-FACHROLLE claim  was missing in the token
            }
        } else {
            berechtigungen = Set.of(); // tenant or ESC-FACHROLLE claim  was missing in the token
        }

        if (!berechtigungen.isEmpty()) {
            berechtigungen.stream().map((Berechtigung::getBerechtigungName)).forEach(builder::addRole);
            LOG.debug("The current user '{}' has the following roles in esc: '{}'", email, berechtigungen.toString());
        } else {
            LOG.info("The current user '{}' has no roles in esc", email);
        }
        return builder.build();
    }
}
