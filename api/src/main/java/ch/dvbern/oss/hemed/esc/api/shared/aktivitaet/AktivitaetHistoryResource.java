package ch.dvbern.oss.hemed.esc.api.shared.aktivitaet;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.shared.aktivitaet.json.AnlassAktivitaetHistoryListJson;
import ch.dvbern.oss.hemed.esc.api.shared.aktivitaet.json.KindAktivitaetHistoryListJson;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.Benutzer;
import ch.dvbern.oss.hemed.esc.api.shared.benutzer.CurrentUserService;
import io.smallrye.faulttolerance.api.RateLimit;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import io.micrometer.core.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Slf4j
@Path("/aktivitaet-history")
@JaxResource
@RequiredArgsConstructor
@Tag(name = OpenApiConst.Tags.TAG_AKTIVITAET)
@RolesAllowed({ BerechtigungName.LISTEN_ADMIN_LESEN, BerechtigungName.LISTEN_LESEN })
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed("AktivitaetHistoryResourceMS")
public class AktivitaetHistoryResource {

    private final AktivitaetHistoryMapper aktivitaetHistoryMapper;

    private final CurrentUserService currentUserService;

    @GET
    @Path("self/kinder")
    @NonNull
    @RolesAllowed({ BerechtigungName.KINDER_LESEN_MEDIZINISCH, BerechtigungName.KINDER_LESEN_NICHT_MEDIZINISCH })
    public KindAktivitaetHistoryListJson findLetzteKinder() {
        ID<Benutzer> benutzerId = currentUserService.currentUserID();
        return new KindAktivitaetHistoryListJson(aktivitaetHistoryMapper.findLetzteKinder(benutzerId));
    }

    @GET
    @Path("self/anlaesse")
    @NonNull
    @RolesAllowed({ BerechtigungName.ANLASSLISTE_LESEN, BerechtigungName.ANLASSLISTE_LESEN_LAEUSE })
    public AnlassAktivitaetHistoryListJson findLetzteAnlaesse() {
        ID<Benutzer> benutzerId = currentUserService.currentUserID();
        return new AnlassAktivitaetHistoryListJson(aktivitaetHistoryMapper.findLetzteAnlaesse(benutzerId));
    }
}
