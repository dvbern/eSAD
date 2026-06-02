package ch.dvbern.oss.hemed.esc.api.shared.preview;

import ch.dvbern.dvbstarter.infrastructure.fileio.FileDownloadUtil;
import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.dvbstarter.shared.types.cleanfilename.CleanFileName;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.notizen.Notiz;
import ch.dvbern.oss.hemed.esc.api.features.notizen.NotizMapper;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.Aufgabe;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.AufgabeService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.AuthorizerService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.Berechtigung;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileData;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileInfo;
import io.micrometer.core.annotation.Timed;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.faulttolerance.api.RateLimit;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.mime.MediaType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;

@Path("/preview")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_PREVIEW)
@Slf4j
@RolesAllowed(BerechtigungName.AUFGABEN_LESEN)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed("PreviewResourceMS")
public class PreviewResource {

    private final PreviewMapper previewMapper;
    private final AufgabeService aufgabeService;
    private final AuthorizerService authorizerService;
    private final NotizMapper notizMapper;

    private static Response toResponse(PreviewResult result) {
        FileInfo fileInfo = new FileInfo() {
            @Override
            public CleanFileName getFileName() {
                return CleanFileName.parse("preview.png");
            }

            @Override
            public MediaType getMediaType() {
                return MediaType.parse(result.getMimeType());
            }

            @Override
            public long getFileSize() {
                return result.getImageData().length;
            }
        };

        Response fileResponse = FileDownloadUtil.buildFileResponse("inline", result.getImageData(), fileInfo);
        return Response.fromResponse(fileResponse)
            .header(HttpHeaders.CACHE_CONTROL, "max-age=3600")
            .build();
    }

    @GET
    @APIResponse(content = @Content(mediaType = jakarta.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM,
        schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "OK", responseCode = "200")
    @Produces(jakarta.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM)
    @Path("/aufgaben/{aufgabeId}/file/{fileId}/preview")
    @RolesAllowed(BerechtigungName.AUFGABEN_LESEN)
    @NonNull
    @Blocking
    public Response downloadPreviewAufgabeFile(
        @PathParam("aufgabeId") @NotNull @NonNull ID<Aufgabe> aufgabeId,
        @NotNull @NonNull @PathParam("fileId") ID<FileData> fileId
    ) {
        FileData fileData = aufgabeService.findUploadedFileData(
            aufgabeId,
            fileId,
            aufgabe -> authorizerService.requireReadAuthorizationAufgabe(
                aufgabe,
                List.of(Berechtigung.AUFGABEN_LESEN)));

        PreviewResult result = previewMapper.generatePreview(fileData);
        return toResponse(result);

    }

    @GET
    @Path("/notizen/{notizId}/file/{fileId}/preview")
    @APIResponse(content = @Content(mediaType = jakarta.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM,
        schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "OK", responseCode = "200")
    @Produces(jakarta.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM)
    @RolesAllowed({BerechtigungName.KINDER_ALLE_NOTIZEN_LESEN, BerechtigungName.KINDER_NOTIZEN_MIT_LABEL_DISPENSATIONSGESUCHE_LESEN})
    @NonNull
    @Blocking
    public Response downloadPreviewNotizFile(
        @PathParam("notizId") @NotNull @NonNull ID<Notiz> notizId,
        @NotNull @NonNull @PathParam("fileId") ID<FileData> fileId
    ) {
        FileData fileData = notizMapper.findUploadedFileData(
            notizId,
            fileId,
            notiz -> authorizerService.requireReadAuthorizationNotiz(
                notiz,
                List.of (Berechtigung.KINDER_ALLE_NOTIZEN_LESEN, Berechtigung.KINDER_NOTIZEN_MIT_LABEL_DISPENSATIONSGESUCHE_LESEN)));
        PreviewResult result = previewMapper.generatePreview(fileData);
        return toResponse(result);
    }

}
