package ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Einladung;
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

@Path("/anlaesse/{anlassId}/einladungen/{einladungId}/untersuchung/hoeren/audiogramm")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_UNTERSUCHUNG_HOEREN_AUDIOGRAMM)
@RolesAllowed(BerechtigungName.SU_HOREN_LESEN)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed(name = "UntersuchungHoerenAudiogrammResourceMS", unit = MetricUnits.MILLISECONDS)
public class UntersuchungHoerenAudiogrammResource {

    private final UntersuchungHoerenAudiogrammMapper untersuchungHoerenAudiogrammMapper;

    @GET
    @RolesAllowed(BerechtigungName.SU_HOREN_LESEN)
    @Nullable
    public UntersuchungHoerenAudiogrammJson getUntersuchungHoerenAudiogramm(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungId
    ) {
        return untersuchungHoerenAudiogrammMapper.getByEinladung(einladungId);
    }

    @GET
    @Path("/default")
    @NonNull
    @RolesAllowed(BerechtigungName.SU_HOREN_LESEN)
    public UntersuchungHoerenAudiogrammSaveJson getDefaultUntersuchungHoerenAudiogrammForAnlassConfig(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungId
    ) {
        return untersuchungHoerenAudiogrammMapper.getDefaultUntersuchungHoerenAudiogrammForAnlassConfig(anlassId);
    }

    @PUT
    @NonNull
    @RolesAllowed(BerechtigungName.SU_HOREN_SCHREIBEN)
    public UntersuchungHoerenAudiogrammJson saveUntersuchungHoerenAudiogramm(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungId,
        @NonNull @NotNull @Valid UntersuchungHoerenAudiogrammSaveJson saveJson
    ) {
        return untersuchungHoerenAudiogrammMapper.saveAudiogramm(einladungId, saveJson);
    }
}
