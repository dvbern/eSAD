package ch.dvbern.oss.hemed.esc.api.features.kinder.duplikate;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.shared.scheduler.SuperAdminRunnerService;
import io.smallrye.faulttolerance.api.RateLimit;
import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import static ch.dvbern.oss.hemed.esc.api.shared.scheduler.SuperAdminEventService.DUPLIKAT_RECALCULATION_EVENT;

@Path("/duplikate")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_DUPLIKATE)
@RolesAllowed(BerechtigungName.DUBLETTEN_ZUSAMMENFUHREN)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed(name = "DuplikatResourceMS", unit = MetricUnits.MILLISECONDS)
public class DuplikatResource {

    private final SuperAdminRunnerService superAdminRunnerService;
    private final DuplikatMapper duplikatMapper;
    private final TenantProducer tenantProducer;
    private final EventBus eventBus;

    @PUT
    @Path("/reload")
    @RolesAllowed(BerechtigungName.DUBLETTEN_ZUSAMMENFUHREN)
    @Transactional(TxType.NOT_SUPPORTED)
    public void reloadDuplikateAsync() {
        eventBus.send(DUPLIKAT_RECALCULATION_EVENT, tenantProducer.produceTenant());
    }

    @PUT
    @Path("/reload-sync")
    @RolesAllowed(BerechtigungName.DUBLETTEN_ZUSAMMENFUHREN)
    @Transactional(TxType.NOT_SUPPORTED)
    public DuplikatListJson reloadDuplikateSync() {
        superAdminRunnerService.runReloadDuplikateJob(true);
        return findAllDuplikate();
    }

    @GET
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.DUBLETTEN_ZUSAMMENFUHREN)
    public DuplikatListJson findAllDuplikate() {
        return new DuplikatListJson(duplikatMapper.findAll());
    }

    @PUT
    @Path("/{duplikatId}/false-positive")
    @RolesAllowed(BerechtigungName.DUBLETTEN_ZUSAMMENFUHREN)
    public DuplikatJson markAsFalsePositive(
        @NonNull @NotNull @PathParam("duplikatId") ID<Duplikat> duplikatId
    ) {
        return duplikatMapper.markAsFalsePositive(duplikatId);
    }

    @PUT
    @Path("/{duplikatId}/merge")
    @RolesAllowed(BerechtigungName.DUBLETTEN_ZUSAMMENFUHREN)
    public DuplikatJson merge(
        @NonNull @NotNull @PathParam("duplikatId") ID<Duplikat> duplikatId,
        @NotNull @QueryParam("basisKindId") ID<Kind> basisKindId
    ) {
        return duplikatMapper.merge(duplikatId, basisKindId);
    }
}
