package ch.dvbern.oss.hemed.esc.api.features.anlass;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.UntersuchungSehenJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.json.UntersuchungSehenSaveJson;
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

@Path("/anlaesse/{anlassId}/einladungen/{einladungId}/untersuchung/sehen")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_UNTERSUCHUNG_SEHEN)
@RolesAllowed(BerechtigungName.SU_SEHEN_LESEN)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed(name = "UntersuchungSehenResourceMS", unit = MetricUnits.MILLISECONDS)
public class UntersuchungSehenResource {

    private final UntersuchungSehenMapper untersuchungSehenMapper;

    @GET
    @RolesAllowed(BerechtigungName.SU_SEHEN_LESEN)
    @Nullable
    public UntersuchungSehenJson getUntersuchungSehen(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungId
    ) {
        return untersuchungSehenMapper.getByEinladung(einladungId);
    }

    @GET
    @Path("/default")
    @RolesAllowed(BerechtigungName.SU_SEHEN_LESEN)
    @NotNull
    public UntersuchungSehenSaveJson getDefaultUntersuchungSehenForAnlassConfig(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungId
    ) {
        return untersuchungSehenMapper.getDefaultUntersuchungSehenForAnlassConfig(anlassId);
    }

    @PUT
    @NonNull
    @RolesAllowed(BerechtigungName.SU_SEHEN_SCHREIBEN)
    public UntersuchungSehenJson saveUntersuchungSehen(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungId,
        @NonNull @NotNull @Valid UntersuchungSehenSaveJson saveJson
    ) {
        return untersuchungSehenMapper.save(einladungId, saveJson);
    }
}
