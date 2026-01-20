package ch.dvbern.oss.hemed.esc.api.testutil.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;

import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.PUPIL_PUSH;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@TestSecurity(
    user = TestAsPupil.USERNAME,
    // java Annotation require compile time Constants so we need to define the array each time :(
    roles = {
        PUPIL_PUSH
    }
)
@OidcSecurity(
    claims = {
        @Claim(key = "sub", value = TestAsPupil.BENUTZER_ID),
        @Claim(key = "tenant", value = "WIN"),
        @Claim(key = "family_name", value = "Serviceaccount"),
        @Claim(key = "given_name", value = "Pupil"),
        @Claim(key = "preferred_username", value = TestAsPupil.USERNAME),
        @Claim(key = "email", value = TestAsPupil.EMAIL),
        @Claim(key = "azp", value = TestAsPupil.AZP),
    }
)
public @interface TestAsPupil {
    String BENUTZER_ID = "774e7a9f-8908-4aab-8fa2-2d21cdc7cda8";
    String EMAIL = "pupil-client@dvbern.ch";
    String USERNAME = "pupil-client@dvbern.ch";
    String TELEFON = null;
    String AZP = "pupil-client";
}
