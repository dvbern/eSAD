package ch.dvbern.oss.hemed.esc.api.features.organisation;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.dvbstarter.shared.types.id.ID;
import ch.dvbern.oss.hemed.esc.api.features.organisation.json.OrganisationJson;
import ch.dvbern.oss.hemed.esc.api.features.organisation.json.OrganisationListJson;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import io.smallrye.faulttolerance.api.RateLimit;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
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
    public OrganisationListJson findAllOrganisationen() {
        return new OrganisationListJson(organisationMapper.findAll());
    }

    // TODO ESC-1180: nur noch findAllOrganisationen, dieses gibt alle berechtigten zurück
    @GET
    @Path("/{organisationId}")
    @RateLimit(RateLimitConst.DEFAULT_LIMIT)
    @RolesAllowed(BerechtigungName.ORGANISATION_VERWALTEN_LESEN)
    public OrganisationJson findOrganisation(@NotNull @NonNull @PathParam("organisationId")ID<Organisation> organisationId) {
        return organisationMapper.getById(organisationId);
    }
}
