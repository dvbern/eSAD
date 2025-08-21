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

import java.util.Set;

import ch.dvbern.oss.hemed.esc.api.features.benutzer.ensure.JsonWebTokenDataDTO;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.SecurityIdentityAugmentor;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;

/**
 * This class is responsible for augmenting the SecurityIdentity with the roles of the user. To that end it checks all
 * the places in the database were such roles could be stored and adds them to the SecurityIdentity.
 */
@ApplicationScoped
@Slf4j
@RequiredArgsConstructor
public class RolesAugmentor implements SecurityIdentityAugmentor {

    private static final FachRolle[] EMPTY_FACHROLLEN = new FachRolle[0];
    private final ConfigService configService;

    @Override
    public Uni<SecurityIdentity> augment(SecurityIdentity identity, AuthenticationRequestContext context) {
       if (identity.isAnonymous()) {
           return Uni.createFrom().item(identity);
       }
       return context.runBlocking(() -> augmentFromjsonWebToken(identity));
    }

    private SecurityIdentity augmentFromjsonWebToken(SecurityIdentity identity) {
        return determineRoles(identity);
    }

    @ActivateRequestContext
    public SecurityIdentity determineRoles(SecurityIdentity identity) {
        QuarkusSecurityIdentity.Builder builder = QuarkusSecurityIdentity.builder(identity);
        // cast to json web token to get the keycloak id from the token (subject)
        JsonWebToken jsonWebToken = (JsonWebToken) identity.getPrincipal();

        String email = jsonWebToken.getClaim(Claims.email);
        String preferedUserName = jsonWebToken.getClaim(Claims.preferred_username);
        String authorizedParty = jsonWebToken.getClaim(Claims.azp);

        Set<Berechtigung> berechtigungen;

        if(isEscadaClientAndUser(authorizedParty, preferedUserName)){
            berechtigungen = Set.of(Berechtigung.ESCADA_PUSH); // escada user has fixed role
        } else {
            FachRolle[] fachRollen = JsonWebTokenDataDTO.fromJsonWebToken(jsonWebToken).getFachRollen().toArray(EMPTY_FACHROLLEN);
            berechtigungen = FachRolle.collectAllBerechtigungen(fachRollen);
        }

        if(!berechtigungen.isEmpty()) {
            berechtigungen.stream().map((Berechtigung::getBerechtigungName)).forEach(builder::addRole);
            LOG.debug("The current user '{}' has the following roles in esc: '{}'", email, berechtigungen.toString());
        } else {
            LOG.info("The current user '{}' has no roles in esc", email);
        }
        return builder.build();
    }

    boolean isEscadaClientAndUser(String authorizedParty, String userName){
        return configService.getEscadaClientUser().equals(userName) && configService.getEscadaAZP().equals(authorizedParty);
    }
}
