package ch.dvbern.oss.hemed.esc.api.features.anlass.signatur;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.dvbstarter.shared.types.id.HasUntersuchungIDWrapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.signatur.json.UntersuchungSignaturJson;
import ch.dvbern.oss.hemed.esc.api.shared.auth.AuthorizerService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.Berechtigung;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import io.smallrye.faulttolerance.api.RateLimit;
import jakarta.annotation.Nullable;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import io.micrometer.core.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/anlaesse/{anlassId}/einladungen/{einladungId}/untersuchung/tab/signatur")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_UNTERSUCHUNG_SIGNATUR)
@RolesAllowed(BerechtigungName.SU_ABSCHLUSS_LESEN)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed("UntersuchungSignaturResourceMS")
public class UntersuchungSignaturResource {

    private final UntersuchungSignaturMapper untersuchungSignaturMapper;
    private final EinladungMapper einladungMapper;
    private final AuthorizerService authorizerService;

    @GET
    @RolesAllowed(BerechtigungName.SU_ABSCHLUSS_LESEN)
    @Nullable
    public UntersuchungSignaturJson getUntersuchungSignatur(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") HasUntersuchungIDWrapper wrappedId
    ) {
        einladungMapper.requireConsistentPathParams(wrappedId, anlassId);
        return untersuchungSignaturMapper.getByEinladung(
            wrappedId.toEinladungId(),
            untersuchung -> authorizerService.requireReadAuthorizationUntersuchung(
                untersuchung,
                Berechtigung.SU_ABSCHLUSS_LESEN));
    }

    @PUT
    @NonNull
    @RolesAllowed(BerechtigungName.SU_ABSCHLUSS_SCHREIBEN)
    public UntersuchungSignaturJson signieren(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") HasUntersuchungIDWrapper wrappedId
    ) {
        einladungMapper.requireConsistentPathParams(wrappedId, anlassId);
        return untersuchungSignaturMapper.signieren(
            wrappedId.toEinladungId(),
            untersuchung -> authorizerService.requireWriteAuthorizationUntersuchung(
                untersuchung,
                List.of(Berechtigung.SU_ABSCHLUSS_SCHREIBEN)));
    }

    @DELETE
    @NonNull
    @RolesAllowed(BerechtigungName.SU_ABSCHLUSS_SCHREIBEN)
    public UntersuchungSignaturJson resetSignatur(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") HasUntersuchungIDWrapper wrappedId
    ) {
        einladungMapper.requireConsistentPathParams(wrappedId, anlassId);
        return untersuchungSignaturMapper.resetSignatur(
            wrappedId.toEinladungId(),
            untersuchung -> authorizerService.requireWriteAuthorizationUntersuchung(
                untersuchung,
                List.of(Berechtigung.SU_ABSCHLUSS_SCHREIBEN)));
    }
}
