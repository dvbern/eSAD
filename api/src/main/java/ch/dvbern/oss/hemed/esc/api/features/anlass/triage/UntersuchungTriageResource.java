package ch.dvbern.oss.hemed.esc.api.features.anlass.triage;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.dvbstarter.shared.types.id.HasUntersuchungIDWrapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.triage.json.UntersuchungTriageJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.triage.json.UntersuchungTriageSaveJson;
import ch.dvbern.oss.hemed.esc.api.shared.auth.AuthorizerService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.Berechtigung;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
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
import io.micrometer.core.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/anlaesse/{anlassId}/einladungen/{einladungId}/untersuchung/triage")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_UNTERSUCHUNG_TRIAGE)
@RolesAllowed(BerechtigungName.SU_TRIAGE_LESEN)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed("UntersuchungTriageResourceMS")
public class UntersuchungTriageResource {

    private final UntersuchungTriageMapper untersuchungTriageMapper;
    private final EinladungMapper einladungMapper;
    private final AuthorizerService authorizerService;

    @GET
    @RolesAllowed(BerechtigungName.SU_TRIAGE_LESEN)
    @Nullable
    public UntersuchungTriageJson getUntersuchungTriage(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") HasUntersuchungIDWrapper wrappedId
    ) {
        einladungMapper.requireConsistentPathParams(wrappedId, anlassId);
        return untersuchungTriageMapper.getByEinladung(
            wrappedId.toEinladungId(),
            untersuchung -> authorizerService.requireReadAuthorizationUntersuchung(
                untersuchung,
                Berechtigung.SU_TRIAGE_LESEN));
    }

    @PUT
    @NonNull
    @RolesAllowed(BerechtigungName.SU_TRIAGE_SCHREIBEN)
    public UntersuchungTriageJson saveUntersuchungTriage(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") HasUntersuchungIDWrapper wrappedId,
        @NotNull @NonNull @Valid UntersuchungTriageSaveJson saveJson
    ) {
        einladungMapper.requireConsistentPathParams(wrappedId, anlassId);
        return untersuchungTriageMapper.save(
            wrappedId.toEinladungId(),
            saveJson,
            untersuchung -> authorizerService.requireWriteAuthorizationUntersuchung(
                untersuchung,
                List.of(Berechtigung.SU_TRIAGE_SCHREIBEN)));
    }
}
