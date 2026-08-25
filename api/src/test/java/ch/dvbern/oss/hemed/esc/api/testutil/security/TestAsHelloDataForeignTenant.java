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
 * The HelloData service account, but resolving to a tenant it has no business exporting.
 * <p>
 * This is the situation the Berechtigung alone does not prevent: {@code RolesAugmentor} grants
 * the machine client its Berechtigung from (azp, preferred_username) without looking at the
 * tenant, while the tenant comes from the request hostname. In production realm isolation makes
 * this hard to reach, but it is an operational control - this annotation exercises what happens
 * when it fails.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@TestSecurity(
    user = "testing-hellodata-foreign-tenant",
    roles = {
        HELLODATA_EXPORT_LESEN
    }
)
@OidcSecurity(
    claims = {
        @Claim(key = "sub", value = TestAsHelloDataForeignTenant.BENUTZER_ID_IN_KC),
        @Claim(key = "tenant", value = "BS"),
        @Claim(key = "family_name", value = "HelloData"),
        @Claim(key = "given_name", value = "ForeignTenant"),
        @Claim(key = "preferred_username", value = TestAsHelloDataForeignTenant.USERNAME),
        @Claim(key = "email", value = TestAsHelloDataForeignTenant.EMAIL),
        @Claim(key = "azp", value = TestAsHelloDataForeignTenant.AZP),
    }
)
@TestAsMarker
public @interface TestAsHelloDataForeignTenant {
    String BENUTZER_ID = "3d5b8e14-2c7a-4f61-9b30-8e1f4a2c7d55";
    String BENUTZER_ID_IN_KC = "6e9c2f80-4b13-4a27-8d55-7c0e3b1a9f42";
    // Same client identity as TestAsHelloDataBE - only the tenant differs.
    String EMAIL = TestAsHelloDataBE.EMAIL;
    String USERNAME = TestAsHelloDataBE.USERNAME;
    String AZP = TestAsHelloDataBE.AZP;
}
