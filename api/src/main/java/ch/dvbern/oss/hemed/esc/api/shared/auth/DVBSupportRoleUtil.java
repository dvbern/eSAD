package ch.dvbern.oss.hemed.esc.api.shared.auth;

import java.util.Set;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@UtilityClass
@Slf4j
public class DVBSupportRoleUtil {

    public static final String DVB_SUPPORT_TENANT_MARKER = "DVB-SUPPORT";

    @NonNull
    public static Set<Berechtigung> assignDVBSupportBerechtigungenForTenant(
        @NonNull String email,
        @Nullable TenantEnum tenantFromUrl
    ) {
        Set<Berechtigung> berechtigungen;

        if (tenantFromUrl == null) {
            LOG.warn("DVB-SUPPORT user '{}' — could not determine tenant from URL, granting no roles", email);
            berechtigungen = Set.of();
        } else {
            berechtigungen = FachRolle.collectAllBerechtigungen(tenantFromUrl, FachRolle.SUPPORT);
        }
        return berechtigungen;
    }

    public static boolean isDVBSupport(@Nullable String tenantClaim) {
        return DVBSupportRoleUtil.DVB_SUPPORT_TENANT_MARKER.equals(tenantClaim);
    }
}
