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

    // Divers

    @POST
    @Path("/trigger-benutzer-inaktivieren-job")
    @RolesAllowed(BerechtigungName.SUPPORT)
    @Transactional(TxType.NOT_SUPPORTED)
    public void triggerBenutzerInaktivierenJob() {
        superAdminRunnerService.runBenutzerInaktivierenJob(true);
    }

    @POST
    @Path("/trigger-kinder-inaktivieren-job")
    @RolesAllowed(BerechtigungName.SUPPORT)
    @Transactional(TxType.NOT_SUPPORTED)
    public void triggerKinderInaktivierenJob() {
        superAdminRunnerService.runKinderInaktivierenJob(true);
    }

    @POST
    @Path("/trigger-reload-duplikate")
    @RolesAllowed(BerechtigungName.SUPPORT)
    @Transactional(TxType.NOT_SUPPORTED)
    public void triggerReloadDuplikate() {
        superAdminRunnerService.runReloadDuplikateJob(true);
    }

    @POST
    @Path("/normalize-kind-namen")
    @RolesAllowed(BerechtigungName.SUPPORT)
    @Transactional(TxType.NOT_SUPPORTED)
    public void normalizeKindNamen() {
        superAdminRunnerService.normalizeKindNamen();
    }

    // Impfstoffe

    @POST
    @Path("/trigger-deactivate-impfstoff-chargen")
    @RolesAllowed(BerechtigungName.SUPPORT)
    @Transactional(TxType.NOT_SUPPORTED)
    public void triggerDeactivateImpfstoffChargenJob() {
        superAdminRunnerService.runDeactivateImpfstoffChargenJob(true);
    }

    @POST
    @Path("/trigger-delete-impfstoff-chargen")
    @RolesAllowed(BerechtigungName.SUPPORT)
    @Transactional(TxType.NOT_SUPPORTED)
    public void triggerDeleteImpfstoffChargenJob() {
        superAdminRunnerService.runDeleteImpfstoffChargenJob(true);
    }

    // DocumentQueue

    @POST
    @Path("/trigger-document-queue-job")
    @RolesAllowed(BerechtigungName.SUPPORT)
    @Transactional(TxType.NOT_SUPPORTED)
    public void triggerDocumentQueueJob() {
        superAdminRunnerService.runDocumentQueueJob(true);
    }

    @POST
    @Path("/trigger-document-queue-clean-up-job")
    @RolesAllowed(BerechtigungName.SUPPORT)
    @Transactional(TxType.NOT_SUPPORTED)
    public void triggerDocumentQueueCleanUpJob() {
        superAdminRunnerService.runDocumentQueueCleanupJob(true);
    }

    // DossierQueue/ Archivierung

    @POST
    @Path("/dossier-queue-processing")
    @RolesAllowed(BerechtigungName.SUPPORT)
    @Transactional(TxType.NOT_SUPPORTED)
    public void triggerDossierQueueProcessingJob() {
        superAdminRunnerService.runDossierQueueProcessingJob(true);
    }

    @POST
    @Path("/dossier-queue-cleanup")
    @RolesAllowed(BerechtigungName.SUPPORT)
    @Transactional(TxType.NOT_SUPPORTED)
    public void triggerDossierQueueCleanupJob() {
        superAdminRunnerService.runDossierQueueCleanupJob(true);
    }

    @POST
    @Path("/trigger-archivierung")
    @RolesAllowed(BerechtigungName.SUPPORT)
    @Transactional(TxType.NOT_SUPPORTED)
    public void triggerArchivierungJob() {
        superAdminRunnerService.runMarkForArchivierungJob(true);
    }

    @POST
    @Path("/trigger-archivierung-delete")
    @RolesAllowed(BerechtigungName.SUPPORT)
    @Transactional(TxType.NOT_SUPPORTED)
    public void triggerArchivierungDeleteJob() {
        superAdminRunnerService.runArchiveDocumentKindDeleteJob(true);
    }

    @POST
    @Path("/archive-download-email-reminder")
    @RolesAllowed(BerechtigungName.SUPPORT)
    @Transactional(TxType.NOT_SUPPORTED)
    public void triggerArchiveDownloadEmailReminderJob() {
        superAdminRunnerService.runArchiveDocumentDownloadEmailReminderJob(true);
    }

    @POST
    @Path("/trigger-filestore-healthcheck")
    @RolesAllowed(BerechtigungName.SUPPORT)
    @Transactional(TxType.NOT_SUPPORTED)
    public void triggerFilestoreHealthcheck() {
        superAdminRunnerService.runFileStoreHealthCheckJob(true);
    }
}
