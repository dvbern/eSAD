package ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.dvbstarter.shared.types.id.HasUntersuchungIDWrapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen.json.UntersuchungVorabklaerungenJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.vorabklaerungen.json.UntersuchungVorabklaerungenSaveJson;
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

@Path("/anlaesse/{anlassId}/einladungen/{einladungId}/untersuchung/tab/vorabklaerungen")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_UNTERSUCHUNG_VORABKLAERUNGEN)
@RolesAllowed(BerechtigungName.SU_VORABKLAERUNGEN_LESEN)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed("UntersuchungVorabklaerungenResourceMS")
public class UntersuchungVorabklaerungenResource {

    private final UntersuchungVorabklaerungenMapper untersuchungVorabklaerungenMapper;
    private final EinladungMapper einladungMapper;
    private final AuthorizerService authorizerService;

    @GET
    @RolesAllowed(BerechtigungName.SU_VORABKLAERUNGEN_LESEN)
    @Nullable
    public UntersuchungVorabklaerungenJson getUntersuchungVorabklaerungen(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") HasUntersuchungIDWrapper wrappedId
    ) {
        einladungMapper.requireConsistentPathParams(wrappedId, anlassId);
        return untersuchungVorabklaerungenMapper.getByEinladung(
            wrappedId,
            untersuchung -> authorizerService.requireReadAuthorizationUntersuchung(
                untersuchung,
                List.of(Berechtigung.SU_VORABKLAERUNGEN_LESEN)));
    }

    @PUT
    @NonNull
    @RolesAllowed(BerechtigungName.SU_VORABKLAERUNGEN_SCHREIBEN)
    public UntersuchungVorabklaerungenJson saveUntersuchungVorabklaerungen(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") HasUntersuchungIDWrapper wrappedId,
        @NonNull @NotNull @Valid UntersuchungVorabklaerungenSaveJson saveJson
    ) {
        einladungMapper.requireConsistentPathParams(wrappedId, anlassId);
        return untersuchungVorabklaerungenMapper.save(
            wrappedId,
            saveJson,
            untersuchung -> authorizerService.requireWriteAuthorizationUntersuchung(
                untersuchung,
                List.of(Berechtigung.SU_VORABKLAERUNGEN_SCHREIBEN)));
    }
}
