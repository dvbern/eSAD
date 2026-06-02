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
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.ANLASSLISTE_LESEN_LAEUSE;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.ANLASSPLANUNG_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.ANLASSPLANUNG_LESEN_LAEUSE;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.ANLASSPLANUNG_SCHREIBEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.ANLASSPLANUNG_SCHREIBEN_LAEUSE;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.ARCHIVIERUNG;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.AUFGABEN_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.AUFGABEN_SCHREIBEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.BENUTZER_BERECHTIGUNGEN_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.BENUTZER_BERECHTIGUNGEN_SCHREIBEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.BENUTZER_OPTIONS_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.DUBLETTEN_ZUSAMMENFUHREN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.EIGENE_BERECHTIGUNGEN_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.IMPFSTOFFE_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.IMPFSTOFFE_SCHREIBEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.INDIVIDUELLE_REPORTS;
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
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.LABELS_SCHREIBEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.SPSS_EXPORT;
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
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.VERANDERUNGEN_NACH_ABSCHLUSS;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.VORLAGEN_ADMIN_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.VORLAGEN_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.VORLAGEN_SCHREIBEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.SCHULE_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.SCHULE_SCHREIBEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.INFOMATERIAL_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.INFOMATERIAL_SCHREIBEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.KLASSEN_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.KLASSEN_SCHREIBEN;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@TestSecurity(
    user = "testing-admin-bs",
    // java Annotation require compile time Constants so we need to define the array each time :(
    roles = {
        BENUTZER_OPTIONS_LESEN,
        AUFGABEN_LESEN,
        AUFGABEN_SCHREIBEN,
        ANLASSLISTE_LESEN,
        ANLASSLISTE_LESEN_LAEUSE,
        ANLASSPLANUNG_LESEN,
        ANLASSPLANUNG_SCHREIBEN,
        ANLASSPLANUNG_LESEN_LAEUSE,
        ANLASSPLANUNG_SCHREIBEN_LAEUSE,
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
        INDIVIDUELLE_REPORTS,
        SPSS_EXPORT,
        KOMPLETTER_EXPORT,
        IMPFSTOFFE_LESEN,
        IMPFSTOFFE_SCHREIBEN,
        DUBLETTEN_ZUSAMMENFUHREN,
        LABELS_LESEN,
        LABELS_ADMIN_LESEN,
        LABELS_SCHREIBEN,
        EIGENE_BERECHTIGUNGEN_LESEN,
        BENUTZER_BERECHTIGUNGEN_LESEN,
        BENUTZER_BERECHTIGUNGEN_SCHREIBEN,
        VORLAGEN_LESEN,
        VORLAGEN_ADMIN_LESEN,
        VORLAGEN_SCHREIBEN,
        AERZTE_LESEN,
        AERZTE_SCHREIBEN,
        ARCHIVIERUNG,
        SCHULE_LESEN,
        SCHULE_SCHREIBEN,
        KLASSEN_LESEN,
        KLASSEN_SCHREIBEN,
        INFOMATERIAL_LESEN,
        INFOMATERIAL_SCHREIBEN
    }
)
@OidcSecurity(
    claims = {
        @Claim(key = "sub", value = TestAsAdminBS.BENUTZER_ID_IN_KC),
        @Claim(key = "tenant", value = "BS"),
        @Claim(key = "family_name", value = "Administrator"),
        @Claim(key = "given_name", value = "Adrian"),
        @Claim(key = "preferred_username", value = TestAsAdminBS.USERNAME),
        @Claim(key = "email", value = TestAsAdminBS.EMAIL),
        @Claim(key = JsonWebTokenClaims.PHONE_CLAIM, value = TestAsAdminBS.TELEFON),
    }
)
@TestAsMarker
public @interface TestAsAdminBS {
    String BENUTZER_ID = "a0f624c9-7634-47fe-b49e-0d26e9310872";
    String BENUTZER_ID_IN_KC = "88aac6e0-ad5f-4a5a-8944-219dc12fa3ec";
    String EMAIL = "admin@example.com";
    String USERNAME = "admin@example.com";
    String TELEFON = "+41 61 123 45 70";
}
