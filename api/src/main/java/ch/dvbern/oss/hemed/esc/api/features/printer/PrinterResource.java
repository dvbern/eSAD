package ch.dvbern.oss.hemed.esc.api.features.printer;

import java.util.List;
import java.util.Objects;
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

import ch.dvbern.dvbstarter.infrastructure.fileio.FileDownloadUtil;
import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anonymereintrag.AnonymerEintrag;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anonymereintrag.AnonymerEintragService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.untersuchung.Untersuchung;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.shared.auth.AuthorizerService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.Berechtigung;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintResultDTO;
import ch.dvbern.oss.hemed.esc.api.shared.printer.anlass.AnlassPrintVariant;
import ch.dvbern.oss.hemed.esc.api.shared.printer.kinderdossier.KinderdossierPrintVariant;

import io.micrometer.core.annotation.Timed;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.faulttolerance.api.RateLimit;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
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
@Timed("PrinterResourceMS")
public class PrinterResource {

    public static final String ATTACHMENT = "attachment";

    private final PrinterMapper printerMapper;
    private final AuthorizerService authorizerService;
    private final AnonymerEintragService anonymerEintragService;

    @GET
    @Path("/kind/{kindId}")
    @Consumes(MediaType.WILDCARD)
    @APIResponse(content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM,
        schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "OK", responseCode = "200")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed({ BerechtigungName.KOMPLETTER_EXPORT })
    @Blocking
    public Response downloadKinderdossier(
        @NotNull @NonNull @PathParam("kindId") ID<Kind> kindId,
        @NotNull @NonNull @QueryParam("variant") KinderdossierPrintVariant variant
    ) {
        authorizerService.requireKinderDownloadAuthorization(kindId);
        PrintResultDTO printResultDTO = printerMapper.printKinderdossier(variant, kindId);

        return FileDownloadUtil.buildFileResponse(
            ATTACHMENT,
            printResultDTO.getContent(),
            printResultDTO.getFileInfo());
    }

    @GET
    @Path("/anonymer-eintrag/{anonymerEintragId}")
    @Consumes(MediaType.WILDCARD)
    @APIResponse(content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM,
        schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "OK", responseCode = "200")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed({ BerechtigungName.ANLASSPLANUNG_SCHREIBEN, BerechtigungName.ANLASSPLANUNG_SCHREIBEN_LAEUSE })
    @Blocking
    public Response downloadAnonymerEintrag(
        @NotNull @NonNull @PathParam("anonymerEintragId") ID<AnonymerEintrag> anonymerEintragId
    ) {
        Untersuchung untersuchung = anonymerEintragService.getUntersuchungForAnonymerEintragWithId(anonymerEintragId);
        Objects.requireNonNull(untersuchung);
        authorizerService.requireReadAuthorizationUntersuchung(
            untersuchung,
            List.of(Berechtigung.ANLASSPLANUNG_SCHREIBEN, Berechtigung.ANLASSPLANUNG_SCHREIBEN_LAEUSE)
        );
        PrintResultDTO printResultDTO = printerMapper.printAnonymerEintrag(anonymerEintragId);

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
        authorizerService.requireKinderDownloadAuthorization(kindId);
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
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @QueryParam("variant") AnlassPrintVariant variant
    ) {
        // Das Anlass-Dokument enthaelt Daten aller eingeladenen Kinder, daher nur fuer Anlass-Zustaendige
        authorizerService.requireReadAuthorizationAnlass(
            anlassId,
            List.of(Berechtigung.KOMPLETTER_EXPORT, Berechtigung.ANLASSPLANUNG_SCHREIBEN));
        PrintResultDTO printResultDTO = printerMapper.printAnlass(anlassId, variant);

        return FileDownloadUtil.buildFileResponse(
            ATTACHMENT,
            printResultDTO.getContent(),
            printResultDTO.getFileInfo());
    }
}
