package ch.dvbern.oss.hemed.esc.api.shared.auth;

import java.util.Optional;

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
}
