package ch.dvbern.oss.hemed.esc.api.features.anlass.impfung;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.dvbstarter.shared.types.id.HasUntersuchungIDWrapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.json.UntersuchungImpfstatusListSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.json.UntersuchungImpfungInternJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.json.UntersuchungImpfungInternSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.json.UntersuchungImpfungenDataJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.json.UntersuchungImpfungenDataMinimalJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.json.UntersuchungImpfungenDataMinimalSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.json.UntersuchungImpfungenJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.impfung.json.UntersuchungImpfungenSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.UntersuchungImpfungenMassnahmenMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.json.UntersuchungImpfungenMassnahmenJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.json.UntersuchungImpfungenMassnahmenSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.auth.AuthorizerService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.Berechtigung;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import io.micrometer.core.annotation.Timed;
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
import jakarta.ws.rs.core.Response.Status;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.Objects;

@Path("/anlaesse/{anlassId}/einladungen/{einladungId}/untersuchung/impfungen")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_UNTERSUCHUNG_IMPFUNGEN)
@RolesAllowed(BerechtigungName.SU_IMPFEN_LESEN)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed("UntersuchungImpfungenResourceMS")
public class UntersuchungImpfungenResource {

    private final UntersuchungImpfungenMapper untersuchungImpfungenMapper;
    private final UntersuchungImpfstatusKrankheitMapper untersuchungImpfstatusKrankheitMapper;
    private final UntersuchungImpfungInternMapper untersuchungImpfungInternMapper;
    private final UntersuchungImpfungenMassnahmenMapper untersuchungImpfungenMassnahmenMapper;
    private final UntersuchungImpfungenDataMinimalMapper untersuchungImpfungenDataMinimalMapper;
    private final EinladungMapper einladungMapper;
    private final AuthorizerService authorizerService;

    @Context
    private HttpServerResponse response;

    @GET
    @RolesAllowed(BerechtigungName.SU_IMPFEN_LESEN)
    @NonNull
    public UntersuchungImpfungenDataJson getUntersuchungImpfungenData(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") HasUntersuchungIDWrapper wrappedId
    ) {
        einladungMapper.requireConsistentPathParams(wrappedId, anlassId);
        return untersuchungImpfungenMapper.getByEinladung(
            wrappedId,
            untersuchung -> authorizerService.requireReadAuthorizationUntersuchung(
                untersuchung,
                List.of(Berechtigung.SU_IMPFEN_LESEN)));
    }

    @PUT
    @NonNull
    @RolesAllowed(BerechtigungName.SU_IMPFEN_SCHREIBEN)
    public UntersuchungImpfungenJson saveUntersuchungImpfungen(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") HasUntersuchungIDWrapper wrappedId,
        @NotNull @NonNull @Valid UntersuchungImpfungenSaveJson saveJson
    ) {
        einladungMapper.requireConsistentPathParams(wrappedId, anlassId);
        return untersuchungImpfungenMapper.save(
            wrappedId,
            saveJson,
            untersuchung -> authorizerService.requireWriteAuthorizationUntersuchung(
                untersuchung,
                List.of(Berechtigung.SU_IMPFEN_SCHREIBEN)));
    }

    @POST
    @Path("/visieren/visum1")
    @RolesAllowed(BerechtigungName.SU_IMPFEN_SCHREIBEN)
    public UntersuchungImpfungenJson visierenDoppelkontrolleVisum1(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungID,
        @NotNull @NonNull @Valid UntersuchungImpfungenSaveJson saveJson
    ) {
        einladungMapper.requireConsistentPathParams(einladungID, anlassId);
        return untersuchungImpfungenMapper.visierenDoppelkontrolleVisum1(
            einladungID,
            saveJson,
            untersuchung -> authorizerService.requireWriteAuthorizationUntersuchung(
                untersuchung,
                List.of(Berechtigung.SU_IMPFEN_SCHREIBEN)));
    }

