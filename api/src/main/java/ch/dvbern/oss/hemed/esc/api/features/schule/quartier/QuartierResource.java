package ch.dvbern.oss.hemed.esc.api.features.schule.quartier;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.oss.hemed.esc.api.features.schule.quartier.json.QuartierListJson;
import ch.dvbern.oss.hemed.esc.api.features.schule.quartier.json.QuartierOptionListJson;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import io.smallrye.faulttolerance.api.RateLimit;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;
import io.micrometer.core.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/quartiere")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_QUARTIERE)
@RolesAllowed(BerechtigungName.LISTEN_SCHREIBEN)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed("QuartierResourceMS")
public class QuartierResource {

    private final QuartierMapper quartierMapper;

    @GET
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed({ BerechtigungName.LISTEN_ADMIN_LESEN, BerechtigungName.LISTEN_LESEN })
    public QuartierListJson findAllQuartiere() {
        return new QuartierListJson(quartierMapper.findAll());
    }

    @GET
    @Path("/options")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed({ BerechtigungName.LISTEN_ADMIN_LESEN, BerechtigungName.LISTEN_LESEN })
    public QuartierOptionListJson findAllQuartierOptions() {
        return new QuartierOptionListJson(quartierMapper.findAllOptions());
    }

}
