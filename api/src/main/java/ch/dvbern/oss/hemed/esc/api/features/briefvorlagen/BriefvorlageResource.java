package ch.dvbern.oss.hemed.esc.api.features.briefvorlagen;

import ch.dvbern.dvbstarter.infrastructure.fileio.FileDownloadUtil;
import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.arzt.Arzt;
import ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.json.BriefvorlageJson;
import ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.json.BriefvorlageListJson;
import ch.dvbern.oss.hemed.esc.api.features.briefvorlagen.json.BriefvorlageSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileInfo;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileStoreService;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintResultDTO;
import io.smallrye.faulttolerance.api.RateLimit;
import io.vertx.core.http.HttpServerResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import io.micrometer.core.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@Path("/briefvorlagen")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_BRIEFVORLAGE)
@RolesAllowed(BerechtigungName.VORLAGEN_LESEN)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed("BriefvorlageResourceMS")
public class BriefvorlageResource {

    private final BriefvorlageMapper briefvorlageMapper;
    private final BriefvorlageService briefvorlageService;
    private final FileStoreService fileStoreService;

    @Context
    private HttpServerResponse response;

    @GET
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed({ BerechtigungName.VORLAGEN_LESEN, BerechtigungName.VORLAGEN_ADMIN_LESEN })
    public BriefvorlageListJson findAllBriefvorlagen() {
        return briefvorlageMapper.findAll();
    }

    @POST
    @NonNull
    @RolesAllowed(BerechtigungName.VORLAGEN_SCHREIBEN)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public BriefvorlageJson createBriefvorlage(
        @NotNull @NonNull @RestForm("file") final FileUpload fileUpload,
        @NotNull @NonNull @Valid @RestForm("briefvorlageSaveJson") @PartType(MediaType.APPLICATION_JSON)
        final BriefvorlageSaveJson briefvorlageJson
    ) {
        response.setStatusCode(Status.CREATED.getStatusCode());
        return briefvorlageMapper.create(briefvorlageJson, fileUpload);
    }

    @PUT
    @Path("/{briefvorlageId}")
    @NonNull
    @RolesAllowed(BerechtigungName.VORLAGEN_SCHREIBEN)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public BriefvorlageJson updateBriefvorlage(
        @NotNull @NonNull @PathParam("briefvorlageId") ID<Briefvorlage> briefvorlageID,
        @Nullable @RestForm("file") final FileUpload fileUpload,
        @NotNull @NonNull @Valid @RestForm("briefvorlageSaveJson") @PartType(MediaType.APPLICATION_JSON)
        final BriefvorlageSaveJson briefvorlageJson
    ) {
        return briefvorlageMapper.update(briefvorlageID, briefvorlageJson, fileUpload);
    }

    @DELETE
    @Path("/{briefvorlageId}")
    @RolesAllowed(BerechtigungName.VORLAGEN_SCHREIBEN)
    public void removeBriefvorlage(@NotNull @NonNull @PathParam("briefvorlageId") ID<Briefvorlage> briefvorlageID) {
        briefvorlageMapper.remove(briefvorlageID);
    }

    @GET
    @Consumes(MediaType.WILDCARD)
    @APIResponse(content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM,
        schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "OK", responseCode = "200")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @NonNull
    @Path("/{briefvorlageId}/kind/{kindId}/download")
    @RolesAllowed(BerechtigungName.VORLAGEN_LESEN)
    public Response downloadBrief(
        @NotNull @NonNull @PathParam("briefvorlageId") ID<Briefvorlage> briefvorlageId,
        @NotNull @NonNull @PathParam("kindId") ID<Kind> kindId,
        @Nullable @QueryParam("einladungId") ID<Einladung> einladungId,
        @Nullable @QueryParam("arztId") ID<Arzt> arztId
    ) {
        PrintResultDTO printResultDTO = briefvorlageMapper.generateBrief(briefvorlageId, kindId, einladungId, arztId);

        return FileDownloadUtil.buildFileResponse(
            "attachment",
            printResultDTO.getContent(),
            printResultDTO.getFileInfo());
    }

    @GET
    @Consumes(MediaType.WILDCARD)
    @APIResponse(content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM,
        schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "OK", responseCode = "200")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @NonNull
    @Path("/{briefvorlageId}/download/test")
    @RolesAllowed(BerechtigungName.VORLAGEN_LESEN)
    public Response downloadTestBrief(
        @NotNull @NonNull @PathParam("briefvorlageId") ID<Briefvorlage> briefvorlageId
    ) {
        PrintResultDTO printResultDTO = briefvorlageMapper.generateTestBrief(briefvorlageId);

        return FileDownloadUtil.buildFileResponse(
            "attachment",
            printResultDTO.getContent(),
            printResultDTO.getFileInfo());
    }

    @GET
    @Consumes(MediaType.WILDCARD)
    @APIResponse(content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM,
        schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "OK", responseCode = "200")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @NonNull
    @Path("/{briefvorlageId}/download")
    @RolesAllowed(BerechtigungName.VORLAGEN_LESEN)
    public Response downloadBriefvorlageFile(
        @NotNull @NonNull @PathParam("briefvorlageId") ID<Briefvorlage> briefvorlageId
    ) {
        FileInfo fileInfo = briefvorlageService.findBriefvorlageFile(briefvorlageId);
        byte[] content = fileStoreService.getFileBytesSync(fileInfo.getFileIdInBucket());

        return FileDownloadUtil.buildFileResponse("attachment", content, fileInfo);
    }
}
