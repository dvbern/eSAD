package ch.dvbern.dvbstarter.infrastructure.logging;

import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantEnum;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantContext;
import com.vdurmont.semver4j.Semver;
import java.util.Optional;

import ch.dvbern.dvbstarter.FilterPriorities;
import ch.dvbern.oss.commons.logging.mdc.LoggingApp;
import ch.dvbern.oss.commons.logging.mdc.LoggingPrincipal;
import ch.dvbern.oss.commons.logging.mdc.LoggingSource;
import ch.dvbern.oss.commons.logging.mdc.LoggingTenant;
import ch.dvbern.oss.commons.logging.mdc.jaxrsfilter.CommonMDCFieldRequestFilterHelper;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.ext.Provider;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Provider
@PreMatching
@Priority(FilterPriorities.LOGGING_INIT)
@RequestScoped
@NoArgsConstructor
@AllArgsConstructor(
    access = AccessLevel.PACKAGE,
    onConstructor_ = @SuppressWarnings("rawtypes")
)
public class LoggingCommonMDCFieldsRequestFilter implements ContainerRequestFilter {

    private final CommonMDCFieldRequestFilterHelper helper = CommonMDCFieldRequestFilterHelper.usingDefaults();

    @Inject
    ConfigService configService;

    @Inject
    TenantContext tenantContext;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        var loggingPrincipal = Optional.ofNullable(requestContext.getSecurityContext().getUserPrincipal())
            .map(p -> new LoggingPrincipal(p.getName()))
            .orElse(null);



        helper.filter(
            requestContext,
            new LoggingApp(
                configService.getAppProject(),
                configService.getAppModule(),
                determineVersion(),
                configService.getInstance()
            ),
            new LoggingSource(requestContext.getUriInfo().getRequestUri().getPath(), requestContext.getMethod()),
            loggingPrincipal,
            new LoggingTenant(getTenant())
        );


    }

    @NonNull
    private Semver determineVersion() {
        try {
            return new Semver(configService.getVersion());
        } catch (Exception e) {
            return new Semver("0.0.0");
        }
    }

    String getTenant() {
        // runs before the tenant is extracted from the URL; requests without tenant must still be logged
        TenantEnum tenant = tenantContext.getOrNull();
        if(tenant != null) {
            return tenant.toString();
        }
        return "";
    }

}
