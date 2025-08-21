import { DisplayConfigDTO } from './dto/display-config-dto';
import { DisplayList } from './dto/display-list';
import { filterListe } from './filter-functions';
import { paginate } from './paginate-functions';
import { sortList } from './sort-functions';

export function prepareDisplayList<T extends object>(
  rawList: T[] | undefined,
  displayConfig: DisplayConfigDTO<T>,
): DisplayList<T> {
  const result: DisplayList<T> = {
    pageNr: 1,
  };

  if (rawList) {
    // original size
    if (displayConfig.needsOriginalSize) {
      result.originalSize = rawList.length;
    }

    // 1. Filter
    const filteredEntries = filterListe(rawList, displayConfig.filter);
    result.filteredSize = filteredEntries.length;

    let resultList: T[] | undefined = filteredEntries;

    // 2. SortDTO
    if (displayConfig.sort) {
      resultList = sortList(resultList, displayConfig.sort);
    }

    result.filteredSortedList = resultList;

    // 3. Paginate
    if (displayConfig.pagination) {
      const { list, page } = paginate(resultList, displayConfig.pagination);
      resultList = list;
      result.pageNr = page;
    }

    result.displayedList = resultList;
  }

  return result;
}

