package ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.dvbstarter.shared.types.id.HasUntersuchungIDWrapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.json.UntersuchungArztBemerkungJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.json.UntersuchungArztBemerkungSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.json.UntersuchungArztBeurteilungJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.aerztlicheuntersuchung.json.UntersuchungArztBeurteilungSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungMapper;
import ch.dvbern.oss.hemed.esc.api.shared.auth.AuthorizerService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.Berechtigung;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import io.micrometer.core.annotation.Timed;
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
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/anlaesse/{anlassId}/einladungen/{einladungId}/untersuchung/arzt")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_UNTERSUCHUNG_ARZT)
@RolesAllowed(BerechtigungName.SU_AERZTLICHE_UNTERSUCHUNG_LESEN)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed("UntersuchungArztResourceMS")
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
        @NotNull @NonNull @PathParam("einladungId") HasUntersuchungIDWrapper wrappedId
    ) {
        einladungMapper.requireConsistentPathParams(wrappedId, anlassId);
        return untersuchungArztBeurteilungMapper.getByEinladung(
            wrappedId,
            untersuchung -> authorizerService.requireReadAuthorizationUntersuchung(
                untersuchung,
                List.of(Berechtigung.SU_AERZTLICHE_UNTERSUCHUNG_LESEN)));
    }

    @GET
    @Path("/beurteilung/default")
    @RolesAllowed(BerechtigungName.SU_AERZTLICHE_UNTERSUCHUNG_LESEN)
    @Nullable
    public UntersuchungArztBeurteilungSaveJson getDefaultUntersuchungArztBeurteilungForAnlassConfig(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") HasUntersuchungIDWrapper wrappedId
    ) {
        einladungMapper.requireConsistentPathParams(wrappedId, anlassId);
        return untersuchungArztBeurteilungMapper.getDefaultUntersuchungArztForAnlassConfig(anlassId);
    }

    @PUT
    @Path("/beurteilung")
    @NonNull
    @RolesAllowed(BerechtigungName.SU_AERZTLICHE_UNTERSUCHUNG_SCHREIBEN)
    public UntersuchungArztBeurteilungJson saveUntersuchungArztBeurteilung(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") HasUntersuchungIDWrapper wrappedId,
        @NonNull @NotNull @Valid UntersuchungArztBeurteilungSaveJson saveJson
    ) {
        einladungMapper.requireConsistentPathParams(wrappedId, anlassId);
        return untersuchungArztBeurteilungMapper.save(
            wrappedId,
            saveJson,
            untersuchung -> authorizerService.requireWriteAuthorizationUntersuchung(
                untersuchung,
                List.of(Berechtigung.SU_AERZTLICHE_UNTERSUCHUNG_SCHREIBEN)));
    }

    @GET
    @Path("/bemerkung")
    @RolesAllowed(BerechtigungName.SU_AERZTLICHE_UNTERSUCHUNG_LESEN)
    @Nullable
    public UntersuchungArztBemerkungJson getUntersuchungArztBemerkung(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") HasUntersuchungIDWrapper wrappedId
    ) {
        einladungMapper.requireConsistentPathParams(wrappedId, anlassId);
        return untersuchungArztBemerkungMapper.getByEinladung(
            wrappedId,
            untersuchung -> authorizerService.requireReadAuthorizationUntersuchung(
                untersuchung,
                List.of(Berechtigung.SU_AERZTLICHE_UNTERSUCHUNG_LESEN)));
    }

    @PUT
    @Path("/bemerkung")
    @NonNull
    @RolesAllowed(BerechtigungName.SU_AERZTLICHE_UNTERSUCHUNG_SCHREIBEN)
    public UntersuchungArztBemerkungJson saveUntersuchungArztBemerkung(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") HasUntersuchungIDWrapper wrappedId,
        @NonNull @NotNull @Valid UntersuchungArztBemerkungSaveJson saveJson
    ) {
        einladungMapper.requireConsistentPathParams(wrappedId, anlassId);
        return untersuchungArztBemerkungMapper.save(
            wrappedId,
            saveJson,
            untersuchung -> authorizerService.requireWriteAuthorizationUntersuchung(
                untersuchung,
                List.of(Berechtigung.SU_AERZTLICHE_UNTERSUCHUNG_SCHREIBEN)));
    }
}
