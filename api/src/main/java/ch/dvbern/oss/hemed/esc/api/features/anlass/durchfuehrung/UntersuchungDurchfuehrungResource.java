package ch.dvbern.oss.hemed.esc.api.features.anlass.durchfuehrung;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.dvbstarter.shared.types.id.HasUntersuchungIDWrapper;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.anlass.Anlass;
import ch.dvbern.oss.hemed.esc.api.features.anlass.AnlassService;
import ch.dvbern.oss.hemed.esc.api.features.anlass.einladung.EinladungMapper;
import ch.dvbern.oss.hemed.esc.api.features.anlass.durchfuehrung.json.UntersuchungDurchfuehrungDataJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.durchfuehrung.json.UntersuchungDurchfuehrungDataSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.durchfuehrung.json.UntersuchungDurchfuehrungJson;
import ch.dvbern.oss.hemed.esc.api.features.anlass.durchfuehrung.json.UntersuchungDurchfuehrungSaveJson;
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

@Path("/anlaesse/{anlassId}/einladungen/{einladungId}/untersuchung/durchfuehrung")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_UNTERSUCHUNG_DURCHFUEHRUNG)
@RolesAllowed(BerechtigungName.SU_DATEN_LESEN)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed("UntersuchungDurchfuehrungResourceMS")
public class UntersuchungDurchfuehrungResource {

    private final UntersuchungDurchfuehrungMapper untersuchungDurchfuehrungMapper;
    private final AuthorizerService authorizerService;
    private final AnlassService anlassService;
    private final EinladungMapper einladungMapper;

    @GET
    @RolesAllowed(BerechtigungName.SU_DATEN_LESEN)
    @Nullable
    public UntersuchungDurchfuehrungDataJson getUntersuchungDurchfuehrung(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") HasUntersuchungIDWrapper wrappedId
    ) {
        einladungMapper.requireConsistentPathParams(wrappedId, anlassId);
        return untersuchungDurchfuehrungMapper.getByEinladung(
            wrappedId.toEinladungId(),
            untersuchung -> authorizerService.requireReadAuthorizationUntersuchung(
                untersuchung,
                List.of(Berechtigung.SU_DATEN_LESEN)));
    }

    @PUT
    @NonNull
    @RolesAllowed(BerechtigungName.SU_DATEN_SCHREIBEN)
    public UntersuchungDurchfuehrungJson saveUntersuchungDurchfuehrung(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") HasUntersuchungIDWrapper wrappedId,
        @NotNull @NonNull @Valid UntersuchungDurchfuehrungSaveJson saveJson
    ) {
        einladungMapper.requireConsistentPathParams(wrappedId, anlassId);
        return untersuchungDurchfuehrungMapper.save(
            wrappedId.toEinladungId(),
            saveJson,
            untersuchung -> authorizerService.requireWriteAuthorizationUntersuchung(
                untersuchung,
                List.of(Berechtigung.SU_DATEN_SCHREIBEN)));
    }

    @PUT
    @NonNull
    @Path("/save-abgeschlossen")
    @RolesAllowed(BerechtigungName.SU_DATEN_SCHREIBEN)
    public UntersuchungDurchfuehrungDataJson saveUntersuchungDurchfuehrungAbgeschlossen(
        @NotNull @NonNull @PathParam("anlassId") ID<Anlass> anlassId,
        @NotNull @NonNull @PathParam("einladungId") HasUntersuchungIDWrapper wrappedId,
        @NotNull @NonNull @Valid UntersuchungDurchfuehrungDataSaveJson saveJson
    ) {
        einladungMapper.requireConsistentPathParams(wrappedId, anlassId);
        anlassService.requireAnlassAbgeschlossen(anlassId);

        return untersuchungDurchfuehrungMapper.saveUntersuchungDurchfuehrungAbgeschlossen(
            wrappedId.toEinladungId(),
            saveJson,
            untersuchung -> authorizerService.requireWriteAuthorizationUntersuchung(
                untersuchung,
                List.of(Berechtigung.SU_DATEN_SCHREIBEN)));
    }
}
