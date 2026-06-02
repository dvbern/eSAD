package ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.dvbstarter.shared.types.id.HasUntersuchungIDWrapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.json.UntersuchungSchulrelevanteBeeintraechtigungJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.schulrelevantebeeintraechtigung.json.UntersuchungSchulrelevanteBeeintraechtigungSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.auth.AuthorizerService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.Berechtigung;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import io.micrometer.core.annotation.Timed;
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
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/anlaesse/{anlassId}/einladungen/{einladungId}/untersuchung/schulrelevantebeeintraechtigung")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_UNTERSUCHUNG_SCHULRELEVANTE_BEEINTRAECHTIGUNG)
@RolesAllowed(BerechtigungName.SU_AERZTLICHE_UNTERSUCHUNG_LESEN)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed("UntersuchungSchulrelevanteBeeintraechtigungResourceMS")
public class UntersuchungSchulrelevanteBeeintraechtigungResource {

    private final UntersuchungSchulrelevanteBeeintraechtigungMapper untersuchungSchulrelevanteBeeintraechtigungMapper;
    private final EinladungMapper einladungMapper;
    private final AuthorizerService authorizerService;

    @GET
    @RolesAllowed(BerechtigungName.SU_AERZTLICHE_UNTERSUCHUNG_LESEN)
    @Nullable
    public UntersuchungSchulrelevanteBeeintraechtigungJson getUntersuchungSchulrelevanteBeeintraechtigung(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") HasUntersuchungIDWrapper wrappedId
    ) {
        einladungMapper.requireConsistentPathParams(wrappedId, anlassId);
        return untersuchungSchulrelevanteBeeintraechtigungMapper.getByEinladung(
            wrappedId,
            untersuchung -> authorizerService.requireReadAuthorizationUntersuchung(
                untersuchung,
                List.of(Berechtigung.SU_AERZTLICHE_UNTERSUCHUNG_LESEN)));
    }

    @PUT
    @NonNull
    @RolesAllowed(BerechtigungName.SU_AERZTLICHE_UNTERSUCHUNG_SCHREIBEN)
    public UntersuchungSchulrelevanteBeeintraechtigungJson saveUntersuchungSchulrelevanteBeeintraechtigung(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") HasUntersuchungIDWrapper wrappedId,
        @NotNull @NonNull @Valid UntersuchungSchulrelevanteBeeintraechtigungSaveJson saveJson
    ) {
        einladungMapper.requireConsistentPathParams(wrappedId, anlassId);
        return untersuchungSchulrelevanteBeeintraechtigungMapper.save(
            wrappedId,
            saveJson,
            untersuchung -> authorizerService.requireWriteAuthorizationUntersuchung(
                untersuchung,
                List.of(Berechtigung.SU_AERZTLICHE_UNTERSUCHUNG_SCHREIBEN)));
    }
}
