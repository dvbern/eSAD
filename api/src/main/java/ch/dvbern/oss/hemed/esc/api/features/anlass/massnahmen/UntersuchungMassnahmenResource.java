package ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.json.UntersuchungMassnahmenJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.json.UntersuchungMassnahmenSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.json.UntersuchungSehenMassnahmenJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.massnahmen.json.UntersuchungSehenMassnahmenSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.auth.AuthorizerService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import io.smallrye.faulttolerance.api.RateLimit;
import jakarta.annotation.Nullable;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/anlaesse/{anlassId}/einladungen/{einladungId}/untersuchung/massnahmen")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_UNTERSUCHUNG_MASSNAHMEN)
@RolesAllowed(BerechtigungName.SU_MASSNAHMEN_LESEN)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed(name = "UntersuchungMassnahmenResourceMS", unit = MetricUnits.MILLISECONDS)
public class UntersuchungMassnahmenResource {

    private final UntersuchungMassnahmenMapper untersuchungMassnahmenMapper;
    private final UntersuchungSehenMassnahmenMapper untersuchungSehenMassnahmenMapper;
    private final EinladungMapper einladungMapper;
    private final AuthorizerService authorizerService;

    @GET
    @RolesAllowed(BerechtigungName.SU_MASSNAHMEN_LESEN)
    @Nullable
    public UntersuchungMassnahmenJson getUntersuchungMassnahmen(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungId
    ) {
        einladungMapper.requireConsistentPathParams(einladungId, anlassId);
        return untersuchungMassnahmenMapper.getByEinladung(einladungId);
    }

    @PUT
    @NonNull
    @RolesAllowed(BerechtigungName.SU_MASSNAHMEN_SCHREIBEN)
    public UntersuchungMassnahmenJson saveUntersuchungMassnahmen(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungId,
        @NonNull @NotNull @Valid UntersuchungMassnahmenSaveJson saveJson
    ) {
        einladungMapper.requireConsistentPathParams(einladungId, anlassId);
        authorizerService.requireWriteAuthorizationAnlass(anlassId);
        return untersuchungMassnahmenMapper.save(einladungId, saveJson);
    }

    @GET
    @Path("/sehen")
    @RolesAllowed(BerechtigungName.SU_MASSNAHMEN_LESEN)
    @Nullable
    public UntersuchungSehenMassnahmenJson getUntersuchungMassnahmenSehen(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungId
    ) {
        einladungMapper.requireConsistentPathParams(einladungId, anlassId);
        return untersuchungSehenMassnahmenMapper.getByEinladung(einladungId);
    }

    @PUT
    @Path("/sehen")
    @NonNull
    @RolesAllowed(BerechtigungName.SU_MASSNAHMEN_SCHREIBEN)
    public UntersuchungSehenMassnahmenJson saveUntersuchungMassnahmenSehen(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungId,
        @NonNull @NotNull @Valid UntersuchungSehenMassnahmenSaveJson saveJson
    ) {
        einladungMapper.requireConsistentPathParams(einladungId, anlassId);
        authorizerService.requireWriteAuthorizationAnlass(anlassId);
        return untersuchungSehenMassnahmenMapper.save(einladungId, saveJson);
    }
}
