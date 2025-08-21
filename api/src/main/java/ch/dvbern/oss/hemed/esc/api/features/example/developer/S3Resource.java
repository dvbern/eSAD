package ch.dvbern.oss.hemed.esc.api.features.example.developer;

import ch.dvbern.dvbstarter.infrastructure.fileio.FileDownloadUtil;
import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileDataService;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileStoreService;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.faulttolerance.api.RateLimit;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

@Slf4j
@Path("/s3")
@JaxResource
@RequiredArgsConstructor
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@RolesAllowed(BerechtigungName.SUPPORT)
public class S3Resource {

    private final FileStoreService fileStoreService;
    private final FileDataService fileDataService;

    @GET
    @Consumes(MediaType.WILDCARD)
    @APIResponse(content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM,
        schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "OK", responseCode = "200")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/files/{fileIdInbucket}")
    @RolesAllowed(BerechtigungName.SUPPORT)
    @NonNull
    @Blocking
    public Response downloadFile(@PathParam("fileIdInbucket") @NotNull @NonNull String fileIdInbucket) {
        var content = fileStoreService.getFileBytesSync(fileIdInbucket);
        return FileDownloadUtil.buildFileResponse(
            "attachment",
            content,
            fileDataService.getFileInfoS3ByFileIdInBucket(fileIdInbucket));
    }
}
