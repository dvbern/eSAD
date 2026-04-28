package ch.dvbern.oss.hemed.esc.api.features.anlass.sprache;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.dvbstarter.shared.types.id.HasUntersuchungIDWrapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.json.UntersuchungSpracheJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.sprache.json.UntersuchungSpracheSaveJson;
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

@Path("/anlaesse/{anlassId}/einladungen/{einladungId}/untersuchung/sprache")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_UNTERSUCHUNG_SPRACHE)
@RolesAllowed(BerechtigungName.SU_AERZTLICHE_UNTERSUCHUNG_LESEN)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed("UntersuchungSpracheResourceMS")
public class UntersuchungSpracheResource {

    private final UntersuchungSpracheMapper untersuchungSpracheMapper;
    private final EinladungMapper einladungMapper;
    private final AuthorizerService authorizerService;

    @GET
    @RolesAllowed(BerechtigungName.SU_AERZTLICHE_UNTERSUCHUNG_LESEN)
    @Nullable
    public UntersuchungSpracheJson getUntersuchungSprache(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") HasUntersuchungIDWrapper wrappedId
    ) {
        einladungMapper.requireConsistentPathParams(wrappedId, anlassId);
        return untersuchungSpracheMapper.getByEinladung(
            wrappedId.toEinladungId(),
            untersuchung -> authorizerService.requireReadAuthorizationUntersuchung(
                untersuchung,
                Berechtigung.SU_AERZTLICHE_UNTERSUCHUNG_LESEN));
    }

    @PUT
    @NonNull
    @RolesAllowed(BerechtigungName.SU_AERZTLICHE_UNTERSUCHUNG_SCHREIBEN)
    public UntersuchungSpracheJson saveUntersuchungSprache(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") HasUntersuchungIDWrapper wrappedId,
        @NotNull @NonNull @Valid UntersuchungSpracheSaveJson saveJson
    ) {
        einladungMapper.requireConsistentPathParams(wrappedId, anlassId);
        return untersuchungSpracheMapper.save(
            wrappedId.toEinladungId(),
            saveJson,
            untersuchung -> authorizerService.requireWriteAuthorizationUntersuchung(
                untersuchung,
                List.of(Berechtigung.SU_AERZTLICHE_UNTERSUCHUNG_SCHREIBEN)));
    }
}
