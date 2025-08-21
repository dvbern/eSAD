import {isNotNullish, Leaves} from '@dvb/util-shared';
import {ColSortDTO} from './dto';
import {DisplayConfigDTO} from './dto/display-config-dto';
import {FilterCombinationMethod} from './dto/filter-combination-method';
import {FilterMethod} from './dto/filter-method';
import {PaginationDTO} from './dto/pagination-dto';
import {SortDTO} from './dto/sort-dto';

export function getFilter<T extends object>(key: string, displayConfig: DisplayConfigDTO<T>): string | undefined {
  return displayConfig?.filter?.filters?.find(each => each.filterName === key)?.filterValue;
}

export function updateSort<T extends object>(
  sort: SortDTO<T>,
  filterSortPage: DisplayConfigDTO<T>,
): DisplayConfigDTO<T> {
  return {
    ...filterSortPage,
    sort: sort,
    pagination: gotoFirstPage(filterSortPage.pagination), // jump to page 1 when changing the sort
  };
}

export function gotoFirstPage(pagination: PaginationDTO | undefined): PaginationDTO | undefined {
  if (!pagination) {
    return undefined;
  }
  return {
    // order matters!
    pageSize: pagination.pageSize,
    pageNr: 1, // jump to page 1 when changing the sort
  };
}

/*
replace the sort array by an array with the new colSort and discard any previous sorts
 */
export function updateSingleColSort<T extends object>(
  colSort: ColSortDTO<T>,
  filterSortPage: DisplayConfigDTO<T>,
): DisplayConfigDTO<T> {
  return updateSort(
    {
      colSorts: [colSort],
    },
    filterSortPage,
  );
}

export function findColSort<T extends object>(
  colName: Leaves<T>,
  displayConfig: DisplayConfigDTO<T>,
): ColSortDTO<T> | undefined {
  return displayConfig.sort?.colSorts?.find(colSort => colName === colSort.colName);
}

export function updatePage<T extends object>(
  page: PaginationDTO,
  filterSortPage: DisplayConfigDTO<T>,
): DisplayConfigDTO<T> {
  return {
    ...filterSortPage,
    pagination: page,
  };
}

export function updateFilter<T extends object>(
  key: Leaves<T>,
  value: string | undefined,
  method: FilterMethod,
  filterCombinationMethod: FilterCombinationMethod,
  filterSortPage: DisplayConfigDTO<T>,
): DisplayConfigDTO<T> {
  const existingOtherFilters = (filterSortPage.filter?.filters ?? []).filter(each => each.filterName !== key);
  const newFilters = [...existingOtherFilters];
  if (isNotNullish(value) && value !== '') {
    newFilters.push({
      filterName: key,
      filterValue: value,
      filterMethod: method,
      filterCombinationMethod: filterCombinationMethod,
    });
  }

  return {
    ...filterSortPage,
    filter: {
      filters: newFilters,
    },
    pagination: gotoFirstPage(filterSortPage.pagination),  // jump to page 1 when changing the filters
  };
}

export function clearFilters<T extends object>(
  filterSortPage: DisplayConfigDTO<T>,
): DisplayConfigDTO<T> {
  return {
    ...filterSortPage,
    filter: {
      filters: [],
    },
    pagination: gotoFirstPage(filterSortPage.pagination),  // jump to page 1 when changing the filters
  };
}
