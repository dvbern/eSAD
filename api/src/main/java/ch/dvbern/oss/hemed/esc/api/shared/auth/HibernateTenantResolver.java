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

    private final TenantProducer tenantProducer;

    @NonNull
    @Override
    public String getDefaultTenantId() {
        return TenantEnum.BS.getHibernateMultiTenancyIdentifier();
    }

    @NonNull
    @Override
    public String resolveTenantId() {
        TenantEnum currentTenant = this.tenantProducer.produceTenant();
        if (currentTenant != null) {
            return currentTenant.getHibernateMultiTenancyIdentifier();
        }
        return getDefaultTenantId();
    }

    @NonNull
    public TenantEnum resolveTenant() {
        return TenantEnum.valueOf(resolveTenantId().toUpperCase());
    }
}
