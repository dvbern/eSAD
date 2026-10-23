package ch.dvbern.oss.hemed.esc.api.shared.settings;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;

import io.micrometer.core.annotation.Timed;
import io.smallrye.faulttolerance.api.RateLimit;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/tenant-settings")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_SETTINGS)
@PermitAll
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed("TenantSettingsResourceMS")
public class TenantSettingsResource {

    private final TenantSettingsMapper tenantSettingsMapper;

    @GET
    @Path("/self")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @PermitAll
    public TenantSettingsJson getTenantSettings() {
        return tenantSettingsMapper.getTenantSettings();
    }

    @PUT
    @Path("/self")
    @NonNull
    @RolesAllowed(BerechtigungName.SUPPORT)
    public TenantSettingsJson updateTenantSettings(
        @NotNull @NonNull @Valid TenantSettingSaveJson tenantSettingSaveJson
    ) {
        return tenantSettingsMapper.update(tenantSettingSaveJson);
    }
}