    @POST
    @Path("/anfordern/visum2")
    @RolesAllowed(BerechtigungName.SU_IMPFEN_SCHREIBEN)
    public UntersuchungImpfungenJson anfordernDoppelkontrolleVisum2(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungID,
        @NotNull @NonNull @Valid UntersuchungImpfungenSaveJson saveJson
    ) {
        einladungMapper.requireConsistentPathParams(einladungID, anlassId);
        Objects.requireNonNull(saveJson.getDoppelkontrolle().getVisumPerson2());
        return untersuchungImpfungenMapper.anfordernDoppelkontrolleVisum2(
            einladungID,
            saveJson.getDoppelkontrolle().getVisumPerson2(),
            saveJson,
            untersuchung -> authorizerService.requireWriteAuthorizationUntersuchung(
                untersuchung,
                List.of(Berechtigung.SU_IMPFEN_SCHREIBEN)));
    }

    @POST
    @Path("/visieren/visum2")
    @RolesAllowed(BerechtigungName.SU_IMPFEN_SCHREIBEN)
    public UntersuchungImpfungenJson visierenDoppelkontrolleVisum2(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungID,
        @NotNull @NonNull @Valid UntersuchungImpfungenSaveJson saveJson
    ) {
        einladungMapper.requireConsistentPathParams(einladungID, anlassId);
        return untersuchungImpfungenMapper.visierenDoppelkontrolleVisum2(
            einladungID,
            saveJson,
            untersuchung -> authorizerService.requireWriteAuthorizationUntersuchung(
                untersuchung,
                List.of(Berechtigung.SU_IMPFEN_SCHREIBEN)));
    }

    @DELETE
    @Path("/visum")
    @RolesAllowed(BerechtigungName.SU_IMPFEN_SCHREIBEN)
    public UntersuchungImpfungenJson resetDoppelkontrolle(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungID,
        @NotNull @NonNull @Valid UntersuchungImpfungenSaveJson saveJson
    ) {
        einladungMapper.requireConsistentPathParams(einladungID, anlassId);
        return untersuchungImpfungenMapper.resetDoppelkontrolle(
            einladungID,
            saveJson,
            untersuchung -> authorizerService.requireWriteAuthorizationUntersuchung(
                untersuchung,
                List.of(Berechtigung.SU_IMPFEN_SCHREIBEN)));
    }

    @PUT
    @Path("/massnahmen")
    @NonNull
    @RolesAllowed(BerechtigungName.SU_IMPFEN_SCHREIBEN)
    public UntersuchungImpfungenMassnahmenJson saveUntersuchungMassnahmen(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") HasUntersuchungIDWrapper wrappedId,
        @NonNull @NotNull @Valid UntersuchungImpfungenMassnahmenSaveJson saveJson
    ) {
        einladungMapper.requireConsistentPathParams(wrappedId, anlassId);
        return untersuchungImpfungenMassnahmenMapper.save(
            wrappedId,
            saveJson,
            untersuchung -> authorizerService.requireWriteAuthorizationUntersuchung(
                untersuchung,
                List.of(Berechtigung.SU_IMPFEN_SCHREIBEN)));
    }

    @PUT
    @Path("/einzeltermin")
    @RolesAllowed(BerechtigungName.SU_IMPFEN_SCHREIBEN)
    public void einzelTerminErstellen(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungID
    ) {
        einladungMapper.requireConsistentPathParams(einladungID, anlassId);
        untersuchungImpfungenMassnahmenMapper.einzelTerminErstellen(
            einladungID,
            untersuchung -> authorizerService.requireWriteAuthorizationUntersuchung(
                untersuchung,
                List.of(Berechtigung.SU_IMPFEN_SCHREIBEN)));
    }

