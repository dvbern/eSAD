package ch.dvbern.oss.hemed.esc.api.testutil.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ch.dvbern.oss.hemed.esc.api.features.benutzer.ensure.JsonWebTokenClaims;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;

import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.ANLASSLISTE_LESEN_LAEUSE;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.ANLASSPLANUNG_LESEN_LAEUSE;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.ANLASSPLANUNG_SCHREIBEN_LAEUSE;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.EIGENE_BERECHTIGUNGEN_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.KINDER_LESEN_NICHT_MEDIZINISCH;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.KINDER_SCHREIBEN_NICHT_MEDIZINISCH;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.LABELS_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.LISTEN_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.SU_DATEN_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.SU_LAEUSE_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.SU_LAEUSE_SCHREIBEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.SU_VORABKLAERUNGEN_LESEN;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@TestSecurity(
    user = "testing-lausbeauftragte",
    roles = {
        SU_DATEN_LESEN,
        SU_VORABKLAERUNGEN_LESEN,
        EIGENE_BERECHTIGUNGEN_LESEN,
        ANLASSLISTE_LESEN_LAEUSE,
        ANLASSPLANUNG_LESEN_LAEUSE,
        ANLASSPLANUNG_SCHREIBEN_LAEUSE,
        SU_LAEUSE_LESEN,
        SU_LAEUSE_SCHREIBEN,
        LABELS_LESEN,
        LISTEN_LESEN,
        KINDER_LESEN_NICHT_MEDIZINISCH,
        KINDER_SCHREIBEN_NICHT_MEDIZINISCH,
    }
)
@OidcSecurity(
    claims = {
        @Claim(key = "sub", value = TestAsLausbeauftragteWIN.BENUTZER_ID_IN_KC),
        @Claim(key = "tenant", value = "WIN"),
        @Claim(key = "family_name", value = "Laus"),
        @Claim(key = "given_name", value = "Lisa"),
        @Claim(key = "preferred_username", value = TestAsLausbeauftragteWIN.USERNAME),
        @Claim(key = "email", value = TestAsLausbeauftragteWIN.EMAIL),
        @Claim(key = JsonWebTokenClaims.PHONE_CLAIM, value = TestAsLausbeauftragteWIN.TELEFON),
    }
)
public @interface TestAsLausbeauftragteWIN {
    String BENUTZER_ID = "9b564ebd-e4b1-43f6-acc1-917a608353c0";
    String BENUTZER_ID_IN_KC = "5b64f9ce-3037-4dd5-89ab-fa9da17c64f6";
    String EMAIL = "lausbeauftragte@mailbucket.dvbern.ch";
    String USERNAME = "lausbeauftragte@mailbucket.dvbern.ch";
    String TELEFON = "+41 61 123 45 70";
}
