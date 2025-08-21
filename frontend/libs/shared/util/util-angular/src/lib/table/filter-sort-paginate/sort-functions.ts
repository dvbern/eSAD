import {isNullish} from '@dvb/util-shared';
import {getByPath} from './colname-functions';
import {ColSortDTO} from './dto/col-sort-dto';
import {SortDTO} from './dto/sort-dto';

export function sortList<T extends object>(entries: T[] | undefined, sort: SortDTO<T>): T[] | undefined {
  let resultList: T[] | undefined = entries;
  if (sort?.colSorts?.length) {
    resultList = singleSortList(resultList, sort.colSorts[0]);
    if (sort.colSorts.length > 1) {
      throw new Error('Sortierung nach mehreren Spalten ist noch nicht umgesetzt');
    }
  }
  return resultList;
}

export function singleSortList<T extends object>(
  entries: T[] | undefined,
  { colName, sortDirection }: ColSortDTO<T>,
): T[] | undefined {
  if (!entries) {
    return undefined;
  }
  if (sortDirection === 'NONE' || colName === '') {
    return entries;
  } else {
    return [...entries].sort((a, b) => {
      const res = genericCompareTo(getByPath(a, colName), getByPath(b, colName));
      return sortDirection === 'ASC' ? res : -res;
    });
  }
}

// attention: this compare function does not include localeCompare
export const genericCompareTo = (v1: unknown, v2: unknown): number => {
  // both nullish -> equal
  if (isNullish(v1) && isNullish(v2)) {
    return 0;
  }

  // one nullish, other not -> nullish is the biggest value and comes at the end
  if (isNullish(v1)) {
    return 1;
  }
  if (isNullish(v2)) {
    return -1;
  }

  // compare values -> smaller is smaller, bigger is bigger

  // @ts-expect-error reason: this code was copied from another project, feel free to rewrite everything...
  if (v1 < v2) {
    return -1;

    // @ts-expect-error reason: this code was copied from another project, feel free to rewrite everything...
  } else if (v1 > v2) {
    return 1;
  } else {
    return 0;
  }
};
