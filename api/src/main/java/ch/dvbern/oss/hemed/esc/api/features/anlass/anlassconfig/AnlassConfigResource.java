/*
 *
 * Copyright (C) 2025 DV Bern AG, Switzerland
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

package ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig;

import java.nio.charset.StandardCharsets;

import ch.dvbern.dvbstarter.infrastructure.fileio.FileDownloadUtil;
import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.json.AnlassConfigListJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.json.AnlassConfigOptionListJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.json.AnlassConfigSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.skripthelper.AnlassConfigSkriptJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.anlassconfig.skripthelper.AnlassConfigSkriptMapper;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import ch.dvbern.oss.hemed.esc.api.shared.auth.TenantProducer;
import ch.dvbern.oss.hemed.esc.api.shared.fileio.FileInfo;
import io.smallrye.faulttolerance.api.RateLimit;
import io.vertx.core.http.HttpServerResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
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
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/anlassconfig")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_ANLASSCONFIG)
@RolesAllowed(BerechtigungName.SUPPORT)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed(name = "AnlassConfigResourceMS", unit = MetricUnits.MILLISECONDS)
public class AnlassConfigResource {

    private final AnlassConfigMapper anlassConfigMapper;
    private final AnlassConfigSkriptMapper anlassConfigSkriptMapper;
    private final TenantProducer tenantProducer;

    @Context
    private HttpServerResponse response;

    @GET
    @NonNull
    @RolesAllowed(BerechtigungName.SUPPORT)
    public AnlassConfigListJson findAllAnlassConfigs() {
        return anlassConfigMapper.findAll();
    }

    @GET
    @Path("/visible")
    @NonNull
    @RolesAllowed({BerechtigungName.ANLASSLISTE_LESEN, BerechtigungName.ANLASSLISTE_LESEN_LAEUSE})
    public AnlassConfigOptionListJson findAllAnlassConfigsVisible() {
        return anlassConfigMapper.findAllVisible();
    }

    @POST
    @NonNull
    @RolesAllowed(BerechtigungName.SUPPORT)
    public AnlassConfigListJson createAnlassConfig(
        @NotNull @NonNull @Valid AnlassConfigSaveJson anlassConfigSaveJson
    ) {
        response.setStatusCode(Status.CREATED.getStatusCode());
        anlassConfigMapper.createAnlassConfig(anlassConfigSaveJson);
        return anlassConfigMapper.findAll();
    }

    @PUT
    @NonNull
    @Path("/{anlassConfigId}")
    @RolesAllowed(BerechtigungName.SUPPORT)
    public AnlassConfigListJson updateAnlassConfig(
        @NotNull @NonNull @PathParam("anlassConfigId") ID<AnlassConfig> anlassConfigId,
        @NotNull @NonNull @Valid AnlassConfigSaveJson anlassConfigSaveJson
    ) {
        anlassConfigMapper.updateAnlassConfig(anlassConfigId, anlassConfigSaveJson);
        return anlassConfigMapper.findAll();
    }

    @GET
    @Path("/script/{anlassConfigId}")
    @Consumes(MediaType.WILDCARD)
    @APIResponse(content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM,
        schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "OK", responseCode = "200")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @RolesAllowed(BerechtigungName.SUPPORT)
    @NonNull
    public Response downloadSqlSkripts(
        @NotNull @NonNull @PathParam("anlassConfigId") ID<AnlassConfig> anlassConfigId
    ) {
        AnlassConfigSkriptJson sqlSkripts = anlassConfigSkriptMapper.getSqlSkripts(anlassConfigId);
        byte[] content = sqlSkripts.getFileContent().getBytes(StandardCharsets.UTF_8);
        FileInfo fileInfo = FileDownloadUtil.buildFileInfo("anlassconfig-skripts.sql", "text", content.length);
        return FileDownloadUtil.buildFileResponse("attachment", content, fileInfo);
    }

    @GET
    @Path("/script")
    @Consumes(MediaType.WILDCARD)
    @APIResponse(content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM,
        schema = @Schema(type = SchemaType.STRING, format = "binary")), description = "OK", responseCode = "200")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @RolesAllowed(BerechtigungName.SUPPORT)
    @NonNull
    public Response downloadAllSqlSkripts() {
        String skript = anlassConfigSkriptMapper.getLiquibaseSkript();
        byte[] content = skript.getBytes(StandardCharsets.UTF_8);
            FileInfo fileInfo = FileDownloadUtil.buildFileInfo("XXXX-update-anlassconfig " + tenantProducer.produceTenant() + ".xml", "text", content.length);
        return FileDownloadUtil.buildFileResponse("attachment", content, fileInfo);
    }
}
