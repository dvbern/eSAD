package ch.dvbern.oss.hemed.esc.api.features.notizen;

import java.util.List;
import java.util.Objects;

import ch.dvbern.dvbstarter.infrastructure.fileio.FileDownloadUtil;
import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.notizen.json.NotizJson;
import ch.dvbern.oss.hemed.esc.api.features.notizen.json.NotizListJson;
import ch.dvbern.oss.hemed.esc.api.features.notizen.json.NotizSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.auth.AuthorizerService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.Berechtigung;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileData;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileInfo;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileStoreService;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.UploadItemSchema;
import ch.dvbern.oss.hemed.esc.api.shared.notizen.RemoveNotizService;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.faulttolerance.api.RateLimit;
import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.core.buffer.Buffer;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.stream.Streams;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@Slf4j
@Path("/notizen")
@JaxResource
@Tag(name = OpenApiConst.Tags.TAG_NOTIZEN)
@RequiredArgsConstructor
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@RolesAllowed({BerechtigungName.KINDER_ALLE_NOTIZEN_LESEN, BerechtigungName.KINDER_NOTIZEN_MIT_LABEL_DISPENSATIONSGESUCHE_LESEN})
public class NotizResource {

    private final NotizMapper notizMapper;
    private final NotizService notizService;
    private final FileStoreService fileStoreService;
    private final AuthorizerService authorizerService;
    private final RemoveNotizService removeNotizService;


    @GET
    @NonNull
    @RolesAllowed({BerechtigungName.KINDER_ALLE_NOTIZEN_LESEN, BerechtigungName.KINDER_NOTIZEN_MIT_LABEL_DISPENSATIONSGESUCHE_LESEN})
    public NotizListJson findAllBerechtigt() {
        return new NotizListJson(notizMapper.findAllBerechtigt(
            notiz -> authorizerService.hasReadAuthorizationNotiz(
                notiz,
                List.of(Berechtigung.KINDER_ALLE_NOTIZEN_LESEN, Berechtigung.KINDER_NOTIZEN_MIT_LABEL_DISPENSATIONSGESUCHE_LESEN))
        ));
    }

    @PUT
    @Path("/{notizId}")
    @NonNull
    @RolesAllowed({BerechtigungName.KINDER_ALLE_NOTIZEN_SCHREIBEN, BerechtigungName.KINDER_NOTIZEN_MIT_LABEL_DISPENSATIONSGESUCHE_SCHREIBEN})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public NotizJson updateNotiz(
        @PathParam("notizId") @NotNull @NonNull ID<Notiz> notizId,
        @Nullable @RestForm("file") final FileUpload fileUpload,
        @Nullable @RestForm("file2") final FileUpload fileUpload2,
        @Nullable @RestForm("file3") final FileUpload fileUpload3,
        @Nullable @RestForm("file4") final FileUpload fileUpload4,
        @NotNull @NonNull @Valid @RestForm("notizSaveJson") @PartType(MediaType.APPLICATION_JSON) final NotizSaveJson saveJson
    ) {
        // collect uploadedFiles
        List<FileUpload> fileUploads = Streams.of(fileUpload, fileUpload2, fileUpload3, fileUpload4)
            .filter(Objects::nonNull)
            .toList();
        return notizMapper.update(
            notizId,
            saveJson,
            fileUploads,
            notiz -> authorizerService.requireWriteAuthorizationNotiz(
                notiz,
                List.of (Berechtigung.KINDER_ALLE_NOTIZEN_SCHREIBEN, Berechtigung.KINDER_NOTIZEN_MIT_LABEL_DISPENSATIONSGESUCHE_SCHREIBEN)));
    }

    @POST
    @Path("/{notizId}/file")
    @RolesAllowed({BerechtigungName.KINDER_ALLE_NOTIZEN_SCHREIBEN, BerechtigungName.KINDER_NOTIZEN_MIT_LABEL_DISPENSATIONSGESUCHE_SCHREIBEN})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public NotizJson addNotizFile(
        @PathParam("notizId") @NotNull @NonNull ID<Notiz> notizId,
        @FormParam(value = "file") @Schema(implementation = UploadItemSchema.class)
        // schema genaration produces object instread of binary strin otherwise
        FileUpload file
    ) {
        return notizMapper.addUploadedFile(notizId, file,
            notiz -> authorizerService.requireWriteAuthorizationNotiz(
                notiz,
                List.of (Berechtigung.KINDER_ALLE_NOTIZEN_SCHREIBEN, Berechtigung.KINDER_NOTIZEN_MIT_LABEL_DISPENSATIONSGESUCHE_SCHREIBEN)));
    }

