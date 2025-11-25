package ch.dvbern.oss.hemed.esc.api.features.anlass.sehen;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.Einladung;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.json.UntersuchungSehenBemerkungFragebogenJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.json.UntersuchungSehenBemerkungFragebogenSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.json.UntersuchungSehenBeurteilungJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sehen.json.UntersuchungSehenBeurteilungSaveJson;
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

@Path("/anlaesse/{anlassId}/einladungen/{einladungId}/untersuchung/sehen")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_UNTERSUCHUNG_SEHEN)
@RolesAllowed(BerechtigungName.SU_SEHEN_LESEN)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed(name = "UntersuchungSehenResourceMS", unit = MetricUnits.MILLISECONDS)
public class UntersuchungSehenResource {

    private final UntersuchungSehenBeurteilungMapper untersuchungSehenBeurteilungMapper;
    private final UntersuchungSehenBemerkungFragebogenMapper untersuchungSehenBemerkungFragebogenMapper;
    private final EinladungMapper einladungMapper;
    private final AuthorizerService authorizerService;

    @GET
    @Path("/beurteilung")
    @RolesAllowed(BerechtigungName.SU_SEHEN_LESEN)
    @Nullable
    public UntersuchungSehenBeurteilungJson getUntersuchungSehenBeurteilung(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungId
    ) {
        einladungMapper.requireConsistentPathParams(einladungId, anlassId);
        return untersuchungSehenBeurteilungMapper.getByEinladung(einladungId);
    }

    @GET
    @Path("/beurteilung/default")
    @RolesAllowed(BerechtigungName.SU_SEHEN_LESEN)
    @NotNull
    public UntersuchungSehenBeurteilungSaveJson getDefaultUntersuchungSehenBeurteilungForAnlassConfig(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungId
    ) {
        einladungMapper.requireConsistentPathParams(einladungId, anlassId);
        return untersuchungSehenBeurteilungMapper.getDefaultUntersuchungSehenForAnlassConfig(anlassId);
    }

    @PUT
    @Path("/beurteilung")
    @NonNull
    @RolesAllowed(BerechtigungName.SU_SEHEN_SCHREIBEN)
    public UntersuchungSehenBeurteilungJson saveUntersuchungSehenBeurteilung(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungId,
        @NonNull @NotNull @Valid UntersuchungSehenBeurteilungSaveJson saveJson
    ) {
        einladungMapper.requireConsistentPathParams(einladungId, anlassId);
        authorizerService.requireWriteAuthorizationAnlass(anlassId);
        return untersuchungSehenBeurteilungMapper.save(einladungId, saveJson);
    }

    @GET
    @Path("/bemerkungfragebogen")
    @RolesAllowed(BerechtigungName.SU_SEHEN_LESEN)
    @Nullable
    public UntersuchungSehenBemerkungFragebogenJson getUntersuchungSehenBemerkungFragebogen(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungId
    ) {
        einladungMapper.requireConsistentPathParams(einladungId, anlassId);
        return untersuchungSehenBemerkungFragebogenMapper.getByEinladung(einladungId);
    }

    @PUT
    @Path("/bemerkungfragebogen")
    @NonNull
    @RolesAllowed(BerechtigungName.SU_SEHEN_SCHREIBEN)
    public UntersuchungSehenBemerkungFragebogenJson saveUntersuchungSehenBemerkungFragebogen(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") ID<Einladung> einladungId,
        @NonNull @NotNull @Valid UntersuchungSehenBemerkungFragebogenSaveJson saveJson
    ) {
        einladungMapper.requireConsistentPathParams(einladungId, anlassId);
        authorizerService.requireWriteAuthorizationAnlass(anlassId);
        return untersuchungSehenBemerkungFragebogenMapper.save(einladungId, saveJson);
    }
}
