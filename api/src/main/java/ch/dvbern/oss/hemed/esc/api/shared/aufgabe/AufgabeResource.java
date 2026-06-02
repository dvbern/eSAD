package ch.dvbern.oss.hemed.esc.api.shared.aufgabe;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.fileio.FileDownloadUtil;
import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.json.AufgabeJson;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.json.AufgabeListJson;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.json.AufgabeSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.aufgabe.json.NumOfOffeneFaelligeAufgabeJson;
import ch.dvbern.oss.hemed.esc.api.shared.auth.AuthorizerService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.Berechtigung;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileData;

import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileInfo;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileStoreService;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.UploadItemSchema;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.faulttolerance.api.RateLimit;
import io.smallrye.mutiny.Multi;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.mutiny.core.buffer.Buffer;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import io.micrometer.core.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@Path("/aufgaben")
@JaxResource
@RequiredArgsConstructor
@Tag(name = OpenApiConst.Tags.TAG_AUFGABE)
@RolesAllowed(BerechtigungName.AUFGABEN_LESEN)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed("AufgabeResourceMS")
public class AufgabeResource {

    private final AufgabeMapper aufgabeMapper;
    private final AufgabeService aufgabeService;
    private final FileStoreService fileStoreService;
    private final AuthorizerService authorizerService;

    @Context
    private HttpServerResponse response;

