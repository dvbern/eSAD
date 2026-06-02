package ch.dvbern.oss.hemed.esc.api.testutil.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import io.quarkus.test.security.TestSecurity;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
// java Annotation require compile time Constants so we need to define the array each time :(
@TestSecurity(user = "testing-user",
    roles = {
        BerechtigungName.BENUTZER_OPTIONS_LESEN,
        BerechtigungName.AUFGABEN_LESEN,
        BerechtigungName.AUFGABEN_SCHREIBEN,
        BerechtigungName.ANLASSLISTE_LESEN,
        BerechtigungName.ANLASSPLANUNG_LESEN,
        BerechtigungName.KINDER_LESEN_MEDIZINISCH,
        BerechtigungName.KINDER_LESEN_NICHT_MEDIZINISCH,
        BerechtigungName.KINDER_NOTIZEN_MIT_LABEL_DISPENSATIONSGESUCHE_LESEN,
        BerechtigungName.KINDER_NOTIZEN_MIT_LABEL_DISPENSATIONSGESUCHE_SCHREIBEN,
        BerechtigungName.AERZTE_LESEN,
        BerechtigungName.IMPFSTOFFE_LESEN,
        BerechtigungName.EIGENE_BERECHTIGUNGEN_LESEN,
        BerechtigungName.VORLAGEN_LESEN
    }
)
@TestAsMarker
public @interface TestAsUser {
}
