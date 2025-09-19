package ch.dvbern.oss.hemed.esc.api.features.anlass;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.UntersuchungDurchfuehrungDataJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.UntersuchungDurchfuehrungDataSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.UntersuchungDurchfuehrungJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.UntersuchungDurchfuehrungSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.auth.AuthorizerService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import io.smallrye.faulttolerance.api.RateLimit;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/anlaesse/{anlassId}/einladungen/{einladungId}/untersuchung/durchfuehrung")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_UNTERSUCHUNG_DURCHFUEHRUNG)
@RolesAllowed(BerechtigungName.SU_DATEN_LESEN)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed(name = "UntersuchungDurchfuehrungResourceMS", unit = MetricUnits.MILLISECONDS)
public class UntersuchungDurchfuehrungResource {

    private final UntersuchungDurchfuehrungMapper untersuchungDurchfuehrungMapper;
    private final AuthorizerService authorizerService;
    private final AnlassService anlassService;
    private final EinladungMapper einladungMapper;

    @GET
    @RolesAllowed(BerechtigungName.SU_DATEN_LESEN)
    @Nullable
    public UntersuchungDurchfuehrungDataJson getUntersuchungDurchfuehrung(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungId
    ) {
        einladungMapper.requireConsistentPathParams(einladungId, anlassId);
        return untersuchungDurchfuehrungMapper.getByEinladung(einladungId);
    }

    @PUT
    @NonNull
    @RolesAllowed(BerechtigungName.SU_DATEN_SCHREIBEN)
    public UntersuchungDurchfuehrungJson saveUntersuchungDurchfuehrung(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungId,
        @NotNull @NonNull @Valid UntersuchungDurchfuehrungSaveJson saveJson
    ) {
        einladungMapper.requireConsistentPathParams(einladungId, anlassId);
        authorizerService.requireWriteAuthorizationAnlass(anlassId);
        return untersuchungDurchfuehrungMapper.save(einladungId, saveJson);
    }

    @PUT
    @NonNull
    @Path("/save-abgeschlossen")
    @RolesAllowed(BerechtigungName.SU_DATEN_SCHREIBEN)
    public UntersuchungDurchfuehrungDataJson saveUntersuchungDurchfuehrungAbgeschlossen(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungId,
        @NotNull @NonNull @Valid UntersuchungDurchfuehrungDataSaveJson saveJson
    ) {
        einladungMapper.requireConsistentPathParams(einladungId, anlassId);
        authorizerService.requireWriteAuthorizationAnlass(anlassId);
        anlassService.requireAnlassAbgeschlossen(anlassId);

        return untersuchungDurchfuehrungMapper.saveUntersuchungDurchfuehrungAbgeschlossen(einladungId, saveJson);
    }
}
