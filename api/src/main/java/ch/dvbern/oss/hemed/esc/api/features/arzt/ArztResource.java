package ch.dvbern.oss.hemed.esc.api.features.arzt;

import ch.dvbern.dvbstarter.infrastructure.fileio.FileDownloadUtil;
import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.arzt.json.ArztIdListJson;
import ch.dvbern.oss.hemed.esc.api.features.arzt.json.ArztJson;
import ch.dvbern.oss.hemed.esc.api.features.arzt.json.ArztListJson;
import ch.dvbern.oss.hemed.esc.api.features.arzt.json.ArztSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.arzt.types.ExportMode;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintResultDTO;
import io.smallrye.common.constraint.NotNull;
import io.smallrye.faulttolerance.api.RateLimit;
import io.vertx.core.http.HttpServerResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/aerzte")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_AERZTE)
@RolesAllowed(BerechtigungName.AERZTE_LESEN)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
public class ArztResource {

    private final ArztMapper arztMapper;

    @Context
    private HttpServerResponse response;

    @POST
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.AERZTE_SCHREIBEN)
    public ArztJson createArzt(@NotNull @NonNull @Valid ArztSaveJson arztSaveJson) {
        response.setStatusCode(Status.CREATED.getStatusCode());
        return arztMapper.create(arztSaveJson);
    }

    @PUT
    @Path("/{arztId}")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.AERZTE_SCHREIBEN)
    public ArztJson updateArzt(
        @NotNull @NonNull @PathParam("arztId") ID<Arzt> arztId,
        @NotNull @NonNull @Valid ArztSaveJson arzt) {
        return arztMapper.update(arztId, arzt);
    }

    @GET
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.AERZTE_LESEN)
    public ArztListJson findAllArzte() {
        return new ArztListJson(arztMapper.findAll());
    }

    @GET
    @Path("/{arztId}")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.AERZTE_LESEN)
    public ArztJson getArzt(@NotNull @NonNull @PathParam("arztId") ID<Arzt> arztId) {
        return arztMapper.getById(arztId);
    }

    @DELETE
    @Path("/{arztId}")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.AERZTE_LESEN)
    public void removeArzt(@NotNull @NonNull @PathParam("arztId") ID<Arzt> arztId) {
        arztMapper.remove(arztId);
    }

    @POST
    @Path("/export/{mode}")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.AERZTE_LESEN)
    @Consumes(MediaType.APPLICATION_JSON)
    @APIResponse(content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM,
        schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "OK", responseCode = "200")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response exportAerzte(@NotNull @NonNull @Valid ArztIdListJson arztIdListjson, @PathParam("mode")
    ExportMode exportMode) {
        PrintResultDTO printResultDTO = arztMapper.exportArztList(arztIdListjson.getArztIdList(), exportMode);

        return FileDownloadUtil.buildFileResponse(
            "attachment",
            printResultDTO.getContent(),
            printResultDTO.getFileInfo());
    }
}
