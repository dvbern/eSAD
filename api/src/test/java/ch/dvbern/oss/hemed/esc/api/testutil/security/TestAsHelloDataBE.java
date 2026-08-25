package ch.dvbern.oss.hemed.esc.api.testutil.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;

import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.HELLODATA_EXPORT_LESEN;

/**
 * HelloData service account: holds the export Berechtigung and nothing else.
 * <p>
 * Mirrors what {@code RolesAugmentor} grants the machine client - service accounts have no
 * Benutzer record and therefore no FachRolle, so there is no FachRolle to mirror.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@TestSecurity(
    user = "testing-hellodata",
    roles = {
        HELLODATA_EXPORT_LESEN
    }
)
@OidcSecurity(
    claims = {
        @Claim(key = "sub", value = TestAsHelloDataBE.BENUTZER_ID_IN_KC),
        @Claim(key = "tenant", value = "BE"),
        @Claim(key = "family_name", value = "HelloData"),
        @Claim(key = "given_name", value = "Service"),
        @Claim(key = "preferred_username", value = TestAsHelloDataBE.USERNAME),
        @Claim(key = "email", value = TestAsHelloDataBE.EMAIL),
        @Claim(key = "azp", value = TestAsHelloDataBE.AZP),
    }
)
@TestAsMarker
public @interface TestAsHelloDataBE {
    String BENUTZER_ID = "9f2a4c31-6c1e-4c2f-9d4b-2b7c5e8a1f03";
    String BENUTZER_ID_IN_KC = "5c8e1a77-1f3d-45b2-8e77-9a4c2d0b6e12";
    // Must match esc.hellodata-export.client.user / .azp - ClientFilter checks both.
    String EMAIL = "";
    String USERNAME = "service-account-hello-data-client";
    String AZP = "hello-data-client";
}
