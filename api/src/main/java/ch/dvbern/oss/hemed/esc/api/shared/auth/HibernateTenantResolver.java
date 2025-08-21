package ch.dvbern.oss.hemed.esc.api.shared.auth;

import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import io.quarkus.hibernate.orm.runtime.tenant.TenantResolver;
import jakarta.enterprise.context.RequestScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.jwt.JsonWebToken;

@PersistenceUnitExtension
@RequestScoped
@RequiredArgsConstructor
@Slf4j
public class HibernateTenantResolver implements TenantResolver {

    private final ConfigService configService;
    private final JsonWebToken jsonWebToken;


    @NonNull
    @Override
    public String getDefaultTenantId() {
        return configService.getTenantOverride()
            .map(HibernateTenantResolver::handleTenantOverrideReturn)
            .orElse(TenantEnum.DEFAULT.getHibernateMultiTenancyIdentifier());
    }

    @NonNull
    @Override
    public String resolveTenantId() {
        return configService.getTenantOverride()
            .map(HibernateTenantResolver::handleTenantOverrideReturn)
            .orElseGet(this::resolveTenantIdFromToken);
    }

    @NonNull
    private static String handleTenantOverrideReturn(@NonNull TenantEnum tenantEnum) {
        LOG.debug("Tenant override detected. Using tenant={}", tenantEnum);
        return tenantEnum.getHibernateMultiTenancyIdentifier();
    }

    @NonNull
    private String resolveTenantIdFromToken() {
        // Nicht-eingeloggter Benutzer. Wir koennen hier abbrechen
        final String rawToken = jsonWebToken.getRawToken();
        if (rawToken == null) {
            LOG.warn("no token found in Request. Using default tenant");
            return getDefaultTenantId();
        }
        // Informationen aus dem Token lesen
        return TenantClaimUtil.extractTenantClaim(jsonWebToken)
            .map(TenantClaimUtil::mapTenantClaimToHibernateTenantID)
            .orElse(getDefaultTenantId());
    }
}
