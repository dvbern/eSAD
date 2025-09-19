package ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung;

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

@Path("/anlaesse/{anlassId}/einladungen/{einladungId}/untersuchung/arzt")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_UNTERSUCHUNG_ARZT)
@RolesAllowed(BerechtigungName.SU_AERZTLICHE_UNTERSUCHUNG_LESEN)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed(name = "UntersuchungArztResourceMS", unit = MetricUnits.MILLISECONDS)
public class UntersuchungArztResource {

    private final UntersuchungArztBeurteilungMapper untersuchungArztBeurteilungMapper;
    private final UntersuchungArztBemerkungMapper untersuchungArztBemerkungMapper;
    private final EinladungMapper einladungMapper;
    private final AuthorizerService authorizerService;

    @GET
    @Path("/beurteilung")
    @RolesAllowed(BerechtigungName.SU_AERZTLICHE_UNTERSUCHUNG_LESEN)
    @Nullable
    public UntersuchungArztBeurteilungJson getUntersuchungArztBeurteilung(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungId
    ) {
        einladungMapper.requireConsistentPathParams(einladungId, anlassId);
        return untersuchungArztBeurteilungMapper.getByEinladung(einladungId);
    }

    @GET
    @Path("/beurteilung/default")
    @RolesAllowed(BerechtigungName.SU_AERZTLICHE_UNTERSUCHUNG_LESEN)
    @Nullable
    public UntersuchungArztBeurteilungSaveJson getDefaultUntersuchungArztBeurteilungForAnlassConfig(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungId
    ) {
        einladungMapper.requireConsistentPathParams(einladungId, anlassId);
        return untersuchungArztBeurteilungMapper.getDefaultUntersuchungArztForAnlassConfig(anlassId);
    }

    @PUT
    @Path("/beurteilung")
    @NonNull
    @RolesAllowed(BerechtigungName.SU_AERZTLICHE_UNTERSUCHUNG_SCHREIBEN)
    public UntersuchungArztBeurteilungJson saveUntersuchungArztBeurteilung(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungId,
        @NonNull @NotNull @Valid UntersuchungArztBeurteilungSaveJson saveJson
    ) {
        einladungMapper.requireConsistentPathParams(einladungId, anlassId);
        authorizerService.requireWriteAuthorizationAnlass(anlassId);
        return untersuchungArztBeurteilungMapper.save(einladungId, saveJson);
    }

    @GET
    @Path("/bemerkung")
    @RolesAllowed(BerechtigungName.SU_AERZTLICHE_UNTERSUCHUNG_LESEN)
    @Nullable
    public UntersuchungArztBemerkungJson getUntersuchungArztBemerkung(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungId
    ) {
        einladungMapper.requireConsistentPathParams(einladungId, anlassId);
        return untersuchungArztBemerkungMapper.getByEinladung(einladungId);
    }

    @PUT
    @Path("/bemerkung")
    @NonNull
    @RolesAllowed(BerechtigungName.SU_AERZTLICHE_UNTERSUCHUNG_SCHREIBEN)
    public UntersuchungArztBemerkungJson saveUntersuchungArztBemerkung(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungId,
        @NonNull @NotNull @Valid UntersuchungArztBemerkungSaveJson saveJson
    ) {
        einladungMapper.requireConsistentPathParams(einladungId, anlassId);
        authorizerService.requireWriteAuthorizationAnlass(anlassId);
        return untersuchungArztBemerkungMapper.save(einladungId, saveJson);
    }
}
