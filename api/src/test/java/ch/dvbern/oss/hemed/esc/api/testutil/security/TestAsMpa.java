package ch.dvbern.oss.hemed.esc.api.testutil.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.UUID;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.ensure.JsonWebTokenClaims;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.oidc.Claim;
import io.quarkus.test.security.oidc.OidcSecurity;

import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.AERZTE_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.ANLASSLISTE_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.ANLASSPLANUNG_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.ANLASSPLANUNG_SCHREIBEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.AUFGABEN_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.AUFGABEN_SCHREIBEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.EIGENE_BERECHTIGUNGEN_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.IMPFSTOFFE_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.INDIVIDUELLE_REPORTS;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.KINDER_ALLE_NOTIZEN_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.KINDER_ALLE_NOTIZEN_SCHREIBEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.KINDER_LESEN_MEDIZINISCH;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.KINDER_LESEN_NICHT_MEDIZINISCH;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.KINDER_NOTIZEN_MIT_LABEL_DISPENSATIONSGESUCHE_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.KINDER_NOTIZEN_MIT_LABEL_DISPENSATIONSGESUCHE_SCHREIBEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.KINDER_SCHREIBEN_MEDIZINISCH;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.KINDER_SCHREIBEN_NICHT_MEDIZINISCH;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.LABELS_ADMIN_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.LABELS_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.LISTEN_ADMIN_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.LISTEN_LESEN;
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
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.VORLAGEN_LESEN;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@TestSecurity(
    user = "testing-mpa",
    roles = {
        AUFGABEN_LESEN,
        AUFGABEN_SCHREIBEN,
        ANLASSLISTE_LESEN,
        ANLASSPLANUNG_LESEN,
        ANLASSPLANUNG_SCHREIBEN,
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
        KINDER_LESEN_MEDIZINISCH,
        KINDER_LESEN_NICHT_MEDIZINISCH,
        KINDER_SCHREIBEN_MEDIZINISCH,
        KINDER_SCHREIBEN_NICHT_MEDIZINISCH,
        KINDER_ALLE_NOTIZEN_LESEN,
        KINDER_ALLE_NOTIZEN_SCHREIBEN,
        KINDER_NOTIZEN_MIT_LABEL_DISPENSATIONSGESUCHE_LESEN,
        KINDER_NOTIZEN_MIT_LABEL_DISPENSATIONSGESUCHE_SCHREIBEN,
        INDIVIDUELLE_REPORTS,
        LISTEN_LESEN,
        LISTEN_ADMIN_LESEN,
        AERZTE_LESEN,
        IMPFSTOFFE_LESEN,
        LABELS_LESEN,
        LABELS_ADMIN_LESEN,
        EIGENE_BERECHTIGUNGEN_LESEN,
        VORLAGEN_LESEN
    }
)
@OidcSecurity(
    claims = {
        @Claim(key = "sub", value = TestAsMpa.BENUTZER_ID_IN_KC),
        @Claim(key = "tenant", value = "BS"),
        @Claim(key = "family_name", value = "Support"),
        @Claim(key = "given_name", value = "Susanne"),
        @Claim(key = "preferred_username", value = TestAsMpa.USERNAME),
        @Claim(key = "email", value = TestAsMpa.EMAIL),
        @Claim(key = JsonWebTokenClaims.PHONE_CLAIM, value = TestAsMpa.TELEFON),
        @Claim(key = JsonWebTokenClaims.TITLE_CLAIM, value = TestAsMpa.TITEL),
    }
)
public @interface TestAsMpa {
    String BENUTZER_ID = "0a610e49-d690-4670-b01b-7fe355b0c643";
    ID<Benutzer> BENUTZER_ID_ID = ID.of(UUID.fromString(BENUTZER_ID), Benutzer.class);
    String BENUTZER_ID_IN_KC = "1a41d20c-af56-4504-8500-4a72f11d26a7";
    String EMAIL = "mpa@mailbucket.dvbern.ch";
    String USERNAME = "mpa@mailbucket.dvbern.ch";
    String TELEFON = "+41 61 123 45 68";
    String TITEL = "Med. Praxisassistentin";
}
