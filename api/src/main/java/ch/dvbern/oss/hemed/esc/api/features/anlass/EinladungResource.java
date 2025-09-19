package ch.dvbern.oss.hemed.esc.api.features.anlass;

import java.util.List;
import java.util.Objects;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.EinladungJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.EinladungListJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.EinladungSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.UntersuchungKindDatenJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.UntersuchungTabInfoJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.UntersuchungTabInfoListJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.UntersuchungTabsSavedJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.json.KindIdListJson;
import ch.dvbern.oss.hemed.esc.api.features.notizen.json.NotizJson;
import ch.dvbern.oss.hemed.esc.api.features.notizen.json.NotizSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.auth.AuthorizerService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
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
import org.apache.commons.lang3.stream.Streams;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@Path("/anlaesse/{anlassId}/einladungen")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_EINLADUNGEN)
@RolesAllowed(BerechtigungName.ANLASSLISTE_LESEN)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed(name = "EinladungResourceMS", unit = MetricUnits.MILLISECONDS)
public class EinladungResource {

    private final EinladungMapper einladungMapper;
    private final UntersuchungKindDatenMapper untersuchungKindDatenMapper;
    private final AuthorizerService authorizerService;

    @Context
    private HttpServerResponse response;

    @POST
    @NonNull
    @RolesAllowed(BerechtigungName.ANLASSPLANUNG_SCHREIBEN)
    public EinladungListJson createEinladungenForAnlass(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @Valid KindIdListJson kindIdListJson
    ) {
        authorizerService.requireWriteAuthorizationAnlass(anlassId);
        return new EinladungListJson(einladungMapper.createEinladungenForAnlass(anlassId, kindIdListJson));
    }

    @DELETE
    @NonNull
    @RolesAllowed(BerechtigungName.ANLASSPLANUNG_SCHREIBEN)
    public Response removeEinladungenForAnlass(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @Valid EinladungListJson einladungListJson
    ) {
        List<ID<Einladung>> einladungenId =
            einladungListJson.getEinladungList().stream().map(EinladungJson::getId).toList();
        authorizerService.requireWriteAuthorizationAnlass(anlassId);
        einladungMapper.requireConsistentPathParams(einladungenId, anlassId);

        einladungMapper.removeEinladungen(einladungListJson);
        return Response.ok().build();
    }

