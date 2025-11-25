package ch.dvbern.oss.hemed.esc.api.features.benutzer;

import java.util.Optional;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.ensure.EnsureBenutzerService;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.json.BenutzerJson;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.json.BenutzerListJson;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.json.BenutzerOptionListJson;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.json.BenutzerSaveJson;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.json.PrincipalJson;
import ch.dvbern.oss.hemed.esc.api.features.benutzer.json.TestingResponseJson;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.CurrentUserService;
import io.smallrye.faulttolerance.api.RateLimit;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/benutzer")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_BENUTZER)
@RolesAllowed(BerechtigungName.SUPPORT)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed(name = "BenutzerResourceMS", unit = MetricUnits.MILLISECONDS)
@Slf4j
public class BenutzerResource {

    private final BenutzerMapper mapper;

    private final EnsureBenutzerService ensureBenutzerService;
    private final CurrentUserService currentUserService;

    @GET
    @Path("/error")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.SUPPORT)
    public TestingResponseJson error() {
        return mapper.mappingErrorExample();
    }

    @GET
    @NonNull
    @Path("/options")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed({ BerechtigungName.LISTEN_ADMIN_LESEN, BerechtigungName.LISTEN_LESEN })
    public BenutzerOptionListJson findAllActiveBenutzerOptions() {
        return new BenutzerOptionListJson(mapper.findAllActiveBenutzerOptions());
    }

    @GET
    @NonNull
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.MITARBEITERLISTE_BERECHTIGUNGEN_LESEN)
    public BenutzerListJson findAllBenutzer() {
        return new BenutzerListJson(mapper.findAllBenutzer());
    }

    @GET
    @Nullable
    @Path("/self")
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional(TxType.NEVER)
    public PrincipalJson getMyBenutzerData() {
        ensureBenutzerService.ensureBenutzer();
        Optional<Benutzer> optionalBenutzer = currentUserService.findCurrentUser();
        return optionalBenutzer.map(BenutzerMapper::toPrincipalJson).orElseThrow();
    }

    @PUT
    @NonNull
    @Path("/{benutzerID}")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.LISTEN_SCHREIBEN)
    public BenutzerJson update(
        @NonNull @NotNull @PathParam("benutzerID") ID<Benutzer> benutzerID,
        @NonNull @NotNull @Valid BenutzerSaveJson saveJson
    ) {
        ID<Benutzer> currentBenutzerID = currentUserService.currentUserID();
        return mapper.update(currentBenutzerID, benutzerID, saveJson);
    }

    @PUT
    @NonNull
    @Path("/{benutzerID}/benutzeraktivstatus")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.LISTEN_SCHREIBEN)
    public BenutzerJson updateBenutzerAktivStatus(
        @NonNull @NotNull @PathParam("benutzerID") ID<Benutzer> benutzerID,
        @NonNull @NotNull @Valid BenutzerSaveJson saveJson
    ) {
        return mapper.updateBenutzerAktivStatus(benutzerID, saveJson.isAktiv());
    }
}
