package ch.dvbern.oss.hemed.esc.api.features.anlass.abschluss;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.dvbstarter.shared.types.id.HasUntersuchungIDWrapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.abschluss.json.UntersuchungAbschlussJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.abschluss.json.UntersuchungAbschlussSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungMapper;
import ch.dvbern.oss.hemed.esc.api.shared.auth.AuthorizerService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.Berechtigung;
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

@Path("/anlaesse/{anlassId}/einladungen/{einladungId}/untersuchung/tab/abschluss")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_UNTERSUCHUNG_ABSCHLUSS)
@RolesAllowed(BerechtigungName.SU_ABSCHLUSS_LESEN)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed("UntersuchungAbschlussResourceMS")
public class UntersuchungAbschlussResource {

    private final UntersuchungAbschlussMapper untersuchungAbschlussMapper;
    private final EinladungMapper einladungMapper;
    private final AuthorizerService authorizerService;

    @GET
    @RolesAllowed(BerechtigungName.SU_ABSCHLUSS_LESEN)
    @Nullable
    public UntersuchungAbschlussJson getUntersuchungAbschluss(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") HasUntersuchungIDWrapper wrappedId
    ) {
        einladungMapper.requireConsistentPathParams(wrappedId, anlassId);
        return untersuchungAbschlussMapper.getByEinladung(
            wrappedId.toEinladungId(),
            untersuchung -> authorizerService.requireReadAuthorizationUntersuchung(
                untersuchung,
                Berechtigung.SU_ABSCHLUSS_LESEN));
    }

    @PUT
    @NonNull
    @RolesAllowed(BerechtigungName.SU_ABSCHLUSS_SCHREIBEN)
    public UntersuchungAbschlussJson saveUntersuchungAbschluss(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") HasUntersuchungIDWrapper wrappedId,
        @NonNull @NotNull @Valid UntersuchungAbschlussSaveJson saveJson
    ) {
        einladungMapper.requireConsistentPathParams(wrappedId, anlassId);
        return untersuchungAbschlussMapper.saveAbschluss(
            wrappedId.toEinladungId(),
            saveJson,
            untersuchung -> authorizerService.requireWriteAuthorizationUntersuchung(
                untersuchung,
                List.of(Berechtigung.SU_ABSCHLUSS_SCHREIBEN)));
    }
}