    @GET
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.ANLASSPLANUNG_LESEN)
    public EinladungListJson findEinladungenByAnlass(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId
    ) {
        return new EinladungListJson(einladungMapper.findEinladungenByAnlass(anlassId));
    }

    @GET
    @Path("/{einladungId}")
    @NonNull
    @RolesAllowed(BerechtigungName.ANLASSPLANUNG_LESEN)
    public EinladungJson getEinladungById(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungId
    ) {
        einladungMapper.requireConsistentPathParams(einladungId, anlassId);
        return einladungMapper.getById(einladungId);
    }

    @GET
    @Path("/{einladungId}/tabssaved")
    @NonNull
    @RolesAllowed(BerechtigungName.ANLASSPLANUNG_LESEN)
    public UntersuchungTabsSavedJson getUntersuchungTabsSavedByEinladungId(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungId
    ) {
        einladungMapper.requireConsistentPathParams(einladungId, anlassId);
        return einladungMapper.getTabsSavedByEinladungId(einladungId);
    }

    @PUT
    @Path("/{einladungId}")
    @NonNull
    @RolesAllowed(BerechtigungName.ANLASSPLANUNG_SCHREIBEN)
    public EinladungJson updateEinladung(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungId,
        @NotNull @NonNull @Valid EinladungSaveJson einladungSaveJson
    ) {
        einladungMapper.requireConsistentPathParams(einladungId, anlassId);
        authorizerService.requireWriteAuthorizationAnlass(anlassId);
        return einladungMapper.updateEinladung(einladungId, einladungSaveJson);
    }

    @POST
    @Path("/{einladungId}/impfausweis")
    @NonNull
    @RolesAllowed(BerechtigungName.KINDER_ALLE_NOTIZEN_SCHREIBEN)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public NotizJson createImpfausweisNotiz(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungId,
        @Nullable @RestForm("file") final FileUpload fileUpload,
        @Nullable @RestForm("file2") final FileUpload fileUpload2,
        @Nullable @RestForm("file3") final FileUpload fileUpload3,
        @Nullable @RestForm("file4") final FileUpload fileUpload4,
        @NotNull @NonNull @Valid @RestForm("notizCreateJson") @PartType(MediaType.APPLICATION_JSON)
        final NotizSaveJson createJson
    ) {
        einladungMapper.requireConsistentPathParams(einladungId, anlassId);
        authorizerService.requireWriteAuthorizationAnlass(anlassId);
        response.setStatusCode(Status.CREATED.getStatusCode());
        List<FileUpload> fileUploads = Streams.of(fileUpload, fileUpload2, fileUpload3, fileUpload4)
            .filter(Objects::nonNull)
            .toList();
        return einladungMapper.createImpfausweisNotiz(einladungId, createJson, fileUploads);
    }

    @POST
    @Path("/{einladungId}/fragebogen")
    @NonNull
    @RolesAllowed(BerechtigungName.KINDER_ALLE_NOTIZEN_SCHREIBEN)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public NotizJson createFragebogenNotiz(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungId,
        @Nullable @RestForm("file") final FileUpload fileUpload,
        @Nullable @RestForm("file2") final FileUpload fileUpload2,
        @Nullable @RestForm("file3") final FileUpload fileUpload3,
        @Nullable @RestForm("file4") final FileUpload fileUpload4,
        @NotNull @NonNull @Valid @RestForm("notizCreateJson") @PartType(MediaType.APPLICATION_JSON)
        final NotizSaveJson createJson
    ) {
        einladungMapper.requireConsistentPathParams(einladungId, anlassId);
        authorizerService.requireWriteAuthorizationAnlass(anlassId);
        response.setStatusCode(Status.CREATED.getStatusCode());
        List<FileUpload> fileUploads = Streams.of(fileUpload, fileUpload2, fileUpload3, fileUpload4)
            .filter(Objects::nonNull)
            .toList();
        return einladungMapper.createFragebogenNotiz(einladungId, createJson, fileUploads);
    }

    @GET
    @Path("/tabinfo")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.KINDER_LESEN)
    public UntersuchungTabInfoListJson getUntersuchungTabInfos(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @QueryParam("einladungIds") List<ID<Einladung>> einladungIds
    ) {
        einladungMapper.requireConsistentPathParams(einladungIds, anlassId);
        List<UntersuchungTabInfoJson> untersuchungTabInfos = einladungMapper.getUntersuchungTabInfos(einladungIds);
        return new UntersuchungTabInfoListJson(untersuchungTabInfos);
    }

    @GET
    @Path("/hiddentabinfo")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.KINDER_LESEN)
    public UntersuchungTabInfoListJson getHiddenUntersuchungTabInfos(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @QueryParam("einladungIds") List<ID<Einladung>> einladungIds
    ) {
        einladungMapper.requireConsistentPathParams(einladungIds, anlassId);
        List<UntersuchungTabInfoJson> untersuchungTabInfos =
            einladungMapper.getHiddenUntersuchungTabInfos(anlassId, einladungIds);
        return new UntersuchungTabInfoListJson(untersuchungTabInfos);
    }

    @GET
    @Valid
    @Path("/{einladungId}/untersuchung/kinddaten")
    @RolesAllowed(BerechtigungName.SU_DATEN_LESEN)
    public UntersuchungKindDatenJson getUntersuchungKindDaten(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungId
    ) {
        einladungMapper.requireConsistentPathParams(einladungId, anlassId);
        return untersuchungKindDatenMapper.getByEinladung(einladungId);
    }

    @PUT
    @Path("/{einladungId}/untersuchung/abschluss")
    @NonNull
    @RolesAllowed(BerechtigungName.ANLASSPLANUNG_SCHREIBEN)
    public Response untersuchungAbschliessen(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungId
    ) {
        einladungMapper.requireConsistentPathParams(einladungId, anlassId);
        authorizerService.requireWriteAuthorizationAnlass(anlassId);
        einladungMapper.untersuchungAbschliessen(einladungId);
        return Response.ok().build();
    }
}
