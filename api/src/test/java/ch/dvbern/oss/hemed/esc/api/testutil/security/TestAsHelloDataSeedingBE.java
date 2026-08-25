package ch.dvbern.oss.hemed.esc.api.testutil.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;

import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.HELLODATA_EXPORT_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.SYSTEM_ADMIN;

/**
 * Only for tests that have to write test data in their ARRANGE step.
 * <p>
 * The real HelloData service account holds <b>only</b> {@code HELLODATA_EXPORT_LESEN} (see
 * {@link TestAsHelloDataBE}) and therefore has no {@code Benutzer} record -
 * {@code AbstractEntityListener.prePersist} then finds no logged-in Benutzer.
 * {@code SYSTEM_ADMIN} bypasses that lookup and allows creating test data.
 * <p>
 * For assertions about the authorization check itself keep using
 * {@link TestAsHelloDataBE}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@TestSecurity(
    user = "testing-hellodata-seeding",
    roles = {
        HELLODATA_EXPORT_LESEN,
        SYSTEM_ADMIN
    }
)
@OidcSecurity(
    claims = {
        @Claim(key = "sub", value = TestAsHelloDataSeedingBE.BENUTZER_ID_IN_KC),
        @Claim(key = "tenant", value = "BE"),
        @Claim(key = "family_name", value = "HelloData"),
        @Claim(key = "given_name", value = "Seeding"),
        @Claim(key = "preferred_username", value = TestAsHelloDataSeedingBE.USERNAME),
        @Claim(key = "email", value = TestAsHelloDataSeedingBE.EMAIL),
        @Claim(key = "azp", value = TestAsHelloDataSeedingBE.AZP),
    }
)
@TestAsMarker
public @interface TestAsHelloDataSeedingBE {
    String BENUTZER_ID = "7b3c9d21-5e4f-4a8b-9c1d-3e2f6a7b8c90";
    String BENUTZER_ID_IN_KC = "1a2b3c4d-5e6f-4718-9a0b-1c2d3e4f5061";
    // Same client identity as TestAsHelloDataBE - ClientFilter matches on user and azp, so the
    // two must not drift apart.
    String EMAIL = TestAsHelloDataBE.EMAIL;
    String USERNAME = TestAsHelloDataBE.USERNAME;
    String AZP = TestAsHelloDataBE.AZP;
}
