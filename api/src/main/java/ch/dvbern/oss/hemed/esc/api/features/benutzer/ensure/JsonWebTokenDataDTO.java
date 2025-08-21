package ch.dvbern.oss.hemed.esc.api.features.benutzer.ensure;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import ch.dvbern.dvbstarter.shared.types.emailaddress.EmailAddress;
import ch.dvbern.oss.hemed.esc.api.shared.auth.FachRolle;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.jwt.Claims;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Value
@Slf4j
@AllArgsConstructor
public class JsonWebTokenDataDTO {
    @Nullable
    String session;
    @NonNull
    @IgnoreForIdClassTest
    UUID subjectId; // keycloak sends us its uuid as a string
    @NonNull
    String username;
    @NonNull
    String familyName;
    @NonNull
    String givenName;  // Enthaelt nur den ersten Vornamen
    @NonNull
    EmailAddress email;
    @NonNull
    String issuer;
    @Nullable
    String mobileNummerClaim;
    @Nullable
    String phoneNumberClaim;
    @Nullable
    String titleClaim;

    @NonNull
    private final Set<FachRolle> fachRollen;

    private static final String[] KEYCLOAK_FIXED_ROLES = { "default-roles-bs", "default-roles-esc-bs" };

    @NonNull
    public static JsonWebTokenDataDTO fromJsonWebToken(@NonNull JsonWebToken jsonWebToken) {
        return new JsonWebTokenDataDTO(jsonWebToken);
    }

    public JsonWebTokenDataDTO(@NonNull JsonWebToken jsonWebToken) {
        // User Properties (immer vorhanden)
        session = jsonWebToken.getClaim(JsonWebTokenClaims.SESSION_ID_CLAIM);
        subjectId = UUID.fromString(jsonWebToken.getSubject());
        username = jsonWebToken.getClaim(Claims.preferred_username);
        familyName = jsonWebToken.getClaim(Claims.family_name);
        givenName = jsonWebToken.getClaim(Claims.given_name);
        email = EmailAddress.fromString(jsonWebToken.getClaim(Claims.email));
        issuer = jsonWebToken.getIssuer();
        // optionale claims
        mobileNummerClaim = (String) jsonWebToken.claim(JsonWebTokenClaims.MOBILE_NUMMER).orElse(null);
        titleClaim = (String) jsonWebToken.claim(JsonWebTokenClaims.TITLE_CLAIM).orElse(null);
        phoneNumberClaim = (String) jsonWebToken.claim(JsonWebTokenClaims.PHONE_CLAIM).orElse(null);

        // extract fachrollen in resource_access.esc.roles
        Optional<JsonArray> fachrolleClaim = extractFachrollen(jsonWebToken);
        if (fachrolleClaim.isPresent()) {
            JsonArray fachrollenClaim = fachrolleClaim.get();
            Set<String> collectedRoles =
                fachrollenClaim.stream().map(arrEle -> ((JsonString) arrEle).getString()).collect(Collectors.toSet());
            fachRollen = mapRoles(collectedRoles);
        } else {
            fachRollen = new HashSet<>();
        }
    }

    @NonNull
    private static Optional<JsonArray> extractFachrollen(@NonNull JsonWebToken jsonWebToken) {
        return jsonWebToken.<JsonObject>claim(JsonWebTokenClaims.CLIENT_ROLE_CLAIM)
            .flatMap((jsonValue -> Optional.ofNullable(jsonValue.getJsonObject(JsonWebTokenClaims.CLIENT_ROLE_CLIENT_NAME_CLAIM))))
            .flatMap((jsonValue -> Optional.ofNullable(jsonValue.getJsonArray(JsonWebTokenClaims.CLIENT_ROLE_ROLES_CLAIM))));
    }

    @NonNull
    public static Set<FachRolle> mapRoles(@NonNull Set<String> roles) {
        Set<FachRolle> set = new HashSet<>();
        List<String> rolesWithoutFixedOrCompositeRoles =
            filterOutKeycloakFixedRoles(filterOutKeycloakFixedRoles(roles));

        for (String role : rolesWithoutFixedOrCompositeRoles) {
            try {
                FachRolle benutzerRolle = FachRolle.valueOf(role);
                set.add(benutzerRolle);
            } catch (IllegalArgumentException exception) {
                LOG.warn("Could not map rollenstring {} to a Benutzerrolle", role);
            }
        }
        return set;
    }

    @NonNull
    private static List<String> filterOutKeycloakFixedRoles(@NonNull Collection<String> roles) {
        return roles.stream().filter(
            s -> Arrays.stream(KEYCLOAK_FIXED_ROLES).noneMatch(keycloakDefaultRole -> keycloakDefaultRole.equals(s))
        ).toList();
    }
}
