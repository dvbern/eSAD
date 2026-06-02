package ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.dvbstarter.shared.types.id.HasUntersuchungIDWrapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.json.UntersuchungHoerenOtoskopieJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.hoeren.json.UntersuchungHoerenOtoskopieSaveJson;
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

@Path("/anlaesse/{anlassId}/einladungen/{einladungId}/untersuchung/hoeren/otoskopie")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_UNTERSUCHUNG_HOEREN_OTOSKOPIE)
@RolesAllowed(BerechtigungName.SU_HOREN_LESEN)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed("UntersuchungHoerenOtoskopieResourceMS")
public class UntersuchungHoerenOtoskopieResource {

    private final UntersuchungHoerenOtoskopieMapper untersuchungHoerenOtoskopieMapper;
    private final EinladungMapper einladungMapper;
    private final AuthorizerService authorizerService;

    @GET
    @RolesAllowed(BerechtigungName.SU_HOREN_LESEN)
    @Nullable
    public UntersuchungHoerenOtoskopieJson getUntersuchungHoerenOtoskopie(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") HasUntersuchungIDWrapper wrappedId
    ) {
        einladungMapper.requireConsistentPathParams(wrappedId, anlassId);
        return untersuchungHoerenOtoskopieMapper.getByEinladung(
            wrappedId,
            untersuchung -> authorizerService.requireReadAuthorizationUntersuchung(
                untersuchung,
                List.of(Berechtigung.SU_HOREN_LESEN)));
    }

    @GET
    @Path("/default")
    @NonNull
    @RolesAllowed(BerechtigungName.SU_HOREN_LESEN)
    public UntersuchungHoerenOtoskopieSaveJson getDefaultUntersuchungHoerenOtoskopieForAnlassConfig(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") HasUntersuchungIDWrapper wrappedId
    ) {
        einladungMapper.requireConsistentPathParams(wrappedId, anlassId);
        return untersuchungHoerenOtoskopieMapper.getDefaultUntersuchungHoerenOtoskopieForAnlassConfig(anlassId);
    }

    @PUT
    @NonNull
    @RolesAllowed(BerechtigungName.SU_HOREN_SCHREIBEN)
    public UntersuchungHoerenOtoskopieJson saveUntersuchungHoerenOtoskopie(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") HasUntersuchungIDWrapper wrappedId,
        @NonNull @NotNull @Valid UntersuchungHoerenOtoskopieSaveJson saveJson
    ) {
        einladungMapper.requireConsistentPathParams(wrappedId, anlassId);
        return untersuchungHoerenOtoskopieMapper.saveOtoskopie(
            wrappedId,
            saveJson,
            untersuchung -> authorizerService.requireWriteAuthorizationUntersuchung(
                untersuchung,
                List.of(Berechtigung.SU_HOREN_SCHREIBEN)));
    }
}
