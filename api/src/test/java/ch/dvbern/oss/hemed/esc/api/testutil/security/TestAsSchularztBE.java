package ch.dvbern.oss.hemed.esc.api.testutil.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.UUID;

import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.ensure.JsonWebTokenClaims;
import ch.dvbern.oss.hemed.esc.api.features.organisation.Organisation;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
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
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.BENUTZER_BERECHTIGUNGEN_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.BENUTZER_BERECHTIGUNGEN_SCHREIBEN;
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
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.LABELS_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.LISTEN_LESEN;
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.ORGANISATION_VERWALTEN_LESEN;
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
import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.VORLAGEN_LESEN;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@TestSecurity(
    user = "testing-schularzt",
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
        LISTEN_LESEN,
        IMPFSTOFFE_LESEN,
        LABELS_LESEN,
        BENUTZER_BERECHTIGUNGEN_LESEN,
        BENUTZER_BERECHTIGUNGEN_SCHREIBEN,
        ORGANISATION_VERWALTEN_LESEN,
        VORLAGEN_LESEN,
        ARCHIVIERUNG
    }
)
@OidcSecurity(
    claims = {
        @Claim(key = "sub", value = TestAsSchularztBE.BENUTZER_ID_IN_KC),
        @Claim(key = "tenant", value = "BE"),
        @Claim(key = "family_name", value = "Schularzt"),
        @Claim(key = "given_name", value = "Julien"),
        @Claim(key = "preferred_username", value = TestAsSchularztBE.USERNAME),
        @Claim(key = "email", value = TestAsSchularztBE.EMAIL),
        @Claim(key = JsonWebTokenClaims.PHONE_CLAIM, value = TestAsSchularztBE.TELEFON),
        @Claim(key = JsonWebTokenClaims.TITLE_CLAIM, value = TestAsSchularztBE.TITEL),
    }
)
@TestAsMarker
public @interface TestAsSchularztBE {
    String BENUTZER_ID = "35e090d9-165d-4ef9-923b-8243b0d34f3a";
    ID<Benutzer> BENUTZER_ID_ID = ID.of(UUID.fromString(BENUTZER_ID), Benutzer.class);
    String ORGANISATION_ID = "a0dfe3f4-4419-4d9e-96b3-1be3b606596d";
    ID<Organisation> ORGANISATION_ID_ID = ID.of(UUID.fromString(ORGANISATION_ID), Organisation.class);
    String BENUTZER_ID_IN_KC = "4839ea1f-2895-4887-a295-6243e8fe0d84";
    String EMAIL = "schularzt@mailbucket.dvbern.ch";
    String USERNAME = "schularzt@mailbucket.dvbern.ch";
    String TELEFON = "+41 61 123 45 72";
    String TITEL = "Dr. Med";
}
