package ch.dvbern.oss.hemed.esc.api.shared.auth;

import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import io.quarkus.hibernate.orm.runtime.tenant.TenantResolver;
import jakarta.enterprise.context.RequestScoped;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;

@PersistenceUnitExtension
@RequestScoped
@RequiredArgsConstructor
@Slf4j
public class HibernateTenantResolver implements TenantResolver {

    private final TenantContext tenantContext;

    @NonNull
    @Override
    public String getDefaultTenantId() {
        return TenantEnum.BS.getHibernateMultiTenancyIdentifier();
    }

    /**
     * No silent fallback to {@link #getDefaultTenantId()}: a forgotten tenant would otherwise read and write the
     * data of the default tenant. {@link TenantContext#get()} fails loudly instead.
     */
    @NonNull
    @Override
    public String resolveTenantId() {
        return tenantContext.get()
            .getHibernateMultiTenancyIdentifier();
    }

    @NonNull
    public TenantEnum resolveTenant() {
        return TenantEnum.valueOf(resolveTenantId().toUpperCase());
    }
}
