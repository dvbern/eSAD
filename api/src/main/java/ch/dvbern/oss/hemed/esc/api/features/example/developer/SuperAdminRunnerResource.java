package ch.dvbern.oss.hemed.esc.api.features.example.developer;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import ch.dvbern.oss.hemed.esc.api.shared.scheduler.SuperAdminRunnerService;
import io.smallrye.faulttolerance.api.RateLimit;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/super-admin-runner")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_SUPER_ADMIN_RUNNER)
@RolesAllowed(BerechtigungName.SUPPORT)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
public class SuperAdminRunnerResource {

    private final SuperAdminRunnerService superAdminRunnerService;

    @POST
    @Path("/trigger-document-queue-job")
    @RolesAllowed(BerechtigungName.SUPPORT)
    @Transactional(TxType.NOT_SUPPORTED)
    public Response triggerDocumentQueueJob() {
        superAdminRunnerService.runDocumentQueueJob(true);
        return Response.ok().build();
    }

    @POST
    @Path("/trigger-document-queue-clean-up-job")
    @RolesAllowed(BerechtigungName.SUPPORT)
    @Transactional(TxType.NOT_SUPPORTED)
    public Response triggerDocumentQueueCleanUpJob() {
        superAdminRunnerService.runDocumentQueueCleanupJob(true);
        return Response.ok().build();
    }

    @POST
    @Path("/trigger-benutzer-inaktivieren-job")
    @RolesAllowed(BerechtigungName.SUPPORT)
    @Transactional(TxType.NOT_SUPPORTED)
    public Response triggerBenutzerInaktivierenJob() {
        superAdminRunnerService.runBenutzerInaktivierenJob(true);
        return Response.ok().build();
    }

    @POST
    @Path("/trigger-kinder-inaktivieren-job")
    @RolesAllowed(BerechtigungName.SUPPORT)
    @Transactional(TxType.NOT_SUPPORTED)
    public Response triggerKinderInaktivierenJob() {
        superAdminRunnerService.runKinderInaktivierenJob(true);
        return Response.ok().build();
    }


    @POST
    @Path("/trigger-reload-duplikate")
    @RolesAllowed(BerechtigungName.SUPPORT)
    @Transactional(TxType.NOT_SUPPORTED)
    public Response triggerReloadDuplikate() {
        superAdminRunnerService.runReloadDuplikateJob(true);
        return Response.ok().build();
    }


    @POST
    @Path("/normalize-kind-namen")
    @RolesAllowed(BerechtigungName.SUPPORT)
    @Transactional(TxType.NOT_SUPPORTED)
    public Response normalizeKindNamen() {
        superAdminRunnerService.normalizeKindNamen();
        return Response.ok().build();
    }
}
