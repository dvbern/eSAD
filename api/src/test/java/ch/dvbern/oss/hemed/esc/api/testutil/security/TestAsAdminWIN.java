package ch.dvbern.oss.hemed.esc.api.testutil.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ch.dvbern.oss.hemed.esc.api.features.benutzer.ensure.JsonWebTokenClaims;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;

import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.AERZTE_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.AERZTE_SCHREIBEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.ANLASSLISTE_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.ANLASSPLANUNG_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.ANLASSPLANUNG_SCHREIBEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.AUFGABEN_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.AUFGABEN_SCHREIBEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.DUBLETTEN_ZUSAMMENFUHREN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.EIGENE_BERECHTIGUNGEN_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.IMPFSTOFFE_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.IMPFSTOFFE_SCHREIBEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.INDIVIDUELLE_REPORTS;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.KINDER_ALLE_NOTIZEN_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.KINDER_ALLE_NOTIZEN_SCHREIBEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.KINDER_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.KINDER_NOTIZEN_MIT_LABEL_DISPENSATIONSGESUCHE_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.KINDER_NOTIZEN_MIT_LABEL_DISPENSATIONSGESUCHE_SCHREIBEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.KINDER_SCHREIBEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.KOMPLETTER_EXPORT;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.LABELS_ADMIN_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.LABELS_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.LABELS_SCHREIBEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.LISTEN_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.LISTEN_SCHREIBEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.MITARBEITERLISTE_BERECHTIGUNGEN_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.SPSS_EXPORT;
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
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.SU_MASSNAHMEN_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.SU_MASSNAHMEN_SCHREIBEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.SU_SEHEN_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.SU_SEHEN_SCHREIBEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.SU_TRIAGE_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.SU_TRIAGE_SCHREIBEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.VERANDERUNGEN_NACH_ABSCHLUSS;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.VORLAGEN_ADMIN_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.VORLAGEN_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.VORLAGEN_SCHREIBEN;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@TestSecurity(
    user = "testing-admin-win",
    // java Annotation require compile time Constants so we need to define the array each time :(
    roles = {
        AUFGABEN_LESEN,
        AUFGABEN_SCHREIBEN,
        ANLASSLISTE_LESEN,
        ANLASSPLANUNG_LESEN,
        ANLASSPLANUNG_SCHREIBEN,
        VERANDERUNGEN_NACH_ABSCHLUSS,
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
        SU_TRIAGE_LESEN,
        SU_TRIAGE_SCHREIBEN,
        SU_AERZTLICHE_UNTERSUCHUNG_LESEN,
        SU_AERZTLICHE_UNTERSUCHUNG_SCHREIBEN,
        SU_MASSNAHMEN_LESEN,
        SU_MASSNAHMEN_SCHREIBEN,
        KINDER_LESEN,
        KINDER_SCHREIBEN,
        KINDER_ALLE_NOTIZEN_LESEN,
        KINDER_ALLE_NOTIZEN_SCHREIBEN,
        KINDER_NOTIZEN_MIT_LABEL_DISPENSATIONSGESUCHE_LESEN,
        KINDER_NOTIZEN_MIT_LABEL_DISPENSATIONSGESUCHE_SCHREIBEN,
        INDIVIDUELLE_REPORTS,
        SPSS_EXPORT,
        KOMPLETTER_EXPORT,
        LISTEN_LESEN,
        LISTEN_SCHREIBEN,
        AERZTE_LESEN,
        AERZTE_SCHREIBEN,
        IMPFSTOFFE_LESEN,
        IMPFSTOFFE_SCHREIBEN,
        DUBLETTEN_ZUSAMMENFUHREN,
        LABELS_LESEN,
        LABELS_ADMIN_LESEN,
        LABELS_SCHREIBEN,
        EIGENE_BERECHTIGUNGEN_LESEN,
        MITARBEITERLISTE_BERECHTIGUNGEN_LESEN,
        VORLAGEN_LESEN,
        VORLAGEN_ADMIN_LESEN,
        VORLAGEN_SCHREIBEN
    }
)
@OidcSecurity(
    claims = {
        @Claim(key = "sub", value = TestAsAdminWIN.BENUTZER_ID),
        @Claim(key = "tenant", value = "WIN"),
        @Claim(key = "family_name", value = "Winterthur"),
        @Claim(key = "given_name", value = "Wilma"),
        @Claim(key = "preferred_username", value = TestAsAdminWIN.USERNAME),
        @Claim(key = "email", value = TestAsAdminWIN.EMAIL),
        @Claim(key = JsonWebTokenClaims.PHONE_CLAIM, value = TestAsAdminWIN.TELEFON),
    }
)
public @interface TestAsAdminWIN {
    String BENUTZER_ID = "925d8edb-158f-4c18-b3eb-164a194b18dc";
    String EMAIL = "admin.winterthur@example.com";
    String USERNAME = "admin.winterthur@example.com";
    String TELEFON = "+41 61 123 45 71";
    String TITEL = null;
}
