package ch.dvbern.oss.hemed.esc.api.features.search;

import ch.dvbern.dvbstarter.infrastructure.ratelimit.RateLimitConst;
import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxResource;
import ch.dvbern.dvbstarter.shared.openapi.OpenApiConst.Tags;
import ch.dvbern.oss.hemed.esc.api.features.search.json.KindSearchListJson;
import ch.dvbern.oss.hemed.esc.api.features.search.json.SearchJson;
import ch.dvbern.oss.hemed.esc.api.features.search.json.SearchSummaryJson;
import ch.dvbern.oss.hemed.esc.api.features.search.json.SearchSummaryListJson;
import ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName;
import ch.dvbern.oss.hemed.esc.api.shared.pagination.PageResultJson;
import io.smallrye.faulttolerance.api.RateLimit;
import io.vertx.core.http.HttpServerResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import static ch.dvbern.oss.hemed.esc.api.shared.auth.BerechtigungName.KINDER_SCHREIBEN_NICHT_MEDIZINISCH;

@Path("/search")
@JaxResource
@RequiredArgsConstructor
@Tag(name = Tags.TAG_SEARCH)
@RolesAllowed({BerechtigungName.KINDER_SCHREIBEN_MEDIZINISCH, KINDER_SCHREIBEN_NICHT_MEDIZINISCH})
@RateLimit(RateLimitConst.DEFAULT_LIMIT)
@Timed(name = "SearchResourceMS", unit = MetricUnits.MILLISECONDS)
public class SearchResource {

	private final SearchMapper searchMapper;

	@Context
	private HttpServerResponse response;

	@GET
	@RateLimit(RateLimitConst.DEFAULT_LIMIT)
	@RolesAllowed({BerechtigungName.KINDER_LESEN_MEDIZINISCH, BerechtigungName.KINDER_LESEN_NICHT_MEDIZINISCH})
	@Path("summary")
	public SearchSummaryListJson findAllKinderSummary() {
		return new SearchSummaryListJson(searchMapper.findAllSummary());
	}

	@POST
	@Path("/paginated")
	@RateLimit(RateLimitConst.DEFAULT_LIMIT)
	@RolesAllowed({BerechtigungName.KINDER_LESEN_MEDIZINISCH, BerechtigungName.KINDER_LESEN_NICHT_MEDIZINISCH})
	@Schema(description = "[GET with body payload](https://opensource.admin.ch/restful-api-guidelines/#get-with-body)"
			+ " - no resources created:")
	public PageResultJson<SearchSummaryJson> findKinderPaginated(
			@NonNull @NotNull @Valid SearchJson searchRequest
	) {
		return searchMapper.findKinderPaginated(searchRequest);
	}

	@GET
	@Path("/search")
	@RateLimit(RateLimitConst.DEFAULT_LIMIT)
	@RolesAllowed({BerechtigungName.KINDER_LESEN_MEDIZINISCH, BerechtigungName.KINDER_LESEN_NICHT_MEDIZINISCH})
	public KindSearchListJson searchKinder(
			@Nullable @QueryParam("searchTerm") String searchTerm
	) {
		return searchMapper.quickSearchKinder(searchTerm);
	}
}
