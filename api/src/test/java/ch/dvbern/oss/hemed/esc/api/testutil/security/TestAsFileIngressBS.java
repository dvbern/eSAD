package ch.dvbern.oss.hemed.esc.api.testutil.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;

import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.FILE_INGRESS_PUSH;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@TestSecurity(
    user = TestAsFileIngressBS.USERNAME,
    // java Annotation require compile time Constants so we need to define the array each time :(
    roles = {
        FILE_INGRESS_PUSH
    }
)
@OidcSecurity(
    claims = {
        @Claim(key = "sub", value = TestAsFileIngressBS.BENUTZER_ID),
        @Claim(key = "tenant", value = "BS"),
        @Claim(key = "family_name", value = "Serviceaccount"),
        @Claim(key = "given_name", value = "FileIngress"),
        @Claim(key = "preferred_username", value = TestAsFileIngressBS.USERNAME),
        @Claim(key = "email", value = TestAsFileIngressBS.EMAIL),
        @Claim(key = "azp", value = TestAsFileIngressBS.AZP),
    }
)
public @interface TestAsFileIngressBS {
    String BENUTZER_ID = "03a61ae1-41a1-4b2f-bc8c-8b4caf2cc85e";
    String EMAIL = "file-ingress-client@dvbern.ch";
    String USERNAME = "file-ingress-client@dvbern.ch";
    String TELEFON = null;
    String AZP = "file-ingress-client";
}