    @GET
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.AUFGABEN_LESEN)
    public AufgabeListJson findAllAufgaben() {
        return this.aufgabeMapper.findAllAufgabenBerechtigt(aufgabe ->
            authorizerService.hasReadAuthorizationAufgabe(
                aufgabe,
                List.of(Berechtigung.AUFGABEN_LESEN)));
    }

    @GET
    @Path("/kind/{kindId}")
    @NonNull
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.AUFGABEN_LESEN)
    public AufgabeListJson findAllForKind(
        @PathParam("kindId") @NotNull @NonNull ID<Kind> kindId
    ) {
        return this.aufgabeMapper.findAllForKind(
            kindId,
            aufgabe -> authorizerService.hasReadAuthorizationAufgabe(
                aufgabe,
                List.of(Berechtigung.AUFGABEN_LESEN)));
    }

    @POST
    @NonNull
    @RolesAllowed(BerechtigungName.AUFGABEN_SCHREIBEN)
    public AufgabeJson createAufgabe(
        @NotNull @NonNull @Valid AufgabeSaveJson aufgabeJson
    ) {
        AufgabeJson newAufgabeJson = aufgabeMapper.create(
            aufgabeJson,
            aufgabe -> authorizerService.requireWriteAuthorizationAufgabe(
                aufgabe,
                List.of(Berechtigung.AUFGABEN_SCHREIBEN)));
        response.setStatusCode(Status.CREATED.getStatusCode());
        return newAufgabeJson;
    }

    @PUT
    @Path("/{aufgabeId}")
    @NonNull
    @RolesAllowed(BerechtigungName.AUFGABEN_SCHREIBEN)
    public AufgabeJson updateAufgabe(
        @NotNull @NonNull @PathParam("aufgabeId") ID<Aufgabe> aufgabeID,
        @NotNull @NonNull @Valid AufgabeSaveJson aufgabeJson
    ) {
        return aufgabeMapper.update(
            aufgabeID,
            aufgabeJson,
            aufgabe -> authorizerService.requireWriteAuthorizationAufgabe(
                aufgabe,
                List.of(Berechtigung.AUFGABEN_SCHREIBEN)));
    }

    @GET
    @Path("/offenefaellige/self")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.AUFGABEN_LESEN)
    public NumOfOffeneFaelligeAufgabeJson getNumOfFaelligeAufgabenForCurrentUser() {
        return new NumOfOffeneFaelligeAufgabeJson(this.aufgabeMapper.getNumOfFaelligeAufgabenForCurrentUser());
    }

    @GET
    @Path("/offenefaellige")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.AUFGABEN_LESEN)
    public NumOfOffeneFaelligeAufgabeJson getNumOfFaelligeAufgaben() {
        return new NumOfOffeneFaelligeAufgabeJson(this.aufgabeMapper.getNumOfOffeneFaelligeAufgaben(
            aufgabe -> authorizerService.hasReadAuthorizationAufgabe(
                aufgabe,
                List.of(Berechtigung.AUFGABEN_LESEN))
        ));
    }

    @DELETE
    @Path("/{aufgabeId}")
    @RolesAllowed(BerechtigungName.AUFGABEN_SCHREIBEN)
    public void removeAufgabe(@NotNull @NonNull @PathParam("aufgabeId") ID<Aufgabe> aufgabeId) {
        aufgabeMapper.remove(
            aufgabeId,
            aufgabe -> authorizerService.requireWriteAuthorizationAufgabe(
                aufgabe,
                List.of(Berechtigung.AUFGABEN_SCHREIBEN)));
    }

    @POST
    @RolesAllowed(BerechtigungName.AUFGABEN_SCHREIBEN)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/{aufgabeId}/file")
    public AufgabeJson addAufgabeFile(
        @PathParam("aufgabeId") @NotNull @NonNull ID<Aufgabe> aufgabeID,
        @FormParam(value = "file") @Schema(implementation = UploadItemSchema.class) FileUpload file
    ) {
        return aufgabeMapper.addUploadedFile(
            aufgabeID,
            file,
            aufgabe -> authorizerService.requireWriteAuthorizationAufgabe(
                aufgabe,
                List.of(Berechtigung.AUFGABEN_SCHREIBEN)));
    }

    @DELETE
    @RolesAllowed(BerechtigungName.AUFGABEN_SCHREIBEN)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/{aufgabeId}/file/{fileId}")
    public AufgabeJson removeAufgabeFile(
        @PathParam("aufgabeId") @NotNull @NonNull ID<Aufgabe> aufgabeId,
        @NotNull @NonNull @PathParam("fileId") ID<FileData> fileId
    ) {
        return aufgabeMapper.removeUploadedFile(
            aufgabeId,
            fileId,
            aufgabe -> authorizerService.requireWriteAuthorizationAufgabe(
                aufgabe,
                List.of(Berechtigung.AUFGABEN_SCHREIBEN)));
    }

    /**
     * use a specific endpoint for downloads in order to make sure that we can check the Berechtigung
     */
    @GET
    @Consumes(MediaType.WILDCARD)
    @APIResponse(content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM,
        schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "OK", responseCode = "200")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/{aufgabeId}/file/{fileId}")
    @RolesAllowed(BerechtigungName.AUFGABEN_LESEN)
    @NonNull
    @Blocking
    public Multi<Buffer> downloadAufgabeFile(
        @PathParam("aufgabeId") @NotNull @NonNull ID<Aufgabe> aufgabeId,
        @NotNull @NonNull @PathParam("fileId") ID<FileData> fileId
    ) {
        FileInfo fileInfoS3 = aufgabeService.findUploadedFileInfoS3(
            aufgabeId,
            fileId,
            aufgabe -> authorizerService.requireReadAuthorizationAufgabe(
                aufgabe,
                List.of(Berechtigung.AUFGABEN_LESEN)));
        return FileDownloadUtil.getBufferRestMultiWithInfo(fileInfoS3, fileStoreService);
    }

    /**
     * since we have trouble settings for content disposition we currently use a sync endpoint
     */
    @GET
    @Consumes(MediaType.WILDCARD)
    @APIResponse(content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM,
        schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "OK", responseCode = "200")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @Path("/{aufgabeId}/file/{fileId}/sync")
    @RolesAllowed(BerechtigungName.AUFGABEN_LESEN)
    @NonNull
    @Blocking
    public Response downloadAufgabeFileSync(
        @PathParam("aufgabeId") @NotNull @NonNull ID<Aufgabe> aufgabeId,
        @NotNull @NonNull @PathParam("fileId") ID<FileData> fileId
    ) {
        FileInfo fileInfo = aufgabeService.findUploadedFileInfoS3(
            aufgabeId,
            fileId,
            aufgabe -> authorizerService.requireReadAuthorizationAufgabe(
                aufgabe,
                List.of(Berechtigung.AUFGABEN_LESEN)));
        var content = fileStoreService.getFileBytesSync(fileInfo.getFileIdInBucket());
        return FileDownloadUtil.buildFileResponse("attachment", content, fileInfo);
    }

    @GET
    @Path("/{aufgabeId}")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.AUFGABEN_LESEN)
    public AufgabeJson getAufgabe(
        @NotNull @NonNull @PathParam("aufgabeId") ID<Aufgabe> aufgabeId
    ) {
        return aufgabeMapper.getById(
            aufgabeId,
            aufgabe -> authorizerService.requireReadAuthorizationAufgabe(
                aufgabe,
                List.of(Berechtigung.AUFGABEN_LESEN)));
    }

    @GET
    @Path("/self")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.AUFGABEN_LESEN)
    public AufgabeListJson findAllNichtAbgeschlosseneAufgabenForCurrent() {
        return this.aufgabeMapper.findAllNichtAbgeschlosseneAufgabenForCurrent();
    }
}
