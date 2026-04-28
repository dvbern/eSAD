package ch.dvbern.oss.hemed.esc.api.testutil.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ch.dvbern.oss.hemed.esc.api.features.benutzer.ensure.JsonWebTokenClaims;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;

import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.ANLASSLISTE_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.ANLASSLISTE_LESEN_LAEUSE;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.ANLASSPLANUNG_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.ANLASSPLANUNG_LESEN_LAEUSE;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.ANLASSPLANUNG_SCHREIBEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.ANLASSPLANUNG_SCHREIBEN_LAEUSE;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.ARCHIVIERUNG;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.AUFGABEN_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.AUFGABEN_SCHREIBEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.EIGENE_BERECHTIGUNGEN_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.IMPFSTOFFE_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.KINDER_ALLE_NOTIZEN_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.KINDER_ALLE_NOTIZEN_SCHREIBEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.KINDER_LESEN_MEDIZINISCH;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.KINDER_LESEN_NICHT_MEDIZINISCH;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.KINDER_NOTIZEN_MIT_LABEL_DISPENSATIONSGESUCHE_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.KINDER_NOTIZEN_MIT_LABEL_DISPENSATIONSGESUCHE_SCHREIBEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.KINDER_SCHREIBEN_MEDIZINISCH;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.KINDER_SCHREIBEN_NICHT_MEDIZINISCH;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.KOMPLETTER_EXPORT;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.LABELS_ADMIN_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.LABELS_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.LISTEN_ADMIN_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.LISTEN_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.SU_ABSCHLUSS_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.SU_ABSCHLUSS_SCHREIBEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.SU_AERZTLICHE_UNTERSUCHUNG_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.SU_AERZTLICHE_UNTERSUCHUNG_SCHREIBEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.SU_DATEN_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.SU_DATEN_SCHREIBEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.SU_GROSSE_GEWICHT_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.SU_GROSSE_GEWICHT_SCHREIBEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.SU_HOREN_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.SU_HOREN_SCHREIBEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.SU_IMPFEN_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.SU_IMPFEN_SCHREIBEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.SU_LAEUSE_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.SU_LAEUSE_SCHREIBEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.SU_MASSNAHMEN_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.SU_MASSNAHMEN_SCHREIBEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.SU_SEHEN_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.SU_SEHEN_SCHREIBEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.SU_TRIAGE_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.SU_TRIAGE_SCHREIBEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.SU_VORABKLAERUNGEN_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.SU_VORABKLAERUNGEN_SCHREIBEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.VORLAGEN_ADMIN_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.VORLAGEN_LESEN;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@TestSecurity(
    user = "testing-pfp",
    roles = {
        AUFGABEN_LESEN,
        AUFGABEN_SCHREIBEN,
        ANLASSLISTE_LESEN,
        ANLASSLISTE_LESEN_LAEUSE,
        ANLASSPLANUNG_LESEN,
        ANLASSPLANUNG_SCHREIBEN,
        ANLASSPLANUNG_LESEN_LAEUSE,
        ANLASSPLANUNG_SCHREIBEN_LAEUSE,
        SU_DATEN_LESEN,
        SU_DATEN_SCHREIBEN,
        SU_IMPFEN_LESEN,
        SU_IMPFEN_SCHREIBEN,
        SU_SEHEN_LESEN,
        SU_SEHEN_SCHREIBEN,
        SU_HOREN_LESEN,
        SU_HOREN_SCHREIBEN,
        SU_GROSSE_GEWICHT_LESEN,
        SU_GROSSE_GEWICHT_SCHREIBEN,
        SU_LAEUSE_LESEN,
        SU_LAEUSE_SCHREIBEN,
        SU_TRIAGE_LESEN,
        SU_TRIAGE_SCHREIBEN,
        SU_AERZTLICHE_UNTERSUCHUNG_LESEN,
        SU_AERZTLICHE_UNTERSUCHUNG_SCHREIBEN,
        SU_MASSNAHMEN_LESEN,
        SU_MASSNAHMEN_SCHREIBEN,
        SU_ABSCHLUSS_LESEN,
        SU_ABSCHLUSS_SCHREIBEN,
        SU_VORABKLAERUNGEN_LESEN,
        SU_VORABKLAERUNGEN_SCHREIBEN,
        KINDER_LESEN_MEDIZINISCH,
        KINDER_LESEN_NICHT_MEDIZINISCH,
        KINDER_SCHREIBEN_MEDIZINISCH,
        KINDER_SCHREIBEN_NICHT_MEDIZINISCH,
        KINDER_ALLE_NOTIZEN_LESEN,
        KINDER_ALLE_NOTIZEN_SCHREIBEN,
        KINDER_NOTIZEN_MIT_LABEL_DISPENSATIONSGESUCHE_LESEN,
        KINDER_NOTIZEN_MIT_LABEL_DISPENSATIONSGESUCHE_SCHREIBEN,
        KOMPLETTER_EXPORT,
        LISTEN_ADMIN_LESEN,
        LISTEN_LESEN,
        IMPFSTOFFE_LESEN,
        LABELS_LESEN,
        LABELS_ADMIN_LESEN,
        EIGENE_BERECHTIGUNGEN_LESEN,
        VORLAGEN_LESEN,
        VORLAGEN_ADMIN_LESEN,
        ARCHIVIERUNG
    }
)
@OidcSecurity(
    claims = {
        @Claim(key = "sub", value = TestAsPfpWIN.BENUTZER_ID_IN_KC),
        @Claim(key = "tenant", value = "BS"),
        @Claim(key = "family_name", value = "Pflegefachperson"),
        @Claim(key = "given_name", value = "Paul"),
        @Claim(key = "preferred_username", value = TestAsPfpWIN.USERNAME),
        @Claim(key = "email", value = TestAsPfpWIN.EMAIL),
        @Claim(key = JsonWebTokenClaims.PHONE_CLAIM, value = TestAsPfpWIN.TELEFON),
    }
)
@TestAsMarker
public @interface TestAsPfpWIN {
    String BENUTZER_ID = "d772c8a6-5f96-48ae-a93b-de333649099a";
    String BENUTZER_ID_IN_KC = "829e8074-1639-4d74-866b-7588e14c12e5";
    String EMAIL = "pfp@mailbucket.dvbern.ch";
    String USERNAME = "pfp@mailbucket.dvbern.ch";
    String TELEFON = "+41 61 123 45 73";
}
