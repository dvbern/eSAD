package ch.dvbern.oss.hemed.esc.api.features.archivierung;

import ch.dvbern.dvbstarter.infrastructure.fileio.FileDownloadUtil;
import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.shared.auth.AuthorizerService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileInfo;
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
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/archivdokumente")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_ARCHIVDOKUMENT)
@RolesAllowed(BerechtigungName.ARCHIVIERUNG)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
public class ArchivdokumentResource {

    private final ArchivdokumentMapper archivdokumentMapper;
    private final FileStoreService fileStoreService;
    private final AuthorizerService authorizerService;


    @GET
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.ARCHIVIERUNG)
    public ArchivdokumentListJson findAllArchivdokumente(
        @NotNull @QueryParam("onlyOpen") boolean onlyOpen
    ) {
        return new ArchivdokumentListJson(archivdokumentMapper.findAll(onlyOpen));
    }

    @GET
    @Path("/{archivdokumentId}/file")
    @Consumes(MediaType.WILDCARD)
    @APIResponse(content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM,
        schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "OK", responseCode = "200")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @RolesAllowed({BerechtigungName.ARCHIVIERUNG})
    @NonNull
    @Blocking
    public Response downloadArchivFile(
        @PathParam("archivdokumentId") @NotNull @NonNull ID<Archivdokument> archivId
    ) {
        authorizerService.requireArchivDownloadAuthorization(archivId);
        FileInfo fileInfo = archivdokumentMapper.downloadArchivFile(archivId);
        var content = fileStoreService.getFileBytesSync(fileInfo.getFileIdInBucket());
        return FileDownloadUtil.buildFileResponse("attachment", content, fileInfo);
    }
}
