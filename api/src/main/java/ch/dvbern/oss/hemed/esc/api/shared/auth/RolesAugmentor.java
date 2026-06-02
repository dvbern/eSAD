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
import java.util.Set;

import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.BenutzerService;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.ensure.JsonWebTokenDataDTO;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.settings.TenantSettingsService;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.SecurityIdentityAugmentor;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.quarkus.vertx.http.runtime.security.HttpSecurityUtils;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
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
        if (routingContext != null) {
            var absoluteuri = routingContext.request().absoluteURI();
            URI uri = URI.create(absoluteuri);
            TenantEnum tenantFromUrl = TenantExtractorFromUrlRequestFilter.getTenantFromUrl(uri);
            tenantProducer.setCurrentTenant(tenantFromUrl);
        }
        return context.runBlocking(() -> augmentWithRoles(identity));
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
        return context.runBlocking(() -> augmentWithRoles(identity));
    }

    @NonNull
    @ActivateRequestContext
    SecurityIdentity augmentWithRoles(@NonNull SecurityIdentity identity) {
        // Je nach TenantSetting: Die Rollen aus dem Token oder aus der DB lesen
        boolean ignoreFachrollenFromKeycloak =
            tenantSettingsService.isBenutzerverwaltungIgnoreFachRolleFromKeycloakEnabled();
        if (ignoreFachrollenFromKeycloak) {
            return augmentFromDatabase(identity);
        }
        return augmentFromJsonWebToken(identity);
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
        String preferedUserName = jsonWebToken.getClaim(Claims.preferred_username);
        String authorizedParty = jsonWebToken.getClaim(Claims.azp);

        Set<Berechtigung> berechtigungen;
        if (isEscadaClientAndUser(authorizedParty, preferedUserName)) {
            LOG.info("The current user '{}' is an escada client user", email);
            berechtigungen = Set.of(Berechtigung.ESCADA_PUSH); // escada user has fixed role
        } else if (isFileIngressClientAndUser(authorizedParty, preferedUserName)) {
            LOG.info("The current user '{}' is a file ingress client user", email);
            berechtigungen = Set.of(Berechtigung.FILE_INGRESS_PUSH); // file ingress user has fixed role
        } else if (isPupilClientAndUser(authorizedParty, preferedUserName)) {
            LOG.info("The current user '{}' is a pupil client user", email);
            berechtigungen = Set.of(Berechtigung.PUPIL_PUSH); // pupil user has fixed role
        } else {
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
        }

        if (!berechtigungen.isEmpty()) {
            berechtigungen.stream().map((Berechtigung::getBerechtigungName)).forEach(builder::addRole);
            LOG.debug("The current user '{}' has the following roles in esc: '{}'", email, berechtigungen.toString());
        } else {
            LOG.info("The current user '{}' has no roles in esc", email);
        }
        return builder.build();
    }

    boolean isEscadaClientAndUser(@NonNull String authorizedParty, @NonNull String userName) {
        return configService.getEscadaClientUser().equals(userName) && configService.getEscadaAZP()
            .equals(authorizedParty);
    }

    boolean isFileIngressClientAndUser(@NonNull String authorizedParty, @NonNull String userName) {
        return configService.getFileIngressClientUser().equals(userName)
            && configService.getFileIngressAZP().equals(authorizedParty);
    }

    boolean isPupilClientAndUser(@NonNull String authorizedParty, @NonNull String userName) {
        return configService.getPupilClientUser().equals(userName)
            && configService.getPupilAZP().equals(authorizedParty);
    }
}
