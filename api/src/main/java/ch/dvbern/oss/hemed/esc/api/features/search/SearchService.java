package ch.dvbern.oss.hemed.esc.api.features.search;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.Service;
import ch.dvbern.oss.hemed.esc.api.features.search.json.KindSearchJson;
import ch.dvbern.oss.hemed.esc.api.features.search.json.SearchJson;
import ch.dvbern.oss.hemed.esc.api.features.search.json.SearchSummaryJson;
import ch.dvbern.oss.hemed.esc.api.features.search.types.KinderQuickSearchTerm;
import ch.dvbern.oss.hemed.esc.api.shared.pagination.PageResultJson;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.NonNull;

@Service
@RequiredArgsConstructor
public class SearchService {

	private final SearchRepo searchRepo;

	@NonNull
	public List<SearchSummaryJson> findAllSummary() {
		return searchRepo.findAllSummary();
	}

	@NonNull
	public PageResultJson<SearchSummaryJson> findKinderPaginated(@NonNull SearchJson pageRequest) {
		return searchRepo.findKinderPaginated(pageRequest);
	}

	@NonNull
	public List<KindSearchJson> quickSearchKinder(@NonNull String searchTerm, int maxResults) {
		var quickSearchTerm = new KinderQuickSearchTerm(searchTerm);
		return searchRepo.quickSearchKinder(quickSearchTerm, maxResults);
	}
}
