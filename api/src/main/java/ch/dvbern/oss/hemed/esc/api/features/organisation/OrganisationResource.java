package ch.dvbern.oss.hemed.esc.api.features.organisation;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.oss.hemed.esc.api.features.organisation.json.OrganisationJson;
import ch.dvbern.oss.hemed.esc.api.features.organisation.json.OrganisationListJson;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import io.smallrye.faulttolerance.api.RateLimit;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Path("/organisation")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_ORGANISATION)
@RolesAllowed(BerechtigungName.ORGANISATION_VERWALTEN_LESEN)
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
public class OrganisationResource {

    private final OrganisationMapper organisationMapper;

    @GET
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.ORGANISATION_VERWALTEN_LESEN)
    public OrganisationListJson findOrganisationenBerechtigt() {
        List<OrganisationJson> organisationenBerechtigt = organisationMapper.findOrganisationenBerechtigt()
            .stream()
            .toList();
        return new OrganisationListJson(organisationenBerechtigt);
    }
}
