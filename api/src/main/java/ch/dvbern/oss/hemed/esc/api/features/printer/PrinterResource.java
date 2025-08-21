package ch.dvbern.oss.hemed.esc.api.features.printer;

import ch.dvbern.dvbstarter.infrastructure.fileio.FileDownloadUtil;
import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintResultDTO;
import ch.dvbern.oss.hemed.esc.api.shared.printer.kinderdossier.KinderdossierPrintVariant;
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
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/printer")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_PRINTER)
@RolesAllowed(BerechtigungName.SUPPORT)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed(name = "PrinterResourceMS", unit = MetricUnits.MILLISECONDS)
public class PrinterResource {

    public static final String ATTACHMENT = "attachment";
    private final PrinterMapper printerMapper;

    @GET
    @Path("/kind/{kindId}")
    @Consumes(MediaType.WILDCARD)
    @APIResponse(content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM,
        schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "OK", responseCode = "200")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.KOMPLETTER_EXPORT)
    @Blocking
    public Response downloadKinderdossier(
        @NotNull @NonNull @PathParam("kindId") ID<Kind> kindId,
        @NotNull @NonNull @QueryParam("variation") KinderdossierPrintVariant variation
    ) {
        PrintResultDTO printResultDTO = printerMapper.printKinderdossier(variation, kindId);

        return FileDownloadUtil.buildFileResponse(
            ATTACHMENT,
            printResultDTO.getContent(),
            printResultDTO.getFileInfo());
    }

    @GET
    @Path("kind/{kindId}/anlass/{anlassId}")
    @Consumes(MediaType.WILDCARD)
    @APIResponse(content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM,
        schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "OK", responseCode = "200")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.KOMPLETTER_EXPORT)
    @Blocking
    public Response downloadKinderdossierForAnlass(
        @NotNull @NonNull @PathParam("kindId") ID<Kind> kindId,
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId
    ) {
        PrintResultDTO printResultDTO = printerMapper.printKinderdossierForAnlass(kindId, anlassId);

        return FileDownloadUtil.buildFileResponse(
            ATTACHMENT,
            printResultDTO.getContent(),
            printResultDTO.getFileInfo());
    }

    @GET
    @Path("/anlass/{anlassId}")
    @Consumes(MediaType.WILDCARD)
    @APIResponse(content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM,
        schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "OK", responseCode = "200")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed({ BerechtigungName.KOMPLETTER_EXPORT, BerechtigungName.ANLASSPLANUNG_SCHREIBEN })
    @Blocking
    public Response downloadAnlassPrint(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId
    ) {
        PrintResultDTO printResultDTO = printerMapper.printAnlass(anlassId);

        return FileDownloadUtil.buildFileResponse(
            ATTACHMENT,
            printResultDTO.getContent(),
            printResultDTO.getFileInfo());
    }
}