    @DELETE
    @Path("/{notizId}/file/{fileId}")
    @RolesAllowed({BerechtigungName.KINDER_ALLE_NOTIZEN_SCHREIBEN, BerechtigungName.KINDER_NOTIZEN_MIT_LABEL_DISPENSATIONSGESUCHE_SCHREIBEN})
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public NotizJson removeNotizFile(
        @PathParam("notizId") @NotNull @NonNull ID<Notiz> notizId,
        @NotNull @NonNull @PathParam("fileId") ID<FileData> fileId
    ) {
        return notizMapper.removeUploadedFile(notizId, fileId,
            notiz -> authorizerService.requireWriteAuthorizationNotiz(
                notiz,
                List.of (Berechtigung.KINDER_ALLE_NOTIZEN_SCHREIBEN, Berechtigung.KINDER_NOTIZEN_MIT_LABEL_DISPENSATIONSGESUCHE_SCHREIBEN)));
    }

    /**
     * use a specific endpoint for downloads in order to make sure that we can check the Berechtigung
     */
    @GET
    @Path("/{notizId}/file/{fileId}")
    @Consumes(MediaType.WILDCARD)
    @APIResponse(content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM,
        schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "OK", responseCode = "200")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @RolesAllowed({BerechtigungName.KINDER_ALLE_NOTIZEN_LESEN, BerechtigungName.KINDER_NOTIZEN_MIT_LABEL_DISPENSATIONSGESUCHE_LESEN})
    @NonNull
    @Blocking
    public Multi<Buffer> downloadNotizFile(
        @PathParam("notizId") @NotNull @NonNull ID<Notiz> notizId,
        @NotNull @NonNull @PathParam("fileId") ID<FileData> fileId
    ) {
        FileInfo fileInfo = notizMapper.findUploadedFileInfo(
            notizId,
            fileId,
            notiz -> authorizerService.requireReadAuthorizationNotiz(
                notiz,
                List.of (Berechtigung.KINDER_ALLE_NOTIZEN_LESEN, Berechtigung.KINDER_NOTIZEN_MIT_LABEL_DISPENSATIONSGESUCHE_LESEN)));
        return FileDownloadUtil.getBufferRestMultiWithInfo(fileInfo, fileStoreService);
    }

    /**
     * since we have trouble settings for content disposition we currently use a sync endpoint
     */
    @GET
    @Path("/{notizId}/file/{fileId}/sync")
    @Consumes(MediaType.WILDCARD)
    @APIResponse(content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM,
        schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "OK", responseCode = "200")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @RolesAllowed({BerechtigungName.KINDER_ALLE_NOTIZEN_LESEN, BerechtigungName.KINDER_NOTIZEN_MIT_LABEL_DISPENSATIONSGESUCHE_LESEN})
    @NonNull
    @Blocking
    public Response downloadNotizFileSync(
        @PathParam("notizId") @NotNull @NonNull ID<Notiz> notizId,
        @NotNull @NonNull @PathParam("fileId") ID<FileData> fileId
    ) {
        FileInfo fileInfo = notizMapper.findUploadedFileInfo(
            notizId,
            fileId,
            notiz -> authorizerService.requireReadAuthorizationNotiz(
                notiz,
                List.of (Berechtigung.KINDER_ALLE_NOTIZEN_LESEN, Berechtigung.KINDER_NOTIZEN_MIT_LABEL_DISPENSATIONSGESUCHE_LESEN)));
        var content = fileStoreService.getFileBytesSync(fileInfo.getFileIdInBucket());
        return FileDownloadUtil.buildFileResponse("attachment", content, fileInfo);
    }

    @DELETE
    @Path("/{notizId}")
    @RolesAllowed({BerechtigungName.KINDER_ALLE_NOTIZEN_SCHREIBEN, BerechtigungName.KINDER_NOTIZEN_MIT_LABEL_DISPENSATIONSGESUCHE_SCHREIBEN})
    public void deleteNotiz(
        @PathParam("notizId") @NotNull @NonNull ID<Notiz> notizId
    ) {
        Notiz notiz = notizService.getById(notizId);
        authorizerService.requireWriteAuthorizationNotiz(
            notiz,
            List.of (Berechtigung.KINDER_ALLE_NOTIZEN_SCHREIBEN, Berechtigung.KINDER_NOTIZEN_MIT_LABEL_DISPENSATIONSGESUCHE_SCHREIBEN));
        notizMapper.checkRemoveAllowed(notiz);
        removeNotizService.removeNotizWithUnlink(notizId);
    }

    @GET
    @NonNull
    @Path("/{notizId}")
    @RolesAllowed({BerechtigungName.KINDER_ALLE_NOTIZEN_LESEN, BerechtigungName.KINDER_NOTIZEN_MIT_LABEL_DISPENSATIONSGESUCHE_LESEN})
    public NotizJson getNotiz(
        @PathParam("notizId") @NotNull @NonNull ID<Notiz> notizId
    ) {
        return notizMapper.getNotiz(
            notizId,
            notiz -> authorizerService.requireReadAuthorizationNotiz(
                notiz,
                List.of (Berechtigung.KINDER_ALLE_NOTIZEN_LESEN, Berechtigung.KINDER_NOTIZEN_MIT_LABEL_DISPENSATIONSGESUCHE_LESEN)));
    }
}
