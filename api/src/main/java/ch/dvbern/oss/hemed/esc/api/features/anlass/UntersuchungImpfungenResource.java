package ch.dvbern.oss.hemed.esc.api.features.anlass;

import java.util.Objects;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.UntersuchungImpfstatusListSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.UntersuchungImpfungInternJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.UntersuchungImpfungInternSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.UntersuchungImpfungenDataJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.UntersuchungImpfungenJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.UntersuchungImpfungenSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungImpfungenMassnahmenJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungImpfungenMassnahmenMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungImpfungenMassnahmenSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.auth.AuthorizerService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import io.smallrye.faulttolerance.api.RateLimit;
import io.vertx.core.http.HttpServerResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/anlaesse/{anlassId}/einladungen/{einladungId}/untersuchung/impfungen")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_UNTERSUCHUNG_IMPFUNGEN)
@RolesAllowed(BerechtigungName.SU_IMPFEN_LESEN)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed(name = "UntersuchungImpfungenResourceMS", unit = MetricUnits.MILLISECONDS)
public class UntersuchungImpfungenResource {

    private final UntersuchungImpfungenMapper untersuchungImpfungenMapper;
    private final UntersuchungImpfstatusKrankheitMapper untersuchungImpfstatusKrankheitMapper;
    private final UntersuchungImpfungInternMapper untersuchungImpfungInternMapper;
    private final UntersuchungImpfungenMassnahmenMapper untersuchungImpfungenMassnahmenMapper;
    private final EinladungMapper einladungMapper;
    private final AuthorizerService authorizerService;

    @Context
    private HttpServerResponse response;

    @GET
    @RolesAllowed(BerechtigungName.SU_IMPFEN_LESEN)
    @NonNull
    public UntersuchungImpfungenDataJson getUntersuchungImpfungenData(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungId
    ) {
        einladungMapper.requireConsistentPathParams(einladungId, anlassId);
        return untersuchungImpfungenMapper.getByEinladung(einladungId);
    }

    @PUT
    @NonNull
    @RolesAllowed(BerechtigungName.SU_IMPFEN_SCHREIBEN)
    public UntersuchungImpfungenJson saveUntersuchungImpfungen(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungId,
        @NotNull @NonNull @Valid UntersuchungImpfungenSaveJson saveJson
    ) {
        einladungMapper.requireConsistentPathParams(einladungId, anlassId);
        authorizerService.requireWriteAuthorizationAnlass(anlassId);
        return untersuchungImpfungenMapper.save(einladungId, saveJson);
    }

    @POST
    @Path("/visieren/visum1")
    @RolesAllowed(BerechtigungName.SU_IMPFEN_SCHREIBEN)
    public UntersuchungImpfungenJson visierenDoppelkontrolleVisum1(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungId,
        @NotNull @NonNull @Valid UntersuchungImpfungenSaveJson saveJson
    ) {
        einladungMapper.requireConsistentPathParams(einladungId, anlassId);
        authorizerService.requireWriteAuthorizationAnlass(anlassId);
        return untersuchungImpfungenMapper.visierenDoppelkontrolleVisum1(einladungId, saveJson);
    }

    @POST
    @Path("/anfordern/visum2")
    @RolesAllowed(BerechtigungName.SU_IMPFEN_SCHREIBEN)
    public UntersuchungImpfungenJson anfordernDoppelkontrolleVisum2(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungId,
        @NotNull @NonNull @Valid UntersuchungImpfungenSaveJson saveJson
    ) {
        einladungMapper.requireConsistentPathParams(einladungId, anlassId);
        authorizerService.requireWriteAuthorizationAnlass(anlassId);
        Objects.requireNonNull(saveJson.getDoppelkontrolle().getVisumPerson2());
        return untersuchungImpfungenMapper.anfordernDoppelkontrolleVisum2(
            einladungId, saveJson.getDoppelkontrolle().getVisumPerson2(), saveJson);
    }

    @POST
    @Path("/visieren/visum2")
    @RolesAllowed(BerechtigungName.SU_IMPFEN_SCHREIBEN)
    public UntersuchungImpfungenJson visierenDoppelkontrolleVisum2(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungId,
        @NotNull @NonNull @Valid UntersuchungImpfungenSaveJson saveJson
    ) {
        einladungMapper.requireConsistentPathParams(einladungId, anlassId);
        authorizerService.requireWriteAuthorizationAnlass(anlassId);
        return untersuchungImpfungenMapper.visierenDoppelkontrolleVisum2(einladungId, saveJson);
    }

