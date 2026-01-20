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

package ch.dvbern.oss.hemed.esc.api.features.importoptions.escada;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst;
import ch.dvbern.oss.hemed.esc.api.features.importoptions.escada.json.EscadaImportResultJson;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.UploadItemSchema;
import io.smallrye.faulttolerance.api.RateLimit;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@Path("/escada")
@JaxResource
@RequiredArgsConstructor
@Tag(name = OpenApiConst.Tags.TAG_ESCADA)
@RolesAllowed(BerechtigungName.ESCADA_PUSH)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
public class EscadaResource {

    private final EscadaService escadaService;
    private final EscadaWorkerService escadaWorkerService;

    @POST
    @Path("/sync")
    @NonNull
    @RolesAllowed(BerechtigungName.ESCADA_PUSH)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Transactional(TxType.NOT_SUPPORTED)
    public EscadaImportResultJson syncWithEscada(
        @FormParam(value = "file")
        @Schema(implementation = UploadItemSchema.class)
        // schema genaration produces object instead of binary string otherwise
        FileUpload csvFile
    ) {
        return escadaService.processEscadaCsvUpload(csvFile);
    }

    @POST
    @RolesAllowed(BerechtigungName.ESCADA_PUSH)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Transactional(TxType.NOT_SUPPORTED)
    public Response syncWithEscadaAsync(
        @FormParam(value = "file")
        @Schema(implementation = UploadItemSchema.class)
        // schema genaration produces object instead of binary string otherwise
        FileUpload csvFile
    ) {
        escadaWorkerService.processEscadaCsvUploadUsingEventBus(csvFile);
        return Response.ok().build();
    }
}
