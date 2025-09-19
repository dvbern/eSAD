package ch.dvbern.oss.hemed.esc.api.features.anlass.groessegewicht;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.EinladungMapper;
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

@Path("/anlaesse/{anlassId}/einladungen/{einladungId}/untersuchung/groessegewicht")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_UNTERSUCHUNG_GROESSEGEWICHT)
@RolesAllowed(BerechtigungName.SU_GROSSE_GEWICHT_LESEN)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed(name = "UntersuchungGroesseGewichtResourceMS", unit = MetricUnits.MILLISECONDS)
public class UntersuchungGroesseGewichtResource {

    private final UntersuchungGroesseGewichtMapper untersuchungGroesseGewichtMapper;
    private final EinladungMapper einladungMapper;
    private final AuthorizerService authorizerService;

    @GET
    @RolesAllowed(BerechtigungName.SU_GROSSE_GEWICHT_LESEN)
    @Nullable
    public UntersuchungGroesseGewichtDataJson getUntersuchungGroesseGewicht(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungId
    ) {
        einladungMapper.requireConsistentPathParams(einladungId, anlassId);
        return untersuchungGroesseGewichtMapper.getUntersuchungGroesseGewicht(einladungId);
    }

    @PUT
    @NonNull
    @RolesAllowed(BerechtigungName.SU_GROSSE_GEWICHT_SCHREIBEN)
    public UntersuchungGroesseGewichtDataJson saveUntersuchungGroesseGewicht(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungId,
        @NonNull @NotNull @Valid UntersuchungGroesseGewichtSaveJson saveJson
    ) {
        einladungMapper.requireConsistentPathParams(einladungId, anlassId);
        authorizerService.requireWriteAuthorizationAnlass(anlassId);
        return untersuchungGroesseGewichtMapper.saveUntersuchungGroesseGewicht(einladungId, saveJson);
    }
}
