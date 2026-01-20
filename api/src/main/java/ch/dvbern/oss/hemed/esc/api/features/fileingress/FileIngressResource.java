/*
 *
 * Copyright (C) 2024 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.hemed.esc.api.features.fileingress;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.UploadItemSchema;
import ch.dvbern.oss.hemed.esc.api.shared.types.IgnoreForIdClassTest;
import io.smallrye.faulttolerance.api.RateLimit;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@Path("/file-ingress")
@JaxResource
@RequiredArgsConstructor
@Tag(name = OpenApiConst.Tags.TAG_FILE_INGRESS)
@RolesAllowed(BerechtigungName.FILE_INGRESS_PUSH)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
public class FileIngressResource {

    private final FileIngressService fileIngressService;

    @POST
    @Path("/dossier/{esadId}/fragebogen")
    @NonNull
    @RolesAllowed(BerechtigungName.FILE_INGRESS_PUSH)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponse(
        responseCode = "200",
        description = "File ingress successful",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = FileIngressResultJson.class)
        )
    )
    @APIResponse(
        responseCode = "400",
        description = "File ingress failed",
        content = @Content(
            mediaType = MediaType.APPLICATION_JSON,
            schema = @Schema(implementation = FileIngressResultJson.class)
        )
    )
    public Response uploadFragebogen(
        @NotNull @NonNull @PathParam("esadId") @IgnoreForIdClassTest String esadId,
        @FormParam(value = "file")
        @Schema(implementation = UploadItemSchema.class) // schema genaration produces object instead of binary string otherwise
        FileUpload fragebogenFile
    ) {
        FileIngressResultJson result =
            fileIngressService.processUploadFragebogen(esadId, fragebogenFile);
        if (result.getStatus() == FileIngressStatus.ERROR) {
            return  Response.status(Status.BAD_REQUEST).entity(result).build();
        }
        return Response.ok(result).entity(result).build();
    }
}
