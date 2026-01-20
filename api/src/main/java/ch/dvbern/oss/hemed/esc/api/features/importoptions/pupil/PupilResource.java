package ch.dvbern.oss.hemed.esc.api.features.importoptions.pupil;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst;
import ch.dvbern.oss.hemed.esc.api.features.importoptions.pupil.json.PupilImportResultJson;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.UploadItemSchema;
import io.smallrye.faulttolerance.api.RateLimit;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@Path("/pupil")
@JaxResource
@RequiredArgsConstructor
@Tag(name = OpenApiConst.Tags.TAG_PUPIL)
@RolesAllowed(BerechtigungName.PUPIL_PUSH)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
public class PupilResource {

    private final PupilService pupilService;
    private final PupilWorkerService pupilWorkerService;

    @POST
    @Path("/sync")
    @NonNull
    @RolesAllowed(BerechtigungName.PUPIL_PUSH)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Transactional(TxType.NOT_SUPPORTED)
    public PupilImportResultJson syncWithPupil(
        @FormParam(value = "file")
        @Schema(implementation = UploadItemSchema.class)
        // schema genaration produces object instead of binary string otherwise
        FileUpload csvFile
    ) {
        return pupilService.processPupilCsvUpload(csvFile);
    }

    @POST
    @RolesAllowed(BerechtigungName.PUPIL_PUSH)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Transactional(TxType.NOT_SUPPORTED)
    public Response syncWithPupilAsync(
        @FormParam(value = "file")
        @Schema(implementation = UploadItemSchema.class)
        // schema genaration produces object instead of binary string otherwise
        FileUpload csvFile
    ) {
        pupilWorkerService.processPupilCsvUploadUsingEventBus(csvFile);
        return Response.ok().build();
    }

}
