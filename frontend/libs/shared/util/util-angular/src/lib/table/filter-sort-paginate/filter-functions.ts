import {isISODateString, isISODateTimeString} from '@dvb/data-access-types';
import {isNotNullish, isNullish} from '@dvb/util-shared';
import {format, isAfter, isBefore, isValid, parseISO} from 'date-fns';
import {getByPath} from './colname-functions';
import {FilterCombinationMethod} from './dto/filter-combination-method';
import {FilterDTO} from './dto/filter-dto';
import {FilterMethod, MULTI_FILTER_SEPARATOR} from './dto/filter-method';

export function filterListe<T extends object, F extends FilterDTO<T>>(liste: T[], suchkriterium: F | undefined): T[] {
  if (!suchkriterium) {
    return liste;
  }

  const filters = suchkriterium.filters ?? [];

  const filterList = liste.filter(row => {
    for (const filter of filters) {
      if (isNotNullish(filter.filterValue)) {
        const colValue = getByPath(row, filter.filterName);
        if (!colMatchesFilterMulti(colValue, filter.filterValue, filter.filterMethod, filter.filterCombinationMethod)) {
          return false;
        }
      }
    }
    return true;
  });

  return filterList;
}

export function colMatchesFilterMulti(
  colValue: unknown,
  filterValue: string,
  filterMethod: FilterMethod,
  multiValueMethod: FilterCombinationMethod,
): boolean {
  switch (multiValueMethod) {
    case 'SINGLE':
      return colMatchesFilter(colValue, filterValue, filterMethod);
    case 'AND': {
      const filterValues = filterValue.split(MULTI_FILTER_SEPARATOR);
      return filterValues.every(singleFilterValue => colMatchesFilter(colValue, singleFilterValue, filterMethod));
    }
    case 'OR': {
      const filterValues = filterValue.split(MULTI_FILTER_SEPARATOR);
      return filterValues.some(singleFilterValue => colMatchesFilter(colValue, singleFilterValue, filterMethod))
    }
    default:
      // exhaustiveness-check
      multiValueMethod satisfies never;
      return false;
  }
}

export function colMatchesFilter(colValue: unknown, filterValue: string, filterMethod: FilterMethod): boolean {
  switch (filterMethod) {
    case 'SUBSTRING':
      return colMatchesSubstringFilter(filterValue, colValue);

    case 'ENUM':
      return colMatchesEnumFilter(filterValue, colValue);

    case 'BOOL':
      return colMatchesBoolFilter(filterValue, colValue);

    case 'PHONE':
      return colMatchesPhoneFilter(filterValue, colValue);

    case 'DATERANGE':
      return colMatchesDaterangeFilter(filterValue, colValue);

    case 'DATE':
      return colMatchesDateFilter(filterValue, colValue);

    default:
      // exhaustiveness-check
      filterMethod satisfies never;
      return false;
  }
}

function colMatchesSubstringFilter(filterValue: string, colValue: unknown): boolean {
  if (typeof filterValue !== 'string') {
    return true;
  }

  if (colValue === undefined || colValue === null) {
    return false;
  }

  // AhvNummer sollte auch mit Substring gefiltert werden können.
  if (typeof colValue === 'number') {
    colValue = String(colValue);
  }

  if (typeof colValue !== 'string') {
    return false;
  }
  return colValue.toLowerCase().includes(filterValue.trim().toLowerCase());
}

function colMatchesEnumFilter(filterValue: string, colValue: unknown): boolean {

  if (colValue === undefined || colValue === null) {
    return false;
  }

  if (Array.isArray(colValue)) {
    return colValue.some(singleColValue => {
      return colMatchesEnumFilter(filterValue, singleColValue);
    })
  }

  if (typeof filterValue !== 'string') {
    return true;
  }

  if (typeof colValue !== 'string') {
    return false;
  }
  return colValue.toLowerCase() === filterValue.trim().toLowerCase();
}


function colMatchesBoolFilter(filterValue: string, colValue: unknown): boolean {
  /*if (typeof filterValue !== 'boolean') {
        return true;
    }

    if (colValue === undefined || colValue === null) {
        return false;
    }
    if (typeof colValue !== 'boolean') {
        return false;
    }*/
  return JSON.stringify(colValue) === filterValue;
}

function colMatchesPhoneFilter(filterValue: string, colValue: unknown): boolean {
  if (typeof filterValue !== 'string') {
    return true;
  }

  if (colValue === undefined || colValue === null) {
    return false;
  }
  if (typeof colValue !== 'string') {
    return false;
  }
  return colValue.replace(/\D/g, '').includes(filterValue.trim().replace(/\D/g, ''));
}

const FILTER_VON_BIS_SEPARATOR = ',';

function colMatchesDaterangeFilter(filterValue: string, colValue: unknown): boolean {
  if (!filterValue.includes(FILTER_VON_BIS_SEPARATOR)) {
    // invalid filter format -> match nothing
    return false;
  }
  const [filterFrom, filterTo] = filterValue.split(FILTER_VON_BIS_SEPARATOR).map(dateString => parseISO(dateString));

  if (!filterFrom && !filterTo) {
    // no filter -> match all
    return true;
  }

  if (isNullish(colValue)) {
    // no col value -> nope
    return false;
  }

  if (typeof colValue !== 'string') {
    return false;
  }

  if (!isISODateString(colValue) && !isISODateTimeString(colValue)) {
    return false;
  }

  const colDate = parseISO(colValue);
  if (!isValid(colDate)) {
    return false;
  }

  // von
  if (filterFrom && isValid(filterFrom)) {
    if (isBefore(colDate, filterFrom)) {
      return false;
    }
  }

  // bis
  if (filterTo && isValid(filterTo)) {
    if (isAfter(colDate, filterTo)) {
      return false;
    }
  }

  // ok
  return true;
}

function colMatchesDateFilter(filterValue: string, colValue: unknown): boolean {
  const filterDate = parseISO(filterValue);

  if (!filterDate || !isValid(filterDate)) {
    return true;
  }

  if (isNullish(colValue)) {
    // no col value -> nope
    return false;
  }

  if (typeof colValue !== 'string') {
    return false;
  }

  if (!isISODateString(colValue) && !isISODateTimeString(colValue)) {
    return false;
  }

  const colDate = parseISO(colValue);
  if (!isValid(colDate)) {
    return false;
  }

  if (format(colDate, 'dd.MM.yyyy') !== format(filterDate, 'dd.MM.yyyy')) {
    return false;
  }
  return true;
}
