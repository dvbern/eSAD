package ch.dvbern.oss.hemed.esc.api.shared.auth;

import java.util.Set;

import lombok.experimental.UtilityClass;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Diese Klasse wird als Delegator verwendet, damit wir die Methode in einigen Tests mocken können.
 * Dies scheint leider direkt auf dem Enum nicht zu funktionieren.
 * Wann brauchts das: Wenn wir mit WIN-Berechtigungen auf der BS-Datenbank testen wollen (da wir die WIN-DB nicht haben)
 * Wie: Siehe AuthorizerServiceTest#useBerechtigungenWin.
 */
@UtilityClass
public class FachRolleUtil {

    @NonNull
    public static Set<Berechtigung> getBerechtigungen(@NonNull TenantEnum tenant, @NonNull FachRolle fachRolle) {
        return fachRolle.getBerechtigungen(tenant);
    }
}
