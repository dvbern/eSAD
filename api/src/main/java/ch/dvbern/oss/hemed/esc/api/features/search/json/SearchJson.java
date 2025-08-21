package ch.dvbern.oss.hemed.esc.api.features.search.json;

import java.util.List;

import ch.dvbern.dvbstarter.infrastructure.stereotypes.JaxDTO;
import ch.dvbern.oss.hemed.esc.api.features.search.types.SearchColumns;
import ch.dvbern.oss.hemed.esc.api.shared.pagination.FilterFieldJson;
import ch.dvbern.oss.hemed.esc.api.shared.pagination.PageRequestJson;
import ch.dvbern.oss.hemed.esc.api.shared.pagination.PagedSearchJson;
import ch.dvbern.oss.hemed.esc.api.shared.pagination.SortJson;
import jakarta.validation.Valid;
import lombok.EqualsAndHashCode;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * In order for the openapi generator to generate types that can handle the generic
 * we need to extend PagedSearchJson whenever we want to create a new Paged search.
 * The class does not need to have any content though.
 */
@JaxDTO
@EqualsAndHashCode(callSuper = true)
public class SearchJson extends PagedSearchJson<SearchColumns> {

    public SearchJson(
        @NonNull @Valid PageRequestJson paginationRequest,
        @NonNull List<SortJson<SearchColumns>> sorts,
        @NonNull List<FilterFieldJson<SearchColumns>> filters
    ) {
        super(paginationRequest, sorts, filters);
    }
    // Add any Kinder-specific search fields here
}
