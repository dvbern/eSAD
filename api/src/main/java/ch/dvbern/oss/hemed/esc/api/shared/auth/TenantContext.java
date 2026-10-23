package ch.dvbern.oss.hemed.esc.api.shared.auth;

import ch.dvbern.oss.commons.logging.mdc.CommonMDCField;
import jakarta.enterprise.context.RequestScoped;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.MDC;

/**
 * Holds the tenant of the current request context.
 * <p>
 * For REST requests the tenant is set by {@link TenantExtractorFromUrlRequestFilter}. Background jobs, event
 * consumers and health checks must use
 * {@link ch.dvbern.oss.hemed.esc.api.shared.scheduler.PerTenantRunnerService}, which runs every tenant in a
 * request context of its own. Never switch the tenant inside an existing request context: request-scoped beans
 * (S3 clients, encryption keys, EntityManager, ...) created for the previous tenant would silently be reused.
 */
@RequestScoped
public class TenantContext {

    /**
     * MDC key of the tenant, identical to the one used by the logging request filter.
     */
    public static final String LOG_MDC_TENANT = CommonMDCField.TenantId.toString();

    @Nullable
    private TenantEnum currentTenant;

    /**
     * Detached (non-CDI) context holding a fixed tenant, e.g. for printers that get the tenant from their DTO.
     * Deliberately does not touch the MDC: the log tenant of the running thread stays unchanged.
     */
    @NonNull
    public static TenantContext forTenant(@NonNull TenantEnum tenant) {
        TenantContext context = new TenantContext();
        context.currentTenant = tenant;
        return context;
    }

    @NonNull
    public TenantEnum get() {
        TenantEnum tenant = currentTenant;
        if (tenant == null) {
            throw noTenantException();
        }
        return tenant;
    }

    /**
     * Only for infrastructure that has to cope with a missing tenant (e.g. logging, translations).
     * Business code must use {@link #get()}.
     */
    @Nullable
    public TenantEnum getOrNull() {
        return currentTenant;
    }

    public void setCurrentTenant(@NonNull TenantEnum tenant) {
        MDC.put(LOG_MDC_TENANT, tenant.name());
        currentTenant = tenant;
    }

    /**
     * Ensures the currently set tenant is the expected one. Components that cache tenant-specific data within a
     * request context use this to fail loudly instead of serving data of another tenant.
     */
    public void assertCurrentTenantIs(@NonNull TenantEnum expectedTenant, @NonNull String contextDescription) {
        TenantEnum tenant = get();
        if (tenant != expectedTenant) {
            throw new IllegalStateException(contextDescription
                + ": current tenant "
                + tenant
                + " does not match the expected tenant "
                + expectedTenant
                + " - the tenant must not be switched inside a request context, use PerTenantRunnerService");
        }
    }

    @NonNull
    private static IllegalStateException noTenantException() {
        return new IllegalStateException("No tenant set on TenantContext: for REST requests the"
            + " TenantExtractorFromUrlRequestFilter should have set it, for background jobs use"
            + " PerTenantRunnerService");
    }
}
