/*
 * Copyright (C) 2025 DV Bern AG, Switzerland
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.hemed.esc.api.features.importoptions.massenimport;

import java.util.Map;

import ch.dvbern.dvbstarter.infrastructure.fileio.FileDownloadUtil;
import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.schule.Schule;
import ch.dvbern.oss.hemed.esc.api.features.schule.klasse.Klasse;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.UploadItemSchema;
import ch.dvbern.oss.hemed.esc.api.shared.printer.PrintResultDTO;
import io.smallrye.faulttolerance.api.RateLimit;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@Path("/massenimport")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_MASSENIMPORT)
@RolesAllowed(BerechtigungName.KINDER_SCHREIBEN_NICHT_MEDIZINISCH)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
public class MassenimportResource {

    private final MassenimportService massenImportService;

    @GET
    @Consumes(MediaType.WILDCARD)
    @APIResponse(content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM,
        schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "OK", responseCode = "200")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @NonNull
    @Path("/template/standard")
    @RolesAllowed(BerechtigungName.KINDER_SCHREIBEN_NICHT_MEDIZINISCH)
    public Response downloadStandardTemplate() {
        PrintResultDTO printResultDTO = massenImportService.getTemplateFile(true);

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
    @Path("/template/klasse")
    @RolesAllowed(BerechtigungName.KINDER_SCHREIBEN_NICHT_MEDIZINISCH)
    public Response downloadKlassenTemplate() {
        PrintResultDTO printResultDTO = massenImportService.getTemplateFile(false);

        return FileDownloadUtil.buildFileResponse(
            "attachment",
            printResultDTO.getContent(),
            printResultDTO.getFileInfo());
    }

    @POST
    @Path("/schule/{schuleID}/klasse/{klasseID}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(BerechtigungName.KINDER_SCHREIBEN_NICHT_MEDIZINISCH)
    public Response massenimportKinderForSchuleAndKlasse(
        @FormParam(value = "file") @Schema(implementation = UploadItemSchema.class) FileUpload fileUpload,
        @NonNull @NotNull @PathParam("schuleID") ID<Schule> schuleID,
        @NonNull @NotNull @PathParam("klasseID") ID<Klasse> klasseID
    ) {
        massenImportService.validateFileStructureThenProcessFileAsync(
            fileUpload,
            schuleID,
            klasseID,
            true
        );
        return okResponse();
    }

    @POST
    @Path("/schule/{schuleID}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed(BerechtigungName.KINDER_SCHREIBEN_NICHT_MEDIZINISCH)
    public Response massenimportKinderForSchuleExtractKlassen(
        @FormParam(value = "file") @Schema(implementation = UploadItemSchema.class) FileUpload fileUpload,
        @NonNull @NotNull @PathParam("schuleID") ID<Schule> schuleID
    ) {
        massenImportService.validateFileStructureThenProcessFileAsync(
            fileUpload,
            schuleID,
            null,
            false
        );
        return okResponse();
    }

    private Response okResponse() {

        return Response.status(Response.Status.OK)
            .entity(Map.of(
                "status", "in_progress",
                "message", "The file structure is valid, import started",
                "hint", "An E-Mail will be sent with the results of the import"
            )).build();
    }
}