    @DELETE
    @Path("/visum")
    @RolesAllowed(BerechtigungName.SU_IMPFEN_SCHREIBEN)
    public UntersuchungImpfungenJson resetDoppelkontrolle(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungId,
        @NotNull @NonNull @Valid UntersuchungImpfungenSaveJson saveJson
    ) {
        einladungMapper.requireConsistentPathParams(einladungId, anlassId);
        authorizerService.requireWriteAuthorizationAnlass(anlassId);
        return untersuchungImpfungenMapper.resetDoppelkontrolle(einladungId, saveJson);
    }

    @PUT
    @Path("/massnahmen")
    @NonNull
    @RolesAllowed(BerechtigungName.SU_IMPFEN_SCHREIBEN)
    public UntersuchungImpfungenMassnahmenJson saveUntersuchungMassnahmen(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungId,
        @NonNull @NotNull @Valid UntersuchungImpfungenMassnahmenSaveJson saveJson
    ) {
        einladungMapper.requireConsistentPathParams(einladungId, anlassId);
        authorizerService.requireWriteAuthorizationAnlass(anlassId);
        return untersuchungImpfungenMassnahmenMapper.save(einladungId, saveJson);
    }

    @PUT
    @Path("/einzeltermin")
    @NonNull
    @RolesAllowed(BerechtigungName.SU_IMPFEN_SCHREIBEN)
    public Response einzelTerminErstellen(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungId
    ) {
        einladungMapper.requireConsistentPathParams(einladungId, anlassId);
        authorizerService.requireWriteAuthorizationAnlass(anlassId);
        untersuchungImpfungenMassnahmenMapper.einzelTerminErstellen(einladungId);
        return Response.ok().build();
    }

    @PUT
    @Path("/impfstatus")
    @NonNull
    @RolesAllowed(BerechtigungName.SU_IMPFEN_SCHREIBEN)
    public UntersuchungImpfstatusListSaveJson saveUntersuchungImpfungenImpfstatus(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungId,
        @NotNull @NonNull @Valid UntersuchungImpfstatusListSaveJson saveJson
    ) {
        einladungMapper.requireConsistentPathParams(einladungId, anlassId);
        authorizerService.requireWriteAuthorizationAnlass(anlassId);
        untersuchungImpfstatusKrankheitMapper.persistAll(einladungId, saveJson);
        return new UntersuchungImpfstatusListSaveJson(
            untersuchungImpfstatusKrankheitMapper.getByEinladung(einladungId));
    }

    @POST
    @Path("/impfungenintern")
    @RolesAllowed(BerechtigungName.SU_DATEN_SCHREIBEN)
    public UntersuchungImpfungInternJson createUntersuchungImpfungIntern(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungId,
        @NotNull @NonNull @Valid UntersuchungImpfungInternSaveJson saveJson
    ) {
        einladungMapper.requireConsistentPathParams(einladungId, anlassId);
        authorizerService.requireWriteAuthorizationAnlass(anlassId);
        response.setStatusCode(Status.CREATED.getStatusCode());
        return untersuchungImpfungInternMapper.create(einladungId, saveJson);
    }

    @PUT
    @Path("/impfungenintern/{impfungInternId}")
    @NonNull
    @RolesAllowed(BerechtigungName.SU_DATEN_SCHREIBEN)
    public UntersuchungImpfungInternJson updateUntersuchungImpfungenIntern(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungId,
        @NotNull @NonNull @PathParam("impfungInternId") ID<UntersuchungImpfungIntern> impfungInternId,
        @NotNull @NonNull @Valid UntersuchungImpfungInternSaveJson saveJson
    ) {
        einladungMapper.requireConsistentPathParams(einladungId, anlassId);
        authorizerService.requireWriteAuthorizationAnlass(anlassId);
        return untersuchungImpfungInternMapper.update(einladungId, impfungInternId, saveJson);
    }

    @DELETE
    @Path("/impfungenintern/{impfungInternId}")
    @RolesAllowed(BerechtigungName.SU_DATEN_SCHREIBEN)
    public void removeUntersuchungImpfungenIntern(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungId,
        @NotNull @NonNull @PathParam("impfungInternId") ID<UntersuchungImpfungIntern> impfungInternId
    ) {
        einladungMapper.requireConsistentPathParams(einladungId, anlassId);
        authorizerService.requireWriteAuthorizationAnlass(anlassId);
        untersuchungImpfungInternMapper.remove(anlassId, impfungInternId);
    }
}
