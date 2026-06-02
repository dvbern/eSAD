package ch.dvbern.oss.hemed.esc.api.features.kindanlaesse;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.kindanlaesse.json.KindAnlassListJson;
import ch.dvbern.oss.hemed.esc.api.features.kindanlaesse.json.LastImpfungJson;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.shared.auth.AuthorizerService;
import ch.dvbern.oss.hemed.esc.api.shared.auth.Berechtigung;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import io.micrometer.core.annotation.Timed;
import io.smallrye.faulttolerance.api.RateLimit;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/kindanlaesse")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_KINDANLAESSE)
@RolesAllowed({ BerechtigungName.ANLASSLISTE_LESEN, BerechtigungName.ANLASSLISTE_LESEN_LAEUSE })
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed("KindAnlaesseResourceMS")
public class KindAnlaesseResource {

    private final KindAnlaesseMapper kindAnlaesseMapper;
    private final AuthorizerService authorizerService;

    @GET
    @Path("/kind/{kindId}/anlaesse")
    @NonNull
    @RolesAllowed({ BerechtigungName.ANLASSLISTE_LESEN, BerechtigungName.ANLASSLISTE_LESEN_LAEUSE })
    public KindAnlassListJson findAnlaesseForKind(
        @PathParam("kindId") @NotNull @NonNull ID<Kind> kindId
    ) {
        return new KindAnlassListJson(kindAnlaesseMapper.findAllForKind(
            kindId,
            einladung -> authorizerService.hasReadAuthorizationEinladung(
                einladung,
                List.of(Berechtigung.ANLASSLISTE_LESEN, Berechtigung.ANLASSLISTE_LESEN_LAEUSE),
                null)));
    }

    @GET
    @Path("/kind/{kindId}/latest-impfung")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.KINDER_LESEN_MEDIZINISCH)
    public LastImpfungJson getLatestImpfung(@NotNull @NonNull @PathParam("kindId") ID<Kind> kindId) {
        return kindAnlaesseMapper.getLatestManuallyChangedImpfungByDossierId(kindId);
    }
}