    @PUT
    @Path("/impfstatus")
    @NonNull
    @RolesAllowed(BerechtigungName.SU_IMPFEN_SCHREIBEN)
    public UntersuchungImpfstatusListSaveJson saveUntersuchungImpfungenImpfstatus(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") HasUntersuchungIDWrapper wrappedId,
        @NotNull @NonNull @Valid UntersuchungImpfstatusListSaveJson saveJson
    ) {
        einladungMapper.requireConsistentPathParams(wrappedId, anlassId);
        untersuchungImpfstatusKrankheitMapper.persistAll(
            wrappedId,
            saveJson,
            untersuchung -> authorizerService.requireWriteAuthorizationUntersuchung(
                untersuchung,
                List.of(Berechtigung.SU_IMPFEN_SCHREIBEN)));
        return new UntersuchungImpfstatusListSaveJson(
            untersuchungImpfstatusKrankheitMapper.getByEinladung(wrappedId));
    }

    @POST
    @Path("/impfungenintern")
    @RolesAllowed(BerechtigungName.SU_DATEN_SCHREIBEN)
    public UntersuchungImpfungInternJson createUntersuchungImpfungIntern(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") HasUntersuchungIDWrapper wrappedId,
        @NotNull @NonNull @Valid UntersuchungImpfungInternSaveJson saveJson
    ) {
        einladungMapper.requireConsistentPathParams(wrappedId, anlassId);
        response.setStatusCode(Status.CREATED.getStatusCode());
        return untersuchungImpfungInternMapper.create(
            wrappedId,
            saveJson,
            untersuchung -> authorizerService.requireWriteAuthorizationUntersuchung(
                untersuchung,
                List.of(Berechtigung.SU_DATEN_SCHREIBEN)));
    }

    @PUT
    @Path("/impfungenintern/{impfungInternId}")
    @NonNull
    @RolesAllowed(BerechtigungName.SU_DATEN_SCHREIBEN)
    public UntersuchungImpfungInternJson updateUntersuchungImpfungenIntern(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") HasUntersuchungIDWrapper wrappedId,
        @NotNull @NonNull @PathParam("impfungInternId") ID<UntersuchungImpfungIntern> impfungInternId,
        @NotNull @NonNull @Valid UntersuchungImpfungInternSaveJson saveJson
    ) {
        einladungMapper.requireConsistentPathParams(wrappedId, anlassId);
        return untersuchungImpfungInternMapper.update(
            wrappedId,
            impfungInternId,
            saveJson,
            untersuchung -> authorizerService.requireWriteAuthorizationUntersuchung(
                untersuchung,
                List.of(Berechtigung.SU_DATEN_SCHREIBEN)));
    }

    @DELETE
    @Path("/impfungenintern/{impfungInternId}")
    @RolesAllowed(BerechtigungName.SU_DATEN_SCHREIBEN)
    public void removeUntersuchungImpfungenIntern(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") HasUntersuchungIDWrapper wrappedId,
        @NotNull @NonNull @PathParam("impfungInternId") ID<UntersuchungImpfungIntern> impfungInternId
    ) {
        einladungMapper.requireConsistentPathParams(wrappedId, anlassId);
        untersuchungImpfungInternMapper.remove(
            anlassId,
            impfungInternId,
            untersuchung -> authorizerService.requireWriteAuthorizationUntersuchung(
                untersuchung,
                List.of(Berechtigung.SU_DATEN_SCHREIBEN)));
    }

    @PUT
    @Path("/dataminimal")
    @NonNull
    @RolesAllowed(BerechtigungName.SU_IMPFEN_SCHREIBEN)
    public UntersuchungImpfungenDataMinimalJson saveUntersuchungImpfungenDataMinimal(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") HasUntersuchungIDWrapper wrappedId,
        @NotNull @NonNull @Valid UntersuchungImpfungenDataMinimalSaveJson saveJson
    ) {
        einladungMapper.requireConsistentPathParams(wrappedId, anlassId);
        return untersuchungImpfungenDataMinimalMapper.save(
            wrappedId,
            saveJson,
            untersuchung -> authorizerService.requireWriteAuthorizationUntersuchung(
                untersuchung,
                List.of(Berechtigung.SU_IMPFEN_SCHREIBEN)));
    }
}
