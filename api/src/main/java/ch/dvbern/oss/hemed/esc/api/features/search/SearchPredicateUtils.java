package ch.dvbern.oss.hemed.esc.api.features.search;

import java.util.List;
import java.util.Optional;

import ch.dvbern.oss.hemed.esc.api.features.search.json.SearchJson;
import ch.dvbern.oss.hemed.esc.api.features.search.types.SearchColumns;
import ch.dvbern.oss.hemed.esc.api.shared.pagination.FilterFieldJson;
import com.querydsl.core.types.Predicate;
import lombok.experimental.UtilityClass;
import org.checkerframework.checker.nullness.qual.NonNull;

@UtilityClass
public final class SearchPredicateUtils {

    public static boolean isSucheForPendenteAnlaesse(@NonNull SearchJson searchJson) {
        for (FilterFieldJson<SearchColumns> filter : searchJson.getFilters()) {
            if (filter.getColumnName() == SearchColumns.ANLASS_PENDENT) {
                return Boolean.parseBoolean(filter.getFilterValue());
            }
        }
        return false;
    }

    @NonNull
    public static Optional<FilterFieldJson<SearchColumns>> getFilterIfPresent(@NonNull SearchJson searchJson, @NonNull SearchColumns searchColumn) {
        for (FilterFieldJson<SearchColumns> filter : searchJson.getFilters()) {
            if (filter.getColumnName() == searchColumn) {
                return Optional.of(filter);
            }
        }
        return Optional.empty();
    }

    @NonNull
    public static List<SearchColumns> getSearchColumnsForAnlassPendentSuche(@NonNull SearchJson searchJson) {
        return searchJson.getFilters()
            .stream()
            .filter(searchColumnsFilterFieldJson -> searchColumnsFilterFieldJson.getColumnName().isCanBeUsedInAnlassPendentSuche())
            .map(FilterFieldJson::getColumnName)
            .toList();
    }

    @NonNull
    public static List<Predicate> getPredicatesNormalsuche(@NonNull SearchJson searchJson) {
        return searchJson.getFilters()
            .stream()
            .filter(searchColumnsFilterFieldJson -> !searchColumnsFilterFieldJson.getColumnName().isCanBeUsedInAnlassPendentSuche())
            .map(filter -> filter.getColumnName().createFilterTerm(filter))
            .toList();
    }
}
