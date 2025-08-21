package ch.dvbern.oss.hemed.esc.api.shared.auth;

import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureException;
import ch.dvbern.dvbstarter.infrastructure.errorhandling.AppFailureMessage;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.jwt.JsonWebToken;

@UtilityClass
@Slf4j
public class TenantClaimUtil {

    public static final String TENANT = "tenant";

    @NonNull
    public static Optional<String> extractTenantClaim(@NonNull JsonWebToken jsonWebToken) {
        return jsonWebToken.claim(TENANT);
    }

    @NonNull
    public static String mapTenantClaimToHibernateTenantID(@NonNull String tenantClaim) {
        try {
            return TenantEnum.valueOf(tenantClaim.toUpperCase()).getHibernateMultiTenancyIdentifier();
        } catch (IllegalArgumentException e) {
            LOG.info(
                "Invalid tenant claim encountered in JWT. claim={}. Make sure it matches a known Tenant",
                tenantClaim,
                e);
            throw new AppFailureException(AppFailureMessage.invalidTenantClaim(tenantClaim), e);
        }
    }
}
