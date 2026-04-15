package ch.dvbern.oss.hemed.esc.api.testutil.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;

import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.ESCADA_PUSH;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@TestSecurity(
    user = TestAsEscadaBS.USERNAME,
    // java Annotation require compile time Constants so we need to define the array each time :(
    roles = {
        ESCADA_PUSH
    }
)
@OidcSecurity(
    claims = {
        @Claim(key = "sub", value = TestAsEscadaBS.BENUTZER_ID),
        @Claim(key = "tenant", value = "BS"),
        @Claim(key = "family_name", value = "Serviceaccount"),
        @Claim(key = "given_name", value = "Escada"),
        @Claim(key = "preferred_username", value = TestAsEscadaBS.USERNAME),
        @Claim(key = "email", value = TestAsEscadaBS.EMAIL),
        @Claim(key = "azp", value = TestAsEscadaBS.AZP),
    }
)
public @interface TestAsEscadaBS {
    String BENUTZER_ID = "53483f35-fd62-437f-b366-32f08cc9d868";
    String EMAIL = "escada-client@dvbern.ch";
    String USERNAME = "escada-client@dvbern.ch";
    String TELEFON = null;
    String AZP = "escada-client";
}
