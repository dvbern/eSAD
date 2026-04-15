package ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.json.UntersuchungHoerenAudiogrammJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.json.UntersuchungHoerenAudiogrammSaveJson;
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
import io.micrometer.core.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/anlaesse/{anlassId}/einladungen/{einladungId}/untersuchung/hoeren/audiogramm")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_UNTERSUCHUNG_HOEREN_AUDIOGRAMM)
@RolesAllowed(BerechtigungName.SU_HOREN_LESEN)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed("UntersuchungHoerenAudiogrammResourceMS")
public class UntersuchungHoerenAudiogrammResource {

    private final UntersuchungHoerenAudiogrammMapper untersuchungHoerenAudiogrammMapper;
    private final EinladungMapper einladungMapper;
    private final AuthorizerService authorizerService;

    @GET
    @RolesAllowed(BerechtigungName.SU_HOREN_LESEN)
    @Nullable
    public UntersuchungHoerenAudiogrammJson getUntersuchungHoerenAudiogramm(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungId
    ) {
        einladungMapper.requireConsistentPathParams(einladungId, anlassId);
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
        einladungMapper.requireConsistentPathParams(einladungId, anlassId);
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
        einladungMapper.requireConsistentPathParams(einladungId, anlassId);
        authorizerService.requireWriteAuthorizationAnlass(anlassId);
        return untersuchungHoerenAudiogrammMapper.saveAudiogramm(einladungId, saveJson);
    }
}
