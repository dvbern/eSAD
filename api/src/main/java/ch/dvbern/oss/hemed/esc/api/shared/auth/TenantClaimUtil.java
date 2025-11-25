package ch.dvbern.oss.hemed.esc.api.shared.auth;

import java.util.Optional;

import ch.dvbern.oss.hemed.esc.api.features.benutzer.ensure.JsonWebTokenClaims;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.jwt.JsonWebToken;

@UtilityClass
@Slf4j
public class TenantClaimUtil {


    @NonNull
    public static Optional<String> extractTenantClaim(@NonNull JsonWebToken jsonWebToken) {
        return jsonWebToken.claim(JsonWebTokenClaims.TENANT_CLAIM);
    }
}
