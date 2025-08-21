package ch.dvbern.oss.hemed.esc.api.features.search;

import java.util.Collections;
import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxMapper;
import ch.dvbern.oss.hemed.esc.api.features.kinder.Kind;
import ch.dvbern.oss.hemed.esc.api.features.search.json.KindSearchJson;
import ch.dvbern.oss.hemed.esc.api.features.search.json.KindSearchListJson;
import ch.dvbern.oss.hemed.esc.api.features.search.json.SearchJson;
import ch.dvbern.oss.hemed.esc.api.features.search.json.SearchSummaryJson;
import ch.dvbern.oss.hemed.esc.api.shared.config.service.ConfigService;
import ch.dvbern.oss.hemed.esc.api.shared.pagination.PageResultJson;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.utils.StringUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@JaxMapper
@RequiredArgsConstructor
@Slf4j
public class SearchMapper {

	private final SearchService searchService;
	private final ConfigService configService;

	@NonNull
	public static KindSearchJson toSearchJson(@NonNull Kind kind) {
		return new KindSearchJson(
				kind.getId().getUUID(),
				kind.getEsadId(),
				kind.getSozialversicherungsNummer(),
				kind.getName(),
				kind.getVorname(),
				kind.getGeschlecht(),
				kind.getGeburtsdatum()
		);
	}

	@NonNull
	public List<SearchSummaryJson> findAllSummary() {
		return searchService.findAllSummary();
	}

	public PageResultJson<SearchSummaryJson> findKinderPaginated(@NonNull @Valid SearchJson pageRequest) {
		return searchService.findKinderPaginated(pageRequest);
	}

	@NonNull
	public KindSearchListJson quickSearchKinder(@Nullable String searchTerm) {
		if (searchTerm == null || StringUtils.isBlank(searchTerm)) {
			return new KindSearchListJson(Collections.emptyList(), false);
		}
		int maxResults = configService.getQuickSearchMaxResults();
		List<KindSearchJson> kindList =
				searchService.quickSearchKinder(searchTerm, maxResults + 1);
		// crop the list to the MAX_SEARCH_RESULTS size  and determine if there are more results
		if (kindList.size() > maxResults) {
			kindList = kindList.subList(0, maxResults);
			return new KindSearchListJson(kindList, true);
		}
		return new KindSearchListJson(kindList, false);
	}
}
